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
 package fr.amapj.view.views.gestioncontratsignes.modifiermasse.paiement;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.models.contrat.modele.GestionPaiement;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.mespaiements.PaiementService;
import fr.amapj.view.views.gestioncontrat.editorpart.GestionContratEditorPart;

/**
 * Permet de modifier uniquement le texte du paiement et le mode d'affichage du montant  
 * pour les contrats sans gestion des paiements 
 */
public class PopupTextePaiement extends GestionContratEditorPart
{

	/**
	 * 
	 */
	public PopupTextePaiement(Long id)
	{		
		setWidth(80);
		popupTitle = "Modification des instructions pour le paiement";
		
		// Chargement de l'objet  à modifier
		modeleContrat = new GestionContratService().loadModeleContrat(id);
		
		if (modeleContrat.gestionPaiement!=GestionPaiement.NON_GERE)
		{
			throw new AmapjRuntimeException();
		}

		
		setModel(modeleContrat);
	}
	
	
	
	
	
	@Override
	protected void configure()
	{
		add(()->addFieldInfo());
		
	}

	private void addFieldInfo()
	{
		drawDetailPaiement(true);

	}


	@Override
	protected void performSauvegarder() 
	{
		new PaiementService().updateTextePaiement(modeleContrat);
	}
	
}
