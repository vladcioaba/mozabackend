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
import com.mozaicgames.utils.CBackendQueryResponse;
import com.mozaicgames.utils.CBackendQueryValidateDevice;
import com.mozaicgames.utils.CBackendUtils;
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
		String strRequestBody = CBackendUtils.getStringFromStream(t.getRequestBody());
		
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
		
		CBackendAdvancedEncryptionStandard encripter = new CBackendAdvancedEncryptionStandard(mEncriptionCode, "AES");
		long deviceId = 0;
		try 
		{
			// decrypt device id from token
			deviceId = Long.parseLong(encripter.decrypt(deviceToken));
		}
		catch (Exception ex)
		{
			// error processing statement
			// return statement error - status error
			intResponseCode = EBackendResponsStatusCode.INTERNAL_ERROR;
			strResponseBody = ex.getMessage();
			CBackendUtils.writeResponseInExchange(t, intResponseCode, strResponseBody);
			return;
		}
		
		CBackendQueryValidateDevice validatorDevice = new CBackendQueryValidateDevice(getDataSource(), deviceId);
		CBackendQueryResponse validatorResponse = validatorDevice.execute();
		
		if (validatorResponse.getCode() != EBackendResponsStatusCode.STATUS_OK)
		{
			CBackendUtils.writeResponseInExchange(t, validatorResponse.getCode(), validatorResponse.getBody());
			return;
		}
		
		Connection sqlConnection = null;
		PreparedStatement preparedStatementInsert = null;
		PreparedStatement preparedStatementUpdate = null;
		
		try 
		{
			sqlConnection = getDataSource().getConnection();
			sqlConnection.setAutoCommit(false);
		
			String strQueryInsert = "insert into users ( user_creation_date ) values ( ? );";
			preparedStatementInsert = sqlConnection.prepareStatement(strQueryInsert, PreparedStatement.RETURN_GENERATED_KEYS);
			preparedStatementInsert.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			int affectedRows = preparedStatementInsert.executeUpdate();
			if (affectedRows == 0)
			{
				throw new Exception("Nothing updated in database!");
			}
			
			int newUserId = 0;
			ResultSet restultLastInsert = preparedStatementInsert.getGeneratedKeys();
			if (restultLastInsert.next())
			{
				newUserId = restultLastInsert.getInt(1);
			}
			restultLastInsert.close();
			preparedStatementInsert.close();
			preparedStatementInsert = null;
			
			// update user id in device
			String strQueryUpdate = "update devices set device_user_id=? where device_id=?;";
			preparedStatementUpdate =  sqlConnection.prepareStatement(strQueryUpdate);
			preparedStatementUpdate.setInt(1, newUserId);
			preparedStatementUpdate.setLong(2, deviceId);
			
			affectedRows = preparedStatementUpdate.executeUpdate();
			if (affectedRows == 0)
			{
				throw new  Exception("Nothing updated in database!");
			}
			preparedStatementUpdate.close();
			preparedStatementUpdate = null;
			
			String userToken = encripter.encrypt(String.valueOf(newUserId));			
			JSONObject jsonResponse = new JSONObject();
			jsonResponse.put("user_token", userToken);
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
			
			if (preparedStatementUpdate != null)
			{
				try  
				{ 
					preparedStatementUpdate.close(); 
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
