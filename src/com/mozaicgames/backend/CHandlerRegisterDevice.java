package com.mozaicgames.backend;

import java.io.IOException;
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
	
	private String mKeyDeviceModel				= "device_model";
	private String mKeyDeviceOsVersion 			= "device_os_version";
	private String mKeyDevicePlatform			= "device_platform";
	private String mKeyDeviceAppVersion			= "device_app_version";
	
	public CHandlerRegisterDevice(DataSource sqlDataSource, String encriptionConde) throws Exception
	{
		super(sqlDataSource);
		mEncriptionCode = encriptionConde;
	}

	@Override
    public void handle(HttpExchange t) throws IOException 
	{
		int intResponseCode = 200;
		String strResponseBody = "";
		String strRequestBody = t.getRequestBody().toString();
		
		JSONObject jsonRequestBody = null;
		try 
		{
			jsonRequestBody = new JSONObject(strRequestBody);
			
			if (jsonRequestBody.has(mKeyDeviceModel) == false ||
				jsonRequestBody.has(mKeyDeviceOsVersion) == false ||
				jsonRequestBody.has(mKeyDevicePlatform) == false ||
				jsonRequestBody.has(mKeyDeviceAppVersion) == false)
			{
				throw new JSONException("missing variables");
			}
		}
		catch (JSONException e)
		{
			// bad input
			// return database connection error - status retry
			intResponseCode = 1;
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
			intResponseCode = 2;
			strResponseBody = e.getMessage();
			outputResponse(t, intResponseCode, strResponseBody);
			return;
		}		
				
		String newUUID = null;
		try 
		{
			sqlStatement = sqlConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_UPDATABLE);
			
			// get last user id
			int device_id = -1;
			ResultSet restultLastInsert = sqlStatement.executeQuery("select device_id from devices order by device_id desc limit 1");
			while (restultLastInsert.next())
			{
				device_id = restultLastInsert.getInt(1);
			}
			
			AdvancedEncryptionStandard encripter = new AdvancedEncryptionStandard(mEncriptionCode);
			newUUID = encripter.encrypt(String.valueOf(device_id));
			
			String remoteAddress = t.getRemoteAddress().getAddress().toString();
			
			sqlStatement.executeQuery("insert into  devices ( device_token, device_model, device_os_version, device_platform, device_id ) values"
							 + " '" + newUUID + "' ,"
							 + " '" + jsonRequestBody.getString(mKeyDeviceModel) + "' ,"
							 + " '" + jsonRequestBody.getString(mKeyDeviceOsVersion) + "' ,"
							 + " '" + jsonRequestBody.getString(mKeyDevicePlatform) + "' ,"
							 + " '" + jsonRequestBody.getString(mKeyDeviceAppVersion) + "' ,"
							 + " '" + remoteAddress + "' ,"
							 + " );");
		}
		catch (Exception e)
		{
			// error processing statement
			// return statement error - status error
			intResponseCode = 3;
			outputResponse(t, intResponseCode, strResponseBody);
			return;
		}
		
		if (intResponseCode == 200)
		{
			// return new device ID;
		}
		
		outputResponse(t, intResponseCode, strResponseBody);
	}
}
