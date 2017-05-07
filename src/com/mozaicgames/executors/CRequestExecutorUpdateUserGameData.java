package com.mozaicgames.executors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.json.JSONObject;

import com.mozaicgames.core.CBackendRequestException;
import com.mozaicgames.core.CBackendRequestExecutor;
import com.mozaicgames.core.CBackendRequestExecutorParameters;
import com.mozaicgames.core.EBackendResponsStatusCode;
import com.mozaicgames.utils.CSqlBuilderUpdate;

public class CRequestExecutorUpdateUserGameData extends CBackendRequestExecutor
{
	@Override
	public boolean isSessionTokenValidationNeeded() 
	{ 
		return true; 
	}
	
	@Override
	public JSONObject execute(JSONObject jsonData, CBackendRequestExecutorParameters parameters) throws CBackendRequestException
	{
		Connection sqlConnection = null;
		PreparedStatement preparedStatementUpdate = null;
		
		try 
		{
			sqlConnection = parameters.getSqlDataSource().getConnection();
			sqlConnection.setAutoCommit(false);
			
			CSqlBuilderUpdate sqlBuilderUpdate = new CSqlBuilderUpdate()
					.table(CDatabaseKeys.mKeyTableUsersTableName)
					.where(CDatabaseKeys.mKeyTableUsersTableName + "=" + parameters.getUserId());
			
			if (jsonData.has(CRequestKeys.mKeyUserDataLevel))
			{
				final int deviceUserLevel = jsonData.getInt(CRequestKeys.mKeyUserDataLevel);
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableUsersUserLevel, Integer.toString(deviceUserLevel));
			}
			
			if (jsonData.has(CRequestKeys.mKeyUserDataTrophies))
			{
				final int deviceUserTrophies = jsonData.getInt(CRequestKeys.mKeyUserDataTrophies);
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableUsersUserTrophies, Integer.toString(deviceUserTrophies));
			}
			
			if (jsonData.has(CRequestKeys.mKeyUserDataXp))
			{
				final int deviceDataXp = jsonData.getInt(CRequestKeys.mKeyUserDataXp);
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableUsersUserXp, Integer.toString(deviceDataXp));
			}
			
			final String strQueryUpdate = sqlBuilderUpdate.toString(); 
			preparedStatementUpdate = sqlConnection.prepareStatement(strQueryUpdate, PreparedStatement.RETURN_GENERATED_KEYS);
			
			int affectedRows = preparedStatementUpdate.executeUpdate();
			if (affectedRows == 0)
			{
				throw new Exception("Nothing updated in database!");
			}	
			
			JSONObject jsonResponse = new JSONObject();
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
			if (preparedStatementUpdate != null)
			{
				try 
				{
					preparedStatementUpdate.close();
				} 
				catch (SQLException e) 
				{
					System.err.println(e.getMessage());
				}
			}
			try 
			{
				sqlConnection.close();
			} 
			catch (Exception e) 
			{
				System.err.println(e.getMessage());
			}
		}
	}
}
