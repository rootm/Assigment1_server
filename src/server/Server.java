package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Server {
	
	private static HashMap<String,PrintWriter> alarmList=new HashMap<String, PrintWriter>();
	private static HashMap<String,String> tokenList=new HashMap<String, String>();
	public static void main(String[] args) throws Exception {
        System.out.println("The Fire Alarm Monitor server is running.");
        ServerSocket Alarmlistener = new ServerSocket(3001);
        try {
            while (true) {
                new Handler(Alarmlistener.accept()).start();
            }
        } finally {
        	Alarmlistener.close();
        }
    }


    private static class Handler extends Thread {
    	//Variables
    	private Socket socket;
    	private String response="";
    	private RequestParserServer parser =new RequestParserServer();
    	//End Variables
    	
    	
        public Handler(Socket socket) {
            this.socket = socket;
        }

     
        
        
       
        
        public void run() {
            try {

               
            	PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
              

                System.out.println("client Connecting");
                
                while (true) {
                    
                    response = in.readLine();
                   
                    if (!(response==null)) {
                    synchronized (alarmList) {
                    	JSONObject json=(JSONObject)JSONValue.parse(Base64.decodeBase64(response).toString());
                    	System.out.println(json.toJSONString());
                        if (!alarmList.containsKey(json.get("id").toString())) {
                            alarmList.put(json.get("id").toString(),out);
                            System.out.println("alarm id added "+json.get("id").toString());
                            break;
                        }else {
                        	JSONObject request=new JSONObject();
                    		json.put("header",parser.AlarmExists());
                    		
                        	out.print(request.toJSONString());
                        	 System.out.println(request.toJSONString());
                        }
                    }
                    }
                }
                
                
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                
             
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
    
}
