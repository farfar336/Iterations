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
	public ArrayList<String> inactivePeerList = new ArrayList<String>();
	public DatagramPacket UDPinPacket = new DatagramPacket(inbuf,inbuf.length);
	public DatagramPacket UDPoutPacket;
	public DatagramSocket UDPserver;
	public int UDPport;
	public Boolean stop = false;
	long startSnip;
	String snips;
	String message;
	String teamName;
	int nextTimeStamp;
	String catchUpSnip="";
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
		final Iterator<String> ite=activePeerList.iterator();
		while(ite.hasNext()) {
			if(peer==ite.next()) {
				ite.remove();
			}
		}
//		if(activePeerList.contains(peer)) {
//			activePeerList.remove(activePeerList.indexOf(peer));
//		}
	}
	public void removePeerFromActivePeerList(ArrayList<String> inactivePeerList) {
		if(inactivePeerList!=null) {
			activePeerList.removeAll(inactivePeerList);
		}
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
		System.out.println(snips);
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
