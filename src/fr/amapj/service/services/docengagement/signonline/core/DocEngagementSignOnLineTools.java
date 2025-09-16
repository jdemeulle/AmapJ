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
 package fr.amapj.service.services.docengagement.signonline.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import com.lowagie.text.pdf.PdfReader;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.FormatUtils;
import fr.amapj.service.engine.pdftools.PdfTools;
import fr.amapj.service.engine.pdftools.PdfTools.Location;

/**
 * Classe pour la manipulation des pdfs des documents d'engagement 
 *
 */
public class DocEngagementSignOnLineTools 
{
	// Signature - caractères spéciaux qui seront remplacés après la création du pdf
	final static public char CHAR_SIGNATURE_AMAPIEN = 'ł'; // U+0142 
		
	final static public char CHAR_SIGNATURE_PRODUCTEUR = 'ŀ'; // U+0140
	
	/**
	 * Recoit en entrée un PDF, le modifie pour ajouter la signature de l'amapien et retourne le nouveau Pdf 
	 */
	public byte[] addSignatureAmapien(byte[] pdfIn, Date amapienDateSignature, String amapienLibSignature)
	{
		return addSignature(CHAR_SIGNATURE_AMAPIEN, pdfIn, amapienDateSignature, amapienLibSignature);
	}

	/**
	 * Recoit en entrée un PDF, le modifie pour ajouter la signature du producteur et retourne le nouveau Pdf 
	 */
	public byte[] addSignatureProducteur(byte[] signatureContratContent, Date producteurDateSignature,String producteurLibSignature) 
	{
		return addSignature(CHAR_SIGNATURE_PRODUCTEUR, signatureContratContent, producteurDateSignature, producteurLibSignature);
	}
	
	private byte[] addSignature(char specialCharSignatureLocation,byte[] pdfIn, Date amapienDateSignature, String amapienLibSignature)
	{
		try
		{
			PdfReader reader = new PdfReader(pdfIn);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			//
			Location l = new PdfTools().findLocation(specialCharSignatureLocation, reader);
			
			if (l==null)
			{
				throw new AmapjRuntimeException("Le document ne contient pas les champs  $contrat.signatureAmapien et $contrat.signatureProducteur qui permettent de poser la signature sur le contrat");
			}
			//
			new PdfTools().addTextToPdf(reader,baos, l, "Signé electroniquement","le "+FormatUtils.getTimeStd().format(amapienDateSignature),"par "+amapienLibSignature);
			//
			reader.close();
			
			return baos.toByteArray();
		}
		catch (IOException e) 
		{
			throw new AmapjRuntimeException(e);
		}
	}

	
	/**
	 * Retourne true si le document contient l'ancre pour la signature amapien et l'ancre pour la signature producteur   
	 */
	public boolean hasSignatureAmapienAndProducteur(byte[] pdfIn)
	{
		try
		{
			PdfReader reader = new PdfReader(pdfIn);
			//
			Location l1 = new PdfTools().findLocation(CHAR_SIGNATURE_AMAPIEN, reader);
			
			Location l2 = new PdfTools().findLocation(CHAR_SIGNATURE_PRODUCTEUR, reader);
			
			reader.close();
			
			return l1!=null && l2!=null;
		}
		catch (IOException e) 
		{
			throw new AmapjRuntimeException(e);
		}
	}
	
	

	/**
	 * Permet de concatener 2 deux pdfs
	 */
	public byte[] concateTwoPdf(byte[] src,int nbPage,byte[] toAdd)
	{
		try
		{
			PdfReader pdfSrc = new PdfReader(src);
			PdfReader pdfToAdd = new PdfReader(toAdd);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			//
			new PdfTools().concateTwoPdf(pdfSrc, nbPage, pdfToAdd, baos);
					
			pdfSrc.close();
			pdfToAdd.close();
			
			return baos.toByteArray();

		}
		catch (IOException e) 
		{
			throw new AmapjRuntimeException(e);
		}
	}	
	
	/**
	 * Permet de recuperer les x premières pages d'un document 
	 */
	public byte[] getFirstPages(byte[] src,int nbPage)
	{
		try
		{
			PdfReader pdfSrc = new PdfReader(src);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			//
			new PdfTools().concateTwoPdf(pdfSrc, nbPage, null, baos);
			
			pdfSrc.close();
			
			return baos.toByteArray();

		}
		catch (IOException e) 
		{
			throw new AmapjRuntimeException(e);
		}
	}
	

	
}
