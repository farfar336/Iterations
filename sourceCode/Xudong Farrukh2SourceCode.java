Wed Mar 03 11:02:16 MST 2021
java
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
public class clientSocket{
	public static String teamName="Xudong Farrukh\n";
	public static String codeResponse="Java\n";
	public static int totalPeers=0;
	public static String peers="";
	public static ArrayList<String> peerListRegistry;
	public static String date="";
	public static String command=null;
	public static int numberOfSources=0;
	public static String sourceLocation;
	public static int port;
	public static String ip;
	public static HashMap<String, String> peersHashMap;
	//write the team name to the server.
	public static void getTeamName(Socket clientSocket) throws IOException {
		String toServer=teamName;
		System.out.println(toServer);
		clientSocket.getOutputStream().write(toServer.getBytes());
	}
	//send the code toServer to server.
	public static void codeRequest(Socket clientSocket) throws IOException {
		//put the source code into sourceCode in order to send to the server
		String sourceCode="";
		String end="\n...\n";
		String toServer=codeResponse+sourceCode+end;
		clientSocket.getOutputStream().write(toServer.getBytes());
		System.out.println("get code finished");
	}
	//send the report to server
	public static void getReport(Socket clientSocket) throws IOException {
		peers="";
		for (int i=0;i<peerListRegistry.size();i++) {
			peers+=peerListRegistry.get(i)+"\n";
		}
		String toServer=totalPeers+"\n"+peers+numberOfSources+"\n";;
		for(Map.Entry<String, String> entry : peersHashMap.entrySet()) {
		    String key = entry.getKey().split("\n")[0];
		    Date getDate  =new Date( Long.parseLong(entry.getKey().split("\n")[1]));
		    date=(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(getDate);
		    toServer+=key+"\n"+date+"\n";
		    String value = entry.getValue();
		    toServer+=value.split("\n").length+"\n";
		    toServer+=value;  
		}
		System.out.println(toServer);
		clientSocket.getOutputStream().write(toServer.getBytes());
		System.out.println("get report finished");
	}
	
	//receive and store peers
	public static void receivePeers(BufferedReader reader) throws NumberFormatException, IOException {
		long time=new Timestamp(System.currentTimeMillis()).getTime();
		System.out.println(date);
		int num=Integer.parseInt(reader.readLine());
		if(num>0) {
			numberOfSources+=1;
		}
		totalPeers+=num;
		System.out.println("num of peers :"+num+"\n");
		if(num!=0) {
			peers="";
			for(int i=0;i<num;i++) {
				String peer=reader.readLine();		
				if(!peerListRegistry.contains(peer)) {
					peerListRegistry.add(peer);
				}else {
					totalPeers-=1;
				}
				peers+=peer;
				peers+="\n";
			}
			String key=sourceLocation+"\n"+time;
			String value=peers;
			peersHashMap.put(key,value);
		}
		System.out.println("receive peers finished");
	}
    public static void main(String[] args) throws IOException{
    	ip=args[0];
    	port=Integer.parseInt(args[1]);
    	sourceLocation=ip+":"+port;
        Socket clientSocket=new Socket(ip,port);
        peerListRegistry =new ArrayList<String>();
        peersHashMap=new HashMap<String, String>();
        BufferedReader reader=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        while (!(command=reader.readLine()).startsWith("close")) {
        	System.out.println(command);
        	switch(command) {
        	case "get team name":
        		getTeamName(clientSocket);
        		break;
        	case "get code":
        		codeRequest(clientSocket);
        		break;
        	case "receive peers":	
        		receivePeers(reader);
        		break;
        	case "get report":
        		getReport(clientSocket);	
        		break;
        	}

        }
        clientSocket.close();
    }
}
