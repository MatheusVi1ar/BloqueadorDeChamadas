package com.example.bloqueadordechamadas // Mantenha seu package original

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class UnknownCallBlockerService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val prefs = getSharedPreferences("bloqueador_prefs", Context.MODE_PRIVATE)
        val isAtivado = prefs.getBoolean("bloqueio_ativo", true)
        val deveNotificar = prefs.getBoolean("notificar_bloqueio", true)

        if (!isAtivado) {
            val response = CallResponse.Builder().build()
            respondToCall(callDetails, response)
            return
        }

        val rawHandle: Uri? = callDetails.handle
        val phoneNumber = rawHandle?.schemeSpecificPart

        if (phoneNumber.isNullOrEmpty() || !isContactSaved(this, phoneNumber)) {
            val numeroFormatado = phoneNumber ?: "Número Privado / Desconhecido"

            // Se o usuário deixou ativada a notificação nas configurações, exibe o aviso
            if (deveNotificar) {
                enviarNotificacaoBloqueio(numeroFormatado)
            }

            val response = CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(false)
                .setSkipNotification(true) // Suprime a notificação padrão de chamada perdida da Samsung
                .setSkipCallLog(false)
                .build()

            respondToCall(callDetails, response)
        } else {
            val response = CallResponse.Builder().build()
            respondToCall(callDetails, response)
        }
    }

    private fun enviarNotificacaoBloqueio(numero: String) {
        val channelId = "canal_bloqueador_chamadas"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Cria o canal de notificação exigido pelo Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Chamadas Bloqueadas",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifica quando um número desconhecido for bloqueado"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Verifica permissão no Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_missed_call) // Ícone nativo de chamada perdida
            .setContentTitle("Chamada Desconhecida Bloqueada")
            .setContentText("O número $numero foi recusado e bloqueado.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        // Usa o timestamp atual para que cada ligação gere uma notificação separada
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun isContactSaved(context: Context, number: String): Boolean {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup._ID)

        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            return cursor.count > 0
        }
        return false
    }
}