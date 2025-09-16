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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.SimpleBookmark;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

import fr.amapj.common.AmapjRuntimeException;

public class PdfTools 
{
	
	/**
	 * Retourne la localisation d'un caractère précis dans le document PDF
	 * On scanne toutes les pages, et on retourne la localisation sur  la première page
	 * Retourne null si le caractère n'est pas trouvé 
	 * 
	 * Il faut utiliser un caractère discriminant, c'est à dire un caractère spécial 
	 */
	public Location findLocation(char c,PdfReader reader) throws IOException
	{
		int nb = reader.getNumberOfPages();
		for (int i = 1; i <= nb; i++) 
		{
			Location t = findLocationPage(c,reader,i);
			if (t!=null)
			{
				return t;
			}
		}
		return null;
	}


	public Location findLocationPage(char c, PdfReader reader, int page) throws IOException
	{
		PdfTextExtractor extractor = new PdfTextExtractor(reader,true);
		
		// Cette méthode retourne un pseudo fichier html avec le contenu textuel du pdf , de la forme 
		// <br class='t-pdf' /><span class="t-word" style="bottom: 34,00%; left: 4,92%; width: 0,00%; height: 0,93%;" id="word264">ł </span>  ...
		String html = extractor.getTextFromPage(page,true);
		
		// On recherche ensuite la localisation du character discriminant, dans notre exemple :  ł
		int index = html.indexOf(c);
		if (index==-1)
		{
			return null;
		}
		
		// On recherche ensuite les valeur de bottom et left
		html = html.substring(0, index);
		index = html.lastIndexOf("bottom");
		html = html.substring(index+8,html.length());
		
		// A cette épage, html est egal à 34,00%; left: 4,92%; width: 0,00%; height: 0,93%;" id="word264">
		index = html.indexOf("%; left: ");
		String bottom = html.substring(0,index);
		
		int index2 = html.indexOf("%; width:");
		String left = html.substring(index+9,index2);
		
		// Voir la classe com.lowagie.text.pdf.parser.Word, ligne 131
		// On fait le clacul inverse , après essai on constate que cropBox et trimBox sont null, on utilise donc uniquement mediaBox 
		// On constate aussi que mediaBox.getLeft() et mediaBox.getBottom() sont aussi égals à 0, on n'en tient pas compte 
		Rectangle mediaBox = reader.getPageSize(page);
		mediaBox.normalize();
				
		float leftf = Float.parseFloat(left.replace(',', '.'));
		float bottomf = Float.parseFloat(bottom.replace(',', '.'));
		
		// Correctif : pour une raison inexpliqué, à chaque page il y a -233% qui est ajouté sur la valeur bottom par le parseur 
		// On corrige ici
		bottomf = bottomf+(page-1)*233.33f;
		
		Location location = new Location();
		location.x = (int) ( leftf*mediaBox.getWidth()/100f );
		location.y = (int) ( bottomf*mediaBox.getHeight()/100f );
		location.pageNumber = page;
		
		 
		
		
		return location;
	}
	
	static public class Location
	{
		public int pageNumber;
		
		// Le 0 est le coin en bas à gauche 
		public int x;
		public int y;
		
		@Override
		public String toString() 
		{
			return "Location [pageNumber=" + pageNumber + ", x=" + x + ", y=" + y + "]";
		}
	}
	
	// MODIFICATION D'UN PDF
	
	/**
	 * Ajout d'un ensemble de ligne à un PDF 
	 */
	public void addTextToPdf(PdfReader reader,OutputStream os,Location l,String... texts) throws IOException
	{
		int x = l.x;
		int y = l.y;
		
		int height = texts.length*12;
		// Si on est tout en bas de la page, on remonte un peu 
		if (y<height)
		{
			y=height;
		}
		
		PdfStamper stamper = new PdfStamper(reader, os);
		PdfContentByte cb = stamper.getOverContent(l.pageNumber);
		cb.beginText();
		BaseFont bf = BaseFont.createFont(BaseFont.COURIER, BaseFont.WINANSI, BaseFont.EMBEDDED);
		cb.setFontAndSize(bf, 10);
		for (int i = 0; i < texts.length; i++) 
		{
			cb.setTextMatrix(x, y-12*i);
			cb.showText(texts[i]);
		}
		cb.endText();			
		stamper.close();			
	}
	
	
	
	/**
	 * Cette methode prend en entrée deux pdf : pdfSrc et pdfToAdd 
	 * 
	 * elle prend les nbPage premières pages du pdfSrc et ajoute ensuite toutes les pages de pdfToAdd, puis retourne le tout 
	 * 
	 * Le resultat est placé dans OutputStream os
	 * 
	 * Si pdfToAdd est null, alors aucune page n'est ajoutée
	 * 
	 * Code example : https://github.com/LibrePDF/OpenPDF/blob/master/pdf-toolbox/src/test/java/com/lowagie/examples/general/copystamp/Concatenate.java
	 */
	public void concateTwoPdf(PdfReader pdfSrc,int nbPage,PdfReader pdfToAdd,OutputStream os) throws IOException
	{
		// Normalisation du document source
		pdfSrc.consolidateNamedDestinations();
		int n = pdfSrc.getNumberOfPages();
		
		// Création du focument de sortie
		Document document = new Document(pdfSrc.getPageSizeWithRotation(1));
		PdfCopy writer = new PdfCopy(document, os);
        document.open();
		
        // Ajout des pages du document source (uniquement les nbPage premières pages) 
        PdfImportedPage page;
        for (int i = 1; i <= nbPage; i++) 
        {
        	page = writer.getImportedPage(pdfSrc, i);
        	writer.addPage(page);
        }
        
        if (pdfToAdd!=null)
        {   
	        // Normalisation du document à ajouter 
	        pdfToAdd.consolidateNamedDestinations();
	     	int nbToAdd = pdfToAdd.getNumberOfPages();
	     	
	        // Ajout des pages du document à ajouter 
	        for (int i = 1; i <= nbToAdd; i++) 
	        {
	        	page = writer.getImportedPage(pdfToAdd, i);
	        	writer.addPage(page);
	        }
        }

        // Fermeture du document 
        document.close();
	}
	
	
	
	public int getNbPages(byte[] pdfContent) 
	{
		try 
		{
			PdfReader reader = new PdfReader(pdfContent);
			int nb = reader.getNumberOfPages();
			reader.close();
			return nb;
		}
		catch (IOException e) 
		{
			throw new AmapjRuntimeException(e);
		}
	}
	
	

	
	// PARTIE TEST 
	
	public static void main(String[] args) throws Exception 
	{
		System.out.println("start");
		for (int i = 0; i < 1000; i++) 
		{
			test2();
		}
		System.out.println("stop");
	}


	private static void test1() throws IOException 
	{
		PdfReader reader = new PdfReader("C:/tmp/original.pdf");
		
		//
		Location l = new PdfTools().findLocation('ł', reader);
		System.out.println("Location ="+l);
		
		//
		new PdfTools().addTextToPdf(reader, new FileOutputStream("C:/tmp/original-stamped.pdf"), l, "Signé electroniquement","le 06/06/2024 10:00:10","par Toto DUPONT");
		
		reader.close();
		
	}
	
	
	private static void test2() throws IOException 
	{
		PdfReader src = new PdfReader("C:/tmp/src.pdf");
		PdfReader add = new PdfReader("C:/tmp/add.pdf");
		FileOutputStream fos = new FileOutputStream("C:/tmp/out.pdf");

		//
		new PdfTools().concateTwoPdf(src, 1, add, fos);
		
		src.close();
		add.close();
		fos.close();
		
	}
}
