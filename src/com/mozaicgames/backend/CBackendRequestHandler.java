package com.mozaicgames.backend;

import java.io.IOException;

import java.sql.Connection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class CBackendRequestHandler implements HttpHandler 
{
	private CBackendDatabaseAutentificationData			mSqlAutentificationData = null;

	protected CBackendRequestHandler(CBackendDatabaseAutentificationData sqlData) throws Exception
	{
		mSqlAutentificationData = sqlData;
		if (mSqlAutentificationData == null)
		{
			throw new Exception("Invalid argument");
		}
	}
	
	protected CBackendDatabaseAutentificationData getSqlAutentificationData()
	{
		return mSqlAutentificationData;
	}
	
	@Override
    public void handle(HttpExchange t) throws IOException 
	{
    }	
}
