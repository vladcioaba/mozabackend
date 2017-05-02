package com.mozaicgames.executors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.json.JSONException;
import org.json.JSONObject;

import com.mozaicgames.core.CBackendRequestException;
import com.mozaicgames.core.CBackendRequestExecutor;
import com.mozaicgames.core.CBackendRequestExecutorParameters;
import com.mozaicgames.core.EBackendResponsStatusCode;
import com.mozaicgames.utils.CBackendAdvancedEncryptionStandard;
import com.mozaicgames.utils.CBackendQueryResponse;
import com.mozaicgames.utils.CBackendQueryValidateDevice;
import com.mozaicgames.utils.CSqlBuilderInsert;
import com.mozaicgames.utils.CSqlBuilderUpdate;

public class CRequestExecutorRegisterUserAnonymous extends CBackendRequestExecutor
{
	public JSONObject execute(JSONObject jsonData, CBackendRequestExecutorParameters parameters) throws CBackendRequestException
	{
		String deviceToken = null;
		try 
		{
			deviceToken = jsonData.getString(CRequestKeys.mKeyClientDeviceToken);
		}		
		catch (JSONException e)
		{
			// bad input
			// return database connection error - status retry
			throw new CBackendRequestException(EBackendResponsStatusCode.INVALID_DATA, "Bad input data!");
		}
		
		final CBackendAdvancedEncryptionStandard encripter = parameters.getEncriptionStandard();
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
			throw new CBackendRequestException(EBackendResponsStatusCode.INTERNAL_ERROR, "Unable to validate tokens!");
		}
		
		final CBackendQueryValidateDevice validatorDevice = new CBackendQueryValidateDevice(parameters.getSqlDataSource(), deviceId);
		final CBackendQueryResponse validatorResponse = validatorDevice.execute();
		
		if (validatorResponse.getCode() != EBackendResponsStatusCode.STATUS_OK)
		{
			throw new CBackendRequestException(validatorResponse.getCode(), validatorResponse.getBody());
		}
		
		final String devicePlatform = validatorResponse.getBody();
		
		Connection sqlConnection = null;
		PreparedStatement preparedStatementInsertUser = null;
		PreparedStatement preparedStatementUpdateDevice = null;
		PreparedStatement preparedStatementInsertUserSettings = null;
		PreparedStatement preparedStatementInsertUserData = null;
		
		try 
		{
			sqlConnection = parameters.getSqlDataSource().getConnection();
			sqlConnection.setAutoCommit(false);
		
			final CSqlBuilderInsert sqlBuilderInsertNewUser = new CSqlBuilderInsert()
					.into(CDatabaseKeys.mKeyTableUsersTableName)
					.value(CDatabaseKeys.mKeyTableUsersUserCreationDate, new Timestamp(System.currentTimeMillis()).toString());
			
			final String strQueryInsertUser = sqlBuilderInsertNewUser.toString();
			preparedStatementInsertUser = sqlConnection.prepareStatement(strQueryInsertUser, PreparedStatement.RETURN_GENERATED_KEYS);
			int affectedRows = preparedStatementInsertUser.executeUpdate();
			if (affectedRows == 0)
			{
				throw new Exception("Nothing updated in database!");
			}
			
			int newUserId = 0;
			ResultSet restultLastInsert = preparedStatementInsertUser.getGeneratedKeys();
			if (restultLastInsert.next())
			{
				newUserId = restultLastInsert.getInt(1);
			}
			restultLastInsert.close();
			
			// update user id in device
			final CSqlBuilderUpdate sqlBuilderUpdate = new CSqlBuilderUpdate()
					.table(CDatabaseKeys.mKeyTableDevicesTableName)
					.set(CDatabaseKeys.mKeyTableDevicesUserId, Integer.toString(newUserId))
					.where(CDatabaseKeys.mKeyTableDevicesDeviceId + "=" + deviceId);
			
			final String strQueryUpdate = sqlBuilderUpdate.toString();
			preparedStatementUpdateDevice =  sqlConnection.prepareStatement(strQueryUpdate);
			
			affectedRows = preparedStatementUpdateDevice.executeUpdate();
			if (affectedRows == 0)
			{
				throw new  Exception("Nothing updated in database!");
			}
			
			final String defaultValueMagnetOn = "1";
			final String defaultValueLeftHandedOn = "0";
			final String defaultValueMusicOn = "1";
			final String defaultValueSfxOn = "1";
			
			final CSqlBuilderInsert sqlBuilderInsertNewUserSettings = new CSqlBuilderInsert()
					.into(CDatabaseKeys.mKeyTableUsersSettingsTableName)
					.value(CDatabaseKeys.mKeyTableUsersSettingsUserId, Integer.toString(newUserId))
					.value(CDatabaseKeys.mKeyTableUsersSettingsMagnetOn, defaultValueMagnetOn)
					.value(CDatabaseKeys.mKeyTableUsersSettingsLeftHandedOn, defaultValueLeftHandedOn)
					.value(CDatabaseKeys.mKeyTableUsersSettingsMusicOn, defaultValueMusicOn)
					.value(CDatabaseKeys.mKeyTableUsersSettingsSfxOn, defaultValueSfxOn);
			
			final String defaultValueCreditsNum = "100";
			final String defaultValueJockersNum = "5";
			final String defaultValueLivesNum = "5";
			final String defaultValueLivesMaxNum = "5";
			
			final String strQueryInsertUserSettings = sqlBuilderInsertNewUserSettings.toString();
			preparedStatementInsertUserSettings = sqlConnection.prepareStatement(strQueryInsertUserSettings, PreparedStatement.RETURN_GENERATED_KEYS);
			affectedRows = preparedStatementInsertUserSettings.executeUpdate();
			if (affectedRows == 0)
			{
				throw new Exception("Nothing updated in database!");
			}	
			
			final CSqlBuilderInsert sqlBuilderInsertNewUserGameData = new CSqlBuilderInsert()
					.into(CDatabaseKeys.mKeyTableUsersDataTableName)
					.value(CDatabaseKeys.mKeyTableUsersDataUserId, Integer.toString(newUserId))
					.value(CDatabaseKeys.mKeyTableUsersDataDevicePlatform, devicePlatform)
					.value(CDatabaseKeys.mKeyTableUsersDataCreditsNum, defaultValueCreditsNum)
					.value(CDatabaseKeys.mKeyTableUsersDataJockersNum, defaultValueJockersNum)
					.value(CDatabaseKeys.mKeyTableUsersDataLivesNum, defaultValueLivesNum)
					.value(CDatabaseKeys.mKeyTableUsersDataLivesMaxNum, defaultValueLivesMaxNum);
			
			final String strQueryInsertUserData = sqlBuilderInsertNewUserGameData.toString();
			preparedStatementInsertUserData = sqlConnection.prepareStatement(strQueryInsertUserData, PreparedStatement.RETURN_GENERATED_KEYS);
			affectedRows = preparedStatementInsertUserData.executeUpdate();
			if (affectedRows == 0)
			{
				throw new Exception("Nothing updated in database!");
			}			

			JSONObject responseUserSettings = new JSONObject();
			responseUserSettings.put(CRequestKeys.mKeyUserSettingsMagnetOn, defaultValueMagnetOn);
			responseUserSettings.put(CRequestKeys.mKeyUserSettingsLeftHandedOn, defaultValueLeftHandedOn);
			responseUserSettings.put(CRequestKeys.mKeyUserSettingsMusicOn, defaultValueMusicOn);
			responseUserSettings.put(CRequestKeys.mKeyUserSettingsSfxOn, defaultValueSfxOn);
			
			JSONObject responseUserData = new JSONObject();
			responseUserData.put(CRequestKeys.mKeyUserGameDataCreditsNum, defaultValueCreditsNum);
			responseUserData.put(CRequestKeys.mKeyUserGameDataJockersNum, defaultValueJockersNum);
			responseUserData.put(CRequestKeys.mKeyUserGameDataLivesNum, defaultValueLivesNum);
			responseUserData.put(CRequestKeys.mKeyUserGameDataLivesMaxNum, defaultValueLivesMaxNum);
			
			String userToken = encripter.encrypt(String.valueOf(newUserId));			
			JSONObject jsonResponse = new JSONObject();
			jsonResponse.put(CRequestKeys.mKeyClientUserToken, userToken);
			jsonResponse.put(CRequestKeys.mKeyClientUserSettingsData, responseUserSettings);
			jsonResponse.put(CRequestKeys.mKeyClientUserGameData, responseUserData);
			sqlConnection.commit();
			return toJSONObject(EBackendResponsStatusCode.STATUS_OK, jsonResponse);
		}
		catch (Exception e)
		{
			// error processing statement
			// return statement error - status error
			throw new CBackendRequestException(EBackendResponsStatusCode.INTERNAL_ERROR, e.getMessage());
		}
		finally
		{			
			if (preparedStatementInsertUser != null)
			{
				try  
				{ 
					preparedStatementInsertUser.close(); 
					preparedStatementInsertUser = null;
				}  
				catch (SQLException e)  
				{ 
					System.err.println(e.getMessage()); 
				}
			}
			
			if (preparedStatementUpdateDevice != null)
			{
				try  
				{ 
					preparedStatementUpdateDevice.close();
					preparedStatementUpdateDevice = null;
				}  
				catch (SQLException e)  
				{ 
					System.err.println(e.getMessage()); 
				}
			}
			
			if (preparedStatementInsertUserData != null)
			{
				try  
				{ 
					preparedStatementInsertUserData.close();
					preparedStatementInsertUserData = null;
				}  
				catch (SQLException e)  
				{ 
					System.err.println(e.getMessage()); 
				}
			}
			
			if (preparedStatementInsertUserSettings != null)
			{
				try  
				{ 
					preparedStatementInsertUserSettings.close();
					preparedStatementInsertUserSettings = null;
				}  
				catch (SQLException e)  
				{ 
					System.err.println(e.getMessage()); 
				}
			}
			
			try 
			{
				sqlConnection.close();
				sqlConnection = null;
			} 
			catch (Exception e) 
			{
				System.err.println(e.getMessage());
			}
		}
	}
}
