package jab.sledgehammer.langpack.sponge.processor

import jab.sledgehammer.langpack.core.LangPack
import jab.sledgehammer.langpack.core.Language
import jab.sledgehammer.langpack.core.objects.LangArg
import jab.sledgehammer.langpack.core.objects.LangGroup
import jab.sledgehammer.langpack.core.objects.complex.Complex
import jab.sledgehammer.langpack.core.objects.definition.LangDefinition
import jab.sledgehammer.langpack.core.objects.definition.StringDefinition
import jab.sledgehammer.langpack.core.objects.formatter.FieldFormatter
import jab.sledgehammer.langpack.core.processor.DefaultProcessor
import jab.sledgehammer.langpack.sponge.util.ChatUtil
import jab.sledgehammer.langpack.sponge.util.text.ClickEvent
import jab.sledgehammer.langpack.sponge.util.text.HoverEvent
import jab.sledgehammer.langpack.sponge.util.text.TextComponent

/**
 * **SpongeProcessor** processes queries for the Sponge-version of LangPack.
 *
 *  @author Jab
 */
class SpongeProcessor(formatter: FieldFormatter) : DefaultProcessor(formatter) {

    /**
     * Processes a text component, inserting arguments and fields set in the lang-pack.
     *
     * @param component The text component to process.
     * @param pack The package instance.
     * @param lang The language context.
     * @param args (Optional) The arguments to process into the string.
     *
     * @return Returns the processed string.
     */
    fun process(
        component: TextComponent, pack: LangPack, lang: Language, context: LangGroup?, vararg args: LangArg,
    ): TextComponent {

        // There's no need to slice a component that is only a field.
        val composition = if (!formatter.isField(component.text)) {
            ChatUtil.slice(component, pack.formatter)
        } else {
            val comp = TextComponent(component.text)
            if (component.extra != null) comp.extra = component.extra
            comp
        }

        composition.hoverEvent = component.hoverEvent
        composition.clickEvent = component.clickEvent

        // Process fields as extras, removing the text after processing it.
        val eraseText = processText(composition, pack, lang, context, *args)
        processHoverEvent(composition, pack, lang, context, *args)
        processClickEvent(composition, pack, lang, context, *args)
        processExtras(composition, pack, lang, context, *args)
        if (eraseText) composition.text = ""

        ChatUtil.spreadColor(composition)
        return composition
    }

    /**
     * Processes a text component, inserting provided arguments.
     *
     * @param component The component to process.
     * @param args (Optional) The arguments to process into the string.
     *
     * @return Returns the processed component.
     */
    fun process(component: TextComponent, vararg args: LangArg): TextComponent {
        // There's no need to slice a component that is only a field.
        val composition = if (!formatter.isField(component.text)) {
            ChatUtil.slice(component, formatter)
        } else {
            val comp = TextComponent(component.text)
            if (component.extra != null) {
                comp.extra = component.extra
            }
            comp
        }

        composition.hoverEvent = component.hoverEvent
        composition.clickEvent = component.clickEvent

        // Process fields as extras, removing the text after processing it.
        val eraseText = processText(composition, *args)
        processHoverEvent(composition, *args)
        processClickEvent(composition, *args)
        processExtras(composition, *args)
        if (eraseText) composition.text = ""

        ChatUtil.spreadColor(composition)
        return composition
    }

    override fun color(string: String, colorCode: Char): String = ChatUtil.color(string, colorCode)

    private fun processText(
        component: TextComponent,
        pack: LangPack,
        lang: Language,
        context: LangGroup?,
        vararg args: LangArg,
    ): Boolean {
        with(component) {
            var eraseText = false
            if (formatter.isField(text)) {
                val fieldStripped = formatter.strip(text!!)
                var found = false
                var field: LangDefinition<*>? = null
                for (arg in args) {
                    if (arg.key.equals(fieldStripped, true)) {
                        field = StringDefinition(pack,
                            context,
                            jab.sledgehammer.langpack.core.util.StringUtil.toAString(arg.value))
                        found = true
                        break
                    }
                }
                if (!found) {
                    field = pack.resolve(fieldStripped, lang, context)
                        ?: pack.resolve(fieldStripped, pack.defaultLang, context)
                }
                if (field != null) {
                    when (field.value) {
                        is Complex<*> -> {
                            val result = (field.value as Complex<*>).get()
                            val processedComponent: TextComponent = if (result is TextComponent) {
                                result
                            } else {
                                TextComponent(result.toString())
                            }

                            // If the reference is forced to global scope, do not provide context.
                            val parent = if (formatter.isPackageScope(text!!)) {
                                null
                            } else {
                                field.parent
                            }

                            addExtra(process(processedComponent, pack, lang, parent, *args))
                            eraseText = true
                        }
                        else -> {
                            text = process(field.value.toString(), pack, lang, field.parent, *args)
                        }
                    }
                } else {
                    text = color(formatter.getPlaceholder(text!!))
                }
            } else {
                text = color(text!!)
            }

            return eraseText
        }
    }

    private fun processText(composition: TextComponent, vararg args: LangArg): Boolean {
        with(composition) {
            var eraseText = false
            if (formatter.isField(text)) {

                val fField = formatter.strip(text!!)
                var field: Any? = null
                for (arg in args) {
                    if (arg.key.equals(fField, true)) {
                        field = arg.value
                        break
                    }
                }

                if (field != null) {
                    when (field) {
                        is TextComponent -> {
                            addExtra(process(field, *args))
                            eraseText = true
                        }
                        is Complex<*> -> {
                            val result = field.get()
                            val processedComponent: TextComponent = if (result is TextComponent) {
                                result
                            } else {
                                TextComponent(result.toString())
                            }
                            addExtra(process(processedComponent, *args))
                            eraseText = true
                        }
                        else -> {
                            text = process(field.toString(), *args)
                        }
                    }
                } else {
                    text = fField
                }
            } else {
                text = color(text!!)
            }
            return eraseText
        }
    }

    private fun processHoverEvent(
        component: TextComponent,
        pack: LangPack,
        lang: Language,
        context: LangGroup?,
        vararg args: LangArg,
    ) {
        with(component) {
            if (hoverEvent == null) return
            val newContents = ArrayList<String>()
            for (content in hoverEvent!!.contents) {
                newContents.add(process(content, pack, lang, context, *args))
            }
            hoverEvent = HoverEvent(newContents)
        }
    }

    private fun processHoverEvent(component: TextComponent, vararg args: LangArg) {
        with(component) {
            if (hoverEvent == null) return
            val newContents = ArrayList<String>()
            for (content in hoverEvent!!.contents) {
                newContents.add(process(content, *args))
            }
            hoverEvent = HoverEvent(newContents)
        }
    }

    private fun processClickEvent(
        component: TextComponent,
        pack: LangPack,
        lang: Language,
        context: LangGroup?,
        vararg args: LangArg,
    ) {
        with(component) {
            if (clickEvent == null) return
            val value = process(clickEvent!!.value, pack, lang, context, *args)
            clickEvent = ClickEvent(value)
        }
    }

    private fun processClickEvent(composition: TextComponent, vararg args: LangArg) {
        with(composition) {
            if (clickEvent == null) return
            val value = process(clickEvent!!.value, *args)
            clickEvent = ClickEvent(value)
        }
    }

    private fun processExtras(
        component: TextComponent,
        pack: LangPack,
        lang: Language,
        context: LangGroup?,
        vararg args: LangArg,
    ) {
        with(component) {
            if (extra == null || formatter.isField(text)) return
            val newExtras = ArrayList<TextComponent>()
            for (next in extra!!) {
                newExtras.add(process(next, pack, lang, context, *args))
            }
            extra!!.clear()
            extra!!.addAll(newExtras)
        }
    }

    private fun processExtras(component: TextComponent, vararg args: LangArg) {
        with(component) {
            if (extra == null || formatter.isField(text)) return
            val newExtras = ArrayList<TextComponent>()
            for (next in extra!!) {
                newExtras.add(process(next, *args))
            }
            extra!!.clear()
            extra!!.addAll(newExtras)
        }
    }
}
