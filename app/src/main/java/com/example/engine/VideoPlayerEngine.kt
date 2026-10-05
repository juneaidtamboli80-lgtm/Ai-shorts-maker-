package com.example.engine

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.view.Surface
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VideoPlayerEngine(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var updateJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isLooping = MutableStateFlow(true)
    val isLooping: StateFlow<Boolean> = _isLooping.asStateFlow()

    private var currentUri: Uri? = null
    private var playbackSpeed: Float = 1.0f
    private var volumeLevel: Float = 1.0f
    private var isMuted: Boolean = false

    private var loopStartMs: Long = 0L
    private var loopEndMs: Long = Long.MAX_VALUE

    fun initializePlayer(uri: Uri, surface: Surface? = null, onPrepared: () -> Unit = {}) {
        release()
        currentUri = uri

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                if (surface != null && surface.isValid) {
                    setSurface(surface)
                }
                isLooping = false
                setOnPreparedListener { mp ->
                    val dur = mp.duration.toLong()
                    _durationMs.value = if (dur > 0) dur else 5000L
                    if (loopEndMs == Long.MAX_VALUE || loopEndMs > _durationMs.value) {
                        loopEndMs = _durationMs.value
                    }
                    applyAudioSettings()
                    applySpeed(playbackSpeed)
                    onPrepared()
                }
                setOnCompletionListener {
                    if (_isLooping.value) {
                        seekTo(loopStartMs)
                        play()
                    } else {
                        _isPlaying.value = false
                    }
                }
                setOnErrorListener { _, what, extra ->
                    _isPlaying.value = false
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        startPositionTracker()
    }

    fun setSurface(surface: Surface?) {
        try {
            if (surface != null && surface.isValid) {
                mediaPlayer?.setSurface(surface)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play() {
        try {
            mediaPlayer?.start()
            _isPlaying.value = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pause() {
        try {
            mediaPlayer?.pause()
            _isPlaying.value = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun seekTo(positionMs: Long) {
        val target = positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(1000L))
        _currentPositionMs.value = target
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mediaPlayer?.seekTo(target, MediaPlayer.SEEK_CLOSEST)
            } else {
                mediaPlayer?.seekTo(target.toInt())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stepForwardFrames(frameCount: Int = 1, fps: Int = 30) {
        val frameDurationMs = 1000L / fps
        seekTo(_currentPositionMs.value + (frameCount * frameDurationMs))
    }

    fun stepBackwardFrames(frameCount: Int = 1, fps: Int = 30) {
        val frameDurationMs = 1000L / fps
        seekTo(_currentPositionMs.value - (frameCount * frameDurationMs))
    }

    fun setTrimBounds(startMs: Long, endMs: Long) {
        this.loopStartMs = startMs
        this.loopEndMs = endMs
        if (_currentPositionMs.value < startMs || _currentPositionMs.value > endMs) {
            seekTo(startMs)
        }
    }

    fun setSpeed(speed: Float) {
        playbackSpeed = speed.coerceIn(0.25f, 4.0f)
        applySpeed(playbackSpeed)
    }

    private fun applySpeed(speed: Float) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying || _isPlaying.value) {
                        mp.playbackParams = mp.playbackParams.setSpeed(speed)
                    } else {
                        val params = PlaybackParams().setSpeed(speed)
                        mp.playbackParams = params
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore if device playback params fail
        }
    }

    fun setVolume(volume: Float, muted: Boolean = false) {
        this.volumeLevel = volume.coerceIn(0f, 2f)
        this.isMuted = muted
        applyAudioSettings()
    }

    private fun applyAudioSettings() {
        try {
            val vol = if (isMuted) 0f else volumeLevel.coerceIn(0f, 1f)
            mediaPlayer?.setVolume(vol, vol)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun startPositionTracker() {
        updateJob?.cancel()
        updateJob = coroutineScope.launch {
            while (isActive) {
                if (_isPlaying.value) {
                    try {
                        mediaPlayer?.let { mp ->
                            if (mp.isPlaying) {
                                val pos = mp.currentPosition.toLong()
                                if (pos >= loopEndMs && _isLooping.value) {
                                    seekTo(loopStartMs)
                                } else {
                                    _currentPositionMs.value = pos
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
                delay(30) // ~33fps UI position refresh
            }
        }
    }

    fun release() {
        updateJob?.cancel()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        mediaPlayer = null
        _isPlaying.value = false
    }
}
