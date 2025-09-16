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

import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;

import fr.amapj.model.engine.db.DbManager;

import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.RollbackException;

/**
 * Contient les informations sur la transaction en cours
 *
 */
public class TransactionInfo
{
	private final static Logger logger = LogManager.getLogger("SQL");
	
	private final static AtomicLong atomicLong = new AtomicLong();
	
	// Correspond au nombre d'appel de fonction avec des annotations @DbRead, ... (stack d'appel) 
	public int nbAppel=0;
	
	public EntityManager em=null;
	
	public EntityTransaction transac=null;
	
	public Long idTransac;
	
	
	
	
	public enum Status
	{
		VIDE , LECTURE , ECRITURE ;
	}
	
	public Status getType()
	{
		if (transac!=null)
		{
			return Status.ECRITURE;
		}
		
		if (em!=null)
		{
			return Status.LECTURE;
		}
			
		return Status.VIDE;	
	}
	
	
	/**
	 * Permet de démarrer une session en lecture
	 */
	public void startSessionLecture()
	{
		em = DbManager.get().getCurrentDb().createEntityManager();
		transac = null;
		idTransac = atomicLong.incrementAndGet();
		
		logger.info("id="+idTransac+" Début transaction lecture");
	}	

	/**
	 * Permet de démarrer une session en écriture
	 */
	public void startSessionEcriture()
	{
		em = DbManager.get().getCurrentDb().createEntityManager();
		transac = em.getTransaction();
		transac.begin();
		idTransac = atomicLong.incrementAndGet();
		
		logger.info("id="+idTransac+" Début transaction écriture");
	}
	
	
	/**
	 * Permet de transformer une session de LECTURE à ECRITURE
	 */
	public void upgradeLectureToEcriture()
	{
		transac = em.getTransaction();
		transac.begin();
		
		logger.info("id="+idTransac+" Upgrade transaction de lecture à écriture");
	}


	public void closeSession(boolean rollback)
	{
		// on mémorise le status de la session 
		Status type = getType();
		EntityTransaction transac1 = transac;
		EntityManager em1 = em;
		
		// On met à zéro tous les elements pour être sur que celle ci ait bien lieu
		nbAppel = 0;
		em = null;
		transac = null;

		// On commit ensuite
		switch (type)
		{
		case VIDE:
			logger.warn("id="+idTransac+" Erreur dans les transactions !! Impossible de fermer une session vide");
			break;
			
		case LECTURE:
			if (rollback)
			{
				logger.info("id="+idTransac+" Fin transaction en lecture sur exception");
				em1.close();
			}
			else
			{
				logger.info("id="+idTransac+" Fin transaction en lecture");
				em1.close();
			}
			break;
			
		case ECRITURE:
			if (rollback)
			{
				logger.info("id="+idTransac+" Début rollback transaction en ecriture");
				transac1.rollback();
				em1.close();
				logger.info("id="+idTransac+" Fin rollback transaction en ecriture");
			}
			else
			{
				logger.info("id="+idTransac+" Début commit transaction en ecriture");
				try
				{
					transac1.commit();
					em1.close();
					logger.info("id="+idTransac+" Fin commit transaction en ecriture");
				}
				catch (RollbackException e) 
				{
					logger.info("id="+idTransac+" Erreur lors du commit ");
					throw e;
				}
			}
			break;
		}

	}
	
	
	
	

}
