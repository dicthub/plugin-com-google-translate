package org.dicthub.plugin.com_google_translate


data class GoogleTranslation(
        val sourceUrl: String,
        val query: String,
        val from: String,
        val to: String,
        val pron: String?,
        val translation: String,
        val details: List<Detail>
)

data class Detail(
        val poc: String,
        val meanings: List<Meaning>
)

data class Meaning(
        val meaning: String,
        val examples: List<String>
)
