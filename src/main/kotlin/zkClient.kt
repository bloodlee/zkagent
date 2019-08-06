import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.RetryNTimes
import org.apache.zookeeper.CreateMode

fun main(args: Array<String>) {
    val client = CuratorFrameworkFactory.newClient("127.0.0.1:41127", RetryNTimes(3, 100))
    client.start()

    client.create()
        .creatingParentsIfNeeded()
        .withMode(CreateMode.EPHEMERAL)
        .forPath("/a/b/c", "init".toByteArray())

    println(String(client.data.forPath("/a/b/c")))

    client.close()
}