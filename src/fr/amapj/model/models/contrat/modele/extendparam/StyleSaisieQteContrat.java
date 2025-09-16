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
 package fr.amapj.model.models.contrat.modele.extendparam;

import fr.amapj.model.engine.metadata.MetaDataEnum;

public enum StyleSaisieQteContrat 
{
	// 
	CHOIX_AUTOMATIQUE,
	
	//  
	GRILLE_DATE_PRODUIT,

	
	LISTE_DATE;
	
	
	
	static public class MetaData extends MetaDataEnum
	{
		
		public void fill()
		{		
			add("Ce champ vous permet de choisir comment l'adhérent pourra saisir les quantités de son contrat.");

			add(CHOIX_AUTOMATIQUE, "Choix automatique" , "Dans ce mode, le logiciel calcule la disposition la plus adaptée en fonction de la taille de l'écran et des données du contrat. C'est le mode par défaut.<br/>"
					+ ".<br/>"
					+ "");
			
			add(GRILLE_DATE_PRODUIT, "Grille dates / produits","Dans ce mode, la saisie des quantités se fait dans une grille avec les dates en ligne et les produits en colonne.<br/>"
					+ "Ce mode est pratique si le nombre de produits est limité (8 maxi).");
			
			add(LISTE_DATE, "Liste de dates","Dans ce mode, le logiciel présente une liste de dates. Pour chaque date, l'adhérent peut cliquer sur un bouton pour faire apparaitre une autre fenêtre permettant la saisie des quantités de cette date uniquement.<br/>"
					+ "Ce mode est pratique si le nombre de produits est important.");
		}
	}	
}
