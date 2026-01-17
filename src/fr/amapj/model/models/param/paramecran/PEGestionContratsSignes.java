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
 * Parametrage de l'écran gestion des contrats signés
 */
public class PEGestionContratsSignes  extends AbstractParamEcran
{
	// Gestion de l'ajout des contrats
	public GestionAjoutContrat gestionAjoutContrat = GestionAjoutContrat.ACTIF_ET_COTISANT;
	
	static public enum GestionAjoutContrat
	{
		ACTIF_ET_COTISANT ,
		
		ACTIF ,
		
		TOUS;
		
		static public class MetaData extends MetaDataEnum
		{
			
			public void fill()
			{	
				add("Ce champ vous permet de préciser le fonctionnement du bouton \"Ajouter un contrat signé \" dans l'écran Gestion des contrats signés");

				add(ACTIF_ET_COTISANT, "Actif et cotisant" , "Dans ce mode, quand un référent clique sur le bouton \"Ajouter un contrat signé \", alors il obtient "
						+ "la liste de tous les adhérents qui sont actifs et qui sont à jour de leur cotisation pour ce contrat. Le référent ne pourra pas créer un contrat si l'adhérent n'est pas actif ou s'il n'a pas cotisé pour la période de cotisation qui est rattachée à ce contrat.");

				add(ACTIF, "Actif" , "Dans ce mode, quand un référent clique sur le bouton \"Ajouter un contrat signé \", alors il obtient "
						+ "la liste de tous les adhérents qui sont actifs, même si ils ne sont pas à jour de leur cotisation pour ce contrat.");


				add(TOUS, "Tous" , "Dans ce mode, quand un référent clique sur le bouton \"Ajouter un contrat signé \", alors il obtient "
						+ "la liste de tous les adhérents ( actifs ou inactifs).");				
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
