package com.mozaicgames.backend;

import java.io.IOException;
import java.io.OutputStream;

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
	
	protected void outputResponse(HttpExchange excenge, int responseCode, String responseBody)
	{
		try
		{
			excenge.sendResponseHeaders(responseCode, responseBody.length());
			OutputStream os = excenge.getResponseBody();
			os.write(responseBody.getBytes());
			os.close();
		}
		catch (IOException ex)
		{
			System.err.println("Error writing reps: " + ex.getMessage());
		}
	}
}
