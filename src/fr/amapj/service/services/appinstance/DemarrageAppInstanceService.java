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
 package fr.amapj.service.services.appinstance;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.model.engine.db.DbManager;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.models.saas.StateOnStart;
import fr.amapj.service.engine.appinitializer.AppInitializer;
import fr.amapj.service.engine.deamons.DeamonsUtils;

/**
 * Permet la gestion des instances de l'application
 * 
 */
public class DemarrageAppInstanceService
{
	
	private final static Logger logger = LogManager.getLogger();
	
	/**
	 * Démarrage de toutes les bases
	 */
	public void startAllDbs()
	{
		DeamonsUtils.executeAsDeamon(getClass(), e->startAllDatabase());	
	}

	
	@DbRead
	public void startAllDatabase()
	{
		DbManager dbManager = DbManager.get();
		AppInstanceService service =  new AppInstanceService();
		List<AppInstanceDTO> dtos = service.getAllInstances(false);
		for (AppInstanceDTO dto : dtos)
		{
			performStart(dbManager,dto);
		}
	}


	private void performStart(DbManager dbManager, AppInstanceDTO dto)
	{
		try
		{
			logger.info("Traitement de la base "+dto.nomInstance+" StateOnStart ="+dto.stateOnStart);
			AppState appState = dto.stateOnStart==StateOnStart.ON_START_BE_ON ? AppState.ON : AppState.OFF;
			dbManager.addDataBase(dto,appState);			
		}
		catch (Exception e) 
		{
			logger.info("Erreur sur la base "+dto.nomInstance,e);
		}
		
	}	
	
}
