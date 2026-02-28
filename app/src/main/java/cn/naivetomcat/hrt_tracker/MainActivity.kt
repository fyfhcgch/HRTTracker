package cn.naivetomcat.hrt_tracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import cn.naivetomcat.hrt_tracker.pk.DoseEvent
import cn.naivetomcat.hrt_tracker.pk.Ester
import cn.naivetomcat.hrt_tracker.pk.Route
import cn.naivetomcat.hrt_tracker.ui.screens.MedicationRecordsScreen
import cn.naivetomcat.hrt_tracker.ui.theme.HRTTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HRTTrackerTheme {
                val events = remember {
                    mutableStateListOf<DoseEvent>().apply {
                        // 添加一些示例数据
                        val currentTime = System.currentTimeMillis() / 3600000.0
                        add(
                            DoseEvent(
                                route = Route.INJECTION,
                                timeH = currentTime - 168.0,  // 7天前
                                doseMG = 5.0,
                                ester = Ester.EV
                            )
                        )
                        add(
                            DoseEvent(
                                route = Route.ORAL,
                                timeH = currentTime - 12.0,  // 12小时前
                                doseMG = 2.0,
                                ester = Ester.E2
                            )
                        )
                        add(
                            DoseEvent(
                                route = Route.SUBLINGUAL,
                                timeH = currentTime - 2.0,  // 2小时前
                                doseMG = 1.0,
                                ester = Ester.E2,
                                extras = mapOf(DoseEvent.ExtraKey.SUBLINGUAL_TIER to 2.0)
                            )
                        )
                    }
                }

                MedicationRecordsScreen(
                    events = events,
                    onEventClick = { event ->
                        Toast.makeText(
                            this,
                            "点击了用药记录: ${event.id}",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onAddClick = {
                        // 添加新的用药记录示例
                        val currentTime = System.currentTimeMillis() / 3600000.0
                        events.add(
                            DoseEvent(
                                route = Route.ORAL,
                                timeH = currentTime,
                                doseMG = 2.0,
                                ester = Ester.E2
                            )
                        )
                        Toast.makeText(
                            this,
                            "已添加新的用药记录",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }
}