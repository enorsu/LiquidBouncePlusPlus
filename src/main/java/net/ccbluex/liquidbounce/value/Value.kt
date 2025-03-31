/*
 * 31/03/2025
 * LiquidBounce++ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/PlusPlusMC/LiquidBouncePlusPlus/
 */
package net.ccbluex.liquidbounce.value

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.extensions.setAlpha
import net.minecraft.client.gui.FontRenderer
import java.awt.Color
import java.util.*

/**
 * Base class for all configurable values in the client.
 *
 * @param T The type of the value
 * @property name The name of the value
 * @property value The current value
 * @property canDisplay Lambda determining if this value should be displayed
 */
abstract class Value<T>(
    val name: String, 
    protected var value: T, 
    var canDisplay: () -> Boolean = { true }
) {
    /**
     * Sets the value and handles change events.
     * @param newValue The new value to set
     */
    fun set(newValue: T) {
        if (newValue == value) return

        val oldValue = get()

        try {
            onChange(oldValue, newValue)
            changeValue(newValue)
            onChanged(oldValue, newValue)
            LiquidBounce.fileManager.saveConfig(LiquidBounce.fileManager.valuesConfig)
        } catch (e: Exception) {
            ClientUtils.getLogger().error(
                "[ValueSystem ($name)]: ${e.javaClass.name} (${e.message}) [$oldValue >> $newValue]",
                e
            )
        }
    }

    /**
     * Gets the current value.
     */
    fun get() = value

    /**
     * Changes the value without triggering events.
     */
    open fun changeValue(value: T) {
        this.value = value
    }

    /**
     * Converts the value to JSON for saving.
     */
    abstract fun toJson(): JsonElement?

    /**
     * Loads the value from JSON.
     */
    abstract fun fromJson(element: JsonElement)

    /**
     * Called before the value changes.
     */
    protected open fun onChange(oldValue: T, newValue: T) {}

    /**
     * Called after the value has changed.
     */
    protected open fun onChanged(oldValue: T, newValue: T) {}
}

/**
 * Boolean value representation.
 */
open class BoolValue(
    name: String, 
    value: Boolean, 
    displayable: () -> Boolean = { true }
) : Value<Boolean>(name, value, displayable) {
    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive) {
            value = element.asBoolean || element.asString.equals("true", ignoreCase = true)
        }
    }
}

/**
 * Integer value with range constraints.
 *
 * @property minimum The minimum allowed value
 * @property maximum The maximum allowed value
 * @property suffix The suffix to display after the value (e.g., "ms", "blocks")
 */
open class IntegerValue(
    name: String,
    value: Int,
    val minimum: Int = 0,
    val maximum: Int = Int.MAX_VALUE,
    val suffix: String = "",
    displayable: () -> Boolean = { true }
) : Value<Int>(name, value, displayable) {
    /**
     * Sets the value from any Number type.
     */
    fun set(newValue: Number) {
        set(newValue.toInt().coerceIn(minimum, maximum))
    }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive) {
            value = element.asInt.coerceIn(minimum, maximum)
        }
    }
}

/**
 * Floating-point value with range constraints.
 *
 * @property minimum The minimum allowed value
 * @property maximum The maximum allowed value
 * @property suffix The suffix to display after the value
 */
open class FloatValue(
    name: String,
    value: Float,
    val minimum: Float = 0f,
    val maximum: Float = Float.MAX_VALUE,
    val suffix: String = "",
    displayable: () -> Boolean = { true }
) : Value<Float>(name, value, displayable) {
    /**
     * Sets the value from any Number type.
     */
    fun set(newValue: Number) {
        set(newValue.toFloat().coerceIn(minimum, maximum))
    }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive) {
            value = element.asFloat.coerceIn(minimum, maximum)
        }
    }
}

/**
 * Text string value.
 */
open class TextValue(
    name: String,
    value: String,
    displayable: () -> Boolean = { true }
) : Value<String>(name, value, displayable) {
    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive) {
            value = element.asString
        }
    }
}

/**
 * Color value with optional transparency support.
 *
 * @property transparent Whether alpha channel should be used
 */
open class ColorValue(
    name: String,
    value: Color,
    val transparent: Boolean = true,
    displayable: () -> Boolean = { true }
) : Value<Color>(name, value, displayable) {
    /**
     * Sets the color using HSB color model.
     */
    fun set(hue: Float, saturation: Float, brightness: Float, alpha: Float) =
        set(Color(Color.HSBtoRGB(hue, saturation, brightness)).setAlpha(alpha))

    override fun toJson(): JsonElement {
        return JsonObject().apply {
            addProperty("red", value.red)
            addProperty("green", value.green)
            addProperty("blue", value.blue)
            addProperty("alpha", value.alpha)
        }
    }

    override fun fromJson(element: JsonElement) {
        if (element.isJsonObject) {
            with(element.asJsonObject) {
                value = Color(
                    get("red").asInt,
                    get("green").asInt,
                    get("blue").asInt,
                    get("alpha").asInt
                )
            }
        }
    }
}

/**
 * Font renderer value.
 */
class FontValue(
    name: String,
    value: FontRenderer,
    displayable: () -> Boolean = { true }
) : Value<FontRenderer>(name, value, displayable) {
    override fun toJson(): JsonElement? {
        val fontDetails = Fonts.getFontDetails(value) ?: return null
        return JsonObject().apply {
            addProperty("fontName", fontDetails[0] as String)
            addProperty("fontSize", fontDetails[1] as Int)
        }
    }

    override fun fromJson(element: JsonElement) {
        if (element.isJsonObject) {
            with(element.asJsonObject) {
                value = Fonts.getFontRenderer(
                    get("fontName").asString,
                    get("fontSize").asInt
                )
            }
        }
    }
}

/**
 * Block ID value with valid Minecraft block ID range.
 */
class BlockValue(
    name: String,
    value: Int,
    displayable: () -> Boolean = { true }
) : IntegerValue(name, value, 1, 197, displayable)

/**
 * Selectable list of string values.
 *
 * @property values The available options in the list
 * @property openList Whether the selection list should be displayed as open
 */
open class ListValue(
    name: String,
    val values: Array<String>,
    value: String,
    displayable: () -> Boolean = { true }
) : Value<String>(name, value, displayable) {
    var openList = false

    init {
        // Ensure the initial value is valid
        changeValue(value)
    }

    /**
     * Checks if the list contains a specific string (case-insensitive).
     */
    operator fun contains(string: String?): Boolean {
        return values.any { it.equals(string, ignoreCase = true) }
    }

    /**
     * Changes the value ensuring it's one of the valid options.
     */
    override fun changeValue(value: String) {
        this.value = values.firstOrNull { it.equals(value, ignoreCase = true) } ?: this.value
    }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive) {
            changeValue(element.asString)
        }
    }
}
