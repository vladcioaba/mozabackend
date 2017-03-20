package com.mozaicgames.utils;

import java.util.ArrayList;
import java.util.List;

public class CSqlBuilderSelect extends CSqlBuilder
{
	private final List<String>  mColumns 		= new ArrayList<String>();
    private final List<String>  mTables 		= new ArrayList<String>();
    private final List<String> 	mJoins		 	= new ArrayList<String>();
	private final List<String> 	mLeftJoins 		= new ArrayList<String>();
	private final List<String> 	mWheres 		= new ArrayList<String>();
	private final List<String> 	mOrderBys 		= new ArrayList<String>();
	private final List<String> 	mGroupBys 		= new ArrayList<String>();
	private final List<String> 	mHavings 		= new ArrayList<String>();
	private int 				mLimit 			= 0;
    private String 				mOrderByType	= ""; 
    
    public CSqlBuilderSelect() 
    {
    	super();
    }
    
    public CSqlBuilderSelect column(String name) 
    {
    	mColumns.add(name);
        return this;
    }
    
    public CSqlBuilderSelect column(String name, boolean groupBy) 
    {
    	mColumns.add(name);
        if (groupBy) 
        {
            mGroupBys.add(name);
        }
        return this;
    }
    
    public CSqlBuilderSelect from(String table) 
    {
        mTables.add(table);
        return this;
    }

    public CSqlBuilderSelect where(String expr) 
    {
        mWheres.add(expr);
        return this;
    }
    
    public CSqlBuilderSelect groupBy(String expr)
    {
        mGroupBys.add(expr);
        return this;
    }

    public CSqlBuilderSelect having(String expr) 
    {
        mHavings.add(expr);
        return this;
    }

    public CSqlBuilderSelect join(String join) 
    {
        mJoins.add(join);
        return this;
    }

    public CSqlBuilderSelect leftJoin(String join) 
    {
        mLeftJoins.add(join);
        return this;
    }

    public CSqlBuilderSelect orderBy(String name) 
    {
        mOrderBys.add(name);
        return this;
    }
    
    public CSqlBuilderSelect orderType(String exp) 
    {
    	mOrderByType = exp;
    	return this;
    }
    
    public CSqlBuilderSelect limit(int expr) 
    {
    	mLimit = expr;
    	return this;
    }
    
    @Override
    public String toString() {

        StringBuilder sql = new StringBuilder("select ");

        if (mColumns.size() == 0) 
        {
            sql.append("*");
        } 
        else 
        {
            appendList(sql, mColumns, "", ", ");
        }

        appendList(sql, mTables, " from ", ", ");
        appendList(sql, mJoins, " join ", " join ");
        appendList(sql, mLeftJoins, " left join ", " left join ");
        appendList(sql, mWheres, " where ", " and ");
        appendList(sql, mGroupBys, " group by ", ", ");
        appendList(sql, mHavings, " having ", " and ");
        appendList(sql, mOrderBys, " order by ", ", ");
        if (mOrderBys.size() > 0)
        {
        	sql.append(" " + mOrderByType);
        }
        
        if (mLimit > 0)
        {
        	sql.append(" limit " + mLimit);
        }
        sql.append(";");

        return sql.toString();
    }
}
