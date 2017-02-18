package com.mozaicgames.backend;

import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class CBackendServer 
{
	
	private HttpServer 						mHttpServer = null;
	private Map<String, HttpHandler>		mHandlers = new Hashtable<>();

	public CBackendServer()
	{
	}
	
	public void registerHandler(String key, HttpHandler handler)
	{
		mHandlers.put(key, handler);
	}
	
	public HttpHandler getHandlerFor(String key)
	{
		return mHandlers.get(key); 
	}
	
	public boolean startOnPort(int port)
	{
		try 
		{
			mHttpServer = HttpServer.create(new InetSocketAddress(port), 0);
		} 
		catch (Exception ex)
		{
			System.err.println(ex.getMessage());
			return false;
		}
		
		// register handlers
		for(String key: mHandlers.keySet())
		{
			mHttpServer.createContext("/" + key, mHandlers.get(key));
		}
		
		mHttpServer.setExecutor(Executors.newCachedThreadPool());
		mHttpServer.start();	
		System.err.println("Server running on port: " + port);
		return true;
	}
	
	public void stop()
	{
		mHttpServer.stop(0);
	}
	
}
