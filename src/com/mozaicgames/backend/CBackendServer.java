package com.mozaicgames.backend;

import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class CBackendServer {
	
	private HttpServer 						pHttpServer = null;
	private Map<String, HttpHandler>		pHandlers = new Hashtable<>();

	public CBackendServer()
	{
	}
	
	public void registerHandler(String key, HttpHandler handler)
	{
		pHandlers.put(key, handler);
	}
	
	public boolean startOnPort(int port)
	{
		try 
		{
			pHttpServer = HttpServer.create(new InetSocketAddress(port), 0);
		} 
		catch (Exception ex)
		{
			System.err.println(ex.getMessage());
			return false;
		}
		
		// register handlers
		for(String key: pHandlers.keySet())
		{
			pHttpServer.createContext("/" + key, pHandlers.get(key));
		}
		
		pHttpServer.setExecutor(Executors.newCachedThreadPool());
		pHttpServer.start();		
		System.err.println("Server running on port: " + port);
		return true;
	}
	
	public void stop()
	{
		
	}
	
}
