package com.mozaicgames.backend;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;

import com.mozaicgames.backend.CBackendRequestHandler;

import com.sun.net.httpserver.HttpExchange;

public class CHandlerRegisterDevice extends CBackendRequestHandler 
{
	
	public CHandlerRegisterDevice(CBackendDatabaseAutentificationData sqlData) throws Exception
	{
		super(sqlData);
	}

	@Override
    public void handle(HttpExchange t) throws IOException 
	{
		Connection sqlConnection = null;
        // Load the Connector/J driver
        try 
        {
        	Class.forName("com.mysql.jdbc.Driver").newInstance();
            // Establish connection to MySQL
        	CBackendDatabaseAutentificationData data = getSqlAutentificationData();
        	sqlConnection = DriverManager.getConnection(data.getUrl(), data.getUrl(), data.getPassword());
        }
        catch (Exception e)
        {
        	System.err.println("Error with the connection to database.");
            System.err.println(e.getMessage());
            return ;
        }
		
		String response = "This is the response";
		t.sendResponseHeaders(200, response.length());
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}
}
