package com.mozaicgames.backend;

import java.io.IOException;

import javax.sql.DataSource;

import org.json.JSONException;
import org.json.JSONObject;

import com.mozaicgames.utils.AdvancedEncryptionStandard;
import com.mozaicgames.utils.Utils;
import com.sun.net.httpserver.HttpExchange;

public class CHandlerUpdateSession extends CBackendRequestHandler 
{
	private final String 											mEncriptionCode; 
	private final CBackendSessionManager							mSessionManager;		
	
	private final String mKeyClientSessionKey   = "session_key";
	private final String mKeyClientDeviceToken	= "client_device_token";
	private final String mKeyClientUserToken	= "client_user_token";
	private final String mKeyClientVersion		= "client_version";

	public CHandlerUpdateSession(DataSource sqlDataSource, String encriptionConde, String minClientVersionAllowed, CBackendSessionManager sessionManager) throws Exception
	{
		super(sqlDataSource, minClientVersionAllowed);
		mEncriptionCode = encriptionConde;
		mSessionManager = sessionManager;
		if (mSessionManager == null)
		{
			throw new Exception("sessionManager is null!");
		}
	}

	@Override
    public void handle(HttpExchange t) throws IOException 
	{		
		EBackendResponsStatusCode intResponseCode = EBackendResponsStatusCode.STATUS_OK;
		String strResponseBody = "";
		String strRequestBody = Utils.getStringFromStream(t.getRequestBody());
		
		String clientVersion = null;
		String sessionKey = null;
		String deviceToken = null;
		String userToken = null;
		try 
		{
			JSONObject jsonRequestBody = new JSONObject(strRequestBody);
			
			if (jsonRequestBody.has(mKeyClientDeviceToken) == false ||
				jsonRequestBody.has(mKeyClientVersion) == false)
			{
				throw new JSONException("Missing variables");
			}
		
			clientVersion = jsonRequestBody.getString(mKeyClientVersion);
			deviceToken = jsonRequestBody.getString(mKeyClientDeviceToken);
			
			if (jsonRequestBody.has(mKeyClientSessionKey))
			{
				sessionKey= jsonRequestBody.getString(mKeyClientSessionKey);
			}
		}		
		catch (JSONException e)
		{
			// bad input
			// return database connection error - status retry
			intResponseCode = EBackendResponsStatusCode.INVALID_DATA;
			strResponseBody = "Bad input data!";
			outputResponse(t, intResponseCode, strResponseBody);
			return;
		}
		
		if (Utils.compareStringIntegerValue(clientVersion, getMinClientVersionAllowed()) == -1)
		{
			// client version not allowed
			intResponseCode = EBackendResponsStatusCode.CLIENT_OUT_OF_DATE;
			strResponseBody = "Client out of date!";
			outputResponse(t, intResponseCode, strResponseBody);
			return;
		}
		
		CBackendSession activeSession = null;
		if (sessionKey != null && mSessionManager.isSessionValid(sessionKey))
		{
			activeSession = mSessionManager.getActiveSession(sessionKey);
		}
		else
		{
			AdvancedEncryptionStandard encripter = new AdvancedEncryptionStandard(mEncriptionCode, "AES");
			// decrypt device id from token
			long deviceId = 0;
			int userId = 0;
			try 
			{
				deviceId= Long.parseLong(encripter.decrypt(deviceToken));
				userId = Integer.parseInt(encripter.decrypt(userToken));
			} 
			catch (Exception e) 
			{
				// error processing statement
				// return statement error - status error
				intResponseCode = EBackendResponsStatusCode.INTERNAL_ERROR;
				strResponseBody = "Unable to validate tokens!";
				outputResponse(t, intResponseCode, strResponseBody);	
			}
			
			final String remoteAddress = t.getRemoteAddress().getAddress().getHostAddress();
			activeSession = mSessionManager.createSession(deviceId, userId, remoteAddress);
		}
		
		if (activeSession != null)
		{
			try 
			{
				JSONObject jsonResponse = new JSONObject();
				jsonResponse.put("active_session", activeSession.getKey());
				strResponseBody = jsonResponse.toString();
				outputResponse(t, intResponseCode, strResponseBody);
				return;
			} 
			catch (JSONException e) 
			{
				System.err.println(e.getMessage());
			}
		}
		
		// error processing statement
		// return statement error - status error
		intResponseCode = EBackendResponsStatusCode.INTERNAL_ERROR;
		strResponseBody = "Unable to retrive active session!";
		outputResponse(t, intResponseCode, strResponseBody);			
	}
}
