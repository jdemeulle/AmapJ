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
 package fr.amapj.service.engine.objectstorage.deamon;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import fr.amapj.common.DateUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.objectstorage.ObjectStorage;
import fr.amapj.model.models.objectstorage.ObjectStorageState;
import fr.amapj.service.engine.deamons.DeamonsUtils;
import fr.amapj.service.engine.objectstorage.ObjectStorageServiceProvider;


/**
 * Ce service permet l'effacement des objects storage 
 *
 */
public class ObjectStorageDeleteService implements Job
{
	private final static Logger logger = LogManager.getLogger();
	
	/**
	 * 
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException
	{
		DeamonsUtils.executeAsDeamon(getClass(), e->deleteOldObjectStorage());
	}
	
	/**
	 * 
	 */
	@DbRead
	public void deleteOldObjectStorage()
	{
		RdbLink em = RdbLink.get();
		
		logger.info("Debut de l'effacement des objects storages");
		
		em.createQuery("select a from ObjectStorage a where a.state=:state");
		em.setParameter("state", ObjectStorageState.DELETED);
		
		List<ObjectStorage> oss = em.result().list(ObjectStorage.class);
		
		logger.info("Il y a "+oss.size()+" élements à effacer.");
		
		for (ObjectStorage os : oss)
		{
			logger.info("Effacement de "+os.id);
			ObjectStorageServiceProvider.find().forDeamonDeleteObjectDefinitively(os.id);
		}
		
		logger.info("Fin de l'effacement des object storage");
	}
}
