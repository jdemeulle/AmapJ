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
 package fr.amapj.service.services.docengagement.signonline.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.google.gson.Gson;

import fr.amapj.common.AmapjRuntimeException;

/**
 * Objet stocké sérialisé dans le champ pdfData de la classe DocEngagementSigne
 * 
 * Contient à la fois le pdf et les données associées 
 *
 * Attention : faire des modifs avec précaution ! 
 */
public class DocEngagementBin
{	
	// Le document d'engagement au format pdf 
	public byte[] pdfContent;
	
	// Les meta data du document d'engagement 
	public DocEngagementData docEngagementData; 
	
	
	
	public byte[] toBytes()
	{
		try 
		{
			// 
			byte[] docEngagementDataJson = new Gson().toJson(docEngagementData).getBytes("UTF-8");
			
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			// La version sur 1 octet
			baos.write(1);
			
			// Toujours 0
			baos.write(0);
			
			// La longueur du pdf sur 4 octets
			writeInt(baos,pdfContent.length);
			
			// La longueur de docEngagementData
			writeInt(baos,docEngagementDataJson.length);
			
			// Le contenu du pdf
			baos.write(pdfContent);
			
			// Le contenu de docEngagementData
			baos.write(docEngagementDataJson);
			
			return baos.toByteArray();
		} 
		catch (IOException e) 
		{
			throw new AmapjRuntimeException(e);
		} 
	}
	
	
	public void fromBytes(byte[] bs)
	{
		try 
		{
			int version = bs[0];
			if (version!=1)
			{
				throw new AmapjRuntimeException("Version incorrecte : "+version);
			}
			
			int lenPdf = readInt(bs,2);
			int lenJson = readInt(bs,6);
			
			pdfContent =  Arrays.copyOfRange(bs,10,10+lenPdf);
			String json = new String(bs,10+lenPdf,lenJson,"UTF-8");
			docEngagementData = new Gson().fromJson(json, DocEngagementData.class);
		} 
		catch (IOException e) 
		{
			throw new AmapjRuntimeException(e);
		} 
	}
	
	
	// PARTIE TECHNIQUE 
	
	private int readInt(byte[] bs, int startIndex) 
	{
		return ByteBuffer.wrap(bs,startIndex,4).getInt();
	}


	private void writeInt(ByteArrayOutputStream baos, int a) throws IOException 
	{
		baos.write(ByteBuffer.allocate(4).putInt(a).array());
	}
}
