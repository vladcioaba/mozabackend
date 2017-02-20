package com.mozaicgames.backend;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;
import javax.sql.RowSet;

import com.mozaicgames.utils.AdvancedEncryptionStandard;

public class CBackendSessionManager 
{
	private final DataSource 								mSqlDataSource;
	private final String 									mEncriptionCode; 
	private final ConcurrentMap<String, CBackendSession> 	mActiveSessions;
	
	public CBackendSessionManager(DataSource sqlDataSource, String encriptionCode) throws Exception
	{
		mSqlDataSource = sqlDataSource;
		mEncriptionCode = encriptionCode;
		mActiveSessions = new ConcurrentHashMap<>();
		if (mSqlDataSource == null || mEncriptionCode == null)
		{
			throw new Exception("Invalid argument");
		}
	}
	
	
	public boolean isSessionValid(String sessionKey)
	{
		CBackendSession session = getActiveSession(sessionKey);
		if (session != null)
		{
			return true;
		}
		return false;
	}
	
	public CBackendSession getActiveSession(String sessionKey)
	{
		return mActiveSessions.get(sessionKey);
	}
	
	public CBackendSession createSession(long deviceId, int userId, String remoteAddress)
	{
		Connection sqlConnection = null;
		Statement sqlStatement = null;
		try 
		{
			sqlConnection = mSqlDataSource.getConnection();
			sqlConnection.setAutoCommit(false);
			
			sqlStatement = sqlConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_UPDATABLE);
			
			// find the session in the database first
			ResultSet response = sqlStatement.executeQuery("select ( session_id, session_creation_date, session_espire_date ) "
							   + "from sessions where deviceId = '" + deviceId + "' and userId = ' " + userId + " '");
			
			final Date currentDate = new Date();
			boolean createNewSession = false;
			long sessionId = 0;
			Date dateCreated = null;
			Date dateExpires = null;
			if (response != null && response.next())
			{
				sessionId = response.getLong(1);
				dateCreated = response.getDate(2);
				dateExpires = response.getDate(3);
				
				if (currentDate.compareTo(dateExpires) == 1)
				{
					// this session expired
					createNewSession = true;
				}
				else
				{
					AdvancedEncryptionStandard encripter = new AdvancedEncryptionStandard(mEncriptionCode, "AES");
					final String newSessionKey = encripter.encrypt(String.valueOf(sessionId));
					CBackendSession newSession = new CBackendSession(sessionId, userId, deviceId, newSessionKey, Calendar.getInstance().getTimeInMillis());
					mActiveSessions.put(newSessionKey, newSession);
					return newSession;
				}
			}
			else
			{
				// there is no session stored in the backend
				// create new session data
				dateCreated = currentDate;
				
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(currentDate);
				calendar.add(Calendar.DATE, 14);
		        dateExpires = calendar.getTime();
		        createNewSession = true;
			}			
			
			// if there is no session in the database or the session there is expired
			// create new session
			if (createNewSession)
			{
				final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				// create session key
				sqlStatement.executeUpdate("insert into sessions ( user_id, device_id, session_creation_date, session_expire_date, session_ip ) values "
							 + "( '" + userId + "',"
					 		 + "  '" + deviceId + "',"
					 		 + "  '" + dateFormat.format(dateCreated) + "',"
					 	     + "  '" + dateFormat.format(dateExpires) + "',"
		 		 		     + "  '" + remoteAddress + "' );", Statement.RETURN_GENERATED_KEYS);				

				ResultSet restultLastInsert = sqlStatement.getGeneratedKeys();
				if (restultLastInsert.next())
				{
					sessionId = restultLastInsert.getLong(1);
					sqlConnection.commit();
				}
				
				AdvancedEncryptionStandard encripter = new AdvancedEncryptionStandard(mEncriptionCode, "AES");
				final String newSessionKey = encripter.encrypt(String.valueOf(sessionId));
				CBackendSession newSession = new CBackendSession(sessionId, userId, deviceId, newSessionKey, Calendar.getInstance().getTimeInMillis());
				mActiveSessions.put(newSessionKey, newSession);
				return newSession;
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
			try 
			{
				sqlStatement.close();
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
			try 
			{
				sqlConnection.close();
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
}
