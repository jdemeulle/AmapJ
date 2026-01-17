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

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.IOUtils;

public class Base64Utils
{
	
	/**
	 * Conbvertit un tableau en une chaine de caractères, en utilisant Base64
	 */
	static public String toBase64(byte[] in)
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Base64OutputStream b64os = new Base64OutputStream(baos, true, 0, null);
			b64os.write(in);
			b64os.close();
	
			return new String(baos.toByteArray(),"UTF-8");   
		} 
		catch (IOException e)
		{
			throw new AmapjRuntimeException(e);
		}
	}
	
	
	
	/**
	 * Permet de convertir une chaine de caractères en un tableau de bytes, en utilisant base64 
	 */
	static public byte[] fromBase64(String in)
	{
		try
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(in.getBytes("UTF-8"));
			Base64InputStream b64is = new Base64InputStream(bais);
		
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(b64is, baos);
			
			baos.close();
			b64is.close();
	
			return baos.toByteArray();   
		} 
		catch (IOException e)
		{
			throw new AmapjRuntimeException(e);
		}
	}
	
}
