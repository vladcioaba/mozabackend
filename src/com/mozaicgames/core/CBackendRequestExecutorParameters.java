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
	
	public CBackendRequestExecutorParameters(final String remoteAddress,
											 final CBackendAdvancedEncryptionStandard encriptionStandard,
											 final DataSource sqlDataSource,
											 final CBackendSessionManager sessionManager,
											 final String clientCoreVersion,
											 final String clientAppVersion)
	{
		mRemoteAddress = remoteAddress;
		mEncriptionStandard = encriptionStandard;
		mSqlDataSource = sqlDataSource;
		mSessionManager = sessionManager;
		mClientCoreVersion = clientCoreVersion;
		mClientAppVersion = clientAppVersion;
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
}
