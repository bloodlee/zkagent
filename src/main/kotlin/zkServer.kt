import net.yqli.zkagent.ZkAgent

fun main() {
    val agent = ZkAgent("/home/yli/zkDataDir2", "/home/yli/zkDataDir2")

    agent.start()

    Thread.sleep(500000)
}
