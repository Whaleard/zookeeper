package com.example.zookeeper.client;

import com.example.zookeeper.base.BaseServer;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author Mr.MC
 */
public class DistributeClient extends BaseServer {

    private String connectString = "10.111.208.237:2181,10.111.208.237:2182,10.111.208.237:2183";

    private int sessionTimeout = 2000;

    private ZooKeeper zkClient;

    @Before
    public void init() throws IOException {
        // 建立连接
        zkClient = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                List<String> childrenList;
                try {
                    childrenList = zkClient.getChildren("/", true);
                } catch (KeeperException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                logger.info("============监听回调方法遍历zookeeper子节点 START");
                for (String children : childrenList) {
                    System.out.println(children);
                }
                logger.info("============监听回调方法遍历zookeeper子节点 END");
            }
        });
    }

    @Test
    public void create() throws InterruptedException, KeeperException {
        // 判断节点是否存在
        Stat stat = zkClient.exists("/weiguo", false);
        if (stat == null) {
            // 创建节点
            String node = zkClient.create("/weiguo", "caocao".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } else {
            System.out.println("Node already exists：/mc");
        }
    }

    @Test
    public void getChildren() throws InterruptedException, KeeperException {
        // 获取子节点
        List<String> childrenList = zkClient.getChildren("/", true);

        logger.info("============获取子节点方法遍历zookeeper子节点 START");
        for (String children : childrenList) {
            System.out.println(children);
        }
        logger.info("============获取子节点方法遍历zookeeper子节点 END");

        Thread.sleep(Long.MAX_VALUE);
    }
}
