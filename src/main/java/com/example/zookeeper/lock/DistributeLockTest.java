package com.example.zookeeper.lock;

import com.example.zookeeper.base.BaseServer;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;

/**
 * @author Mr.MC
 */
public class DistributeLockTest extends BaseServer {

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        final DistributeLock lock1 = new DistributeLock();

        final DistributeLock lock2 = new DistributeLock();

        new Thread(() -> {
            try {
                lock1.zkLock();
                System.out.println("============线程1启动，获取到锁");

                Thread.sleep(5000);

                lock1.zkUnlock();
                System.out.println("============线程1关闭，释放锁");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (KeeperException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                lock2.zkLock();
                System.out.println("============线程2启动，获取到锁");

                Thread.sleep(5000);

                lock2.zkUnlock();
                System.out.println("============线程2关闭，释放锁");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (KeeperException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
