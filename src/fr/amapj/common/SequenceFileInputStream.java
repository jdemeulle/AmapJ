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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Permet de créer un seul input stream à partir de n fichiers 
 * 
 * Adaptation de la classe SequenceInputStream en prenant en compte les
 * commentaires de Thierry Nov 15, 2016 at 8:49 dans
 * https://stackoverflow.com/questions/14295099/how-to-chain-multiple-different-inputstreams-into-one-inputstream
 *
 */
public class SequenceFileInputStream extends InputStream
{
	List<String> fileNames;

	InputStream in;

	public SequenceFileInputStream(List<String> fileNames)
	{
		this.fileNames = fileNames;
		try
		{
			nextStream();
		} 
		catch (IOException ex)
		{
			// This should never happen
			throw new Error("panic");
		}
	}

	/**
	 * Continues reading in the next stream if an EOF is reached.
	 */
	final void nextStream() throws IOException
	{
		if (in != null)
		{
			in.close();
		}

		in = getNext();

	}

	public int available() throws IOException
	{
		if (in == null)
		{
			return 0; // no way to signal EOF from available()
		}
		return in.available();
	}

	/**
	 */
	public int read() throws IOException
	{
		while (in != null)
		{
			int c = in.read();
			if (c != -1)
			{
				return c;
			}
			nextStream();
		}
		return -1;
	}

	/**
	 */
	public int read(byte b[], int off, int len) throws IOException
	{
		if (in == null)
		{
			return -1;
		} else if (b == null)
		{
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off)
		{
			throw new IndexOutOfBoundsException();
		} else if (len == 0)
		{
			return 0;
		}
		do
		{
			int n = in.read(b, off, len);
			if (n > 0)
			{
				return n;
			}
			nextStream();
		} while (in != null);
		return -1;
	}

	/**
	 */
	public void close() throws IOException
	{
		if (in != null)
		{
			in.close();
		}
	}

	int current = -1;

	private InputStream getNext()
	{
		current++;		
		if (current>=fileNames.size())
		{
			return null;
		}
		
		String fileName = fileNames.get(current);

		try
		{
			return new FileInputStream(fileName);
		} 
		catch (FileNotFoundException e)
		{
			String message = "Impossible de charger le fichier." + StackUtils.asString(e) + "\n";
			return new ByteArrayInputStream(message.getBytes());
		}
	}
}
