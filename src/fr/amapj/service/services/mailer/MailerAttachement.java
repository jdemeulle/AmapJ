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
 package fr.amapj.service.services.mailer;

import java.io.File;
import java.io.IOException;

import javax.activation.FileTypeMap;

import org.apache.commons.io.FileUtils;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.service.engine.generator.CoreGenerator;
import fr.amapj.service.engine.generator.CoreGeneratorService;
import fr.amapj.service.engine.generator.FileInfoDTO;

/**
 * Permet de stocker une piece jointe 
 * 
 *
 */
public class MailerAttachement
{

	private byte[] data;
	
	private String name;
	
	private String mimeType;
	
	
	/**
	 * Permet de construire une pièce jointe à partir d'un fichier local
	 */
	public MailerAttachement(File file)
	{
		mimeType = FileTypeMap.getDefaultFileTypeMap().getContentType(file);
		name = file.getName();
		try
		{
			data = FileUtils.readFileToByteArray(file);
		} 
		catch (IOException e)
		{
			throw new AmapjRuntimeException("",e);
		}
	}
	
	
	/**
	 * Permet de construire une pièce jointe à partir d'un fichier zip ou excel ou ... 
	 */	
	public MailerAttachement(CoreGenerator generator)
	{
		CoreGeneratorService excelGeneratorService = new CoreGeneratorService();
		FileInfoDTO fileInfo = excelGeneratorService.getFileInfo(generator);
		name = fileInfo.fileName+"."+fileInfo.extension;
		
		data = generator.getByteArrayContent();
		mimeType = computeMimeType(fileInfo.extension);
		
	}
	
	
	private String computeMimeType(String extension) 
	{
		if (extension.equals("xls"))
		{
			return "application/vnd.ms-excel";
		}
		if (extension.equals("xlsx"))
		{
			return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		}
		if (extension.equals("zip"))
		{
			return "application/zip";
		}
		if (extension.equals("pdf"))
		{
			return "application/pdf";
		}	
		
		return "";
	}

	public String getName()
	{
		return name;
	}

	public byte[] getData() 
	{
		return data;
	}

	public String getMimeType() 
	{
		return mimeType;
	}
}
