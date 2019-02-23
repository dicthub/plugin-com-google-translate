package org.dicthub.plugin.com_google_translate

import org.dicthub.plugin.shared.util.AjaxHttpClient
import kotlin.js.Promise


val hash = js("function sM(r,t){var n;if(null!==yr)n=yr;else{n=wr(String.fromCharCode(84));var e=wr(String.fromCharCode(75));(n=[n(),n()])[1]=e(),n=(yr=t||\"\")||\"\"}var a=wr(String.fromCharCode(116));e=wr(String.fromCharCode(107));(a=[a(),a()])[1]=e(),e=\"&\"+a.join(\"\")+\"=\",a=n.split(\".\"),n=Number(a[0])||0;for(var o=[],h=0,f=0;f<r.length;f++){var u=r.charCodeAt(f);128>u?o[h++]=u:(2048>u?o[h++]=u>>6|192:(55296==(64512&u)&&f+1<r.length&&56320==(64512&r.charCodeAt(f+1))?(u=65536+((1023&u)<<10)+(1023&r.charCodeAt(++f)),o[h++]=u>>18|240,o[h++]=u>>12&63|128):o[h++]=u>>12|224,o[h++]=u>>6&63|128),o[h++]=63&u|128)}for(r=n,h=0;h<o.length;h++)r+=o[h],r=xr(r,\"+-a^+6\");return r=xr(r,\"+-3^+b+-f\"),0>(r^=Number(a[1])||0)&&(r=2147483648+(2147483647&r)),e+((r%=1e6).toString()+\".\")+(r^n)}var yr=null,wr=function(r){return function(){return r}},xr=function(r,t){for(var n=0;n<t.length-2;n+=3){var e=\"a\"<=(e=t.charAt(n+2))?e.charCodeAt(0)-87:Number(e);e=\"+\"==t.charAt(n+1)?r>>>e:r<<e;r=\"+\"==t.charAt(n)?r+e&4294967295:r^e}return r};sM")

fun getGTranslateToken(): Promise<String> {

    return Promise { resolve, reject ->
        AjaxHttpClient.get(baseUrl()).then { html ->
            val regex = Regex("TKK='(\\d+\\.\\d+)'|tkk:'(\\d+\\.\\d+)'")
            val match = regex.find(html)
            match?.groups?.lastOrNull()?.value?.let {
                resolve(it)
            } ?: run {
                reject(IllegalStateException("Failed when getting google token"))
            }
        }.catch { reject(it) }
    }
}

