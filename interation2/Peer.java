// Peer.java

package interation2;
import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/*
This class is the Peer class which holds peer information. It is used in the client class.

Functions:
	-setUpUDPserver: Setups a UDP socket and store the port number of the server
	-receiveMessage: Receives the message from the server
	-getMyLocation: Get the location of our peer
	-getPeerLocation: Get the location of the peer
	-getPort: Get the port number of the server
	-getMyAddress: Get the IP address of the server
	-sendToServer: Sends a message to the server
	-addActivePeer: If this peer is a new peer, and there is a snippet history, send this peer with the history of snippets
	-getCatchUpMessages: When a peer connects, it will recieve all the snippets that have been sent since the server started
	-setActivePeerList: Set active peer list
	-setStop: If we receive a stop, then close the UDP connection
	-sendToPeers: Send info to all the other peers
	-getMessage: When a peer recieves a UDP message, act accordingly based on if it is a snip, stop, or peer message
	-removePeerFromActivePeerList: Remove a peer from the active peer list
	-randomPeer: Select a random peer from the active peer list
	-removePeerFromActivePeerList: Remove peer(s) from the active peer list
	-handleCatchUpMessage: Handle the catch up message. Put the timestamp and snippet and sender into the hashmap.
	-getSnipsFromHashmap: Read the snippet from the hashmap, in the order of timestamp
	-sendPeerInfo: Send peer info to another peer
	-checkTimeLimit: Check that the time limit has not be exceeded for a peer to be considered inactive
	-checkSilentPeer: Check for peers to label them as silent if needed
	-keepSendingSnippet: Send a snippet multiple times to a peer
	-handleMissingAckPeer: Check if a peer did not send an ack. If it didn't, then add it to the missing ack peers list
*/

public class Peer {
	public byte[] inbuf = new byte[10000];
	public byte[] outbuf = new byte[10000];
	public volatile ArrayList<String> activePeerList = new ArrayList<String>();
	public DatagramPacket UDPinPacket = new DatagramPacket(inbuf, inbuf.length);
	public DatagramPacket UDPoutPacket;
	public DatagramSocket UDPserver;
	public int UDPport;
	public Boolean stop = false;
	String snips;
	String message;
	String teamName;
	int nextTimeStamp;
	int numberofAck = 0;
	String ackMessage = "";
	ConcurrentHashMap<Integer, ArrayList<String>> responseToSnip = new ConcurrentHashMap<Integer, ArrayList<String>>();
	HashMap<Integer, String> TimestampAndSnippet = new HashMap<Integer, String>();

	// A hashmap that stores location and time. Location is the other peer's location
	// Time is the timestamp (in seconds) that receives the peer info from this peer's location
	public static ConcurrentHashMap<String, Long> locationAndTime = new ConcurrentHashMap<String, Long>();
	public static final int inactiveTimeLimit = 240;

	// Contains all the peers that does not respond (ack message) to snippet
	public ArrayList<String> missingAckPeers = new ArrayList<String>();

	// Contaisn all the peers that does not send their peer message
	public ArrayList<String> silentPeers = new ArrayList<String>();
	
	// Setups a UDP socket and store the port number of the server
	public void setUpUDPserver() throws SocketException {
		UDPserver = new DatagramSocket();
		UDPport = UDPserver.getLocalPort();
	}
	
	// Receives the message from the server
	public String receiveMessage() throws IOException {
		UDPserver.receive(UDPinPacket);
		String message = new String(UDPinPacket.getData(), 0, UDPinPacket.getLength());
		return message;
	}

	// Get the location of our peer
	public String getMyLocation() throws UnknownHostException {
		String IP = String.valueOf(getMyAddress());
		IP = IP.replace("/", ""); //Remove the / at the beginning
		String port = String.valueOf(UDPport);
		String location = IP + ":" + port;
		return location;
	}

	// Get the location of the peer
	public String getPeerLocation() {
		String address = UDPinPacket.getAddress().toString().replace("/","");
		int port = UDPinPacket.getPort();
		String location = address + ":" + port;
		return location;
	}
	
	// Get the port number of the server
	public int getPort() {
		return UDPport;
	}
	
	// Get the IP address of the server
	public InetAddress getMyAddress() throws UnknownHostException {
		return InetAddress.getByName(InetAddress.getLocalHost().toString().split("/")[1]);
	}
	
	// Sends a message to the server
	public void sendToServer(String message, InetAddress IP, int port) throws IOException {
		outbuf = message.getBytes();
		UDPoutPacket = new DatagramPacket(outbuf, outbuf.length, IP, port);
		UDPserver.send(UDPoutPacket);
	}
	
	// If this peer is a new peer, and there is a snippet history, send this peer with the history of snippets
	public void addActivePeer(String peer) throws IOException {
		peer = peer.replace("/", "");
		
		if(!activePeerList.contains(peer)) {
			activePeerList.add(peer);
			if(snips != null && peer != getMyLocation()) {
				InetAddress ip = InetAddress.getByName(peer.split(":")[0]);
				int port = Integer.parseInt(peer.split(":")[1]);
				System.out.println(getCatchUpMessages());
				String[] catcuUpMessagesArray = getCatchUpMessages().split("\n");

				// Send each line, one at a time
				for(String line:catcuUpMessagesArray) {
					sendToServer(line, ip, port);
				}
			}
		}	
	}
	
	// When a peer connects, it will recieve all the snippets that have been sent since the server started
	public String getCatchUpMessages() throws UnknownHostException {
		String mes = "";
		String[] snippetArray = snips.split("\n");

		for(String line:snippetArray) {
			mes += "ctch";
			String[] temp = line.split(" ");
			int timestamp = Integer.parseInt(temp[0]);
			String originalSender = temp[temp.length-1];
			String content = line.replace(originalSender, "");
			content = content.replace(timestamp + " ", "");
			mes += originalSender + " ";
			mes += timestamp + " ";
			mes += content + "\n";
		}
		return mes;
	}
	
	// Set active peer list
	public void setActivePeerList(ArrayList<String> newPeerList) {
		activePeerList = newPeerList;
	}
	
	// If we receive a stop, then close the UDP connection
	public void setStop() {
		System.out.println("stop UDP");
		stop = true;
		UDPserver.close();
	}

	// Send info to all the other peers
	public void sendToPeers(String message, ArrayList<String> list) throws IOException {
		for(int i = 0; i < list.size(); i++) {
			InetAddress ip = InetAddress.getByName(list.get(i).split(":")[0]);
			int port = Integer.parseInt(list.get(i).split(":")[1]);

			sendToServer(message, ip, port);
		}
	}
	
	// When a peer recieves a UDP message, act accordingly based on if it is a snip, stop, or peer message
	public String getMessage() throws IOException {
			message = receiveMessage();

			if(message.startsWith("stop")) {
				sendToPeers("stop", activePeerList);

				InetAddress ip = InetAddress.getByName(getPeerLocation().split(":")[0]);
				int port = Integer.parseInt(getPeerLocation().split(":")[1]);
				String response = "ack"+teamName;

				sendToServer(response, ip, port);
				setStop();
				System.out.println("send  " + response + "  to "+ getPeerLocation());
			}
			else if(message.startsWith("snip")) {
				String newsnip = message.replace("snip", "");
				String[] snipArray = newsnip.split("\n");
				int receivedTimestamp = Integer.parseInt(snipArray[snipArray.length-1].split(" ")[0]);
				String sender = getPeerLocation();
				String[] snippetArray = snipArray[snipArray.length-1].split(" ");
				String content = "";

				for(int i = 1;i<snippetArray.length; i++) {
					content += snippetArray[i] + " ";
				}
				if(!TimestampAndSnippet.containsKey(receivedTimestamp)) {
					nextTimeStamp = receivedTimestamp +1;
					TimestampAndSnippet.put(receivedTimestamp, content + " " + sender);
					snips = getSnipsFromHashmap();
					System.out.print(snips);
				}
				sendToServer("ack " + receivedTimestamp, InetAddress.getByName(getPeerLocation().split(":")[0]), Integer.parseInt(getPeerLocation().split(":")[1]));
			}
			else if(message.startsWith("peer")){
				String peer = message.replace("peer", "");

				addActivePeer(getPeerLocation());
				addActivePeer(peer);
				System.out.println("Peer added   " + peer);
				return message;
			}
			/* If we received an ack, we will update the hashmap (responseToSnip)
			 * such that we can detect which peer did not send an ack to us
			 */
			else if(message.startsWith("ack")) {
				int receivedTimestamp = Integer.parseInt(message.split(" ")[1]);
				ackMessage += receivedTimestamp +"  " + getPeerLocation() + "\r\n";
				System.out.println(message +"  " + getPeerLocation());
				numberofAck++;
				ArrayList <String>currentList  = responseToSnip.get(receivedTimestamp);

				if(currentList != null) {
					if(!currentList.contains(getPeerLocation())) {
						currentList.add(getPeerLocation());
						responseToSnip.put(receivedTimestamp, currentList);
					}
				}
				else {
					currentList = new ArrayList<String>();
					currentList.add(getPeerLocation());
					responseToSnip.put(receivedTimestamp, currentList);
				}
			}
			else if(message.startsWith("ctch")) {
				handleCatchUpMessage(message);
			}
			return null;
	}
	
	// Remove a peer from the active peer list	
	public void removePeerFromActivePeerList(String peer) {
		if(activePeerList.contains(peer)) {
			activePeerList.remove(activePeerList.indexOf(peer));
		}
		System.out.println("current list" + activePeerList);
	}
	
	// Select a random peer from the active peer list
	public String randomPeer() {
		Random rand = new Random();
        return activePeerList.get(rand.nextInt(activePeerList.size()));
	}

	// Remove peer(s) from the active peer list
	public void removePeerFromActivePeerList(ArrayList<String> inactivePeerList) {
		if(inactivePeerList != null) {
			activePeerList.removeAll(inactivePeerList);
		}
		System.out.println("current list" + activePeerList);
	}
	
	// Handle the catch up message. Put the timestamp and snippet and sender into the hashmap.
	public void handleCatchUpMessage(String message) {
		message = message.replace("ctch", "");
		String originalSender = message.split(" ")[0];
		int receivedTimestamp = Integer.parseInt(message.split(" ")[1]);
		String[] snippetArray = message.split(" ");
		String snippet = "";

		for(int i = 2;i<snippetArray.length; i++) {
			snippet += snippetArray[i] + " ";
		}

		TimestampAndSnippet.put(receivedTimestamp, snippet + " " + originalSender);
		snips = getSnipsFromHashmap();

		if(receivedTimestamp>nextTimeStamp-1) {
			System.out.println(receivedTimestamp + " " + snippet + " " + originalSender);
			nextTimeStamp = receivedTimestamp + 1;
		}
	}
	
	// Read the snippet from the hashmap, in the order of timestamp
	public String getSnipsFromHashmap() {
		String result = "";
		Object[] keys = TimestampAndSnippet.keySet().toArray();
		Arrays.sort(keys);

		for(Object key:keys) {
			result += key + " " + TimestampAndSnippet.get(key) + "\n";
		}
		return result;
	}
	
	// Send peer info to another peer
	public void sendPeerInfo() throws InterruptedException, IOException {
		if(activePeerList.size() > 0) {
			String ip = InetAddress.getByName(InetAddress.getLocalHost().toString().split("/")[1]).toString();
			String message = "peer" + randomPeer();

			sendToPeers(message, activePeerList);
		}
	}
	
	// Check that the time limit has not be exceeded for a peer to be considered inactive
	public static Boolean checkTimeLimit(long currentTime, long time) {
		return (currentTime-time) > inactiveTimeLimit;
	}
	
	// Check for peers to label them as silent if needed
	public void checkSilentPeer() {
		while(!stop) {
			long currentTime = new Timestamp(System.currentTimeMillis()).getTime()/1000;

			for(Map.Entry<String, Long> entry : locationAndTime.entrySet()) {
			    String location = entry.getKey();
			    long time = entry.getValue();

			    if(checkTimeLimit(currentTime, time)) {
			    	System.out.println(location + "    disconnected" );
			    	removePeerFromActivePeerList(location);
			    	silentPeers.add(location);
			    	locationAndTime.remove(location);
			    }
			}
		}
	}

	// Send a snippet multiple times to a peer
	public void keepSendingSnippet(ArrayList<String> responseList, String targetPeer, String snippet, int count) throws IOException {
		if(responseList != null) {
			if(!responseList.contains(targetPeer)) {
				InetAddress ip = InetAddress.getByName(targetPeer.split(":")[0]);
				int port = Integer.parseInt(targetPeer.split(":")[1]);
				System.out.println("Round " + count + " try to send snippet to " + targetPeer);

				sendToServer(snippet, ip, port);
			}
			
		}else {
			System.out.println("Round " + count + " try to send snippet to " + targetPeer);
		}
	}

	// Check if a peer did not send an ack. If it didn't, then add it to the missing ack peers list
	public void handleMissingAckPeer(ArrayList<String> responseList, String aPeer, ArrayList<String> wholeList) {
		if(responseList != null) {
			if(!responseList.contains(aPeer)) {
				System.out.println(aPeer + " did not sent ack, it has been removed");
				missingAckPeers.add(aPeer);
				locationAndTime.remove(aPeer);
			}
		}
		else {
			missingAckPeers.addAll(wholeList);
		}
	}
}