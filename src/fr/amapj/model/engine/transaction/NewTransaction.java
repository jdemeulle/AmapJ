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

import java.util.function.Consumer;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.model.engine.db.DbManager;
import fr.amapj.model.engine.rdblink.RdbLink;

/**
 * Outil permettant de faire une nouvelle transaction basique au sein d'une transaction existante
 * 
 * Attention : dans la méthode appelée, il ne faut faire aucun appel de service, car ils seraient
 * appeler dans l'ancienne transaction
 * 
 */
public class NewTransaction
{
	
	private final static Logger logger = LogManager.getLogger();
	
	/**
	 * Outil permettant de faire une nouvelle transaction basique au sein d'une transaction existante
	 * 
	 * Attention : dans la méthode appelée, il ne faut faire aucun appel de service, car ils seraient
	 * appelés dans l'ancienne transaction
	 */
	static public <T> T writeWithResult(Function<RdbLink, T> fn)
	{
		EntityManager em = DbManager.get().getCurrentDb().createEntityManager();
		return write(fn, em);
	}
	
	static public void write(Consumer<RdbLink> consumer)
	{
		Function<RdbLink, Void> fn = e->{consumer.accept(e); return null;}; 
		writeWithResult(fn);
	}
	
	
	
	static private <T> T write(Function<RdbLink, T> fn,EntityManager em)
	{
		EntityTransaction transac = em.getTransaction();
		transac.begin();
		
		T result = null;
		
		
		try
		{
			logger.info("Début d'une NOUVELLE transaction en ecriture");
			result = fn.apply(new RdbLink(em));
		}
		catch(Throwable t)
		{
			logger.info("Rollback d'une NOUVELLE transaction en ecriture");
			transac.rollback();
			em.close();
			throw t;
		}
		
		logger.info("Début commit d'une NOUVELLE transaction en ecriture");
		transac.commit();
		em.close();
		logger.info("Fin commit d'une NOUVELLE transaction en ecriture");
		
		return result;
		
	}
	
	
	/**
	 * Outil permettant de faire une nouvelle transaction basique au sein d'une transaction existante,
	 * en passant dans la base MASTER
	 * 
	 * Attention : dans la méthode appelée, il ne faire aucun appel de service, car ils seraient
	 * appelés dans l'ancienne transaction
	 */
	static public <T> T writeInMasterWithResult(Function<RdbLink, T> fn)
	{
		EntityManager em = DbManager.get().getMasterDb().createEntityManager();
		return write(fn, em);
	}
	
	static public void writeInMaster(Consumer<RdbLink> consumer)
	{
		Function<RdbLink, Void> fn = e->{consumer.accept(e); return null;}; 
		writeInMasterWithResult(fn);
	}
	

}
