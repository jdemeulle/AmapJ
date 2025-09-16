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
 package fr.amapj.view.views.appinstance;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import com.vaadin.ui.Upload.Receiver;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.BoundedByteArrayOutputStream;



@SuppressWarnings("serial")
public class BackupImporter implements Receiver 
{
	
	public BoundedByteArrayOutputStream baos;

	private String filename;

	public BackupImporter() 
	{
	}

	@Override
	public OutputStream receiveUpload(String filename, String mimeType)
	{
		this.filename = filename;
		baos = new BoundedByteArrayOutputStream(5_000_000,20_000_000); // Taile maxi : 20 Mo 
		return baos;
	}

	public String getNomInstance() 
	{
		return filename.substring(0,filename.length()-7); 	// 7 : .tar.gz
	}
	
	public void extractFile(String destDir) 
	{
		try
		{
		    GZIPInputStream zipIn = new GZIPInputStream(baos.getInputStream());
		    TarArchiveInputStream tarIn = new TarArchiveInputStream(zipIn);
		    
		    String shortName = filename.substring(0,filename.length()-32); 	// 32 : longueur de _V044_2022_11_28_05_02_03.tar.gz
		    String extension = filename.substring(filename.length()-32,filename.length()-7);
		   
		    processEntry(tarIn,shortName,"properties",destDir,extension);
		    processEntry(tarIn,shortName,"script",destDir,extension);
		   
		    
		    tarIn.close();
		}
		catch (IOException e) 
		{
			throw new AmapjRuntimeException(e);
		}
	    
	}
	
	
	private void processEntry(TarArchiveInputStream tarIn,String dbName,String suffix,String dirDest, String extension) throws IOException
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
        
        if (entry.getName().indexOf("..")!=-1 || entry.getName().indexOf('/')!=-1 || entry.getName().indexOf('\\')!=-1)
        {
        	throw new AmapjRuntimeException();
        }
        
        FileOutputStream fos = new FileOutputStream(dirDest+"/"+dbName+extension+"."+suffix, false);
        
        try (BufferedOutputStream dest = new BufferedOutputStream(fos, bufferSize)) 
        {
            while ((count = tarIn.read(data, 0, bufferSize)) != -1) 
            {
                dest.write(data, 0, count);
            }
        }            
	}

	
	

}
