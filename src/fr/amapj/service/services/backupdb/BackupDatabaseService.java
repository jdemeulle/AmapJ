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
 package fr.amapj.service.services.backupdb;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.DateUtils;
import fr.amapj.common.FormatUtils;
import fr.amapj.model.engine.db.DbManager;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.tools.SpecificDbUtils;
import fr.amapj.model.engine.tools.TestTools;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.param.paramecran.PEExtendedParametres;
import fr.amapj.service.engine.deamons.DeamonsContext;
import fr.amapj.service.engine.deamons.DeamonsUtils;
import fr.amapj.service.services.advanced.maintenance.MaintenanceService;
import fr.amapj.service.services.mailer.MailerAttachement;
import fr.amapj.service.services.mailer.MailerMessage;
import fr.amapj.service.services.mailer.MailerService;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.ui.AppConfiguration;

/**
 * Sauvegarde de la base
 *
 */
public class BackupDatabaseService implements Job
{
	private final static Logger logger = LogManager.getLogger();
	
	
	public BackupDatabaseService()
	{
		
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException
	{
		// Lecture du contenu du mail dans la base Master
		PEExtendedParametres pe = SpecificDbUtils.executeInMaster(()->(PEExtendedParametres) new ParametresService().loadParamEcran(MenuList.EXTENDED_PARAMETRES));
		
		// Réalisation de la sauvegarde dans chaque base
		DeamonsUtils.executeAsDeamon(getClass(), e->backupDatabase(e,pe.masterDbEmailBackupContent));
	}
	
	
	/**
	 * Permet de faire le backup de la base , et son envoi par mail 
	 * Ceci est vérifié dans une transaction en ecriture  
	 */
	@DbWrite
	public void backupDatabase(DeamonsContext deamonsContext,String emailBackupContent)
	{
		RdbLink em = RdbLink.get();
		
		String dbName = DbManager.get().getCurrentDb().getDbName();
		String version = new MaintenanceService().getShortVersion();
		
		logger.info("Debut de la sauvegarde de la base pour "+dbName);
		
		String backupDir = AppConfiguration.getConf().getBackupDirectory();
		if (backupDir==null)
		{
			throw new AmapjRuntimeException("Le répertoire de stockage des sauvegardes n'est pas défini");
		}
		
		Date ref = DateUtils.getDate();
		SimpleDateFormat df1 = new SimpleDateFormat("yyyy_MM_dd");
		SimpleDateFormat df2 = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		String shortRepName = df1.format(ref);
		String shortFileName = dbName+"_"+version+"_"+df2.format(ref)+".tar.gz";
		
		String fullRepName = backupDir+"/"+shortRepName;
		String fullFileName = fullRepName+"/"+shortFileName;
		
		
		File rep = new File(fullRepName);
		if (rep.exists()==false)
		{
			boolean ret = rep.mkdir();
			if (ret==false)
			{
				throw new AmapjRuntimeException("Impossible de créer le repertoire :"+fullRepName);
			}
		}
		
		
		
		em.createNativeQuery("BACKUP DATABASE TO '"+fullFileName+"' BLOCKING").executeUpdate();
		
		File file= new File(fullFileName);
		if (file.canRead()==false)
		{
			throw new AmapjRuntimeException("Erreur lors de la sauvegarde. Impossible de lire le fichier");
		}
		
	
		// On envoie ensuite le fichier par mail 
		ParametresDTO param = new ParametresService().getParametres();
		String htmlContent = computeHtmlContent(param,emailBackupContent,version,ref,dbName);
		MailerMessage message = new MailerMessage(param.backupReceiver,"Backup de la base de "+param.nomAmap,htmlContent);
		message.addAttachement(new MailerAttachement(file));
		
		new MailerService().sendHtmlMail(message);
		
		logger.info("Fin de la sauvegarde de la base pour "+dbName);
			
	}
	

	private String computeHtmlContent(ParametresDTO param, String htmlContent, String version, Date ref, String dbName) 
	{
		if (htmlContent==null)
		{
			htmlContent = "Sauvegarde de la base #DB# (Nom de l'AMAP : #NOM_AMAP#)";
		}
		
		// Mise en place des <br/>
		htmlContent = htmlContent.replaceAll("\r\n", "<br/>");
		htmlContent = htmlContent.replaceAll("\n", "<br/>");
		htmlContent = htmlContent.replaceAll("\r", "<br/>");
		
		
		// Remplacement des zones de textes
		htmlContent = htmlContent.replaceAll("#NOM_AMAP#", param.nomAmap);
		htmlContent = htmlContent.replaceAll("#DATE_REF#", FormatUtils.getTimeStd().format(ref));
		htmlContent = htmlContent.replaceAll("#VERSION#", version);
		htmlContent = htmlContent.replaceAll("#DB#", dbName);
		
		
		
		
		return htmlContent;
	}
	
	

	public static void main(String[] args)
	{
		TestTools.init();
		
		new BackupDatabaseService().backupDatabase(new DeamonsContext(),"");
	}


}
