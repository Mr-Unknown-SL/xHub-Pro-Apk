package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PinAuthState
import kotlinx.coroutines.delay

@Composable
fun PinLockScreen(
    authState: PinAuthState,
    pinBuffer: String,
    errorMessage: String?,
    onDigitInput: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    // Expanded Pattern & Biometrics Callbacks
    lockStyle: String = "4_pin", // "4_pin", "6_pin", "pattern", "biometrics"
    patternBuffer: List<Int> = emptyList(),
    onPatternNodeAdded: (Int) -> Unit = {},
    onPatternSubmit: () -> Unit = {},
    onPatternClear: () -> Unit = {},
    onSelectStyle: (String) -> Unit = {},
    onProceedWelcome: () -> Unit = {},
    onBackToStyles: () -> Unit = {},
    onBiometricSuccess: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pin_lock_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // --- STATE 1: WELCOME SCREEN WIZARD ---
            if (authState == PinAuthState.SetupWelcome) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 40.dp)
                    ) {
                        // Cyber Gold Glow Circle
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "X",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Serif
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "xHub Pro",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "BY MR. UNKNOWN",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 3.sp
                            ),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "discretion first protocol",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "To access this workspace, please configure a secure customized authorization mechanism. Your choices are processed securely off-grid and never leave this node.",
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    val welcomeBtnInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    Button(
                        onClick = onProceedWelcome,
                        interactionSource = welcomeBtnInteractionSource,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .pressScale(welcomeBtnInteractionSource)
                            .testTag("welcome_next_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "NEXT PROTOCOL",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }

            // --- STATE 2: LOCK TYPE CHOOSER WIZARD ---
            else if (authState == PinAuthState.SetupChooseStyle) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Choose Workspace Lock Type",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Select your preferred unlocking method below",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LockStyleSelectCard(
                                title = "Pattern Lock",
                                desc = "Unlock swiftly with custom nodes drawing path",
                                icon = Icons.Filled.List,
                                onClick = { onSelectStyle("pattern") }
                            )

                            LockStyleSelectCard(
                                title = "4-Digit PIN",
                                desc = "Fast & highly compatible classic dynamic passcode",
                                icon = Icons.Filled.Lock,
                                onClick = { onSelectStyle("4_pin") }
                            )

                            LockStyleSelectCard(
                                title = "6-Digit PIN",
                                desc = "Extra secure complex digit layout lock",
                                icon = Icons.Filled.Lock,
                                onClick = { onSelectStyle("6_pin") }
                            )
                        }
                    }

                    // Bottom info label
                    Text(
                        text = "You can change style later inside Dashboard Settings.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // --- STATE 3: ACTIVE SCREEN (SETUP OR VERIFY WORKSPACE LOCKINGS) ---
            else {
                val isSetupMode = authState == PinAuthState.SetupLock || authState == PinAuthState.SetupConfirmPin
                val isPattern = lockStyle == "pattern"
                val isBiometrics = lockStyle == "biometrics"

                // Dynamic Header Info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    // Back button during locked wizard
                    if (isSetupMode) {
                        IconButton(
                            onClick = onBackToStyles,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .testTag("setup_back_to_styles")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val headerIcon = when {
                            isBiometrics -> Icons.Filled.Person
                            isPattern -> Icons.Filled.List
                            else -> Icons.Filled.Lock
                        }
                        Icon(
                            imageVector = headerIcon,
                            contentDescription = "Security Status icon",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val (title, info) = if (isSetupMode) {
                        val isConfirm = authState == PinAuthState.SetupConfirmPin
                        when {
                            isPattern -> if (isConfirm) "Confirm Pattern" to "Redraw your pattern layout to confirm" else "Create Pattern Lock" to "Draw a custom connection pattern across dots (choose min 3)"
                            lockStyle == "6_pin" -> if (isConfirm) "Confirm 6-Digit PIN" to "Re-enter your 6-digit passcode" else "Create 6-Digit passcode" to "Set a secure numeric PIN"
                            else -> if (isConfirm) "Confirm 4-Digit PIN" to "Re-enter your 4-digit passcode" else "Create 4-Digit PIN" to "Set a secure standard passcode"
                        }
                    } else {
                        when {
                            isBiometrics -> "Fingerprint Portal Lock" to "Authenticating your workspace session..."
                            isPattern -> "Pattern Workspace Locked" to "Draw your secure secret pattern to enter"
                            lockStyle == "6_pin" -> "Enter 6-Digit PIN" to "Verify private passcode layout"
                            else -> "Enter 4-Digit PIN" to "Verify private passcode layout"
                        }
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = info,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                // Middle section errors display
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).padding(vertical = 12.dp)) {
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        if (errorMessage != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                            ) {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // --- DRAW GRAPHIC SYSTEMS DYNAMICALLY ---
                    if (isBiometrics) {
                        // BIOMETRIC SCANNING SYSTEM CARD (MOCK SCAN INTERACTION WITH HIGH QUALITY ANIMATION)
                        BiometricMockScanner(onSuccess = onBiometricSuccess)
                    } else if (isPattern) {
                        // CUSTOM 3X3 DRAWER CONNECT SYSTEM
                        PatternLockDrawGrid(
                            patternBuffer = patternBuffer,
                            onNodeAdded = onPatternNodeAdded,
                            onSubmit = onPatternSubmit,
                            onClear = onPatternClear
                        )
                    } else {
                        // DIGIT PIN CODE STATUS ROW INDICATOR
                        val expectedLen = if (lockStyle == "6_pin") 6 else 4
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 32.dp)
                        ) {
                            (0 until expectedLen).forEach { idx ->
                                val filled = idx < pinBuffer.length
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(
                                            color = if (filled) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .background(
                                            color = if (filled) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }

                // Bottom Tactile circular control pad (Only for standard PIN mode codes)
                if (!isPattern && !isBiometrics) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val rows = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("C", "0", "⌫")
                        )

                        for (row in rows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(0.85f),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (symbol in row) {
                                    KeypadButton(
                                        symbol = symbol,
                                        onClick = {
                                            when (symbol) {
                                                "⌫" -> onBackspace()
                                                "C" -> onClear()
                                                else -> onDigitInput(symbol)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    symbol: String,
    onClick: () -> Unit
) {
    val isAction = symbol == "⌫" || symbol == "C"
    
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(
                color = if (isAction) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .bounceClick(onClick = onClick)
            .testTag("keypad_btn_$symbol"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            ),
            color = if (isAction) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
fun LockStyleSelectCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
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

// Interactive 3x3 Dot Grid Pattern Drawer View
@Composable
fun PatternLockDrawGrid(
    patternBuffer: List<Int>,
    onNodeAdded: (Int) -> Unit,
    onSubmit: () -> Unit,
    onClear: () -> Unit
) {
    val nodePositions = remember { mutableStateMapOf<Int, Offset>() }
    var dragPosition by remember { mutableStateOf<Offset?>(null) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.padding(top = 16.dp)
    ) {
        // Pattern Grid board
        Box(
            modifier = Modifier
                .size(280.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            onClear()
                            dragPosition = offset
                            nodePositions.forEach { (index, nodeOffset) ->
                                val distance = (nodeOffset - offset).getDistance()
                                if (distance < 50f) {
                                    onNodeAdded(index)
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            val currentPos = change.position
                            dragPosition = currentPos
                            nodePositions.forEach { (index, offset) ->
                                val distance = (offset - currentPos).getDistance()
                                if (distance < 50f) {
                                    onNodeAdded(index)
                                }
                            }
                        },
                        onDragEnd = {
                            dragPosition = null
                            if (patternBuffer.isNotEmpty()) {
                                onSubmit()
                            }
                        },
                        onDragCancel = {
                            dragPosition = null
                            onClear()
                        }
                    )
                }
        ) {
            // Draw connector lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (patternBuffer.size >= 2) {
                    for (i in 0 until patternBuffer.size - 1) {
                        val startNode = patternBuffer[i]
                        val endNode = patternBuffer[i + 1]
                        val startPos = nodePositions[startNode]
                        val endPos = nodePositions[endNode]
                        if (startPos != null && endPos != null) {
                            drawLine(
                                color = primaryColor,
                                start = startPos,
                                end = endPos,
                                strokeWidth = 8f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                // Draw live floating drag line from last dot to user finger
                val lastDot = patternBuffer.lastOrNull()
                val liveFingerPos = dragPosition
                if (lastDot != null && liveFingerPos != null) {
                    val startPos = nodePositions[lastDot]
                    if (startPos != null) {
                        drawLine(
                            color = primaryColor.copy(alpha = 0.8f),
                            start = startPos,
                            end = liveFingerPos,
                            strokeWidth = 8f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Lay out the 9 dots in a 3x3 standard coordinate system grid.
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in 0..2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (col in 0..2) {
                            val idx = row * 3 + col
                            val selected = patternBuffer.contains(idx)
                            
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .onGloballyPositioned { layoutCoordinates ->
                                        // Store accurate coordinate offsets
                                        val parentPos = layoutCoordinates.positionInParent()
                                        val size = layoutCoordinates.size
                                        nodePositions[idx] = Offset(
                                            parentPos.x + size.width / 2f,
                                            parentPos.y + size.height / 2f
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(if (selected) 24.dp else 14.dp)
                                        .background(
                                            color = if (selected) primaryColor else outlineColor,
                                            shape = CircleShape
                                        )
                                        .border(
                                            width = if (selected) 4.dp else 0.dp,
                                            color = if (selected) primaryColor.copy(alpha = 0.3f) else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Drawer buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            val resetInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            OutlinedButton(
                onClick = onClear,
                interactionSource = resetInteractionSource,
                modifier = Modifier
                    .weight(1f)
                    .pressScale(resetInteractionSource),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("RESET")
            }
            
            val continueInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            Button(
                onClick = { if (patternBuffer.isNotEmpty()) onSubmit() },
                interactionSource = continueInteractionSource,
                modifier = Modifier
                    .weight(1f)
                    .pressScale(continueInteractionSource),
                shape = RoundedCornerShape(10.dp),
                enabled = patternBuffer.isNotEmpty()
            ) {
                Text("CONTINUE")
            }
        }
    }
}

// Beautiful, high-fidelity dynamic Fingerprint Icon drawn purely via Canvas
@Composable
fun FingerprintIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)

        // Draw multiple beautiful concentric fingerprint arcs
        for (i in 1..5) {
            val radius = (i * 10).dp.toPx()
            if (radius < w / 2f) {
                drawArc(
                    color = color,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
        }
        // Draw lower loops / lines
        val loopWidth = 4.dp.toPx()
        drawLine(
            color = color,
            start = Offset(center.x - 10.dp.toPx(), center.y),
            end = Offset(center.x - 10.dp.toPx(), center.y + 20.dp.toPx()),
            strokeWidth = loopWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(center.x + 10.dp.toPx(), center.y),
            end = Offset(center.x + 10.dp.toPx(), center.y + 20.dp.toPx()),
            strokeWidth = loopWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(center.x, center.y + 5.dp.toPx()),
            end = Offset(center.x, center.y + 25.dp.toPx()),
            strokeWidth = loopWidth,
            cap = StrokeCap.Round
        )
    }
}

// Gorgeous secure scanning circle that lets simulated bio pass beautifully
@Composable
fun BiometricMockScanner(
    onSuccess: () -> Unit
) {
    var isScanning by remember { mutableStateOf(false) }
    var scanStatusMessage by remember { mutableStateOf("Tap scan sensor below") }
    val primaryColor = MaterialTheme.colorScheme.primary

    // Launched scanner timer simulation
    LaunchedEffect(isScanning) {
        if (isScanning) {
            scanStatusMessage = "Analyzing biological trace..."
            delay(1500)
            scanStatusMessage = "Decrypting verification signatures..."
            delay(1000)
            scanStatusMessage = "Access Granted!"
            delay(300)
            onSuccess()
            isScanning = false
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    if (isScanning) primaryColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
                )
                .border(2.dp, if (isScanning) primaryColor else outlineLightBorder(), CircleShape)
                .clickable {
                    if (!isScanning) isScanning = true
                }
                .testTag("biometric_scanner_view"),
            contentAlignment = Alignment.Center
        ) {
            if (isScanning) {
                // Spinning circle loading loop
                CircularProgressIndicator(
                    modifier = Modifier.size(140.dp),
                    color = primaryColor,
                    strokeWidth = 3.dp
                )
            }
            
            FingerprintIcon(
                color = if (isScanning) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(72.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = scanStatusMessage,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = if (isScanning) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isScanning) "Keep finger securely flat against screen sensor" else "Press on biometric scan icon to trigger instant verification",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun outlineLightBorder(): Color {
    return MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
}

// PREMIUM NEXT-LEVEL CLICK ANIMATION MODIFIER
@Composable
private fun Modifier.bounceClick(onClick: () -> Unit = {}): Modifier {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
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
            indication = androidx.compose.foundation.LocalIndication.current,
            onClick = onClick
        )
}

// PREMIUM ANIMATED SCALE MODIFIER BASED ON INTERACTION SOURCE
@Composable
private fun Modifier.pressScale(interactionSource: androidx.compose.foundation.interaction.InteractionSource): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "pressScale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
