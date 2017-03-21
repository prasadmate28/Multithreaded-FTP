package com.ds.client;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLocktest implements Runnable {

	static ReentrantReadWriteLock rwl ;
	public ReadWriteLocktest(ReentrantReadWriteLock rwl){
		this.rwl = rwl;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try{
			 rwl = new ReentrantReadWriteLock();
			 rwl.writeLock().lock();
			 System.out.println("Read Lock lagaya");
			 //rwl.writeLock().lock();
			 //System.out.println("abb write lock bhi lagaya");
			// new Thread(new ReadWriteLocktest(rwl)).start();
			 if(rwl.writeLock().tryLock()){
				 System.out.println("isko write lock mil gaaya");
			 }
			 System.out.println("Read lock count :: "+rwl.getReadLockCount());
			System.out.println("get queue lenght"+rwl.getQueueLength());
			System.out.println("write hold count"+rwl.getWriteHoldCount());
			System.out.println("write hold count"+rwl.isWriteLocked());
			System.out.println("write hold count"+rwl.isWriteLockedByCurrentThread());
			System.out.println();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("write hold count"+rwl.getWriteHoldCount());
	}
	
	

}
