package com.ds.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Terminate implements Runnable {
	Socket terminateClient;
	DataInputStream sockInp = null;
	DataOutputStream sockOutp = null;
	
	Terminate(ServerSocket tPort) throws IOException {
		terminateClient = tPort.accept();
	}

	public void run() {
		
		System.out.println("::: Inside terminate client thread :::" + Thread.currentThread().getId());
		try{

			sockInp = new DataInputStream(terminateClient.getInputStream());
			sockOutp = new DataOutputStream(terminateClient.getOutputStream());

			//Read Client request from InputStream.
			String commandId = sockInp.readUTF();
			
			if(ServerManager.checkForCommand(Integer.parseInt(commandId))){
			
				if(!ServerManager.addTerminateCommand(Integer.parseInt(commandId)))
					System.out.println("Command already held in termination queue :: " + commandId);
				else{
					System.out.println("Command held in queue for termination:: " + commandId);
				}
				
			}else{
				System.out.println("Invalid commandId for termination :: " + commandId);
			}

		}catch(Exception e){
			System.out.println("Exception in performing FTP functions ::: "+ e.getMessage());
			//e.printStackTrace();
		}

	}

	private void writeOnOutpLine(String writeMsg) {
		// TODO Auto-generated method stub
		try{
			sockOutp.writeUTF(writeMsg);
		}catch(IOException e){
			System.out.println("Exception in writing message on outline " + e.getMessage());
			//e.printStackTrace();
		}
	}

	public String readFromInpLine() {
		// TODO Auto-generated method stub
		String IOread = null;
		try{
			IOread = sockInp.readUTF();
		}catch (IOException ioe){
			//ioe.printStackTrace();
			System.out.println("Exception in reading from input line " + ioe.getMessage());
		}
		return IOread;
	}

}

