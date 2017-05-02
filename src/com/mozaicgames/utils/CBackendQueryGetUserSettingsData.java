package com.mozaicgames.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.json.JSONObject;

import com.mozaicgames.core.CBackendRequestException;
import com.mozaicgames.core.EBackendResponsStatusCode;
import com.mozaicgames.executors.CDatabaseKeys;
import com.mozaicgames.executors.CRequestKeys;

public class CBackendQueryGetUserSettingsData 
{
	private CBackendQueryGetUserSettingsData() 
	{
		
	}
	
	static public JSONObject getUserGameData(int userId, DataSource dataSource) throws CBackendRequestException
	{
		Connection sqlConnection = null;
		PreparedStatement preparedStatementSelect = null;
		try {
			sqlConnection = dataSource.getConnection();
			sqlConnection.setAutoCommit(false);

			final CSqlBuilderSelect sqlBuilderSelect = new CSqlBuilderSelect()
					.from(CDatabaseKeys.mKeyTableUsersSettingsTableName)
					.column(CDatabaseKeys.mKeyTableUsersSettingsMagnetOn)
					.column(CDatabaseKeys.mKeyTableUsersSettingsLeftHandedOn)
					.column(CDatabaseKeys.mKeyTableUsersSettingsMusicOn)
					.column(CDatabaseKeys.mKeyTableUsersSettingsSfxOn)
					.where(CDatabaseKeys.mKeyTableUsersSettingsUserId + "=" + userId);

			// find the session in the database first
			final String strQuerySelect = sqlBuilderSelect.toString();
			preparedStatementSelect = sqlConnection.prepareStatement(strQuerySelect);

			ResultSet response = preparedStatementSelect.executeQuery();
			if (response != null && response.next()) 
			{
				final short dataMagnetOn = response.getShort(1);
				final short dataLeftHandedOn = response.getShort(2);
				final short dataMusicOn = response.getShort(3);
				final short dataSfxOn = response.getShort(4);
				
				JSONObject responseUserData = new JSONObject();
				
				responseUserData.put(CRequestKeys.mKeyUserSettingsMagnetOn, dataMagnetOn);
				responseUserData.put(CRequestKeys.mKeyUserSettingsLeftHandedOn, dataLeftHandedOn);
				responseUserData.put(CRequestKeys.mKeyUserSettingsMusicOn, dataMusicOn);
				responseUserData.put(CRequestKeys.mKeyUserSettingsSfxOn, dataSfxOn);
				
				return responseUserData;
			}
		} 
		catch (Exception e) 
		{
			// could not get a connection
			// return database connection error - status retry
			System.err.println("Register handler Null pointer exception: " + e.getMessage());
		} 
		finally 
		{
			if (preparedStatementSelect != null) 
			{
				try 
				{
					preparedStatementSelect.close();
					preparedStatementSelect = null;
				} 
				catch (SQLException e) 
				{
					System.err.println("Ex: " + e.getMessage());
				}
			}

			try 
			{
				sqlConnection.close();
				sqlConnection = null;
			} 
			catch (SQLException e) 
			{
				System.err.println("Ex: " + e.getMessage());
			}
		}
		throw new CBackendRequestException(EBackendResponsStatusCode.INTERNAL_ERROR, "Internal error");
	}
}
