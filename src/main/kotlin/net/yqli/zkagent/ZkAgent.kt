package net.yqli.zkagent

import org.apache.zookeeper.server.ServerConfig
import org.apache.zookeeper.server.quorum.QuorumPeerConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import java.net.ServerSocket
import java.util.*

/**
 * ZK Agent
 */
class ZkAgent(private val zkDataDir: String) {

    companion object {
        val LOGGER : Logger = LoggerFactory.getLogger(ZkAgent.javaClass)
    }

    private var zkServer : ZkServerMain? = null

    /**
     * Start the agent.
     */
    fun start() {
        startZk(findAvailablePort().toString())
    }

    /**
     * Stop the agent
     */
    fun stop() {
        zkServer?.shutdown()
    }

    /**
     * Start the ZK server.
     *
     * @port the ZK port.
     */
    private fun startZk(port: String) {
        val prop = Properties()
        prop.setProperty("tickTime", "2000")
        prop.setProperty("dataDir", zkDataDir)

        prop.setProperty("clientPort", port)

        val quorumConfig = QuorumPeerConfig()

        try {
            quorumConfig.parseProperties(prop)

            zkServer = ZkServerMain()

            val serverConfig = ServerConfig()
            serverConfig.readFrom(quorumConfig)

            zkServer?.runFromConfig(serverConfig)
        } catch (e: Exception) {
            LOGGER.error("Failed to start standalone zk server", e)
        }

    }

    /**
     * Find the available socket port
     *
     * @return the port
     */
    private fun findAvailablePort() : Int {
        var socket : ServerSocket? = null

        try {
            socket = ServerSocket(0)
            socket.reuseAddress = true

            return socket.localPort
        } finally {
            socket?.close()
        }
        throw IllegalStateException("Could not find a free TCP/IP port")
    }

}