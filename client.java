package interation2;
/*
To do: Document the following, specified by the TA:
	-state briefly that functional programming is the approach at play here
	-break down the general flow and structure of your script/program
	-Mention key classnames, function names, design patterns used, etc... Try to form somewhat of a cohesive narrative in this section, so it reads like a passage from your favorite novel. 
*/

// Importing libraries
import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import interation2.Peer;
public class client{
	// Global variables
	public static String teamName;
	public static String serverIP;
	public static int serverPort;
	public static String sourceLocation;
	public static int numOfSource;
	public static int totalPeers;
	public static String peers;
	public static ArrayList<String> peerList;
	public static HashMap<String, String> peersHashMap;
	public static Socket clientSocket;
	public static BufferedReader reader;
	public static Peer peer;
	public static int UDPport;
	public static String snips;
	public static long startsnip;
	Thread mythread;
	
	
	// Sends the string to the server
	public static void sendToServer(String toServer, Socket clientSocket) throws IOException {
		clientSocket.getOutputStream().write(toServer.getBytes());
	}

	// Sends the team name to the server
	public static void teamNameRequest(Socket clientSocket) throws IOException {
		String toServer = teamName + "\n";
		System.out.println(teamName + " - Received get team name request");
		System.out.println(teamName + " - about to send team name ");
		sendToServer(toServer, clientSocket);
		System.out.println(teamName + " - Finished request");
	}

	// Sends the code to the server
	public static void codeRequest(Socket clientSocket) throws IOException {
		System.out.println(teamName + " - Received get code request");

		// To do: Update this at the nd
		//Put the source code into sourceCode in order to send to the server
		String sourceCode = "import java.io.*;\r\n" + 
				"import java.net.Socket;\r\n" + 
				"import java.net.UnknownHostException;\r\n" + 
				"import java.sql.Timestamp;\r\n" + 
				"import java.text.SimpleDateFormat;\r\n" + 
				"import java.util.ArrayList;\r\n" + 
				"import java.util.Date;\r\n" + 
				"import java.util.HashMap;\r\n" + 
				"import java.util.Map;\r\n" + 
				"public class clientSocket{\r\n" + 
				"	public static String teamName=\"Xudong Farrukh\\n\";\r\n" + 
				"	public static String codeResponse=\"Java\\n\";\r\n" + 
				"	public static int totalPeers=0;\r\n" + 
				"	public static String peers=\"\";\r\n" + 
				"	public static ArrayList<String> peerList;\r\n" + 
				"	public static String date=\"\";\r\n" + 
				"	public static String command=null;\r\n" + 
				"	public static int numOfSource=0;\r\n" + 
				"	public static String sourceLocation;\r\n" + 
				"	public static int port;\r\n" + 
				"	public static String ip;\r\n" + 
				"	public static HashMap<String, String> peersHashMap;\r\n" + 
				"	//write the team name to the server.\r\n" + 
				"	public static void getTeamName(Socket clientSocket) throws IOException {\r\n" + 
				"		String toServer=teamName;\r\n" + 
				"		System.out.println(toServer);\r\n" + 
				"		clientSocket.getOutputStream().write(toServer.getBytes());\r\n" + 
				"	}\r\n" + 
				"	//send the code toServer to server.\r\n" + 
				"	public static void codeRequest(Socket clientSocket) throws IOException {\r\n" + 
				"		//put the source code into sourceCode in order to send to the server\r\n" + 
				"		String sourceCode=\"\";\r\n" + 
				"		String end=\"\\n...\\n\";\r\n" + 
				"		String toServer=codeResponse+sourceCode+end;\r\n" + 
				"		clientSocket.getOutputStream().write(toServer.getBytes());\r\n" + 
				"		System.out.println(\"get code finished\");\r\n" + 
				"	}\r\n" + 
				"	//send the report to server\r\n" + 
				"	public static void getReport(Socket clientSocket) throws IOException {\r\n" + 
				"		peers=\"\";\r\n" + 
				"		for (int i=0;i<peerList.size();i++) {\r\n" + 
				"			peers+=peerList.get(i)+\"\\n\";\r\n" + 
				"		}\r\n" + 
				"		String toServer=totalPeers+\"\\n\"+peers+numOfSource+\"\\n\";;\r\n" + 
				"		for(Map.Entry<String, String> entry : peersHashMap.entrySet()) {\r\n" + 
				"		    String key = entry.getKey().split(\"\\n\")[0];\r\n" + 
				"		    Date getDate  =new Date( Long.parseLong(entry.getKey().split(\"\\n\")[1]));\r\n" + 
				"		    date=(new SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss\")).format(getDate);\r\n" + 
				"		    toServer+=key+\"\\n\"+date+\"\\n\";\r\n" + 
				"		    String value = entry.getValue();\r\n" + 
				"		    toServer+=value.split(\"\\n\").length+\"\\n\";\r\n" + 
				"		    toServer+=value;  \r\n" + 
				"		}\r\n" + 
				"		System.out.println(toServer);\r\n" + 
				"		clientSocket.getOutputStream().write(toServer.getBytes());\r\n" + 
				"		System.out.println(\"get report finished\");\r\n" + 
				"	}\r\n" + 
				"	\r\n" + 
				"	//receive and store peers\r\n" + 
				"	public static void receivePeers(BufferedReader reader) throws NumberFormatException, IOException {\r\n" + 
				"		long time=new Timestamp(System.currentTimeMillis()).getTime();\r\n" + 
				"		System.out.println(date);\r\n" + 
				"		int num=Integer.parseInt(reader.readLine());\r\n" + 
				"		if(num>0) {\r\n" + 
				"			numOfSource+=1;\r\n" + 
				"		}\r\n" + 
				"		totalPeers+=num;\r\n" + 
				"		System.out.println(\"num of peers :\"+num+\"\\n\");\r\n" + 
				"		if(num!=0) {\r\n" + 
				"			peers=\"\";\r\n" + 
				"			for(int i=0;i<num;i++) {\r\n" + 
				"				String peer=reader.readLine();		\r\n" + 
				"				if(!peerList.contains(peer)) {\r\n" + 
				"					peerList.add(peer);\r\n" + 
				"				}else {\r\n" + 
				"					totalPeers-=1;\r\n" + 
				"				}\r\n" + 
				"				peers+=peer;\r\n" + 
				"				peers+=\"\\n\";\r\n" + 
				"			}\r\n" + 
				"			String key=sourceLocation+\"\\n\"+time;\r\n" + 
				"			String value=peers;\r\n" + 
				"			peersHashMap.put(key,value);\r\n" + 
				"		}\r\n" + 
				"		System.out.println(\"receive peers finished\");\r\n" + 
				"	}\r\n" + 
				"    public static void main(String[] args) throws IOException{\r\n" + 
				"    	ip=args[0];\r\n" + 
				"    	port=Integer.parseInt(args[1]);\r\n" + 
				"    	sourceLocation=ip+\":\"+port;\r\n" + 
				"        Socket clientSocket=new Socket(ip,port);\r\n" + 
				"        peerList =new ArrayList<String>();\r\n" + 
				"        peersHashMap=new HashMap<String, String>();\r\n" + 
				"        BufferedReader reader=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));\r\n" + 
				"        while (!(command=reader.readLine()).startsWith(\"close\")) {\r\n" + 
				"        	System.out.println(command);\r\n" + 
				"        	switch(command) {\r\n" + 
				"        	case \"get team name\":\r\n" + 
				"        		getTeamName(clientSocket);\r\n" + 
				"        		break;\r\n" + 
				"        	case \"get code\":\r\n" + 
				"        		codeRequest(clientSocket);\r\n" + 
				"        		break;\r\n" + 
				"        	case \"receive peers\":	\r\n" + 
				"        		receivePeers(reader);\r\n" + 
				"        		break;\r\n" + 
				"        	case \"get report\":\r\n" + 
				"        		getReport(clientSocket);	\r\n" + 
				"        		break;\r\n" + 
				"        	}\r\n" + 
				"\r\n" + 
				"        }\r\n" + 
				"        clientSocket.close();\r\n" + 
				"    }\r\n" + 
				"}";
		String end = "\n...\n";
		String codeResponse = "Java\n";
		String toServer = codeResponse + sourceCode + end;

		System.out.println(teamName + " - about to send source code ");
		sendToServer(toServer, clientSocket);
		System.out.println(teamName + " - Finished request");
	}
	
	// Gets a list of peers and returns it in string format
	public static String getPeers(){
		peers = "";
		for (int i=0;i<peerList.size();i++) {
			peers+=peerList.get(i)+"\n";
		}
		return peers;
	}

	// Gets info about peers and returns info in string format
	public static String getPeerInfo(){
		String peerInfo = "";
		String todaysDate;
		for(Map.Entry<String, String> entry: peersHashMap.entrySet()) {
		    String serverIPAndPortNumber = "Server IP and Port Number: " + entry.getKey().split("\n")[0];
		    Date date = new Date( Long.parseLong(entry.getKey().split("\n")[1]));
		    todaysDate = "Today's date: " + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(date);
		    peerInfo += serverIPAndPortNumber+"\n"+todaysDate+"\n";
		    String peerIPAndPortNumber = "Peer IP and Port Number: " + entry.getValue();
		    peerInfo += peerIPAndPortNumber.split("\n").length+"\n";
		    peerInfo += peerIPAndPortNumber;  
		}
		return peerInfo;
	}

	// To do: Update report request
	//Gets report about peers and sends it to the server
	public static void reportRequest(Socket clientSocket) throws IOException {
		System.out.println(teamName + " - Received get report request");

		peers = getPeers();
		String toServer = totalPeers + "\n" + peers + numOfSource + "\n";
		toServer += getPeerInfo();

		System.out.println(teamName + " - about to send report");
		sendToServer(toServer, clientSocket);
		System.out.println(teamName + " - Finished request");
	}
	
	// Store peers 
	public static void storePeers(int num, BufferedReader reader) throws NumberFormatException, IOException {
		long time = new Timestamp(System.currentTimeMillis()).getTime();
		
		peers = "";
		for(int i = 0; i < num; i ++) {
			String peer = reader.readLine();		
			if(!peerList.contains(peer)) {
				peerList.add(peer);
			}else {
				totalPeers -= 1;
			}
			peers += peer;
			peers += "\n";
		}
		peer.setPeerList(peerList);
		String sourceAndTime = sourceLocation + "\n" + time;
		peersHashMap.put(sourceAndTime, peers);
	}

	// Receives and stores peers
	public static void peersRequest(BufferedReader reader) throws NumberFormatException, IOException {
		System.out.println(teamName + " - Received get peers request");
		
		int numOfPeersReceived = Integer.parseInt(reader.readLine());
		if(numOfPeersReceived > 0) {
			numOfSource += 1;
		}
		totalPeers += numOfPeersReceived;

		if(numOfPeersReceived > 0) {
			storePeers(numOfPeersReceived, reader);
		}

		System.out.println(teamName + " - Finished request");
	}

	// Sends the team name to the server
	public static void locationRequest(Socket clientSocket) throws IOException {
		System.out.println(teamName + " - Received get location request");

		String toServer = sourceLocation + "\n";

		System.out.println("I'm at location: " + peer.getPort());
		sendToServer(toServer, clientSocket);
		System.out.println(teamName + " - Finished request");
	}

	// Process requests from the server
	public static void processRequests() throws IOException{
		String request;
		while (!(request = reader.readLine()).startsWith("close")) {
			System.out.println(teamName + " - Request received: " + request);
			switch(request) {
			case "get team name":
				teamNameRequest(clientSocket);
				break;
			case "get code":
				codeRequest(clientSocket);
				break;
			case "receive peers":	
				peersRequest(reader);
				break;
			case "get report":
				reportRequest(clientSocket);	
				break;
			case "get location":
				locationRequest(clientSocket);
				break;
			}
		}
		clientSocket.close();
	}

	// Connect client to server
	public static void connectClient(String serverIP, int serverPort)throws IOException{
        clientSocket = new Socket(serverIP, serverPort);
        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
	}

	// Read command line arguements
	public static void readCommandLineArguements(String [] args){
		serverIP = args[0];
		serverPort = Integer.parseInt(args[1]);
		teamName = args[2];
	}

	// Initialize all global variables
	public static void initializeGlobalVariables(){
		sourceLocation = serverIP + ":" + serverPort;
        peerList = new ArrayList <String>();
        peersHashMap = new HashMap<String, String>();
		numOfSource = 0;
		totalPeers = 0;
		peers = "";
	}


    public static void main(String[] args) throws IOException, InterruptedException{
		// Initalize variables
		readCommandLineArguements(args);
		initializeGlobalVariables();
		peer=new Peer();
		peer.setUpUDPserver();
		Runnable task1=() -> { try {
			peer.thread1();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}};
		
		
		Thread mythread=new Thread(task1);
		
		// Process requests after starting up
		connectClient(serverIP,serverPort);
		
		processRequests();
		peer.setPeerList(peerList);
		// To do: Put code for collaboration 
		System.out.println(teamName + " - Finished with registry, starting collaboration");
		mythread.run();
	

		// Process requests after shutting down
		connectClient(serverIP, serverPort);
		processRequests(); 
		
    }
}