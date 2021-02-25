package jab.spigot.language.util

import jab.spigot.language.LangArg
import jab.spigot.language.LangPackage
import jab.spigot.language.Language

interface IStringProcessor {

    /**
     * TODO: Document
     *
     * @param string
     */
    fun getFields(string: String): Array<String>

    /**
     * TODO: Document
     *
     * @param field
     */
    fun formatField(field: String): String

    /**
     * TODO: Document
     *
     * @param string
     * @param args
     */
    fun process(string: String, vararg args: LangArg): String

    /**
     * TODO: Document
     *
     * @param string
     * @param pkg
     * @param lang
     * @param args
     *
     * @return
     */
    fun process(
        string: String,
        pkg: LangPackage,
        lang: Language = Language.ENGLISH,
        vararg args: LangArg
    ): String
}