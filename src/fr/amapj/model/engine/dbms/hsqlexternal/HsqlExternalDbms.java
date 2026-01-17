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
 package fr.amapj.model.engine.dbms.hsqlexternal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.model.engine.dbms.DBMS;
import fr.amapj.model.engine.dbms.DBMSTools;
import fr.amapj.model.engine.transaction.DataBaseInfo;


/**
 * Cette classe permet de gérer un serveur Hsql externe à l'application
 *
 */
public class HsqlExternalDbms implements DBMS
{
	
	private final static Logger logger = LogManager.getLogger();
	
	private HsqlExternalDbmsConf conf;
	
	
	public HsqlExternalDbms(HsqlExternalDbmsConf conf)
	{
		this.conf = conf;
	}
	
	/**
	 * Réalise l'initialisation du DBMS , si nécessaire
	 */
	@Override
	public void startDBMS()
	{
		// Nothing to do 	
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
		// DO NOTHING
	}
	
	
	/**
	 * Permet l'arret d'une base de données
	 * @param dataBaseInfo
	 */
	@Override
	public void stopOneBase(DataBaseInfo dataBaseInfo)
	{	
		// DO NOTHING
	}
	
	
	/**
	 * Permet l'arret du DBMS , si nécessaire 
	 */
	@Override
	public void stopDBMS(List<DataBaseInfo> dataBaseInfos)
	{
		// DO NOTHING
		
	}

	@Override
	public void createOneBase(DataBaseInfo dataBaseInfo)
	{
		// TODO 
		
	}
		
	
	@Override
	public void replaceOneBase(DataBaseInfo dataBaseInfo, byte[] fileProperties,byte[] fileScript)
	{
		// TODO
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
	
	private Connection getConnection(DataBaseInfo dataBaseInfo) throws SQLException 
	{
		return DriverManager.getConnection(dataBaseInfo.url,dataBaseInfo.user,dataBaseInfo.password);
	}

	@Override
	public String[] computeUrlUserPassword(String dbName, String user, String password) 
	{
		// 
		String[] res = new String[3];
		res[0] = conf.createUrl(dbName);
		res[1] = user;
		res[2] = password;
		
		return res;
	}	
	
}
