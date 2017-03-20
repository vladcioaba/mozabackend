package com.mozaicgames.utils;

import java.util.ArrayList;
import java.util.List;

public class CSqlBuilderInsert extends CSqlBuilder
{
	private final List<String>  mColumns 		= new ArrayList<String>();
	private final List<String>  mTables 		= new ArrayList<String>();
	private final List<String>  mValues 		= new ArrayList<String>();
    
	public CSqlBuilderInsert into(String name) 
    {
    	mTables.add(name);
        return this;
    }
	
	public CSqlBuilderInsert value(String name, String value) 
    {
		mColumns.add(name);
    	mValues.add("'" + value + "'");
        return this;
    }

	 @Override
    public String toString() {

        StringBuilder sql = new StringBuilder("insert into ");

        appendList(sql, mTables, "", ", ");
        appendList(sql, mColumns, " ( ", ", ");
        appendList(sql, mValues, " ) values ( ", ", ");
        sql.append(");");

        return sql.toString();
    }
}
