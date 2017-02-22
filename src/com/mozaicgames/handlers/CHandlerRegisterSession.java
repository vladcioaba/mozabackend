package com.mozaicgames.handlers;

import java.io.IOException;

import javax.sql.DataSource;

import org.json.JSONException;
import org.json.JSONObject;

import com.mozaicgames.core.CBackendRequestHandler;
import com.mozaicgames.core.EBackendResponsStatusCode;
import com.mozaicgames.utils.CBackendAdvancedEncryptionStandard;
import com.mozaicgames.utils.CBackendQueryValidateDevice;
import com.mozaicgames.utils.CBackendSession;
import com.mozaicgames.utils.CBackendSessionManager;
import com.mozaicgames.utils.CBackendQueryResponse;
import com.mozaicgames.utils.CBackendUtils;
import com.sun.net.httpserver.HttpExchange;

public class CHandlerRegisterSession extends CBackendRequestHandler 
{
	private final String 											mEncriptionCode; 
	private final CBackendSessionManager							mSessionManager;		
	
	private final String mKeyClientDeviceToken	= "client_device_token";
	private final String mKeyClientUserToken	= "client_user_token";
	private final String mKeyClientVersion		= "client_version";

	public CHandlerRegisterSession(DataSource sqlDataSource, String encriptionCode, String minClientVersionAllowed, CBackendSessionManager sessionManager) throws Exception
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
		String deviceToken = null;
		String userToken = null;
		try 
		{
			JSONObject jsonRequestBody = new JSONObject(strRequestBody);
			
			if (jsonRequestBody.has(mKeyClientDeviceToken) == false ||
				jsonRequestBody.has(mKeyClientUserToken) == false ||
				jsonRequestBody.has(mKeyClientVersion) == false)
			{
				throw new JSONException("Missing variables");
			}
		
			clientVersion = jsonRequestBody.getString(mKeyClientVersion);
			deviceToken = jsonRequestBody.getString(mKeyClientDeviceToken);
			userToken = jsonRequestBody.getString(mKeyClientUserToken);
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
		
		CBackendAdvancedEncryptionStandard encripter = new CBackendAdvancedEncryptionStandard(mEncriptionCode, "AES");
		// decrypt device id from token
		long deviceId = 0;
		int userId = 0;
		try 
		{
			deviceId = Long.parseLong(encripter.decrypt(deviceToken));
			userId = Integer.parseInt(encripter.decrypt(userToken));
		} 
		catch (Exception e) 
		{
			// error processing statement
			// return statement error - status error
			intResponseCode = EBackendResponsStatusCode.INTERNAL_ERROR;
			strResponseBody = "Unable to validate tokens!";
			CBackendUtils.writeResponseInExchange(t, intResponseCode, strResponseBody);
			return;
		}
		
		CBackendQueryValidateDevice validatorDevice = new CBackendQueryValidateDevice(getDataSource(), deviceId);
		CBackendQueryResponse validatorResponse = validatorDevice.execute();
		
		if (validatorResponse.getCode() != EBackendResponsStatusCode.STATUS_OK)
		{
			CBackendUtils.writeResponseInExchange(t, validatorResponse.getCode(), validatorResponse.getBody());
			return;
		}
	
		CBackendSession activeSession = mSessionManager.getSessionFor(deviceId, userId);
		if (activeSession == null) 
		{
			final String remoteAddress = t.getRemoteAddress().getAddress().getHostAddress();
			activeSession = mSessionManager.createSession(deviceId, userId, remoteAddress);
		}
		
		if (activeSession != null)
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
