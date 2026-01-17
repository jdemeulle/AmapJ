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
 package fr.amapj.view.views.archivage.contrat;

import com.vaadin.ui.OptionGroup;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.models.contrat.modele.EtatModeleContrat;
import fr.amapj.service.services.archivage.ArchivageContratService;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.gestioncontrat.ModeleContratSummaryDTO;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;

/**
 * Popup pour la saisie de l'état du contrat
 *  
 */
public class PopupRetourActif extends WizardFormPopup
{
	private ModeleContratSummaryDTO mcDto;
	
	
	@Override
	protected void configure()
	{
		add(()->addSaisie());
		add(()->addConfirmation());
	}
	
	

	/**
	 * 
	 */
	public PopupRetourActif(ModeleContratSummaryDTO mcDto)
	{
		popupTitle = "Sortir un contrat des archives";
		this.mcDto = mcDto;
	}
	
	
	protected void addSaisie()
	{
		addHtml("Le contrat "+mcDto.nom+" est actuellement à l'état Archivé.");
		
		addHtml("Avec cet outil, vous allez pouvoir le replacer à l'état ACTIF, ce qui vous permettra de faire des modifications sur ce contrat.");
		
		addHtml("Cet outil ne devrait être utilisé qu'en cas d'erreur constatée après l'archivage du contrat.");
		
	}



	
	private void addConfirmation()
	{
		String str = "Êtes-vous sûr de vouloir sortir ce contrat des archives et de le placer à l'état ACTIF ?";
		
		addHtml(str);
	}
	
	

	protected void performSauvegarder()
	{
		new GestionContratService().updateEtat(EtatModeleContrat.ACTIF,mcDto.id);
	}
}
