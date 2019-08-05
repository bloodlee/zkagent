import org.apache.zookeeper.server.ServerConfig
import org.apache.zookeeper.server.ZooKeeperServerMain
import org.apache.zookeeper.server.quorum.QuorumPeerConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import java.net.ServerSocket
import java.util.*

class ZkAgent(val zkDataDir: String) {

    companion object {
        val LOGGER : Logger = LoggerFactory.getLogger(ZkAgent.javaClass)
    }

    private var zkServer : ZooKeeperServerMain? = null

    fun startZk() {
        val prop = Properties()
        prop.setProperty("tickTime", "2000")
        prop.setProperty("dataDir", zkDataDir)

        val quorumConfig = QuorumPeerConfig()

        try {
            quorumConfig.parseProperties(prop)

            zkServer = ZooKeeperServerMain()

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