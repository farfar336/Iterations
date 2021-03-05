package interation2;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

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
	public ArrayList<String> activePeerList = new ArrayList<String>();
	public DatagramPacket UDPinPacket = new DatagramPacket(inbuf,inbuf.length);
	public DatagramPacket UDPoutPacket;
	public DatagramSocket UDPserver;
	public int UDPport;
	public Boolean stop = false;
	long startSnip;
	String snips;
	String message;
	int nextTimeStamp;
	
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

	// Gets the location of the peer
	// public String getLocation() {
	// 	String address = UDPinPacket.getAddress().toString();
	// 	int port = UDPinPacket.getPort();
	// 	String location = address + ":" + port;
	// 	UDPinPacket = new DatagramPacket(inbuf,inbuf.length);
	// 	return location;
	// }
	
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
			if(snips!=null) {
				InetAddress ip=InetAddress.getByName(peer.split(":")[0]);
				int port=Integer.parseInt(peer.split(":")[1]);
				sendMessage("historyOfSnippets "+snips,ip,port);
			}
		}	
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

	// Send info to other server
	public void sendInfo(String message) throws IOException {
		for(int i = 0; i < activePeerList.size(); i++) {
			InetAddress ip = InetAddress.getByName(activePeerList.get(i).split(":")[0]);
			int port = Integer.parseInt(activePeerList.get(i).split(":")[1]);
			sendMessage(message, ip, port);
		}
	}
	
	// When a peer recieves a UDP, act accordingly based on if it is a snip, stop, or peer message
	public String getMessage() throws IOException {
			message=receiveMessage();
			if(message.equals("stop")) {
				sendInfo("stop");
				setStop();
				System.out.println("receive stop message");
			}
			else if(message.startsWith("snip")) {
				snips=message.replace("snip ", "");
				if(!snips.isEmpty()) {
					System.out.println(snips);
					String[] snipArray=snips.split("\n");
					nextTimeStamp=Integer.parseInt(snipArray[snipArray.length-1].split(" ")[0])+1;
				}

			}
			else if(message.startsWith("peer")){
				String peer=message.replace("peer", "");
				addActivePeer(peer);
				System.out.println("Peer added   "+peer);
				return peer;
			}
			else if(message.startsWith("historyOfSnippets ")) {
				snips=message.replace("historyOfSnippets ", "");
				if(!snips.isEmpty()) {
					
					String[] snipArray=snips.split("\n");
					nextTimeStamp=Integer.parseInt(snipArray[snipArray.length-1].split(" ")[0])+1;
				}
			}
			return null;
	}
	// Remove a peer from active peer list
	public void removePeerFromActivePeerList(String peer) {
		if(activePeerList.contains(peer)) {
			activePeerList.remove(activePeerList.indexOf(peer));
		}
	}
	
	// Send peer information
	public void sendPeer() throws InterruptedException, IOException {
		if(activePeerList.size() > 0) {
			String ip = InetAddress.getByName(InetAddress.getLocalHost().toString().split("/")[1]).toString();
			String message = "peer" + ip.replace("/", "") + ":" + getPort();
			sendInfo(message);
		}
	}
}
