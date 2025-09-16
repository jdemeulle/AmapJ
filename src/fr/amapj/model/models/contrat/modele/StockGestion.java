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
 * Mode de gestion des stocks
 *
 */
public enum StockGestion
{
	// 
	NON,
	
	//  
	OUI ;

	
	
	
	static public class MetaData extends MetaDataEnum
	{
		public void fill()
		{		
			add("Ce champ vous permet d'activer ou de désactiver la gestion des limites en quantité pour ce contrat");

			add(NON, "Non" , "C'est le cas standard : il n'y a pas de limites sur le nombre de contrats pouvant être souscrits.");
			
			add(OUI, "Oui","Le producteur peut livrer une quantité limitée de paniers pour ce contrat. Dans ce mode, vous allez pouvoir indiquer les quantités maximum que le producteur peut fournir. La saisie des contrats sera impossible quand le nombre maximun sera atteint .");
			
			addBrLink("La gestion des limites en quantités", "docs_utilisateur_limites_quantites.html");
		}
	}		
}
