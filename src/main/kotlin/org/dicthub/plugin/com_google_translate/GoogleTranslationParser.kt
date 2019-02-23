package org.dicthub.plugin.com_google_translate

class GoogleTranslationParser {

    fun parse(sourceUrl: String, jsonStr: String, from: String, to: String): GoogleTranslation? {

        val json = JSON.parse<Array<*>>(jsonStr)

        val firstLine = json.getOrNull(0) as? Array<*> ?: return null

        val text = firstLine.getOrNull(0)?.let { it as? Array<*> } ?: return null
        val pron = firstLine.getOrNull(1) ?.let { it as? Array<*> } ?: return null


        return GoogleTranslation(
                sourceUrl = sourceUrl,
                query = text[1] as String,
                translation = text[0] as String,
                pron = pron[3] as String?,
                from = from,
                to = to,
                details = json.getOrNull(1)?.let { it as? Array<*> }?.let { parseTranslationDetails(it) } ?: emptyList())
    }

    private fun parseTranslationDetails(secondLine: Array<*>): List<Detail> {

        return secondLine.filterIsInstance<Array<*>>().map { line ->
            val type = line.getOrNull(0) as String
            val meanings = line.getOrNull(2)?.let { it as Array<*> }?.map { info ->
                val arr = info as Array<*>
                Meaning(
                        meaning = arr.getOrNull(0) as String,
                        examples = arr.getOrNull(1)?.let { it as Array<*> }?.filterIsInstance<String>()?.toList()
                                ?: emptyList()
                )
            } ?: emptyList()

            Detail(poc = type, meanings = meanings)
        }
    }
}