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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.RuntimeUtils;
import fr.amapj.messagingcenter.miniproxy.TNProxyClient;
import fr.amapj.messagingcenter.miniproxy.core.ServiceNotAvailableException;
import fr.amapj.messagingcenter.miniproxy.model.html2pdf.request.ConvertHtml2PdfRequest;
import fr.amapj.messagingcenter.miniproxy.model.html2pdf.response.ConvertHtml2PdfResponse;
import fr.amapj.model.engine.db.DbManager;
import fr.amapj.view.engine.ui.AppConfiguration;

/**
 * Permet de réaliser la conversion d'un Html en Pdf 
 *
 */
public class Html2PdfConverter 
{
	
	private final static Logger logger = LogManager.getLogger();
	
	public byte[] convertHtmlToPdf(String html)
	{
		if (AppConfiguration.getConf().isPdfConversionWithMessagingCenter())
		{
			return convertHtmlToPdfWithMessagingCenter(html);
		}
		else
		{
			return convertHtmlToPdfWithLocalInstall(html);
			
		}
		
	}

	private byte[] convertHtmlToPdfWithLocalInstall(String html)
	{
		logger.info("Conversion d'un html de taille "+html.length());
		
		try
		{
			File in = File.createTempFile("inpdf", ".html"); 
			File out = File.createTempFile("outpdf", ".pdf");
			
			FileOutputStream fos = new FileOutputStream(in);
			fos.write(html.getBytes("UTF-8"));
			fos.flush();
			fos.close();
			
		
			String wkhtmltopdfCommand = AppConfiguration.getConf().getWkhtmltopdfCommand();
			if (wkhtmltopdfCommand==null)
			{
				throw new RuntimeException("Impossible de generer le fichier PDF car  wkhtmltopdf n'est pas installé ou son chemin d'accès n'est pas configuré");
			}
			
			String addCmdLine = " --disable-smart-shrinking -T 0 -B 0 -L 0 -R 0 ";
			
			String fullCommand = wkhtmltopdfCommand+" "+addCmdLine+" "+in.getCanonicalPath()+" "+out.getCanonicalPath();
			
			logger.info("Lancement de la commande="+fullCommand);
			
			RuntimeUtils.executeCommandLine(fullCommand, 50000);
			
			byte[] pdfContent = FileUtils.readFileToByteArray(out);
			
			
			// On efface les fichiers
			in.delete();
			out.delete();
			
			logger.info("Success");
			
			//
			return pdfContent;
			
		} 
		catch (IOException | InterruptedException | TimeoutException e)
		{
			logger.error("Erreur lors de la conversion du fichier",e);
			throw new AmapjRuntimeException(e);
		}
	}
	
	
	
	private byte[] convertHtmlToPdfWithMessagingCenter(String html) 
	{
		ConvertHtml2PdfRequest request = new ConvertHtml2PdfRequest();
		request.dbName= DbManager.get().getCurrentDb().getDbName();
		request.html = html;
		ConvertHtml2PdfResponse response;
		try 
		{
			response = getProxy().convertHtml2Pdf(request);
		} 
		catch (ServiceNotAvailableException e) 
		{
			throw new AmapjRuntimeException("Service non disponible.");
		}
		if (response.pdfContent==null)
		{
			throw new AmapjRuntimeException("Impossible de convertir le fichier : "+response.error);
		}
		
		return response.pdfContent;
	}
	
	private TNProxyClient getProxy()
	{
		AppConfiguration conf = AppConfiguration.getConf();
		return new TNProxyClient(conf.getMessagingCenterKey(),conf.getMessagingCenterUrl()); 
	}
}
