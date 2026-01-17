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
 package fr.amapj.model.engine.dbms.hsqlinternal;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.Server;
import org.hsqldb.server.ServerConfiguration;
import org.hsqldb.server.ServerConstants;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.StackUtils;
import fr.amapj.model.engine.dbms.DBMS;
import fr.amapj.model.engine.dbms.DBMSTools;
import fr.amapj.model.engine.ddl.MakeSqlSchemaForEmptyDb;
import fr.amapj.model.engine.transaction.DataBaseInfo;
import fr.amapj.service.services.appinstance.AppInstanceDTO;


/**
 * Cette classe permet de gérer un serveur Hsql intégré à l'application
 *
 */
public class HsqlInternalDbms implements DBMS
{
	
	private final static Logger logger = LogManager.getLogger();
	
	// Reference vers le serveur base de données
	private Server server;
	
	private HsqlInternalDbmsConf conf;
	
	public HsqlInternalDbms(HsqlInternalDbmsConf conf)
	{
		this.conf = conf;
	}
	
	/**
	 * Réalise l'initialisation du DBMS , si nécessaire
	 * @param conf
	 */
	@Override
	public void startDBMS()
	{
		// Chargement du driver pour pouvoir ouvrir les bases plus tard
		try
		{
			Class.forName("org.hsqldb.jdbcDriver");
		} 
		catch (ClassNotFoundException e1)
		{
			throw new AmapjRuntimeException("Erreur au chargement du driver hsqldb",e1);
		}
		
		//
		HsqlProperties argProps = new HsqlProperties();

		argProps.setProperty("server.no_system_exit", "true");
		argProps.setProperty("server.port", conf.getPort());
		argProps.setProperty("server.remote_open", true);
		argProps.setProperty("server.maxdatabases", 200);
		

		ServerConfiguration.translateAddressProperty(argProps);

		// finished setting up properties;
		server = new Server();

		try
		{
			server.setProperties(argProps);
		} 
		catch (Exception e)
		{
			logger.warn("Impossible de démarrer correctement le DBMS : " + StackUtils.asString(e));
			throw new RuntimeException("Impossible de démarrer le DBMS");
		}

		server.start();
		server.checkRunning(true);		
	}


	
	/**
	 * Permet le démarrage d'une nouvelle base de données avec HSQL
	 * 
	 *  Si la base de données n'existe pas, elle est créée , vide
	 * 
	 */
	@Override
	public void startOneBase(DataBaseInfo dataBaseInfo)
	{
		String dbName = dataBaseInfo.getDbName();
		
		logger.info("Demarrage de la base "+dbName);
		
		// Ajout de la base dans HSQL
		// Voir la doc ici : http://hsqldb.org/doc/guide/listeners-chapt.html#lsc_remote_open 
		
		String url1 = "jdbc:hsqldb:hsql://localhost:"+conf.getPort()+"/"+dbName+";file:"+conf.getContentDirectory()+"/"+dbName;	
		
		try
		{
			Connection conn = DriverManager.getConnection(url1, dataBaseInfo.user, dataBaseInfo.password);
			conn.close();
		} 
		catch (SQLException e)
		{
			throw new AmapjRuntimeException("Impossible de créer correctement la base de données url = "+url1,e);
		}   	
	}
	
	
	/**
	 * Permet l'arret d'une base de données
	 * @param dataBaseInfo
	 */
	@Override
	public void stopOneBase(DataBaseInfo dataBaseInfo)
	{	
		String dbName = dataBaseInfo.getDbName();
		
		logger.info("Arret de la base "+dbName);
		
		try
		{
			Connection conn = getConnection(dataBaseInfo);
			Statement st = conn.createStatement();
			st.execute("SHUTDOWN");
			conn.close();
		} 
		catch (SQLException e)
		{
			// Do nothing, only log
			logger.warn("Impossible d'arreter correctement la base de données "+dataBaseInfo.url+ "  "+StackUtils.asString(e));
		}   
		
	}
	
	


	/**
	 * Permet l'arret du DBMS 
	 */
	@Override
	public void stopDBMS(List<DataBaseInfo> dataBaseInfos)
	{
		for (DataBaseInfo dataBaseInfo : dataBaseInfos)
		{
			stopOneBase(dataBaseInfo);
		}
		
		server.shutdown();
		
		// On attend ensuite la fin de la base (attente max de 15 secondes) 
		for(int i=0;i<15;i++)
		{
			if (server.getState()==ServerConstants.SERVER_STATE_SHUTDOWN)
			{
				return ;
			}
			try
			{
				Thread.sleep(1000);
			} 
			catch (InterruptedException e)
			{
				// Nothing to do
			}
			logger.info("Attente de l'arret complet de la base "+i+"/15");
		}
	}

	

	
	
	@Override
	public int executeUpdateSqlCommand(DataBaseInfo dataBaseInfo,String sqlCommand) throws SQLException
	{			
		Connection conn = getConnection(dataBaseInfo);
		Statement st = conn.createStatement();
		int res = st.executeUpdate(sqlCommand);	
		conn.commit();
		conn.close();
		
		return res;
		   			
	}
	
	
	
	
	@Override
	public List<List<String>> executeQuerySqlCommand(DataBaseInfo dataBaseInfo,String sqlCommand) throws SQLException
	{
		Connection conn = getConnection(dataBaseInfo);
		Statement st = conn.createStatement();
		
		ResultSet resultset = st.executeQuery(sqlCommand);	
		
		List<List<String>> result = DBMSTools.readResultSet(resultset);	
		
		conn.commit();
		conn.close();
		
		return result;
	}
	
	
	
	

	@Override
	public void createOneBase(DataBaseInfo dataBaseInfo)
	{	
		// On démarre tout d'abord la base de données, ce qui permet de créer le fichier vide
		startOneBase(dataBaseInfo);
		
		// On vérifie ensuite que la base est bien vide
		if ( (numberOfTables(dataBaseInfo))!=0 )
		{
			throw new AmapjRuntimeException("La base n'est pas vide");
		}
		
		// On crée ensuite le schéma 
		String platform = "org.eclipse.persistence.platform.database.HSQLPlatform";
		String driver = "org.hsqldb.jdbcDriver";
		new MakeSqlSchemaForEmptyDb().createSqlSchema(dataBaseInfo.url, platform, driver, dataBaseInfo.user, dataBaseInfo.password);
		
		// On update ensuite le sequence counter
		updateSequenceCounter(dataBaseInfo);	
	}
	
	
	
	private void updateSequenceCounter(DataBaseInfo dataBaseInfo)
	{
		try
		{
			Connection conn = getConnection(dataBaseInfo);
			Statement st = conn.createStatement();
			st.execute("update SEQUENCE set SEQ_COUNT = 10000");	
			conn.commit();
			conn.close();
		} 
		catch (SQLException e)
		{
			throw new AmapjRuntimeException("Impossible d'accèder la base de données url = "+dataBaseInfo.url,e);
		}   			
	}

	/**
	 * COmpte le nombre de tables présentes dans cette base de données 
	 * @param dbName
	 * @return
	 */
	private int numberOfTables(DataBaseInfo dataBaseInfo)
	{
		try
		{
			Connection conn = getConnection(dataBaseInfo);
			Statement st = conn.createStatement();
			st.execute("SELECT * FROM   INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA='PUBLIC'");	
			int nb = 0;
			ResultSet set = st.getResultSet();
			while(set.next())
			{
				nb++;
			}
			conn.close();
			return nb;
		} 
		catch (SQLException e)
		{
			throw new AmapjRuntimeException("Impossible d'accèder la base de données url = "+dataBaseInfo.url,e);
		}   	
	}
	
	/*
	@Override
	public void restoreOneBase(AppInstanceDTO appInstanceDTO,BackupImporter backupImporter)
	{
		String dbName = appInstanceDTO.nomInstance; 
		
		// On copie les fichiers au bon endroit  
		backupImporter.extractFile(conf.getContentDirectory());
		
		// On démarre ensuite la base de données
		startOneBase(dbName);

		// On enregistre la base de données pour qu'elle soit accessible par les services
		// registerDb(appInstanceDTO, AppState.ON);
		
		// On efface les données sensibles de la base de données, en faisant un appel de service dans cette base 
		// TODO SpecificDbUtils.executeInSpecificDb(dbName, ()->new DemoService().generateDemoData(appInstanceDTO));
					
	}
	*/
	
	@Override
	public void replaceOneBase(DataBaseInfo dataBaseInfo,byte[] fileProperties,byte[] fileScript)
	{

		// On arrête la base de données 
		stopOneBase(dataBaseInfo);
				
		// On copie les fichiers au bon endroit  
		String dirDest = conf.getContentDirectory();
		try 
		{
			FileUtils.writeByteArrayToFile(new File(dirDest+"/"+dataBaseInfo.dbName+".properties"), fileProperties);
			FileUtils.writeByteArrayToFile(new File(dirDest+"/"+dataBaseInfo.dbName+".script"), fileScript);
		} 
		catch (IOException e) 
		{
			throw new AmapjRuntimeException(e);
		}
		
		// On démarre ensuite la base de données
		startOneBase(dataBaseInfo);			
	}

	@Override
	public String[] computeUrlUserPassword(String dbName, String user, String password) 
	{
		// Dans le cas d'une base de données interne, le user password est celui par défaut : SA , vide
		String[] res = new String[3];
		
		res[0] = conf.createUrl(dbName);
		res[1] = "SA";
		res[2] = "";
		
		return res;
	}	
	
	
	private Connection getConnection(DataBaseInfo dataBaseInfo) throws SQLException 
	{
		return DriverManager.getConnection(dataBaseInfo.url,dataBaseInfo.user,dataBaseInfo.password);
	}
}
