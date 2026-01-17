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
 package fr.amapj.service.services.advanced.maintenance;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.util.IOUtils;

import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbWrite;

/**
 * Service pour la maintenance de base 
 *
 */
public class MaintenanceService
{
	
	/**
	 * Permet de vider le cache de la base
	 * Ceci est fait dans une transaction en ecriture  
	 * obligatoire après requete SQL manuelle
	 */
	@DbWrite
	public void resetDatabaseCache()
	{
		RdbLink em = RdbLink.get();
		em.getEm().getEntityManagerFactory().getCache().evictAll();
	}

	
	/**
	 * Lit le numéro de la version, avec la date de compilation
	 * Exemple : V041,24/03/2024 18:10:15
	 */
	public String getVersion()
	{
		try
		{
			InputStream in = this.getClass().getResourceAsStream("/amapj_version.txt");
			byte[] bs = IOUtils.toByteArray(in);
			return new String(bs);
		} 
		catch (IOException e)
		{
			return "error";
		}
	}
	
	/**
	 * Lit le numéro de la version
	 * Exemple : V041
	 */
	public String getShortVersion()
	{
		return getVersion().substring(0,4);
	}
	
	
	
}
