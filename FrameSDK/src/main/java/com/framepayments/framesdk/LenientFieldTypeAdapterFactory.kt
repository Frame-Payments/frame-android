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
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

/**
 * Degrades a field with the wrong JSON type to null instead of failing the whole object,
 * matching iOS's `@Lenient` property wrapper. A field whose Kotlin-declared type cannot hold
 * null still throws, since there is nothing safe to degrade it to.
 *
 * `field.type.isPrimitive` alone is not enough to detect that: it is true only for JVM
 * primitives (`int`, `boolean`, …), not for a Kotlin non-null reference type like `String` or
 * `List<T>` — those are reference types at the JVM level, so a naive check would silently null
 * out a field Kotlin's type system promises can never be null, surfacing as an unrelated NPE far
 * from this deserialization site whenever the field is later read. Kotlin's own nullability
 * annotations use class-file (not runtime) retention, so distinguishing the two requires
 * `kotlin-reflect`'s `KProperty.returnType.isMarkedNullable` rather than plain [Field] reflection.
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
                    if (!isNullable(field)) continue // non-nullable: let the delegate throw
                    if (!decodesCleanly(gson, field, working.get(key))) working.remove(key)
                }
                return delegate.fromJsonTree(working)
            }
        }
    }

    /**
     * Whether [field] can safely hold null: a JVM primitive cannot, and neither can a Kotlin
     * property whose declared type is non-null. Any field this can't resolve to a Kotlin
     * property (a synthetic field, or a class kotlin-reflect can't introspect) is treated as
     * non-nullable — the conservative default, since removing the key is only ever safe when we
     * can prove it is.
     */
    private fun isNullable(field: Field): Boolean {
        if (field.type.isPrimitive) return false
        val property = field.declaringClass.kotlin.memberProperties
            .find { it.javaField == field } ?: return false
        return property.returnType.isMarkedNullable
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
