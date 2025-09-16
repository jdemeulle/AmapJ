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
 package fr.amapj.model.models.fichierbase;

import fr.amapj.model.engine.metadata.MetaDataEnum;

/**
 * Mode de gestion des stocks pour ce prodcuteur 
 */
public enum ProducteurStockGestion
{
	// 
	NON,
	
	//  
	OUI ;
	
	
	static public class MetaData extends MetaDataEnum
	{
		public void fill()
		{		
			add("Ce champ vous permet d'activer ou de désactiver la gestion des limites en quantité pour ce producteur");

			add(NON, "Non" , "C'est le cas standard : pas de gestion des limites en quantités.");
			
			add(OUI, "Oui","A la création d'un contrat pour ce producteur, vous allez pouvoir indiquer pour chaque produit les quantités limites qu'il peut fournir. Les adhérents ne pourront pas souscrire  plus de contrats que la quantité limite.");
			
			addBrLink("La gestion des limites en quantités", "docs_utilisateur_limites_quantites.html");
		}
	}		
}
