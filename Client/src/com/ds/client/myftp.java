package com.ds.client;

import java.util.HashMap;

public class myftp {

	static String PROMPT = "mytftp> ";

	public static void main(String args[]){
		
		try{
			
			HashMap<String,Object> clientInfo = new HashMap<String,Object>();
			/*clientInfo.put("hostname", args[0]);
			clientInfo.put("nport", args[1]);
			clientInfo.put("tport", args[2]);*/
			clientInfo.put("hostname", "192.168.1.10");//192.168.1.11
			clientInfo.put("nport", "1200");
			clientInfo.put("tport", "1201");
			new Thread(new ClientThread(clientInfo)).start();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
}
