package com.mozaicgames.core;

import javax.sql.DataSource;

import com.mozaicgames.utils.CBackendAdvancedEncryptionStandard;
import com.mozaicgames.utils.CBackendSessionManager;

public class CBackendRequestExecutorParameters 
{

	private	final String								mRemoteAddress;
	private final CBackendAdvancedEncryptionStandard	mEncriptionStandard;
	private final DataSource							mSqlDataSource;
	private final CBackendSessionManager 				mSessionManager;
	private final String								mClientCoreVersion;
	private final String								mClientAppVersion;
	private final int									mUserId;
	private final long									mDeviceId;
	private final long									mSessionId;
	private final String								mSessionToken;
	private final String								mDevicePlatform;
	
	public CBackendRequestExecutorParameters(final String remoteAddress,
											 final CBackendAdvancedEncryptionStandard encriptionStandard,
											 final DataSource sqlDataSource,
											 final CBackendSessionManager sessionManager,
											 final String clientCoreVersion,
											 final String clientAppVersion,
											 final int userId,
											 final long deviceId,
											 final long sessionId,
											 final String sessionToken,
											 final String devicePlatform)
	{
		mRemoteAddress = remoteAddress;
		mEncriptionStandard = encriptionStandard;
		mSqlDataSource = sqlDataSource;
		mSessionManager = sessionManager;
		mClientCoreVersion = clientCoreVersion;
		mClientAppVersion = clientAppVersion;
		mUserId = userId;
		mDeviceId = deviceId;
		mSessionId = sessionId;
		mSessionToken = sessionToken;
		mDevicePlatform = devicePlatform;
	}
	
	public String getRemoteAddress()
	{
		return mRemoteAddress;
	}
	
	public CBackendAdvancedEncryptionStandard getEncriptionStandard()
	{
		return mEncriptionStandard;
	}
	
	public DataSource getSqlDataSource()
	{
		return mSqlDataSource;
	}
	
	public CBackendSessionManager getSessionManager()
	{
		return mSessionManager;
	}
	
	public String getClientCoreVersion()
	{
		return mClientCoreVersion;
	}
	
	public String getClientAppVersion()
	{
		return mClientAppVersion;
	}
	
	public int getUserId() 
	{
		return mUserId;
	}
	
	public long getDeviceId()
	{
		return mDeviceId;
	}
	
	public long getSessionId()
	{
		return mSessionId;
	}
	
	public String getSessionToken()
	{
		return mSessionToken;
	}
	
	public String getDevicePlatform()
	{
		return mDevicePlatform;
	}
}
