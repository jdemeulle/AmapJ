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
 package fr.amapj.service.services.mescontrats.small;

import java.util.Date;

import fr.amapj.model.models.contrat.modele.NatureContrat;
import fr.amapj.service.services.mescontrats.small.inscription.SmallInscriptionDTO;

/**
 * Represente un contrat ou un modele de contrat 
 *
 */
public class SmallContratDTO
{
	public Long contratId;
	
	public Long modeleContratId;
	
	public String nom;
	
	public String description;

	public String nomProducteur;
	
	public Date dateFinInscription;
	
	public Date dateDebut;
	
	public Date dateFin;

	public int nbLivraison;
	
	
	// Nature du contrat 
	public NatureContrat nature;
		
	
	// Regle d'inscription dans le cas d'un adherent standard 
	public SmallInscriptionDTO inscriptionDTO;
	
}
