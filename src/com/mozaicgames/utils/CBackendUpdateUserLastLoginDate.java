package com.mozaicgames.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.sql.DataSource;

import com.mozaicgames.core.EBackendResponsStatusCode;
import com.mozaicgames.executors.CDatabaseKeys;

public class CBackendUpdateUserLastLoginDate implements IBackendQueryExecuter 
{
	
	private final DataSource		mDataSource;
	private final int				mUserId;
	
	public CBackendUpdateUserLastLoginDate(DataSource dataSource, int userId)
	{
		mDataSource = dataSource;
		mUserId = userId;
	}
	
	public CBackendQueryResponse execute()
	{
		EBackendResponsStatusCode intResponseCode = EBackendResponsStatusCode.STATUS_OK;
		String strResponseBody = "";
		
		PreparedStatement preparedStatementUpdateLastLoginDate = null;
		Connection sqlConnection = null;
		
		try 
		{
			sqlConnection = mDataSource.getConnection();
			final Timestamp updateTime = new Timestamp(System.currentTimeMillis());
			
			final CSqlBuilderUpdate sqlBuilderUpdate = new CSqlBuilderUpdate()
					.set(CDatabaseKeys.mKeyTableUsersUserLastLoginDate, updateTime.toString())
					.table(CDatabaseKeys.mKeyTableUsersTableName)
					.where(CDatabaseKeys.mKeyTableUsersUserId + "=" + mUserId);		
			
			final String strQuerySelectDevice = sqlBuilderUpdate.toString();
			preparedStatementUpdateLastLoginDate = sqlConnection.prepareStatement(strQuerySelectDevice);
			int resultSetDeviceQuery = preparedStatementUpdateLastLoginDate.executeUpdate();
			if (resultSetDeviceQuery == 0)
			{
				// the device is invalid
				intResponseCode = EBackendResponsStatusCode.INVALID_TOKEN_USER;
				strResponseBody =  "Invalid user token!";				
			}
			preparedStatementUpdateLastLoginDate.close();
			preparedStatementUpdateLastLoginDate = null;
		}
		catch (Exception e) 
		{
			// error processing statement
			// return statement error - status error
			intResponseCode = EBackendResponsStatusCode.INTERNAL_ERROR;
			strResponseBody = "Unable to retrive device data!";
		}
		finally
		{
			if (preparedStatementUpdateLastLoginDate != null)
			{
				try  
				{ 
					preparedStatementUpdateLastLoginDate.close(); 
				}  
				catch (SQLException e)  
				{ 
					System.err.println(e.getMessage()); 
				}
				preparedStatementUpdateLastLoginDate = null;
			}
			
			if (sqlConnection != null)
			{
				try  
				{ 
					sqlConnection.close();
				}  
				catch (SQLException e)  
				{ 
					System.err.println(e.getMessage()); 
				}
				sqlConnection = null;
			}
		}
		
		return new CBackendQueryResponse(intResponseCode, strResponseBody);
	}
	
}