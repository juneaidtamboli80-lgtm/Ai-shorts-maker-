package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.Converters
import com.example.engine.KeyframeInterpolator
import com.example.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Veloce Video AI", appName)
    }

    @Test
    fun `test keyframe interpolator at boundaries and midpoints`() {
        val keyframes = listOf(
            Keyframe(timeMs = 0L, posX = 0.2f, posY = 0.2f, scale = 1.0f),
            Keyframe(timeMs = 1000L, posX = 0.8f, posY = 0.8f, scale = 2.0f)
        )

        val atStart = KeyframeInterpolator.interpolate(keyframes, 0L)
        assertEquals(0.2f, atStart.posX, 0.01f)
        assertEquals(1.0f, atStart.scale, 0.01f)

        val atEnd = KeyframeInterpolator.interpolate(keyframes, 1000L)
        assertEquals(0.8f, atEnd.posX, 0.01f)
        assertEquals(2.0f, atEnd.scale, 0.01f)

        val atMid = KeyframeInterpolator.interpolate(keyframes, 500L)
        assertEquals(0.5f, atMid.posX, 0.05f)
        assertEquals(1.5f, atMid.scale, 0.05f)
    }

    @Test
    fun `test words serialization and deserialization`() {
        val words = listOf(
            WordTiming("Hello", 0L, 500L),
            WordTiming("World", 500L, 1000L)
        )
        val json = Converters.serializeWords(words)
        val deserialized = Converters.deserializeWords(json)
        assertEquals(2, deserialized.size)
        assertEquals("Hello", deserialized[0].word)
        assertEquals("World", deserialized[1].word)
    }

    @Test
    fun `test subtitle style serialization`() {
        val style = SubtitleStyle(
            textColorHex = "#38BDF8",
            activeWordColorHex = "#FDE047",
            animation = CaptionAnimation.KARAOKE_HIGHLIGHT
        )
        val json = Converters.serializeSubtitleStyle(style)
        val deserialized = Converters.deserializeSubtitleStyle(json)
        assertEquals("#38BDF8", deserialized.textColorHex)
        assertEquals("#FDE047", deserialized.activeWordColorHex)
        assertEquals(CaptionAnimation.KARAOKE_HIGHLIGHT, deserialized.animation)
    }
}
