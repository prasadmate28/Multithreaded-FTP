package com.ds.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class ClientThread implements Runnable {

	DataInputStream dInput;
	DataOutputStream dOutput;
	ClientCommands clientCommands;
	Socket clientSocket;
	Scanner sc;
	
	boolean ampersandFlag = false;
	HashMap<String,Object> clientInfo;
	String ampersandCommand;
	
	public ClientThread(HashMap<String,Object> clientInfo, 
			String ampersandCommand, String currentServerWorkingDirectory, boolean flag) throws Exception{
		
		this.clientSocket = new Socket((String)clientInfo.get("hostname"),Integer.parseInt((String) clientInfo.get("nport")));
		this.dInput = new DataInputStream(clientSocket.getInputStream());
		this.dOutput = new DataOutputStream(clientSocket.getOutputStream());
		this.sc = new Scanner(System.in);
		this.clientCommands = new ClientCommands(dInput,dOutput,currentServerWorkingDirectory);
		this.clientInfo = clientInfo;
		
		//make ampersand flag true in this new thread for ampersand functions
		this.ampersandFlag = true;
		this.ampersandCommand = ampersandCommand;

		System.out.println(" .....Client connected (for ampersand function) ..... ");
	}
	
	public ClientThread(HashMap<String, Object> clientInfo2) throws Exception{
		// TODO Auto-generated constructor stub
		this.clientInfo = clientInfo2;
		this.clientSocket = new Socket((String)clientInfo2.get("hostname"),Integer.parseInt((String) clientInfo2.get("nport")));
		this.dInput = new DataInputStream(clientSocket.getInputStream());
		this.dOutput = new DataOutputStream(clientSocket.getOutputStream());
		this.sc = new Scanner(System.in);
		this.clientCommands = new ClientCommands(dInput,dOutput);
		
		System.out.println(" .....Client connected..... "+clientInfo);
	}

	private void performClientFTPFunctions(String clientCommand) {
		
		try{
			
			
				String command[] = clientCommand.split(" ", 2);
				switch (command[0]) {
				
		            case ClientCommands.GET:
		            	clientCommands.get(command[1]);
		                break;
		            case ClientCommands.PUT:
		            	clientCommands.put(command[1]);
		                break;
		            case ClientCommands.DELETE:
		            	String delStatus = clientCommands.delete(command[0]+"#"+command[1]);
		            	System.out.println(delStatus);
		                break;
		            case ClientCommands.LS:
		            	String lsStatus = clientCommands.ls(command[0]);
		            	System.out.println(lsStatus );
		                break;
		            case ClientCommands.CD:
		            	String cdStatus = clientCommands.cd(command[0]+"#"+command[1]);
		                System.out.println(cdStatus);
		            	break;
		            case ClientCommands.MKDIR:   
		            	String mkdirStatus = clientCommands.mkdir(command[0]+"#"+command[1]);
		            	System.out.println(mkdirStatus);
		                break;
		            case ClientCommands.PWD:     
		            	String serverPath=clientCommands.pwd(command[0]);
		            	System.out.println(serverPath);
		                break;
		            case ClientCommands.TERMINATE:
		            	clientCommands.terminate(clientInfo,Integer.parseInt(command[1]));
		            	break;
		            default:        
		            	System.out.println("Invalid FTP command. Please enter correct command.");
		            	break;
				}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		sc = new Scanner(System.in);
		try{
			
			if(ampersandFlag){
				//generate new client thread for $ functions
				String command = ampersandCommand.substring(0,ampersandCommand.length()-1);
				command = command.trim();
				performClientFTPFunctions(command);
				// quit after done 
				dOutput.writeUTF("quit");
				System.out.println("Client :: signing off (ampersand thread)....");
				clientCommands.releaseClientResources();
				
			}else{
				
				while (true){
					System.out.print(myftp.PROMPT);
	
					String command = sc.nextLine();
					if(command.equalsIgnoreCase(ClientCommands.QUIT)){
						dOutput.writeUTF(command);
						System.out.println("Client signing off....");
						clientCommands.releaseClientResources();
						break;
					}
					else{
						if(!command.endsWith("&")){
							// if command does not contain &
							ampersandFlag = false;
							performClientFTPFunctions(command);
						}else{
							// command contains &
							new Thread(new ClientThread(clientInfo,command,clientCommands.currentWorkingDirectory,true)).start();
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
