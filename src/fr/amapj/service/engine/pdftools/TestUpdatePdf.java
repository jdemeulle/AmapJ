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
 package fr.amapj.service.engine.pdftools;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
import com.lowagie.text.pdf.parser.MarkedUpTextAssembler;
import com.lowagie.text.pdf.parser.PdfContentReaderTool;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

public class TestUpdatePdf 
{
	public static void main(String[] args) throws Exception 
	{
		new TestUpdatePdf().execute();	
	}

	private void execute() throws Exception 
	{
		PdfReader reader = new PdfReader("C:/tmp/original.pdf");
		
		
		
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream("C:/tmp/original-stamped.pdf"));
		PdfContentByte cb = stamper.getOverContent(1);
		cb.beginText();
		cb.setTextMatrix(100, 450);
		BaseFont bf = BaseFont.createFont(BaseFont.COURIER, BaseFont.WINANSI, BaseFont.EMBEDDED);
		cb.setFontAndSize(bf, 12);
		cb.showText("Text at position 100,450.");
		cb.endText();
		
		// adding an extra page
        stamper.insertPage(reader.getNumberOfPages()+1, PageSize.A4);
        cb = stamper.getOverContent(reader.getNumberOfPages());
        cb.beginText();
        cb.setFontAndSize(bf, 18);
        cb.showTextAligned(Element.ALIGN_LEFT, "AJOUT D UN TEXTE SUR LA DERNIERE PAGE", 30, 600, 0);
        cb.endText();
		
		
		stamper.close();		
		
		reader.close();	
	}	
}
