package cn.naivetomcat.hrt_tracker.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import cn.naivetomcat.hrt_tracker.pk.Route

/**
 * 根据给药途径获取对应的图标
 */
fun getRouteIcon(route: Route): ImageVector {
    return when (route) {
        Route.INJECTION -> Icons.Filled.Vaccines       // 注射
        Route.ORAL -> Icons.Filled.Medication          // 口服
        Route.SUBLINGUAL -> Icons.Filled.WaterDrop     // 舌下
        Route.GEL -> Icons.Filled.Soap                 // 凝胶
        Route.PATCH_APPLY -> Icons.Filled.AddBox       // 应用贴片
        Route.PATCH_REMOVE -> Icons.Filled.RemoveCircle // 移除贴片
    }
}

/**
 * 获取给药途径的显示名称
 */
fun getRouteDisplayName(route: Route): String {
    return when (route) {
        Route.INJECTION -> "肌肉注射"
        Route.ORAL -> "口服"
        Route.SUBLINGUAL -> "舌下含服"
        Route.GEL -> "透皮凝胶"
        Route.PATCH_APPLY -> "应用贴片"
        Route.PATCH_REMOVE -> "移除贴片"
    }
}
