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
 package fr.amapj.view.views.remiseproducteur;

import fr.amapj.service.services.edgenerator.excel.EGRemise;
import fr.amapj.service.services.remiseproducteur.PaiementRemiseDTO;
import fr.amapj.service.services.remiseproducteur.RemiseDTO;
import fr.amapj.service.services.remiseproducteur.RemiseProducteurService;
import fr.amapj.view.engine.excelgenerator.LinkCreator;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;

/**
 * Permet de saisir les remises
 * 
 *
 */
public class RemiseEditorPart extends WizardFormPopup
{
	private RemiseDTO remiseDTO;

	/**
	 * 
	 */
	public RemiseEditorPart(RemiseDTO remiseDTO)
	{
		setWidth(80);
		popupTitle = "Réalisation d'une remise";
		
		this.remiseDTO = remiseDTO;
		
		setModel(remiseDTO);

	}
	
	@Override
	protected void configure()
	{	
		add(()->addFieldAccueil());
		add(()->addFieldAffichage());
		add(()->addFieldConfirmation());
		add(()->addImpression());
	}


	private void addFieldAccueil()
	{
		// Titre
		setStepTitle("saisie de la date de la remise");
		
		String montant = new CurrencyTextFieldConverter().convertToString(remiseDTO.mnt)+" €";
		String text = 	"Vous allez valider une remise de chèques à un producteur<br/>"+
						"Date de remise prévue au contrat : "+remiseDTO.moisRemise+"<br/>"+
						"Montant total de la remise "+montant+"<br/><br/>"+
						"Merci de saisir ci dessous la date réelle de remise des chèques";
		
		addHtml(text);
		
		//
		addDateField("Date réelle de la remise", "dateReelleRemise");

	}

	private void addFieldAffichage()
	{
		// Titre
		setStepTitle("les chèques à inclure dans la remise");
		
		
		String text = 	"Voici la liste des chèques à inclure dans la remise :<br/>";
		
		for (PaiementRemiseDTO paiement : remiseDTO.paiements)
		{
			String montant = new CurrencyTextFieldConverter().convertToString(paiement.montant)+" €";
			text = text+paiement.nomUtilisateur+" "+paiement.prenomUtilisateur+" - Montant = "+montant;
			text = add(text,paiement.commentaire1);
			text = add(text,paiement.commentaire2);
			text = add(text,paiement.commentaire3);
			text = add(text,paiement.commentaire4);
			text=text+"<br/>";
		}
		
		addHtml(text);
		
	}
	
	
	private String  add(String text, String commentaire) 
	{
		if (commentaire!=null)
		{
			return text+" - "+commentaire;
		}
		else
		{
			return text;
		}
	}

	private void addFieldConfirmation()
	{
		// Titre
		setStepTitle("confirmation");
		setNextButtonAsSave();
		
		String text = 	"Confirmez vous avoir tous les chèques ? <br/>";
		
		addHtml(text);
		
	}
	

	private void addImpression() 
	{
		// Enregistrement de la remise
		Long id = new RemiseProducteurService().performRemise(remiseDTO);
		
		// Titre
		setStepTitle("impression");
		setAllButtonsAsOK();
		
		addHtml("<b>La remise a bien été enregistrée.</b><br/>En cliquant sur le lien ci dessous, vous pouvez imprimer un bilan des chèques de cette remise, à donner au producteur avec les chèques.");
		
		form.addComponent(LinkCreator.createLink(new EGRemise(id)));
	}
	
	
	@Override
	protected void performSauvegarder()
	{
		// Nothing to do 
	}

}
