import net.yqli.zkagent.ZkAgent

fun main() {
    val agent = ZkAgent("\\home\\jali\\zkDataDir2", "/home/jali/zkDataDir2")

    agent.start()

    Thread.sleep(500000)
}
