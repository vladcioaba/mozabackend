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

public class CBackendQuerryGetUserGameData 
{
	private CBackendQuerryGetUserGameData() 
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
					.from(CDatabaseKeys.mKeyTableUsersdata)
					.column(CDatabaseKeys.mKeyTableUsersdataDataMagnetOn)
					.column(CDatabaseKeys.mKeyTableUsersdataDatLeftHandedOn)
					.column(CDatabaseKeys.mKeyTableUsersdataDataMusicOn)
					.column(CDatabaseKeys.mKeyTableUsersdataDataSfxOn)
					.column(CDatabaseKeys.mKeyTableUsersdataDataCreditsNum)
					.column(CDatabaseKeys.mKeyTableUsersdataDataJockersNum)
					.column(CDatabaseKeys.mKeyTableUsersdataDataLivesNum)
					.where(CDatabaseKeys.mKeyTableUsersdataUserId + "=" + userId);

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
				final int dataCreditsNum = response.getInt(5);
				final int dataJockersNum = response.getInt(6);
				final int dataLivesNum = response.getInt(7);
				
				JSONObject responseUserData = new JSONObject();
				
				responseUserData.put(CRequestKeys.mKeyUserSettingsDataMagnetOn, dataMagnetOn);
				responseUserData.put(CRequestKeys.mKeyUserSettingsDataLeftHandedOn, dataLeftHandedOn);
				responseUserData.put(CRequestKeys.mKeyUserSettingsDataMusicOn, dataMusicOn);
				responseUserData.put(CRequestKeys.mKeyUserSettingsDataSfxOn, dataSfxOn);
				responseUserData.put(CRequestKeys.mKeyUserGameDataCreditsNum, dataCreditsNum);
				responseUserData.put(CRequestKeys.mKeyUserGameDataJockersNum, dataJockersNum);
				responseUserData.put(CRequestKeys.mKeyUserGameDataLivesNum, dataLivesNum);
				
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
