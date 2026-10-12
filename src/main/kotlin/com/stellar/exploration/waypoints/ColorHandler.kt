package com.stellar.exploration.waypoints

import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.util.ARGB
import net.minecraft.world.item.ItemStack
import java.util.regex.Pattern

/**
 * Utility for parsing and stripping custom color codes from waypoint items and names.
 */
object ColorHandler {
    private val HEX_PATTERN: Pattern = Pattern.compile("#[0-9a-fA-F]{6}")
    private val CODE_PATTERN: Pattern = Pattern.compile("[§&][0-9a-f]")

    private const val HEX_RADIX: Int = 16
    private const val HEX_START: Int = 1
    private const val HEX_END: Int = 7
    private const val HEX_STRING_LENGTH: Int = 7
    private const val FORMAT_CODE_MIN_LENGTH: Int = 2
    private const val ALPHA_OPAQUE: Int = 255

    fun getColor(stack: ItemStack): Int? {
        val customName = stack.get(DataComponents.CUSTOM_NAME)
        val customColor = getColor(customName)
        if (customColor != null) {
            return customColor
        }
        val itemName = stack.get(DataComponents.ITEM_NAME)
        return getColor(itemName)
    }

    fun getColor(component: Component?): Int? {
        if (component == null) return null
        return getColor(component.string)
    }

    fun getColor(text: String?): Int? {
        if (text.isNullOrBlank()) return null

        val hexMatcher = HEX_PATTERN.matcher(text)
        if (hexMatcher.find()) {
            return parseHexCode(hexMatcher.group())
        }

        val codeMatcher = CODE_PATTERN.matcher(text)
        if (codeMatcher.find()) {
            return parseFormattingCode(codeMatcher.group())
        }

        return null
    }

    fun parseHexCode(string: String?): Int? {
        if (string.isNullOrBlank() || string.length != HEX_STRING_LENGTH) return null
        return try {
            val rgb = string.substring(HEX_START, HEX_END).toInt(HEX_RADIX)
            ARGB.color(ALPHA_OPAQUE, rgb)
        } catch (_: NumberFormatException) {
            null
        }
    }

    fun parseFormattingCode(string: String?): Int? {
        if (string == null || string.length < FORMAT_CODE_MIN_LENGTH) return null
        val formatting = ChatFormatting.getByCode(string[1]) ?: return null
        val textColor = TextColor.fromLegacyFormat(formatting) ?: return null
        return ARGB.color(ALPHA_OPAQUE, textColor.value)
    }

    fun extractCustomName(stack: ItemStack): Component? {
        val customName = stack.get(DataComponents.CUSTOM_NAME) ?: return null
        val cleaned = removeColorCodes(customName.string)
        return if (cleaned.isNotBlank()) Component.literal(cleaned) else null
    }

    fun extractDisplayName(stack: ItemStack): Component? = extractCustomName(stack)

    fun removeColorCodes(text: String): String = text
        .replace(Regex("( ?)([({<\\[]?)(#[0-9a-fA-F]{6})([)}>\\]]?)"), "")
        .replace(Regex("[§&][0-9a-fA-Fk-orK-OR]"), "")
        .trim()
}
