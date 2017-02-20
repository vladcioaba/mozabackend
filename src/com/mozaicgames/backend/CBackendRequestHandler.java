package com.mozaicgames.backend;

import java.io.IOException;
import java.io.OutputStream;

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
	
	protected void outputResponse(HttpExchange excenge, EBackendResponsStatusCode invalidRequest, String responseBody)
	{
		try
		{
			excenge.sendResponseHeaders(invalidRequest.getValue(), responseBody.length());
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
