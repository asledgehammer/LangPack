@file:Suppress("MemberVisibilityCanBePrivate")

package com.asledgehammer.langpack.textcomponent.objects.complex

import com.asledgehammer.cfg.CFGSection
import com.asledgehammer.langpack.core.LangPack
import com.asledgehammer.langpack.core.Language
import com.asledgehammer.langpack.core.objects.LangArg
import com.asledgehammer.langpack.core.objects.LangGroup
import com.asledgehammer.langpack.core.objects.complex.Complex
import com.asledgehammer.langpack.core.objects.definition.ComplexDefinition
import com.asledgehammer.langpack.core.objects.definition.LangDefinition
import com.asledgehammer.langpack.core.objects.formatter.FieldFormatter
import com.asledgehammer.langpack.core.processor.LangProcessor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text

/**
 * **ActionText** packages defined [HoverEvent] and [ClickEvent] as [TextComponentHoverText] and [TextComponentCommandText] wrappers.
 * The object is complex and resolvable for [LangProcessor].
 *
 * @author Jab
 */
open class TextComponentActionText : Complex<TextComponent> {

    override var definition: ComplexDefinition? = null

    /**
     * The text to display for the resolved [TextComponent].
     */
    var text: String

    /**
     * The text to display for the resolved [TextComponent] when hovered in chat.
     */
    var hoverText: TextComponentHoverText? = null

    /**
     * The command to execute for resolved [TextComponent] when clicked in chat.
     */
    var commandText: TextComponentCommandText? = null

    /**
     * None constructor.
     *
     * @param text The text to display.
     */
    constructor(text: String) {
        this.text = text
    }

    /**
     * Hover constructor.
     *
     * @param text The text to display.
     * @param hoverText The hover text to display.
     */
    constructor(text: String, hoverText: TextComponentHoverText) {
        this.text = text
        this.hoverText = hoverText
    }

    /**
     * Command constructor.
     *
     * @param text The text to display.
     * @param command The command to execute.
     */
    constructor(text: String, command: String) {
        this.text = text
        this.commandText = TextComponentCommandText(command)
    }

    /**
     * Full primitives constructor
     *
     * @param text The text to display.
     * @param command The command to execute.
     * @param hover The hover text to display.
     */
    constructor(text: String, command: String, hover: List<String>) {
        this.text = text
        this.commandText = TextComponentCommandText(command)
        this.hoverText = TextComponentHoverText(hover)
    }

    /**
     * Full objects constructor
     *
     * @param text The text to display.
     * @param commandText The command to execute.
     * @param hoverText The hover text to display.
     */
    constructor(text: String, commandText: TextComponentCommandText, hoverText: TextComponentHoverText) {
        this.text = text
        this.commandText = commandText
        this.hoverText = hoverText
    }

    /**
     * Import constructor.
     *
     * @param cfg The YAML to read.
     */
    constructor(cfg: CFGSection) {

        val readHoverText = fun(cfg: CFGSection) {

            if (cfg.contains("hover")) {
                val lines = ArrayList<Text>()
                if (cfg.isList("hover")) {
                    for (arg in cfg.getStringList("hover")) {
                        if (lines.isEmpty()) {
                            lines.add(Text(arg))
                        } else {
                            lines.add(Text("\n$arg"))
                        }
                    }
                } else {
                    lines.add(Text(cfg.getString("hover")))
                }
                hoverText = TextComponentHoverText(lines)
            }
        }

        text = cfg.getString("text")
        if (cfg.contains("hover")) readHoverText(cfg)
        if (cfg.contains("command")) {
            val line = cfg.getString("command")
            this.commandText = TextComponentCommandText(line)
        }
    }

    override fun process(pack: LangPack, lang: Language, context: LangGroup?, vararg args: LangArg): TextComponent {
        val text = pack.processor.process(text, pack, lang, context, *args)
        val component = TextComponent(text)
        if (hoverText != null) component.hoverEvent = hoverText!!.process(pack, lang, context, *args)
        if (commandText != null) component.clickEvent = commandText!!.process(pack, lang, context, *args)
        return component
    }

    override fun walk(definition: LangDefinition<*>): TextComponentActionText {
        val walked = TextComponentActionText(definition.walk(text))
        if (commandText != null) walked.commandText = commandText!!.walk(definition)
        if (hoverText != null) walked.hoverText = hoverText!!.walk(definition)
        return walked
    }

    override fun needsWalk(formatter: FieldFormatter): Boolean {
        if (formatter.needsWalk(text)) return true
        if (commandText != null && commandText!!.needsWalk(formatter)) return true
        else if (hoverText != null && hoverText!!.needsWalk(formatter)) return true
        return false
    }

    override fun get(): TextComponent {
        val component = TextComponent(text)
        if (hoverText != null) component.hoverEvent = hoverText!!.get()
        if (commandText != null) component.clickEvent = commandText!!.get()
        return component
    }

    /**
     * **ActionText.Loader** loads [TextComponentActionText] from YAML with the assigned type *action*.
     *
     * @author Jab
     */
    class Loader : Complex.Loader<TextComponentActionText> {
        override fun load(cfg: CFGSection): TextComponentActionText = TextComponentActionText(cfg)
    }
}
