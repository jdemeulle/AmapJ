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
 package fr.amapj.view.views.gestioncontrat.editorpart;

import fr.amapj.service.services.gestioncontrat.GestionContratService;

/**
 * Permet la gestion de la signature en ligne des documents d'engagement
 */
public class ModifGestionDocEngagementEditorPart extends GestionContratEditorPart
{
	
	@Override
	protected void configure()
	{
		add(()->drawSignatureContrat());
	}
	
	/**
	 * 
	 */
	public ModifGestionDocEngagementEditorPart(Long id)
	{
		super();
		popupTitle = "Modification de la gestion des documents d'engagement";
		setWidth(80);
				
		// Chargement de l'objet  à modifier
		modeleContrat = new GestionContratService().loadModeleContrat(id);
		
		setModel(modeleContrat);
		
	}	

	protected void performSauvegarder()
	{	
		// Sauvegarde du contrat
		new GestionContratService().updateSignatureEnLigne(modeleContrat);
	}
	
	/**
	 * Vérifie si il n'y a pas déjà des contrats signés, qui vont empecher de modifier le mode de gestion de la signature en ligne
	 */
	@Override
	protected String checkInitialCondition()
	{
		int nbInscrits = new GestionContratService().getNbInscrits(modeleContrat.id);
		if (nbInscrits!=0)
		{
			String str = "Vous ne pouvez plus modifier directement le mode de gestion des documents d'engagement de ce contrat<br/>"+
						 "car "+nbInscrits+" adhérents ont déjà souscrits à ce contrat.<br/>"+
						 "Si vous souhaitez vraiment faire ceci , vous devez aller dans \"Gestion des contrats signés\", puis vous cliquez sur le bouton \"Modifier en masse\".<br/>";
			return str;
		}
		
		return null;
	}
	
}
