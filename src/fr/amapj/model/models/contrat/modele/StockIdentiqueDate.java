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
 package fr.amapj.model.models.contrat.modele;

import fr.amapj.model.engine.metadata.MetaDataEnum;

/**
 * Permet d'indiquer si le stock est identique pour chaque date  
 */
public enum StockIdentiqueDate
{
	// 
	OUI,
	
	//  
	NON ;

	
	static public class MetaData extends MetaDataEnum
	{
		public void fill()
		{		
			add(OUI, "Oui" , "Le producteur peut fournir les mêmes quantités pour chaque date de livraison. C'est le cas le plus simple, à utiliser en priorité.");
			
			add(NON, "Non","Les quantités que le producteur peut fournir varient en fonction des dates de livraison. C'est le cas le plus complexe, il faudra saisir pour chaque date les quantités que le producteur peut fournir.");
			
			addBrLink("La gestion des limites en quantités", "docs_utilisateur_limites_quantites.html");
		}
	}		
}
