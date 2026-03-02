package cn.naivetomcat.hrt_tracker.utils

import cn.naivetomcat.hrt_tracker.data.MedicationPlan
import cn.naivetomcat.hrt_tracker.pk.DoseEvent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

/**
 * 用药方案预测工具
 * 根据用药方案生成未来的虚拟DoseEvent列表
 */
object MedicationPlanPredictor {

    /**
     * 根据用药方案生成未来的DoseEvent列表
     * @param plan 用药方案
     * @param fromDateTime 起始时间
     * @param daysAhead 预测多少天
     * @return 生成的DoseEvent列表
     */
    fun generateFutureEvents(
        plan: MedicationPlan,
        fromDateTime: LocalDateTime = LocalDateTime.now(),
        daysAhead: Int = 15
    ): List<DoseEvent> {
        if (!plan.isEnabled) {
            return emptyList()
        }

        val events = mutableListOf<DoseEvent>()
        val today = fromDateTime.toLocalDate()

        when (plan.scheduleType) {
            MedicationPlan.ScheduleType.DAILY -> {
                // 每天的每个时间点
                for (dayOffset in 0 until daysAhead) {
                    val date = today.plusDays(dayOffset.toLong())
                    
                    for (time in plan.timeOfDay) {
                        val dateTime = LocalDateTime.of(date, time)
                        
                        // 只添加未来的事件
                        if (dateTime.isAfter(fromDateTime)) {
                            events.add(createDoseEvent(plan, dateTime))
                        }
                    }
                }
            }

            MedicationPlan.ScheduleType.WEEKLY -> {
                // 每周特定几天的每个时间点
                for (dayOffset in 0 until daysAhead) {
                    val date = today.plusDays(dayOffset.toLong())
                    val dayOfWeek = date.dayOfWeek
                    
                    if (plan.daysOfWeek.contains(dayOfWeek)) {
                        for (time in plan.timeOfDay) {
                            val dateTime = LocalDateTime.of(date, time)
                            
                            // 只添加未来的事件
                            if (dateTime.isAfter(fromDateTime)) {
                                events.add(createDoseEvent(plan, dateTime))
                            }
                        }
                    }
                }
            }

            MedicationPlan.ScheduleType.CUSTOM -> {
                // 自定义间隔天数
                var dayOffset = 0
                
                while (dayOffset < daysAhead) {
                    val date = today.plusDays(dayOffset.toLong())
                    
                    for (time in plan.timeOfDay) {
                        val dateTime = LocalDateTime.of(date, time)
                        
                        // 只添加未来的事件
                        if (dateTime.isAfter(fromDateTime)) {
                            events.add(createDoseEvent(plan, dateTime))
                        }
                    }
                    
                    dayOffset += plan.intervalDays
                }
            }
        }

        return events.sortedBy { it.timeH }
    }

    /**
     * 根据用药方案和时间创建DoseEvent
     */
    private fun createDoseEvent(plan: MedicationPlan, dateTime: LocalDateTime): DoseEvent {
        val timeH = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 3600000.0
        
        return DoseEvent(
            id = UUID.randomUUID(),
            route = plan.route,
            timeH = timeH,
            doseMG = plan.doseMG,
            ester = plan.ester,
            extras = plan.extras
        )
    }

    /**
     * 为多个用药方案生成未来事件
     * @param plans 用药方案列表
     * @param fromDateTime 起始时间
     * @param daysAhead 预测多少天
     * @return 合并后的DoseEvent列表
     */
    fun generateFutureEventsForPlans(
        plans: List<MedicationPlan>,
        fromDateTime: LocalDateTime = LocalDateTime.now(),
        daysAhead: Int = 15
    ): List<DoseEvent> {
        return plans
            .filter { it.isEnabled }
            .flatMap { plan -> generateFutureEvents(plan, fromDateTime, daysAhead) }
            .sortedBy { it.timeH }
    }
}
