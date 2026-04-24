package com.example.zookeeper.server;

import com.example.zookeeper.base.BaseServer;
import org.apache.zookeeper.*;

import java.io.IOException;

/**
 * 模拟服务器注册到zookeeper
 * @author Mr.MC
 */
public class DistributeServer extends BaseServer {

    private String connectString = "10.111.208.237:2181,10.111.208.237:2182,10.111.208.237:2183";

    private int sessionTimeout = 2000;

    private ZooKeeper zk;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        DistributeServer server = new DistributeServer();
        // 1、获取zookeeper连接
        server.getConnect();
        // 2、注册服务器到zookeeper集群
        server.register(args[0]);
        // 3、启动业务逻辑
        server.business();

    }

    private void business() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
    }

    private void register(String hostName) throws InterruptedException, KeeperException {
        zk.create("/servers/" + hostName, hostName.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        logger.info("============" + hostName + " is online");
    }

    private void getConnect() throws IOException {
        zk = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {

            }
        });
    }
}
