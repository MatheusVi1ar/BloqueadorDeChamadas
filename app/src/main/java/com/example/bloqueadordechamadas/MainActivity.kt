package com.example.bloqueadordechamadas // Mantenha o seu pacote original

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppPrincipal()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPrincipal() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Call, contentDescription = "Bloqueador") },
                    label = { Text("Bloqueador") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Correio de Voz") },
                    label = { Text("Correio de Voz") }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (selectedTab == 0) {
                AbaBloqueador()
            } else {
                AbaCorreioDeVoz()
            }
        }
    }
}

@Composable
fun AbaBloqueador() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("bloqueador_prefs", Context.MODE_PRIVATE) }

    var isProtecaoAtiva by remember { mutableStateOf(prefs.getBoolean("bloqueio_ativo", true)) }
    var isNotificacaoAtiva by remember { mutableStateOf(prefs.getBoolean("notificar_bloqueio", true)) }

    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            isProtecaoAtiva = true
            prefs.edit().putBoolean("bloqueio_ativo", true).apply()
            Toast.makeText(context, "Proteção ativada!", Toast.LENGTH_SHORT).show()
        } else {
            isProtecaoAtiva = false
            prefs.edit().putBoolean("bloqueio_ativo", false).apply()
        }
    }

    fun solicitarPapelDeBloqueador() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
            ) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                roleLauncher.launch(intent)
            } else {
                isProtecaoAtiva = true
                prefs.edit().putBoolean("bloqueio_ativo", true).apply()
                Toast.makeText(context, "Proteção ativada!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val contactsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) solicitarPapelDeBloqueador()
        else {
            isProtecaoAtiva = false
            prefs.edit().putBoolean("bloqueio_ativo", false).apply()
        }
    }

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isNotificacaoAtiva = isGranted
        prefs.edit().putBoolean("notificar_bloqueio", isGranted).apply()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Bloqueador de Chamadas", fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(28.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Bloquear Desconhecidos", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text("Rejeita ligações fora dos contatos", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = isProtecaoAtiva,
                onCheckedChange = { ligar ->
                    if (ligar) {
                        val temContatos = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                        if (!temContatos) contactsLauncher.launch(Manifest.permission.READ_CONTACTS)
                        else solicitarPapelDeBloqueador()
                    } else {
                        isProtecaoAtiva = false
                        prefs.edit().putBoolean("bloqueio_ativo", false).apply()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Avisar sobre Bloqueio", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text("Mostra aviso quando bloquear", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = isNotificacaoAtiva,
                onCheckedChange = { ligar ->
                    if (ligar && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        isNotificacaoAtiva = ligar
                        prefs.edit().putBoolean("notificar_bloqueio", ligar).apply()
                    }
                }
            )
        }
    }
}

@Composable
fun AbaCorreioDeVoz() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("bloqueador_prefs", Context.MODE_PRIVATE) }

    var isRemoverAtivo by remember { mutableStateOf(prefs.getBoolean("remover_correio_voz", true)) }

    fun temAcessoNotificacoes(): Boolean {
        val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        val myService = ComponentName(context, VoicemailDismissService::class.java).flattenToString()
        return enabledListeners != null && enabledListeners.contains(myService)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Limpeza de Correio de Voz", fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Apaga automaticamente o ícone persistente de caixa postal/recado assim que a operadora tentar exibir.",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Ocultar Automaticamente", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text("Cancela notificações de correio de voz", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = isRemoverAtivo,
                onCheckedChange = { ligar ->
                    isRemoverAtivo = ligar
                    prefs.edit().putBoolean("remover_correio_voz", ligar).apply()
                }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Botão para levar o usuário à tela de concessão de acesso do Android
        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                context.startActivity(intent)
            }
        ) {
            Text("Autorizar Acesso a Notificações")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (temAcessoNotificacoes()) "Status: Autorizado no Sistema ✅" else "Status: Não autorizado (Clique no botão acima)",
            fontSize = 12.sp,
            color = if (temAcessoNotificacoes()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}