package com.mozaicgames.backend;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.json.JSONException;
import org.json.JSONObject;

import com.mozaicgames.utils.AdvancedEncryptionStandard;
import com.mozaicgames.utils.Utils;
import com.sun.net.httpserver.HttpExchange;

public class CHandlerUpdateUser extends CBackendRequestHandler 
{

	private String mEncriptionCode			= null; 
	
	private String mKeyClientDeviceToken	= "client_device_token";
	private String mKeyClientVersion		= "client_version";

	public CHandlerUpdateUser(DataSource sqlDataSource, String encriptionConde) throws Exception
	{
		super(sqlDataSource);
		mEncriptionCode = encriptionConde;
	}

	@Override
    public void handle(HttpExchange t) throws IOException 
	{		
		EBackendResponsStatusCode intResponseCode = EBackendResponsStatusCode.STATUS_OK;
		String strResponseBody = "";
		String strRequestBody = Utils.getStringFromStream(t.getRequestBody());
				
		JSONObject jsonRequestBody = null;
		try 
		{
			jsonRequestBody = new JSONObject(strRequestBody);
			
			if (jsonRequestBody.has(mKeyClientDeviceToken) == false ||
				jsonRequestBody.has(mKeyClientVersion) == false)
			{
				throw new JSONException("Missing variables");
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
			
			AdvancedEncryptionStandard encripter = new AdvancedEncryptionStandard(mEncriptionCode, "AES");
			String newUUID = encripter.encrypt(String.valueOf(device_id));
			String remoteAddress = t.getRemoteAddress().getAddress().getHostAddress();
			
			sqlStatement.executeUpdate("insert into  devices ( device_token, device_model, device_os_version, device_platform, device_app_version, device_ip ) values "
							 + "('" + newUUID + "' ,"
							 + " '" + jsonRequestBody.getString(mKeyClientVersion) + "' ,"
							 + " '" + remoteAddress + "');");		
			
			JSONObject jsonResponse = new JSONObject();
			jsonResponse.put("device_token", newUUID);
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
