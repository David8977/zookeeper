package com.sznetweb.zk;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class CreateNode implements Watcher {
	private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
	 Lock lock = new ReentrantLock();    //注意这个地方
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		ZooKeeper zk = new ZooKeeper("192.168.9.218:2181", 2000,new CreateNode());
		System.out.println(zk.getState());
		
		try {
			connectedSemaphore.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//String path1;
		try {
			/*path1 = zk.create("/master", "".getBytes(), Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL);

			System.out.println("Success create znode: " + path1);*/

			String path2 = zk.create("/master-", "".getBytes(),
					Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			System.out.println("Success create znode: " + path2);

			String path3 = zk.create("/master-", "".getBytes(),
					Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			System.out.println("Success create znode: " + path3);
		} catch (KeeperException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("---请输入OK--");
		System.in.read();

	}

	@Override
	public void process(WatchedEvent event) {
		System.out.println(event.getState());
		if (KeeperState.SyncConnected == event.getState()) {
			connectedSemaphore.countDown();
		}

	}

}
