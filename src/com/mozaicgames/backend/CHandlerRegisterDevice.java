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
import com.mozaicgames.utils.Utils;
import com.sun.net.httpserver.HttpExchange;

public class CHandlerRegisterDevice extends CBackendRequestHandler 
{

	private final String 							mEncriptionCode;
	
	private final String mKeyDeviceModel			= "device_model";
	private final String mKeyDeviceOsVersion 		= "device_os_version";
	private final String mKeyDevicePlatform			= "device_platform";
	private final String mKeyClientVersion			= "client_version";

	public CHandlerRegisterDevice(DataSource sqlDataSource, String encriptionConde, String minClientVersionAllowed) throws Exception
	{
		super(sqlDataSource, minClientVersionAllowed);
		mEncriptionCode = encriptionConde;
	}

	@Override
    public void handle(HttpExchange t) throws IOException 
	{		
		EBackendResponsStatusCode intResponseCode = EBackendResponsStatusCode.STATUS_OK;
		String strResponseBody = "";
		String strRequestBody = Utils.getStringFromStream(t.getRequestBody());
		
		String clientVersion = null;
		String deviceModel = null;
		String deviceOsVerrsion = null;
		String devicePlatform = null;
		try 
		{
			JSONObject jsonRequestBody = new JSONObject(strRequestBody);			
			clientVersion = jsonRequestBody.getString(mKeyClientVersion);
			deviceModel = jsonRequestBody.getString(mKeyDeviceModel);
			deviceOsVerrsion= jsonRequestBody.getString(mKeyDeviceOsVersion);
			devicePlatform = jsonRequestBody.getString(mKeyDevicePlatform);			
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
		
		Connection sqlConnection = null;
		Statement sqlStatement = null;
		
		try 
		{
			sqlConnection = getDataSource().getConnection();
			sqlConnection.setAutoCommit(false);
			sqlStatement = sqlConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_UPDATABLE);
			
			final String remoteAddress = t.getRemoteAddress().getAddress().getHostAddress();
			
			// create new device
			int affectedRows = sqlStatement.executeUpdate("insert into devices ( device_model, device_os_version, device_platform, device_app_version, device_ip ) values "
							 + "('" + deviceModel + "' ,"
							 + " '" + deviceOsVerrsion + "' ,"
							 + " '" + devicePlatform + "' ,"
							 + " '" + clientVersion + "' ,"
							 + " '" + remoteAddress + "');", Statement.RETURN_GENERATED_KEYS);		
			
			if (affectedRows == 0)
			{
				throw new Exception("Nothing updated in database!");
			}
			
			// get last user id
			long newDeviceId = 0;
			ResultSet restultLastInsert = sqlStatement.getGeneratedKeys();
			if (restultLastInsert.next())
			{
				newDeviceId = restultLastInsert.getLong(1);
			}			
			sqlConnection.commit();
			
			AdvancedEncryptionStandard encripter = new AdvancedEncryptionStandard(mEncriptionCode, "AES");
			final String newUUID = encripter.encrypt(String.valueOf(newDeviceId));
			
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
			try 
			{
				sqlStatement.close();
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
			try 
			{
				sqlConnection.close();
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
	}
}