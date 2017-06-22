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
			final JSONArray jsonResponseArray = new JSONArray();
			
			CBackendSession activeSession = null;
			String deviceToken = null;
			long deviceId = 0;
			int userId = 0;
			long sessionId = 0;
			String devicePlatform = "";
			
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
			    	jsonResponseArray.put(CBackendRequestExecutor.toJSONObject(EBackendResponsStatusCode.INVALID_DATA, "Invalid data format!"));
					continue;
				}
	
			    String strRequestName = null;
				try 
				{
					strRequestName = jsonRequstObject.getString(mKeyRequestName);
				} 
				catch (JSONException e) 
				{
					// object at index i does not have a string name.
					jsonResponseArray.put(CBackendRequestExecutor.toJSONObject(EBackendResponsStatusCode.INVALID_DATA, "Invalid data format!"));
					continue;
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
						System.err.println(e1.getMessage());
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
					System.err.println(e1.getMessage());
					continue;
				}
				
				try 
				{
					if (mExecuters.containsKey(strRequestName) == false)
					{
						throw new CBackendRequestException(EBackendResponsStatusCode.INVALID_REQUEST, "Unknown request name!");
					}
					CBackendRequestExecutor executor = mExecuters.get(strRequestName);
				
					boolean updateSessionToken = false;
					
					if (executor.isSessionTokenValidationNeeded())
					{
						if (activeSession != null)
						{
							if (false == mSessionManager.isSessionValid(activeSession))
							{
								// the session is updated
								updateSessionToken = true;
							}
						}
						else
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
							
							activeSession = mSessionManager.getActiveSessionFor(deviceToken);
							if (activeSession == null)
							{
								activeSession = mSessionManager.getLastKnownSessionFor(deviceToken);
								if (activeSession == null)
								{
									throw new CBackendRequestException(EBackendResponsStatusCode.INVALID_TOKEN_SESSION_KEY, "Unknown session token!");
								}	
							
								if (false == mSessionManager.isSessionValid(activeSession))
								{
									// the session is updated
									updateSessionToken = true;
								}
							}
						}
					}

					if (activeSession != null)
					{
						sessionId = activeSession.getId();						
						deviceId = activeSession.getDeviceId();
						userId = activeSession.getUserId();			
						if (activeSession.getIp().equals(remoteAddress) == false)
						{
							updateSessionToken = true;
						}
						
						// if there is no active session there's no device
						final CBackendQueryValidateDevice validatorDevice = new CBackendQueryValidateDevice(mSqlDataSource, deviceId);
						final CBackendQueryResponse validatorResponse = validatorDevice.execute();		
						if (validatorResponse.getCode() != EBackendResponsStatusCode.STATUS_OK)
						{
							throw new CBackendRequestException(validatorResponse.getCode(), validatorResponse.getBody());
						}
						devicePlatform = validatorResponse.getBody();
						
						if (updateSessionToken)
						{
							activeSession = mSessionManager.updateSession(sessionId, deviceId, userId, activeSession.getKey(), remoteAddress, devicePlatform);			
							try 
							{
								jsonExecutorResponse.put(CRequestKeys.mKeyClientSessionToken, activeSession.getKey());
							} 
							catch (JSONException e) 
							{
								throw new CBackendRequestException(EBackendResponsStatusCode.INTERNAL_ERROR, "Ex: " + e.getMessage());
							}
						}
					}
					
					final CBackendRequestExecutorParameters parameters = new CBackendRequestExecutorParameters(
							remoteAddress,
							mEncripter,
							mSqlDataSource,
						    mSessionManager,
							strClientCoreVersion,
							strClientAppVersion,
							userId,
							deviceId,
							sessionId,
							deviceToken,
							devicePlatform);
											
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
						System.err.println(e1.getMessage());;
						continue;
					}
				}
								
				jsonResponseArray.put(jsonExecutorResponse);
			}
			
			jsonResponseBody = CBackendRequestExecutor.toJSONObject(EBackendResponsStatusCode.STATUS_OK, jsonResponseArray);
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
