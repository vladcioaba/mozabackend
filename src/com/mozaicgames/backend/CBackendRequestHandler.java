package com.mozaicgames.backend;

import java.io.IOException;

import java.sql.Connection;

import javax.sql.DataSource;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class CBackendRequestHandler implements HttpHandler 
{
	private DataSource			mSqlDataSource = null;

	protected CBackendRequestHandler(DataSource sqlDataSource) throws Exception
	{
		mSqlDataSource = sqlDataSource;
		if (mSqlDataSource == null)
		{
			throw new Exception("Invalid argument");
		}
	}
	
	protected DataSource getDataSource()
	{
		return mSqlDataSource;
	}
	
	@Override
    public void handle(HttpExchange t) throws IOException 
	{
    }	
}
