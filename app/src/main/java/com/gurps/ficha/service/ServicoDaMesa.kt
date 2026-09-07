package com.gurps.ficha.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.gurps.ficha.MainActivity
import com.gurps.ficha.R

/**
 * **A sala continua de pé com o telefone no bolso** — lote MNA-8 do
 * `PLANO_MESA_NO_APP.md`.
 *
 * > *"até sair da mesa, mesmo que minimize, troque de aba dentro do app, abra
 * > qualquer outro app no telefone, TUDO fica conectado até dar SAIR"*
 *
 * ## 🔴 Por que um serviço, e não um jeito mais barato
 *
 * Trocar de aba já estava resolvido pelo MNA-1: a sala mora acima das telas.
 * Mas quando o aplicativo inteiro sai da frente — você abre o WhatsApp, ou a
 * tela apaga —, o Android **congela** o processo. A página para, o microfone
 * fecha, e os outros passam a ouvir silêncio. Você continua na lista, com o 🎤
 * aceso, e ninguém liga os dois fatos: parece que a mesa travou.
 *
 * Um serviço em primeiro plano é a única forma de dizer ao Android *"isto está
 * acontecendo agora, não congele"*. Ele **obriga** a uma notificação fixa na
 * barra — e isso é bom: é o que diz que a mesa está aberta, e é por onde se sai
 * dela sem procurar o aplicativo.
 *
 * É o que o Spotify e o Discord fazem, e pela mesma razão.
 *
 * ## 🟥 O que eu NÃO consigo provar daqui
 *
 * Este é o arquivo do plano que mais depende do aparelho, e o único que pode
 * falhar por razões que não controlo: fabricante que mata serviços, economia de
 * bateria agressiva, versão do Android.
 *
 * ⚠️ Por isso ele **nunca derruba a mesa**: toda tentativa de acender está num
 * `try`, e se falhar a sala continua funcionando **enquanto o aplicativo estiver
 * na frente** — que é o que ela fazia antes deste lote. O preço de o serviço
 * falhar é voltar ao MNA-1, e não perder tudo.
 *
 * ## ⚠️ O tipo `microphone`, e a armadilha do Android 14
 *
 * Do Android 14 em diante, um serviço de tipo `microphone` só acende se o
 * aplicativo **já tiver** a permissão `RECORD_AUDIO`. Acender antes disso lança
 * exceção.
 *
 * 🔴 E a ordem natural das coisas é exatamente a errada: você abre a aba (a sala
 * sobe) e só liga o microfone depois. Por isso o serviço é tentado **duas
 * vezes**: quando a sala abre, e outra vez assim que a permissão do microfone é
 * concedida. A segunda é a que costuma pegar.
 */
class ServicoDaMesa : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACAO_SAIR) {
            // 🔴 Sair pela notificação é sair de verdade. A janela é apagada na
            // linha principal — ela é uma view, e views não se tocam de outra.
            Handler(Looper.getMainLooper()).post {
                com.gurps.ficha.ui.features.mesa.SalaDaMesa.sair()
            }
            pararDeVez()
            return START_NOT_STICKY
        }

        criarOCanal()
        try {
            acenderDeVerdade()
        } catch (erro: Exception) {
            // ⚠️ Não dá para segurar o processo. A mesa continua a valer com o
            // aplicativo na frente; parar aqui é melhor do que ficar um serviço
            // meio de pé com uma notificação que mente.
            android.util.Log.w(ETIQUETA, "nao consegui acender: " + erro.message)
            pararDeVez()
            return START_NOT_STICKY
        }
        return START_NOT_STICKY
    }

    /**
     * ⚠️ `START_NOT_STICKY`, e não `START_STICKY`.
     *
     * 🔴 Se o sistema matar o processo, a janela da Mesa morre com ele. Um
     * serviço ressuscitado sozinho poria na barra uma notificação dizendo *"você
     * está na mesa"* com sala nenhuma atrás — e você só descobriria ao tocar
     * nela.
     */
    private fun acenderDeVerdade() {
        val aviso = aNotificacao()
        val temMicrofone = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && temMicrofone) {
            startForeground(ID_DO_AVISO, aviso, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            // Sem microfone concedido ainda. Do Android 14 em diante isto lança,
            // e quem chamou trata — ver o cabeçalho.
            startForeground(ID_DO_AVISO, aviso)
        }
    }

    private fun pararDeVez() {
        // ⚠️ `ServiceCompat`, e nao `ContextCompat`: parar um servico em
        // primeiro plano e coisa do servico, e nao do contexto.
        ServiceCompat.stopForeground(
            this, ServiceCompat.STOP_FOREGROUND_REMOVE
        )
        stopSelf()
    }

    private fun criarOCanal() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val gerente = getSystemService(NotificationManager::class.java) ?: return
        if (gerente.getNotificationChannel(CANAL) != null) return
        gerente.createNotificationChannel(
            NotificationChannel(
                CANAL, "Mesa Virtual",
                // ⚠️ `LOW`: sem som e sem tremer. Ela fica ali a sessão inteira,
                // e um aviso que apita seria um aviso que a pessoa desliga.
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Fica na barra enquanto você está na mesa."
                setShowBadge(false)
            }
        )
    }

    private fun aNotificacao(): Notification {
        val bandeiras = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val abrir = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            bandeiras
        )
        val sair = PendingIntent.getService(
            this, 1,
            Intent(this, ServicoDaMesa::class.java).setAction(ACAO_SAIR),
            bandeiras
        )

        return NotificationCompat.Builder(this, CANAL)
            .setContentTitle("Você está na mesa")
            .setContentText("A voz e a conversa continuam com o telefone no bolso.")
            .setSmallIcon(R.drawable.icon_app)
            .setContentIntent(abrir)
            // 🔴 O botão de sair mora AQUI, e não só dentro da página. Com o
            // aplicativo no bolso, esta notificação é a única coisa da mesa que
            // se vê — e uma sala de que não se sai sem ir procurá-la é uma sala
            // em que se fica sem querer.
            .addAction(0, "Sair da mesa", sair)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        private const val ETIQUETA = "ServicoDaMesa"
        private const val CANAL = "mesa_virtual"
        private const val ID_DO_AVISO = 4207
        const val ACAO_SAIR = "com.gurps.ficha.MESA_SAIR"

        /**
         * **Acende o serviço.** Pode ser chamado de mais — o Android trata um
         * `startForegroundService` repetido como o mesmo serviço.
         *
         * @return se a ordem foi dada. `false` não é o fim do mundo: a sala
         *   continua valendo com o aplicativo na frente.
         */
        fun acender(contexto: Context): Boolean = try {
            val intent = Intent(contexto, ServicoDaMesa::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                contexto.startForegroundService(intent)
            } else {
                contexto.startService(intent)
            }
            true
        } catch (erro: Exception) {
            android.util.Log.w(ETIQUETA, "nao consegui pedir o servico: " + erro.message)
            false
        }

        /** Apaga o serviço. ⚠️ Só o SAIR passa por aqui — ver o [SalaDaMesa]. */
        fun apagar(contexto: Context) {
            try {
                contexto.stopService(Intent(contexto, ServicoDaMesa::class.java))
            } catch (erro: Exception) {
                android.util.Log.w(ETIQUETA, "nao consegui parar: " + erro.message)
            }
        }
    }
}
