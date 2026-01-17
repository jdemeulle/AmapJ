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
 * Parametrage de l'écran "Mes adhesions"
 */
public class PEMesAdhesions  extends AbstractParamEcran
{
	// Gestion des adhesions
	public GestionAdhesion gestionAdhesion= GestionAdhesion.INSCRIPTION_ADHERENTS;
	
	static public enum GestionAdhesion
	{
		INSCRIPTION_ADHERENTS ,
		
		INSCRIPTION_TRESORIER ;
		
		static public class MetaData extends MetaDataEnum
		{
			public void fill()
			{	
				addLink("La gestion des adhésions", "docs_utilisateur_adhesion.html");
				
				add("Ce champ vous permet d'indiquer comment vous allez gérer les adhésions (ou cotisations) dans votre AMAP.");

				add(INSCRIPTION_ADHERENTS, "Adhésion saisie par les adhérents" , "Dans ce mode, les adhérents peuvent renouveller leur adhésion en ligne, sans intevention du trésorier. Pour cela, ils vont dans l'écran Mes Adhésions.");

				add(INSCRIPTION_TRESORIER, "Adhésion saisie par le trésorier" , "Dans ce mode, les adhérents peuvent visualiser les conditions d'adhésion en ligne, mais ils ne peuvent pas saisir eux même leur adhésion. Le trésorier va saisir lui même l'adhésion, quand il reçoit le chèque de la part de l'amapien. Ceci peut être pratique pour être sûr de bien réceptionner les chèques.");

				
			}
		}			
	}
	
	//  
	public ImpressionBulletin impressionBulletinMode = ImpressionBulletin.FROM_POPUP;
	
	static public enum ImpressionBulletin
	{
		FROM_POPUP ,
		
		FROM_LISTE_AND_POPUP ;
		
		static public class MetaData extends MetaDataEnum
		{
			public void fill()
			{	
				addLink("La gestion des adhésions", "docs_utilisateur_adhesion.html");
				
				add("Ce champ vous permet d'indiquer comment les bulletins d'adhésions vont s'afficher sur cet écran.");

				add(FROM_POPUP, "Dans le popup de saisie" , "L'adhérent peut voir et imprimer son bulletin d'adhésion dans le popup de saisie de l'adhésion (il faut cliquer sur Adhérer ou Voir).");

				add(FROM_LISTE_AND_POPUP, "Dans le popup de saisie ET dans la page Mes adhésions" , "L'adhérent peut voir et imprimer son bulletin d'adhésion dans le popup de saisie de l'adhésion ET dans la page Mes Adhésions");

				
			}
		}			
	}
	
	
	// Getters et setters
	public GestionAdhesion getGestionAdhesion()
	{
		return gestionAdhesion;
	}

	public void setGestionAdhesion(GestionAdhesion gestionAdhesion)
	{
		this.gestionAdhesion = gestionAdhesion;
	}

	public ImpressionBulletin getImpressionBulletinMode()
	{
		return impressionBulletinMode;
	}

	public void setImpressionBulletinMode(ImpressionBulletin impressionBulletinMode)
	{
		this.impressionBulletinMode = impressionBulletinMode;
	}
	
	
	

}
