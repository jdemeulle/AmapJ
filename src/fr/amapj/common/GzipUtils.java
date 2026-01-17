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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.IOUtils;

public class GzipUtils
{
	
	/**
	 * Permet de compresser un tableau de byte pour pouvoir le socker dans la base de données
	 * On enchaine une compression par GZIP puis un encodage en base 64
	 */
	static public String compressByteArray(byte[] in)
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Base64OutputStream b64os = new Base64OutputStream(baos, true, 0, null);
			GZIPOutputStream os = new GZIPOutputStream(b64os);
			os.write(in);
			os.close();
	
			return new String(baos.toByteArray(),"UTF-8");   
		} 
		catch (IOException e)
		{
			throw new AmapjRuntimeException(e);
		}
	}
	
	
	/**
	 * Permet de decompresser un tableau de byte stocké sous la forme d'une chaine de caractères dans la base de données
	 * On enchaine un décodage base64 puis une decompression par GZIP 
	 */
	static public byte[] uncompressByteArray(String in)
	{
		try
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(in.getBytes("UTF-8"));
			Base64InputStream b64is = new Base64InputStream(bais);
			GZIPInputStream is = new GZIPInputStream(b64is);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(is, baos);
			
			baos.close();
			is.close();
	
			return baos.toByteArray();   
		} 
		catch (IOException e)
		{
			throw new AmapjRuntimeException(e);
		}
	}
	
	
	
	/**
	 * Permet de compresser une chaine de caractère pour pouvoir la socker dans la base de données
	 * On enchaine une compression par GZIP puis un encodage en base 64
	 */
	static public String compress(String in)
	{
		try
		{
			return compressByteArray(in.getBytes("UTF-8"));
		} 
		catch (UnsupportedEncodingException e)
		{
			throw new AmapjRuntimeException(e);
		}
	}
	
	
	/**
	 * Permet de decompresser une chaine de caractère venant de la base de données
	 * On enchaine un décodage base64 puis une decompression par GZIP 
	 */
	static public String uncompress(String in)
	{
		try
		{
			return new String(uncompressByteArray(in),"UTF-8");
		} 
		catch (UnsupportedEncodingException e)
		{
			throw new AmapjRuntimeException(e);
		} 
	}
}
