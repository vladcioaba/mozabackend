package com.mozaicgames.handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.sql.DataSource;

import org.json.JSONException;
import org.json.JSONObject;

import com.mozaicgames.core.CBackendRequestHandler;
import com.mozaicgames.core.EBackendResponsStatusCode;
import com.mozaicgames.utils.CBackendAdvancedEncryptionStandard;
import com.mozaicgames.utils.CBackendUtils;
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
		String strRequestBody = CBackendUtils.getStringFromStream(t.getRequestBody());
		
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
		
		Connection sqlConnection = null;
		PreparedStatement preparedStatementInsert = null;
		
		try 
		{
			sqlConnection = getDataSource().getConnection();
			sqlConnection.setAutoCommit(false);
			final String remoteAddress = t.getRemoteAddress().getAddress().getHostAddress();
			
			String strQueryInsert = "insert into devices ( device_model, device_os_version, device_platform, device_app_version, device_ip, device_creation_time ) values (?,?,?,?,?, ?);";
			preparedStatementInsert = sqlConnection.prepareStatement(strQueryInsert, PreparedStatement.RETURN_GENERATED_KEYS);
			preparedStatementInsert.setString(1, deviceModel);
			preparedStatementInsert.setString(2, deviceOsVerrsion);
			preparedStatementInsert.setString(3, devicePlatform);
			preparedStatementInsert.setString(4, clientVersion);
			preparedStatementInsert.setString(5, remoteAddress);
			preparedStatementInsert.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
			
			int affectedRows = preparedStatementInsert.executeUpdate();
			if (affectedRows == 0)
			{
				throw new Exception("Nothing updated in database!");
			}
			
			// get last user id
			long newDeviceId = 0;
			ResultSet restultLastInsert = preparedStatementInsert.getGeneratedKeys();
			if (restultLastInsert.next())
			{
				newDeviceId = restultLastInsert.getLong(1);
			}			
			preparedStatementInsert.close();
			preparedStatementInsert = null;
			
			CBackendAdvancedEncryptionStandard encripter = new CBackendAdvancedEncryptionStandard(mEncriptionCode, "AES");
			final String newUUID = encripter.encrypt(String.valueOf(newDeviceId));
			
			JSONObject jsonResponse = new JSONObject();
			jsonResponse.put("device_token", newUUID);
			strResponseBody = jsonResponse.toString();
			CBackendUtils.writeResponseInExchange(t, intResponseCode, strResponseBody);
			
			sqlConnection.commit();			
		}
		catch (Exception e)
		{
			// error processing statement
			// return statement error - status error
			intResponseCode = EBackendResponsStatusCode.INTERNAL_ERROR;
			strResponseBody = e.getMessage();
			CBackendUtils.writeResponseInExchange(t, intResponseCode, strResponseBody);
		}
		finally
		{
			if (preparedStatementInsert != null)
			{
				try 
				{
					preparedStatementInsert.close();
				} 
				catch (SQLException e) 
				{
					e.printStackTrace();
				}
			}
			try 
			{
				sqlConnection.close();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
	}
}
