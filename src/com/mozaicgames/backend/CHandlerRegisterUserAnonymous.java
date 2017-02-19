package com.mozaicgames.backend;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.sql.DataSource;

import org.json.JSONException;
import org.json.JSONObject;

import com.mozaicgames.backend.CBackendRequestHandler;
import com.mozaicgames.utils.AdvancedEncryptionStandard;
import com.mozaicgames.utils.Utils;
import com.sun.net.httpserver.HttpExchange;

public class CHandlerRegisterUserAnonymous extends CBackendRequestHandler 
{

	private String mEncriptionCode			= null;
	
	private String mKeyDeviceModel			= "device_model";
	private String mKeyDeviceOsVersion 		= "device_os_version";
	private String mKeyDevicePlatform		= "device_platform";
	private String mKeyClientVersion		= "client_version";

	public CHandlerRegisterUserAnonymous(DataSource sqlDataSource, String encriptionConde, String minClientVersionAllowed) throws Exception
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
			
			if (jsonRequestBody.has(mKeyDeviceModel) == false ||
				jsonRequestBody.has(mKeyDeviceOsVersion) == false ||
				jsonRequestBody.has(mKeyDevicePlatform) == false ||
				jsonRequestBody.has(mKeyClientVersion) == false)
			{
				throw new JSONException("Missing variables");
			}
		
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
			long newDeviceId = 0;
			ResultSet restultLastInsert = sqlStatement.executeQuery("select device_id from devices order by device_id desc limit 1");
			if (restultLastInsert.next())
			{
				newDeviceId = restultLastInsert.getLong(1) + 1;
			}
			
			AdvancedEncryptionStandard encripter = new AdvancedEncryptionStandard(mEncriptionCode, "AES");
			String newUUID = encripter.encrypt(String.valueOf(newDeviceId));
			String remoteAddress = t.getRemoteAddress().getAddress().getHostAddress();
			
			// create new device
			int affectedRows = sqlStatement.executeUpdate("insert into devices ( device_token, device_model, device_os_version, device_platform, device_app_version, device_ip ) values "
							 + "('" + newUUID + "' ,"
							 + " '" + deviceModel + "' ,"
							 + " '" + deviceOsVerrsion + "' ,"
							 + " '" + devicePlatform + "' ,"
							 + " '" + clientVersion + "' ,"
							 + " '" + remoteAddress + "');");		
			
			if (affectedRows == 0)
			{
				throw new JSONException("Nothing updated in database!");
			}

			// create new user
			affectedRows = sqlStatement.executeUpdate("insert into users values ();", Statement.RETURN_GENERATED_KEYS);			
			if (affectedRows == 0)
			{
				throw new JSONException("Nothing updated in database!");
			}
			
			int newUserId = 0;
			restultLastInsert = sqlStatement.getGeneratedKeys();
			if (restultLastInsert.next())
			{
				newUserId = restultLastInsert.getInt(1);
			}
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date currentDate = new Date();
			
			Calendar cal = Calendar.getInstance();
	        cal.setTime(currentDate);
	        cal.add(Calendar.DATE, 14);
	        Date expireDate = cal.getTime();
			
			// create session key
			affectedRows = sqlStatement.executeUpdate("insert into sessions ( user_id, device_id, session_creation_date, session_expire_date, session_ip ) values "
						 + "( '" + newUserId + "',"
				 		 + "  '" + newDeviceId + "',"
				 		 + "  '" + dateFormat.format(currentDate) + "',"
				 	     + "  '" + dateFormat.format(expireDate) + "',"
	 		 		     + "  '" + remoteAddress + "' );", Statement.RETURN_GENERATED_KEYS);
			if (affectedRows == 0)
			{
				throw new JSONException("Nothing updated in database!");
			}
			long newSessionId = 0;
			restultLastInsert = sqlStatement.getGeneratedKeys();
			if (restultLastInsert.next())
			{
				newSessionId = restultLastInsert.getLong(1);
			}			
			sqlConnection.commit();
			
			// update user id in device
			affectedRows = sqlStatement.executeUpdate("update devices set device_user_id = '" + newUserId + "'"
						 + " where device_id = '" + newDeviceId + "';");
			if (affectedRows == 0)
			{
				throw new JSONException("Nothing updated in database!");
			}
			sqlConnection.commit();
						
			String newSessionKey = encripter.encrypt(String.valueOf(newSessionId));
			
			JSONObject jsonResponse = new JSONObject();
			jsonResponse.put("device_token", newUUID);
			jsonResponse.put("session_key", newSessionKey);
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
