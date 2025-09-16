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
 package fr.amapj.view.views.gestioncontratsignes.modifiermasse.date;

import com.vaadin.data.util.BeanItem;

import fr.amapj.service.services.gestioncontratsigne.AnnulationDateLivraisonDTO;
import fr.amapj.service.services.gestioncontratsigne.GestionContratSigneService;
import fr.amapj.service.services.gestioncontratsigne.GestionContratSigneService.ResBarrerDate;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;

/**
 * Popup 
 * 
 *
 */
public class PopupBarrerDateLivraison extends WizardFormPopup
{

	private AnnulationDateLivraisonDTO annulationDto;
	private ResBarrerDate resBarrerDate;

	/**
	 * 
	 */
	public PopupBarrerDateLivraison(Long mcId)
	{
		setWidth(80);
		popupTitle = "Barrer une ou plusieurs dates de livraison (avec mise à zéro des quantités commandées)";

		// Chargement de l'objet à créer
		annulationDto = new GestionContratSigneService().getAnnulationDateLivraisonDTO(mcId);
		
		setModel(annulationDto);

	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldInfoGenerales());
		add(()->addFieldSaisieDate());
		add(()->addFieldConfirmation());
			
	}

	private void addFieldInfoGenerales()
	{
		// Titre
		setStepTitle("les informations générales.");
		
		String str =    "Cet outil va vous permettre de barrer une ou plusieurs dates de livraisons, pour tous les adhérents à ce contrat.<br/>"+
				        "Les quantités commandées sur les dates barrées seront alors remises à zéro pour les contrats déjà signés.<br/>"+ 	
						"<br/>"+
						"Exemple de cas d'utilisation : un producteur a prévu de livrer ses produits pendant 4 mois à l'AMAP<br/>"+
						"20 adhérents ont souscrits à ce contrat sur les 4 mois<br/>"+
						"Suite à un problème agricole ou autre, le producteur cesse ses livraisons au bout de 3 mois<br/>"+
						"Cet outil permet alors de barrer les dates et de mettre à zéro les quantités commandées sur le dernier mois<br/>"+
						"Il faut ensuite gérer le trop payé des adhérents sous la forme d'un avoir<br/>"+
						"<br/><br/>"+
						"Autre exemple : un producteur ne peut assurer une livraison suite à un problème de dernière minute<br/>"+
						"Cet outil permet alors de mettre à zéro les quantités commandées sur cette livraison et de barrer la livraison.<br/>";
										
		
		addHtml(str);
		

	}
	
	

	private void addFieldSaisieDate()
	{
		// Titre
		setStepTitle("les dates de la première et de la dernière livraison à annuler");
		
		String str = 	"Toutes les dates de livraisons comprises entre ces deux dates seront annulées (quantités mise à zéro)</br>";
		addHtml(str);
		
		//
		addDateField("Date de la première date à annuler","dateDebut");
		
		addDateField("Date de la dernière date à annuler","dateFin");

	}
	
	private void addFieldConfirmation()
	{
		// Titre
		setStepTitle("confirmation avant suppression");
		
		resBarrerDate = new GestionContratSigneService().getAnnulationInfo(annulationDto);
		
		addHtml("Vous allez apporter les modifications suivantes sur ce contrat:");
		
		addHtml(resBarrerDate.msg);
		
		addHtml("Appuyez sur Sauvegarder pour réaliser cette modification, ou Annuler pour ne rien modifier");
		
	}


	

	@Override
	protected void performSauvegarder() throws OnSaveException
	{
		new GestionContratSigneService().performAnnulationDateLivraison(annulationDto,resBarrerDate);
	}
	
}
