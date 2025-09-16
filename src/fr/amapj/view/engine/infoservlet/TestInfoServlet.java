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
 package fr.amapj.view.engine.infoservlet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fr.amapj.messagingcenter.miniproxy.core.BasicHttpClient;
import fr.amapj.messagingcenter.miniproxy.core.BasicHttpDeserializer;
import fr.amapj.messagingcenter.miniproxy.core.BasicHttpSerializer;
import fr.amapj.messagingcenter.miniproxy.core.BasicHttpDeserializer.FileInfo;

public class TestInfoServlet 
{
	static private String urlWebServer = "http://127.0.0.1:8080/amapj/infoservlet/";
	static private String key = "0123456789012345678901234567890123456789";
	
	
	public static void main(String[] args) throws IOException 
	{
		// new TestInfoServlet().testMonitor();
		new TestInfoServlet().testBackup();
	}

	
	private void testMonitor() throws IOException 
	{
		BasicHttpClient bc = new BasicHttpClient(urlWebServer+"monitor");
		
		// Ecriture de la clé et de la date demandée
		BasicHttpSerializer bos = bc.startWrite();
		bos.writeString(key);
		bos.writeInt(0);
		
		// Lecture de la réponse
		BasicHttpDeserializer bis = bc.startRead();
		
		// Nombre de fichiers
		String str = bis.readString();
		System.out.println("Res="+str);
		
		bc.end();
				
	}
	
	private void testBackup() throws IOException 
	{
		String date = new SimpleDateFormat("yyyy_MM_dd").format(new Date());
		
		BasicHttpClient bc = new BasicHttpClient(urlWebServer+"backup");
		
		// Ecriture de la clé et de la date demandée
		BasicHttpSerializer bos = bc.startWrite();
		bos.writeString(key);
		bos.writeString(date);
		
		// Lecture de la réponse
		BasicHttpDeserializer bis = bc.startRead();
		
		// Nombre de fichiers
		int nb = bis.readInt();
		System.out.println("Nombre de fichiers à télécharger : "+nb);
		
		for (int i = 0; i < nb; i++) 
		{
			FileInfo f = bis.readFileAndCopyTo(new File("c:/prive/dev/amapj/tmp/"+date));
			
			String msg ="Transfert du fichier : "+f.fileName+" Size = "+f.fileSize+" effectué.";
			System.out.println(msg);
		}
		
		bc.end();
	}
}
