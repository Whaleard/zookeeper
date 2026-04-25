package com.example.zookeeper.lock;

import com.example.zookeeper.base.BaseServer;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 原生zookeeper实现分布式锁
 * @author Mr.MC
 */
public class DistributeLock extends BaseServer {

    private final String connectionString = "10.111.208.237:2181,10.111.208.237:2182,10.111.208.237:2183";
    private final int sessionTimeout = 2000;
    private final ZooKeeper zk;

    private CountDownLatch connectLatch = new CountDownLatch(1);
    private CountDownLatch waitLatch = new CountDownLatch(1);

    private String waitPath;
    private String currentMode;

    public DistributeLock() throws IOException, InterruptedException, KeeperException {
        // 获取连接
        zk = new ZooKeeper(connectionString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                // 释放connectLatch
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    connectLatch.countDown();
                }

                // 释放waitLatch
                if (watchedEvent.getType() == Event.EventType.NodeDeleted && watchedEvent.getPath().equals(waitPath)) {
                    waitLatch.countDown();
                }
            }
        });

        // 等待zookeeper正常连接后，继续执行程序
        connectLatch.await();

        // 判断根节点/locks是否存在
        Stat stat = zk.exists("/locks", false);
        if (stat == null) {
            // 创建根节点
            zk.create("/locks", "locks".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }

    /**
     * 对zookeeper加锁
     */
    public void zkLock() throws InterruptedException, KeeperException {
        // 创建临时带序号节点
        currentMode = zk.create("/locks/" + "seq-", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        // 判断创建的节点是否是最小序号的节点。如果是，获取到锁；如果不是，监听其前一个序号节点
        List<String> childrenList = zk.getChildren("/locks", false);

        // 如果只有一个节点，那就直接获取锁；如果有多个节点，需要判断序号最小节点
        if (childrenList.size() == 1) {
            return;
        } else {
            Collections.sort(childrenList);

            // 获取节点名称：seq-xxx
            String thisNode = currentMode.substring("/locks/".length());
            // 通过节点名称获取该节点在节点集合中的位置
            int index = childrenList.indexOf(thisNode);

            // 判断
            if (index == -1) {
                throw new RuntimeException("数据异常");
            } else if (index == 0) {
                // 第一个节点，可以获取锁
                return;
            } else {
                // 需要监听它前一个序号节点
                waitPath = "/locks/" + childrenList.get(index - 1);
                zk.getData(waitPath, true, null);

                // 等待监听
                waitLatch.await();

                return;
            }
        }
    }

    /**
     * 对zookeeper解锁
     */
    public void zkUnlock() {
        // 删除节点
        try {
            zk.delete(currentMode, -1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        }
    }
}
