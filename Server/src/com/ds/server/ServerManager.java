package com.ds.server;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerManager {
	public static Map<Path, ReentrantReadWriteLock> transferMap;
	public static Map<Integer, Path> commandIDMap;
	public static Queue<Integer> writeQueue;
	public static LinkedList<Integer> readPool;
 	public static Set<Integer> terminateSet;
	//------------ STRING Constants-----------
	public static final String GET = "get";
	public static final String PUT = "put";
	public static final String LS = "ls";
	public static final String MKDIR = "mkdir";
	public static final String CD = "cd";
	public static final String PWD = "pwd";
	public static final String DELETE = "delete";
	public static final String QUIT = "quit";
	public static final String TERMINATE = "terminate";
	
	public ServerManager() {
		transferMap = new HashMap<Path, ReentrantReadWriteLock>();
		commandIDMap = new HashMap<Integer, Path>();
		writeQueue = new LinkedList<Integer>();
		terminateSet = new HashSet<Integer>();
		readPool = new LinkedList<>();
	}
	
	public static synchronized boolean addNewTransferCommand(String command, int id, String path) {
		if(command.equals(GET)){

			if(transferMap.containsKey(Paths.get(path))) 
				return false;
			else {
				commandIDMap.put(new Integer(id), Paths.get(path));
				transferMap.put(Paths.get(path), new ReentrantReadWriteLock());
				transferMap.get(Paths.get(path)).readLock().lock();
				//transferMap.get(Paths.get(path)).writeLock().lock();
			}
			
		}else if(command.equals(PUT)){
			if(transferMap.containsKey(Paths.get(path))){
				writeQueue.add(id);
				return false;
			}
			else {
				transferMap.put(Paths.get(path), new ReentrantReadWriteLock());
				transferMap.get(Paths.get(path)).writeLock().lock();
				commandIDMap.put(new Integer(id), Paths.get(path));
			}
		}
		else{return false;}
		
		return true;
	}
	
	public static synchronized void remove(String command, String path) {
		if(command.equals(GET)){

			if(transferMap.get(Paths.get(path)).getReadLockCount() > 0)
				transferMap.get(Paths.get(path)).readLock().unlock();
		

		}if(command.equals(PUT)){
			if(transferMap.get(Paths.get(path)).getWriteHoldCount() > 0)
				transferMap.get(Paths.get(path)).writeLock().unlock();
		}
		
		if(transferMap.get(Paths.get(path)).getReadLockCount() == 0 && transferMap.get(Paths.get(path)).getWriteHoldCount() == 0){
			transferMap.remove(Paths.get(path));
			commandIDMap.values().remove(Paths.get(path));	
			
		}
		
	}

	public static synchronized void terminate(String command, int cmdID, String filePath) {
		// TODO Auto-generated method stub
		if(command.equals(GET)){
			transferMap.get(Paths.get(filePath)).readLock().unlock();
			//if the file is not holding any read or write locks then remove it ffrom list of transfer files
			if(!transferMap.get(Paths.get(filePath)).isWriteLocked() && 
					transferMap.get(Paths.get(filePath)).getReadLockCount() == 0){
				transferMap.remove(Paths.get(filePath));
			}
			commandIDMap.remove(Paths.get(filePath));
			terminateSet.remove(Paths.get(filePath));
		}else if(command.equals(PUT)){

			transferMap.get(Paths.get(filePath)).writeLock().unlock();
			//if the file is not holding any read or write locks then remove it from list of transfer files
			if(!transferMap.get(Paths.get(filePath)).isWriteLocked() && 
					transferMap.get(Paths.get(filePath)).getReadLockCount() == 0){
				transferMap.remove(Paths.get(filePath));
			}
			commandIDMap.remove(Paths.get(filePath));
			terminateSet.remove(Paths.get(filePath));
		}
		else{}
	}

	public static synchronized boolean pollForFileWrite(int id, String absolutePath) {
		// TODO Auto-generated method stub
		if(transferMap.containsKey(Paths.get(absolutePath))){																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																								
			if(writeQueue.contains(id) && 
					transferMap.get(Paths.get(absolutePath)).isWriteLocked()){
				return transferMap.get(Paths.get(absolutePath)).writeLock().tryLock();
			}else{
				//transferMap.put(Paths.get(absolutePath), new ReentrantReadWriteLock());
				transferMap.get(Paths.get(absolutePath)).writeLock().lock();
				return true;
			}
		}
		return false;
	}

	public static synchronized boolean pollForFileRead(int id, String fileName) {
		// TODO Auto-generated method stub
		try{
			if(transferMap.containsKey(Paths.get(fileName))) {
				if(readPool.contains(id))
					return !transferMap.get(Paths.get(fileName)).readLock().tryLock();
			}else {
				transferMap.put(Paths.get(fileName), new ReentrantReadWriteLock());
				transferMap.get(Paths.get(fileName)).readLock().lock();
				commandIDMap.put(id, Paths.get(fileName));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	public synchronized static boolean addTerminateCommand(int commandId) {
		// TODO Auto-generated method stub
		if(commandIDMap.containsKey(commandId)){

			if(terminateSet.contains(commandId))
				return false;
			else{
				terminateSet.add(commandId);
			}
			
		}else{
			System.out.println("Command id for termination is invalid :: " + commandId);
		}
		return true;
	}

	public static synchronized void addToPool(String commandType,int commandId) {
		// TODO Auto-generated method stub
		if(commandType.equals(GET)){
			readPool.add(commandId);
		}else if(commandType.equals(PUT)){
			
		}
	}

	public static void deletePutFile(File file) {
		// TODO Auto-generated method stub
		try{
			
			if(file.exists() && checkForFilesInExecution(file)){
				file.delete();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static synchronized boolean checkForCommand(int id) {
		// TODO Auto-generated method stub
		return commandIDMap.containsKey(id);
	}

	public static synchronized boolean checkForFilesInExecution(File file) {
		// TODO Auto-generated method stub
		try{
			if(commandIDMap.containsValue(Paths.get(file.getAbsolutePath()))){
				return true;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	
	
}