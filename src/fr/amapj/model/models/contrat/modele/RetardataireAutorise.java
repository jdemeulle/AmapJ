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

public enum RetardataireAutorise
{
	// 
	OUI ,
	
	// 
	NON ;
	

	static public class MetaData extends MetaDataEnum
	{
		
		public void fill()
		{		
			add("Ce champ vous permet d'indiquer si l'inscription en tant que nouvel arrivant / retardataire est autorisé.");

			add(OUI, "Oui" , "Dans ce mode, il est possible de s'inscrire en tant que nouvel arrivant / retardataire sur un contrat.<br/>"
					+ "Les inscriptions en tant que nouvel arrivant / retardataire se font dans l'écran Découverte / Visite, et sont possibles même après la date de fin des inscriptions.<br/>"
					+ "Quand un amapien s'inscrit dans ce mode, il peut saisir des quantités uniquement sur les dates de livraison dans le futur (date de livraison après date courante + delai de prévenance de la fiche producteur).");
			
			add(NON, "Non","L'inscription en en tant que nouvel arrivant / retardataire n'est pas possible.<br/>"
					+ "Après la date de fin des inscriptions, les amapiens ne peuvent plus s'inscrire.<br/>"
					+ "Seul le référent peut alors ajouter des contrats.");
			
			addBrLink("Gestion des nouveaux arrivants / retardataires", "docs_utilisateur_nouveaux_arrivants.html");
		}
	}	
	

}
