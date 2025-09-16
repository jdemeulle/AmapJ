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
 package fr.amapj.view.views.archivage.gestion;

import com.vaadin.ui.Button;

import fr.amapj.model.models.permanence.periode.PeriodePermanence;
import fr.amapj.service.services.archivage.ArchivageContratService;
import fr.amapj.service.services.archivage.ArchivageUtilisateurService;
import fr.amapj.service.services.gestioncotisation.GestionCotisationService;
import fr.amapj.service.services.parametres.ParametresArchivageDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.permanence.periode.PeriodePermanenceService;
import fr.amapj.service.services.producteur.ProducteurService;
import fr.amapj.view.engine.popup.PopupListener;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.views.archivage.gestion.PopupSaisieNombreJour.Regle;

/**
 * Saisie des paramètres d'archivage
 *
 */
public class PopupSaisieParametresArchivage extends WizardFormPopup
{

	private ParametresArchivageDTO dto;
	

	/**
	 * 
	 */
	public PopupSaisieParametresArchivage()
	{
		setWidth(80);
		popupTitle = "Modification des paramètres d'archivage";

		this.dto = new ParametresService().getParametresArchivage();
		setModel(dto);

	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldContrat());
		add(()->addFieldUtilisateur());
		add(()->addFieldProducteur());
		add(()->addFieldPermanence());
		add(()->addFieldCotisation());
	}
	
	

	private void addFieldContrat()
	{
		form.removeAllComponents();
		
		// Titre
		setStepTitle("Paramètres d'archivage / suppression des contrats");
		
		//
		String s1 = new ArchivageContratService().computeArchivageLib(dto);
		addBloc(s1,()->addFieldContrat(),new Regle("la date de dernière livraison est plus vieille que","archivageContrat"));
		
		//
		s1 = new ArchivageContratService().computeSuppressionLib(dto);
		addBloc(s1,()->addFieldContrat(),new Regle("la date de dernière livraison est plus vieille que","suppressionContrat"));
	}
	

	private void addFieldUtilisateur()
	{
		form.removeAllComponents();
		
		// Titre
		setStepTitle("Paramètres d'archivage / suppression des utilisateurs");

		//
		String s1 = new ArchivageUtilisateurService().computeArchivageLib(dto);
		addBloc(s1,()->addFieldUtilisateur(),new Regle("la date de fin de sa dernière adhésion ou de sa dernière livraison est plus vieille que","archivageUtilisateur"));
		
		//
		s1 = new ArchivageUtilisateurService().computeSuppressionLib(dto);
		addBloc(s1,()->addFieldUtilisateur());

				
	}

	
	private void addFieldProducteur()
	{
		form.removeAllComponents();
		
		// Titre
		setStepTitle("Paramètres d'archivage / suppression des producteurs");
		
		//
		String s1 = new ProducteurService().computeArchivageLib(dto);
		addBloc(s1,()->addFieldProducteur(),new Regle("la date de dernière livraison est plus vieille que","archivageProducteur"));
		
		//
		s1 = new ProducteurService().computeSuppressionLib(dto);
		addBloc(s1,()->addFieldProducteur());
		
	}
	
	private void addFieldPermanence()
	{
		form.removeAllComponents();
		
		// Titre
		setStepTitle("Paramètres de suppression des périodes de permanence");
		
		//
		String s1 = new PeriodePermanenceService().computeSuppressionLib(dto);
		addBloc(s1,()->addFieldPermanence(),new Regle("la date de fin de cette période de permanence est plus vieille que","suppressionPeriodePermanence"));
		
	}
	
	
	private void addFieldCotisation()
	{
		form.removeAllComponents();
		
		// Titre
		setStepTitle("Paramètres de suppression des périodes de cotisation");
		
		//
		String s1 = new GestionCotisationService().computeSuppressionLib(dto);
		addBloc(s1,()->addFieldCotisation(),new Regle("la date de fin de cette période de cotisation est plus vieille que","suppressionPeriodeCotisation"));
	}
	
	
	private void addBloc(String s1, PopupListener listener, Regle... regles) 
	{
		//
		addHtml(s1);
		if(regles.length>0)
		{
			Button b = new Button("Modifier cette régle", e->new PopupSaisieNombreJour(dto, regles).open(listener));
			form.addComponent(b);
		}
		else
		{
			addHtml("Regle non modifiable");
		}
		
		// Une ligne vide
		addHtml("");

	}

	

	@Override
	protected void performSauvegarder()
	{
		new ParametresService().updateParametresArchivage(dto);
	}
	
}
