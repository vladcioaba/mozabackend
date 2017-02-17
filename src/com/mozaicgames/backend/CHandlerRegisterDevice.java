package com.mozaicgames.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.json.JSONException;
import org.json.JSONObject;

import com.mozaicgames.backend.CBackendRequestHandler;
import com.mozaicgames.utils.AdvancedEncryptionStandard;
import com.sun.net.httpserver.HttpExchange;

public class CHandlerRegisterDevice extends CBackendRequestHandler 
{

	private String mEncriptionCode				= null; 
	
	private String mKeyData						= "data";
	private String mKeyClientVersion			= "client_version";

	private String mKeyDataDeviceModel			= "device_model";
	private String mKeyDataDeviceOsVersion 		= "device_os_version";
	private String mKeyDataDevicePlatform		= "device_platform";
	
	public CHandlerRegisterDevice(DataSource sqlDataSource, String encriptionConde) throws Exception
	{
		super(sqlDataSource);
		mEncriptionCode = encriptionConde;
	}

	@Override
    public void handle(HttpExchange t) throws IOException 
	{		
		EBackendResponsStatusCode intResponseCode = EBackendResponsStatusCode.STATUS_OK;
		String strResponseBody = "";
		
		InputStreamReader isr =  new InputStreamReader(t.getRequestBody(), "utf-8");
		BufferedReader br = new BufferedReader(isr);
		String strRequestBody = "";
		int b;
		while ((b = br.read()) != -1) {
			strRequestBody += (char)b;
		}
		br.close();
		isr.close();
		
		
		JSONObject jsonRequestBody = null;
		JSONObject jsonRequestData = null;
		try 
		{
			jsonRequestBody = new JSONObject(strRequestBody);
			
			if (jsonRequestBody.has(mKeyData) == false ||
				jsonRequestBody.has(mKeyClientVersion) == false)
			{
				throw new JSONException("missing variables");
			}
			
			jsonRequestData = jsonRequestBody.getJSONObject(mKeyData);			
			if (jsonRequestData.has(mKeyDataDeviceModel) == false ||
				jsonRequestData.has(mKeyDataDeviceOsVersion) == false ||
				jsonRequestData.has(mKeyDataDevicePlatform) == false)
			{
				throw new JSONException("missing variables");
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
		
		Connection sqlConnection = null;
		Statement sqlStatement = null;
		
		try 
		{
			sqlConnection = getDataSource().getConnection();
		}
		catch (SQLException e)
		{
			// could not get a connection
			// return database connection error - status retry
			intResponseCode = EBackendResponsStatusCode.INTERNAL_ERROR;
			strResponseBody = e.getMessage();
			outputResponse(t, intResponseCode, strResponseBody);
			return;
		}		
				
		String newUUID = null;
		try 
		{
			sqlStatement = sqlConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_UPDATABLE);
			
			// get last user id
			long device_id = 0;
			ResultSet restultLastInsert = sqlStatement.executeQuery("select device_id from devices order by device_id desc limit 1");
			while (restultLastInsert.next())
			{
				device_id = restultLastInsert.getLong(1);
			}
			
			AdvancedEncryptionStandard encripter = new AdvancedEncryptionStandard(mEncriptionCode, "RC4");
			newUUID = encripter.encrypt(String.valueOf(device_id));
			
			String remoteAddress = t.getRemoteAddress().getAddress().getHostAddress();
			
			sqlStatement.executeUpdate("insert into  devices ( device_token, device_model, device_os_version, device_platform, device_app_version, device_ip ) values "
							 + "('" + newUUID + "' ,"
							 + " '" + jsonRequestData.getString(mKeyDataDeviceModel) + "' ,"
							 + " '" + jsonRequestData.getString(mKeyDataDeviceOsVersion) + "' ,"
							 + " '" + jsonRequestData.getString(mKeyDataDevicePlatform) + "' ,"
							 + " '" + jsonRequestBody.getString(mKeyClientVersion) + "' ,"
							 + " '" + remoteAddress + "');");		
			
			JSONObject jsonResponse = new JSONObject();
			JSONObject jsonResponseDataToken = new JSONObject();
			jsonResponseDataToken.put("device_token", newUUID);
			jsonResponse.put("data", jsonResponseDataToken);
			strResponseBody = jsonResponse.toString();
			outputResponse(t, intResponseCode, strResponseBody);
		}
		catch (Exception e)
		{
			// error processing statement
			// return statement error - status error
			intResponseCode = EBackendResponsStatusCode.INTERNAL_ERROR;
			strResponseBody = e.getMessage();
			outputResponse(t, intResponseCode, strResponseBody);
		}
		finally
		{
			try {
				sqlStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				sqlConnection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
