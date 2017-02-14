package com.mozaicgames.backend;

import java.io.IOException;

import javax.sql.DataSource;

import com.sun.net.httpserver.HttpExchange;

public class CHandlerRoot extends CBackendRequestHandler 
{
	public CHandlerRoot(DataSource sqlDataSource) throws Exception {
		super(sqlDataSource);
	}

	@Override
    public void handle(HttpExchange t) throws IOException 
	{
		super.outputResponse(t, EBackendResponsStatusCode.INVALID_REQUEST, "Invalid request address.");
    }
}
