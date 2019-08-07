package net.yqli.zkagent

import org.apache.zookeeper.server.ContainerManager
import org.apache.zookeeper.server.ServerCnxnFactory
import org.apache.zookeeper.server.ServerConfig
import org.apache.zookeeper.server.persistence.FileTxnSnapLog
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Wrapper of ZooKeeperServerMain
 */
class ZkServerMain {

    companion object {
        val LOG : Logger = LoggerFactory.getLogger(ZkServerMain::class.java)
    }

    private var cnxnFactory: ServerCnxnFactory? = null

    private var containerManager: ContainerManager? = null

    private var zkServer: ZkServer? = null

    /**
     * Run from the given configuration.
     *
     * @param config the server configuration.
     */
    fun runFromConfig(config: ServerConfig, startLatch: CountDownLatch) {
        LOG.info("Starting server")

        var txnLog: FileTxnSnapLog? = null

        try {
            txnLog = FileTxnSnapLog(config.dataLogDir, config.dataDir)

            val shutdownLatch = CountDownLatch(1)

            val zkServer = ZkServer(txnLog, config, shutdownLatch)
            this.zkServer = zkServer

            txnLog.setServerStats(zkServer.serverStats())


            if (config.getClientPortAddress() != null) {
                cnxnFactory = ServerCnxnFactory.createFactory()
                cnxnFactory!!.configure(config.clientPortAddress, config.maxClientCnxns, false)
                cnxnFactory!!.startup(zkServer)
            }

            // don't start the secure cnxn

            containerManager = ContainerManager(zkServer.getZKDatabase(), zkServer.getFirstProcessor(),
                    Integer.getInteger("znode.container.checkIntervalMs", TimeUnit.MINUTES.toMillis(1).toInt()),
                    Integer.getInteger("znode.container.maxPerMinute", 10000)
            )
            containerManager!!.start()

            // start procedure is done.
            startLatch.countDown()

            shutdownLatch.await()

            doShutdown()

            cnxnFactory?.join()
            if (zkServer.canShutdown()) {
                zkServer.shutdown(true)
            }

        } catch (e: Exception) {
            LOG.warn("Server interrupted", e)
        } finally {
            txnLog?.close()
        }
    }

    private fun doShutdown() {
        containerManager?.stop()
        cnxnFactory?.shutdown()
    }

    /**
     * Try to shutdown the server gracefully.
     */
    fun gracefullyShutDown() {
        zkServer?.sendShutdownState()
    }
}