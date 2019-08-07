package net.yqli.zkagent

import org.apache.zookeeper.server.ServerConfig
import org.apache.zookeeper.server.quorum.QuorumPeerConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

/**
 * ZK Agent
 *
 * @param zkDataDir the "dataDir" of zookeeper
 * @param workingDirPath the working directory of zkAgent and it will be used by ZkAgent to lock the properties file.
 */
class ZkAgent(private val zkDataDir: String, private val workingDirPath: String) {

    companion object {
        val LOGGER : Logger = LoggerFactory.getLogger(ZkAgent::class.java)

        const val ZK_SERVER_PROP_FILE_NAME = "zk_server_info.properties"
        const val ZK_SERVER_HOST_NAME = "zk_server_host"
        const val ZK_SERVER_PORT = "zk_server_port"
        const val LOCK_FILE_NAME = ".lock"
   }

    private var zkServer : ZkServerMain? = null

    /**
     * Start the agent.
     *
     * @return the zkServer properties.
     */
    @Synchronized
    fun start(): ZkServerProp {
        val workingDir = File(workingDirPath)
        val lockFile = File(workingDir, LOCK_FILE_NAME)

        if (!lockFile.exists()) {
            lockFile.createNewFile()
        }

        val propertyFile = File(workingDir, ZK_SERVER_PROP_FILE_NAME)
        val lockFileChannel = RandomAccessFile(lockFile, "rw").channel

        lockFileChannel.use {
            val lockFileLock = lockFileChannel.lock()

            lockFileLock.use {
                // when the property file exists, it means zk server was already launched.
                if (propertyFile.exists() && propertyFile.isFile) {
                    val zkServerProp = Properties()

                    zkServerProp.load(FileInputStream(propertyFile))

                    return ZkServerProp(zkServerProp.getProperty(ZK_SERVER_HOST_NAME, ""),
                                        zkServerProp.getProperty(ZK_SERVER_PORT, ""))
                } else {
                    val hostName = ZkAgentUtil.getHostName()
                    val port = ZkAgentUtil.findAvailablePort()

                    val startLatch = CountDownLatch(1)

                    thread {
                        startZk(port.toString(), startLatch)
                    }

                    startLatch.await()

                    val prop = Properties()
                    prop.setProperty(ZK_SERVER_HOST_NAME, hostName)
                    prop.setProperty(ZK_SERVER_PORT, port.toString())

                    prop.store(FileOutputStream(propertyFile), "")

                    return ZkServerProp(hostName, port.toString())
                }

            }
        }
    }

    /**
     * Stop the agent
     */
    @Synchronized
    fun stop() {
        // do something later.
    }

    /**
     * Start the ZK server.
     *
     * @param port the ZK port.
     * @param startLatch the latch for start.
     */
    private fun startZk(port: String, startLatch: CountDownLatch) {
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

            zkServer!!.runFromConfig(serverConfig, startLatch)
        } catch (e: Exception) {
            LOGGER.error("Failed to start standalone zk server", e)
        }

    }


}