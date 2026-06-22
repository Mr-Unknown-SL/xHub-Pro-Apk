package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppViewModel
import com.example.ui.PinAuthState
import com.example.ui.components.DashboardScreen
import com.example.ui.components.PinLockScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val CHANNEL_ID = "xhub_panic_channel"
    private val NOTIFICATION_ID = 2691

    override fun onCreate(savedInstanceState: Bundle?) {
        // Intercept panic exit actions before UI spins up
        handlePanicIntent(intent)
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel()

        setContent {
            val viewModel: AppViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            MyApplicationTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handlePanicIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // App is visible/active, show quick exit panel notification
        showPanicNotification()
    }

    override fun onPause() {
        super.onPause()
        // App is backgrounded, disappear from tray to preserve perfect discretion
        cancelPanicNotification()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelPanicNotification()
    }

    private fun handlePanicIntent(intent: Intent?) {
        if (intent != null && intent.getBooleanExtra("panic_exit", false)) {
            cancelPanicNotification()
            finishAndRemoveTask()
            // Clean process layout from memory immediately
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(0)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "xHub Pro Security Channel"
            val descriptionText = "Fast emergency locker for your active private browser sessions"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
                setSound(null, null) // Silent/discrete notification by default
                enableVibration(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showPanicNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("panic_exit", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Discrete generic icon (Off padlock standard vector indicator)
        val androidLockIcon = android.R.drawable.ic_lock_power_off

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(androidLockIcon)
            .setContentTitle("xHub Pro Active Session")
            .setContentText("Emergency Protocol: Tap to instantly lock & kill session!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun cancelPanicNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}

@Composable
fun AppNavigation(viewModel: AppViewModel) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val pinBuffer by viewModel.pinBuffer.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val activeUrl by viewModel.activeUrl.collectAsStateWithLifecycle()
    val activeName by viewModel.activeName.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val showWelcomeDialog by viewModel.showWelcomeDialog.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val activeLockStyle by viewModel.activeLockStyle.collectAsStateWithLifecycle()
    val patternBuffer by viewModel.patternBuffer.collectAsStateWithLifecycle()

    if (authState != PinAuthState.Unlocked) {
        PinLockScreen(
            authState = authState,
            pinBuffer = pinBuffer,
            errorMessage = errorMessage,
            onDigitInput = { viewModel.inputDigit(it) },
            onBackspace = { viewModel.inputBackspace() },
            onClear = { viewModel.clearInput() },
            lockStyle = activeLockStyle,
            patternBuffer = patternBuffer,
            onPatternNodeAdded = { viewModel.addPatternNode(it) },
            onPatternSubmit = { viewModel.submitPattern() },
            onPatternClear = { viewModel.clearPattern() },
            onSelectStyle = { viewModel.selectLockStyle(it) },
            onProceedWelcome = { viewModel.proceedFromWelcome() },
            onBackToStyles = { viewModel.reverseToChooseStyle() },
            onBiometricSuccess = { viewModel.verifyBiometricSuccess() }
        )
    } else {
        DashboardScreen(
            activeUrl = activeUrl,
            activeName = activeName,
            bookmarks = bookmarks,
            showWelcomeDialog = showWelcomeDialog,
            themeMode = themeMode,
            onUrlSelected = { name, url -> viewModel.selectUrl(name, url) },
            onDismissWelcome = { viewModel.dismissWelcome() },
            onResetPin = { viewModel.resetPin() },
            onThemeChange = { mode -> viewModel.setThemeMode(mode) }
        )
    }
}
