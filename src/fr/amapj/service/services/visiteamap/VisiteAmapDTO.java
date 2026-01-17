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
 package fr.amapj.service.services.visiteamap;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.amapj.service.services.mescontrats.small.SmallContratDTO;
import fr.amapj.service.services.meslivraisons.QteProdDTO;

/**
 * 
 */
public class VisiteAmapDTO
{
	
	public List<Jour> jours = new ArrayList<>();
	
	static public class Jour
	{
		public Date dateLiv;
		public List<Producteur> producteurs = new ArrayList<>();
	}
	
	static public class Producteur
	{
		public String producteurNom;
		public List<Contrat> contrats = new ArrayList<>();
	}
	
	static public class Contrat
	{
		public String contratNom;
		public Long modeleContratId;
		public Long modeleContratDateId;
		  
		public List<LigneContrat> ligneContrats = new ArrayList<>();
		
		// Informations relatives à l'utilisateur
		public SmallContratDTO contratDTO;
		public List<QteProdDTO> qteProdDTOs;
		
	}
	
	static public class LigneContrat
	{
		// Date de livraison
		public Date dateLiv;
		
		// Les informations du producteur
		public String producteurNom;
		
		public Long producteurId;
		
		// Les informations du contrat
		public String modeleContratNom; 
		
		public Long modeleContratId;
		
		public Long modeleContratDateId;
		
		// Les informations du produit
		public int produitPrix;
		
		public String produitNom;
		
		public String produitConditionnement;
		
		public Long produitId;
		 
		public int produitIndx;
		
	}
	
}
