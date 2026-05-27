package com.example

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SongMetadata(
    val name: String,
    val durationMs: Long
)

data class PlayerUiState(
    val selectedUri: Uri? = null,
    val songName: String? = null,
    val durationMs: Long = 0L,
    val currentPositionMs: Long = 0L,
    val isPlaying: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null
) {
    val progress: Float
        get() = if (durationMs > 0) currentPositionMs.toFloat() / durationMs.toFloat() else 0f

    val formattedPosition: String
        get() = formatTime(currentPositionMs)

    val formattedDuration: String
        get() = formatTime(durationMs)

    private fun formatTime(ms: Long): String {
        val totalSecs = ms / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format("%02d:%02d", mins, secs)
    }
}

class SongPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    fun selectSong(uri: Uri) {
        // Stop and release previous player
        releasePlayer()

        val context = getApplication<Application>()
        val metadata = getSongMetadata(context, uri)

        _uiState.update {
            PlayerUiState(
                selectedUri = uri,
                songName = metadata.name,
                durationMs = metadata.durationMs,
                isPlaying = false,
                isCompleted = false
            )
        }

        initializePlayer(uri)
    }

    private fun initializePlayer(uri: Uri) {
        val context = getApplication<Application>()
        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, uri)
                prepare()
            }

            mediaPlayer = player

            // Set duration from media player if retriever failed earlier
            val duration = player.duration.toLong()
            _uiState.update { it.copy(durationMs = duration) }

            player.setOnCompletionListener {
                onPlaybackCompleted()
            }

            player.setOnErrorListener { _, what, extra ->
                _uiState.update { it.copy(error = "Ошибка воспроизведения: $what ($extra)") }
                stopTrackingProgress()
                false
            }

            // Automatically play the song once upon loaded, matching the requirement
            play()

        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Не удалось загрузить аудио: ${e.localizedMessage}") }
        }
    }

    fun play() {
        val player = mediaPlayer ?: return
        if (!player.isPlaying) {
            try {
                // If we were completed, start from beginning
                if (_uiState.value.isCompleted) {
                    player.seekTo(0)
                    _uiState.update { it.copy(isCompleted = false, currentPositionMs = 0) }
                }
                player.start()
                _uiState.update { it.copy(isPlaying = true) }
                startTrackingProgress()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Ошибка при воспроизведении: ${e.localizedMessage}") }
            }
        }
    }

    fun pause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _uiState.update { it.copy(isPlaying = false) }
            stopTrackingProgress()
        }
    }

    fun seekTo(progress: Float) {
        val player = mediaPlayer ?: return
        val targetMs = (progress * _uiState.value.durationMs).toLong()
        player.seekTo(targetMs.toInt())
        _uiState.update { it.copy(currentPositionMs = targetMs) }
    }

    fun reset() {
        releasePlayer()
        _uiState.value = PlayerUiState()
    }

    private fun onPlaybackCompleted() {
        _uiState.update {
            it.copy(
                isPlaying = false,
                isCompleted = true,
                currentPositionMs = it.durationMs
            )
        }
        stopTrackingProgress()
    }

    private fun startTrackingProgress() {
        stopTrackingProgress()
        progressJob = viewModelScope.launch {
            while (true) {
                mediaPlayer?.let { player ->
                    try {
                        if (player.isPlaying) {
                            _uiState.update {
                                it.copy(currentPositionMs = player.currentPosition.toLong())
                            }
                        }
                    } catch (e: Exception) {
                        // ignore if player state transitions during lock
                    }
                }
                delay(200)
            }
        }
    }

    private fun stopTrackingProgress() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun releasePlayer() {
        stopTrackingProgress()
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
            } catch (e: Exception) {
                // Ignore
            }
            try {
                it.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
        mediaPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }

    private fun getSongMetadata(context: Context, uri: Uri): SongMetadata {
        var name = "Аудиофайл"
        var duration = 0L

        // Query display name
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    name = cursor.getString(nameIndex) ?: "Аудиофайл"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Try getting duration via retriever
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration = durationStr?.toLongOrNull() ?: 0L
            retriever.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return SongMetadata(name, duration)
    }
}
