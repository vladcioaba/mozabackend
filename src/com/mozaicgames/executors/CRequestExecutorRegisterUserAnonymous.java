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
		
			final String defaultValueUserLevel = "1";
			final String defaultValueUserXp = "0";
			final String defaultValueUserTrophies = "1000";
			
			final CSqlBuilderInsert sqlBuilderInsertNewUser = new CSqlBuilderInsert()
					.into(CDatabaseKeys.mKeyTableUsersTableName)
					.value(CDatabaseKeys.mKeyTableUsersUserLevel, defaultValueUserLevel)
					.value(CDatabaseKeys.mKeyTableUsersUserXp, defaultValueUserXp)
					.value(CDatabaseKeys.mKeyTableUsersUserTrophies, defaultValueUserTrophies)
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
			final String defaultValueMusicOn = "0";
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
			final String defaultValueTokensNum = "5";
			
			final String strQueryInsertUserSettings = sqlBuilderInsertNewUserSettings.toString();
			preparedStatementInsertUserSettings = sqlConnection.prepareStatement(strQueryInsertUserSettings, PreparedStatement.RETURN_GENERATED_KEYS);
			affectedRows = preparedStatementInsertUserSettings.executeUpdate();
			if (affectedRows == 0)
			{
				throw new Exception("Nothing updated in database!");
			}	
			
			final CSqlBuilderInsert sqlBuilderInsertNewUserGameData = new CSqlBuilderInsert()
					.into(CDatabaseKeys.mKeyTableUsersWalletDataTableName)
					.value(CDatabaseKeys.mKeyTableUsersWalletDataUserId, Integer.toString(newUserId))
					.value(CDatabaseKeys.mKeyTableUsersWalletDataDevicePlatform, devicePlatform)
					.value(CDatabaseKeys.mKeyTableUsersWalletDataCreditsNum, defaultValueCreditsNum)
					.value(CDatabaseKeys.mKeyTableUsersWalletDataJokersNum, defaultValueJockersNum)
					.value(CDatabaseKeys.mKeyTableUsersWalletDataTokensNum, defaultValueTokensNum);
			
			final String strQueryInsertUserData = sqlBuilderInsertNewUserGameData.toString();
			preparedStatementInsertUserData = sqlConnection.prepareStatement(strQueryInsertUserData, PreparedStatement.RETURN_GENERATED_KEYS);
			affectedRows = preparedStatementInsertUserData.executeUpdate();
			if (affectedRows == 0)
			{
				throw new Exception("Nothing updated in database!");
			}			

			JSONObject responseUserSettingsData = new JSONObject();
			responseUserSettingsData.put(CRequestKeys.mKeyUserSettingsMagnetOn, defaultValueMagnetOn);
			responseUserSettingsData.put(CRequestKeys.mKeyUserSettingsLeftHandedOn, defaultValueLeftHandedOn);
			responseUserSettingsData.put(CRequestKeys.mKeyUserSettingsMusicOn, defaultValueMusicOn);
			responseUserSettingsData.put(CRequestKeys.mKeyUserSettingsSfxOn, defaultValueSfxOn);
			
			JSONObject responseUserWalletData = new JSONObject();
			responseUserWalletData.put(CRequestKeys.mKeyUserWalletDataCreditsNum, defaultValueCreditsNum);
			responseUserWalletData.put(CRequestKeys.mKeyUserWalletDataJokersNum, defaultValueJockersNum);
			responseUserWalletData.put(CRequestKeys.mKeyUserWalletDataTokensNum, defaultValueTokensNum);
			
			JSONObject responseUserGameData = new JSONObject();
			responseUserGameData.put(CRequestKeys.mKeyUserDataLevel, defaultValueUserLevel);
			responseUserGameData.put(CRequestKeys.mKeyUserDataXp, defaultValueUserXp);
			responseUserGameData.put(CRequestKeys.mKeyUserDataTrophies, defaultValueUserTrophies);
			
			JSONObject jsonResponse = new JSONObject();
			String userToken = encripter.encrypt(String.valueOf(newUserId));			
			jsonResponse.put(CRequestKeys.mKeyClientUserToken, userToken);
			jsonResponse.put(CRequestKeys.mKeyClientUserSettingsData, responseUserSettingsData);
			jsonResponse.put(CRequestKeys.mKeyClientUserWalletData, responseUserWalletData);
			jsonResponse.put(CRequestKeys.mKeyClientUserGameData, responseUserGameData);
			
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
