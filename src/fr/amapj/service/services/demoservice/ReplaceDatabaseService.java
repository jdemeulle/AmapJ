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
 package fr.amapj.service.services.demoservice;

import java.util.List;

import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.service.services.authentification.PasswordManager;

public class ReplaceDatabaseService 
{
	@DbWrite
	public Void resetAllPassword()
	{
		RdbLink em = RdbLink.get();
		em.createQuery("select u from Utilisateur u order by u.id");
		List<Utilisateur> us = em.result().list(Utilisateur.class);
		
		for (Utilisateur u : us) 
		{
			new PasswordManager().setUserPassword(u.id, "a");
		}
		return null;
	}
}
