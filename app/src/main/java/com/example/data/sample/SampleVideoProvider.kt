package com.example.data.sample

import android.content.Context
import android.graphics.*
import android.media.*
import android.net.Uri
import com.example.data.database.CaptionEntity
import com.example.data.database.Converters
import com.example.data.database.ProjectEntity
import com.example.data.database.TextLayerEntity
import com.example.data.database.TrackingObjectEntity
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

object SampleVideoProvider {

    data class SampleProjectTemplate(
        val title: String,
        val durationMs: Long,
        val description: String,
        val width: Int = 1080,
        val height: Int = 1920,
        val defaultCaptions: List<CaptionSegment>,
        val defaultTextLayers: List<TextLayer>,
        val defaultTracking: List<TrackingObject>
    )

    fun getSampleTemplates(): List<SampleProjectTemplate> {
        val sample1Captions = listOf(
            CaptionSegment(
                id = "cap_1",
                projectId = "sample_1",
                startMs = 0L,
                endMs = 2800L,
                text = "Welcome to the future of AI video editing on Android!",
                words = listOf(
                    WordTiming("Welcome", 0L, 400L),
                    WordTiming("to", 400L, 600L),
                    WordTiming("the", 600L, 800L),
                    WordTiming("future", 800L, 1300L),
                    WordTiming("of", 1300L, 1500L),
                    WordTiming("AI", 1500L, 1900L),
                    WordTiming("video", 1900L, 2300L),
                    WordTiming("editing", 2300L, 2800L)
                ),
                style = SubtitleStyle(
                    textColorHex = "#FFFFFF",
                    activeWordColorHex = "#FACC15",
                    animation = CaptionAnimation.SCALE_ACTIVE
                )
            ),
            CaptionSegment(
                id = "cap_2",
                projectId = "sample_1",
                startMs = 2900L,
                endMs = 5800L,
                text = "Auto-captions, object tracking, and keyframe animations.",
                words = listOf(
                    WordTiming("Auto-captions,", 2900L, 3600L),
                    WordTiming("object", 3600L, 4100L),
                    WordTiming("tracking,", 4100L, 4700L),
                    WordTiming("and", 4700L, 4900L),
                    WordTiming("keyframe", 4900L, 5400L),
                    WordTiming("animations.", 5400L, 5800L)
                ),
                style = SubtitleStyle(
                    textColorHex = "#38BDF8",
                    activeWordColorHex = "#EC4899",
                    animation = CaptionAnimation.KARAOKE_HIGHLIGHT
                )
            ),
            CaptionSegment(
                id = "cap_3",
                projectId = "sample_1",
                startMs = 5900L,
                endMs = 8500L,
                text = "Create viral shorts in seconds with smart auto-crop.",
                words = listOf(
                    WordTiming("Create", 5900L, 6300L),
                    WordTiming("viral", 6300L, 6800L),
                    WordTiming("shorts", 6800L, 7300L),
                    WordTiming("in", 7300L, 7500L),
                    WordTiming("seconds", 7500L, 8000L),
                    WordTiming("with", 8000L, 8200L),
                    WordTiming("AI.", 8200L, 8500L)
                ),
                style = SubtitleStyle(
                    textColorHex = "#FFFFFF",
                    activeWordColorHex = "#34D399",
                    animation = CaptionAnimation.POP
                )
            )
        )

        val sample1Tracking = listOf(
            TrackingObject(
                id = "track_1",
                projectId = "sample_1",
                label = "Speaker Face",
                isFace = true,
                startMs = 0L,
                endMs = 8500L,
                sensitivity = 0.85f,
                smoothing = 0.4f,
                attachmentType = TrackedAttachmentType.TEXT,
                attachedText = "Alex Rivers • Creator",
                boundingBoxes = listOf(
                    BoundingBoxKeyframe(0L, 0.35f, 0.25f, 0.65f, 0.55f),
                    BoundingBoxKeyframe(2000L, 0.32f, 0.26f, 0.62f, 0.56f),
                    BoundingBoxKeyframe(4000L, 0.38f, 0.24f, 0.68f, 0.54f),
                    BoundingBoxKeyframe(6000L, 0.34f, 0.27f, 0.64f, 0.57f),
                    BoundingBoxKeyframe(8500L, 0.35f, 0.25f, 0.65f, 0.55f)
                )
            )
        )

        val sample1TextLayers = listOf(
            TextLayer(
                id = "text_1",
                projectId = "sample_1",
                text = "⚡ VELOCE AI STUDIO",
                startMs = 0L,
                endMs = 4000L,
                posX = 0.5f,
                posY = 0.15f,
                scale = 1.0f,
                fontFamily = "SansSerif",
                fontSizeSp = 20f,
                isBold = true,
                colorHex = "#38BDF8",
                backgroundColorHex = "#0D1117",
                backgroundOpacity = 0.6f,
                animation = LayerAnimationType.ZOOM_IN,
                keyframes = listOf(
                    Keyframe(0L, 0.5f, 0.15f, 0.8f, 0f, 0f),
                    Keyframe(500L, 0.5f, 0.15f, 1.0f, 0f, 1f),
                    Keyframe(3500L, 0.5f, 0.15f, 1.0f, 0f, 1f),
                    Keyframe(4000L, 0.5f, 0.15f, 1.1f, 0f, 0f)
                )
            )
        )

        return listOf(
            SampleProjectTemplate(
                title = "AI Video Showcase (Demo)",
                durationMs = 9000L,
                description = "Vertical 9:16 demo with karaoke captions, facial tracking, and animated overlays.",
                width = 1080,
                height = 1920,
                defaultCaptions = sample1Captions,
                defaultTextLayers = sample1TextLayers,
                defaultTracking = sample1Tracking
            ),
            SampleProjectTemplate(
                title = "Podcast Highlights 9:16",
                durationMs = 12000L,
                description = "Perfect template for podcast clips with speaker detection & active word pop animation.",
                width = 1080,
                height = 1920,
                defaultCaptions = listOf(
                    CaptionSegment(
                        id = "pod_1",
                        projectId = "sample_2",
                        startMs = 0L,
                        endMs = 5000L,
                        text = "The biggest secret to high retention is fast-paced captions.",
                        words = listOf(
                            WordTiming("The", 0L, 400L),
                            WordTiming("biggest", 400L, 900L),
                            WordTiming("secret", 900L, 1500L),
                            WordTiming("to", 1500L, 1700L),
                            WordTiming("high", 1700L, 2100L),
                            WordTiming("retention", 2100L, 2900L),
                            WordTiming("is", 2900L, 3100L),
                            WordTiming("fast-paced", 3100L, 4000L),
                            WordTiming("captions.", 4000L, 5000L)
                        ),
                        style = SubtitleStyle(
                            textColorHex = "#FFFFFF",
                            activeWordColorHex = "#A855F7",
                            animation = CaptionAnimation.BOUNCE
                        )
                    )
                ),
                defaultTextLayers = emptyList(),
                defaultTracking = emptyList()
            )
        )
    }

    /**
     * Generates a smooth, high-quality test video file (.mp4) using MediaCodec and MediaMuxer
     * if no video exists yet, ensuring immediate playable previews and real video editing testing.
     */
    suspend fun generateOrGetSampleVideoFile(context: Context, templateIndex: Int = 0): File = withContext(Dispatchers.IO) {
        val sampleDir = File(context.filesDir, "sample_videos")
        if (!sampleDir.exists()) sampleDir.mkdirs()

        val videoFile = File(sampleDir, "sample_video_$templateIndex.mp4")
        if (videoFile.exists() && videoFile.length() > 5000) {
            return@withContext videoFile
        }

        try {
            createSyntheticMp4Video(videoFile, width = 720, height = 1280, durationMs = 9000L, fps = 30)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: create empty placeholder file if hardware encoder is unavailable in environment
            if (!videoFile.exists()) {
                videoFile.createNewFile()
                FileOutputStream(videoFile).use { it.write(ByteArray(1024)) }
            }
        }
        return@withContext videoFile
    }

    private fun createSyntheticMp4Video(outputFile: File, width: Int, height: Int, durationMs: Long, fps: Int) {
        val mimeType = "video/avc"
        val bitRate = 2_500_000
        val frameIntervalMs = 1000L / fps
        val totalFrames = (durationMs / frameIntervalMs).toInt()

        val format = MediaFormat.createVideoFormat(mimeType, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        val encoder = MediaCodec.createEncoderByType(mimeType)
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val inputSurface = encoder.createInputSurface()
        encoder.start()

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var trackIndex = -1
        var muxerStarted = false

        val bufferInfo = MediaCodec.BufferInfo()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 44f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#38BDF8")
            textSize = 28f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.CENTER
        }

        for (frame in 0 until totalFrames) {
            val presentationTimeUs = frame * (1_000_000L / fps)

            // Draw frame to input surface
            val canvas = inputSurface.lockCanvas(null)
            try {
                val progress = frame.toFloat() / totalFrames.toFloat()
                
                // Cinematic dynamic gradient background
                val hue = (progress * 360f) % 360f
                val baseColor = Color.HSVToColor(floatArrayOf(hue, 0.45f, 0.25f))
                canvas.drawColor(baseColor)

                // Draw speaker avatar circle with dynamic motion
                val speakerX = width / 2f + Math.sin(frame * 0.08).toFloat() * 40f
                val speakerY = height * 0.38f + Math.cos(frame * 0.05).toFloat() * 20f
                val radius = 160f

                // Outer neon ring
                paint.color = Color.parseColor("#6366F1")
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 10f
                canvas.drawCircle(speakerX, speakerY, radius + 15f, paint)

                // Face circle fill
                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#1E293B")
                canvas.drawCircle(speakerX, speakerY, radius, paint)

                // Face icon / smile
                paint.color = Color.parseColor("#FDE047")
                canvas.drawCircle(speakerX - 45f, speakerY - 20f, 18f, paint)
                canvas.drawCircle(speakerX + 45f, speakerY - 20f, 18f, paint)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 8f
                val mouthRect = RectF(speakerX - 45f, speakerY + 10f, speakerX + 45f, speakerY + 60f)
                canvas.drawArc(mouthRect, 0f, 180f, false, paint)

                // Title overlay
                canvas.drawText("⚡ VELOCE AI STUDIO", width / 2f, height * 0.15f, textPaint)
                canvas.drawText("Frame $frame / $totalFrames • ${(frame * frameIntervalMs) / 1000f}s", width / 2f, height * 0.20f, subTextPaint)

                // Audio waveform bars visualization at bottom
                val barCount = 24
                val barWidth = (width - 120f) / barCount
                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#38BDF8")
                for (b in 0 until barCount) {
                    val barHeight = 20f + Math.abs(Math.sin(frame * 0.15 + b * 0.4)).toFloat() * 120f
                    val left = 60f + b * barWidth + 4f
                    val right = left + barWidth - 8f
                    val bottom = height * 0.90f
                    val top = bottom - barHeight
                    canvas.drawRoundRect(left, top, right, bottom, 8f, 8f, paint)
                }

            } finally {
                inputSurface.unlockCanvasAndPost(canvas)
            }

            // Drain encoder output
            while (true) {
                val outputBufferId = encoder.dequeueOutputBuffer(bufferInfo, 10000)
                if (outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    break
                } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val newFormat = encoder.outputFormat
                    trackIndex = muxer.addTrack(newFormat)
                    muxer.start()
                    muxerStarted = true
                } else if (outputBufferId >= 0) {
                    val encodedData = encoder.getOutputBuffer(outputBufferId)
                    if (encodedData != null && muxerStarted && (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                        bufferInfo.presentationTimeUs = presentationTimeUs
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    }
                    encoder.releaseOutputBuffer(outputBufferId, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break
                    }
                }
            }
        }

        encoder.signalEndOfInputStream()
        
        // Drain remaining frames
        var eos = false
        while (!eos) {
            val outputBufferId = encoder.dequeueOutputBuffer(bufferInfo, 10000)
            if (outputBufferId >= 0) {
                val encodedData = encoder.getOutputBuffer(outputBufferId)
                if (encodedData != null && muxerStarted && (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                    muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                }
                encoder.releaseOutputBuffer(outputBufferId, false)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    eos = true
                }
            } else if (outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break
            }
        }

        try {
            encoder.stop()
            encoder.release()
            if (muxerStarted) {
                muxer.stop()
                muxer.release()
            }
        } catch (e: Exception) {
            // Cleanup
        }
    }
}
