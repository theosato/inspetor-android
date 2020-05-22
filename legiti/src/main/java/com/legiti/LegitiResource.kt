package com.legiti

import android.content.Context
import com.snowplowanalytics.snowplow.tracker.Tracker
import com.snowplowanalytics.snowplow.tracker.events.SelfDescribing
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson
import java.util.*
import com.legiti.helpers.*
import com.legiti.services.LegitiResourceService
import com.snowplowanalytics.snowplow.tracker.events.ScreenView
import kotlin.collections.HashMap

internal class LegitiResource(config: LegitiConfig, androidContext: Context):
    LegitiResourceService {

    private var spTracker: Tracker
    private var androidContext: Context
    private var legitiDeviceData: HashMap<String, Any?>

    var currentUserId: String? = null

    init {
        SnowplowManager.init(config)
        this.androidContext = androidContext
        this.spTracker = SnowplowManager.setupTracker(this.androidContext) ?: throw fail("Legiti Exception 9000: Internal error.")
        this.legitiDeviceData = LegitiDeviceData(androidContext).getDeviceData()
    }

    override fun trackUserAction(data: HashMap<String, String?>, action: UserAction): Boolean {
        this.trackUnstructuredEvent(
            LegitiDependencies.FRONTEND_USER_SCHEMA_VERSION,
            data,
            action.rawValue()
        )

        return true
    }

    override fun trackUserAuthAction(data: HashMap<String, String?>, action: AuthAction): Boolean {
        this.trackUnstructuredEvent(
            LegitiDependencies.FRONTEND_AUTH_SCHEMA_VERSION,
            data,
            action.rawValue()
        )

        return true
    }

    override fun trackPasswordRecoveryAction(data: HashMap<String, String?>, action: PasswordAction): Boolean {
        this.trackUnstructuredEvent(
            LegitiDependencies.FRONTEND_PASS_RECOVERY_SCHEMA_VERSION,
            data,
            action.rawValue()
        )

        return true
    }

    override fun trackPasswordResetAction(data: HashMap<String, String?>, action: PasswordAction): Boolean {
        this.trackUnstructuredEvent(
            LegitiDependencies.FRONTEND_PASS_RESET_SCHEMA_VERSION,
            data,
            action.rawValue()
        )

        return true
    }

    override fun trackOrderAction(data: HashMap<String, String?>, action: OrderAction): Boolean {
        this.trackUnstructuredEvent(
            LegitiDependencies.FRONTEND_ORDER_SCHEMA_VERSION,
            data,
            action.rawValue()
        )

        return true
    }

    override fun trackPageView(page_title: String): Boolean {
        val spContexts: ArrayList<SelfDescribingJson> = arrayListOf()
        this.getFingerprintContext().let{ spContexts.add(it) }

        this.spTracker.track(
            ScreenView.builder()
                .name(page_title)
                .id(UUID.randomUUID().toString())
                .customContext(spContexts)
                .build() ?: throw fail("Legiti Exception 9000: Internal error.")
        )

        // Making sure there are no more events to be sent
        this.spTracker.emitter?.flush()

        return true
    }

    private fun trackUnstructuredEvent(schema: String, data: HashMap<String, String?>, action: String) {
        val legitiData = SelfDescribingJson(schema, data)

        val spContexts: ArrayList<SelfDescribingJson> = arrayListOf(
            this.setupActionContext(action)
        )


        this.getFingerprintContext().let{ spContexts.add(it) }

        this.spTracker.track(
            SelfDescribing.builder()
                .eventData(legitiData)
                .customContext(spContexts)
                .build() ?: throw fail("Legiti Exception 9000: Internal error.")
        )

        // Making sure there are no more events to be sent
        this.spTracker.emitter?.flush()
    }

    private fun setupActionContext(action: String): SelfDescribingJson {
        val contextMap: HashMap<String, String>? = hashMapOf(
            "action" to action
        )

        return SelfDescribingJson(
            LegitiDependencies.FRONTEND_CONTEXT_SCHEMA_VERSION,
            contextMap
        )

    }

    private fun fail(message: String): Throwable {
        throw Exception(message)
    }

    private fun getFingerprintContext(): SelfDescribingJson {
        val deviceData = this.legitiDeviceData
        deviceData["logged_user_id"] = this.currentUserId

        return SelfDescribingJson(
            LegitiDependencies.FRONTEND_FINGERPRINT_SCHEMA_VERSION,
            deviceData
        )
    }


}
