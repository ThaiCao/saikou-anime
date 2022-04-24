package ani.saikou

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.lagradost.nicehttp.Requests
import okhttp3.OkHttpClient

val okHttpClient = OkHttpClient()
val httpClient = Requests(okHttpClient)
val mapper = JsonMapper.builder().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false).build()
