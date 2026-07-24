package dev.anilbeesetti.nextplayer.core.common

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UtilsTest {

    @Test
    fun isLocalServerUrl_returnsTrueFor127001AndLocalhost() {
        assertTrue(Utils.isLocalServerUrl("http://127.0.0.1:41707/file/7312/?size=515008273&token=62d278b9-dac7-4330-8deb-3d72d85c2f09"))
        assertTrue(Utils.isLocalServerUrl("http://127.0.0.1:8080/stream.mp4"))
        assertTrue(Utils.isLocalServerUrl("http://localhost:5000/video.mkv"))
        assertTrue(Utils.isLocalServerUrl("https://127.0.0.1/test"))
    }

    @Test
    fun isLocalServerUrl_returnsFalseForRegularUrlsAndFiles() {
        assertFalse(Utils.isLocalServerUrl("/storage/emulated/0/Movies/video.mp4"))
        assertFalse(Utils.isLocalServerUrl("content://media/external/video/media/123"))
        assertFalse(Utils.isLocalServerUrl("https://example.com/video.mp4"))
    }
}
