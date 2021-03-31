Wed Mar 31 17:47:21 MDT 2021
java
package interation2;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;


/*
Key functions:
	-setUpUDPserver: This method will setup a UDP socket and store the port number of UDP
	-receiveMessage: Receive the message from other UDP
	-getMyLocation: Get the location of our peer
	-getPeerLocation: Get the location of a peer
	-getPort: Get the port number of this UDP server
	-getAddress: Get the IP address of the UDP server
	-sendMessage: Send message to other UDP server
	-addActivePeer: Add peer to active peerlist
	-setActivePeerList: Set active peer list
	-setStop: If we receive a stop, then close the UDP
	-sendInfo: Send info to other server
	-getMessage: When a peer recieves a UDP, act accordingly based on if it is a snip, stop, or peer message
	-removePeerFromActivePeerList: Remove a peer from active peer list
	-sendPeer: Send peer information
*/

public class Peer {
	public byte[] inbuf = new byte[10000];
	public byte[] outbuf = new byte[10000];
	public volatile ArrayList<String> activePeerList = new ArrayList<String>();
	public DatagramPacket UDPinPacket = new DatagramPacket(inbuf,inbuf.length);
	public DatagramPacket UDPoutPacket;
	public DatagramSocket UDPserver;
	public int UDPport;
	public Boolean stop = false;
	String snips;
	String message;
	String teamName;
	int nextTimeStamp;
	ConcurrentHashMap<Integer,ArrayList<String>> responseToSnip=new ConcurrentHashMap<Integer,ArrayList<String>>();
	HashMap<Integer, String> TimestampAndSnippet=new HashMap<Integer, String>();
	// This method will setup a UDP socket and store the port number of UDP
	public void setUpUDPserver() throws SocketException {
		UDPserver = new DatagramSocket();
		UDPserver.setSoTimeout(5000);
		UDPport = UDPserver.getLocalPort();
	}
	// Receive the message from other UDP
	public String receiveMessage() throws IOException {
		UDPserver.receive(UDPinPacket);
		String message = new String(UDPinPacket.getData(),0,UDPinPacket.getLength());
		return message;
	}

	// Get the location of our peer
	public String getMyLocation() throws UnknownHostException {
		String IP = String.valueOf(getAddress());
		IP = IP.replace("/",""); //Remove the / at the beginning
		String port = String.valueOf(UDPport);
		String location = IP + ":" + port;
		return location;
	}

	// Get the location of a peer
	public String getPeerLocation(String peer) throws UnknownHostException {
		String IP = String.valueOf(InetAddress.getByName(peer.split(":")[0]));
		IP = IP.replace("/",""); //Remove the / at the beginning
		String port = String.valueOf(Integer.parseInt(peer.split(":")[1]));
		String location = IP + ":" + port;
		return location;
	}

	//Gets the location of the peer
	public String getLocation() {
		String address = UDPinPacket.getAddress().toString().replace("/","");
		int port = UDPinPacket.getPort();
		String location = address + ":" + port;
		return location;
	}
	
	// Get the port number of this UDP server
	public int getPort() {
		return UDPport;
	}
	
	// Get the IP address of the UDP server
	public InetAddress getAddress() throws UnknownHostException {
		//return InetAddress.getByName("localhost");
		return InetAddress.getByName(InetAddress.getLocalHost().toString().split("/")[1]);
	}
	
	// Send message to other UDP server
	public void sendMessage(String message,InetAddress IP, int port) throws IOException {
		outbuf = message.getBytes();
		UDPoutPacket = new DatagramPacket(outbuf,outbuf.length,IP,port);
		UDPserver.send(UDPoutPacket);
	}
	
	// Add peer to active peerlist
	//if this peer is a new peer, and there is a snippet history, send this peer with the history of snippets
	public void addActivePeer(String peer) throws IOException {
		peer=peer.replace("/", "");
		if(!activePeerList.contains(peer)) {
			activePeerList.add(peer);
			if(snips!=null&&peer!=getMyLocation()) {
				InetAddress ip=InetAddress.getByName(peer.split(":")[0]);
				int port=Integer.parseInt(peer.split(":")[1]);
				System.out.println(getCatchUpMessages());
				String[] catcuUpMessagesArray=getCatchUpMessages().split("\n");
				//we send each line at one time
				for(String line:catcuUpMessagesArray) {
					sendMessage(line,ip,port);
				}
			}
		}	
	}
	
	/*
	 * this method is used to form a catch up message
	 * the format for every line is  
	 * "ctch"<Original Sender>+Space+<timestamp>+space+<content>
	 */
	public String getCatchUpMessages() throws UnknownHostException {
		String mes="";
		String[] snippetArray=snips.split("\n");
		for(String line:snippetArray) {
			mes+="ctch";
			String[] temp=line.split(" ");
			int timestamp=Integer.parseInt(temp[0]);
			String content=temp[1];
			String originalSender=temp[2];
			mes+=originalSender+" ";
			mes+=timestamp+" ";
			mes+=content+"\n";
		}
		return mes;
	}
	
	// Set active peer list
	public void setActivePeerList(ArrayList<String> newPeerList) {
		activePeerList = newPeerList;
	}
	
	// If we receive a stop, then close the UDP
	public void setStop() {
		System.out.println("stop UDP");
		stop = true;
		UDPserver.close();
	}

	// Send info to all the other peers
	public void sendInfo(String message,ArrayList<String> list) throws IOException {
		for(int i = 0; i < list.size(); i++) {
			InetAddress ip = InetAddress.getByName(list.get(i).split(":")[0]);
			int port = Integer.parseInt(list.get(i).split(":")[1]);
			sendMessage(message, ip, port);
		}
	}
	
	// When a peer recieves a UDP, act accordingly based on if it is a snip, stop, or peer message
	public String getMessage() throws IOException {
			message=receiveMessage();
			//System.out.println("Messaged received: " + message);
			if(message.startsWith("stop")) {
				sendInfo("stop",activePeerList);
				InetAddress ip=InetAddress.getByName(getLocation().split(":")[0]);
				int port=Integer.parseInt(getLocation().split(":")[1]);
				String response="ack"+teamName;
				sendMessage(response,ip,port);
				setStop();
				System.out.println("send  "+response+"  to "+getLocation());
			}
			else if(message.startsWith("snip")) {
					String newsnip=message.replace("snip", "");
					String[] snipArray=newsnip.split("\n");
					int receivedTimestamp=Integer.parseInt(snipArray[snipArray.length-1].split(" ")[0]);
					String sender=getLocation();
					String content=snipArray[snipArray.length-1].split(" ")[1];
					if(!TimestampAndSnippet.containsKey(receivedTimestamp)) {
						nextTimeStamp=receivedTimestamp+1;
						TimestampAndSnippet.put(receivedTimestamp, content+" "+sender);
						snips=getSnipsFromHashmap();
						System.out.print(snips);
					}
					//in case the ack message is not received by the sender
					sendMessage("ack "+receivedTimestamp,InetAddress.getByName(getLocation().split(":")[0]),Integer.parseInt(getLocation().split(":")[1]));
				}
				

			
			else if(message.startsWith("peer")){
				String peer=message.replace("peer", "");
				addActivePeer(peer);
				System.out.println("Peer added   "+peer);
				return message;
			}
			/* If we received a ack, we will update the hashmap(responseToSnip)
			 * such that we can detect which peer did not send a ack to us
			 * 
			 * 
			 */
			else if(message.startsWith("ack")) {
				int receivedTimestamp=Integer.parseInt(message.split(" ")[1]);
				ArrayList <String>currentList =responseToSnip.get(receivedTimestamp);
				if(currentList!=null) {
					if(!currentList.contains(getLocation())) {
						currentList.add(getLocation());
						responseToSnip.put(receivedTimestamp, currentList);
					}
				}else {
					currentList=new ArrayList<String>();
					currentList.add(getLocation());
					responseToSnip.put(receivedTimestamp, currentList);
				}
				
				
				return message;
			}
			else if(message.startsWith("ctch")) {
				
				handleCatchUpMessage(message);
			}
			return null;
	}
	
	// Remove a peer from active peer list 
	
	public void removePeerFromActivePeerList(String peer) {
		if(activePeerList.contains(peer)) {
			activePeerList.remove(activePeerList.indexOf(peer));
		}
		System.out.println("current list"+activePeerList);

	}
	public void removePeerFromActivePeerList(ArrayList<String> inactivePeerList) {
		if(inactivePeerList!=null) {
			activePeerList.removeAll(inactivePeerList);
		}
		System.out.println("current list"+activePeerList);
	}
	
	/*
	 * handle the catch up message,because catch up message has different protocol
	 * put the timestamp and snippet and sender into the hashmap.
	 */
	public void handleCatchUpMessage(String message) {
		message=message.replace("ctch", "");
		String originalSender=message.split(" ")[0];
		int receivedTimestamp=Integer.parseInt(message.split(" ")[1]);
		String snippet = message.split(" ")[2];

		TimestampAndSnippet.put(receivedTimestamp, snippet+" "+originalSender);
		snips=getSnipsFromHashmap();
		if(receivedTimestamp>nextTimeStamp-1) {
			System.out.println(receivedTimestamp+" "+snippet+" "+originalSender);
			nextTimeStamp=receivedTimestamp+1;
		}
		
	}
	
	//read the snippet from the hashmap, in the order of timestamp
	public String getSnipsFromHashmap() {
		String result="";
		Object[] keys = TimestampAndSnippet.keySet().toArray();
		Arrays.sort(keys);
		for(Object key:keys) {
			result+=key+" "+TimestampAndSnippet.get(key)+"\n";
		}
		
		return result;
	}
	
	// Send peer information
	public void sendPeer() throws InterruptedException, IOException {
		if(activePeerList.size() > 0) {
			String ip = InetAddress.getByName(InetAddress.getLocalHost().toString().split("/")[1]).toString();
			String message = "peer" + ip.replace("/", "") + ":" + getPort();
			sendInfo(message,activePeerList);
		}
	}

}





package interation2;

/*
	To do:
		-Required part: code [Xudong]
			-Look in the iteration 3 pdf, Shutting down system requirements (stop UDP/IP messages)

		-Optional part: code [Xudong] 
			-Look in the iteration 3 pdf, User interface and snippets requirements (snip UDP/IP messages)
			-Look in the iteration 3 pdf, Group management requirements (peer UDP/IP messages)

		-Required part: report [Farrukh]
			-Look in the iteration 3 pdf, Updated report request (stop UDP/IP messages)

		-Optional part: report [Farrukh] 
			-Look in the iteration 3 pdf, Updated Registry Communication Protocol

		-Fix UDP peer messages in report [Farrukh]
			-Clarify with professor on what is expected
			-Implement changes

		-Class diagram [Farrukh]
			-Can add stuff on to the diagram from iteration 2

		-Code documentation [Farrukh]
			-Update Code documentation
			-Add more comments where needed

		-Video demo (5-10 mins) [Farrukh]
			-A video that gives a brief explanation of your implementation, shows your peer in action, and explains
			under which circumstances a peer would not receive a stop message, even with the resends in
			iteration 3.

		-Submitting our work [Farrukh]
			-Friday March 26 between 10am and 10:30 am, or
			-Friday March 26 between 11:30 and 12:30 am, or
			-Friday March 26 between 1pm and 2pm, or
			-Wednesday March 31 between 11am and 11:30 am, or
			-Wednesday March 31 between 12:30pm and 1:30 pm.
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
		-sendToServer: Sends the string to the server
		-teamNameRequest: Sends the team name to the server
		-codeRequest: Sends the code to the server
		-arrayToString: Gets a list of peers and return it in string format
		-countLines: Returns the number of UDP
		-getReport: Get the report 
		-reportRequest: Gets report about peers and sends it to the server
		-storePeers: Store peers 
		-peersRequest: Receives and stores peers
		-locationRequest: Sends the team name to the server
		-processRequests: Process requests from the server
		-connectClient: Connect client to server
		-readCommandLineArguements: Read command line arguements
		-initializeGlobalVariables: Initialize all global variables
		-checkTimeLimit: Check if the time difference between current time and recorded time is greater than 240 seconds
		-addToPeersSent: Store info about a message when we send a peer and format it properly
		-addToPeersReceived: Store info about a message when a peer is received and format it properly
		-receiveMessage: Handle the logic for a when peer receives a message
		-collaborationThread: A thread to collaborate with other peers and send peer information to them
		-getSnipThread: Get input from user and format it. If input has more than 25 characters, get the first 25 characters
		-checkActivePeerThread: Check if a peer is active or not. If a peer does not send their peer info for more than 4 mins, then remove it from the active peer list
*/

// Importing libraries
import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Client{
	// Global variables

	// Variables related to team information
	public static String teamName;
	public static String teamLocation;

	// Variables related to peers
	public static int totalPeers;
	public static ArrayList<String> peerListRegistry;
	public static ArrayList<String> currentPeerList;
	public static HashMap<String, String> peersHashMap;
	//contain all the peers that does not response(ack message) to snippet
	public static ArrayList<String> missingAckPeers;
	//contain all the peers that does not send their peer message.
	public static ArrayList<String> silentPeers;
	public static Peer peer;
	public static Executor executor,executor1,executor2;
	public static final int inactiveTimeLimit=240;
	
	//threadLocal allow us to create a local variable in the thread.
	public static ThreadLocal<String> myThreadLocal = new ThreadLocal<String>();

	// this is a hashmap that stores location and time
	// location is the other peer's location
	// time is the timestamp(in second) that receive the peer info from this peer's location
	public static ConcurrentHashMap<String,Long> locationAndTime;
	// Variables related to connecting client
	public static Socket clientSocket;
	public static BufferedReader reader;
	public static String sourceLocation;
	public static String serverIP;
	public static int serverPort;
	public static int numberOfSources;


	// Variables related to UDP messages
	public static String peersReceived;
	public static String peersSent;
	public static String snippets;
	public static int nextSnipTimestamp;
	public static final int maxSnipLength=25;
	public static String latestSnip;
	
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

	// Read the source code
	public static String readSourceCode(){
		String sourceCode = "";
		String Path = "interation2"; //Change this to match your path
		try {
			File file = new File(Path, "sourceCode.txt");
			Scanner myReader = new Scanner(file);
			while (myReader.hasNextLine()) {
			  sourceCode += myReader.nextLine() + "\n";
			//   System.out.println(data);
			}
			myReader.close();
		  } catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		  }

		// System.out.println("sourceCode: " + sourceCode);
		return sourceCode;
	}

	// Sends the code to the server
	public static void codeRequest(Socket clientSocket) throws IOException {
		System.out.println(teamName + " - Received get code request");

		//Put the source code into sourceCode in order to send to the server
		String sourceCode = readSourceCode();
		String end = "\n...\n";
		String codeResponse = "Java\n";
		String toServer = codeResponse + sourceCode + end;

		System.out.println(teamName + " - about to send source code ");
		sendToServer(toServer, clientSocket);
		System.out.println(teamName + " - Finished request");
	}
	
	// Gets a list of peers and return it in string format
	public static String arrayToString(ArrayList<String> peersArray){
		String peers = "";
		for (int i = 0; i < peersArray.size();i++) {
			if (i == 0){ //Don't add \n to the first entry
				peers += peersArray.get(i); 
			}
			else{ //Add to the other entries
				peers += "\n" + peersArray.get(i);
			}
		}
		return peers;
	}

	// Returns the number of UDP
	public static int countLines(String str){
		if (str != null){
			String [] lines = str.split("\r\n|\r|\n");
			return lines.length;
		}
		else
			return 0;
	}

	public static String  removeLastLine(String str){
		if(str.lastIndexOf("\n")>0) {
			return str.substring(0, str.lastIndexOf("\n"));
		} else {
			return str;
		}
	}

	// Get the report 
	public static String getReport(){
		// Prepare variables
		int totalPeersFromRegistry = peerListRegistry.size();
		String peersFromRegistry = arrayToString(peerListRegistry);
		Date aDate = new Date();
		String reportDateReceived = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(aDate);
		int totalCurrentPeers = currentPeerList.size(); 
		String currentPeerListString = arrayToString(currentPeerList);
		int numberOfPeersReceived = countLines(peersReceived);
		int numberOfPeersSent = countLines(peersSent);
		int numberOfSnippets = countLines(snippets);
		//snippets = removeLastLine(snippets);

		// Format report
		String report = 
		totalCurrentPeers + "\n" +
		currentPeerListString + "\n" +
		numberOfSources + "\n" + 
		sourceLocation + "\n" +   
		reportDateReceived + "\n" +
		totalPeersFromRegistry + "\n" + 
		peersFromRegistry + "\n" + 
		numberOfPeersReceived + "\n" +
		peersReceived +
		numberOfPeersSent + "\n" +
		peersSent +
		numberOfSnippets + "\n" +
		snippets;

		//System.out.println("start of report \n" + report + "end of report");
		return report;
	}

	//Gets report about peers and sends it to the server
	public static void reportRequest(Socket clientSocket) throws IOException {
		System.out.println(teamName + " - Received get report request");

		System.out.println(teamName + " - about to send report");
		String toServer = getReport();
		sendToServer(toServer, clientSocket);
		// System.out.println(" - toServer\n" + toServer);
		System.out.println(teamName + " - Finished request");
	}
	
	// Store peers 
	public static void storePeers(int num, BufferedReader reader) throws NumberFormatException, IOException {
		long time = new Timestamp(System.currentTimeMillis()).getTime();
		
		// String peers = "";
		for(int i = 0; i < num; i ++) {
			String peerInList = reader.readLine();		
			if(!currentPeerList.contains(peerInList)) {
				currentPeerList.add(peerInList);
				locationAndTime.put(peerInList.replace("/", ""), time/1000);
			}else {
				totalPeers -= 1;
			}
			peer.addActivePeer(peerInList);
		}
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

		peerListRegistry = new ArrayList<String>(currentPeerList);
		System.out.println(teamName + " - Finished request");
	}

	// Sends the team name to the server
	public static void locationRequest(Socket clientSocket) throws IOException {
		System.out.println(teamName + " - Received get location request");

		String toServer = InetAddress.getLocalHost().toString().split("/")[1]+":" +peer.getPort() + "\n";

		// teamLocation = peer.getLocation();
		// System.out.println("Team location: " + teamLocation);
		System.out.println("I'm at location: " + peer.getMyLocation());
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
	public static void initializeGlobalVariables() throws SocketException{
		peersReceived = "";
		peersSent = "";
		teamLocation = "";
		sourceLocation = serverIP + ":" + serverPort;
		numberOfSources = 0;
		currentPeerList = new ArrayList <String>();
		totalPeers = 0;
		peer = new Peer();
		peer.teamName=teamName;
		peer.setUpUDPserver();
		locationAndTime = new ConcurrentHashMap<String,Long>();
		executor = Executors.newSingleThreadExecutor();
		executor1 = Executors.newSingleThreadExecutor();
		executor2 = Executors.newSingleThreadExecutor();
		missingAckPeers=new ArrayList<String>();
		silentPeers=new ArrayList<String> ();
		nextSnipTimestamp = 0;
		
	}

	// Check if the time difference between current time and recorded time is greater than 240 seconds
	public static Boolean checkTimeLimit(long currentTime,long time) {
		return (currentTime-time) > inactiveTimeLimit;
	}
	
	// Store info about a message when we send a peer and format it properly
	public static void addToPeersSent() throws UnknownHostException {
		ArrayList<String> activePeerList = peer.activePeerList;

		for(int i = 0; i < activePeerList.size(); i++) {
			String toPeer = peer.getPeerLocation(activePeerList.get(i));
			String fromPeer = peer.getMyLocation(); 
			Date aDate = new Date();
			String dateSent = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(aDate);
			peersSent += toPeer + " " +  fromPeer + " " +  dateSent + "\n";
		}
	}

	// Store info about a message when a peer is received and format it properly
	public static void addToPeersReceived(String aPeer) throws UnknownHostException {
		String fromPeer = peer.getPeerLocation(aPeer); 
		String toPeer = peer.getMyLocation();
		Date aDate = new Date();
		String dateReceived = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(aDate);

		peersReceived += fromPeer + " " +  toPeer + " " +  dateReceived + "\n";
	}

	// Handle the logic for a when peer receives a message
	public static void receiveMessage()  {
		try {
			String newMessage=peer.getMessage();
			if(newMessage!=null) {
				if(newMessage.startsWith("peer")) {
					String newPeer = newMessage.replace("peer", "");
					snippets = peer.snips;
					nextSnipTimestamp = peer.nextTimeStamp;
					if(newPeer != null) {
						addToPeersReceived(newPeer);
						if(!currentPeerList.contains(newPeer)) {
							currentPeerList.add(newPeer);
						}
						long time = new Timestamp(System.currentTimeMillis()).getTime()/1000;
						locationAndTime.put(newPeer,time);
					}
				}

			}
			
			
		} catch (SocketTimeoutException e) {
			// TODO Auto-generated catch block
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// A thread to collaborate with other peers and send peer information to them
	static Thread collaborationThread = new Thread (new Runnable() {
		public void run() {
			while(!peer.stop) {
				try {
					peer.sendPeer();
					addToPeersSent();
					Thread.sleep(60000);
				} catch (InterruptedException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	
	});

	// Get input from user and format it. If input has more than 25 characters, get the first 25 characters
	static Thread getSnipThread = new Thread(new Runnable() {
		@Override
		public void run() {
			Scanner keyboard = new Scanner(System.in);
			while(!peer.stop) {		
				String input = keyboard.nextLine();
				if(input.length() > maxSnipLength) {
					input = input.substring(0, maxSnipLength);
				}
				try {
					
					nextSnipTimestamp=peer.nextTimeStamp;
					String snip = nextSnipTimestamp +" "+ input + " ";
					
					if(snippets != null) {
						snippets += snip ;
					}else {
						snippets = snip ;
					}
					latestSnip="snip"+ snip;
					ArrayList<String> list=peer.activePeerList;
					(new Thread(deleteInactivePeer)).start();
					Thread.currentThread().sleep(10);
					//System.out.println("sending "+latestSnip);
					peer.sendInfo(latestSnip,list);
					
		
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			keyboard.close();
		}
	});
	
	/*
	 * Threadlocal allows a thread to create a local variable for the thread.
	 * a thread could only get and set it's own local variable.
	 * threadlocal.get() will return the variable, threadlocal.set() will set the variable
	 * we stored the snippet as the local variable, such that we can working on different snippet
	 * the name of the snippet is the timestamp of the snippet. such that we do not have to create one more hashmap
	 * 
	 * thread will sleep for 10 seconds , and then send the snippets to the peers that do not send ack message
	 * 
	 */
	static Runnable deleteInactivePeer = ()-> {
			myThreadLocal.set(latestSnip);
			Thread.currentThread().setName(String.valueOf(nextSnipTimestamp));
			int count=0;
			ArrayList<String> activeList = null;
			try {
				while(!Thread.currentThread().isInterrupted()&&!peer.stop&&count<3) {
					Thread.sleep(10000);
					try {
						activeList=peer.responseToSnip.get(Integer.parseInt(Thread.currentThread().getName()));
						for(String aPeer:peer.activePeerList) {
							if(activeList!=null&&!activeList.contains(aPeer)) {
								InetAddress ip=InetAddress.getByName(aPeer.split(":")[0]);
								int port=Integer.parseInt(aPeer.split(":")[1]);
								System.out.println("Round "+count+" try to send snippet to "+aPeer);
								peer.sendMessage(myThreadLocal.get(), ip, port);
							}
						}
						
				} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println("System shut down");
				}
					count++;
				}
				activeList=peer.responseToSnip.get(Integer.parseInt(Thread.currentThread().getName()));
				for(String aPeer:peer.activePeerList) {
					if(activeList!=null&&!activeList.contains(aPeer)) {
						System.out.println(aPeer+" did not sent ack,it has been removed");
						missingAckPeers.add(aPeer);
						locationAndTime.remove(aPeer);
					}
				}
				peer.removePeerFromActivePeerList(missingAckPeers);
				
				
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.out.println("Thread is interupted");
			} 
		
	};
	
	// Check if a peer is active or not. If a peer does not send their peer info for more than 4 mins, then remove it from the active peer list
	static Thread checkActivePeerThread = new Thread(new Runnable() {
		@Override
		public void run() {
			while(!peer.stop) {
				long currentTime = new Timestamp(System.currentTimeMillis()).getTime()/1000;
				for(Map.Entry<String, Long> entry : locationAndTime.entrySet()) {
				    String location = entry.getKey();
				    long time = entry.getValue();
				    if(checkTimeLimit(currentTime,time)) {
				    	System.out.println(location + "    disconnected" );
				    	peer.removePeerFromActivePeerList(location);
				    	silentPeers.add(location);
				    	locationAndTime.remove(location);
				    }
				}
			}
		}
	});

    public static void main(String[] args) throws IOException, InterruptedException{
		// Initalize variables
		readCommandLineArguements(args);
		initializeGlobalVariables();

		// Process requests after starting up
		connectClient(serverIP,serverPort);
		processRequests();

		// Start making peers send UDP messages
		System.out.println(teamName + " - Finished with registry, starting collaboration");
		executor1.execute(collaborationThread);
		executor.execute(getSnipThread);
		executor2.execute(checkActivePeerThread);

		while(!peer.stop) {
			receiveMessage();
		}

		// Process requests after shutting down
		connectClient(serverIP, serverPort);
		processRequests(); 
		System.exit(1);
    }
}

