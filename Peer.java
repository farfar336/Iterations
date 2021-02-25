package interation2;
import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Scanner;

public class Peer  {
	public byte[] inbuf = new byte[256];
	public byte[] outbuf = new byte[256];
	public ArrayList<String> peerList;
	public DatagramPacket UDPinPacket=new DatagramPacket(inbuf,inbuf.length);
	public DatagramPacket UDPoutPacket;
	public DatagramSocket UDPserver;
	public int UDPport;
	public Boolean stop=false;
	long startSnip;
	String snips;
	String message;
	
	//this method will setup a UDP socket and store the port number of UDP
	public  void setUpUDPserver() throws SocketException {
		UDPserver=new DatagramSocket();
		UDPserver.setSoTimeout(5000);
		UDPport=UDPserver.getLocalPort();
		
	}
	//receive the message from other UDP
	public  String receiveMessage() throws IOException {
		UDPserver.receive(UDPinPacket);
		return new String(UDPinPacket.getData(),0,UDPinPacket.getLength());
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
	
	public void stop() {
		System.out.println("stop UDP");
		UDPserver.close();
	}
	public void setPeerList(ArrayList<String> newPeerList) {
		peerList=newPeerList;
	}
	public void setStop() {
		Thread.currentThread().interrupt();
		stop=true;
	}
	public void sendInfo(String message) throws IOException {
		for(int i=0;i<peerList.size();i++) {
			InetAddress ip=InetAddress.getByName(peerList.get(i).split(":")[0]);
			int port=Integer.parseInt(peerList.get(i).split(":")[1]);
			sendMessage(message, ip, port);
		}
	}
public void UDPreceiveMessage() throws IOException {
			message=receiveMessage();
			if(message==null) {
				return;
			}
			System.out.println("receive message"+message);
			if(message.equals("stop")) {
				sendInfo("stop");
				setStop();
				stop();
			}
			else if(message.startsWith("snip")) {
				snips=message;
				sendInfo(snips);
				}
			else if(message.startsWith("peer")){
				peerList.add(message.replace("peer", ""));
				setPeerList(peerList);
			}
			message=null;
		
		
	}
	
	public void sendPeer() throws InterruptedException {
			Thread.sleep(10000);
			if(peerList.size()>0) {
				for(int i=0;i<peerList.size();i++) {
					InetAddress ip;
					System.out.println(peerList.get(i));
					try {
						ip = InetAddress.getByName(peerList.get(i).split(":")[0].toString().replace("/", ""));
						int port=Integer.parseInt(peerList.get(i).split(":")[1]);
						String message="peer"+getAddress()+":"+getPort();
						sendMessage(message.replace("/", ""), ip, port);
						System.out.println("send message  "+message +"to"+ip+":"+port);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
			
		
	}
	public void getsnip() throws IOException {
		if(startSnip==0) {
			startSnip=new Timestamp(System.currentTimeMillis()).getTime();
		}
		Scanner keyboard = new Scanner(System.in);
		String command = keyboard.nextLine();
		long currenttime=new Timestamp(System.currentTimeMillis()).getTime();
		long timestamp=currenttime-startSnip;
		String snip="snip"+timestamp+" "+command+" "+getAddress()+":"+getPort();
		if(snips!=null) {
			snips+=snip+"\n";
		}else {
			snips=snip;
		}
		sendInfo(snips);
	}

	public void thread1() throws IOException, InterruptedException  {
		// TODO Auto-generated method stub
		while(!stop) {
			
				try {
					UDPreceiveMessage();
				} catch (SocketTimeoutException e) {
					// TODO Auto-generated catch block
					System.out.println("did not receive message");
				}
			
			if(stop) {
				Thread.currentThread().interrupt();
			}
			sendPeer();
			
			
			
			
		}
		
		
	}
	
	
	
	
	
	
	
}
