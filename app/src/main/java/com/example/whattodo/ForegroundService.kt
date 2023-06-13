package com.example.whattodo

import android.Manifest
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.whattodo.manager.Persistence.PersistenceService
import kotlinx.coroutines.*

class ForegroundService() : Service() {
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "channel_id"
        private const val NOTIFICATION_ID = 1
        // 알림 간격
        private const val INTERVAL_MILLIS = 60 * 1000L // 1분
        // 알림 대상이 되는 우선도 기준
        private const val PRIORITY_CRITERIA = 30
    }

    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(this)
    }

    private val alarmManager: AlarmManager by lazy {
        getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            intent.setClass(this@ForegroundService, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this@ForegroundService, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            println("start make noti")

            var todoCount = 0
            var todoList: ArrayList<ToDo>

            CoroutineScope(Dispatchers.IO).launch {
                todoList = PersistenceService.share.getAllTodo()
                withContext(Dispatchers.Main)
                {
                    // 이 부분에서 todoList로 반복문 돌리면서 우선도 계산 -> 우선도가 특정 값 이상인 ToDo 객체만 모아서 알림 쏴주기
                    // 알림 내용 예시: 우선도가 **이상인 할 일이 4개가 있습니다.
                    // 특정 우선도 값을 사용자가 직접 설정할 수 있도록 해도 좋을듯
                    var adapter = MyAdapter(todoList)
                    adapter.CalcItemsPrority()

                    for (todo in adapter.items) {
                        if (todo.priority > PRIORITY_CRITERIA) {
                            todoCount += 1
                        }
                    }
                    var priorityFlag = NotificationCompat.PRIORITY_HIGH
                    if (todoCount == 0) {
                        priorityFlag = NotificationCompat.PRIORITY_MIN
                    }

                    var noti =
                        NotificationCompat.Builder(this@ForegroundService, NOTIFICATION_CHANNEL_ID)
                            .setContentTitle("What To Do 알림이 도착했습니다.")
                            .setContentText("우선도가 높은 일정이 ${todoCount}개 있습니다.")
                            .setSmallIcon(R.drawable.baseline_today_24)
                            .setContentIntent(pendingIntent)
                            .setPriority(priorityFlag)
                            .build()
                    if (ActivityCompat.checkSelfPermission(
                            this@ForegroundService,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                    }
                    notificationManager.notify(NOTIFICATION_ID, noti)
                }
            }
            scheduleNextAlarm()
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        registerAlarmReceiver()
        scheduleNextAlarm()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val intent2 = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this@ForegroundService, 0, intent2, PendingIntent.FLAG_IMMUTABLE)

        println("start make noti")

        var todoCount = 0
        var todoList: ArrayList<ToDo>

        CoroutineScope(Dispatchers.IO).launch {
        todoList = PersistenceService.share.getAllTodo()
        withContext(Dispatchers.Main)
        {
            // 이 부분에서 todoList로 반복문 돌리면서 우선도 계산 -> 우선도가 특정 값 이상인 ToDo 객체만 모아서 알림 쏴주기
            // 알림 내용 예시: 우선도가 **이상인 할 일이 4개가 있습니다.
            // 특정 우선도 값을 사용자가 직접 설정할 수 있도록 해도 좋을듯
            var adapter = MyAdapter(todoList)
            adapter.CalcItemsPrority()

            for (todo in adapter.items) {
                if (todo.priority > PRIORITY_CRITERIA) {
                    todoCount += 1
                }
            }
            var priorityFlag = NotificationCompat.PRIORITY_HIGH
            if (todoCount == 0) {
                priorityFlag = NotificationCompat.PRIORITY_MIN
            }

            var noti =
                NotificationCompat.Builder(this@ForegroundService, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("What To Do 알림이 도착했습니다.")
                    .setContentText("우선도가 높은 일정이 ${todoCount}개 있습니다.")
                    .setSmallIcon(R.drawable.baseline_today_24)
                    .setContentIntent(pendingIntent)
                    .setPriority(priorityFlag)
                    .build()
            if (ActivityCompat.checkSelfPermission(
                    this@ForegroundService,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
            startForeground(NOTIFICATION_ID, noti)
        }}


        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterAlarmReceiver()
    }

    private fun createNotificationChannel() {
            val name = "My Channel"
            val descriptionText = "Channel description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            notificationManager.createNotificationChannel(channel)
    }

    private fun registerAlarmReceiver() {
        registerReceiver(broadcastReceiver, IntentFilter("com.example.ALARM_ACTION"))
    }

    private fun unregisterAlarmReceiver() {
        unregisterReceiver(broadcastReceiver)
    }

    private fun scheduleNextAlarm() {
        val intent = Intent("com.example.ALARM_ACTION")
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val startTimeMillis = System.currentTimeMillis() + INTERVAL_MILLIS
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            startTimeMillis,
            pendingIntent
        )
    }
}