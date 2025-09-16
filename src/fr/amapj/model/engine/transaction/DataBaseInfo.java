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
 package fr.amapj.model.engine.transaction;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.persistence.config.PersistenceUnitProperties;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.service.services.appinstance.AppState;


/**
 * Cette classe memorise les infos d'une base de données
 * 
 * Il faut noter que l'application gère x bases de données, une base de données pour chaque tenant
 *
 */
public class DataBaseInfo
{
	
	public String dbName;
	
	public String url;
	
	public String user;
	
	public String password;
	
	private AppState state;
	
	// Persistence Unit Number, correspond à puXXX du fichier persistence.xml
	private int puNumber;
	
	private EntityManagerFactory entityManagerFactory;
	

	public DataBaseInfo(String dbName,String url,String user,String password, int puNumber)
	{
		this.dbName = dbName;
		this.url = url;
		this.user = user;
		this.password = password;
		this.state = AppState.OFF;
		this.puNumber = puNumber;
		this.entityManagerFactory = null;
	}

	public String getDbName()
	{
		return dbName;
	}


	public AppState getState()
	{
		return state;
	}

	public void setState(AppState state)
	{
		this.state = state;
	}
	
	
	
	
	/**
	 * Permet de créer un entity manager avec la base donnée en paramètres
	 * 
	 * Cette méthode doit être utilisée uniquement par NewTransaction ou DbUtil
	 * 
	 */
	public EntityManager createEntityManager()
	{
		if (state!=AppState.ON)
		{
			throw new AmapjRuntimeException("L'application est en maintenance et n'est plus accessible.");
		}
		
		return entityManagerFactory.createEntityManager();
	}
	
	/**
	 * Permet la creation d'un EntityManagerFactory
	 * 
	 */
	private EntityManagerFactory createEntityManagerFactory()
	{
		Map<String, Object> mp = new HashMap<String, Object>(); 
		
		mp.put("eclipselink.jdbc.platform","org.eclipse.persistence.platform.database.HSQLPlatform");
		mp.put("javax.persistence.jdbc.driver","org.hsqldb.jdbcDriver" );
		mp.put("javax.persistence.jdbc.url",url);
		mp.put(PersistenceUnitProperties.JDBC_USER, user);
		mp.put(PersistenceUnitProperties.JDBC_PASSWORD, password);
		
		mp.put("eclipselink.logging.level" ,"INFO" );
		mp.put("eclipselink.logging.level.sql" ,"FINE" );
		
		
		mp.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.NONE);
		mp.put(PersistenceUnitProperties.LOGGING_LOGGER, "fr.amapj.model.engine.db.EclipseLinkLogger");
		
		DecimalFormat df = new DecimalFormat("000");
		String puName = "pu"+df.format(puNumber);
	
		return Persistence.createEntityManagerFactory(puName,mp);	
	}
	
	
	public void closeEntityManagerFactory()
	{
		entityManagerFactory.close();
		entityManagerFactory = null;
	}
	
	public void renewEntityManagerFactory()
	{
		if (entityManagerFactory!=null)
		{
			throw new AmapjRuntimeException("Il faut d'abord fermer EntityManagerFactory");
		}
		entityManagerFactory = createEntityManagerFactory();
	}
}
