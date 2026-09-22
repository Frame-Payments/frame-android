package com.framepayments.framesdk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import com.google.gson.JsonSyntaxException

class LenientFieldTypeAdapterFactoryTest {

    data class NullableField(val id: String, val count: Int? = null)
    data class NonNullField(val id: String, val count: Int = 0)

    @Test
    fun `degrades a nullable field with the wrong JSON type to null`() {
        val json = """{"id":"a","count":"not a number"}"""
        val result = FrameNetworking.gson.fromJson(json, NullableField::class.java)
        assertEquals("a", result.id)
        assertNull(result.count)
    }

    @Test
    fun `does not degrade a non-null field with the wrong JSON type`() {
        val json = """{"id":"a","count":"not a number"}"""
        // count is non-null Int; the malformed value must not be silently dropped to a
        // default-constructed 0 or otherwise masked -- the delegate should still fail.
        assertThrows(JsonSyntaxException::class.java) {
            FrameNetworking.gson.fromJson(json, NonNullField::class.java)
        }
    }

    @Test
    fun `still degrades a JVM-primitive-backed nullable field`() {
        val json = """{"id":"a","count":null}"""
        val result = FrameNetworking.gson.fromJson(json, NullableField::class.java)
        assertEquals("a", result.id)
        assertNull(result.count)
    }
}
