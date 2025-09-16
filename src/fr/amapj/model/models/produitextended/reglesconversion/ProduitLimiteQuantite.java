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
 package fr.amapj.model.models.produitextended.reglesconversion;

import fr.amapj.model.engine.metadata.MetaDataEnum;

/**
 * Mode de gestion des stocks pour ce produit 
 */
public enum ProduitLimiteQuantite
{
	// 
	SUIVI_STANDARD,
	
	//  
	SUIVI_AVEC_REGLE_CALCUL,
	
	//
	NON_SUIVI
	;
	
	
	static public class MetaData extends MetaDataEnum
	{
		public void fill()
		{		
			add("Ce champ vous permet d'activer ou de désactiver la gestion des limites en quantité pour ce produit");

			add(SUIVI_STANDARD, "Suivi standard","La gestion des limites en quantité est activé pour ce produit, et vous allez saisir simplement la quantité disponible.");
			
			add(SUIVI_AVEC_REGLE_CALCUL, "Suivi avec régle de calcul" , "La gestion des limites en quantité est activée pour ce produit, mais ce produit est dépendant d'un autre produit, vous n'allez pas saisir sa quantité.</br>"
					+ "Exemple : nous considérons un contrat avec deux produits, un grand et un petit panier. Le grand panier est équivalent à deux petits paniers, le producteur peut fournir 80 petits paniers.</br>"
					+ "Vous allez alors indiquer que le grand panier est équivalent à 2 petits paniers, et vous aller saisir uniquement la quantité de petits paniers");
			
			add(NON_SUIVI, "Pas de suivi","La gestion des limites en quantité N est PAS activé pour ce produit.");

			addBrLink("La gestion des limites en quantités", "docs_utilisateur_limites_quantites.html");
		}
	}		
}
