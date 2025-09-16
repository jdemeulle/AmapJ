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
 package fr.amapj.view.views.cotisation.reception;

import fr.amapj.service.services.gestioncotisation.GestionCotisationService;
import fr.amapj.service.services.gestioncotisation.PeriodeCotisationUtilisateurDTO;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.popup.formpopup.validator.IValidator;
import fr.amapj.view.engine.popup.formpopup.validator.NotNullValidator;

/**
 * Permet la modification d'une cotisation
 * 
 *
 */
public class PopupModifCotisation extends WizardFormPopup
{

	private PeriodeCotisationUtilisateurDTO dto;


	/**
	 * 
	 */
	public PopupModifCotisation(PeriodeCotisationUtilisateurDTO dto)
	{
		this.dto = dto;
		
		setWidth(40);
		popupTitle = "Modification d'une nouvelle cotisation";
		
		setModel(dto);

	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldInfoGenerales());
	}
	

	private void addFieldInfoGenerales()
	{
		//
		addHtml("Modification pour "+dto.nomUtilisateur+" "+dto.prenomUtilisateur);
		
		IValidator notNull = new NotNullValidator();
		
		// Champ 1
		addCurrencyField("Montant", "montantAdhesion",false);

		//
		addComboEnumField("Etat du paiement", "etatPaiementAdhesion",notNull);
		
		//
		addComboEnumField("Type du paiement", "typePaiementAdhesion",notNull);

	}

	
	@Override
	protected void performSauvegarder()
	{
		new GestionCotisationService().createOrUpdateCotisation(false,dto);
	}
	
}
