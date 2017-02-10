package com.mozaicgames.backend;

public class CBackendDatabaseAutentificationData {

	private		String  mUrl;
	private		String 	mUser;
	private 	String  mPassword;
	
	public CBackendDatabaseAutentificationData(String url, String user, String password)
	{
		mUrl = url;
		mUser = user;
		mPassword = password;
	}
	
	public String getUrl()
	{
		return mUrl;
	}
	
	public String getUser()
	{
		return mUser;
	}
	
	public String getPassword()
	{
		return mPassword;
	}
}
