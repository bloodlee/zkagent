package net.yqli.zkagent

import org.apache.zookeeper.server.RequestProcessor
import org.apache.zookeeper.server.ServerConfig
import org.apache.zookeeper.server.ZooKeeperServer
import org.apache.zookeeper.server.persistence.FileTxnSnapLog
import java.util.concurrent.CountDownLatch

/**
 * Customized Zookeeper Server
 */
class ZkServer(txnLog: FileTxnSnapLog,
               config: ServerConfig,
               private val countDownLatch: CountDownLatch)
    : ZooKeeperServer(txnLog, config.tickTime, config.minSessionTimeout, config.maxSessionTimeout, null) {

    /**
     * Get the first processor
     */
    fun getFirstProcessor() : RequestProcessor {
        return firstProcessor
    }

    /**
     * Set the state
     *
     * @param state the state
     */
     override fun setState(state: State) {
        this.state = state

        if (state == State.ERROR || state == State.SHUTDOWN) {
            countDownLatch.countDown()
        }
    }

    /**
     * check if the server can be shutdown.
     *
     * @return true or false.
     */
    public override fun canShutdown(): Boolean {
        return super.canShutdown()
    }

    /**
     * Send shutdown state to running server if it can be shutdown.
     */
    fun sendShutdownState() {
        if (canShutdown()) {
            setState(State.SHUTDOWN)
        }
    }
}