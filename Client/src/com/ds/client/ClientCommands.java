package com.ds.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ClientCommands {

	//----------Constannts ------
			public static final String GET = "get";
			public static final String PUT = "put";
			public static final String LS = "ls";
			public static final String MKDIR = "mkdir";
			public static final String CD = "cd";
			public static final String PWD = "pwd";
			public static final String DELETE = "delete";
			public static final String QUIT = "quit";
			public static final String TERMINATE = "terminate";
	//---------------------------------
	DataInputStream dInput;
	DataOutputStream dOutput;
	Scanner sc;
	String currentWorkingDirectory;
	String currentClientDirectory;
	

	public ClientCommands(DataInputStream dInput, DataOutputStream dOutput) {
		// TODO Auto-generated constructor stub
		this.sc = new Scanner(System.in);
		this.dInput = dInput;
		this.dOutput = dOutput;
		this.currentWorkingDirectory = getServerRootDirectory();
		this.currentClientDirectory = System.getProperty("user.dir");
	}

	public ClientCommands(DataInputStream dInput1, DataOutputStream dOutput1,
			 String currentServerWorkingDirectory) {
		// TODO Auto-generated constructor stub
		this.sc = new Scanner(System.in);
		this.dInput = dInput1;
		this.dOutput = dOutput1;
		this.currentWorkingDirectory = setServerRootDirectory(currentServerWorkingDirectory);
		this.currentClientDirectory = System.getProperty("user.dir");
	}

	private String setServerRootDirectory(String currentServerWorkingDirectory) {
		// TODO Auto-generated method stub
		try {
			dOutput.writeUTF("setRootDirectory");
			dOutput.writeUTF(currentServerWorkingDirectory);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return currentServerWorkingDirectory;
	}

	private String getServerRootDirectory() {
		// TODO Auto-generated method stub
		try {
			dOutput.writeUTF("getRootDirectory");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return readFromInpLine();
	}

	public String delete(String cmd) throws IOException {
		writeOnOutpLine(cmd);
		return readFromInpLine();
	}

	public String ls(String cmd) throws IOException {
		writeOnOutpLine(cmd);
		return readFromInpLine();
	}

	public String cd(String cmd) throws IOException {
		writeOnOutpLine(cmd);
		//update server path
		String cdStatus = readFromInpLine();
		currentWorkingDirectory = pwd("pwd");
		return cdStatus;
	}

	public String mkdir(String cmd) throws IOException {
		// TODO Auto-generated method stub
		String status = null;
		writeOnOutpLine(cmd);
		status = readFromInpLine();
		return status;
	}

	public String pwd(String cmd) throws IOException {
		// TODO Auto-generated method stub
		String serverPath = null;
		
		writeOnOutpLine(cmd);
		serverPath = readFromInpLine(); 
		
		return serverPath;
	}
	
	
	public void get(String cmd) {
		// TODO Auto-generated method stub
		try{
			
			String filePath = getFileName(cmd);
			System.out.println("Filepath "+ filePath);
			//write command + param to server
			writeOnOutpLine("get");
			writeOnOutpLine(cmd);
			
	        String serverStatus = readFromInpLine();
	        if (serverStatus.equals("File Not Found")) {
	            System.out.println("File " + cmd + " doesn't exist on server. Check for the correct file location.");
	            return;
	        }
	        else if(serverStatus.equals("READY")) {
	        	
	        	// get command Id
	    		int commandId = Integer.parseInt(readFromInpLine());
	    		
        		System.out.println("\n Receiving file... for command ID:: " +commandId);
	        	
        		File fileIn = new File(sliceFileName(cmd));
	        	if (fileIn.exists()) {
	        		System.out.println("File already exists on path "+fileIn.getAbsolutePath()+
	        				"\n Do you wish to overwrite (y or n)");
	        		if (sc.next()=="n") {
	        			dOutput.flush();
	        			return;
	        		}
	        	}
	        	
	        	//receiving file in 1000 bytes size
	        	downloadFileInChunks(fileIn);
	        	
	            //----copy receiving 
	            System.out.println("Client: File downloaded successfully..");
	        }else{
	        	 System.out.println("Client: File already in use by some other process");
	        }
	    
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	public void put(String cmd) {
		// TODO Auto-generated method stub
		try{
			String filePath = currentWorkingDirectory + File.separator + cmd;
			File fileOut = new File(currentClientDirectory + File.separator + cmd);
			//--- check on system for existence of files
			if (!fileOut.exists()) {
				System.out.println("File " + filePath + " does  not exist");
				return;
			}
			
			writeOnOutpLine("put");
			writeOnOutpLine(cmd);
			//writeOnOutpLine(filePath);
	        String status = readFromInpLine();
	        //---- if file already exists on the server
	        if (status.equals("File Already Exists")){
	            System.out.println(status+"\n Do you wish to overwrite (y or n)");
	            //dOutput.writeUTF(sc.next());
	            if(sc.next()=="n") {
	                return;
	            }
	        }
	        
        	System.out.println("\n Sending file to server...");
        	
			int commandId = Integer.parseInt(readFromInpLine());
			System.out.println(" 'Put' command Id is :: " + commandId);
			
			//upload file in chunk of 1000 bytes
			uploadFileInChunks(currentClientDirectory+File.separator + cmd);
			
			System.out.println(readFromInpLine());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void terminate(HashMap<String, Object> clientInfo, int commandId) {
		// TODO Auto-generated method stub
		try{
			System.out.println("Terminate signal for command Id:: " + commandId);
			new Thread(new TerminateClient(clientInfo,commandId)).start();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
//=========================== Helper Functions =====================================//
	void releaseClientResources() {
		// TODO Auto-generated method stub
        try{
        	
        	this.dInput.close();
            this.dOutput.close();
            
        }catch(Exception e){
        	System.out.println("Error while releasing client resourcess :::"+ e.getMessage());
        	//e.printStackTrace();
        }
        
	}

	void writeOnOutpLine(String writeMsg) {
		// TODO Auto-generated method stub
		try{
			dOutput.writeUTF(writeMsg);
		}catch(IOException e){
			System.out.println("Exception in write on outputline method :: "+e.getMessage());
			//e.printStackTrace();
		}
	}
	
	String readFromInpLine() {
		// TODO Auto-generated method stub
    	String IOread = null;
    	try{
    		IOread = dInput.readUTF();
    	}catch (IOException ioe){
    		System.out.println("Exception in reading from input line :: "+ ioe.getMessage());
    		//ioe.printStackTrace();
    	}
		return IOread;
	}
	
	private void downloadFile(File fileName) {
		// TODO Auto-generated method stub
		try{
			FileOutputStream fout = new FileOutputStream(fileName);
            int ch;
            do {
                ch = Integer.parseInt(readFromInpLine());
                if (ch != -1) {
                    fout.write(ch);
                }
            }while(ch != -1);
            fout.close();
            System.err.println(readFromInpLine());
            
		}catch (IOException ioe){
			System.out.println(" Exception in downloading file ::: " + ioe.getMessage()) ;
			//ioe.printStackTrace();
		}
	}
	
	private String sliceFileName(String param) {
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

	private String getFileName(String param) {
		// TODO Auto-generated method stub
		String retFileName = null;
		try{
			
			if(param.indexOf('\\') > -1 || 
					param.indexOf('/') > -1){
				retFileName = param;
			}
			else{
				retFileName = currentWorkingDirectory + File.separator + param;
			}
		}catch(Exception e){
			System.out.println("Exception in getFileName Functions" + e.getMessage());
		}
    	return retFileName;
	}
	
	private void uploadFile(File fileOut) {
		// TODO Auto-generated method stub
		try{
			FileInputStream fin = new FileInputStream(fileOut);
	        int ch;
	        do {
	            ch = fin.read();
	            dOutput.writeUTF(String.valueOf(ch));
	        }while(ch != -1);
	        fin.close();
	        System.err.println(readFromInpLine());
		}catch (IOException ioe){
			System.out.println(" Exception in upload file method :: "+ ioe.getMessage());
			//ioe.printStackTrace();
		}
	}
	
	private void downloadFileInChunks(File fileName) {
		
		try{
			byte[] fileSizeBuffer = new byte[8];
			dInput.read(fileSizeBuffer);
			ByteArrayInputStream bais = new ByteArrayInputStream(fileSizeBuffer);
			DataInputStream dis = new DataInputStream(bais);
			long fileSize = dis.readLong();

			FileOutputStream f = new FileOutputStream(fileName);
			int count = 0;
			byte[] buffer = new byte[1000];
			long bytesReceived = 0;
			while(bytesReceived < fileSize) {
				count = dInput.read(buffer);
				f.write(buffer, 0, count);
				bytesReceived += count;
			}
			f.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void uploadFileInChunks(String filePath) {
		// TODO Auto-generated method stub
		byte[] buffer = new byte[1000];
		try {
			File f = new File(filePath);
			
			long fileLength = f.length();
			byte[] fileSizeBytes = ByteBuffer.allocate(8).putLong(fileLength).array();
			dOutput.write(fileSizeBytes, 0, 8);
			
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
			int count = 0;
			while((count = in.read(buffer)) > 0)
				dOutput.write(buffer, 0, count);
			
			in.close();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
