package com.sznetweb.zk;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

public class ZkTestMain extends Thread{

	private static final int NUM=100;
	private static CountDownLatch countDownLatch=new CountDownLatch(NUM);
	
	public static void main(String[] args) {
		
		for (int i = 0; i < NUM; i++) {
			new ZkTestMain().start();
			countDownLatch.countDown();
		}
	}

	@Override
	public void run() {

		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		CreateOrderNum();
	}
	private static int j = 0;
	//jvm 自带的锁
    //private static Lock lock=new ReentrantLock();
	//分布式锁
    private static Lock lock=new ZookeeperLock();
	private  static void CreateOrderNum() {
		//System.out.println("CreateOrderNum");
		lock.lock();
		try {
			SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMddHHmmss");
			++j;
			System.out.println(Thread.currentThread().getId()+"订单ID>>>>>:"+simpleDateFormat.format(new Date())+"-"+j);
		}finally{
			lock.unlock();
		}

	}
}
