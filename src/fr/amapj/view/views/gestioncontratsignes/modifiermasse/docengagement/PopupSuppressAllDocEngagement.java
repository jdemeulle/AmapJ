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
 package fr.amapj.view.views.gestioncontratsignes.modifiermasse.docengagement;

import java.util.List;

import com.vaadin.ui.TextField;

import fr.amapj.service.services.docengagement.signonline.DocEngagementSignOnLineDTO;
import fr.amapj.service.services.docengagement.signonline.DocEngagementSignOnLineService;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.gestioncontrat.ModeleContratDTO;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;

/**
 * Permet de supprimer tous les documents d'engagement déjà signés en ligne 
 */
public class PopupSuppressAllDocEngagement extends WizardFormPopup
{
	private ModeleContratDTO modeleContrat;
	private TextField tf;
	private long nbDocSigneAmapien;

	@Override
	protected void configure()
	{
		add(()->drawIntro());
		add(()->drawPerformAction(),()->checkPerformAction());
		add(()->drawBilan());
	}
	
	/**
	 * 
	 */
	public PopupSuppressAllDocEngagement(Long id)
	{
		super();
		popupTitle = "Suppression des documents d'engagement déjà signés";
		setWidth(80);
				
		modeleContrat = new GestionContratService().loadModeleContrat(id);
		List<DocEngagementSignOnLineDTO> docs = new DocEngagementSignOnLineService().getBilanSignature(id);
		nbDocSigneAmapien = docs.stream().filter(e->e.signedByAmapien!=null).count();
	}
	
	
	protected String checkInitialCondition()
	{
		if (nbDocSigneAmapien==0)
		{
			return "Ce contrat n'a pas de document d'engagement déjà signé";
		}
		return null;
	}
	
	
	
	private void drawIntro() 
	{
		setStepTitle("Introduction");
		
		addText("vous allez modifier le contrat "+modeleContrat.nom+", qui utilise la signature en ligne des documents d'engagement.");
		
		addText("Cet outil permet de supprimer tous les documents d'engagement déjà signés.");
		
		addText("Par contre, les données des contrats souscrits (quantité, paiement) seront bien conservées.");
		
		addText("Une fois les documents d'engagement supprimés, les amapiens pourront les signer de nouveau en allant dans l'écran \"Mes contrats\", en cliquant juste sur \"Signer\", sans avoir à ressaisir les quantités.");
		
		addText("Cet outil doit être utilisé par exemple quand vous constatez une erreur importante dans les documents d'engagements déjà signés, et vous souhaitez donc les refaire signer avec le bon document.");
				
	}
	
	
	
	private void drawPerformAction() 
	{
		// Titre
		setStepTitle("Confirmation");
		
		addText("Le contrat "+modeleContrat.nom+" comprend "+nbDocSigneAmapien+" documents d'engagement déjà signés en ligne.");
		
		addText("Vous allez d'effacer de façon définitive ces documents d'engagement.");
		
		addHtml("Si vous souhaitez continuer, vous devez saisir ci dessous le texte <br/>JE CONFIRME<br/> puis cliquer sur Continuer");
		
		tf = new TextField("");
		tf.setValue("");
		tf.setImmediate(true);
		form.addComponent(tf);		
	}

	private String checkPerformAction() 
	{
		if (tf.getValue().equals("JE CONFIRME"))
		{
			return null;
		}
		else
		{
			return "Pour pouvoir continuer, vous devez saisir le texte JE CONFIRME"; 
		}		
	}
	
	private void drawBilan() 
	{
		setAllButtonsAsOK();
		String res = new DocEngagementSignOnLineService().deleteAllDocEngagement(modeleContrat);
		
		// Titre
		setStepTitle("bilan");
		
		addText("Les documents d'engagement ont été supprimés et pourront être signés de nouveau.");
		
		addHtml(res);		
	}


	protected void performSauvegarder()
	{	
		// Nothing to do 
	}
}
