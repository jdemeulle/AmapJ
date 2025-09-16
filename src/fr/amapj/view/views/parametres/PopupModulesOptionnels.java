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
 package fr.amapj.view.views.parametres;

import com.vaadin.data.util.BeanItem;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.RichTextArea;

import fr.amapj.model.models.param.SmtpType;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.session.SessionManager;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.popup.formpopup.validator.NotNullValidator;

/**
 * Permet à un utilisateur de mettre à jour ses coordonnées
 * 
 *
 */
public class PopupModulesOptionnels extends WizardFormPopup
{

	private ParametresDTO dto;


	/**
	 * 
	 */
	public PopupModulesOptionnels(ParametresDTO dto)
	{
		setWidth(95);
		popupTitle = "Modules optionnels";

		this.dto = dto;
		setModel(dto);

	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldPermanenceInfo());
		add(()->addFieldMailPeriodique());
		add(()->addFieldGestionCotisation());
		add(()->addFieldGestionStock());
	}
	
	
	private void addFieldPermanenceInfo()
	{
		// Titre
		setStepTitle("module \"Gestion des permanences\"");
				
		// Champ 9
		addComboEnumField("Activation du module \"Gestion des permanences\"", "etatPlanningDistribution");
		
		addComboEnumField("Envoi d'un mail de rappel pour les permanences",  "envoiMailRappelPermanence");
		
		addIntegerField("Délai en jours entre la permanence et l'envoi du mail", "delaiMailRappelPermanence");
		
		addTextField("Titre du mail de rappel pour les permanences", "titreMailRappelPermanence");
		
		RichTextArea f =  addRichTextAeraField("Contenu du mail de rappel pour les permanences", "contenuMailRappelPermanence");
		f.setHeight(8, Unit.CM);

	}
	
	private void addFieldMailPeriodique()
	{
		// Titre
		setStepTitle("Envoi d'un mail périodique (tous les mois)");
				
		// Champ 9
		addComboEnumField("Activation de l'envoi d'un mail périodique (tous les mois)",  "envoiMailPeriodique");
		
		addIntegerField("Numéro du jour dans le mois où le mail sera envoyé", "numJourDansMois");
		
		addTextField("Titre du mail périodique", "titreMailPeriodique");
		
		RichTextArea f =  addRichTextAeraField("Contenu du mail périodique", "contenuMailPeriodique");
		f.setHeight(8, Unit.CM);

	}
	
	private void addFieldGestionCotisation()
	{
		// Titre
		setStepTitle("module \"Gestion des cotisations\"");
				
		// Champ 9
		addComboEnumField("Activation du module \"Gestion des cotisations\"",  "etatGestionCotisation");
		
	}
	
	
	private void addFieldGestionStock()
	{
		// Titre
		setStepTitle("module \"Gestion des limites en quantité \"");
				
		// Champ 9
		addComboEnumField("Activation du module \"Gestion des limites en quantité \"",  "etatGestionStock");
		
	}
	

	@Override
	protected void performSauvegarder()
	{
		new ParametresService().update(dto);
	}
	
}
