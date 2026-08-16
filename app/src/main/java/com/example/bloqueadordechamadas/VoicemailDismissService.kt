package com.example.bloqueadordechamadas

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class VoicemailDismissService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (sbn == null) return

        val prefs = getSharedPreferences("bloqueador_prefs", MODE_PRIVATE)
        val autoDismissVoicemail = prefs.getBoolean("remover_correio_voz", true)

        if (!autoDismissVoicemail) return

        val packageName = sbn.packageName ?: ""
        val extras = sbn.notification.extras
        val title = extras.getString("android.title", "").lowercase()
        val text = extras.getCharSequence("android.text", "").toString().lowercase()

        // Verifica se a notificação é do sistema de telefonia ou discador
        val isTelephonyPackage = packageName.contains("telecom") ||
                packageName.contains("phone") ||
                packageName.contains("telephony")

        // Palavras-chave típicas de caixa postal no Brasil (Samsung, Vivo, Claro, TIM)
        val hasVoicemailKeyword = title.contains("correio de voz") ||
                title.contains("recado") ||
                title.contains("caixa postal") ||
                title.contains("voicemail") ||
                text.contains("correio de voz") ||
                text.contains("recado") ||
                text.contains("caixa postal") ||
                text.contains("voicemail")

        if (isTelephonyPackage && hasVoicemailKeyword) {
            // Cancela e apaga a notificação da barra de status no mesmo instante
            cancelNotification(sbn.key)
        }
    }
}