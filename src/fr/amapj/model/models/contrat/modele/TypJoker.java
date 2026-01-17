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


public enum TypJoker
{
	// 
	SANS_JOKER,
	
	//  
	JOKER_ABSENCE,

	
	JOKER_REPORT;
	
	
	
	static public class MetaData extends MetaDataEnum
	{
		public void fill()
		{		
			add("Ce champ vous permet d'activer ou de désactiver la gestion des jokers pour ce contrat");

			add(SANS_JOKER, "Pas de jokers" , "L'amapien doit récupérer son panier chaque semaine.");
			
			add(JOKER_ABSENCE, "Joker en mode absence","Dans ce mode, les amapiens pourront indiquer X dates où ils ne seront pas présents pour récupérer leur panier.");
			
			add(JOKER_REPORT, "Joker en mode report","Dans ce mode, les amapiens pourront reporter X dates sur d'autres dates.<br/>"
					+ "Par exemple, un amapien commande un panier de légumes chaque semaine.<br/>"
					+ "Il peut indiquer qu'il sera absent le 13 juillet et ce panier sera reporté le 27 juillet.<br/>"
					+ "L'amapien aura donc deux paniers le 27 juillet.");
			
			addBrLink("La gestion des jokers", "docs_utilisateur_joker.html");
		}
	}	
	
	
	
	public static String helpGeneral = "Les 5 champs ci dessus permettent d'activer la gestion des jokers (absence ou report de panier) pour ce contrat.<br/>"
			+ "Exemple 1 : si vous indiquez "
			+ "<ul>"
			+ "    <li>Type de joker = Joker en mode Absence</li>"
			+ "    <li>Nombre minimum d'absences pour ce contrat = 0</li>"
			+ "    <li>Nombre maximum d'absences pour ce contrat = 3</li>"
			+ "</ul>"
			+ "alors les amapiens pourront poser soit 0, soit 1, soit 2, soit 3 dates où ils ne seront pas présents pour récupérer leur panier.<br/><br/>"
			+ "Exemple 2 : si vous indiquez "
			+ "<ul>"
			+ "    <li>Type de joker = Joker en mode Report</li>"
			+ "    <li>Nombre minimum de report pour ce contrat = 0</li>"
			+ "    <li>Nombre maximum de report pour ce contrat = 2</li>"
			+ "</ul>"
			+ "alors les amapiens pourront reporter soit 0, soit 1, soit 2 dates. Ils auront le double de panier sur les dates reportées<br/><br/>"
			+ "A noter : il est préféreable de faire du report de panier que de l'absence de panier.<br/>"
			+ "En effet, en mode absence, ceci fait varier le montant total du contrat, ce qui oblige à réaliser à une régularisation en fin de contrat.";
	
	
	public static String helpDelai =	"Exemple :<br>" +
			"Si les livraisons ont lieu le jeudi et si vous mettez 3 dans le champ délai de prévenance<br>"+
			"alors les amapiens pourront modifier leur joker jusqu'au dimanche soir minuit précédent la distribution.";
	
	
}
