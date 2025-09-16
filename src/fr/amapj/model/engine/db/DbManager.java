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
 package fr.amapj.model.engine.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.engine.dbms.DBMS;
import fr.amapj.model.engine.dbms.DBMSConf;
import fr.amapj.model.engine.dbms.hsqlexternal.HsqlExternalDbms;
import fr.amapj.model.engine.dbms.hsqlexternal.HsqlExternalDbmsConf;
import fr.amapj.model.engine.dbms.hsqlinternal.HsqlInternalDbms;
import fr.amapj.model.engine.dbms.hsqlinternal.HsqlInternalDbmsConf;
import fr.amapj.model.engine.tools.SpecificDbUtils;
import fr.amapj.model.engine.transaction.DataBaseInfo;
import fr.amapj.service.services.appinstance.AppInstanceDTO;
import fr.amapj.service.services.appinstance.AppState;
import fr.amapj.service.services.demoservice.DemoService;
import fr.amapj.service.services.demoservice.ReplaceDatabaseService;
import fr.amapj.service.services.session.SessionManager;

public class DbManager
{
	
	private final static Logger logger = LogManager.getLogger();
	
	// Le DBMS qui gère les bases de données 
	private DBMS dbms; 
	
	/**
	 * Liste des bases de données gérées par l'application , y compris celles qui sont arrêtées
	 * 
	 * La premiere base de données est la base de données MASTER
	 */
	private List<DataBaseInfo> dataBaseInfos;
	
	/**
	 * Permet le stockage du nom de la base dans le cas des démons
	 */
	private ThreadLocal<DataBaseInfo> demonDbName = new ThreadLocal<DataBaseInfo>();

	
	private static DbManager mainInstance;

	private DBMSConf dbmsConf;

	private AppInstanceDTO masterConf;
	
	
	public static DbManager get()
	{
		return mainInstance;
	}
	
	public static void initialize(DBMSConf dbmsConf,AppInstanceDTO masterConf)
	{
		mainInstance = new DbManager(dbmsConf,masterConf);
	}
	
	private DbManager(DBMSConf dbmsConf, AppInstanceDTO masterConf)
	{
		this.dbmsConf = dbmsConf;
		this.masterConf = masterConf;
		dataBaseInfos = Collections.synchronizedList(new ArrayList<DataBaseInfo>());
	}
		
	/**
	 * Démarrage du DBMS
	 */
	public void startDbms()
	{	
		dbms = createDBMS(dbmsConf);
		dbms.startDBMS();
	}
	
	
	
	private DBMS createDBMS(DBMSConf dbmsConf)
	{
		DBMS res;
		if (dbmsConf instanceof HsqlInternalDbmsConf)
		{
			res = new HsqlInternalDbms( (HsqlInternalDbmsConf) dbmsConf); 
		}
		else if (dbmsConf instanceof HsqlExternalDbmsConf)
		{
			res = new HsqlExternalDbms( (HsqlExternalDbmsConf) dbmsConf);
		}
		else
		{
			throw new AmapjRuntimeException("Erreur");
		}
		return res;
	}



	/**
	 * Démarrage de la base MASTER
	 */
	public void startMasterBase()
	{
		addDataBase(masterConf, AppState.ON);
	}
	
	
	
	/**
	 * Permet l'ajout d'une base de données au niveau de la liste des bases gérées, avec l'état indiqué
	 * 
	 * La base de données doit déjà être existante dans le DBMS
	 */
	public void addDataBase(AppInstanceDTO dto,AppState state)
	{	
		// On enregistre la base à l'etat OFF 
		registerDataBase(dto.nomInstance, dto.dbUserName, dto.dbPassword);
		
		// On positionne son état comme demandé 
		setDbState(dto.nomInstance,state);
		
		
	}
	
	
	/**
	 * Arret du DBMS
	 */
	public void stopDbms()
	{
		dbms.stopDBMS(dataBaseInfos);
	}



	/**
	 * Cette méthode permet la création d'une NOUVELLE base de donnée
	 * 
	 * Cette méthode doit :
	 * - créer la base 
	 * - la démarrer 
	 * - l'enregistrer au niveau de DbUtil
	 * - la remplir avec les données indiquées
	 * 
	 * En fin de cette méthode , la base est à l'état démarrée 
	 * 
	 * @param dto
	 */
	public void createDataBase(AppInstanceDTO dto)
	{
		// On vérifie que la base n'existe pas déjà
		if (findDataBaseFromName(dto.nomInstance)!=null)
		{
			throw new AmapjRuntimeException("La base existe déjà");
		}
		
		// On enregistre la base de données à l'état OFF
		DataBaseInfo dataBaseInfo = registerDataBase(dto.nomInstance, dto.dbUserName, dto.dbPassword);
		
		// Le DBMS crée la base
		dbms.createOneBase(dataBaseInfo);
		
		// On positionne la base à l'état ON 
		setDbState(dto.nomInstance,AppState.ON);
	
		// On remplit la base de données, en faisant un appel de service dans cette base
		SpecificDbUtils.executeInSpecificDb(dto.nomInstance, ()->new DemoService().generateDemoData(dto));
	}
	

	/**
	 * Cette méthode permet la restauration d'une base de donnée à partir d'une sauvegarde
	 * 
	 * Cette méthode doit :
	 * - copier les fichiers 
	 * - démarrer la base  
	 * - l'enregistrer au niveau de DbUtil
	 * - la modifier (on efface les informations pour l'envoi des mails par sécurité) 
	 * 
	 * En fin de cette méthode , la base est à l'état démarrée 
	 * 
	 * @param dto
	 * @param backupImporter 
	 */
	/*
	public void restoreDataBase(AppInstanceDTO dto, BackupImporter backupImporter) 
	{
		String dbName = appInstanceDTO.nomInstance; 
		
		// On copie les fichiers au bon endroit  
		backupImporter.extractFile(conf.getContentDirectory());
		
		// On démarre ensuite la base de données
		startOneBase(dbName);

		// On enregistre la base de données pour qu'elle soit accessible par les services
		registerDb(appInstanceDTO, AppState.ON);
		
		// On efface les données sensibles de la base de données, en faisant un appel de service dans cette base 
		// TODO SpecificDbUtils.executeInSpecificDb(dbName, ()->new DemoService().generateDemoData(appInstanceDTO));
	}
	*/

	
	/**
	 * Cette méthode permet de remplacer une base de donnée par une autre 
	 * à partir du contenu des deux fichiers
	 * 
	 * Cette méthode doit :
	 * - arreter la base existante 
	 * - copier les fichiers 
	 * - redémarrer la base  
	 * - la modifier (on efface les informations pour l'envoi des mails par sécurité) 
	 * 
	 * En fin de cette méthode , la base est à l'état démarrée 
	 * 
	 */
	public void replaceDataBase(DataBaseInfo dataBaseInfo, byte[] fileProperties,byte[] fileScript) 
	{
		// On arrete le persistenceManager
		dataBaseInfo.closeEntityManagerFactory();

		dbms.replaceOneBase(dataBaseInfo, fileProperties, fileScript);
		
		// On démarre le persistenceManager
		dataBaseInfo.renewEntityManagerFactory();
		
		// On efface tous les mots de passe, en faisant un appel de service dans cette base 
		SpecificDbUtils.executeInSpecificDb(dataBaseInfo.dbName, ()->new ReplaceDatabaseService().resetAllPassword());
					
		
	}

	// PARTIE EN PROVENANCE DE DBUTIL 
	
	/**
	 * Retourne la base de données courante
	 */
	public DataBaseInfo getCurrentDb()
	{
		// Le nom de la base provient soit d'une variable positionnée par le démon, soit du contexte de la session
		DataBaseInfo dbName = demonDbName.get();
		if (dbName==null)
		{
			dbName = SessionManager.getDb();
		}
		return dbName;
	}
	
	
	
	/**
	 * Permet d'ajouter une base de données dans la liste des bases de données gérées
	 * La base est à l'état OFF 
	 * @return 
	 */
	private DataBaseInfo registerDataBase(String dbName,String user,String password)
	{
		// On vérifie d'abord que cette base n'existe pas déjà
		if (findDataBaseFromName(dbName)!=null)
		{
			throw new AmapjRuntimeException("Il est interdit de créer deux fois la base de données : "+dbName);
		}
		
		// On demande au dbms de calculer l'url de la base de données  
		String[] us = dbms.computeUrlUserPassword(dbName,user,password);
		
		// On ajoute cette base dans la liste des bases gérées
		int puNumber = dataBaseInfos.size();
		DataBaseInfo dataBaseInfo = new DataBaseInfo(dbName, us[0], us[1], us[2], puNumber);
		dataBaseInfos.add(dataBaseInfo);	
		return dataBaseInfo;
	}
	
	/**
	 * Retourne la  base de données avec ce nom
	 * 
	 * Retourne null si elle n'existe pas
	 */
	public DataBaseInfo findDataBaseFromName(String dbName)
	{
		if (dbName==null)
		{
			return null;
		}
		for (DataBaseInfo dataBaseInfo : dataBaseInfos)
		{
			if (dataBaseInfo.getDbName().equals(dbName))
			{
				return dataBaseInfo;
			}
		}
		return null;
	}
	
	
	/*
	 * Partie specificque pour les demons
	 */
	
	/**
	 * Permet d'indiquer le nom de la base sur laquelle s'execute le demon
	 */
	public void setDbForDeamonThread(DataBaseInfo dataBaseInfo)
	{
		demonDbName.set(dataBaseInfo);
	}
	
	/**
	 * Retourne la liste de toutes les bases, dans l'ordre de leur création
	 */
	public List<DataBaseInfo> getAllDbs()
	{
		return dataBaseInfos;
	}
	
	/**
	 * Retourne la base MASTER
	 */
	public DataBaseInfo getMasterDb()
	{
		return dataBaseInfos.get(0);
	}
	
	
	/*
	 * Partie Changement d'état d'une base de données 
	 */
	
	public void setDbState(String dbName,AppState appState)
	{
		DataBaseInfo dataBaseInfo = findDataBaseFromName(dbName);

		// La base n'existe pas du tout
		if (dataBaseInfo == null)
		{
			throw new AmapjRuntimeException("Base non trouvée");
		}

		// Réalisation des opérations nécessaires
		switch (dataBaseInfo.getState())
		{
		case OFF:
			changeStateFromOff(dataBaseInfo,appState);
			break;

		case DATABASE_ONLY:
			changeStateFromDatabaseOnly(dataBaseInfo,appState);
			break;

		case ON:
			changeStateFromOn(dataBaseInfo,appState);
			break;

		default:
			throw new AmapjRuntimeException("Erreur de programmation");
		}
		
		// On mémorise son état
		dataBaseInfo.setState(appState);
		
	}

	private void changeStateFromOn(DataBaseInfo dataBaseInfo, AppState appState)
	{
		switch (appState)
		{
		case ON:
		case DATABASE_ONLY:
			// Rien à faire
			break;

		case OFF:
			dbms.stopOneBase(dataBaseInfo);
			dataBaseInfo.closeEntityManagerFactory();
			break;
		
		default:
			throw new AmapjRuntimeException("Erreur de programmation");
		}

	}

	private void changeStateFromDatabaseOnly(DataBaseInfo dataBaseInfo, AppState appState)
	{
		switch (appState)
		{
		case ON:
		case DATABASE_ONLY:
			// Rien à faire
			break;

		case OFF:
			dbms.stopOneBase(dataBaseInfo);
			dataBaseInfo.closeEntityManagerFactory();
			break;
		
		default:
			throw new AmapjRuntimeException("Erreur de programmation");
		}

	}

	private void changeStateFromOff(DataBaseInfo dataBaseInfo, AppState appState)
	{
		switch (appState)
		{
		case ON:
		case DATABASE_ONLY:
			dbms.startOneBase(dataBaseInfo);
			dataBaseInfo.renewEntityManagerFactory();
			break;

		case OFF:
			// Rien à faire
			break;
		
		default:
			throw new AmapjRuntimeException("Erreur de programmation");
		}		
	}	 
	
	
	/*
	 * Partie requête SQL 
	 */
	
	/**
	 * Permet l'execution d'une requete SQL d'update ou d'insert ou de modification du schéma sur la base indiquée
	 * Retourne le nombre de lignes modifiées
	 */
	public int executeUpdateSqlCommand(String sqlCommand,AppInstanceDTO dto) throws SQLException
	{
		DataBaseInfo dataBaseInfo = findDataBaseFromName(dto.nomInstance);
		
		return dbms.executeUpdateSqlCommand(dataBaseInfo,sqlCommand);
	}
	
	/**
	 * Permet l'execution d'une requete SQL de requete (SELECT ...)
	 * Retourne le resultat de la requete
	 */
	public List<List<String>> executeQuerySqlCommand(String sqlCommand,AppInstanceDTO dto) throws SQLException
	{
		DataBaseInfo dataBaseInfo = findDataBaseFromName(dto.nomInstance);
		
		return dbms.executeQuerySqlCommand(dataBaseInfo,sqlCommand);
	}

	
}
