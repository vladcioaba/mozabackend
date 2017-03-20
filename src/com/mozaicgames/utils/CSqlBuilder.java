package com.mozaicgames.utils;

import java.util.List;

public class CSqlBuilder 
{
    public CSqlBuilder() 
    {

    }

    protected void appendList(StringBuilder sql, List<String> list, String init, String sep) 
    {
        boolean first = true;
        for (String s : list) 
        {
            if (first) 
            {
                sql.append(init);
            } 
            else 
            {
                sql.append(sep);
            }
            sql.append(s);
            first = false;
        }
    }
    
    @Override
    public String toString() 
    {
    	return "CSqlBuilder";
    }

}
