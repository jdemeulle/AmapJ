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

 package fr.amapj.view.views.utilisateur;

import com.vaadin.data.util.BeanItem;

import fr.amapj.service.services.gestioncotisation.GestionCotisationService;
import fr.amapj.service.services.gestioncotisation.PeriodeCotisationUtilisateurDTO;
import fr.amapj.service.services.utilisateur.UtilisateurDTO;
import fr.amapj.service.services.utilisateur.UtilisateurService;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.popup.formpopup.validator.IValidator;
import fr.amapj.view.engine.popup.formpopup.validator.NotNullValidator;
import fr.amapj.view.views.searcher.SDPeriodeCotisationEnCours;

/**
 * Permet l'ajout d'une nouvelle cotisation sur l'utilisateur que l'on est en train de créer
 * 
 *
 */
public class CreationUtilisateurAjoutCotisation extends WizardFormPopup
{

	private PeriodeCotisationUtilisateurDTO dto;
	private UtilisateurDTO utilisateurDto;

	/**
	 * 
	 */
	public CreationUtilisateurAjoutCotisation(Long idUtilisateur)
	{
		utilisateurDto = new UtilisateurService().loadUtilisateurDto(idUtilisateur);
				
		
		setWidth(40);
		popupTitle = "Ajout d'une nouvelle adhésion pour "+utilisateurDto.nom+" "+utilisateurDto.prenom;
		
		this.dto = new PeriodeCotisationUtilisateurDTO();
		this.dto.idUtilisateur = utilisateurDto.id;
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
		IValidator notNull = new NotNullValidator();
		
		// 
		addSearcher("Période d'adhésion", "idPeriodeCotisation", new SDPeriodeCotisationEnCours(), null,notNull);
	
		// 
		addCurrencyField("Montant", "montantAdhesion",false);

		//
		addComboEnumField("État du paiement", "etatPaiementAdhesion",notNull);
		
		//
		addComboEnumField("Type du paiement", "typePaiementAdhesion",notNull);

	}

	
	@Override
	protected void performSauvegarder()
	{
		new GestionCotisationService().createOrUpdateCotisation(true,dto);
	}

	
}
