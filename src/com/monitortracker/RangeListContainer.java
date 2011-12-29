package com.monitortracker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

public class RangeListContainer 
{

	private ArrayList<grstruct> jlist_items;

	//item
	public ArrayList<grstruct> getListItems() 
	{
	  return jlist_items;
	}
	
	public grstruct getoneJL(int index)
	{
		return jlist_items.get(index);
	}
	
	public RangeListContainer() 
	{
		jlist_items = new ArrayList<grstruct>();
	}

	public void addRXMLItem(grstruct item) 
	{
		jlist_items.add(item);
	}
}
