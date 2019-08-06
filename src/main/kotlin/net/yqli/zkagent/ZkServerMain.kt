package net.yqli.zkagent

import org.apache.zookeeper.server.ZooKeeperServerMain

/**
 * Wrapper of ZooKeeperServerMain
 */
class ZkServerMain : ZooKeeperServerMain() {

    /**
     * Shutdown the server
     */
    public override fun shutdown() {
        shutdown()
    }

}