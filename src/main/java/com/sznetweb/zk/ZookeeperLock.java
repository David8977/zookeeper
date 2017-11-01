package com.sznetweb.zk;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;


public class ZookeeperLock implements Lock {
	

	ZkClient zkClient=new ZkClient("192.168.9.218:2181", 5000);
	String PATH="/lock";
	
	private  CountDownLatch countDownLatch=null;
	
	List<CountDownLatch> listCount=new ArrayList<CountDownLatch>();
	
	private static boolean isFrist=true;

	@Override
	public void lock() {
		
		if(isFrist){
			init();
		}
		//System.out.println("1");
		if(tryLock()){
			//System.out.println("2");
			return;
		}
		waitForLock();
		//System.out.println("4");
		lock();
	}

	private void init() {
		
		if(zkClient.exists(PATH)){
			zkClient.delete(PATH);
			isFrist=false;
		}
	}
	
	 private void waitForLock() {

		 IZkDataListener listener = new IZkDataListener(){
			@Override
			public void handleDataChange(String dataPath, Object data)
					throws Exception {
			}
			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
				System.out.println("3");
				CountDownLatch tempCountDownLatch=listCount.get(0);
				if(tempCountDownLatch!=null){
					
					tempCountDownLatch.countDown();
					System.out.println(Thread.currentThread().getId()+">>3-1>>"+tempCountDownLatch.getCount());
					
					listCount.remove(0);
					
				}
			}
		};
		
		zkClient.subscribeDataChanges(PATH,listener);
		
		if(zkClient.exists(PATH)){
			try {
				countDownLatch=new CountDownLatch(1);
				System.out.println(Thread.currentThread().getId()+">>3-2>>"+countDownLatch.getCount());
				listCount.add(countDownLatch);
				countDownLatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		zkClient.unsubscribeDataChanges(PATH,listener);
	}

	@Override
	public void unlock() {
		zkClient.delete(PATH);
		//System.out.println("5");
	}
	
	@Override
	public boolean tryLock() {
		try {
			System.out.println(Thread.currentThread().getId()+"createPersistent");
			zkClient.createPersistent(PATH, "");
			return true;
		} catch (ZkNodeExistsException e) {
			//System.err.println("打印false");
			return false;
		}

	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit)
			throws InterruptedException {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public Condition newCondition() {
		// TODO Auto-generated method stub
		return null;
	}

}
