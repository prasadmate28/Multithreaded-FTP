package com.ds.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientServerThread implements Runnable {
	DataInputStream sockInp = null;
	DataOutputStream sockOutp = null;
	String serverRootDirectory;
	Socket normalClient;
	
	public ClientServerThread() {}

	ClientServerThread(ServerSocket s) throws IOException {
		normalClient = s.accept();
		System.out.println("----- A Client connected to me ----- IP Address :: "+ normalClient.getInetAddress()+" :: Host Name :: "+normalClient.getInetAddress().getHostName());//;
	}

	public void run() {

		try{

			sockInp = new DataInputStream(normalClient.getInputStream());
			sockOutp = new DataOutputStream(normalClient.getOutputStream());
			ServerUtility serverUtility = new ServerUtility();
			String clientReq = null;

			while(true){
				//Read Client request from InputStream.
				clientReq = sockInp.readUTF();
				String command = null, parameters = null;

				if(clientReq.contains("#")){
					command = clientReq.split("#")[0];
					parameters = clientReq.split("#")[1];
					
				}else{
					command = clientReq;
				}
				
				if(command.equals(ServerUtility.QUIT)){
					try{
						sockInp.close();
						sockOutp.close();
						System.out.println("Client connection terminated on request :: Client Name :: " + normalClient.getInetAddress().getHostName());
						normalClient.close();
					}catch (IOException e){
						System.out.println("Exception in quit command "+e.getMessage());
						
					}finally{break;}
				}
				System.out.println("Client command :: " + command);
				switch (command){

				case ServerUtility.GET:
					if(!serverUtility.executeGET(sockInp,sockOutp)){
						System.out.println("Get command terminated :: Operation unsuccessful " + Thread.currentThread().getId());
					}
					continue;
				case ServerUtility.PUT:
					serverUtility.executePUT(sockInp,sockOutp);
					continue;
				case ServerUtility.PWD:
					writeOnOutpLine(serverUtility.executePWD().toString());
					System.out.println("PWD Command excuted by client "+ Thread.currentThread().getId());
					continue;
				case ServerUtility.CD:
					writeOnOutpLine(serverUtility.executeCD(parameters));
					System.out.println("CD Command excuted  by client "+ Thread.currentThread().getId() + "::"+parameters);
					continue;
				case ServerUtility.LS:
					writeOnOutpLine(serverUtility.executeLS());
					System.out.println("LS Command excuted by client "+ Thread.currentThread().getId());
					continue;
				case ServerUtility.MKDIR:
					serverUtility.executeMKDIR(parameters);
					writeOnOutpLine("New Directory made");
					System.out.println("MKDIR Command excuted by client "+ Thread.currentThread().getId()+"::"+parameters);
					continue;
				case ServerUtility.DELETE:
					if(serverUtility.executeDELETE(parameters)){
						writeOnOutpLine("File successfully deleted");
					}else{
						writeOnOutpLine("File delete unsuccessful. Either file is not present or is held for transfer.");
					}
					System.out.println("DELETE Command excuted by client "+ Thread.currentThread().getId()+"::"+parameters);
					continue;
				case "getRootDirectory":
					writeOnOutpLine(myftpserver.serverRootDirectory);
					continue;
				case "setRootDirectory":
					serverUtility.currentDirectory = readFromInpLine();
					continue;	
				}
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
