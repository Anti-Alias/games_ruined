package ruined

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory


fun main() {
    App.start()
}

object App {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Deploys RuinedVerticle to new vertx instance.
     */
    fun start() {
        val json = JsonObject().put("workerPoolSize", 16)
        val vertxOptions = VertxOptions(json)
        val vertx = Vertx.vertx(vertxOptions)
        vertx.deployVerticle(RuinedVerticle::class.java, DeploymentOptions())
    }
}