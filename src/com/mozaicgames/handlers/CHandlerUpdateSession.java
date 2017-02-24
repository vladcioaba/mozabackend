package com.mozaicgames.handlers;

import java.io.IOException;

import javax.sql.DataSource;

import org.json.JSONException;
import org.json.JSONObject;

import com.mozaicgames.core.CBackendRequestHandler;
import com.mozaicgames.core.EBackendResponsStatusCode;
import com.mozaicgames.utils.CBackendQueryResponse;
import com.mozaicgames.utils.CBackendQueryValidateDevice;
import com.mozaicgames.utils.CBackendSession;
import com.mozaicgames.utils.CBackendSessionManager;
import com.mozaicgames.utils.CBackendUtils;
import com.sun.net.httpserver.HttpExchange;

public class CHandlerUpdateSession extends CBackendRequestHandler 
{
	private final CBackendSessionManager							mSessionManager;		
	
	private final String mKeySessionKey			= "session_key";
	private final String mKeyClientVersion		= "client_version";

	public CHandlerUpdateSession(DataSource sqlDataSource, String minClientVersionAllowed, CBackendSessionManager sessionManager) throws Exception
	{
		super(sqlDataSource, minClientVersionAllowed);
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
			
			if (jsonRequestBody.has(mKeySessionKey) == false ||
				jsonRequestBody.has(mKeyClientVersion) == false)
			{
				throw new JSONException("Missing variables");
			}
		
			clientVersion = jsonRequestBody.getString(mKeyClientVersion);
			sessionKey = jsonRequestBody.getString(mKeySessionKey);
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
		
		CBackendSession session = mSessionManager.getLastKnownSessionFor(sessionKey);
		if (session != null)
		{
			CBackendQueryValidateDevice validatorDevice = new CBackendQueryValidateDevice(getDataSource(), session.getDeviceId());
			CBackendQueryResponse validatorResponse = validatorDevice.execute();
			
			if (validatorResponse.getCode() != EBackendResponsStatusCode.STATUS_OK)
			{
				CBackendUtils.writeResponseInExchange(t, validatorResponse.getCode(), validatorResponse.getBody());
				return;
			}
			
			if (false == mSessionManager.isSessionValid(session))
			{
				final String remoteAddress = t.getRemoteAddress().getAddress().getHostAddress();
				session = mSessionManager.createSession(session.getDeviceId(), session.getUserId(), remoteAddress);
				
				if (session == null)
				{
					// internal error
					intResponseCode = EBackendResponsStatusCode.INVALID_TOKEN_SESSION_KEY;
					strResponseBody = "Invalid session key!";
					CBackendUtils.writeResponseInExchange(t, intResponseCode, strResponseBody);
					return;
				}
			}			
			
			try 
			{
				JSONObject jsonResponse = new JSONObject();
				jsonResponse.put("session_key", session.getKey());
				strResponseBody = jsonResponse.toString();
				CBackendUtils.writeResponseInExchange(t, intResponseCode, strResponseBody);
				return;
			}
			catch (JSONException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			intResponseCode = EBackendResponsStatusCode.INTERNAL_ERROR;
			strResponseBody = "Internall error!";
			CBackendUtils.writeResponseInExchange(t, intResponseCode, strResponseBody);
			return;
		}
		else			
		{
			// client version not allowed
			intResponseCode = EBackendResponsStatusCode.INVALID_TOKEN_SESSION_KEY;
			strResponseBody = "Invalid session key!";
			CBackendUtils.writeResponseInExchange(t, intResponseCode, strResponseBody);
		}	
	}
}
