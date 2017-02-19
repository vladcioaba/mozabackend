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
	
	public static int compareStringIntegerValue(String s1, String s2)  
	{  
		final String delimeter = "\\."; 
		String[] s1Tokens = s1.split(delimeter);  
		String[] s2Tokens = s2.split(delimeter);  

		int length = Math.max(s1Tokens.length, s2Tokens.length);
		for (int i = 0; i < length; i++)  
		{  
			int s1Value = 0;
			if (i < s1Tokens.length)
			{
				s1Value = Integer.parseInt(s1Tokens[i]);
			}
			
		 	int s2Value = 0;
		 	if (i < s2Tokens.length)
		 	{
		 		s2Value = Integer.parseInt(s2Tokens[i]);
		 	}
		 	
		 	if (s1Value == s2Value)
		 	{
		 		continue;
		 	}
		 	if (s1Value < s2Value)
		 	{
		 		return -1;
		 	}
		 	else
		 	{
		 		return 1;
		 	}
		}
		return 0;  //values are equal
	 } 

}
