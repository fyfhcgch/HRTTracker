package cn.naivetomcat.hrt_tracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.naivetomcat.hrt_tracker.data.MedicationPlan
import cn.naivetomcat.hrt_tracker.pk.*
import cn.naivetomcat.hrt_tracker.ui.theme.HRTTrackerTheme
import cn.naivetomcat.hrt_tracker.ui.utils.getRouteDisplayName
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 添加或编辑用药方案的底部弹窗
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MedicationPlanBottomSheet(
    showBottomSheet: Boolean,
    onDismiss: () -> Unit,
    onSave: (MedicationPlan) -> Unit,
    onDelete: ((UUID) -> Unit)? = null,
    planToEdit: MedicationPlan? = null
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    // 表单状态
    var name by remember(planToEdit, showBottomSheet) {
        mutableStateOf(planToEdit?.name ?: "")
    }

    var selectedRoute by remember(planToEdit, showBottomSheet) {
        mutableStateOf(planToEdit?.route ?: Route.INJECTION)
    }

    var selectedEster by remember(planToEdit, showBottomSheet) {
        mutableStateOf(planToEdit?.ester ?: Ester.EV)
    }

    var scheduleType by remember(planToEdit, showBottomSheet) {
        mutableStateOf(planToEdit?.scheduleType ?: MedicationPlan.ScheduleType.DAILY)
    }

    var doseMGText by remember(planToEdit, showBottomSheet) {
        mutableStateOf(planToEdit?.doseMG?.toString() ?: "")
    }

    var timeOfDay by remember(planToEdit, showBottomSheet) {
        mutableStateOf(planToEdit?.timeOfDay ?: listOf(LocalTime.of(9, 0)))
    }

    var daysOfWeek by remember(planToEdit, showBottomSheet) {
        mutableStateOf(planToEdit?.daysOfWeek ?: emptySet())
    }

    var intervalDays by remember(planToEdit, showBottomSheet) {
        mutableStateOf(planToEdit?.intervalDays?.toString() ?: "1")
    }

    // 舌下吸收等级
    var sublingualTier by remember(planToEdit, showBottomSheet) {
        mutableStateOf(
            planToEdit?.extras?.get(DoseEvent.ExtraKey.SUBLINGUAL_TIER)?.toInt()?.let { tier ->
                SublingualTier.values().getOrElse(tier) { SublingualTier.STANDARD }
            } ?: SublingualTier.STANDARD
        )
    }

    var showTimePicker by remember { mutableStateOf(false) }
    var timeIndexToEdit by remember { mutableStateOf(0) }

    // 根据给药途径过滤可用的酯类
    val availableEsters = remember(selectedRoute) {
        getAvailableEstersForRoute(selectedRoute)
    }

    // 如果当前选择的酯类不在可用列表中，自动切换到第一个
    LaunchedEffect(selectedRoute, availableEsters) {
        if (selectedEster !in availableEsters) {
            selectedEster = availableEsters.firstOrNull() ?: Ester.E2
        }
    }

    // 验证表单
    val isValid = remember(name, doseMGText, timeOfDay, scheduleType, daysOfWeek, intervalDays) {
        name.isNotBlank() &&
                doseMGText.toDoubleOrNull() != null &&
                doseMGText.toDouble() > 0 &&
                timeOfDay.isNotEmpty() &&
                (scheduleType != MedicationPlan.ScheduleType.WEEKLY || daysOfWeek.isNotEmpty()) &&
                (scheduleType != MedicationPlan.ScheduleType.CUSTOM || (intervalDays.toIntOrNull() ?: 0) > 0)
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 标题
                Text(
                    text = if (planToEdit != null) "编辑用药方案" else "添加用药方案",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // 方案名称
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("方案名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 给药途径选择
                RouteSelectionSection(
                    selectedRoute = selectedRoute,
                    onRouteSelected = { selectedRoute = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 药物类型选择
                EsterSelectionSection(
                    selectedEster = selectedEster,
                    availableEsters = availableEsters,
                    onEsterSelected = { selectedEster = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 剂量输入
                OutlinedTextField(
                    value = doseMGText,
                    onValueChange = { doseMGText = it },
                    label = { Text("剂量 (mg)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 舌下吸收等级（仅舌下途径显示）
                AnimatedVisibility(visible = selectedRoute == Route.SUBLINGUAL) {
                    Column {
                        SublingualTierSelector(
                            selectedTier = sublingualTier,
                            onTierSelected = { sublingualTier = it }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(0.dp))

                // 给药周期类型选择
                ScheduleTypeSection(
                    selectedType = scheduleType,
                    onTypeSelected = { scheduleType = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 根据周期类型显示不同的配置
                when (scheduleType) {
                    MedicationPlan.ScheduleType.DAILY -> {
                        // 每天：只需要选择时间
                    }
                    MedicationPlan.ScheduleType.WEEKLY -> {
                        // 每周：选择星期几
                        DaysOfWeekSection(
                            selectedDays = daysOfWeek,
                            onDayToggled = { day ->
                                daysOfWeek = if (daysOfWeek.contains(day)) {
                                    daysOfWeek - day
                                } else {
                                    daysOfWeek + day
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    MedicationPlan.ScheduleType.CUSTOM -> {
                        // 自定义：输入间隔天数
                        OutlinedTextField(
                            value = intervalDays,
                            onValueChange = { intervalDays = it },
                            label = { Text("间隔天数") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // 时间选择
                TimeOfDaySection(
                    times = timeOfDay,
                    onAddTime = {
                        timeOfDay = timeOfDay + LocalTime.of(9, 0)
                    },
                    onRemoveTime = { index ->
                        if (timeOfDay.size > 1) {
                            timeOfDay = timeOfDay.filterIndexed { i, _ -> i != index }
                        }
                    },
                    onEditTime = { index ->
                        timeIndexToEdit = index
                        showTimePicker = true
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 删除按钮（仅编辑时显示）
                    if (planToEdit != null && onDelete != null) {
                        OutlinedButton(
                            onClick = {
                                onDelete(planToEdit.id)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("删除")
                        }
                    }

                    // 取消按钮
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }

                    // 保存按钮
                    Button(
                        onClick = {
                            val doseMG = doseMGText.toDoubleOrNull() ?: 0.0
                            
                            // 构建extras
                            val extras = mutableMapOf<DoseEvent.ExtraKey, Double>()
                            if (selectedRoute == Route.SUBLINGUAL) {
                                extras[DoseEvent.ExtraKey.SUBLINGUAL_TIER] = sublingualTier.ordinal.toDouble()
                            }
                            
                            val plan = MedicationPlan(
                                id = planToEdit?.id ?: UUID.randomUUID(),
                                name = name,
                                route = selectedRoute,
                                ester = selectedEster,
                                doseMG = doseMG,
                                scheduleType = scheduleType,
                                timeOfDay = timeOfDay,
                                daysOfWeek = daysOfWeek,
                                intervalDays = intervalDays.toIntOrNull() ?: 1,
                                isEnabled = planToEdit?.isEnabled ?: true,
                                extras = extras,
                                createdAt = planToEdit?.createdAt ?: System.currentTimeMillis()
                            )
                            onSave(plan)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isValid
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }

    // 时间选择器
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = timeOfDay.getOrNull(timeIndexToEdit)?.hour ?: 9,
            initialMinute = timeOfDay.getOrNull(timeIndexToEdit)?.minute ?: 0,
            is24Hour = true
        )

        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                timeOfDay = timeOfDay.mapIndexed { index, time ->
                    if (index == timeIndexToEdit) newTime else time
                }
                showTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

/**
 * 给药途径选择组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteSelectionSection(
    selectedRoute: Route,
    onRouteSelected: (Route) -> Unit
) {
    Column {
        Text(
            text = "给药途径",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            Route.values().forEachIndexed { index, route ->
                if (route != Route.PATCH_REMOVE) { // 不显示移除贴片选项
                    SegmentedButton(
                        selected = selectedRoute == route,
                        onClick = { onRouteSelected(route) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = Route.values().size - 1
                        )
                    ) {
                        Text(
                            text = getRouteDisplayName(route),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

/**
 * 药物类型选择组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EsterSelectionSection(
    selectedEster: Ester,
    availableEsters: List<Ester>,
    onEsterSelected: (Ester) -> Unit
) {
    Column {
        Text(
            text = "药物类型",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            availableEsters.forEachIndexed { index, ester ->
                SegmentedButton(
                    selected = selectedEster == ester,
                    onClick = { onEsterSelected(ester) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = availableEsters.size
                    )
                ) {
                    Text(
                        text = ester.name,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/**
 * 给药周期类型选择组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleTypeSection(
    selectedType: MedicationPlan.ScheduleType,
    onTypeSelected: (MedicationPlan.ScheduleType) -> Unit
) {
    Column {
        Text(
            text = "给药周期",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            val types = MedicationPlan.ScheduleType.values()
            types.forEachIndexed { index, type ->
                SegmentedButton(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = types.size
                    )
                ) {
                    Text(
                        text = when (type) {
                            MedicationPlan.ScheduleType.DAILY -> "每天"
                            MedicationPlan.ScheduleType.WEEKLY -> "每周"
                            MedicationPlan.ScheduleType.CUSTOM -> "自定义"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/**
 * 星期选择组件
 */
@Composable
private fun DaysOfWeekSection(
    selectedDays: Set<DayOfWeek>,
    onDayToggled: (DayOfWeek) -> Unit
) {
    Column {
        Text(
            text = "星期选择",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val days = listOf(
                DayOfWeek.MONDAY to "一",
                DayOfWeek.TUESDAY to "二",
                DayOfWeek.WEDNESDAY to "三",
                DayOfWeek.THURSDAY to "四",
                DayOfWeek.FRIDAY to "五",
                DayOfWeek.SATURDAY to "六",
                DayOfWeek.SUNDAY to "日"
            )

            days.forEach { (day, label) ->
                FilterChip(
                    selected = selectedDays.contains(day),
                    onClick = { onDayToggled(day) },
                    label = { Text(label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 时间选择组件
 */
@Composable
private fun TimeOfDaySection(
    times: List<LocalTime>,
    onAddTime: () -> Unit,
    onRemoveTime: (Int) -> Unit,
    onEditTime: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "给药时间",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onAddTime) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加时间"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        times.forEachIndexed { index, time ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedCard(
                    onClick = { onEditTime(index) },
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                if (times.size > 1) {
                    IconButton(onClick = { onRemoveTime(index) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "删除时间"
                        )
                    }
                }
            }

            if (index < times.size - 1) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * 时间选择器对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确定")
            }
        },
        text = { content() }
    )
}

/**
 * 舌下吸收等级选择器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SublingualTierSelector(
    selectedTier: SublingualTier,
    onTierSelected: (SublingualTier) -> Unit
) {
    Column {
        Text(
            text = "舌下吸收等级",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SublingualTier.values().forEachIndexed { index, tier ->
                SegmentedButton(
                    selected = selectedTier == tier,
                    onClick = { onTierSelected(tier) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = SublingualTier.values().size
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = getSublingualTierName(tier),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selectedTier == tier) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = getSublingualTierDescription(tier),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

/**
 * 获取舌下吸收等级名称
 */
private fun getSublingualTierName(tier: SublingualTier): String {
    return when (tier) {
        SublingualTier.QUICK -> "快速"
        SublingualTier.CASUAL -> "随意"
        SublingualTier.STANDARD -> "标准"
        SublingualTier.STRICT -> "严格"
    }
}

/**
 * 获取舌下吸收等级描述
 */
private fun getSublingualTierDescription(tier: SublingualTier): String {
    return when (tier) {
        SublingualTier.QUICK -> "~2分钟"
        SublingualTier.CASUAL -> "~5分钟"
        SublingualTier.STANDARD -> "~10分钟"
        SublingualTier.STRICT -> "~15分钟"
    }
}

/**
 * 根据给药途径获取可用的酯类列表
 */
private fun getAvailableEstersForRoute(route: Route): List<Ester> {
    return when (route) {
        Route.INJECTION -> listOf(Ester.E2, Ester.EB, Ester.EV, Ester.EC, Ester.EN)
        Route.ORAL -> listOf(Ester.E2, Ester.EV)
        Route.SUBLINGUAL -> listOf(Ester.E2, Ester.EV)
        Route.GEL -> listOf(Ester.E2)
        Route.PATCH_APPLY -> listOf(Ester.E2)
        Route.PATCH_REMOVE -> listOf(Ester.E2)
    }
}

/**
 * 预览
 */
@Preview(showBackground = true)
@Composable
private fun MedicationPlanBottomSheetPreview() {
    HRTTrackerTheme {
        // 预览占位
        Box(modifier = Modifier.fillMaxSize()) {
            Text("用药方案底部弹窗预览")
        }
    }
}
