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

public class CBackendQueryGetUserGameData 
{
	private CBackendQueryGetUserGameData() 
	{
		
	}
	
	static public JSONObject getUserData(int userId, DataSource dataSource) throws CBackendRequestException
	{
		Connection sqlConnection = null;
		PreparedStatement preparedStatementSelect = null;
		try {
			sqlConnection = dataSource.getConnection();
			sqlConnection.setAutoCommit(false);

			CSqlBuilderSelect sqlBuilderSelect = new CSqlBuilderSelect()
					.from(CDatabaseKeys.mKeyTableUsersTableName)
					.column(CDatabaseKeys.mKeyTableUsersUserLevel)
					.column(CDatabaseKeys.mKeyTableUsersUserXp)
					.column(CDatabaseKeys.mKeyTableUsersUserTrophies)
					.where(CDatabaseKeys.mKeyTableUsersUserId + "=" + userId);
			
			// find the session in the database first
			final String strQuerySelect = sqlBuilderSelect.toString();
			preparedStatementSelect = sqlConnection.prepareStatement(strQuerySelect);

			ResultSet response = preparedStatementSelect.executeQuery();
			if (response != null && response.next()) 
			{
				final int dataUserLevel = response.getInt(1);
				final int dataUserXp = response.getInt(2);
				final int dataUserTrophies = response.getInt(3);
				final int dataUserLevelMaxXp = (int) (100 * Math.pow(dataUserLevel, 2) - 1);
				
				JSONObject responseUserData = new JSONObject();
				responseUserData.put(CRequestKeys.mKeyUserDataLevel, dataUserLevel);
				responseUserData.put(CRequestKeys.mKeyUserDataXp, dataUserXp);
				responseUserData.put(CRequestKeys.mKeyUserDataLevelMaxXp, dataUserLevelMaxXp);
				responseUserData.put(CRequestKeys.mKeyUserDataTrophies, dataUserTrophies);
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
