package com.ds.server;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.regex.Pattern;


public class ServerUtility {
	
	public static final String GET = "get";
	public static final String PUT = "put";
	public static final String LS = "ls";
	public static final String MKDIR = "mkdir";
	public static final String CD = "cd";
	public static final String PWD = "pwd";
	public static final String DELETE = "delete";
	public static final String QUIT = "quit";
	public static final String TERMINATE = "terminate";
	public final String ROOT = "user.dir";
	
	String currentDirectory = null;
	public static int cmdID = 123;
	
	public static ServerManager serverManager = new ServerManager();
	
	public ServerUtility(){
		this.currentDirectory = executePWD();
	}
	public String defaultMsg() {
		// TODO Auto-generated method stub
		return "Invalid FTP command. Please enter correct command";
	}


	public String executeLS() {
		// TODO Auto-generated method stub
		//ArrayList<String> fileNames = new ArrayList<String>();
		StringBuilder directoryList = new StringBuilder();

		try{
		
			File files = new File(currentDirectory);
			File[] fList = files.listFiles();
			for (File f: fList)
				directoryList.append(f.getName()+"\t");
				//fileNames.add(f.getName());
	
		}catch(Exception e){
			System.out.println("----Exception in LS command execution -----"+e.getMessage());
		}
		return directoryList.toString();
	}


	public String executePWD() {
		// TODO Auto-generated method stub
		
		try{
			if(currentDirectory==null)
				currentDirectory = myftpserver.serverRootDirectory;
		}catch(Exception e){
			System.out.println("----Exception in PWD command execution -----"+e.getMessage());
		}
		return currentDirectory;
	}

	public String executeCD(String dirPath) {
		// TODO Auto-generated method stub
		String retStatus = null;
		File curr = null;
		try{
			
			if(dirPath.equals("..")){
				if(currentDirectory!=null)
					curr = new File(currentDirectory);
				else
					curr = new File(executePWD());
				if(curr.getAbsoluteFile().getParent() != null) {
					System.setProperty(ROOT, curr.getAbsoluteFile().getParent());
					currentDirectory = curr.getAbsoluteFile().getParent();
				}
				else
					System.out.println("::: Reached root directory :::" + ROOT);
				retStatus = "Directory changed successfully";
			}else{
				File current = new File(executePWD() + File.separator +dirPath); //made change
				System.out.println("Inside CD ::" + current.getAbsolutePath());
				if(current.exists() && current.isDirectory()){
					//System.setProperty(ROOT, current.getAbsoluteFile().getPath());
					currentDirectory = current.getAbsoluteFile().getPath();
					retStatus = ":::: Directory changed successfully ::::";
				}
				else
					retStatus = "Directory does not exist. Enter correct directory name and try again.";
			}
			
		}catch(Exception e){
			System.out.println("----Exception in CD command execution -----"+e.getMessage());
		}
		
		return retStatus; 
	}


	public void executeMKDIR(String parameters) {
		// TODO Auto-generated method stub
		try{
			File newDir = new File(currentDirectory + File.separator+parameters);
			if(!newDir.exists()){
				newDir.mkdir();
			}else{
				System.out.println("directory already exists");
				return;
			}
			
		}catch(Exception e){
			System.out.println("Exception in mkdir command execution" + e.getMessage());
		}
		
	}


	public boolean executeDELETE(String parameters) {
		// TODO Auto-generated method stub
		
		try{
			File file = new File(currentDirectory+File.separator+parameters);
			if(file.exists()) {
				if(!ServerManager.checkForFilesInExecution(file)){
					file.delete();
					return true;
				}else{
					System.out.println("file cannot be deleted :: " + file.getAbsolutePath());
					return false;
				}
			}
			else{				
				System.out.println(":::: File does not exist ::: File:: " + currentDirectory+File.separator+parameters);
				return false;
			}
		}catch(Exception e){
			System.out.println("Exception in execute delete command"+e.getMessage());
		}
		return false;
	}
	
	public boolean executeGET(DataInputStream sockInp,DataOutputStream sockOutp) {
		// TODO Auto-generated method stub
		try{
			String fileName = sockInp.readUTF();
			
			File getFile = new File(currentDirectory+ File.separator + fileName);
			
			int id = ++cmdID;//***to be generated in servermanager
			System.out.println(":::: For command ID :: "+id+" getting file from Server path :: "+getFile.getAbsolutePath());
			
			
			if(getFile.exists()) {// && !ServerManager.transferMap.containsKey(Paths.get(getFile.getAbsolutePath()))) {
				ServerManager.addToPool(GET ,id);
				//polling
				
				while(ServerManager.pollForFileRead(id, getFile.getAbsolutePath())){
					Thread.sleep(100);
				}
				
				// file exists and is not in the trasferfilemap
				sockOutp.writeUTF("READY");// Status 1 : ready signal to client
				sockOutp.writeUTF(Integer.toString(id));//send cmdid to client
				//ServerManager.addNewTransferCommand(GET,cmdID, getFile.getAbsolutePath());
				
				Thread.sleep(1000);
				
				if(ServerManager.terminateSet.contains(cmdID)){
					//if cmd is in terminate set
					ServerManager.terminate(GET,cmdID, getFile.getAbsolutePath());
					return false;
				}else{
					if(!transferGetBytes(getFile,sockOutp))
						return false;
					ServerManager.remove(GET,getFile.getAbsolutePath());
				}
				//-----end----
				System.out.println("Server: File "+fileName+" sent successfully to client " + Thread.currentThread().getId());
				
			}else { //if(!getFile.exists()) {
				sockOutp.writeUTF("File Not Found");//send status2
				System.out.println(":::: GET file does not exist ::::");
			/*}
			else {//file exists and is in the transfermap and has a writelock
				sockOutp.writeUTF("File already in use");//send status2
				System.out.println(":::: GET file already in use :: Pooling the thread::");*/

				//add in readPool
				
			}
			
		}catch(Exception e){
			System.out.println("::::Exception in Get file function:: "+e.getMessage());
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean transferGetBytes(File getFile, DataOutputStream sockOutp) {
		byte[] buffer = new byte[1000];
		try {
			File file = new File(getFile.getAbsolutePath());
			
			long fileSize = file.length();
			byte[] fileSizeBytes = ByteBuffer.allocate(8).putLong(fileSize).array();
			sockOutp.write(fileSizeBytes, 0, 8);
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			int count = 0;
			while((count = in.read(buffer)) > 0){
				
				if(ServerManager.terminateSet.contains(cmdID)){
					ServerManager.terminate(GET,cmdID, getFile.getAbsolutePath());
					in.close();
					return false;
				}
				sockOutp.write(buffer, 0, count);

			}
			in.close();
		} catch(Exception e){
			e.printStackTrace();
		}
		return true;
	}

	public void executePUT(DataInputStream sockInp,DataOutputStream sockOutp) {
		// TODO Auto-generated method stub
		int id = ++cmdID;
		try{
			String fileName = sockInp.readUTF();
			File putFile = new File(currentDirectory + File.separator+ fileName);
			
			System.out.println("For command ID :: "+id+" :::: file is being copied to server directory path :: "+putFile.getAbsolutePath());
			//-----send status to client
			
			if(!putFile.exists()){
				sockOutp.writeUTF("File does not Exist");
			}
			if(putFile.exists()){
				sockOutp.writeUTF("File Already Exists");
			}
				if(!ServerManager.addNewTransferCommand(PUT, id, putFile.getAbsolutePath())){
					while(!ServerManager.pollForFileWrite(id, putFile.getAbsolutePath())){
						Thread.sleep(100);
					}
				}
				//Thread.sleep(100);
				
				//----begin sending file to client-----
				sockOutp.writeUTF(Integer.toString(id));//send commandID
				if(!transferPutBytes(putFile,sockInp))
					return;
				ServerManager.remove(PUT,putFile.getAbsolutePath());
				sockOutp.writeUTF("Server: File "+fileName+" uploaded successfully");
				//sockOutp.writeUTF(id + "completed successfully");
				//cmdStatus.remove(id);
				
				/*	}
		else if(putFile.exists()){// && !ServerManager.transferMap.containsKey(Paths.get(putFile.getAbsolutePath()))
				sockOutp.writeUTF("File Already Exists");
			}
			else
				sockOutp.writeUTF("File in use");*/
			
		}catch(Exception e){
			System.out.println(":::: Exception in executing PUT function :: "+ e.getMessage());
			e.printStackTrace();
		}
	}
	
	public boolean transferPutBytes(File putFile, DataInputStream sockInp) {
		try {

		byte[] fileSizeBuffer = new byte[8];
		sockInp.read(fileSizeBuffer);
		ByteArrayInputStream bais = new ByteArrayInputStream(fileSizeBuffer);
		DataInputStream dis = new DataInputStream(bais);
		long fileSize = dis.readLong();
			
		FileOutputStream f = new FileOutputStream(putFile);
		int count = 0;
		byte[] buffer = new byte[1000];
		long bytesReceived = 0;
		while(bytesReceived < fileSize) {
			if (ServerManager.terminateSet.contains(cmdID)) {
				ServerManager.terminate(PUT, cmdID, putFile.getAbsolutePath());
				ServerManager.deletePutFile(putFile);
				f.close();
				return false;
			}else{
				
				count = sockInp.read(buffer);
				f.write(buffer, 0, count);
				bytesReceived += count;
			}
		}
		f.close();
		}
		catch(Exception e) {
			System.out.println("Error in transferring file");
		}
		//-----end----
		return true;
	}

	private String getFileName(String param) {
		// TODO Auto-generated method stub
		String retFileName = null;
		try{
			
			if(param.indexOf('\\') > -1 ){
				String[] p = param.split(Pattern.quote("\\"));
				retFileName = p[p.length - 1];
			}
			else if(param.indexOf('/') > -1){
				String[] p = param.split("/");
				retFileName = p[p.length - 1];
			}
			else{
				retFileName = param;
			}
		}catch(Exception e){
			System.out.println("Exception in getFileName Functions" + e.getMessage());
		}
    	return retFileName;
	}

}
