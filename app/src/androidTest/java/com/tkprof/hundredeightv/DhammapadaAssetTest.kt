package com.tkprof.HundredEightV

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DhammapadaAssetTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun testDhammapadaP1() {
        verifyAssetFile("법구경_p1_108.json", 108, 1, 108)
    }

    @Test
    fun testDhammapadaP2() {
        verifyAssetFile("법구경_p2_105.json", 105, 109, 213)
    }

    @Test
    fun testDhammapadaP3() {
        verifyAssetFile("법구경_p3_109.json", 109, 214, 322)
    }

    @Test
    fun testDhammapadaP4() {
        verifyAssetFile("법구경_p4_106.json", 106, 323, 428)
    }

    private fun verifyAssetFile(fileName: String, expectedCount: Int, startId: Int, endId: Int) {
        val jsonString = Util.loadFile2String(context, fileName)
        assertFalse("File $fileName should not be empty", jsonString.isEmpty())

        val jsonArray = JSONArray(jsonString)
        assertEquals("Entry count mismatch for $fileName", expectedCount, jsonArray.length())

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val id = item.getInt("id")
            val text = item.getString("text")

            // Verify sequential IDs
            assertEquals("ID mismatch at index $i in $fileName", startId + i, id)

            // Verify no parentheses remain
            assertFalse("Text in $fileName (ID $id) still contains parentheses: $text", text.contains("(") || text.contains(")"))
            
            // Verify basic text content
            assertTrue("Text in $fileName (ID $id) is too short", text.length > 5)
        }
        
        assertEquals("Last ID mismatch in $fileName", endId, jsonArray.getJSONObject(jsonArray.length() - 1).getInt("id"))
    }
}
