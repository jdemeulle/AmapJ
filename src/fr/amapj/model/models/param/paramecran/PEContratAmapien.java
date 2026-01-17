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
import fr.amapj.model.models.param.paramecran.common.AbstractParamEcran;

/**
 * Parametrage de l'écran "Contrats d'un amapien"
 */
public class PEContratAmapien  extends AbstractParamEcran
{
	// Gestion de l'ajout des contrats
	public GestionAjoutContrat gestionAjoutContrat = GestionAjoutContrat.VERIFICATION_ADHESION;
	
	static public enum GestionAjoutContrat
	{
		VERIFICATION_ADHESION ,
		
		PAS_DE_VERIFICATION ;
		
		
		static public class MetaData extends MetaDataEnum
		{
			
			public void fill()
			{	
				add("Ce champ vous permet de préciser le fonctionnement du bouton \"Ajouter un contrat signé \" dans l'écran Contrats d'un amapien");

				add(VERIFICATION_ADHESION, "Vérification de l'adhésion" , "Dans ce mode, quand un référent clique sur le bouton \"Ajouter nouveau contrat \", alors il peut "
						+ "inscrire l'adhérent sur un nouveau contrat uniquement si l'adhérent est à jour de sa cotisation . Le référent ne pourra pas créer un contrat si l'adherent n'a pas cotisé pour la période de cotisation qui est rattachée à ce contrat.");

				add(PAS_DE_VERIFICATION, "Pas de vérification" , "Dans ce mode, quand un référent clique sur le bouton \"Ajouter nouveau contrat \", alors il peut "
						+ "inscrire l'adhérent sur tous les contrats, même si l'adhérent n'est pas à jour de sa cotisation pour ce contrat.");

			}		
		}			
	}

	
	
	
	
	// Getters et setters
	public GestionAjoutContrat getGestionAjoutContrat() 
	{
		return gestionAjoutContrat;
	}

	public void setGestionAjoutContrat(GestionAjoutContrat gestionAjoutContrat) 
	{
		this.gestionAjoutContrat = gestionAjoutContrat;
	}
	
	
	

}
