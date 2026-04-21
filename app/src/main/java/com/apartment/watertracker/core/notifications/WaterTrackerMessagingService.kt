package com.apartment.watertracker.core.notifications

import com.apartment.watertracker.domain.repository.AuthRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WaterTrackerMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var authRepository: AuthRepository

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        scope.launch {
            authRepository.registerFcmToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        val title = message.notification?.title ?: message.data["title"] ?: "Water Tracker"
        val body = message.notification?.body ?: message.data["body"] ?: "New activity recorded"
        val type = message.data["type"]

        if (type == "NEW_BID") {
            NotificationHelper.showBidNotification(this, title, body)
        } else {
            NotificationHelper.showTankerArrivalNotification(this, title, body)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
