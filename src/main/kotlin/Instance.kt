import org.dicthub.plugin.com_google_translate.GoogleTranslationParser
import org.dicthub.plugin.com_google_translate.GoogleTranslationProvider
import org.dicthub.plugin.com_google_translate.GoogleTranslationRenderer
import org.dicthub.plugin.shared.util.AjaxHttpClient

@JsName("create_plugin_com_google_translate")
fun create_plugin_com_google_translate() : GoogleTranslationProvider {

    return GoogleTranslationProvider(AjaxHttpClient, GoogleTranslationParser(), GoogleTranslationRenderer())
}