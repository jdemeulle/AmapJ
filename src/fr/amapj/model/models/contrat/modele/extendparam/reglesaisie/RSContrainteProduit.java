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
 package fr.amapj.model.models.contrat.modele.extendparam.reglesaisie;

import fr.amapj.model.engine.metadata.MetaDataEnum;

/**
 * Regle de saisie : contrainte sur la date 
 */
public enum RSContrainteProduit
{
	// 
	POUR_UN_PRODUIT,
	
	POUR_PLUSIEURS_PRODUITS,
	
	POUR_TOUS_PRODUITS
	
	;
	
	
	static public class MetaData extends MetaDataEnum
	{
		public void fill()
		{		
			add(POUR_UN_PRODUIT, "Pour un produit","La régle s'applique sur un produit précis que vous allez indiquer.");
			
			add(POUR_PLUSIEURS_PRODUITS, "Pour plusieurs produits","La régle s'applique sur la somme des quantités de plusieurs produits, que vous allez indiquer.");
			
			add(POUR_TOUS_PRODUITS, "Pour tous les produits","La régle s'applique sur la somme des quantités de tous les produits.");
			
		}
	}		
}
