package com.inspetor

import android.content.Context
import com.snowplowanalytics.snowplow.tracker.DevicePlatforms
import com.snowplowanalytics.snowplow.tracker.Emitter
import com.snowplowanalytics.snowplow.tracker.Subject
import com.snowplowanalytics.snowplow.tracker.Tracker
import com.snowplowanalytics.snowplow.tracker.emitter.BufferOption
import com.snowplowanalytics.snowplow.tracker.emitter.HttpMethod
import com.snowplowanalytics.snowplow.tracker.emitter.RequestSecurity

object SnowplowManager {
    private lateinit var trackerName: String
    private lateinit var appId: String
    private lateinit var collectorUri: String
    private lateinit var httpMethod: HttpMethod
    private lateinit var protocolType: RequestSecurity
    private lateinit var bufferOption: BufferOption
    private var base64Encoded: Boolean = false

    fun init(config: InspetorConfig) {
        this.trackerName = config.trackerName
        this.appId = config.appId
        this.collectorUri = InspetorDependencies.DEFAULT_COLLECTOR_URI
        this.base64Encoded = InspetorDependencies.DEFAULT_BASE64_OPTION
        this.bufferOption = switchBufferOptionSize(InspetorDependencies.DEFAULT_BUFFERSIZE_OPTION)
        this.httpMethod = switchHttpMethod(InspetorDependencies.DEFAULT_HTTP_METHOD_TYPE)
        this.protocolType = switchSecurityProtocol(InspetorDependencies.DEFAULT_PROTOCOL_TYPE)

        if (config.devEnv == true) {
            this.collectorUri = InspetorDependencies.DEFAULT_COLLECTOR_DEV_URI
        }

        if (config.inspetorEnv == true) {
            this.collectorUri = InspetorDependencies.DEFAULT_COLLECTOR_INSPETOR_URI
        }

        require(verifySetup())
    }

    private fun verifySetup(): Boolean {
        return (this.appId != "" && this.trackerName != "")
    }

    fun setupTracker(androidContext: Context): Tracker? {
        val emitter = Emitter.EmitterBuilder(collectorUri, androidContext.applicationContext)
            .method(this.httpMethod)
            .option(this.bufferOption)
            .security(this.protocolType)
            .build() ?: throw fail("Inspetor Exception 9000: Internal error.")

        return Tracker.init(
            Tracker.TrackerBuilder(emitter, this.trackerName, this.appId, androidContext.applicationContext)
                .base64(this.base64Encoded)
                .platform(DevicePlatforms.Mobile)
                .subject(Subject.SubjectBuilder().context(androidContext).build())
                .sessionContext(true)
                .sessionCheckInterval(10) // Checks every 10 seconds (default is 15)
                .foregroundTimeout(300)   // Timeout after 5 minutes (default is 10)
                .backgroundTimeout(120)
                .geoLocationContext(false) // Since we are not being able to get the location anyway
                .applicationContext(true)
                .mobileContext(true)
                .build()
        ) ?: throw fail("Inspetor Exception 9000: Internal error.")
    }

    private fun fail(message: String): Throwable {
        throw Exception(message)
    }

    private fun switchBufferOptionSize (bufferOptionSize: BufferOptionSize): BufferOption {
        return when(bufferOptionSize) {
            BufferOptionSize.SINGLE -> BufferOption.Single
            BufferOptionSize.DEFAULT -> BufferOption.DefaultGroup
            BufferOptionSize.HEAVY -> BufferOption.HeavyGroup
        }
    }

    private fun switchSecurityProtocol (requestSecurityProtocol: RequestSecurityProtocol): RequestSecurity {
        return when (requestSecurityProtocol) {
            RequestSecurityProtocol.HTTP -> RequestSecurity.HTTP
            RequestSecurityProtocol.HTTPS -> RequestSecurity.HTTPS
        }
    }

    private fun switchHttpMethod (httpMethodType: HttpMethodType): HttpMethod {
        return when (httpMethodType) {
            HttpMethodType.GET -> HttpMethod.GET
            HttpMethodType.POST -> HttpMethod.POST
        }
    }

}