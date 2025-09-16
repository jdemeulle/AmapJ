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
 package fr.amapj.service.services.moncompte;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.service.services.utilisateur.UtilisateurDTO;

public class MonCompteService
{
	private final static Logger logger = LogManager.getLogger();


	/**
	 * Permet de changer le password
	 * 
	 * Retourne true si le mot de passe a pu être changé, 
	 * false sinon 
	 */
	@DbWrite
	public boolean setNewEmail(final Long userId, final String newEmail)
	{
		 RdbLink em = RdbLink.get();
	
		Utilisateur r = em.find(Utilisateur.class, userId);
		if (r == null)
		{
			logger.warn("Impossible de retrouver l'utilisateur avec l'id " + userId);
			return false;
		}

		r.email = newEmail;
		return true;

	}

	// PARTIE MISE A JOUR DES COORDONNEES
	
	@DbWrite
	public void updateCoordoonees(final UtilisateurDTO dto)
	{
		RdbLink em = RdbLink.get();
		
		Utilisateur u = em.find(Utilisateur.class, dto.id);

		u.numTel1 = dto.numTel1;
		u.numTel2 = dto.numTel2;
		u.libAdr1 = dto.libAdr1;
		u.codePostal = dto.codePostal;
		u.ville = dto.ville;
	}

}
