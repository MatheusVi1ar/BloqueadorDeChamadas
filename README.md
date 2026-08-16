# 🛡️ Bloqueador de Chamadas & Limpeza de Correio de Voz

Um aplicativo Android moderno e leve desenvolvido em **Kotlin** e **Jetpack Compose**, projetado para interceptar e rejeitar automaticamente ligações de números desconhecidos (fora da agenda de contatos) e suprimir notificações persistentes de correio de voz / caixa postal da operadora.

---

## ✨ Funcionalidades Principais

- **🚫 Bloqueio Instantâneo de Desconhecidos:** Intercepta chamadas em segundo plano antes do aparelho começar a tocar, sem consumo contínuo de bateria.
- **🔇 Descarte Sem Caixa Postal:** Bloqueia chamadas sem forçar o sinal de ocupado tradicional que ativa o redirecionamento para o correio de voz da operadora.
- **🔔 Notificações Parametrizáveis:** Alerta opcional avisando qual número foi recusado e bloqueado.
- **📼 Limpeza Automática de Correio de Voz:** Detecta e remove instantaneamente o ícone/notificação persistente de recado/caixa postal da barra de status.
- **⚡ Inicialização Automática com o Sistema:** Funciona diretamente pelo subsistema de telecomunicações do Android, sem necessidade de manter o app aberto.
- **🎨 Interface Moderna em Jetpack Compose:** Navegação fluida por abas com Material 3 e controles simples de ligar/desligar.

---

## 🛠️ Tecnologias e APIs Utilizadas

- **Linguagem:** [Kotlin](https://kotlinlang.org/)
- **Interface:** [Jetpack Compose](https://developer.android.com/jetpack/compose) com Material 3
- **Triagem de Chamadas:** `CallScreeningService` & `RoleManager` (Android 10+ / API 29+)
- **Gerenciamento de Notificações:** `NotificationListenerService` & `NotificationChannel`
- **Consulta de Contatos:** `ContentResolver` com `ContactsContract.PhoneLookup`
- **Armazenamento de Preferências:** `SharedPreferences`

---

## 📱 Requisitos

- **Android 10.0 (API Level 29)** ou superior.
- Permissões do sistema:
  - `READ_CONTACTS` (Identificação de contatos salvos)
  - `POST_NOTIFICATIONS` (Alertas no Android 13+)
  - `BIND_SCREENING_SERVICE` (Triagem de chamadas)
  - `BIND_NOTIFICATION_LISTENER_SERVICE` (Escuta e descarte de notificações)

---

## 🚀 Como Executar o Projeto

1. **Clone o repositório:**
   ```bash
   git clone [https://github.com/seu-usuario/bloqueador-de-chamadas.git](https://github.com/seu-usuario/bloqueador-de-chamadas.git)
