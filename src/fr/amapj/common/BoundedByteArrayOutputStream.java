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
 package fr.amapj.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * A byte array backed output stream with a limit. 
 * The limit should be smaller
 * than the buffer capacity. 
 */
public class BoundedByteArrayOutputStream extends ByteArrayOutputStream 
{
	private int limit;

	public BoundedByteArrayOutputStream(int capacity) 
	{
		this(capacity, capacity);
	}

	public BoundedByteArrayOutputStream(int capacity, int limit) 
	{
		super(capacity);
		this.limit = limit;
	}

	@Override
	public void write(int b)
	{
		if (count+1 > limit) 
		{
			throw new RuntimeException("Reach the limit of the buffer.");
		}
		super.write(b);
	}

	@Override
	public void write(byte b[], int off, int len)
	{
		if (count + len > limit) 
		{
			throw new RuntimeException("Reach the limit of the buffer");
		}

		super.write(b,off,len);
	}
	
	
	public InputStream getInputStream()
	{
		return new ByteArrayInputStream(buf, 0, count);
	}

}