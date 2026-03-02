package cn.naivetomcat.hrt_tracker.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 用药提醒广播接收器
 * 接收定时提醒的广播并显示通知
 */
class MedicationReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_PLAN_ID = "plan_id"
        const val EXTRA_PLAN_NAME = "plan_name"
        const val EXTRA_PLAN_DESCRIPTION = "plan_description"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val planId = intent.getStringExtra(EXTRA_PLAN_ID) ?: return
        val planName = intent.getStringExtra(EXTRA_PLAN_NAME) ?: "用药提醒"
        val planDescription = intent.getStringExtra(EXTRA_PLAN_DESCRIPTION) ?: ""
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)

        // 发送通知
        val notificationHelper = NotificationHelper(context)
        notificationHelper.sendMedicationReminder(
            planId = planId,
            planName = planName,
            description = planDescription,
            notificationId = notificationId
        )
    }
}
