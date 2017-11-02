package com.sznetweb.zk;


import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;


public class zkLock implements Lock {
	

	ZkClient zkClient=new ZkClient("192.168.9.218:2181", 5000);
	private static final String PATH="/lock";
	
	private static final String TEMPPATH="/lock/getLock";
	
	private  CountDownLatch countDownLatch=null;
	
	//List<CountDownLatch> listCount=new ArrayList<CountDownLatch>();
	
	private static boolean isFrist=true;
	
	
	 volatile String currentPath=null;
	String beforePath=null;

	@Override
	public void lock() {
		
		if(isFrist){
			init();
		}
		System.out.println("1");
		
		if (!tryLock()) {
			waitForLock();
			lock();
		} else {
			System.out.println(Thread.currentThread().getName() + " 获得分布式锁！");
		}
	}

	synchronized private void init() {
		
		if(!zkClient.exists(PATH)){
			zkClient.createPersistent(PATH);
			isFrist=false;
		}
	}
	//private List<String> createNodeList=new ArrayList<String>();
	@Override
	public boolean tryLock() {
		
		if (currentPath == null || currentPath.length() <= 0) {
			currentPath =zkClient.createEphemeralSequential(TEMPPATH, "lock");	
		}
		

		List<String> zTempNodeList=zkClient.getChildren(PATH);
	    Collections.sort(zTempNodeList);
	    for (String str : zTempNodeList) {
	    	System.out.println(str);
		}
		System.out.println(Thread.currentThread().getId()+"createEphemeralSequential"+currentPath);

		String tempPath=PATH+"/"+zTempNodeList.get(0);
		System.err.println(currentPath+">>>>>"+tempPath+">>>>"+currentPath.equals(tempPath));
		//3 //1
		if(currentPath.equals(tempPath)){
			return true;
		}
		else{
			String key=currentPath.substring(6);
			int index=Collections.binarySearch(zTempNodeList, key);
			beforePath=zTempNodeList.get(index-1);
			beforePath = PATH+"/"+beforePath;
		}
		return false;
	}

	private void waitForLock() {

		 IZkDataListener listener = new IZkDataListener(){
			public void handleDataChange(String dataPath, Object data)
					throws Exception {
			}
			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
				System.out.println("3");
				
				if(countDownLatch!=null){
					countDownLatch.countDown();
					System.out.println(Thread.currentThread().getId()+">>3-1>>"+countDownLatch.getCount());
	
					
				}
			}
		};

	   System.err.println("listener"+beforePath);
		zkClient.subscribeDataChanges(beforePath,listener);
		if(zkClient.exists(beforePath)){
			try {
				countDownLatch=new CountDownLatch(1);
				System.out.println(Thread.currentThread().getId()+">>3-2>>"+countDownLatch.getCount());
				countDownLatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		zkClient.unsubscribeDataChanges(beforePath,listener);
	}

	@Override
	public void unlock() {
		zkClient.delete(currentPath);
		System.out.println("5");
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
