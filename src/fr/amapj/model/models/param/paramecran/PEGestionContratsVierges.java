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
 package fr.amapj.model.models.param.paramecran;

import fr.amapj.model.engine.metadata.MetaDataEnum;
import fr.amapj.model.models.param.ChoixOuiNon;
import fr.amapj.model.models.param.paramecran.common.AbstractParamEcran;

/**
 * Parametrage de l'écran gestion des contrats vierges
 */
public class PEGestionContratsVierges  extends AbstractParamEcran
{
	// Indique si la saisie de la periode de cotisation est obligatoire lors de la saisie d'un contrat 
	public ChoixOuiNon periodeCotisationObligatoire = ChoixOuiNon.OUI;
	
	// Gestion des retardataires
	public GestionRetardataire gestionRetardataire = GestionRetardataire.TOUJOURS_NON_AUTORISE;
	
	static public enum GestionRetardataire
	{
		TOUJOURS_NON_AUTORISE ,
		
		LIBRE_CHOIX ,
		
		TOUJOURS_AUTORISE;
		
		static public class MetaData extends MetaDataEnum
		{
			
			public static String link = createLink("La gestion des nouveaux arrivants / retardataires","docs_utilisateur_nouveaux_arrivants.html");
			
			public void fill()
			{	
				add(link);
				
				add("Ce champ vous permet d'indiquer comment vous allez gérer l'inscription de ces nouveaux arrivants dans votre AMAP");

				add(TOUJOURS_NON_AUTORISE, "Toujours non" , "Dans ce mode, quand un référent crée un contrat, le champ \"L'inscription en tant que retardataire est autorisé\" est toujours forcé à NON.<br/>"
						+ "En cas de nouveaux arrivants, c'est le référent qui devra créer manuellement les nouveaux contrats, en allant dans Gestion des contrats signés puis Ajouter.");

				add(LIBRE_CHOIX, "Le référent peut choisir oui ou non" , "Dans ce mode, quand un référent crée un contrat, il peut saisir soit OUI soit NON dans dans le champ \"L'inscription en tant que retardataire est autorisé\"");

				add(TOUJOURS_AUTORISE, "Toujours oui" , "Dans ce mode, quand un référent crée un contrat, le champ \"L'inscription en tant que retardataire est autorisé\" est toujours forcé à OUI.");				
			}
			
			
			public static String infoParametrage = "Si vous souhaitez modifier ce comportement, un ADMIN doit "
					+ "<ul>"
					+ "<li>aller dans ADMIN / Paramétres généraux</li>"
					+ "<li>cliquer sur le bouton Ecran Gestion des contrats vierges</li>"
					+ "<li>Aller à \"Paramétrage de l'étape 4 : la date de fin des inscriptions\"</li>"
					+ "<li>modifier la valeur du champ \"L'inscription en tant que retardataire est autorisé\"</li>"
					+ "</ul>";
			
			public static String helpToujoursNonAutorise = link+"Au sein de votre Amap, la gestion des nouveaux arrivants / retardataire n'est pas activée.<br/>"+infoParametrage; 
			
			public static String helpToujoursAutorise = link+"Au sein de votre Amap, par défaut il est toujours possible de s'inscrire en tant que nouvel arrivant / retardataire.<br/>"+infoParametrage;
			
		}			
	}
	
	
	// Gestion des retardataires
	public GestionJoker gestionJoker = GestionJoker.TOUJOURS_NON;
	
	static public enum GestionJoker
	{
		TOUJOURS_NON ,
		
		MODE_REPORT,
		
		MODE_ABSENCE,
		
		TOUT_POSSIBLE;
		
		static public class MetaData extends MetaDataEnum
		{
			
			public static String link = createLink("La gestion des jokers","docs_utilisateur_joker.html");
			
			public void fill()
			{	
				add(link);
				
				add("Ce champ vous permet d'indiquer comment vous allez gérer les jokers dans votre AMAP");

				add(TOUJOURS_NON, "Toujours non" , "Dans ce mode, quand un référent crée un contrat, le champ \"Activer la gestion des jokers\" est toujours forcé à \"Pas de jokers\".");

				add(MODE_REPORT, "Joker en mode report possible" , "Dans ce mode, quand un référent crée un contrat, il peut saisir soit \"Pas de joker\" soit \"Joker en mode report\" dans dans le champ \"Activer la gestion des jokers\"");
				
				add(MODE_ABSENCE, "Joker en mode absence possible" , "Dans ce mode, quand un référent crée un contrat, il peut saisir soit \"Pas de joker\" soit \"Joker en mode absence\" dans dans le champ \"Activer la gestion des jokers\"");
				
				add(TOUT_POSSIBLE, "Joker en mode report ou absence possible" , "Dans ce mode, quand un référent crée un contrat, il peut saisir soit \"Pas de joker\" soit \"Joker en mode absence\" soit \"Joker en mode report\"dans dans le champ \"Activer la gestion des jokers\"");	
			}
			
			public static String infoParametrage = "Si vous souhaitez modifier ce comportement, un ADMIN doit "
					+ "<ul>"
					+ "<li>aller dans ADMIN / Paramétres généraux</li>"
					+ "<li>cliquer sur le bouton Ecran Gestion des contrats vierges</li>"
					+ "<li>Aller à \"Paramétrage de l'étape 4 : la date de fin des inscriptions\"</li>"
					+ "<li>modifier la valeur du champ \"Activer les jokers\"</li>"
					+ "</ul>";
			
			public static String helpToujoursNon = link+"Au sein de votre Amap, la gestion des jokers n'est pas activée.<br/>"+infoParametrage; 
			
		}			
	}
	
	
	// Getters et setters
	public ChoixOuiNon getPeriodeCotisationObligatoire() 
	{
		return periodeCotisationObligatoire;
	}

	public void setPeriodeCotisationObligatoire(ChoixOuiNon periodeCotisationObligatoire) 
	{
		this.periodeCotisationObligatoire = periodeCotisationObligatoire;
	}

	public GestionRetardataire getGestionRetardataire() 
	{
		return gestionRetardataire;
	}

	public void setGestionRetardataire(GestionRetardataire gestionRetardataire) 
	{
		this.gestionRetardataire = gestionRetardataire;
	}

	public GestionJoker getGestionJoker() 
	{
		return gestionJoker;
	}

	public void setGestionJoker(GestionJoker gestionJoker) 
	{
		this.gestionJoker = gestionJoker;
	}
	
	

}
