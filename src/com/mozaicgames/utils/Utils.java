package com.mozaicgames.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utils {

	public static String getStringFromStream(InputStream stream) throws IOException
	{
		InputStreamReader isr =  new InputStreamReader(stream, "utf-8");
		BufferedReader br = new BufferedReader(isr);
		String strStream = "";
		int b;
		while ((b = br.read()) != -1) {
			strStream += (char)b;
		}
		br.close();
		isr.close();
		return strStream; 
	}

}
