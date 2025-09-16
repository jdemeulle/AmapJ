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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.service.engine.generator.CoreGenerator;
import fr.amapj.service.engine.generator.CoreGeneratorService;


/**
 * Permet la gestion des fichiers zips contenant une liste de fichiers 
 * 
 */
abstract public class AbstractZipGenerator implements CoreGenerator
{
	/**
	 * Permet de remplir le fichier zip   
	 */
	abstract public void fillZipFile(RdbLink em, ZipGeneratorTool et);
	
	abstract public String getFileName(RdbLink em);
	
	abstract public String getNameToDisplay(RdbLink em);
	
	private String nameToDisplaySuffix;
	
	/**
	 * Permet de positionner un suffixe au file name, par exemple pour préciser son format
	 * Ce suffixe est à la fin du nom de fichier, mais avant l'extension 
	 */
	public void setNameToDisplaySuffix(String nameToDisplaySuffix)
	{
		this.nameToDisplaySuffix = nameToDisplaySuffix;
	}	
	
	@Override
	public String getNameToDisplaySuffix()
	{
		return nameToDisplaySuffix;
	}
	
	
	public String getExtension()
	{
		return "zip";
	}
	
	
	@Override
	public InputStream getContent()
	{
		ZipGeneratorTool generatorTool = new CoreGeneratorService().getFichierZip(this);
		return generatorTool.getInputStream();
	}
	
	@Override
	public byte[] getByteArrayContent()
	{
		try
		{
			return IOUtils.toByteArray(getContent());
		}
		catch (IOException e) 
		{
			throw new AmapjRuntimeException(e);
		}
	}
	
}
