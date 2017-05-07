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

public class CRequestExecutorUpdateUserWalletData extends CBackendRequestExecutor
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
			
			int numFieldsUpdated = 0;
			CSqlBuilderUpdate sqlBuilderUpdate = new CSqlBuilderUpdate()
					.table(CDatabaseKeys.mKeyTableUsersWalletDataTableName)
					.where(CDatabaseKeys.mKeyTableUsersWalletDataUserId + "=" + parameters.getUserId());
		
			if (jsonData.has(CRequestKeys.mKeyUserWalletDataCreditsNum))
			{
				final int deviceCreditsNum = jsonData.getInt(CRequestKeys.mKeyUserWalletDataCreditsNum);
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableUsersWalletDataCreditsNum, Integer.toString(deviceCreditsNum));
				numFieldsUpdated++;
			}
			
			if (jsonData.has(CRequestKeys.mKeyUserWalletDataJokersNum))
			{
				final int deviceJockersNum = jsonData.getInt(CRequestKeys.mKeyUserWalletDataJokersNum);
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableUsersWalletDataJokersNum, Integer.toString(deviceJockersNum));
				numFieldsUpdated++;
			}
			
			if (jsonData.has(CRequestKeys.mKeyUserWalletDataLivesNum))
			{
				final int deviceLivesNum = jsonData.getInt(CRequestKeys.mKeyUserWalletDataLivesNum);
				sqlBuilderUpdate.set(CDatabaseKeys.mKeyTableUsersWalletDataLivesNum, Integer.toString(deviceLivesNum));
				numFieldsUpdated++;
			}
			
			if (numFieldsUpdated > 0)
			{
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
			else
			{
				return toJSONObject(EBackendResponsStatusCode.STATUS_OK, null);
			}
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
