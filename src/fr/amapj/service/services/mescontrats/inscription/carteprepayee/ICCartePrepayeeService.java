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
 package fr.amapj.service.services.mescontrats.inscription.carteprepayee;

import java.util.Date;

import fr.amapj.common.DateUtils;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.mescontrats.ContratLigDTO;

/**
 * Partie spécifique aux cartes prépayées 
 *
 */
public class ICCartePrepayeeService
{

	/**
	 * Permet de calculer les informations relatives à une carte prepayée
	 * 
	 * Retourne la date de la première ligne modifiable
	 * 
	 *  Retourne nulle si il n'est plus possible de modifier ce contrat
	 */
	public Date computeCartePrepayee(ContratDTO contratDTO,Date now,Contrat c,ModeleContrat mc)
	{
		for (int i = 0; i < contratDTO.contratLigs.size(); i++) 
		{
			ContratLigDTO lig = contratDTO.contratLigs.get(i);
			if (ligModifiable(lig,now,mc.cartePrepayeeDelai))
			{
				return lig.date;
			}
		}
		return null;
	}	
	
	
	private boolean ligModifiable(ContratLigDTO lig,Date now, int cartePrepayeeDelai)
	{
		Date d = DateUtils.addDays(lig.date, -cartePrepayeeDelai);
		return  d.after(now);
	}	
}
