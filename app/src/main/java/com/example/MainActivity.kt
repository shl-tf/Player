package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        SongPlayerApp()
      }
    }
  }
}

@Composable
fun SongPlayerApp(viewModel: SongPlayerViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Launcher to select audio files from device storage securely
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectSong(it) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0C0914), // Premium deep stellar black
                            Color(0xFF130E26), // Cosmic deep indigo
                            Color(0xFF0A0711)  // Bottom pure darkness
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top aesthetic branding header
                AppHeader()

                // Playback content or Empty state selection layout
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.selectedUri == null) {
                        EmptyStateView(
                            onSelectFileClick = { audioPickerLauncher.launch("audio/*") }
                        )
                    } else {
                        PlayerView(
                            uiState = uiState,
                            onPlayClick = { viewModel.play() },
                            onPauseClick = { viewModel.pause() },
                            onSeekChange = { viewModel.seekTo(it) },
                            onChangeSongClick = { audioPickerLauncher.launch("audio/*") },
                            onResetClick = { viewModel.reset() }
                        )
                    }
                }

                // Info footer
                AppFooter()
            }
        }
    }
}

@Composable
fun AppHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Text(
            text = "AUDIO PRO",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                color = Color(0xFF00E5FF)
            )
        )
        Text(
            text = "Воспроизведение песни без повторов",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White.copy(alpha = 0.5f)
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun AppFooter() {
    Text(
        text = "Выбранный трек проигрывается ровно 1 раз и останавливается",
        style = MaterialTheme.typography.bodySmall.copy(
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Medium
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun EmptyStateView(onSelectFileClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Rotating disc representation when loading
        Box(
            modifier = Modifier
                .size(180.dp)
                .background(Color.White.copy(alpha = 0.02f), CircleShape)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.04f),
                    radius = size.minDimension / 3f,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            // Elegant glowing music note symbol
            Canvas(modifier = Modifier.size(50.dp)) {
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(w * 0.4f, h * 0.7f)
                    cubicTo(w * 0.4f, h * 0.55f, w * 0.15f, h * 0.55f, w * 0.15f, h * 0.7f)
                    cubicTo(w * 0.15f, h * 0.85f, w * 0.4f, h * 0.85f, w * 0.4f, h * 0.7f)
                    lineTo(w * 0.4f, h * 0.15f)
                    lineTo(w * 0.8f, h * 0.25f)
                    lineTo(w * 0.8f, h * 0.55f)
                    moveTo(w * 0.8f, h * 0.55f)
                    cubicTo(w * 0.8f, h * 0.4f, w * 0.55f, h * 0.4f, w * 0.55f, h * 0.55f)
                    cubicTo(w * 0.55f, h * 0.7f, w * 0.8f, h * 0.7f, w * 0.8f, h * 0.55f)
                    moveTo(w * 0.4f, h * 0.15f)
                    lineTo(w * 0.8f, h * 0.25f)
                }
                drawPath(
                    path = path,
                    color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Проводник песен",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Нажмите на кнопку ниже, чтобы открыть аудиофайл. Песня автоматически проиграется 1 раз.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = 0.6f)
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onSelectFileClick,
            modifier = Modifier
                .testTag("select_song_button")
                .height(56.dp)
                .fillMaxWidth(0.85f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE040FB), // bright neon purple
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить песню"
                )
                Text(
                    text = "Выбрать песню",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}

@Composable
fun PlayerView(
    uiState: PlayerUiState,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onSeekChange: (Float) -> Unit,
    onChangeSongClick: () -> Unit,
    onResetClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing spinning Vinyl Disc
        VinylRecord(isPlaying = uiState.isPlaying)

        Spacer(modifier = Modifier.height(16.dp))

        // Visual equalizer bars responding to playback
        AudioVisualizer(isPlaying = uiState.isPlaying)

        Spacer(modifier = Modifier.height(24.dp))

        // State indicator status badge (e.g., Playing, Paused, Completed)
        StatusPill(isPlaying = uiState.isPlaying, isCompleted = uiState.isCompleted)

        Spacer(modifier = Modifier.height(16.dp))

        // Current track name
        Text(
            text = uiState.songName ?: "Неизвестный трек",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 28.sp
            ),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Slider component to display progress
        Slider(
            value = uiState.progress,
            onValueChange = onSeekChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00E5FF),
                activeTrackColor = Color(0xFF00E5FF),
                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )

        // Text indicators for elapsed and total track duration
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = uiState.formattedPosition,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = uiState.formattedDuration,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Interactive primary panel (Play, Pause, Reset, Change)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Re-select / close current file
            IconButton(
                onClick = onResetClick,
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Очистить текущий аудиофайл",
                    tint = Color.White
                )
            }

            // Big glowing Play/Pause FAB
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF00E5FF),
                                Color(0xFFE040FB)
                            )
                        )
                    )
                    .clickable {
                        if (uiState.isPlaying) onPauseClick() else onPlayClick()
                    }
                    .testTag("play_pause_button")
                    .shadow(16.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                PlayPauseIcon(
                    isPlaying = uiState.isPlaying,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Quick re-pick / change song option
            IconButton(
                onClick = onChangeSongClick,
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Сменить трек",
                    tint = Color.White
                )
            }
        }

        // Live player rendering error message handler
        uiState.error?.let { err ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x11FF5252),
                    contentColor = Color(0xFFFF5252)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = err,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun VinylRecord(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotation")
    val rotationAngle by if (isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(5000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Box(
        modifier = modifier
            .size(200.dp)
            .graphicsLayer { rotationZ = rotationAngle }
            .shadow(20.dp, CircleShape)
            .background(Color(0xFF0B0B0C), CircleShape)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Draw real records circles inside
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxRadius = size.minDimension / 2f
            for (i in 3..9) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.06f),
                    radius = maxRadius * (i / 11f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        // Glowing vinyl label accent
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00E5FF),
                            Color(0xFFE040FB)
                        )
                    ),
                    shape = CircleShape
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Little turntable needle pin hole
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFF0C0914), CircleShape)
            )
        }
    }
}

@Composable
fun AudioVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val barCount = 20
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")

    val animations = (0 until barCount).map { index ->
        if (isPlaying) {
            infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 350 + (index * 59) % 250,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )
        } else {
            remember { mutableStateOf(0.12f) }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom
    ) {
        animations.forEach { heightState ->
            val factor = heightState.value
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(factor)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF00E5FF),
                                Color(0xFFE040FB)
                            )
                        ),
                        shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                    )
            )
        }
    }
}

@Composable
fun StatusPill(isPlaying: Boolean, isCompleted: Boolean) {
    val text = when {
        isCompleted -> "Воспроизведение завершено (1 раз)"
        isPlaying -> "Воспроизведение..."
        else -> "На паузе"
    }

    val containerColor = when {
        isCompleted -> Color(0xFF4CAF50).copy(alpha = 0.15f)
        isPlaying -> Color(0xFF00E5FF).copy(alpha = 0.15f)
        else -> Color.White.copy(alpha = 0.07f)
    }

    val contentColor = when {
        isCompleted -> Color(0xFF81C784)
        isPlaying -> Color(0xFF00E5FF)
        else -> Color.White.copy(alpha = 0.6f)
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isPlaying) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(700, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_opacity"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = alpha))
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun PlayPauseIcon(
    isPlaying: Boolean,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (isPlaying) {
            // Draw 2 rounded vertical rectangles for Pause
            val barW = w * 0.28f
            val spacing = w * 0.22f
            
            // Left bar
            drawRoundRect(
                color = tint,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.11f, h * 0.15f),
                size = androidx.compose.ui.geometry.Size(barW, h * 0.7f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            // Right bar
            drawRoundRect(
                color = tint,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.11f + barW + spacing, h * 0.15f),
                size = androidx.compose.ui.geometry.Size(barW, h * 0.7f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        } else {
            // Draw Play triangle with clean sharp points
            val path = Path().apply {
                moveTo(w * 0.25f, h * 0.15f)
                lineTo(w * 0.85f, h * 0.5f)
                lineTo(w * 0.25f, h * 0.85f)
                close()
            }
            drawPath(
                path = path,
                color = tint,
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
        }
    }
}

