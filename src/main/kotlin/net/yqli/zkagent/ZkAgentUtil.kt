package net.yqli.zkagent

import java.net.InetAddress
import java.net.ServerSocket

/**
 * Utils for ZkAgent
 */
object ZkAgentUtil {
    /**
     * Find the available socket port
     *
     * @return the port
     */
    fun findAvailablePort(): Int {
        var socket: ServerSocket? = null

        try {
            socket = ServerSocket(0)
            socket.reuseAddress = true

            return socket.localPort
        } finally {
            socket?.close()
        }
    }

    /**
     * Get the host name.
     *
     * @return the host name
     */
    fun getHostName(): String {
        return InetAddress.getLocalHost().canonicalHostName
    }
}