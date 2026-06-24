package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import coil.compose.AsyncImage
import com.example.data.BookmarkEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    activeUrl: String,
    activeName: String,
    bookmarks: List<BookmarkEntity>,
    favoriteUrls: Set<String>,
    showWelcomeDialog: Boolean,
    themeMode: String,
    onUrlSelected: (String, String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDismissWelcome: () -> Unit,
    onResetPin: () -> Unit,
    onThemeChange: (String) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var insideSettings by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Bottom Tab selection state
    var currentTab by remember { mutableStateOf("home") }

    // Internet connectivity check state
    val context = LocalContext.current
    var showNoNetDialog by remember { mutableStateOf(false) }
    var showNoNetPage by remember { mutableStateOf(false) }
    var retryCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        if (!isInternetAvailable(context)) {
            showNoNetDialog = true
        }
    }

    // Generates a colorful background for site initials avatar placeholders if image fails to load
    fun getAvatarBrush(char: String): Brush {
        val hash = char.hashCode()
        val colors = when (hash % 5) {
            0 -> listOf(Color(0xFFE91E63), Color(0xFFFF4081))
            1 -> listOf(Color(0xFF9C27B0), Color(0xFFE040FB))
            2 -> listOf(Color(0xFF00BCD4), Color(0xFF00E5FF))
            3 -> listOf(Color(0xFF4CAF50), Color(0xFF69F0AE))
            else -> listOf(Color(0xFFFF9800), Color(0xFFFFAB40))
        }
        return Brush.linearGradient(colors)
    }

    // Modal Drawer Wrapper
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false, // Disable swipe/drag to open/close drawer as requested
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight(),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Private Web Engine",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "xHub Pro",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close Drawer",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Founder: Mr.Unknown",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Search Bar Section
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("drawer_search_bar"),
                    placeholder = {
                        Text(
                            text = "Search portals...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search sites",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Scrollable Booking Portals List
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val sortedAndFilteredBookmarks = remember(bookmarks, searchQuery) {
                        val sorted = bookmarks.sortedBy { it.name.lowercase() }
                        if (searchQuery.isBlank()) {
                            sorted
                        } else {
                            sorted.filter {
                                it.name.contains(searchQuery, ignoreCase = true) ||
                                it.url.contains(searchQuery, ignoreCase = true)
                            }
                        }
                    }

                    if (bookmarks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Loading synchronized sites...\nPlease verify internet connection.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    } else if (sortedAndFilteredBookmarks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No matching portals found",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try searching with other words",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(sortedAndFilteredBookmarks, key = { it.id }) { item ->
                                val isSelected = item.url == activeUrl
                                val onClickState = remember(item, onUrlSelected) {
                                    {
                                        onUrlSelected(item.name, item.url)
                                        scope.launch { drawerState.close() }
                                        Unit
                                    }
                                }
                                val onToggleFavoriteState = remember(item, onToggleFavorite) {
                                    {
                                        onToggleFavorite(item.url)
                                    }
                                }
                                NavigationItemRow(
                                    item = item,
                                    isSelected = isSelected,
                                    isFavorite = favoriteUrls.contains(item.url),
                                    onClick = onClickState,
                                    onToggleFavorite = onToggleFavoriteState
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Bottom Drawer Controllers
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick close settings link or reset option
                    OutlinedButton(
                        onClick = {
                            scope.launch { drawerState.close() }
                            insideSettings = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings configuration link")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Workspace Settings",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    ) {
        // Main Portal Content Scaffold
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        if (insideSettings) {
                            IconButton(
                                onClick = { insideSettings = false },
                                modifier = Modifier.testTag("appbar_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Return to Web Browser Engine",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                    }
                                },
                                modifier = Modifier.testTag("hamburger_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Slide Drawer Bookmarks Controller",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = if (insideSettings) "Settings Page" else if (activeUrl.isNotEmpty()) activeName else "xHub Pro",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (insideSettings) "Configure parameters offline" else if (activeUrl.isEmpty()) "Secure Hub Launcher" else "Private Tunnel Active",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    actions = {
                        // Quick Web Close Action Row
                        if (activeUrl.isNotEmpty() && !insideSettings) {
                            IconButton(
                                onClick = { onUrlSelected("", "") }, // Clears active URL, returning to Homepage Launcher
                                modifier = Modifier.testTag("appbar_close_web_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close Web Session",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        // Directly clickable single Theme icon switcher instead of a dropdown.
                        IconButton(
                            onClick = {
                                val newMode = if (themeMode == "dark") "light" else "dark"
                                onThemeChange(newMode)
                            },
                            modifier = Modifier.testTag("appbar_theme_button")
                        ) {
                            if (themeMode == "dark") {
                                // When theme is dark, show sun icon to shift to light
                                Text("☀️", fontSize = 20.sp)
                            } else {
                                // When theme is light, show half-moon icon to shift to dark
                                Text("🌙", fontSize = 20.sp)
                            }
                        }

                        // Settings Icon Action
                        IconButton(
                            onClick = { insideSettings = !insideSettings },
                            modifier = Modifier.testTag("appbar_settings_button")
                        ) {
                            Icon(
                                imageVector = if (insideSettings) Icons.Filled.Close else Icons.Filled.Settings,
                                contentDescription = "Settings Page Log",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                if (!insideSettings && !showNoNetPage) {
                    NavigationBar(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        val navItemColors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )

                        NavigationBarItem(
                            selected = activeUrl.isEmpty() && currentTab == "home",
                            onClick = {
                                if (activeUrl.isNotEmpty()) {
                                    onUrlSelected("", "")
                                }
                                currentTab = "home"
                            },
                            icon = { Icon(Icons.Filled.Home, contentDescription = "Home Tab") },
                            label = { Text("Home") },
                            colors = navItemColors
                        )
                        NavigationBarItem(
                            selected = activeUrl.isEmpty() && currentTab == "favorite",
                            onClick = {
                                if (activeUrl.isNotEmpty()) {
                                    onUrlSelected("", "")
                                }
                                currentTab = "favorite"
                            },
                            icon = { Icon(Icons.Filled.Favorite, contentDescription = "Favorites Tab") },
                            label = { Text("Favorites") },
                            colors = navItemColors
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showNoNetPage) {
                    NoConnectionScreen(
                        onRetry = {
                            if (isInternetAvailable(context)) {
                                showNoNetPage = false
                                showNoNetDialog = false
                                retryCount = 0
                                Toast.makeText(context, "Connected successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Network still unavailable.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onExit = {
                            (context as? android.app.Activity)?.finishAndRemoveTask()
                            java.lang.System.exit(0)
                        }
                    )
                } else if (insideSettings) {
                    // --- SECURITY SETTINGS SUB PAGE ---
                    SettingsContentPage(
                        onResetLock = {
                            insideSettings = false
                            onResetPin()
                        }
                    )
                } else {
                    // Host Private Web Engine
                    if (activeUrl.isEmpty()) {
                        if (currentTab == "home") {
                            HomeDashboardContent(
                                sortedBookmarkingList = bookmarks,
                                searchQuery = searchQuery,
                                favoriteUrls = favoriteUrls,
                                onUrlSelected = onUrlSelected,
                                onToggleFavorite = onToggleFavorite,
                                onOpenDrawer = {
                                    scope.launch { drawerState.open() }
                                }
                            )
                        } else {
                            FavoriteDashboardContent(
                                allBookmarkingList = bookmarks,
                                favoriteUrls = favoriteUrls,
                                onUrlSelected = onUrlSelected,
                                onToggleFavorite = onToggleFavorite
                            )
                        }
                    } else {
                        AdvancedWebView(
                            url = activeUrl,
                            onTitleChanged = { webTitle ->
                                // Optional side effect of title updating
                            }
                        )
                    }
                }
            }
        }
    }

    // NETWORK OFFLINE DETECTOR DIALOG
    if (showNoNetDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Connection Lost",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Text(
                    text = "No internet connection detected on portal launcher bootstrap. Please recheck your data credentials, retry, or exit the workspace.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isInternetAvailable(context)) {
                            showNoNetDialog = false
                            showNoNetPage = false
                            retryCount = 0
                            Toast.makeText(context, "Connected!", Toast.LENGTH_SHORT).show()
                        } else {
                            retryCount += 1
                            if (retryCount >= 3) {
                                showNoNetDialog = false
                                showNoNetPage = true
                            } else {
                                Toast.makeText(context, "Offline. Retries left: ${3 - retryCount}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Retry")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        (context as? android.app.Activity)?.finishAndRemoveTask()
                        java.lang.System.exit(0)
                    }
                ) {
                    Text("Exit")
                }
            }
        )
    }

    // FIRST-TIME USER INTRODUCTORY WELCOME POPUP
    if (showWelcomeDialog) {
        AlertDialog(
            onDismissRequest = { onDismissWelcome() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Welcome to xHub Pro",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Styled Founder / Creator Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "App Owner: Mr.Unknown",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Text(
                        text = "xHub Pro is a highly secure, privacy-focused private browser environment designed to insulate your browsing sessions.",
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Text(
                        text = "💡 Quick Guide:\n• Tap topbar menu icon to reveal private bookmarked portals.\n• Your secure lock passcode, traces or patterns protect data integrity.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onDismissWelcome() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dismiss_welcome_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Get Started",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("welcome_popup_dialog")
        )
    }
}

// Stunning Material 3 style Off-grid Settings View
@Composable
fun SettingsContentPage(
    onResetLock: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Security Preferences",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Change Lock Option Block
        item {
            Surface(
                onClick = onResetLock,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth().testTag("settings_change_lock_style")
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Change Lock Style",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Switch between pattern, numeric pins or system biometrics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "About Application",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // About Block containing MR. UNKNOWN signature and detailed specs
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.Red.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "App Owner: Mr.Unknown",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Developer & Founder of custom portals system",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Text(
                        text = "xHub Pro delivers unparalleled high security and session encapsulation. All cache files, browsing footprints and web configurations remain isolated inside the local storage engine.",
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "🔒 Security Highlights:\n• Dynamic Multi Lock: Choose from Pattern tracing grids, 4/6 codes or secure fingerprint scanners.\n• Encrypted Preferences: Stored safely offline.\n• Cloud Synced Bookmarks: Automated repo updating keeping your portals connected.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NavigationItemRow(
    item: BookmarkEntity,
    isSelected: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }

    val avatarBrush = remember(item.logoChar) {
        val avatarHash = item.logoChar.hashCode()
        val colors = when (avatarHash % 5) {
            0 -> listOf(Color(0xFFE91E63), Color(0xFFFF4081))
            1 -> listOf(Color(0xFF9C27B0), Color(0xFFE040FB))
            2 -> listOf(Color(0xFF00BCD4), Color(0xFF00E5FF))
            3 -> listOf(Color(0xFF4CAF50), Color(0xFF69F0AE))
            else -> listOf(Color(0xFFFF9800), Color(0xFFFFAB40))
        }
        Brush.linearGradient(colors)
    }

    val rootDomain = remember(item.url) { extractDomain(item.url) }
    val faviconModel = remember(rootDomain) { "https://www.google.com/s2/favicons?sz=64&domain=$rootDomain" }

    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .bounceCombinedClick(
                    onClick = onClick,
                    onLongClick = { showDropdown = true }
                )
                .testTag("bookmark_item_${item.id}"),
            color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                Color.Transparent
            },
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // High Resolution Favicon Loader with Auto Initial Letter Gradient Fallback
                var isImageError by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            textColorBrush(isImageError, avatarBrush, MaterialTheme.colorScheme.surfaceVariant),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isImageError) {
                        AsyncImage(
                            model = faviconModel,
                            contentDescription = "favicon for ${item.name}",
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape),
                            onError = { isImageError = true },
                            onSuccess = { isImageError = false }
                        )
                    }

                    if (isImageError) {
                        Text(
                            text = item.logoChar,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        if (isFavorite) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Favorite Portal",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false }
        ) {
            DropdownMenuItem(
                text = { Text(if (isFavorite) "Remove from Favorites" else "Add to Favorites") },
                leadingIcon = {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.FavoriteBorder else Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = if (isFavorite) Color.Gray else MaterialTheme.colorScheme.primary
                    )
                },
                onClick = {
                    showDropdown = false
                    onToggleFavorite()
                }
            )
        }
    }
}

// Utility to cleanly extract root domain hostnames for precise favicon loading
private fun extractDomain(url: String): String {
    return try {
        val uri = java.net.URI(url)
        val domain = uri.host ?: ""
        if (domain.startsWith("www.")) domain.substring(4) else domain
    } catch (e: Exception) {
        url.replace("https://", "").replace("http://", "").split("/").firstOrNull() ?: ""
    }
}

// Return dynamic background colors for favicon placeholders
@Composable
private fun textColorBrush(isError: Boolean, errorBrush: Brush, normalColor: Color): Brush {
    val normalBrush = remember(normalColor) { Brush.linearGradient(listOf(normalColor, normalColor)) }
    return if (isError) {
        errorBrush
    } else {
        normalBrush
    }
}

// PREMIUM NEXT-LEVEL CLICK ANIMATION MODIFIER
@Composable
private fun Modifier.bounceClick(onClick: () -> Unit = {}): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounce"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            onClick = onClick
        )
}

// PREMIUM COMBINED CLICK EMBEDDED MODIFIER
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Modifier.bounceCombinedClick(
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounce"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .combinedClickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            onClick = onClick,
            onLongClick = onLongClick
        )
}

// PREMIUM ANIMATED SCALE MODIFIER BASED ON INTERACTION SOURCE
@Composable
private fun Modifier.pressScale(interactionSource: androidx.compose.foundation.interaction.InteractionSource): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "pressScale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun EmptySiteSelectPlaceholder(onOpenMenu: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .size(90.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Select site to open",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap the menu icon in the top left corner to browse and select your favorite secure site",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Beautiful customized call-to-action button with bounce animation
            val placeholderBtnInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            Button(
                onClick = onOpenMenu,
                interactionSource = placeholderBtnInteractionSource,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .height(48.dp)
                    .pressScale(placeholderBtnInteractionSource)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.List,
                        contentDescription = null
                    )
                    Text(
                        text = "Open Portal Menu",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeDashboardContent(
    sortedBookmarkingList: List<BookmarkEntity>,
    searchQuery: String,
    favoriteUrls: Set<String>,
    onUrlSelected: (String, String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenDrawer: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val sorted = remember(sortedBookmarkingList) {
        sortedBookmarkingList.sortedBy { it.name.lowercase() }
    }
    val filteredList = remember(sorted, query) {
        if (query.isBlank()) {
            sorted
        } else {
            sorted.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.url.contains(query, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // High fidelity elegant hero card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Encapsulated Private Engine",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Welcome to xHub Pro. Access isolated secure sites with full privacy footprints encapsulation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        // Search Bar Row on Homepage
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("home_search_bar"),
            placeholder = { Text("Search portals directly...", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Portals List
        if (sortedBookmarkingList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Connecting and synchronizing database ports...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No secure sites match your search",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Secure Portals (Sorted A-Z)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                items(filteredList, key = { it.id }) { item ->
                    val onClickState = remember(item, onUrlSelected) {
                        { onUrlSelected(item.name, item.url) }
                    }
                    val onToggleFavoriteState = remember(item, onToggleFavorite) {
                        { onToggleFavorite(item.url) }
                    }

                    NavigationItemRow(
                        item = item,
                        isSelected = false,
                        isFavorite = favoriteUrls.contains(item.url),
                        onClick = onClickState,
                        onToggleFavorite = onToggleFavoriteState
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteDashboardContent(
    allBookmarkingList: List<BookmarkEntity>,
    favoriteUrls: Set<String>,
    onUrlSelected: (String, String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    val sorted = remember(allBookmarkingList) {
        allBookmarkingList.sortedBy { it.name.lowercase() }
    }
    val favoriteList = remember(sorted, favoriteUrls) {
        sorted.filter { favoriteUrls.contains(it.url) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Visual Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Your Favorites Registry",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Launch your most frequently accessed private links here instantly, outside the main drawer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        if (favoriteList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No favorites configured",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Touch and hold (long-press) any portal in the site list drawer or home tab, then select 'Add to Favorites'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Bookmarked Favorites (${favoriteList.size})",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                items(favoriteList, key = { item -> item.url }) { item ->
                    val onClickState = remember(item, onUrlSelected) {
                        { onUrlSelected(item.name, item.url) }
                    }
                    val onToggleFavoriteState = remember(item, onToggleFavorite) {
                        { onToggleFavorite(item.url) }
                    }

                    NavigationItemRow(
                        item = item,
                        isSelected = false,
                        isFavorite = true,
                        onClick = onClickState,
                        onToggleFavorite = onToggleFavoriteState
                    )
                }
            }
        }
    }
}

@Composable
fun NoConnectionScreen(
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "noconn")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Center visual pulsating warning
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .size(100.dp)
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Connection Lost",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "xHub Pro is unable to bridge portals safely in offline mode.\nPlease verify your internet is turned on.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exit button
                OutlinedButton(
                    onClick = onExit,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Exit", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }

                // Retry Button
                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Retry", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

fun isInternetAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNet = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(activeNet) ?: return false
    return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
           caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
           caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
}
