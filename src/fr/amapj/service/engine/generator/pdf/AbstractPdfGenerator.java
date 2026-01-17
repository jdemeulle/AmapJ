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
 package fr.amapj.service.engine.generator.pdf;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.tools.TestTools;
import fr.amapj.service.engine.generator.CoreGenerator;
import fr.amapj.service.engine.generator.CoreGeneratorService;


/**
 * Permet la gestion des extractions au format PDF
 */
abstract public class AbstractPdfGenerator implements CoreGenerator
{
	
	private final static Logger logger = LogManager.getLogger();
	
	/**
	 * Permet de générer le fichier Excel pour un modele de contrat
	 * @return
	 */
	abstract public void fillPdfFile(RdbLink em,PdfGeneratorTool et);
	
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
		return "pdf";
	}
	
	/**
	 * Cette méthode doit être utilisée pour les tests unitaires uniquement
	 * Retourne le contenu HTML du document 
	 */
	public String getHtmlContentForTest()
	{
		PdfGeneratorTool pdfGeneratorTool = new CoreGeneratorService().getFichierPdf(this);
		String html = pdfGeneratorTool.getFinalDoc();
		return html;
	}
	
	@Override
	public InputStream getContent()
	{
		return new ByteArrayInputStream(getByteArrayContent());
	}
	
	@Override
	public byte[] getByteArrayContent()
	{
		PdfGeneratorTool pdfGeneratorTool = new CoreGeneratorService().getFichierPdf(this);
		String html = pdfGeneratorTool.getFinalDoc();
		
		return new Html2PdfConverter().convertHtmlToPdf(html);
	}
	
	
	
	public void test() throws Exception
	{
		TestTools.init();
		
		String filename = "test.pdf";
		
		FileOutputStream fos = new FileOutputStream(filename);
		IOUtils.copy(getContent(), fos);
		fos.close();
		

		System.out.println("Your pdf file has been generated!");
	}
}
