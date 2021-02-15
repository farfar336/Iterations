import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class client{
	public static String teamName="Xudong Farrukh\n";
	public static String codeResponse="Java\n";
	public static int numOfPeers=0;
	public static String peers="";
	public static ArrayList<String> peerList;
	public static String date="";
	public static String command=null;
	public static int numOfSource=0;
	public static String sourceLocation;
	public static int port;
	public static String ip;
	public static HashMap<String, String> hashmap;

	//write the team name to the server.
	public static void getTeamName(Socket client) throws IOException {
		String response=teamName;
		System.out.println(response);
		client.getOutputStream().write(response.getBytes());
	}

	//send the code response to server.
	public static void codeRequest(Socket client) throws IOException {
		//put the source code into sourceCode in order to send to the server
		String sourceCode="import java.io.*;\r\n" + 
				"import java.net.Socket;\r\n" + 
				"import java.net.UnknownHostException;\r\n" + 
				"import java.sql.Timestamp;\r\n" + 
				"import java.text.SimpleDateFormat;\r\n" + 
				"import java.util.ArrayList;\r\n" + 
				"import java.util.Date;\r\n" + 
				"import java.util.HashMap;\r\n" + 
				"import java.util.Map;\r\n" + 
				"public class client{\r\n" + 
				"	public static String teamName=\"Xudong Farrukh\\n\";\r\n" + 
				"	public static String codeResponse=\"Java\\n\";\r\n" + 
				"	public static int numOfPeers=0;\r\n" + 
				"	public static String peers=\"\";\r\n" + 
				"	public static ArrayList<String> peerList;\r\n" + 
				"	public static String date=\"\";\r\n" + 
				"	public static String command=null;\r\n" + 
				"	public static int numOfSource=0;\r\n" + 
				"	public static String sourceLocation;\r\n" + 
				"	public static int port;\r\n" + 
				"	public static String ip;\r\n" + 
				"	public static HashMap<String, String> hashmap;\r\n" + 
				"	//write the team name to the server.\r\n" + 
				"	public static void getTeamName(Socket client) throws IOException {\r\n" + 
				"		String response=teamName;\r\n" + 
				"		System.out.println(response);\r\n" + 
				"		client.getOutputStream().write(response.getBytes());\r\n" + 
				"	}\r\n" + 
				"	//send the code response to server.\r\n" + 
				"	public static void codeRequest(Socket client) throws IOException {\r\n" + 
				"		//put the source code into sourceCode in order to send to the server\r\n" + 
				"		String sourceCode=\"\";\r\n" + 
				"		String end=\"\\n...\\n\";\r\n" + 
				"		String response=codeResponse+sourceCode+end;\r\n" + 
				"		client.getOutputStream().write(response.getBytes());\r\n" + 
				"		System.out.println(\"get code finished\");\r\n" + 
				"	}\r\n" + 
				"	//send the report to server\r\n" + 
				"	public static void getReport(Socket client) throws IOException {\r\n" + 
				"		peers=\"\";\r\n" + 
				"		for (int i=0;i<peerList.size();i++) {\r\n" + 
				"			peers+=peerList.get(i)+\"\\n\";\r\n" + 
				"		}\r\n" + 
				"		String response=numOfPeers+\"\\n\"+peers+numOfSource+\"\\n\";;\r\n" + 
				"		for(Map.Entry<String, String> entry : hashmap.entrySet()) {\r\n" + 
				"		    String key = entry.getKey().split(\"\\n\")[0];\r\n" + 
				"		    Date getDate  =new Date( Long.parseLong(entry.getKey().split(\"\\n\")[1]));\r\n" + 
				"		    date=(new SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss\")).format(getDate);\r\n" + 
				"		    response+=key+\"\\n\"+date+\"\\n\";\r\n" + 
				"		    String value = entry.getValue();\r\n" + 
				"		    response+=value.split(\"\\n\").length+\"\\n\";\r\n" + 
				"		    response+=value;  \r\n" + 
				"		}\r\n" + 
				"		System.out.println(response);\r\n" + 
				"		client.getOutputStream().write(response.getBytes());\r\n" + 
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
				"		numOfPeers+=num;\r\n" + 
				"		System.out.println(\"num of peers :\"+num+\"\\n\");\r\n" + 
				"		if(num!=0) {\r\n" + 
				"			peers=\"\";\r\n" + 
				"			for(int i=0;i<num;i++) {\r\n" + 
				"				String peer=reader.readLine();		\r\n" + 
				"				if(!peerList.contains(peer)) {\r\n" + 
				"					peerList.add(peer);\r\n" + 
				"				}else {\r\n" + 
				"					numOfPeers-=1;\r\n" + 
				"				}\r\n" + 
				"				peers+=peer;\r\n" + 
				"				peers+=\"\\n\";\r\n" + 
				"			}\r\n" + 
				"			String key=sourceLocation+\"\\n\"+time;\r\n" + 
				"			String value=peers;\r\n" + 
				"			hashmap.put(key,value);\r\n" + 
				"		}\r\n" + 
				"		System.out.println(\"receive peers finished\");\r\n" + 
				"	}\r\n" + 
				"    public static void main(String[] args) throws IOException{\r\n" + 
				"    	ip=args[0];\r\n" + 
				"    	port=Integer.parseInt(args[1]);\r\n" + 
				"    	sourceLocation=ip+\":\"+port;\r\n" + 
				"        Socket client=new Socket(ip,port);\r\n" + 
				"        peerList =new ArrayList<String>();\r\n" + 
				"        hashmap=new HashMap<String, String>();\r\n" + 
				"        BufferedReader reader=new BufferedReader(new InputStreamReader(client.getInputStream()));\r\n" + 
				"        while (!(command=reader.readLine()).startsWith(\"close\")) {\r\n" + 
				"        	System.out.println(command);\r\n" + 
				"        	switch(command) {\r\n" + 
				"        	case \"get team name\":\r\n" + 
				"        		getTeamName(client);\r\n" + 
				"        		break;\r\n" + 
				"        	case \"get code\":\r\n" + 
				"        		codeRequest(client);\r\n" + 
				"        		break;\r\n" + 
				"        	case \"receive peers\":	\r\n" + 
				"        		receivePeers(reader);\r\n" + 
				"        		break;\r\n" + 
				"        	case \"get report\":\r\n" + 
				"        		getReport(client);	\r\n" + 
				"        		break;\r\n" + 
				"        	}\r\n" + 
				"\r\n" + 
				"        }\r\n" + 
				"        client.close();\r\n" + 
				"    }\r\n" + 
				"}";
		String end="\n...\n";
		String response=codeResponse+sourceCode+end;
		System.out.println(response);
		client.getOutputStream().write(response.getBytes());
		System.out.println("get code finished");
	}
	
	//send the report to server
	public static void getReport(Socket client) throws IOException {
		peers="";
		for (int i=0;i<peerList.size();i++) {
			peers+=peerList.get(i)+"\n";
		}
		String response="Number of peers:" + numOfPeers+"\n"+peers+numOfSource+"\n";;
		for(Map.Entry<String, String> entry : hashmap.entrySet()) {
		    String key = entry.getKey().split("\n")[0];
		    Date getDate  =new Date( Long.parseLong(entry.getKey().split("\n")[1]));
		    date=(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(getDate);
		    response+=key+"\n"+date+"\n";
		    String value = entry.getValue();
		    response+=value.split("\n").length+"\n";
		    response+=value;  
		}
		System.out.println(response);
		client.getOutputStream().write(response.getBytes());
		System.out.println("get report finished");
	}
	
	//receive and store peers
	public static void receivePeers(BufferedReader reader) throws NumberFormatException, IOException {
		long time=new Timestamp(System.currentTimeMillis()).getTime();
		System.out.println(date);
		int num=Integer.parseInt(reader.readLine());
		if(num>0) {
			numOfSource+=1;
		}
		numOfPeers+=num;
		System.out.println("num of peers :"+num+"\n");
		if(num!=0) {
			peers="";
			for(int i=0;i<num;i++) {
				String peer=reader.readLine();		
				if(!peerList.contains(peer)) {
					peerList.add(peer);
				}else {
					numOfPeers-=1;
				}
				peers+=peer;
				peers+="\n";
			}
			String key=sourceLocation+"\n"+time;
			String value=peers;
			hashmap.put(key,value);
		}
		System.out.println("receive peers finished");
	}
    public static void main(String[] args) throws IOException{
    	ip=args[0];
    	port=Integer.parseInt(args[1]);
    	sourceLocation=ip+":"+port;
        Socket client=new Socket(ip,port);
        peerList =new ArrayList<String>();
        hashmap=new HashMap<String, String>();
        BufferedReader reader=new BufferedReader(new InputStreamReader(client.getInputStream()));
        while (!(command=reader.readLine()).startsWith("close")) {
        	System.out.println(command);
        	switch(command) {
        	case "get team name":
        		getTeamName(client);
        		break;
        	case "get code":
        		codeRequest(client);
        		break;
        	case "receive peers":	
        		receivePeers(reader);
        		break;
        	case "get report":
        		getReport(client);	
        		break;
        	}

        }
        client.close();
    }
}