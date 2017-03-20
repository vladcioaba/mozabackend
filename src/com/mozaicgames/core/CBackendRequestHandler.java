package com.mozaicgames.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Map;

import javax.sql.DataSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mozaicgames.executors.CRequestKeys;
import com.mozaicgames.utils.CBackendAdvancedEncryptionStandard;
import com.mozaicgames.utils.CBackendQueryResponse;
import com.mozaicgames.utils.CBackendQueryValidateDevice;
import com.mozaicgames.utils.CBackendSession;
import com.mozaicgames.utils.CBackendSessionManager;
import com.mozaicgames.utils.CBackendUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class CBackendRequestHandler implements HttpHandler 
{
	private final DataSource							mSqlDataSource;
	private final CBackendSessionManager				mSessionManager;
	private final String 								mMinClientVersionAllowed;
	private final CBackendAdvancedEncryptionStandard	mEncripter;

	private final String								mKeyRequestArray			= "requests";
	private final String								mKeyResponseObject 			= "response";
	private final String 								mKeyRequestName				= "name";
	private final String 								mKeyRequestData				= "data";
	
	private Map<String, CBackendRequestExecutor>		mExecuters 					= new Hashtable<>();
	
	public CBackendRequestHandler(final DataSource sqlDataSource,
						 		  final CBackendSessionManager sessionManager,
						 		  final String minClientVersionAllowed,
						 		  final CBackendAdvancedEncryptionStandard encripter) throws Exception
	{
		mSqlDataSource = sqlDataSource;
		mSessionManager = sessionManager;
		if (mSqlDataSource == null || mSessionManager == null)
		{
			throw new Exception("Invalid argument");
		}
		mMinClientVersionAllowed = minClientVersionAllowed;
		mEncripter = encripter;
	}
	
	public void addRequestExecuter(String key, CBackendRequestExecutor executor)
	{
		mExecuters.put(key, executor);
	}
	
	@Override
    public void handle(HttpExchange t) throws IOException 
	{
		// This variable is here for debug purpuses
		final String strRequestBody = CBackendUtils.getStringFromStream(t.getRequestBody());
		
		JSONObject jsonResponseBody = null;
		
		JSONObject jsonRequestBody = null;
		JSONArray jsonRequestsArray = null;
		String strClientCoreVersion = null;
		String strClientAppVersion = null;
		final String remoteAddress = t.getRemoteAddress().getAddress().getHostAddress();
		
		boolean continueExecution = true;
		
		try 
		{
			jsonRequestBody = new JSONObject(strRequestBody);
			strClientCoreVersion = jsonRequestBody.getString(CRequestKeys.mKeyDeviceClientCoreVersion);
			strClientAppVersion = jsonRequestBody.getString(CRequestKeys.mKeyDeviceClientAppVersion);
			jsonRequestsArray = jsonRequestBody.getJSONArray(mKeyRequestArray);
		}
		catch (JSONException ex)
		{
			// bad input
			// return database connection error - status retry
			jsonResponseBody = CBackendRequestExecutor.toJSONObject(EBackendResponsStatusCode.INVALID_DATA, "Invalid input data! " + ex.getMessage());
			continueExecution = false;
		}
		
		if (continueExecution && CBackendUtils.compareStringIntegerValue(mMinClientVersionAllowed, strClientCoreVersion) > 0)  
		{
			jsonResponseBody = CBackendRequestExecutor.toJSONObject(EBackendResponsStatusCode.CLIENT_OUT_OF_DATE, "Invalid application version!");
			continueExecution = false;
		}
		
		if (continueExecution)
		{
			jsonResponseBody = null;			
			final JSONArray jsonResponseArray = new JSONArray();
			
			final int jsonRequestsArrayLength = jsonRequestsArray.length();
			for (int i = 0; i < jsonRequestsArrayLength; i++) 
			{
				JSONObject jsonRequstObject = null;
			    try 
			    {
					jsonRequstObject = jsonRequestsArray.getJSONObject(i);
				} 
			    catch (JSONException e) 
			    {
			    	// object at index i is not a jsonobject.
			    	jsonResponseBody = CBackendRequestExecutor.toJSONObject(EBackendResponsStatusCode.INVALID_DATA, "Invalid data format!");
					break;
				}
	
			    String strRequestName = null;
				try 
				{
					strRequestName = jsonRequstObject.getString(mKeyRequestName);
				} 
				catch (JSONException e) 
				{
					// object at index i does not have a string name.
					jsonResponseBody = CBackendRequestExecutor.toJSONObject(EBackendResponsStatusCode.INVALID_DATA, "Invalid data format!");
					break;
				} 
				
				JSONObject jsonRequestData = null;
				try 
				{
					jsonRequestData = jsonRequstObject.getJSONObject(mKeyRequestData);
				} 
				catch (JSONException e) 
				{
					// object at index i does not have a jsonobject data.
					JSONObject jsonExecutorResponse = CBackendRequestExecutor.toJSONObject(EBackendResponsStatusCode.INVALID_DATA, "Invalid data format!");
					try 
					{
						jsonExecutorResponse.put(mKeyRequestName, strRequestName);
					} 
					catch (JSONException e1) 
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					jsonResponseArray.put(jsonExecutorResponse);
					continue;
				}
				
				// execute request				
				JSONObject jsonExecutorResponse = new JSONObject();
				try 
				{
					jsonExecutorResponse.put(mKeyRequestName, strRequestName);
				} 
				catch (JSONException e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
					continue;
				}
				
				try 
				{
					if (mExecuters.containsKey(strRequestName) == false)
					{
						throw new CBackendRequestException(EBackendResponsStatusCode.INVALID_REQUEST, "Unknown request name!");
					}
					CBackendRequestExecutor executor = mExecuters.get(strRequestName);
				
					String deviceToken = null;
					long deviceId = 0;
					int userId = 0;
					
					if (executor.isSessionTokenValidationNeeded())
					{
						try 
						{
							deviceToken = jsonRequestData.getString(CRequestKeys.mKeyClientSessionToken);
						}		
						catch (JSONException e)
						{
							// bad input
							// return database connection error - status retry
							throw new CBackendRequestException(EBackendResponsStatusCode.INVALID_DATA, "Invalid input data!");
						}
						
						CBackendSession activeSession = mSessionManager.getActiveSessionFor(deviceToken);
						if (activeSession == null)
						{
							CBackendSession lastKnownSession = mSessionManager.getLastKnownSessionFor(deviceToken);
							if (lastKnownSession == null)
							{
								throw new CBackendRequestException(EBackendResponsStatusCode.INVALID_TOKEN_SESSION_KEY, "Unknown session token!");
							}
							
							deviceId = lastKnownSession.getDeviceId();
							userId = lastKnownSession.getUserId();
						}
						else
						{
							if (activeSession.getIp().equals(remoteAddress) == false)
							{
								throw new CBackendRequestException(EBackendResponsStatusCode.INVALID_TOKEN_SESSION_KEY, "Unknown session token!");
							}
							deviceId = activeSession.getDeviceId();
							userId = activeSession.getUserId();
						}
						
						final CBackendQueryValidateDevice validatorDevice = new CBackendQueryValidateDevice(mSqlDataSource, deviceId);
						final CBackendQueryResponse validatorResponse = validatorDevice.execute();		
						if (validatorResponse.getCode() != EBackendResponsStatusCode.STATUS_OK)
						{
							throw new CBackendRequestException(validatorResponse.getCode(), validatorResponse.getBody());
						}
					}
					
					final CBackendRequestExecutorParameters parameters = new CBackendRequestExecutorParameters(remoteAddress,
							   mEncripter,
							   mSqlDataSource,
						       mSessionManager,
							   strClientCoreVersion,
							   strClientAppVersion,
							   userId,
							   deviceId);
											
					final JSONObject jsonResult = executor.execute(jsonRequestData, parameters);
					try 
					{
						jsonExecutorResponse.put(mKeyResponseObject, jsonResult);
					}
					catch (JSONException e) 
					{
						throw new CBackendRequestException(EBackendResponsStatusCode.INTERNAL_ERROR, "Ex: " + e.getMessage());
					}
				}
				catch (CBackendRequestException e)
				{
					try 
					{
						jsonExecutorResponse.put(mKeyResponseObject, CBackendRequestExecutor.toJSONObject(e.getStatus(), e.getBody()));
					}
					catch (JSONException e1) 
					{
						e1.printStackTrace();
						continue;
					}
				}
								
				jsonResponseArray.put(jsonExecutorResponse);
			}
			
			if (jsonResponseBody == null)
			{
				jsonResponseBody = CBackendRequestExecutor.toJSONObject(EBackendResponsStatusCode.STATUS_OK, jsonResponseArray);
			}
		}
		
		// write response to string
		String strFinal = jsonResponseBody.toString();
		t.sendResponseHeaders(200, strFinal.length());
		OutputStream os = t.getResponseBody();
		os.write(strFinal.getBytes());
		os.flush();
		os.close();		
    }
}
