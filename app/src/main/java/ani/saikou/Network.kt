package ani.saikou

import com.lagradost.nicehttp.Requests
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.IOException

val defaultHeaders = mapOf(
    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36"
)
val okHttpClient = OkHttpClient()
val client = Requests(okHttpClient, defaultHeaders)

fun <K, V, R> Map<out K, V>.asyncMap(f: suspend (Map.Entry<K, V>) -> R): List<R> = runBlocking {
    map { withContext(Dispatchers.IO) { async { f(it) } } }.map { it.await() }
}

fun <A, B> Collection<A>.asyncMap(f: suspend (A) -> B): List<B> = runBlocking {
    map { async { f(it) } }.map { it.await() }
}

fun logError(e:Exception) {
    toastString(e.localizedMessage)
}

fun <T> tryWith(call : () -> T) : T?{
    return try {
        call.invoke()
    }
    catch (e:Exception){
        logError(e)
        null
    }
}

suspend fun <T> tryWithSuspend(call: suspend () -> T) : T?{
    return try{
        call.invoke()
    }
    catch (e: Exception) {
        logError(e)
        null
    }
}

suspend fun <T> tryForNetwork(call: suspend () -> T) : T?{
    return try{
        call.invoke()
    }
    catch (e: IOException) {
        logError(e)
        null
    }
}