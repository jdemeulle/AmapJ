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
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.messagingcenter.miniproxy.core.BasicHttpDeserializer;
import fr.amapj.messagingcenter.miniproxy.core.BasicHttpSerializer;
import fr.amapj.service.services.appinstance.AppInstanceService;
import fr.amapj.view.engine.ui.AppConfiguration;

/**
 * Servlet pour fournir des informations générales
 * 
 */
public class InfoServlet extends HttpServlet 
{
	
	private final static Logger logger = LogManager.getLogger();
	
	public enum InfoType
	{
		MONITOR , 
		MAINTENANCE,
		BACKUP , 
		APPINSTANCE_STAT_INFO,
		TABLE_SIZE
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException 
	{
		InfoType type = getInfoType(req.getPathInfo());
		if (type==null)
		{
			return;
		}
		
		switch (type)
		{
		case MONITOR:
			doMonitor(req,res);
			break;

		case MAINTENANCE:
			doMaintenance(res);
			break;
			
		case BACKUP:
			doBackup(req,res);
			break;
			
		case APPINSTANCE_STAT_INFO:
			doAppInstanceStatInfo(req,res);
			break;
		
		case TABLE_SIZE:
			doTableSize(req,res);
			break;
			
		default:
			// Do nothing 
			break;
		}	
	}

	private InfoType getInfoType(String pathInfo)
	{
		if (pathInfo==null)
		{
			return null;
		}
		
		if (pathInfo.startsWith("/monitor"))
		{
			return InfoType.MONITOR;
		}
		
		if (pathInfo.startsWith("/maintenance"))
		{
			return InfoType.MAINTENANCE;
		}
		
		if (pathInfo.startsWith("/backup"))
		{
			return InfoType.BACKUP;
		}
		
		if (pathInfo.startsWith("/appinstance_stat_info"))
		{
			return InfoType.APPINSTANCE_STAT_INFO;
		}
		
		if (pathInfo.startsWith("/table_size"))
		{
			return InfoType.TABLE_SIZE;
		}
		
		return null;
		
	}

	
	private void doMaintenance(HttpServletResponse res) throws IOException
	{
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		
		out.write("<html><head><title></title></head><body>");
		
		out.write("<h1>Application non disponible</h1>");
		out.write("<p>Désolé, l'application est en cours de maintenance ou votre abonnement a expiré. Merci de vous reconnecter plus tard.</p>");
		out.write("</body></html>");		
	}
	
	
	/**
	 * Permet le monitoring des différentes valeurs du serveur
	 */
	private void doMonitor(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		logger.info("Debut du doMonitor");
		
		BasicHttpDeserializer bis = new BasicHttpDeserializer(req.getInputStream());
		BasicHttpSerializer bos = new BasicHttpSerializer(res.getOutputStream());		
		
		//
		checkMaintenanceKey(bis);
		
		// 
		int top = bis.readInt();
		String str =  MonitorInfo.performMonitorInfo(top==1);
		bos.writeString(str);
		bos.flush();
	}	
	
	
	/**
	 * Calcul de la taille des tables
	 */
	private void doTableSize(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		logger.info("Debut du doTableSize");
		
		BasicHttpDeserializer bis = new BasicHttpDeserializer(req.getInputStream());
		BasicHttpSerializer bos = new BasicHttpSerializer(res.getOutputStream());		
		
		//
		checkMaintenanceKey(bis);
		
		// 
		int size = bis.readInt();
		String str =  new AppInstanceService().getSchemaAllBases(size==1);
		bos.writeString(str);
		bos.flush();
	}	
	
	
	
	private void doBackup(HttpServletRequest req, HttpServletResponse res) throws IOException 
	{
		logger.info("Debut du backup");
		
		
		BasicHttpDeserializer bis = new BasicHttpDeserializer(req.getInputStream());
		BasicHttpSerializer bos = new BasicHttpSerializer(res.getOutputStream());		
		
		//
		checkMaintenanceKey(bis);
		
		String backupDir = AppConfiguration.getConf().getBackupDirectory();
		String date =  bis.readString();
				
		// Verification de la validité de la date 
		for (int i = 0; i < date.length(); i++) 
		{
			char c = date.charAt(i);
			if (isAllowed(c)==false)
			{
				throw new IOException("Erreur de validité de la date - c="+c);
			}
		}
		
		logger.info("Date du backup : "+date);
		
		File[] fs = new File(backupDir+"/"+date).listFiles();
		if (fs==null)
		{
			throw new IOException("Le repertoire "+backupDir+"/"+date+" n'existe pas.");
		}
		
		bos.writeInt(fs.length);
		
		for (File file : fs) 
		{
			logger.info("Envoi du fichier : "+file.getName());
			bos.writeFile(file);
		}

		bos.flush();
	}


	private boolean isAllowed(char c) 
	{
		if (c=='_')
		{
			return true;
		}
		if (c>='0' && c<='9')
		{
			return true;
		}
		return false;
	}



	
	private void doAppInstanceStatInfo(HttpServletRequest req, HttpServletResponse res) throws IOException 
	{
		BasicHttpDeserializer bis = new BasicHttpDeserializer(req.getInputStream());
		BasicHttpSerializer bos = new BasicHttpSerializer(res.getOutputStream());		
		
		//
		checkMaintenanceKey(bis);
		
		//
		String str = new AppInstanceService().getStatInfo();
		bos.writeGzipString(str);
		bos.flush();
	}


	
	
	private void checkMaintenanceKey(BasicHttpDeserializer bis) throws IOException 
	{
		logger.info("Debut verification de la cle de maintenance");
	
		String maintenanceKey = AppConfiguration.getConf().getMaintenanceKey();
		String key = bis.readString(200);
		
		//
		if (maintenanceKey.length()<40 || key.equals(maintenanceKey)==false)
		{
			logger.info("Cle de maintenance erronnée ou trop courte");
			throw new IOException("Erreur dans la clé de maintenance");
		}
		logger.info("Cle de maintenance correcte");
		
	}
}
