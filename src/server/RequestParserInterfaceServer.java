package server;

import server.ResponseException;

public interface RequestParserInterfaceServer {
	public String getResponseType(String response);
	public String auhtInit(String alarmId);
	public int Response(String response);
	
	public String sensorReadingsRequest(String token);	
	public String authChallage(String token)throws ResponseException;
	public String getauthChallangeReply(String response) throws Exception ;
	public String authSuccess(String sessionToken);
	public String authFail();
	
}
