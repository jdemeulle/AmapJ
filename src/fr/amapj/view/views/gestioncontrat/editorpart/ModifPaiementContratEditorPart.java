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
import fr.amapj.service.services.mespaiements.PaiementService;
import fr.amapj.view.views.gestioncontrat.editorpart.utils.GestionContratPaiementUtils;

/**
 * Permet de modifier les infos de paiements
 * 
 *
 */
public class ModifPaiementContratEditorPart extends GestionContratEditorPart
{
	
	/**
	 * 
	 */
	public ModifPaiementContratEditorPart(Long id)
	{
		setWidth(80);
		popupTitle = "Modification des conditions de paiement d'un contrat";
		
		// Chargement de l'objet  à modifier
		modeleContrat = new GestionContratService().loadModeleContrat(id);
		new GestionContratPaiementUtils().checkAndUpdateStrategieAndSaisiePaiementCalculDate(modeleContrat);
		
		setModel(modeleContrat);

	}
	
	@Override
	protected void configure()
	{
		add(()->drawTypePaiement(null),()->checkTypePaiement());
		add(()->drawDetailPaiement(true));
	}

	
	
	@Override
	protected void performSauvegarder()
	{
		new PaiementService().updateInfoPaiement(modeleContrat,true);
	}
	
	/**
	 * Vérifie si il n'y a pas déjà des contrats signés, qui vont empecher de modifier les dates de paiements
	 */
	@Override
	protected String checkInitialCondition()
	{
		int nbInscrits = new GestionContratService().getNbInscrits(modeleContrat.id);
		if (nbInscrits!=0)
		{
			String str = "Vous ne pouvez plus modifier les conditions de paiement de ce contrat<br/>"+
						 "car "+nbInscrits+" adhérents ont déjà souscrits à ce contrat<br/>."+
						 "Si vous souhaitez vraiment modifier les conditions de paiement, vous devez aller dans \"Gestion des contrats signés\", puis vous cliquez sur le bouton \"Modifier en masse\".<br/>";
			return str;
		}
		
		return null;
	}
}
