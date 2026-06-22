package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BookmarkEntity
import com.example.data.BookmarkRepository
import com.example.data.PrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

sealed class PinAuthState {
    object SetupWelcome : PinAuthState()     // Welcome page with Creator/Mr.Unknown note & Next button
    object SetupChooseStyle : PinAuthState() // Panel to select Lock type: 4-pin, 6-pin, pattern, biometrics
    object SetupLock : PinAuthState()        // Input code/pattern first time
    object SetupConfirmPin : PinAuthState()  // Input code/pattern second time to confirm
    object RequestPin : PinAuthState()       // Login locker view using configured lock type
    object Unlocked : PinAuthState()         // Passed and active inside the private browser
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val prefsManager = PrefsManager(application)
    private val database = AppDatabase.getDatabase(application)
    private val repository = BookmarkRepository(database.bookmarkDao())

    // Bookmarks UI state from Database Flow
    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current Auth Status
    private val _authState = MutableStateFlow<PinAuthState>(PinAuthState.RequestPin)
    val authState: StateFlow<PinAuthState> = _authState.asStateFlow()

    // Configured Lock style ("4_pin", "6_pin", "pattern", "biometrics")
    private val _activeLockStyle = MutableStateFlow(prefsManager.getLockStyle())
    val activeLockStyle: StateFlow<String> = _activeLockStyle.asStateFlow()

    // Passcode digit buffers
    private val _pinBuffer = MutableStateFlow("")
    val pinBuffer: StateFlow<String> = _pinBuffer.asStateFlow()

    private val _tempSetupPin = MutableStateFlow("")
    val tempSetupPin: StateFlow<String> = _tempSetupPin.asStateFlow()

    // Interactive Pattern Buffers
    private val _patternBuffer = MutableStateFlow<List<Int>>(emptyList())
    val patternBuffer: StateFlow<List<Int>> = _patternBuffer.asStateFlow()

    private val _tempSetupPattern = MutableStateFlow<List<Int>>(emptyList())
    val tempSetupPattern: StateFlow<List<Int>> = _tempSetupPattern.asStateFlow()

    // Feedback messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Active Website Parameters
    private val _activeUrl = MutableStateFlow("https://www.pornhub.com")
    val activeUrl: StateFlow<String> = _activeUrl.asStateFlow()

    private val _activeName = MutableStateFlow("Pornhub")
    val activeName: StateFlow<String> = _activeName.asStateFlow()

    // Welcome dialogue tracker
    private val _showWelcomeDialog = MutableStateFlow(false)
    val showWelcomeDialog: StateFlow<Boolean> = _showWelcomeDialog.asStateFlow()

    // App theme ("system", "dark", "light")
    private val _themeMode = MutableStateFlow(prefsManager.getAppTheme())
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // Synchronisation state
    private val _isSyncing = MutableStateFlow(true)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        viewModelScope.launch {
            // Seed database offline defaults so the user has immediate access
            repository.checkAndSeedDefaults()
            
            // Set initial state based on configured settings
            if (!prefsManager.isSetupCompleted()) {
                _authState.value = PinAuthState.SetupWelcome
            } else {
                _authState.value = PinAuthState.RequestPin
                // If biometrics configured, we automatically trigger standard biometric request (handled inside UI)
            }

            // Sync websites list from custom GitHub repo
            syncRemoteSites()

            // Update default active parameters based on database synchronization
            repository.allBookmarks.collect { list ->
                if (list.isNotEmpty() && _activeUrl.value == "https://www.pornhub.com" && _activeName.value == "Pornhub") {
                    val first = list.first()
                    _activeUrl.value = first.url
                    _activeName.value = first.name
                }
            }
        }
    }

    // Load custom JSON dynamic list from GitHub
    fun syncRemoteSites() {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            try {
                val url = URL("https://raw.githubusercontent.com/Mr-Unknown-SL/xHUB-Pro/refs/heads/main/sites.json")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val rawJson = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(rawJson)
                    
                    if (jsonArray.length() > 0) {
                        repository.deleteAll()
                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            val name = obj.optString("site_name", "Portal Site")
                            val link = obj.optString("link", "")
                            
                            if (link.isNotEmpty()) {
                                var formattedLink = link.trim()
                                if (!formattedLink.startsWith("http://") && !formattedLink.startsWith("https://")) {
                                    formattedLink = "https://$formattedLink"
                                }
                                val symbol = if (name.isNotEmpty()) name.first().uppercase() else "H"
                                repository.insert(
                                    BookmarkEntity(
                                        name = name,
                                        url = formattedLink,
                                        logoChar = symbol
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSyncing.value = false
            }
        }
    }

    // Setup Navigation Controllers
    fun proceedFromWelcome() {
        _authState.value = PinAuthState.SetupChooseStyle
    }

    fun selectLockStyle(style: String) {
        prefsManager.setLockStyle(style)
        _activeLockStyle.value = style
        _errorMessage.value = null
        clearInput()
        
        if (style == "biometrics") {
            // Save setup direct as biometric setup doesn't require pattern loops
            prefsManager.setSetupCompleted(true)
            _authState.value = PinAuthState.Unlocked
            checkWelcomeDialogStatus()
        } else {
            _authState.value = PinAuthState.SetupLock
        }
    }

    fun reverseToChooseStyle() {
        _authState.value = PinAuthState.SetupChooseStyle
        clearInput()
    }

    // Handles Theme toggling
    fun setThemeMode(mode: String) {
        if (mode == "dark" || mode == "light" || mode == "system") {
            prefsManager.setAppTheme(mode)
            _themeMode.value = mode
        }
    }

    // PIN Typing Core logic
    fun inputDigit(digit: String) {
        _errorMessage.value = null
        val limit = if (_activeLockStyle.value == "6_pin") 6 else 4
        if (_pinBuffer.value.length < limit) {
            _pinBuffer.value += digit
            
            // Auto validate PIN code upon completing limit length
            if (_pinBuffer.value.length == limit) {
                processPinSubmission()
            }
        }
    }

    fun inputBackspace() {
        if (_pinBuffer.value.isNotEmpty()) {
            _pinBuffer.value = _pinBuffer.value.dropLast(1)
        }
    }

    // Active Interactive Pattern Core Lock methods
    fun addPatternNode(nodeIndex: Int) {
        _errorMessage.value = null
        val currentList = _patternBuffer.value.toMutableList()
        if (!currentList.contains(nodeIndex)) {
            currentList.add(nodeIndex)
            _patternBuffer.value = currentList
        }
    }

    fun submitPattern() {
        val pat = _patternBuffer.value
        if (pat.size < 3) {
            _errorMessage.value = "Connect at least 3 dots to set a pattern!"
            _patternBuffer.value = emptyList()
            return
        }

        val patternString = pat.joinToString(",")
        _patternBuffer.value = emptyList() // Clear active UI lines

        when (val current = _authState.value) {
            PinAuthState.SetupLock -> {
                _tempSetupPattern.value = pat
                _authState.value = PinAuthState.SetupConfirmPin
            }
            PinAuthState.SetupConfirmPin -> {
                val expectedString = _tempSetupPattern.value.joinToString(",")
                if (patternString == expectedString) {
                    prefsManager.setPattern(patternString)
                    prefsManager.setSetupCompleted(true)
                    _tempSetupPattern.value = emptyList()
                    _authState.value = PinAuthState.Unlocked
                    checkWelcomeDialogStatus()
                } else {
                    _errorMessage.value = "Pattern matching failed! Try drawing again."
                    _authState.value = PinAuthState.SetupLock
                    _tempSetupPattern.value = emptyList()
                }
            }
            PinAuthState.RequestPin -> {
                if (prefsManager.verifyPattern(patternString)) {
                    _authState.value = PinAuthState.Unlocked
                    checkWelcomeDialogStatus()
                } else {
                    _errorMessage.value = "Incorrect pattern! Access Denied."
                }
            }
            else -> {}
        }
    }

    fun clearPattern() {
        _patternBuffer.value = emptyList()
        _errorMessage.value = null
    }

    // Fingerprint / Biometric click verification mock
    fun verifyBiometricSuccess() {
        _authState.value = PinAuthState.Unlocked
        checkWelcomeDialogStatus()
    }

    // Common input reset
    fun clearInput() {
        _pinBuffer.value = ""
        _patternBuffer.value = emptyList()
        _tempSetupPin.value = ""
        _tempSetupPattern.value = emptyList()
        _errorMessage.value = null
    }

    private fun processPinSubmission() {
        val pin = _pinBuffer.value
        _pinBuffer.value = "" // clear instantly for safety
        
        when (val current = _authState.value) {
            PinAuthState.SetupLock -> {
                _tempSetupPin.value = pin
                _authState.value = PinAuthState.SetupConfirmPin
            }
            PinAuthState.SetupConfirmPin -> {
                if (pin == _tempSetupPin.value) {
                    prefsManager.setPin(pin)
                    prefsManager.setSetupCompleted(true)
                    _authState.value = PinAuthState.Unlocked
                    _tempSetupPin.value = ""
                    checkWelcomeDialogStatus()
                } else {
                    _errorMessage.value = "PIN matching failed! Try setting it again."
                    _authState.value = PinAuthState.SetupLock
                    _tempSetupPin.value = ""
                }
            }
            PinAuthState.RequestPin -> {
                if (prefsManager.verifyPin(pin)) {
                    _authState.value = PinAuthState.Unlocked
                    checkWelcomeDialogStatus()
                } else {
                    _errorMessage.value = "Incorrect passcode! Access Denied."
                }
            }
            else -> {}
        }
    }

    private fun checkWelcomeDialogStatus() {
        if (!prefsManager.isWelcomeDismissed()) {
            _showWelcomeDialog.value = true
        }
    }

    fun dismissWelcome() {
        _showWelcomeDialog.value = false
        prefsManager.setWelcomeDismissed(true)
    }

    // Dynamic address routing
    fun selectUrl(name: String, url: String) {
        var formattedUrl = url.trim()
        if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")) {
            formattedUrl = "https://$formattedUrl"
        }
        _activeUrl.value = formattedUrl
        _activeName.value = name
    }

    // Total security cleanup (Restarts lock configuration flow)
    fun resetPin() {
        prefsManager.clearPin()
        // Maintain theme and welcome dialog visibility logic separately
        clearInput()
        _activeLockStyle.value = "4_pin"
        _authState.value = PinAuthState.SetupChooseStyle
        _showWelcomeDialog.value = false
    }
}
