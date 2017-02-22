package com.mozaicgames.core;

import java.io.IOException;

import javax.sql.DataSource;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class CBackendRequestHandler implements HttpHandler 
{
	private final DataSource			mSqlDataSource;
	private final String 				mMinClientVersionAllowed;

	protected CBackendRequestHandler(DataSource sqlDataSource, String minClientVersionAllowed) throws Exception
	{
		mSqlDataSource = sqlDataSource;
		if (mSqlDataSource == null)
		{
			throw new Exception("Invalid argument");
		}
		mMinClientVersionAllowed = minClientVersionAllowed;
	}
	
	protected DataSource getDataSource()
	{
		return mSqlDataSource;
	}
	
	protected String getMinClientVersionAllowed()
	{
		return mMinClientVersionAllowed;
	}
	
	@Override
    public void handle(HttpExchange t) throws IOException 
	{
    }	
}
