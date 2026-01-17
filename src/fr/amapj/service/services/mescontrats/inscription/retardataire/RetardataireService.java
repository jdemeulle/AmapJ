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
 package fr.amapj.service.services.mescontrats.inscription.retardataire;

import java.util.Date;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.DateUtils;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.RetardataireAutorise;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.mescontrats.ContratLigDTO;

/**
 * Partie spécifique à l'inscription retardataire
 *
 */
public class RetardataireService
{
	
	/**
	 * Permet de calculer les informations relatives à l'inscription retardataire (la date de la premiere livraison modifiable)
	 * Retourne null si non concerné par l'inscription retardataire 
	 */
	public Date computeICRetardataireDTO(ContratDTO contratDTO,Date now, Contrat c, ModeleContrat mc)
	{
		// Si il y a un contrat existant 
		if (c!=null)
		{
			switch (c.typInscriptionContrat)
			{
			case STANDARD:
				// si il est mode normal : pas concerné
				return null;

			case RETARDATAIRE:
				// On peut modifier le jour même de la création du contrat en mode retardataire
				Date limit = DateUtils.addDays(DateUtils.suppressTime(c.dateCreation),1);
				if(now.before(limit))
				{
					return computeICRetardataireDTOInternal(contratDTO,now,c,mc);
				}
				else
				{
					return null;
				}

			default: throw new AmapjRuntimeException("c="+c.typInscriptionContrat);
				
			}	
		}
		
		
		// Si il n'y a pas de contrat existant, on vérifie si il est possible de s'inscire en retardataire 
		
		// Est ce autorisé ? 
		if (mc.retardataireAutorise==RetardataireAutorise.NON)
		{
			return null;
		}
		
		// 
		return computeICRetardataireDTOInternal(contratDTO,now,c,mc);		
	}

	
	
	private Date computeICRetardataireDTOInternal(ContratDTO contratDTO,Date now, Contrat c, ModeleContrat mc)
	{
		int retardataireDelai = mc.producteur.delaiModifContrat;
		for (int i = 0; i < contratDTO.contratLigs.size(); i++) 
		{
			ContratLigDTO lig = contratDTO.contratLigs.get(i);
			if (retardataireLigModifiable(lig,now,retardataireDelai))
			{
				return lig.date;
			}
		}
		return null;
	}	
	
	
	private boolean retardataireLigModifiable(ContratLigDTO lig,Date now, int retardataireDelai)
	{
		Date d = DateUtils.addDays(lig.date, -retardataireDelai);
		return  d.after(now);
	}
}
