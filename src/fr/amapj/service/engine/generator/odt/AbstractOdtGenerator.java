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
 package fr.amapj.service.engine.generator.odt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.odftoolkit.simple.TextDocument;

import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.tools.TestTools;
import fr.amapj.service.engine.generator.CoreGenerator;
import fr.amapj.service.engine.generator.CoreGeneratorService;


/**
 * Permet la gestion des extractions au format ODT
 * 
 *  
 *
 */
abstract public class AbstractOdtGenerator implements CoreGenerator
{
	
	
	/**
	 * Permet de générer le fichier Excel pour un modele de contrat
	 * @return
	 */
	abstract public void fillWordFile(RdbLink em,OdtGeneratorTool et);
	
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
		return ".odt";
	}
	
	
	@Override
	public InputStream getContent()
	{
		return new ByteArrayInputStream(getByteArrayContent());
	}
	
	@Override
	public byte[] getByteArrayContent()
	{
		TextDocument workbook = new CoreGeneratorService().getFichierOdt(this);
		
		ByteArrayOutputStream imagebuffer = new ByteArrayOutputStream();
	
		try
		{
			workbook.save(imagebuffer);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Erreur inattendue");
		}
		return imagebuffer.toByteArray();
	}
	
	
	public void test() throws Exception
	{
		TestTools.init();
		
		String filename = "test.odt";
		TextDocument doc =  new CoreGeneratorService().getFichierOdt(this); 
		
		
		doc.save(filename);
		System.out.println("Your odt file has been generated!");
	}
	
	
}
