package interation2;

/*
	To do:
		-Required part: code [Xudong] (done)
			-Look in the iteration 3 pdf, Shutting down system requirements (stop UDP/IP messages)

		-Optional part: code [Xudong] (done)
			-Look in the iteration 3 pdf, User interface and snippets requirements (snip UDP/IP messages)
			-Look in the iteration 3 pdf, Group management requirements (peer UDP/IP messages)

		-Required part: report [Xudong] (done)
			-Look in the iteration 3 pdf, Updated report request (stop UDP/IP messages)

		-Optional part: report [Farrukh] 
			-Look in the iteration 3 pdf, Updated Registry Communication Protocol

		-Fix UDP peer messages in report [Farrukh]
			-Clarify with professor on what is expected (done)
			-Implement changes

		-Class diagram [Farrukh] (done)
			-Can add stuff on to the diagram from iteration 2

		-Code documentation [Farrukh]
			-Update Code documentation
			-Add more comments where needed

		-Video demo (5-10 mins) [Farrukh] (done)
			-A video that gives a brief explanation of your implementation, shows your peer in action, and explains
			under which circumstances a peer would not receive a stop message, even with the resends in
			iteration 3.

		-Submitting our work [Farrukh] 
			-Submit Wednesday March 31 between 12:30pm and 1:30 pm. (done)
			-Submit the required and optional feature on port: 55921 and 12955 respectively
*/

/*
To do:
-In iteration3, check that both output files have the correct content from the server

To run, enter this in command line: java interation2/Client 136.159.5.22 55921 "Xudong Farrukh"

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

	public static Peer peer;
	public static Executor executor,executor1,executor2;

	//threadLocal allow us to create a local variable in the thread.
	public static ThreadLocal<LocalThreadVariable> myThreadLocal = new ThreadLocal<LocalThreadVariable>();

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
		String Path = ""; //Change this to match your path
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
	public static String arrayToStringWithAliveness(ArrayList<String> peersArray){
		String peers = "";
		for (int i = 0; i < peersArray.size();i++) {
			String currentPeer=peersArray.get(i);
			if (i == 0){ //Don't add \n to the first entry
				peers += peersArray.get(i); 
				if(peer.missingAckPeers.contains(currentPeer)) {
					peers+=" "+"missing_ack";
				}
				else if(peer.silentPeers.contains(currentPeer)) {
					peers+=" "+"silent";
				}
				else {
					peers+=" "+"alive";
				}
			}
			else{ //Add to the other entries
				peers += "\n" + peersArray.get(i);
				if(peer.missingAckPeers.contains(currentPeer)) {
					peers+=" "+"missing_ack";
				}
				else if(peer.silentPeers.contains(currentPeer)) {
					peers+=" "+"silent";
				}
				else {
					peers+=" "+"alive";
				}
			}
		}
		return peers;
	}
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
		String ackMessage=peer.ackMessage;
		
		int totalPeersFromRegistry = peerListRegistry.size();
		String peersFromRegistry = arrayToString(peerListRegistry);
		Date aDate = new Date();
		String reportDateReceived = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(aDate);
		int totalCurrentPeers = currentPeerList.size(); 
		String currentPeerListString = arrayToStringWithAliveness(currentPeerList);
		int numberOfPeersReceived = countLines(peersReceived);
		int numberOfPeersSent = countLines(peersSent);
		int numberOfSnippets = countLines(peer.snips);

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
		peer.snips
		+
		peer.numberofAck+"\n"+
		ackMessage
		;
		System.out.println("start of report \n" + report + "end of report");
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
		for(int i = 0; i < num; i ++) {
			String peerInList = reader.readLine();		
			if(!currentPeerList.contains(peerInList)) {
				currentPeerList.add(peerInList);
				peer.locationAndTime.put(peerInList.replace("/", ""), time/1000);
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
		System.out.println("I'm at location: " + peer.getMyLocation());
		sendToServer(toServer, clientSocket);
		System.out.println(teamName + " - Finished request");
	}
	public static void putStringIntoFile(String aString, String fileName) throws FileNotFoundException{
		try (PrintWriter out = new PrintWriter(fileName);) {
			out.println(aString);
		}
	}

	// Recieves the code from the server and puts it into a file
	public static void getCodeFromServer(BufferedReader reader) throws IOException {
		String code = "";
		String line;

		while ((line = reader.readLine()) != null) {
			code += line + "\n";
		}
		putStringIntoFile(code, "codeFromServer.txt");
	}

	// Recieves the report from the server and puts it into a file
	// Puts a string into a file
	public static void getReportFromServer(BufferedReader reader) throws IOException {
		String report = "";
		String line;

		while ((line = reader.readLine()) != null) {
			report += line + "\n";
		}
		putStringIntoFile(report, "reportFromServer.txt");
	}

	// Process requests from the server
	public static void processRequests() throws IOException{
		String request;
		boolean stopRequests = false;
		while (stopRequests == false) {
			request = reader.readLine();
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
			case "close":
				getCodeFromServer(reader);
				getReportFromServer(reader);
				stopRequests = true;
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
		executor = Executors.newSingleThreadExecutor();
		executor1 = Executors.newSingleThreadExecutor();
		executor2 = Executors.newSingleThreadExecutor();
	
		nextSnipTimestamp = 0;	
	}

	
	
	
	// Store info about a message when we send a peer and format it properly
	public static void addToPeersSent() throws UnknownHostException {
		ArrayList<String> activePeerList = peer.activePeerList;
		for(int i = 0; i < activePeerList.size(); i++) {
			String toPeer = activePeerList.get(i);
			String fromPeer = peer.getMyLocation(); 
			Date aDate = new Date();
			String dateSent = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(aDate);
			peersSent += toPeer + " " +  fromPeer + " " +  dateSent + "\n";
		}
	}
	

	// Store info about a message when a peer is received and format it properly
	public static void addToPeersReceived(String receivedPeer,String sourcePeer) throws UnknownHostException {
		Date aDate = new Date();
		String myLocation=peer.getMyLocation();
		String dateReceived = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(aDate);
		peersReceived += receivedPeer + " " +  sourcePeer + " " +  dateReceived + "\n";
	}

	// Handle the logic for a when peer receives a message
	public static void receiveMessage()  {
		String newMessage;
		try {
			newMessage = peer.getMessage();
			if(newMessage!=null) {
					String receivedPeer = newMessage.replace("peer", "");
					String sourcePeer=peer.getPeerLocation();
					snippets = peer.snips;
					nextSnipTimestamp = peer.nextTimeStamp;
					if(receivedPeer != null) {
						addToPeersReceived(receivedPeer,sourcePeer);	
						if(!currentPeerList.contains(sourcePeer)) {
							currentPeerList.add(sourcePeer);
						}
						long time = new Timestamp(System.currentTimeMillis()).getTime()/1000;
						peer.locationAndTime.put(sourcePeer,time);
					}
				
			}
		}
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
}

	// A thread to collaborate with other peers and send peer information to them
	static Thread collaborationThread = new Thread (new Runnable() {
		public void run() {
			while(!peer.stop) {
				try {
					peer.sendPeerInfo();
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
					latestSnip="snip"+ snip;
					ArrayList<String> list=peer.activePeerList;
					(new Thread(deleteInactivePeer)).start();
					//allow thread to store timestamp and snippet into threadlocal
					Thread.currentThread().sleep(10);
					peer.sendInfo(latestSnip,list);
					} 
					catch (IOException | InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			keyboard.close();
		}
	});
	
	/*
	 * Use ThreadConfinement to implement
	 * ThreadLocal allows a thread to create a local variable for the thread.
	 * a thread could only get and set it's own local variable.
	 * threadlocal.get() will return the variable, threadlocal.set() will set the variable
	 * LocalThreadVariable will store the current peers in the system, the timestamp we are working on and the
	 * snippet.
	 * Clone the value of the peerList in the Peer, such that we do not pass by value of reference
	 * 
	 * 
	 * thread will sleep for 10 seconds , and then send the snippets to the peers that do not send ack message
	 * 
	 */
	static Runnable deleteInactivePeer = ()-> {
			LocalThreadVariable variable=new LocalThreadVariable(nextSnipTimestamp,latestSnip,peer.activePeerList);
			myThreadLocal.set(variable);
			int count=0;
			ArrayList<String> responseList = null;
			try {
				while(!Thread.currentThread().isInterrupted()&&!peer.stop&&count<3) {
					Thread.sleep(10000);
					responseList=peer.responseToSnip.get(myThreadLocal.get().getTimestamp());
					for(String aPeer:myThreadLocal.get().getCurrentList()) {
						peer.keepSendingSnippet(responseList,aPeer,myThreadLocal.get().getWorkingSnippet(),count);
					}
					count++;
				}
				responseList=peer.responseToSnip.get(myThreadLocal.get().getTimestamp());
				for(String aPeer:myThreadLocal.get().getCurrentList()) {	
					peer.handleMissingAckPeer(responseList,aPeer,myThreadLocal.get().getCurrentList());
				}
				peer.removePeerFromActivePeerList(peer.missingAckPeers);
			}
			catch (InterruptedException | IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Thread is interupted");
			} 
		
	};
	
	// Check if a peer is active or not. If a peer does not send their peer info for more than 4 mins, then remove it from the active peer list
	static Thread checkActivePeerThread = new Thread(new Runnable() {
		@Override
		public void run() {
			peer.checkSilentPeer();
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