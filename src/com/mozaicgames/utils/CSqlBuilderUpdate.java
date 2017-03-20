package com.mozaicgames.utils;

import java.util.ArrayList;
import java.util.List;

public class CSqlBuilderUpdate extends CSqlBuilder
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
    
    public CSqlBuilderUpdate()
	{
		super();
	}
	
    public CSqlBuilderUpdate table(String name)
    {
    	mTables.add(name);
    	return this;
    }
    
    public CSqlBuilderUpdate set(String name, String value) 
    {
    	mColumns.add(name + "='" + value + "'");
        return this;
    }

    public CSqlBuilderUpdate where(String expr) 
    {
        mWheres.add(expr);
        return this;
    }
    
    public CSqlBuilderUpdate groupBy(String expr)
    {
        mGroupBys.add(expr);
        return this;
    }

    public CSqlBuilderUpdate having(String expr) 
    {
        mHavings.add(expr);
        return this;
    }

    public CSqlBuilderUpdate join(String join) 
    {
        mJoins.add(join);
        return this;
    }

    public CSqlBuilderUpdate leftJoin(String join) 
    {
        mLeftJoins.add(join);
        return this;
    }

    public CSqlBuilderUpdate orderBy(String name) 
    {
        mOrderBys.add(name);
        return this;
    }
    
    public CSqlBuilderUpdate orderType(String exp) 
    {
    	mOrderByType = exp;
    	return this;
    }
    
    public CSqlBuilderUpdate limit(int expr) 
    {
    	mLimit = expr;
    	return this;
    }
    
    @Override
    public String toString() {

        StringBuilder sql = new StringBuilder("update ");

        appendList(sql, mTables, "", ", ");
        appendList(sql, mColumns, " set ", ", ");
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
