package interation2;
import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;


public class Peer  {
	public byte[] inbuf = new byte[256];
	public byte[] outbuf = new byte[256];
	public ArrayList<String> activePeerList=new ArrayList<String>();
	public DatagramPacket UDPinPacket=new DatagramPacket(inbuf,inbuf.length);
	public DatagramPacket UDPoutPacket;
	public DatagramSocket UDPserver;
	public int UDPport;
	public Boolean stop=false;
	long startSnip;
	String snips;
	String message;
	int nextTimeStamp;
	
	
	//this method will setup a UDP socket and store the port number of UDP
	public  void setUpUDPserver() throws SocketException {
		UDPserver=new DatagramSocket();
		UDPserver.setSoTimeout(5000);
		UDPport=UDPserver.getLocalPort();
	}
	//receive the message from other UDP
	public  String receiveMessage() throws IOException {
		UDPserver.receive(UDPinPacket);
		String message=new String(UDPinPacket.getData(),0,UDPinPacket.getLength());
		return message;
	}
	public String getLocation() {
		String address=UDPinPacket.getAddress().toString();
		int port=UDPinPacket.getPort();
		String location=address+":"+port;
		UDPinPacket=new DatagramPacket(inbuf,inbuf.length);
		return location;
	}
	
	//get the port number of this UDP server
	public  int getPort() {
		return UDPport;
	}
	
	//get the IP address of the UDP server
	public  InetAddress getAddress() throws UnknownHostException {
		//return InetAddress.getByName("localhost");
		return InetAddress.getByName(InetAddress.getLocalHost().toString().split("/")[1]);
	}
	
	//send message to other UDP server
	public  void sendMessage(String message,InetAddress IP, int port) throws IOException {
		outbuf=message.getBytes();
		UDPoutPacket=new DatagramPacket(outbuf,outbuf.length,IP,port);
		UDPserver.send(UDPoutPacket);
	}
	
	//add peer to active peerlist
	public void addActivePeer(String peer) {
		peer=peer.replace("/", "");
		if(!activePeerList.contains(peer)) {
			activePeerList.add(peer);
		}	
	}
	
	//set acctive peer list
	public void setActivePeerList(ArrayList<String> newPeerList) {
		activePeerList=newPeerList;
	}
	
	//if we receive a stop. close the UDP
	public void setStop() {
		System.out.println("stop UDP");
		stop=true;
		UDPserver.close();
	}
	
	//send info to other server
	public void sendInfo(String message) throws IOException {
		
		for(int i=0;i<activePeerList.size();i++) {
			InetAddress ip=InetAddress.getByName(activePeerList.get(i).split(":")[0]);
			int port=Integer.parseInt(activePeerList.get(i).split(":")[1]);
			sendMessage(message, ip, port);
		}
	}
	
	//receive message , if there is any new peer, return the peer
public String getMessage() throws IOException {
			message=receiveMessage();
			if(message.equals("stop")) {
				sendInfo("stop");
				setStop();
				System.out.println("receive stop message");
			}
			else if(message.startsWith("snip")) {
				snips=message.replace("snip ", "");
				System.out.println(snips);
				String[] snipArray=snips.split("\n");
				nextTimeStamp=Integer.parseInt(snipArray[snipArray.length-1].split(" ")[0])+1;
				
				}
			else if(message.startsWith("peer")){
				String peer=message.replace("peer", "");
				addActivePeer(peer);
				System.out.println("Peer added   "+peer);
				return peer;
			}
			return null;
	}
	//remove a peer from active peer list
	public void removePeerFromActivePeerList(String peer) {
		if(activePeerList.contains(peer)) {
			activePeerList.remove(activePeerList.indexOf(peer));
		}
		
	}
	
	//send peer information
	public void sendPeer() throws InterruptedException, IOException {
			if(activePeerList.size()>0) {
				String ip=InetAddress.getByName(InetAddress.getLocalHost().toString().split("/")[1]).toString();
				String message="peer"+ip.replace("/", "")+":"+getPort();
				sendInfo(message);
			}
	}


	
	
	
	
	
	
	
}
