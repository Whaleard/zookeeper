package com.example.zookeeper.server;

import com.example.zookeeper.base.BaseServer;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 模拟客户端监听zookeeper
 * @author Mr.MC
 */
public class DistributeClient extends BaseServer {

    private String connectString = "10.111.208.237:2181,10.111.208.237:2182,10.111.208.237:2183";

    private int sessionTimeout = 2000;

    private ZooKeeper zk;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        DistributeClient client = new DistributeClient();
        // 1、获取zookeeper连接
        client.getConnect();

        // 2、监听/servers下面子节点的增加和删除
        client.getServerList();

        // 3、业务逻辑
        client.business();
    }

    private void business() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
    }

    private void getServerList() throws InterruptedException, KeeperException {
        List<String> childrenList = zk.getChildren("/servers", true);

        ArrayList<String> servers = new ArrayList<>();
        logger.info("============获取子节点方法遍历zookeeper子节点 START");
        for (String children : childrenList) {

            byte[] data = zk.getData("/servers/" + children, false, null);

            servers.add(new String(data));
        }
        System.out.println(servers);
        logger.info("============获取子节点方法遍历zookeeper子节点 END");
    }

    private void getConnect() throws IOException {
        zk = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                try {
                    getServerList();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (KeeperException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
