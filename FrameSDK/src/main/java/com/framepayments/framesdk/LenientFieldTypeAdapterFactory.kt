package com.framepayments.framesdk

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * Degrades a field with the wrong JSON type to null instead of failing the whole object,
 * matching iOS's `@Lenient` property wrapper. A field whose declared type cannot hold null (a
 * non-nullable primitive) still throws, since there is nothing safe to degrade it to.
 *
 * Registered once on [FrameNetworking.gson] rather than applied per field.
 */
object LenientFieldTypeAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val fieldsByJsonName = fieldsByJsonName(type.rawType)
        if (fieldsByJsonName.isEmpty()) return null

        val delegate = gson.getDelegateAdapter(this, type)
        val elementAdapter = gson.getAdapter(JsonElement::class.java)

        return object : TypeAdapter<T>() {
            override fun write(out: JsonWriter, value: T) = delegate.write(out, value)

            override fun read(input: JsonReader): T {
                val tree = elementAdapter.read(input)
                if (tree !is JsonObject) return delegate.fromJsonTree(tree)

                val working = tree.deepCopy()
                for (key in working.keySet().toList()) {
                    val field = fieldsByJsonName[key] ?: continue
                    if (field.type.isPrimitive) continue // required field: let the delegate throw
                    if (!decodesCleanly(gson, field, working.get(key))) working.remove(key)
                }
                return delegate.fromJsonTree(working)
            }
        }
    }

    private fun decodesCleanly(gson: Gson, field: Field, value: JsonElement): Boolean =
        try {
            gson.getAdapter(TypeToken.get(field.genericType)).fromJsonTree(value)
            true
        } catch (e: RuntimeException) {
            false
        }

    private fun fieldsByJsonName(raw: Class<*>): Map<String, Field> {
        val map = LinkedHashMap<String, Field>()
        var current: Class<*>? = raw
        while (current != null && current != Any::class.java) {
            for (field in current.declaredFields) {
                if (Modifier.isStatic(field.modifiers) || field.isSynthetic) continue
                val jsonName = field.getAnnotation(SerializedName::class.java)?.value ?: field.name
                map.putIfAbsent(jsonName, field)
            }
            current = current.superclass
        }
        return map
    }
}
