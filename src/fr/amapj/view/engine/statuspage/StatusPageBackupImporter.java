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
 package fr.amapj.view.engine.statuspage;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import com.vaadin.ui.Upload.Receiver;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.BoundedByteArrayOutputStream;
import fr.amapj.common.FormatUtils;
import fr.amapj.service.services.advanced.maintenance.MaintenanceService;



@SuppressWarnings("serial")
public class StatusPageBackupImporter implements Receiver
{
	
	public BoundedByteArrayOutputStream baos;

	private String filename;

	public StatusPageBackupImporter() 
	{
	}

	@Override
	public OutputStream receiveUpload(String filename, String mimeType)
	{
		this.filename = filename;
		baos = new BoundedByteArrayOutputStream(5_000_000,50_000_000); // Taile maxi : 50 Mo 
		return baos;
	}

	
	public Result getResult() 
	{
		try
		{
			return performResult();
		}
		catch (Exception e) 
		{
			Result r = new Result();
			r.errorMessage = e.getMessage();
			return r;
		}
	}
	
	private Result performResult() throws ParseException, IOException 
	{
		// filename : amap1_V044_2022_11_28_05_02_03.tar.gz
		// 32 : longueur de _V044_2022_11_28_05_02_03.tar.gz
		
		Result r = new Result();
		if (filename.length()<=32)
		{
			throw new RuntimeException("Nom de fichier incorrect : le nom du fichier doit avoir une longueur supérieure à 32");
		}
		
		if (filename.endsWith(".tar.gz")==false)
		{
			throw new RuntimeException("Nom de fichier incorrect : le nom du fichier doit se terminer par .tar.gz");
		}
		
		String dbName = filename.substring(0,filename.length()-32); 	
	    String extension = filename.substring(filename.length()-32,filename.length()-7);
	    
	    r.version = extension.substring(1,5);
	    r.isVersionOK = isVersionOK(r.version);
	    
	    SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
	    Date ref = df.parse(extension.substring(6,25));
		
	    r.content = "<ul>"
					+ "<li>Type de la base de données : SAUVEGARDE d'une base de données de production</li>"
					+ "<li>Nom de la base : "+dbName+"</li>"
					+ "<li>Date de la sauvegarde : "+FormatUtils.getTimeStd().format(ref)+"</li>"
					+ "<li>Version AMAPJ à la date de sauvegarde : "+r.version+"</li>"
					+ "<li>Login à utiliser : les mêmes qu'auparavant</li>"
					+ "<li>Mot de passe à utiliser : a</li>"
					+ "<li>(Tous les mots de passe ont été remplacés par a)</li>"
					+ "</ul>";
		
		GZIPInputStream zipIn = new GZIPInputStream(baos.getInputStream());
	    TarArchiveInputStream tarIn = new TarArchiveInputStream(zipIn);
	    
	    r.fileProperties = processEntry(tarIn,dbName,"properties");
	    r.fileScript = processEntry(tarIn,dbName,"script");
	   
	    tarIn.close();
		
		return r;
	}
	


	private boolean isVersionOK(String version) 
	{
		String currentVersion = new MaintenanceService().getShortVersion();
		return currentVersion.equals(version);
	}	

	private byte[] processEntry(TarArchiveInputStream tarIn,String dbName,String suffix) throws IOException
	{
		ArchiveEntry entry = tarIn.getNextEntry();
		
		if (entry.isDirectory()) 
	    {
			throw new AmapjRuntimeException();
	    }
		
        int count;
        int bufferSize = 128*1024;
        byte data[] = new byte[bufferSize];
        
        if (entry.getName().equals(dbName+"."+suffix)==false)
        {
        	throw new AmapjRuntimeException("entryName = "+entry.getName());
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BufferedOutputStream dest = new BufferedOutputStream(baos, bufferSize)) 
        {
            while ((count = tarIn.read(data, 0, bufferSize)) != -1) 
            {
                dest.write(data, 0, count);
            }
        }
        return baos.toByteArray();
	}


	static public class Result
	{
		public String errorMessage;
		
		public String warningVersionMessage;
		
		public String version;
		
		public boolean isVersionOK;
		
		public String content;
		
		public byte[] fileProperties;
		
		public byte[] fileScript;
	}
	
	

}
