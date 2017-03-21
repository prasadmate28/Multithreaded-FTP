package com.ds.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class TerminateClient implements Runnable {
	DataInputStream dInput;
	DataOutputStream dOutput;
	//ClientManager clientManager;
	ClientCommands clientCommands;
	Socket terminateSocket;
	int terminatecommandId;
	
/*	public TerminateClient(HashMap<String, Object> clientInfo, ClientManager clientManager, int commandId) throws Exception {
		this.terminateSocket = new Socket((String)clientInfo.get("hostname"),Integer.parseInt((String) clientInfo.get("tport")));
		this.dInput = new DataInputStream(terminateSocket.getInputStream());
		this.dOutput = new DataOutputStream(terminateSocket.getOutputStream());
		this.terminatecommandId = commandId;
		this.clientManager = clientManager;
	}*/
	
	public TerminateClient(HashMap<String, Object> clientInfo, int commandId) throws Exception{
		// TODO Auto-generated constructor stub
		this.terminateSocket = new Socket((String)clientInfo.get("hostname"),Integer.parseInt((String) clientInfo.get("tport")));
		this.dInput = new DataInputStream(terminateSocket.getInputStream());
		this.dOutput = new DataOutputStream(terminateSocket.getOutputStream());
		this.terminatecommandId = commandId;
	}

	public void run() {
		
		try{
			dOutput.writeUTF(Integer.toString(terminatecommandId));
		}catch (IOException e){
			System.out.println("Exception in terminate run on client side::");
		}
	}
}
