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
 * Permet d'indiquer si ce contrat partage son stock avec d'autres contrats 
 */
public enum StockMultiContrat
{
	// 
	NON,
	
	//  
	OUI ;

	
	static public class MetaData extends MetaDataEnum
	{
		public void fill()
		{		
			add(NON, "Non" , "Cas le plus simple : vous saississez des quantités limites qui s'appliquent uniquement à ce contrat");
			
			add(OUI, "Oui","Cas le plus complexe : ce contrat propose des produits qui sont proposés en même temps dans d'autres contrats, et la limite est globale à l'ensemble des contrats. Ce mode est complexe, il est donc préférable de l'utiliser uniquement si vous en avez vraiment besoin.");
			
			addBrLink("La gestion des limites en quantités", "docs_utilisateur_limites_quantites.html");
		}
	}		
}
