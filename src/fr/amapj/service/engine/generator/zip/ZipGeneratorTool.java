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
 package fr.amapj.service.engine.generator.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.DeleteOnCloseFileInputStream;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.service.engine.generator.CoreGenerator;

/**
 * Permet la génération facile des fichiers ZIP
 * 
 */
public class ZipGeneratorTool 
{

	private ZipOutputStream out;
	private File temp;
	private List<String> fileNames = new ArrayList<>();

	public ZipGeneratorTool() 
	{
		try
		{
			temp = File.createTempFile("zipper", "zip");
			temp.deleteOnExit();
			out = new ZipOutputStream(new FileOutputStream(temp));
		}
		catch (IOException e) 
		{
			throw new AmapjRuntimeException(e);
		}
	}

	public void addFile(String fileName, String extension,byte[] fileContent) 
	{
		String realFileName = computeRealFileName(fileName);
		
		try
		{
			ZipEntry e = new ZipEntry(realFileName+"."+extension);
			out.putNextEntry(e);
	
			out.write(fileContent, 0, fileContent.length);
			out.closeEntry();
		}
		catch (IOException e) 
		{
			throw new AmapjRuntimeException(e);
		}
			
	}
	
	private String computeRealFileName(String fileName) 
	{
		String f1 = escapeFileName(fileName);
		if (fileNames.contains(f1))
		{
			int index = 1;
			String f2 = f1+" ("+index+")";
			while (fileNames.contains(f2))
			{
				index++;
				f2 = f1+" ("+index+")";
			}
			fileNames.add(f2);
			return f2;
		}
		else
		{
			fileNames.add(f1);
			return f1;
		}
	}

	/**
	 * Il est nécessaire de supprimer les caractères spéciaux dans les noms de fichiers
	 * comme les ; les / et \ 
	 */
	private String escapeFileName(String fileName) 
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fileName.length(); i++) 
		{
			sb.append(escape(fileName.charAt(i)));
		}
		
		return sb.toString();
	}

	private char escape(char c) 
	{
		if (c>='0' &&  c<='9')
		{
			return c;
		}
		if (c>='a' &&  c<='z')
		{
			return c;
		}
		if (c>='A' &&  c<='Z')
		{
			return c;
		}
		if (c=='.' || c=='\'' || c=='(' || c==')' || c=='!' || c=='%'  || c=='_' ||  c==' ' || c=='à' || c=='é' || c=='è' || c=='ê' || c=='ï' || c=='î' || c=='À' || c=='É' || c=='È' || c=='Ê' || c=='Ï')
		{
			return c;
		}
		
		return '_';
	}

	public void addFile(RdbLink em,CoreGenerator coreGenerator) 
	{
		addFile(coreGenerator.getFileName(em),coreGenerator.getExtension(), coreGenerator.getByteArrayContent());
	}

	public InputStream getInputStream() 
	{
		try
		{
			out.close();
			return new DeleteOnCloseFileInputStream(temp, true);
		}
		catch (IOException e) 
		{
			throw new AmapjRuntimeException(e);
		}
	}

	
}
