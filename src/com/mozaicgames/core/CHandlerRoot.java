package com.mozaicgames.core;

import java.io.IOException;

import javax.sql.DataSource;

import com.mozaicgames.utils.CBackendUtils;
import com.sun.net.httpserver.HttpExchange;

public class CHandlerRoot extends CBackendRequestHandler 
{
	public CHandlerRoot(DataSource sqlDataSource) throws Exception {
		super(sqlDataSource, "");
	}

	@Override
    public void handle(HttpExchange t) throws IOException 
	{
		CBackendUtils.writeResponseInExchange(t, EBackendResponsStatusCode.INVALID_REQUEST, "Invalid request address.");
    }
}
