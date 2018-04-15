package server;

import server.ResponseException;

public interface RequestParserInterfaceServer {
	public String getResponseType(String response);
	public String auhtInit(String alarmId);
	public int Response(String response);
	public String exchangeCypher(String encrypted,String pass);
	public String sensorReadingsRequest(String token);	
	public String AuthChallangeToken()throws ResponseException;
	public String getauthChallangeReply(String response) throws Exception ;
	public String SessionToken(String token);
	
}
