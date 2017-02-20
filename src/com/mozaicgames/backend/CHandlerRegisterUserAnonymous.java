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

public class CHandlerRegisterUserAnonymous extends CBackendRequestHandler 
{

	private final String 							mEncriptionCode;
	
	private final String mKeyDeviceToken			= "device_token";
	private final String mKeyClientVersion			= "client_version";

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
		String deviceToken = null;
		try 
		{
			JSONObject jsonRequestBody = new JSONObject(strRequestBody);
			clientVersion = jsonRequestBody.getString(mKeyClientVersion);
			deviceToken = jsonRequestBody.getString(mKeyDeviceToken);
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
			AdvancedEncryptionStandard encripter = new AdvancedEncryptionStandard(mEncriptionCode, "AES");
			// decrypt device id from token
			final long deviceId = Long.parseLong(encripter.decrypt(deviceToken));
			
			sqlStatement = sqlConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_UPDATABLE);
					
			// create new user
			int affectedRows = sqlStatement.executeUpdate("insert into users values ();", Statement.RETURN_GENERATED_KEYS);			
			if (affectedRows == 0)
			{
				throw new JSONException("Nothing updated in database!");
			}
			
			int newUserId = 0;
			ResultSet restultLastInsert = sqlStatement.getGeneratedKeys();
			if (restultLastInsert.next())
			{
				newUserId = restultLastInsert.getInt(1);
			}
			
			// update user id in device
			affectedRows = sqlStatement.executeUpdate("update devices set device_user_id = '" + newUserId + "', "						 
						 + " where device_id = '" + deviceId + "';");
			if (affectedRows == 0)
			{
				throw new JSONException("Nothing updated in database!");
			}
			sqlConnection.commit();
			
			String userToken = encripter.encrypt(String.valueOf(newUserId));
			
			JSONObject jsonResponse = new JSONObject();
			jsonResponse.put("user_token", userToken);
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
