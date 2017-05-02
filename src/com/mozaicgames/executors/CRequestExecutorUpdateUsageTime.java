package com.mozaicgames.executors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.json.JSONObject;

import com.mozaicgames.core.CBackendRequestException;
import com.mozaicgames.core.CBackendRequestExecutor;
import com.mozaicgames.core.CBackendRequestExecutorParameters;
import com.mozaicgames.core.EBackendResponsStatusCode;
import com.mozaicgames.utils.CSqlBuilderUpdate;

public class CRequestExecutorUpdateUsageTime extends CBackendRequestExecutor
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
			final int deviceUsageTime = jsonData.getInt(CRequestKeys.mKeyDeviceUsageTime);

			sqlConnection = parameters.getSqlDataSource().getConnection();
			sqlConnection.setAutoCommit(false);

//			CSqlBuilderSelect sqlBuilderSelect = new CSqlBuilderSelect()
//				.from(CDatabaseKeys.mKeyTableDevicesTableName)
//				.column(CDatabaseKeys.mKeyTableDevicesUsageTime)
//				.where("");
//			
//			final String strQuerySelect = sqlBuilderSelect.toString();
//			sqlConnection.pre
			
			CSqlBuilderUpdate sqlBuilderUpdate = new CSqlBuilderUpdate()
					.table(CDatabaseKeys.mKeyTableDevicesTableName)
					.set(CDatabaseKeys.mKeyTableDevicesUpdateDate, new Timestamp(System.currentTimeMillis()).toString())
					.update(CDatabaseKeys.mKeyTableDevicesUsageTime, Integer.toString(deviceUsageTime), "+")
					.where(CDatabaseKeys.mKeyTableDevicesDeviceId + "=" + parameters.getDeviceId());
			
			final String strQueryUpdate = sqlBuilderUpdate.toString(); 
			preparedStatementUpdate = sqlConnection.prepareStatement(strQueryUpdate, PreparedStatement.RETURN_GENERATED_KEYS);
			
			int affectedRows = preparedStatementUpdate.executeUpdate();
			if (affectedRows == 0)
			{
				throw new Exception("Nothing updated in database!");
			}		
			
			sqlConnection.commit();
			return toJSONObject(EBackendResponsStatusCode.STATUS_OK, null);
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
