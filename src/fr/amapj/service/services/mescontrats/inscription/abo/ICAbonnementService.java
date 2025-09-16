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
 package fr.amapj.service.services.mescontrats.inscription.abo;

import java.util.Date;

import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.mescontrats.ContratStatusService;
import fr.amapj.service.services.mescontrats.inscription.abo.ICAbonnementDTO.AboState;
import fr.amapj.service.services.mescontrats.inscription.retardataire.RetardataireService;


public class ICAbonnementService 
{
	/**
	 * Permet de calculer les conditions d'inscription à un contrat de type ABONNEMENT 
	 * 
	 * Attenttion : cette méthode doit être appelée uniquement quand on est avec ModeSaisie = STANDARD
	 */
	public ICAbonnementDTO computeAbonnement(ContratDTO contratDTO,Date now,Contrat c,ModeleContrat mc)
	{		
		ICAbonnementDTO dto = new ICAbonnementDTO();
		
		// Si on est avant la date de fin des inscriptions 
		if (new ContratStatusService().isInscriptionNonTerminee(mc, now))
		{
			dto.status = AboState.STANDARD;
			return dto;
		}
		
		// Gestion des retardataires
		Date startDateRetardataire = new RetardataireService().computeICRetardataireDTO(contratDTO,now,c,mc);
		if (startDateRetardataire!=null)
		{
			dto.status = AboState.RETARDAIRE;
			dto.startDateRetardataire = startDateRetardataire;
			return dto;
		}
		
		// Autres cas
		dto.status = AboState.JOKER_OU_NONE;
		return dto;
	}
}
