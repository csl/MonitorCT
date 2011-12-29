package com.monitortracker;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class RangeListHandler extends DefaultHandler
{
	//tag
	private String TAG = "RFood";
	
	private final static int ID = 1;
	private final static int NAME = 2;
	private final static int GPS = 3;
	private final static int STIME = 4;
	private final static int DTIME = 5;
	private final static int CHILD = 6;
	
	private grstruct jls;
	private RangeListContainer jlcs;
	
	private int type;

	public RangeListContainer getContainer() 
	{
		return jlcs;
	}

	public grstruct getJListStruct() 
	{
		return jlcs.getoneJL(0);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		
		String s = new String(ch, start, length);
		
		switch (type) 
		{
		case ID:
			jls.id = s;
			type = 0;
			break;
		case NAME:
			jls.name = s;
			type = 0;
			break;
		case GPS:
			jls.gpsdata = s;
			type = 0;
			break;
		case STIME:
			jls.stime = s;
			type = 0;
			break;
		case DTIME:
			jls.dtime = s;
			type = 0;
			break;
		case CHILD:
			jls.child = s;
			type = 0;
			break;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		if (localName.toLowerCase().equals("rfitem")) 
		{
			jlcs.addRXMLItem(jls);	
		}
	}

	@Override
	public void startDocument() throws SAXException 
	{
		jlcs = new RangeListContainer();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException 
	{
		if (localName.toLowerCase().equals("item")) 
		{
			jls = new grstruct();
			return;
		}
		else if (localName.toLowerCase().equals("id")) 
		{
			type = ID;
			return;
		}
		else if (localName.toLowerCase().equals("name")) 
		{
			type = NAME;
			return;
		}
		else if (localName.toLowerCase().equals("gps")) 
		{
			type = GPS;
			return;
		}
		else if (localName.toLowerCase().equals("stime")) 
		{
			type = STIME;
			return;
		}
		else if (localName.toLowerCase().equals("dtime")) 
		{
			type = DTIME;
			return;
		}
		else if (localName.toLowerCase().equals("child")) 
		{
			type = CHILD;
			return;
		}
		type = 0;
	}

}