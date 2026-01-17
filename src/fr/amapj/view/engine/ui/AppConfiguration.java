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
 package fr.amapj.view.engine.ui;

import java.util.Date;
import java.util.EnumSet;
import java.util.Properties;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.engine.dbms.DBMSConf;
import fr.amapj.model.engine.dbms.hsqlexternal.HsqlExternalDbmsConf;
import fr.amapj.model.engine.dbms.hsqlinternal.HsqlInternalDbmsConf;
import fr.amapj.service.engine.appinitializer.MockServletParameter;
import fr.amapj.service.engine.appinitializer.ServletParameter;
import fr.amapj.service.services.appinstance.AppInstanceDTO;

/**
 * Paramètres de configuration de l'application
 * 
 */
public class AppConfiguration
{

	static private AppConfiguration mainInstance;

	static public AppConfiguration getConf()
	{
		if (mainInstance == null)
		{
			throw new RuntimeException("Vous devez d'abord charger les parametres avec la methode load");
		}
		return mainInstance;
	}

	/**
	 * Permet le chargement des parametres
	 */
	static public void load(ServletParameter param)
	{
		if (mainInstance != null)
		{
			throw new RuntimeException("Impossible de charger deux fois les parametres");
		}

		mainInstance = new AppConfiguration();
		mainInstance.loadInternal(param);
	}

	private AppConfiguration()
	{
	}

	// Répertoire pour la sauvegarde de la base
	private String backupDirectory;
	
	// Commande additionnelle qui sera executée après la sauvegarde (par exemple, copie sur un autre disque, ...)
	private String backupCommand;
	
	// Clé de securité sur 40 caractères pour les operations de maintenance (download des backups, extraction des stats, ...)  
	private String maintenanceKey;
	
	// 
	private boolean pdfConversionWithMessagingCenter;
	
	// Chemin complet jusqu'a l'executable wkhtmltopdf
	private String wkhtmltopdfCommand = null;
	
	// Indique si les administrateurs ont des droits complets ou limités
	// Dans le cas limités, il ne peuvent pas modifier les paramètres envoi de mail , ...
	private boolean adminFull = true;
	
	// Memorisation du contextPath (retourné par 
	private String contextPath;
	
	// Inqiue si il est possible de modifier l'heure courante 
	private boolean allowTimeControl = false;
	
	// Indique si le status page  mode est activé  
	private boolean statusPageAllowed = false;
	
	// Indique que les mails ne sont pas envoyés mais uniquement stockés en mémoire pour debug  
	private MailConfig mailConfig;
	
	// Permet de forcer l'url de base qui sera utilisée pour le SUDO 
	// Pratique pour analyser les bases de production sur une machine de test 
	private String overrideSudoBaseUrl = null;
	
	private String messagingCenterUrl = null;
	
	private String messagingCenterKey = null;
	
	// Storage de type S3
	private String s3StorageEndPoint = null;
	private String s3StorageLogin = null;
	private String s3StoragePassword = null;
	private String s3StorageBucket = null;
	private String s3StorageKeyPrefix = null;
	
	//
	private String localCacheDir;
	private String localCacheSize;
	

	//
	private DBMSConf dbmsConf;
	
	private AppInstanceDTO masterConf;

	private void loadInternal(ServletParameter param)
	{
		contextPath = param.getContextPath();

		// TODO verifier que c'est bien un directory
		backupDirectory = param.read("database.backupdir");
		
		backupCommand = param.read("database.backupCmd");
		
		maintenanceKey = param.read("maintenanceKey");
		
		pdfConversionWithMessagingCenter =  param.read("pdfConversionWithMessagingCenter", "FALSE").equalsIgnoreCase("TRUE");
		
		wkhtmltopdfCommand = param.read("wkhtmltopdf");
		
		// TODO verifier que c'est bien un directory
		String logDir = param.read("logDir");
		AmapJLogManager.setLogDir(logDir);
		
		// 
		adminFull = (param.read("adminFull", "TRUE")).equalsIgnoreCase("TRUE");
		
		//
		allowTimeControl = (param.read("allowTimeControl", "FALSE")).equalsIgnoreCase("TRUE");
		
		//
		statusPageAllowed = (param.read("statusPageAllowed", "FALSE")).equalsIgnoreCase("TRUE");

		//
		mailConfig = readMailConfig(param.read("mailConfig"));

		//
		overrideSudoBaseUrl = param.read("overrideSudoBaseUrl");
		
		//
		messagingCenterUrl = param.read("messagingCenterUrl");
		messagingCenterKey = param.read("messagingCenterKey");
		
		//
		s3StorageEndPoint = param.read("s3StorageEndPoint");
		s3StorageLogin = param.read("s3StorageLogin");
		s3StoragePassword = param.read("s3StoragePassword");
		s3StorageBucket = param.read("s3StorageBucket");
		s3StorageKeyPrefix = param.read("s3StorageKeyPrefix");
		
		//
		localCacheDir = param.read("localCacheDir");
		localCacheSize = param.read("localCacheSize");
		
		
		// Lecture des DBMS
		String dbms =  param.read("dbms");
		dbmsConf = createDbmsConf(dbms,param);
		dbmsConf.load(param);
		
		
		
		// Lecture de la base master
		masterConf = createMasterConf(param);
		
	}
	
	
	private MailConfig readMailConfig(String read) 
	{
		EnumSet<MailConfig> enums = EnumSet.allOf(MailConfig.class);
		for (MailConfig en : enums)
		{
			if (en.name().equalsIgnoreCase(read)) 
			{
				return en;
			}
		}
		return MailConfig.NO_MAIL;
	}

	private AppInstanceDTO createMasterConf(ServletParameter param)
	{
		AppInstanceDTO dto = new AppInstanceDTO();
		
		dto.id = 0L;
		
		dto.nomInstance = param.read("master.name");
		
		dto.dbUserName = param.read("master.user");
		
		dto.dbPassword = param.read("master.password");
		
		dto.dateCreation = new Date(0);
		
		return dto;
	}

	private DBMSConf createDbmsConf(String dbmsName, ServletParameter param)
	{
		DBMSConf res;
		
		String type =  param.read("dbms."+dbmsName+".type");
		
		if (type.equals("hsql_internal"))
		{
			res = new HsqlInternalDbmsConf(dbmsName);
		}
		else if (type.equals("hsql_external"))
		{
			res = new HsqlExternalDbmsConf(dbmsName);
		} 
		else
		{
			throw new AmapjRuntimeException("Le type <"+type+"> n'est pas reconnu ");
		}
		return res;
	}
	

	public String getBackupDirectory()
	{
		return backupDirectory;
	}
	
	public DBMSConf getDbmsConf()
	{
		return dbmsConf;
	}
	
	public AppInstanceDTO getMasterConf()
	{
		return masterConf;
	}
	
	public boolean isAdminFull()
	{
		return adminFull;
	}
	
	public boolean isAllowTimeControl()
	{
		return allowTimeControl;
	}
	
	public boolean isStatusPageAllowed()
	{
		return statusPageAllowed;
	}

	public String getBackupCommand()
	{
		return backupCommand;
	}
	
	public String getMaintenanceKey()
	{
		return maintenanceKey;
	}

	public String getWkhtmltopdfCommand()
	{
		return wkhtmltopdfCommand;
	}

	public String getContextPath()
	{
		return contextPath;
	}

	public String getOverrideSudoBaseUrl() 
	{
		return overrideSudoBaseUrl;
	}

	public String getMessagingCenterUrl()
	{
		return messagingCenterUrl;
	}

	public String getMessagingCenterKey()
	{
		return messagingCenterKey;
	}
	
	public MailConfig getMailConfig() 
	{
		return mailConfig;
	}

	public boolean isPdfConversionWithMessagingCenter() 
	{
		return pdfConversionWithMessagingCenter;
	}
	
	public String getS3StorageEndPoint()
	{
		return s3StorageEndPoint;
	}

	public String getS3StorageLogin()
	{
		return s3StorageLogin;
	}

	public String getS3StoragePassword()
	{
		return s3StoragePassword;
	}

	public String getS3StorageBucket()
	{
		return s3StorageBucket;
	}
	
	public String getS3StorageKeyPrefix() 
	{
		return s3StorageKeyPrefix;
	}

	public String getLocalCacheDir() 
	{
		return localCacheDir;
	}

	public String getLocalCacheSize() 
	{
		return localCacheSize;
	}

	/**
	 * Permet de créer une configuration pour les tests
	 * 
	 * ATTENTION : cette méthode doit être appelée uniquement par TestTools.init()
	 */
	public static void initializeForTesting()
	{
		mainInstance = new AppConfiguration();

		Properties prop = new Properties();
		
		prop.put("dbms", "he");
		prop.put("dbms.he.type", "hsql_external");
		prop.put("dbms.he.ip", "127.0.0.1");
		prop.put("dbms.he.port", "9001");
		 
		prop.put("master.dbms","he");
		prop.put("master.name","master");
		prop.put("master.user","SA");
		prop.put("master.password","");
		 
		prop.put("logDir","../logs/");

		MockServletParameter param = new MockServletParameter(prop);
		
		mainInstance.loadInternal(param);
		
		
	}
		
	
	/**
	 * Permet de créer une configuration pour les tests UNITAIRES 
	 * 
	 * ATTENTION : cette méthode doit être appelée uniquement par EngineTester
	 */
	public static void initializeForUnitTesting()
	{
		mainInstance = new AppConfiguration();

		Properties prop = new Properties();
		
		//  
		prop.put("dbms", "hi");
		prop.put("dbms.hi.type", "hsql_internal");
		prop.put("dbms.hi.port", "9500");
		prop.put("dbms.hi.dir", "c:/prive/dev/amapj/git/amapj-dev/tests-units/db/data");
		
		 
		prop.put("master.dbms","hi");
		prop.put("master.name","master");
		
		prop.put("mailConfig","storage");
		
		prop.put("logDir","../../logs/");
		
		prop.put("wkhtmltopdf","C:/Program Files/wkhtmltopdf/bin/wkhtmltopdf.exe");
		
		prop.put("s3StorageEndPoint","http://127.0.0.1:9000/");
		prop.put("s3StorageLogin","s3login");
		prop.put("s3StoragePassword","s3password");
		prop.put("s3StorageBucket","abucket");
		prop.put("s3StorageKeyPrefix","slot1");
		prop.put("localCacheDir","c:/prive/dev/amapj/serverfiles/tests-units/localcache");
		prop.put("localCacheSize","1000");
		


		MockServletParameter param = new MockServletParameter(prop);
		
		mainInstance.loadInternal(param);
		
		
	}
	
	static public enum MailConfig
	{
		// Les mails ne sont pas envoyés
		NO_MAIL,
		
		// Les mails sont envoyés en utilisant l'API GMAIL
		GMAIL , 
		
		// Les mails sont envoyés avec un postfix local
		POSTFIX_LOCAL , 
		
		// Les mails sont envoyés par le messaging center
		MESSAGING_CENTER , 
		
		// Les mails sont stockés localement en mémoire (pour debug)  
		STORAGE
	}

}
