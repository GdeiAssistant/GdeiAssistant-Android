package cn.gdeiassistant.ui.components

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.gdeiassistant.R
import cn.gdeiassistant.ui.theme.AppShapes
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import kotlinx.coroutines.delay

@Composable
fun NativeImageGallery(
    imageUrls: List<String>,
    modifier: Modifier = Modifier,
    cardWidth: Dp = 220.dp,
    cardHeight: Dp = 180.dp
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier.horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        imageUrls.forEachIndexed { index, url ->
            Column(
                modifier = Modifier.width(cardWidth),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = AppShapes.card,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ) {
                    SubcomposeAsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(cardHeight)
                            .clip(AppShapes.card),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(cardHeight)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(cardHeight)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.GraphicEq,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        success = { SubcomposeAsyncImageContent() }
                    )
                }
                Text(
                    text = stringResource(R.string.native_media_image_position, index + 1, imageUrls.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun NativeAudioPlayerCard(
    title: String,
    subtitle: String,
    url: String,
    modifier: Modifier = Modifier
) {
    val defaultErrorMessage = stringResource(R.string.native_audio_load_failed)
    var mediaPlayer by remember(url) { mutableStateOf<MediaPlayer?>(null) }
    var isPrepared by remember(url) { mutableStateOf(false) }
    var isPreparing by remember(url) { mutableStateOf(false) }
    var isPlaying by remember(url) { mutableStateOf(false) }
    var durationMs by remember(url) { mutableIntStateOf(0) }
    var positionMs by remember(url) { mutableIntStateOf(0) }
    var errorMessage by remember(url) { mutableStateOf<String?>(null) }

    DisposableEffect(url) {
        val player = MediaPlayer()
        player.setOnPreparedListener { preparedPlayer ->
            durationMs = preparedPlayer.duration.coerceAtLeast(0)
            positionMs = 0
            isPrepared = true
            isPreparing = false
            errorMessage = null
            preparedPlayer.start()
            isPlaying = true
        }
        player.setOnCompletionListener {
            isPlaying = false
            positionMs = durationMs
        }
        player.setOnErrorListener { failedPlayer, _, _ ->
            runCatching { failedPlayer.reset() }
            isPrepared = false
            isPreparing = false
            isPlaying = false
            errorMessage = defaultErrorMessage
            true
        }
        mediaPlayer = player

        onDispose {
            isPlaying = false
            isPreparing = false
            runCatching { player.stop() }
            runCatching { player.reset() }
            player.release()
            mediaPlayer = null
        }
    }

    LaunchedEffect(isPlaying, mediaPlayer) {
        while (isPlaying) {
            positionMs = runCatching { mediaPlayer?.currentPosition ?: positionMs }
                .getOrDefault(positionMs)
            delay(250)
        }
    }

    val statusText = when {
        !errorMessage.isNullOrBlank() -> errorMessage.orEmpty()
        isPreparing -> stringResource(R.string.native_audio_preparing)
        isPlaying -> stringResource(R.string.native_audio_playing)
        else -> stringResource(R.string.native_audio_ready)
    }
    val progress = if (durationMs > 0) {
        (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    SectionCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                onClick = {
                    val player = mediaPlayer ?: return@Surface
                    if (isPlaying) {
                        runCatching {
                            player.pause()
                            positionMs = player.currentPosition
                        }
                        isPlaying = false
                    } else if (isPrepared) {
                        runCatching {
                            if (durationMs > 0 && positionMs >= durationMs - 400) {
                                player.seekTo(0)
                                positionMs = 0
                            }
                            player.start()
                        }.onSuccess {
                            isPlaying = true
                            errorMessage = null
                        }.onFailure {
                            errorMessage = it.localizedMessage ?: defaultErrorMessage
                        }
                    } else {
                        try {
                            errorMessage = null
                            isPreparing = true
                            player.reset()
                            player.setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .build()
                            )
                            player.setDataSource(url)
                            player.prepareAsync()
                        } catch (error: Exception) {
                            isPreparing = false
                            isPrepared = false
                            errorMessage = error.localizedMessage ?: defaultErrorMessage
                        }
                    }
                },
                shape = AppShapes.button,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                enabled = !isPreparing
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = if (isPreparing) "loading" else if (isPlaying) "pause" else "play",
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "audio-player-state"
                    ) { state ->
                        when (state) {
                            "loading" -> CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )

                            "pause" -> Icon(
                                imageVector = Icons.Rounded.Pause,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )

                            else -> Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (errorMessage.isNullOrBlank()) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                )
            }
            Text(
                text = "${formatDuration(positionMs)} / ${formatDuration(durationMs)}",
                style = MaterialTheme.typography.labelLarge,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDuration(durationMs: Int): String {
    val totalSeconds = durationMs.coerceAtLeast(0) / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
