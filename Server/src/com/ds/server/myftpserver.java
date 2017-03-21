package com.ds.server;

import java.net.ServerSocket;

public class myftpserver implements Runnable{
	ServerSocket commonPort = null;
	ServerSocket tPort = null;
	String serverType = null;
	static String serverRootDirectory;
	public myftpserver(String server,ServerSocket commonPort, ServerSocket tPort) {
		// TODO Auto-generated constructor stub
		serverType = server;
		this.commonPort = commonPort;
		this.tPort = tPort;
	}
	public myftpserver() {}

	public static void main(String args[]) {
		myftpserver server = new myftpserver();
		System.out.println("Running FTP server...." );
		server.makeConnection(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
		//server.makeConnection(1000,1001);
	}
	
	public void makeConnection(int portNo, int tPortNo) {
		try {
			commonPort = new ServerSocket(portNo);
			tPort = new ServerSocket(tPortNo);
			System.out.println("Hosting service on Port Number :: " + portNo + " and " + tPortNo);
			
			serverRootDirectory = System.getProperty("user.dir");
			
			new Thread(new myftpserver("normalThread",commonPort,tPort)).start();
			new Thread(new myftpserver("terminalThread",commonPort,tPort)).start();
		}
		catch(Exception e) {
			System.out.println("Unable to establish connection");
		}
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try{
		if(serverType.equals("normalThread")){
			while (true){
				Thread cst =  new Thread(new ClientServerThread(commonPort));
				System.out.println("Client Thread created");
				cst.start();
			}
		}else{
			while (true){
				Thread tst = new Thread(new Terminate(tPort));
				System.out.println("Terminate thread created");
				tst.start();
			}
		}
	}catch(Exception e){
		e.printStackTrace();
	}
	}

}
