package com.mozaicgames.handlers;

import java.io.IOException;

import javax.sql.DataSource;

import org.json.JSONException;
import org.json.JSONObject;

import com.mozaicgames.core.CBackendRequestHandler;
import com.mozaicgames.core.EBackendResponsStatusCode;
import com.mozaicgames.utils.CBackendAdvancedEncryptionStandard;
import com.mozaicgames.utils.CBackendQueryResponse;
import com.mozaicgames.utils.CBackendQueryValidateDevice;
import com.mozaicgames.utils.CBackendSession;
import com.mozaicgames.utils.CBackendSessionManager;
import com.mozaicgames.utils.CBackendUtils;
import com.sun.net.httpserver.HttpExchange;

public class CHandlerRegisterGameData extends CBackendRequestHandler 
{
	private final String 											mEncriptionCode; 
	private final CBackendSessionManager							mSessionManager;		
	
	private final String mKeyClientSessionToken	= "session_key";
	private final String mKeyClientVersion		= "client_version";

	public CHandlerRegisterGameData(DataSource sqlDataSource, String encriptionCode, String minClientVersionAllowed, CBackendSessionManager sessionManager) throws Exception
	{
		super(sqlDataSource, minClientVersionAllowed);
		mEncriptionCode = encriptionCode;
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
		String strRequestBody = CBackendUtils.getStringFromStream(t.getRequestBody());
		
		String clientVersion = null;
		String sessionKey = null;
		try 
		{
			JSONObject jsonRequestBody = new JSONObject(strRequestBody);
			
			if (jsonRequestBody.has(mKeyClientSessionToken) == false ||
				jsonRequestBody.has(mKeyClientVersion) == false)
			{
				throw new JSONException("Missing variables");
			}
		
			clientVersion = jsonRequestBody.getString(mKeyClientVersion);
			sessionKey = jsonRequestBody.getString(mKeyClientSessionToken);
		}		
		catch (JSONException e)
		{
			// bad input
			// return database connection error - status retry
			intResponseCode = EBackendResponsStatusCode.INVALID_DATA;
			strResponseBody = "Bad input data!";
			CBackendUtils.writeResponseInExchange(t, intResponseCode, strResponseBody);
			return;
		}
		
		if (CBackendUtils.compareStringIntegerValue(clientVersion, getMinClientVersionAllowed()) == -1)
		{
			// client version not allowed
			intResponseCode = EBackendResponsStatusCode.CLIENT_OUT_OF_DATE;
			strResponseBody = "Client out of date!";
			CBackendUtils.writeResponseInExchange(t, intResponseCode, strResponseBody);
			return;
		}
	
		CBackendSession activeSession = mSessionManager.getSessionFor(sessionKey);
		if (activeSession == null) 
		{
			intResponseCode = EBackendResponsStatusCode.INVALID_TOKEN_SESSION_KEY;
			strResponseBody = "Invalid session key!";
			CBackendUtils.writeResponseInExchange(t, intResponseCode, strResponseBody);			
		}
		else
		{
			try 
			{
				JSONObject jsonResponse = new JSONObject();
				jsonResponse.put("session_key", activeSession.getKey());
				strResponseBody = jsonResponse.toString();
				CBackendUtils.writeResponseInExchange(t, intResponseCode, strResponseBody);
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
		CBackendUtils.writeResponseInExchange(t, intResponseCode, strResponseBody);			
	}
}
