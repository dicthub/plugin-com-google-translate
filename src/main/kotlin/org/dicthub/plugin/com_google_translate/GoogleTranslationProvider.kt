package org.dicthub.plugin.com_google_translate


import org.dicthub.plugin.shared.util.*
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.browser.localStorage
import kotlin.js.Promise
import kotlin.js.json

const val ID = "plugin-com-google-translate"

const val DOMAIN_TRANSLATE_GOOGLE_COM = "https://translate.google.com"
const val DOMAIN_TRANSLATE_GOOGLE_CN = "https://translate.google.cn"

private const val OPTION_USE_GOOGLE_CN = "useGoogleCn"

private var useCnDomain = false
fun baseUrl() = if (useCnDomain) DOMAIN_TRANSLATE_GOOGLE_CN else DOMAIN_TRANSLATE_GOOGLE_COM

class GoogleTranslationProvider constructor(
        private val httpClient: HttpAsyncClient,
        private val googleTranslationParser: GoogleTranslationParser,
        private val googleTranslationRenderer: GoogleTranslationRenderer) : TranslationProvider {

    override fun id() = ID

    override fun meta() = createMeta(
            name = "Google Translation",
            description = "Provides all language translation",
            source = "Google Translate",
            sourceUrl = baseUrl(),
            author = "DictHub",
            authorUrl = "https://github.com/willings/DictHub",
            options = json(
                    "useGoogleCn" to createMetaOptionConfig(
                            type = "checkbox",
                            description = "Use https://translate.google.cn/ to avoid GFW",
                            default = "false"
                    )
            )
    )

    override fun canTranslate(query: Query) = query.getFrom() != query.getTo()

    override fun translate(query: Query): Promise<String> {
        return Promise { resolve, _ ->
            translateUsingCachedToken(query).then(resolve).catch {
                translateUsingNewToken(query).then(resolve)
            }
        }
    }

    override fun updateOptions(options: MetaOptions) {
        useCnDomain = options[OPTION_USE_GOOGLE_CN] == "true"
        console.info("useCnDomain", useCnDomain)
    }

    private val tokenStorageKey = "plugin-googletranslation-token"
    private fun translateUsingCachedToken(query: Query) : Promise<String> {
        return Promise { resolve, reject ->
            localStorage[tokenStorageKey]?.let { token ->
                console.info("Translate using cached google token $token")
                translateWithToken(query, token).then {
                    resolve(googleTranslationRenderer.render(it, token))
                }.catch(reject)
            } ?: run {
                reject(IllegalStateException("No cached google token available"))
            }
        }
    }

    private fun translateUsingNewToken(query: Query): Promise<String> {
        return getToken().then { token ->
            console.info("Translate using new google token $token")
            localStorage[tokenStorageKey] = token
            translateWithToken(query, token).then {
                googleTranslationRenderer.render(it, token)
            }.catch {
                renderFailure(id(), sourceUrl(query), query, it)
            }
        }.catch {
            renderFailure(id(), sourceUrl(query), query, it)
        }
    }

    private fun getToken() = Promise<String> { resolve, reject ->
        getGTranslateToken().then(resolve).catch {
            reject(TranslationException("Failed to get token", it))
        }
    }

    private fun translateWithToken(query: Query, token: String) = Promise<GoogleTranslation> { resolve, reject ->
        val tk = hash(query.getText(), token)
        val requestUrl = queryUrl(tk, query)
        httpClient.get(requestUrl).then { rawContent ->
            googleTranslationParser.parse(sourceUrl(query), rawContent, query.getFrom(), query.getTo())
                    ?.let(resolve)
                    ?: run {
                        val parsingFailure = TranslationParsingFailureException()
                                .apply(buildTranslationException(query))
                                .apply {
                                    this.requestUrl = requestUrl
                                    this.rawContent = rawContent
                                }
                        reject(parsingFailure)
                    }
        }.catch {
            val httpException = TranslationHttpFailureException()
                    .apply(buildTranslationException(query))
            reject(httpException)
        }
    }

    private fun buildTranslationException(query: Query) = { exception: TranslationException ->
        exception.providerId = id()
        exception.query = query
        exception.manualCheckUrl = sourceUrl(query)
    }

    private fun sourceUrl(query: Query): String = "${baseUrl()}/#" +
            "${query.getFrom()}/${query.getTo()}/${encodeURIComponent(query.getText())}"

    private fun queryUrl(token: String, q: Query) = "${baseUrl()}/translate_a/single?client=webapp" +
            "&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&pc=1&otf=1&ssel=0&tsel=0&kc=1" +
            "&sl=${q.getFrom()}&tl=${q.getTo()}&hl=${q.getTo()}&tk=$token&q=${encodeURIComponent(q.getText())}"
}

external fun encodeURIComponent(str: String): String