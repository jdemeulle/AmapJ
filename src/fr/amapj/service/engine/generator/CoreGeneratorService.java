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
 package fr.amapj.service.engine.generator;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.odftoolkit.simple.TextDocument;

import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.service.engine.generator.bin.AbstractBinGenerator;
import fr.amapj.service.engine.generator.excel.AbstractExcelGenerator;
import fr.amapj.service.engine.generator.excel.ExcelFormat;
import fr.amapj.service.engine.generator.excel.ExcelGeneratorTool;
import fr.amapj.service.engine.generator.odt.AbstractOdtGenerator;
import fr.amapj.service.engine.generator.odt.OdtGeneratorTool;
import fr.amapj.service.engine.generator.pdf.AbstractPdfGenerator;
import fr.amapj.service.engine.generator.pdf.PdfGeneratorTool;
import fr.amapj.service.engine.generator.zip.AbstractZipGenerator;
import fr.amapj.service.engine.generator.zip.ZipGeneratorTool;


/**
 * Permet la gestion des extractions excels , pdf, ...
 */
public class CoreGeneratorService
{

	// PARTIE COMMUNE EXCEL, PDF, ...
	
	/**
	 * Permet de générer les informations sur le fichier Excel, PDF, ...
	 */
	@DbRead
	public List<FileInfoDTO>  getFileInfo(List<CoreGenerator> generators)
	{
		RdbLink em = RdbLink.get();
		
		List<FileInfoDTO> res = new ArrayList<>();
		
		for (CoreGenerator generator : generators)
		{
			FileInfoDTO dto = new FileInfoDTO();
			
			dto.fileName = generator.getFileName(em);
			
			dto.nameToDisplay = generator.getNameToDisplay(em);
			String suffix = generator.getNameToDisplaySuffix();
			if (suffix!=null)
			{
				dto.nameToDisplay = dto.nameToDisplay+suffix;
			}
			dto.extension = generator.getExtension();
			dto.generator = generator;
			res.add(dto);
		}
		
		return res;
	}	
	
	/**
	 * Idem à la fonction précédente mais sur un seul fichier 
	 */
	public FileInfoDTO  getFileInfo(CoreGenerator generator)
	{
		List<CoreGenerator> generators = new ArrayList<>();
		generators.add(generator);
		List<FileInfoDTO> res = getFileInfo(generators);
		return res.get(0);
	}
	
	
	// PARTIE SPECIFIQUE A EXCEL
	
	/**
	 * Permet de générer un fichier Excel 
	 */
	@DbRead
	public Workbook  getFichierExcel(AbstractExcelGenerator generator)
	{
		RdbLink em = RdbLink.get();
	
		ExcelFormat format = generator.getFormat();
		ExcelGeneratorTool et = new ExcelGeneratorTool(format);
		generator.fillExcelFile(em, et);
		return et.getWb();
	}
	
	

	
	// PARTIE SPECIFIQUE A ODT
	
	/**
	 * Permet de générer un fichier ODT (Libre Office) 
	 */
	@DbRead
	public TextDocument getFichierOdt(AbstractOdtGenerator generator)
	{
		RdbLink em = RdbLink.get();
	
		OdtGeneratorTool et = new OdtGeneratorTool();
		generator.fillWordFile(em, et);
		return et.getDoc();
	}
	
	
	// PARTIE SPECIFIQUE A PDF
	
	/**
	 * Permet de générer un fichier PDF (à partir d'une source HTML) 
	 */
	@DbRead
	public PdfGeneratorTool getFichierPdf(AbstractPdfGenerator generator)
	{
		RdbLink em = RdbLink.get();
	
		PdfGeneratorTool et = new PdfGeneratorTool();
		generator.fillPdfFile(em, et);
		return et;
	}
	
	
	// PARTIE SPECIFIQUE A ZIP 
	
	/**
	 * Permet de générer un fichier ZIP 
	 */
	@DbRead
	public ZipGeneratorTool getFichierZip(AbstractZipGenerator generator)
	{
		RdbLink em = RdbLink.get();
	
		ZipGeneratorTool et = new ZipGeneratorTool();
		generator.fillZipFile(em, et);
		return et;
	}
	
	// PARTIE SPECIFIQUE A BIN 
	
	/**
	 * Permet de générer un fichier BIN 
	 */
	@DbRead
	public byte[] getFichierBin(AbstractBinGenerator generator)
	{
		RdbLink em = RdbLink.get();
		return generator.generateContent(em);
	}
	

}
