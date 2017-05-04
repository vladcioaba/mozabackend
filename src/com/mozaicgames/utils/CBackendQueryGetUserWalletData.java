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

public class CBackendQueryGetUserWalletData 
{
	private CBackendQueryGetUserWalletData() 
	{
		
	}
	
	static public JSONObject getUserGameData(int userId, String devicePlatform, DataSource dataSource) throws CBackendRequestException
	{
		Connection sqlConnection = null;
		PreparedStatement preparedStatementSelect = null;
		try {
			sqlConnection = dataSource.getConnection();
			sqlConnection.setAutoCommit(false);

			final CSqlBuilderSelect sqlBuilderSelect = new CSqlBuilderSelect()
					.from(CDatabaseKeys.mKeyTableUsersWalletDataTableName)
					.column(CDatabaseKeys.mKeyTableUsersWalletDataCreditsNum)
					.column(CDatabaseKeys.mKeyTableUsersWalletDataJockersNum)
					.column(CDatabaseKeys.mKeyTableUsersWalletDataLivesNum)
					.where(CDatabaseKeys.mKeyTableUsersWalletDataUserId + "='" + userId + "' and " + CDatabaseKeys.mKeyTableUsersWalletDataDevicePlatform + "='" + devicePlatform + "'");

			// find the session in the database first
			final String strQuerySelect = sqlBuilderSelect.toString();
			preparedStatementSelect = sqlConnection.prepareStatement(strQuerySelect);

			ResultSet response = preparedStatementSelect.executeQuery();
			if (response != null && response.next()) 
			{
				final int dataCreditsNum = response.getInt(1);
				final int dataJockersNum = response.getInt(2);
				final int dataLivesNum = response.getInt(3);

				JSONObject responseUserData = new JSONObject();
				responseUserData.put(CRequestKeys.mKeyUserWalletDataCreditsNum, dataCreditsNum);
				responseUserData.put(CRequestKeys.mKeyUserWalletDataJockersNum, dataJockersNum);
				responseUserData.put(CRequestKeys.mKeyUserWalletDataLivesNum, dataLivesNum);
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
