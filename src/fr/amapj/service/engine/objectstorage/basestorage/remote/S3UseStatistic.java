/*
 *  Copyright 2013-2050 Emmanuel BRUN (contact@amapj.fr)
 * 
 *  This file is part of AmapJ.
 *  
 *  AmapJ is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  AmapJ is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with AmapJ.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 */
 package fr.amapj.service.engine.objectstorage.basestorage.remote;

/**
 * Permet de mesurer des statistics sur l'usage du stockage S3
 *
 */
public class S3UseStatistic 
{
	private long getCount;
	
	private long getSize;
	
	private long putCount;
	
	private long putSize;
	
	private long deleteCount;
	
	synchronized public void incrGet(int size)
	{
		getCount++;
		getSize+=size;
	}
	
	synchronized public void incrPut(int size)
	{
		putCount++;
		putSize+=size;
	}
	
	synchronized public void incrDelete()
	{
		deleteCount++;
	}
	
	
	public String getStatistics()
	{
		String res = 	"S3 Use Statistic <br/>"+
						"getCount=" + getCount +"<br/>"+
						"getSize=" + getSize +"<br/>"+
						"putCount=" + putCount +"<br/>"+
						"putSize="	+ putSize + "<br/>"+
						"deleteCount=" + deleteCount +"<br/><br/>";
		return res;
	}
}
