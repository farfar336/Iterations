package interation2;
/*
To do: 
	Document the following, specified by the TA:
		-state briefly that functional programming is the approach at play here [Farrukh] (done)
		-break down the general flow and structure of your script/program [Farrukh] (done)
		-Mention key classnames, function names, design patterns used, etc... Try to form somewhat of a cohesive narrative in this section, so it reads like a passage from your favorite novel.  [Farrukh]

	Code requirements:
		-Location Request [Farrukh] (done)
		-Updated Report Request [Farrukh]
		-Update source code variable [Farrukh]
		-Group management requirements (peer UDP/IP messages) [Xudong]
		-User interface and snippets requirements (snip UDP/IP messages) [Xudong]
		-Shutting down system requirements (stop UDP/IP messages) [Xudong]

	Other requirements:
		-Class diagram of your solution to the D2L dropbox for this iteration. [Farrukh]
		-A video that gives a brief explanation of your implementation and shows your peer in action. The length
		of your video should be between 2 and 7 minutes. Only the first 7 minutes of your video will be viewed
		when grading. Use the same D2L dropbox to either upload the video or provide a link to your video. [Farrukh]
		-Running your peer on Friday Feb 26 between 1pm and 2pm. [Farrukh] [Xudong]
*/

/*
 	Code documentation
	-Functional programming is used
	-General flow of code: Command line arguements are read to initalize variables. Then it connects the client to the registry 
	and starts processing requests. After all requests are complete, the connection is closed. Then the collaboration part is 
	started, where each peer keeps track of other peers by sending messages to each other. Additionally, a GUI is visible for 
	adding snippets of text to the system. Lamports logical clock ensures that the time stamps fulfill the happens-before order. 
	When the system is shutting down, it will send a message to all peers and terminate them. Finally, the registry is connected 
	again to process requests. After the requests are complete, the system stops.
	-Key functions: 
		-readCommandLineArguements: Read command line arguments
		-initializeGlobalVariables: Initialize all global variables
		-connectClient: Connect client to server
		-processRequests: Process requests from the server
		-teamNameRequest: Sends the team name to the server
		-codeRequest: Sends the code to the server
		-peersRequest: Receives and stores peers
		-reportRequest: Gets report about peers and sends it to the server
		-locationRequest: Sends the location to the server
		-storePeers: Stores peers
		-getPeerInfo: Gets info about peers and returns info in string format
		-getPeers: Gets a list of peers and returns it in string format
		-sendToServer: Sends the string to the server
		-To do: Mention more more from Xudong's code
*/

// Importing libraries
import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class client{
	// Global variables
	public static String teamName;
	public static String serverIP;
	public static int serverPort;
	public static String sourceLocation;
	public static int numberOfSources;
	public static int totalPeers;
	public static ArrayList<String> peerListRegistry;
	public static ArrayList<String> currentPeerList;
	public static HashMap<String, String> peersHashMap;
	public static Socket clientSocket;
	public static BufferedReader reader;

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
				"	public static ArrayList<String> peerListRegistry;\r\n" + 
				"	public static String date=\"\";\r\n" + 
				"	public static String command=null;\r\n" + 
				"	public static int numberOfSources=0;\r\n" + 
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
				"		for (int i=0;i<peerListRegistry.size();i++) {\r\n" + 
				"			peers+=peerListRegistry.get(i)+\"\\n\";\r\n" + 
				"		}\r\n" + 
				"		String toServer=totalPeers+\"\\n\"+peers+numberOfSources+\"\\n\";;\r\n" + 
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
				"			numberOfSources+=1;\r\n" + 
				"		}\r\n" + 
				"		totalPeers+=num;\r\n" + 
				"		System.out.println(\"num of peers :\"+num+\"\\n\");\r\n" + 
				"		if(num!=0) {\r\n" + 
				"			peers=\"\";\r\n" + 
				"			for(int i=0;i<num;i++) {\r\n" + 
				"				String peer=reader.readLine();		\r\n" + 
				"				if(!peerListRegistry.contains(peer)) {\r\n" + 
				"					peerListRegistry.add(peer);\r\n" + 
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
				"        peerListRegistry =new ArrayList<String>();\r\n" + 
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
	public static String peersToString(ArrayList<String> peersArray){
		String peers = "";
		for (int i=0;i<peersArray.size();i++) {
			if (i == 0){ //Don't add \n to the first entry
				peers+=peersArray.get(i); 
			}
			else{ //Add to the other entries
				peers+="\n" + peersArray.get(i);
			}
	
		}
		return peers;
	}

	// To do: Decide if want to remove this
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

	public static String getReport(){
		// Prepare variables
		int totalPeersFromRegistry = peerListRegistry.size();
		String peersFromRegistry = peersToString(peerListRegistry);
		Date aDate = new Date();
		String reportDateReceived = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(aDate);
		int totalcurrentPeers = currentPeerList.size(); 
		String currentPeerListString = peersToString(currentPeerList);
		int numberOfUDPsReceived = 99999; //To do: Use actual variable
		String UDPMessagesReceived = "To do: UDPMessagesReceived"; //To do: Use actual variable
		int numberOfUDPsSent = 99999; //To do: Use actual variable
		String UDPMessagesSent = "To do: UDPMessagesSent"; //To do: Use actual variable
		int numberOfSnippets = 99999; //To do: Use actual variable
		String snippetContents = "To do: snippetContents"; //To do: Use actual variable

		// Format report
		String report = 
		totalPeersFromRegistry + "\n" + // To do: Check this is correct with multiple peers
		peersFromRegistry + "\n" +  // To do: Check this is correct with multiple peers
		numberOfSources + "\n" + 
		sourceLocation + "\n" +   
		reportDateReceived + "\n" +
		totalcurrentPeers + "\n" + // To do: Check this is correct with multiple peers
		currentPeerListString + "\n" + // To do: Check this is correct with multiple peers
		numberOfUDPsReceived + "\n" +
		UDPMessagesReceived + "\n" +
		numberOfUDPsSent + "\n" +
		UDPMessagesSent + "\n" +
		numberOfSnippets + "\n" +
		snippetContents + "\n";

		System.out.println("report \n" + report);
		// report += getPeerInfo();
		return report;
	}

	// Gets report about peers and sends it to the server
	public static void reportRequest(Socket clientSocket) throws IOException {
		System.out.println(teamName + " - Received get report request");

		System.out.println(teamName + " - about to send report");
		String toServer = getReport() + "\n";
		sendToServer(toServer, clientSocket);
		System.out.println(teamName + " - Finished request");
	}
	
	// Stores peers 
	public static void storePeers(int num, BufferedReader reader) throws NumberFormatException, IOException {
		long time = new Timestamp(System.currentTimeMillis()).getTime();
		
		String peers = "";
		for(int i = 0; i < num; i ++) {
			String peer = reader.readLine();		
			if(!currentPeerList.contains(peer)) {
				currentPeerList.add(peer);
			}else {
				totalPeers -= 1;
			}
			peers += peer;
			peers += "\n";
		}

		String sourceAndTime = sourceLocation + "\n" + time;
		peersHashMap.put(sourceAndTime, peers);
	}

	// Receives and stores peers
	public static void peersRequest(BufferedReader reader) throws NumberFormatException, IOException {
		System.out.println(teamName + " - Received get peers request");
		
		int numOfPeersReceived = Integer.parseInt(reader.readLine());
		if(numOfPeersReceived > 0) {
			numberOfSources += 1;
		}
		totalPeers += numOfPeersReceived;

		if(numOfPeersReceived > 0) {
			storePeers(numOfPeersReceived, reader);
		}

		peerListRegistry = currentPeerList;
		System.out.println(teamName + " - Finished request");
	}

	// Sends the location to the server
	public static void locationRequest(Socket clientSocket) throws IOException {
		System.out.println(teamName + " - Received get location request");

		String toServer = sourceLocation + "\n";

		System.out.println("I'm at location: " + sourceLocation);
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

	// Read command line arguments
	public static void readCommandLineArguements(String [] args){
		serverIP = args[0];
		serverPort = Integer.parseInt(args[1]);
		teamName = args[2];
	}

	// Initialize all global variables
	public static void initializeGlobalVariables(){
		sourceLocation = serverIP + ":" + serverPort;
		numberOfSources = 0;
        peerListRegistry = new ArrayList <String>();
		currentPeerList = new ArrayList <String>();
        peersHashMap = new HashMap<String, String>();
	}
    public static void main(String[] args) throws IOException{
		// Initalize variables
		readCommandLineArguements(args);
		initializeGlobalVariables(); 

		// Process requests after starting up
		connectClient(serverIP,serverPort);
		processRequests();

		// To do: Put code for collaboration 
		System.out.println(teamName + " - Finished with registry, starting collaboration");

		// Process requests after shutting down
		// connectClient(serverIP, serverPort);
		// processRequests(); 
		System.out.println("Get report request will print this: \n" + getReport()); //To do: Remove this later
    }
}