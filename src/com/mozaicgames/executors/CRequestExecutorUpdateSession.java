package com.mozaicgames.executors;

import org.json.JSONException;
import org.json.JSONObject;

import com.mozaicgames.core.CBackendRequestException;
import com.mozaicgames.core.CBackendRequestExecutor;
import com.mozaicgames.core.CBackendRequestExecutorParameters;
import com.mozaicgames.core.EBackendResponsStatusCode;
import com.mozaicgames.utils.CBackendQueryGetUserGameData;
import com.mozaicgames.utils.CBackendQueryGetUserWalletData;
import com.mozaicgames.utils.CBackendQueryGetUserSettingsData;
import com.mozaicgames.utils.CBackendQueryResponse;
import com.mozaicgames.utils.CBackendQueryValidateDevice;
import com.mozaicgames.utils.CBackendSession;
import com.mozaicgames.utils.CBackendSessionManager;

public class CRequestExecutorUpdateSession extends CBackendRequestExecutor
{
	@Override
	public boolean isSessionTokenValidationNeeded() 
	{ 
		return false; 
	}
	
	@Override
	public JSONObject execute(JSONObject jsonData, CBackendRequestExecutorParameters parameters) throws CBackendRequestException
	{
		String deviceToken = null;
		try 
		{
			deviceToken = jsonData.getString(CRequestKeys.mKeyClientSessionToken);
		}		
		catch (JSONException e)
		{
			// bad input
			// return database connection error - status retry
			throw new CBackendRequestException(EBackendResponsStatusCode.INVALID_DATA, "Invalid input data!");
		}
		
		long deviceId = 0;
		int userId = 0;
		boolean createNewSession = false;
		
		final CBackendSessionManager sessionManager = parameters.getSessionManager();		
		final String remoteAddress = parameters.getRemoteAddress();
		CBackendSession activeSession = sessionManager.getActiveSessionFor(deviceToken);
		if (null == activeSession)
		{
			CBackendSession lastKnownSession = sessionManager.getLastKnownSessionFor(deviceToken);
			if (null == lastKnownSession)
			{
				throw new CBackendRequestException(EBackendResponsStatusCode.INVALID_TOKEN_SESSION_KEY, "Unknown session token!");
			}
			
			activeSession = lastKnownSession;
			createNewSession = true;
		}
		else
		{
			createNewSession = activeSession.getIp().equals(remoteAddress) == false;	
		}
		
//		if (false == sessionManager.isSessionValid(activeSession))
//		{
//			throw new CBackendRequestException(EBackendResponsStatusCode.TOKEN_SESSION_KEY_EXPIRED, "Token expired!");
//		}
		
		deviceId = activeSession.getDeviceId();
		userId = activeSession.getUserId();
		
		final CBackendQueryValidateDevice validatorDevice = new CBackendQueryValidateDevice(parameters.getSqlDataSource(), deviceId);
		final CBackendQueryResponse validatorResponse = validatorDevice.execute();		
		if (validatorResponse.getCode() != EBackendResponsStatusCode.STATUS_OK)
		{
			throw new CBackendRequestException(validatorResponse.getCode(), validatorResponse.getBody());
		}
		
		final String devicePlatform = validatorResponse.getBody(); 
		if (createNewSession)
		{
			activeSession = sessionManager.createSession(deviceId, userId, remoteAddress, devicePlatform);
		}
		
		if (activeSession != null)
		{
			try 
			{
				JSONObject jsonResponse = new JSONObject();
				jsonResponse.put(CRequestKeys.mKeyClientSessionToken, activeSession.getKey());
				jsonResponse.put(CRequestKeys.mKeyClientUserSettingsData, CBackendQueryGetUserSettingsData.getUserGameData(userId, parameters.getSqlDataSource()));
				jsonResponse.put(CRequestKeys.mKeyClientUserWalletData, CBackendQueryGetUserWalletData.getUserGameData(userId, devicePlatform, parameters.getSqlDataSource()));
				jsonResponse.put(CRequestKeys.mKeyClientUserGameData, CBackendQueryGetUserGameData.getUserData(userId, parameters.getSqlDataSource()));
				return toJSONObject(EBackendResponsStatusCode.STATUS_OK, jsonResponse);
			} 
			catch (JSONException e)
			{
				System.err.println(e.getMessage());
			}
		}
		
		// error processing statement
		// return statement error - status error
		throw new CBackendRequestException(EBackendResponsStatusCode.INTERNAL_ERROR, "Unable to retrive active session!");
	}
}
