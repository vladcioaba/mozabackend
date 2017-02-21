package com.mozaicgames.utils;

import java.io.IOException;
import java.io.OutputStream;

import com.mozaicgames.backend.EBackendResponsStatusCode;
import com.sun.net.httpserver.HttpExchange;

public class CBackendResponseWriter {
	
	protected CBackendResponseWriter()
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
