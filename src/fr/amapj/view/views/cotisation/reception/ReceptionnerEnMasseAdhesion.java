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

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;

import fr.amapj.common.DateUtils;
import fr.amapj.model.models.cotisation.EtatPaiementAdhesion;
import fr.amapj.service.services.gestioncotisation.BilanAdhesionDTO;
import fr.amapj.service.services.gestioncotisation.GestionCotisationService;
import fr.amapj.service.services.gestioncotisation.PeriodeCotisationUtilisateurDTO;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.tools.table.complex.ComplexTableBuilder;

/**
 * Popup pour la réception en masse des adhesions
 *  
 */
public class ReceptionnerEnMasseAdhesion extends WizardFormPopup
{

	private List<PeriodeCotisationUtilisateurDTO> lines;
	
	private ComplexTableBuilder<PeriodeCotisationUtilisateurDTO> builder;


	/**
	 * Réception pour un contrat
	 */
	public ReceptionnerEnMasseAdhesion(Long idPeriodeCotisation)
	{
		super();
			
		popupTitle = "Réception en masse des adhésions";
		setHeight("100%");
		setWidth(100);
		
		BilanAdhesionDTO bilanAdhesionDTO = new GestionCotisationService().loadBilanAdhesion(idPeriodeCotisation);
		lines = bilanAdhesionDTO.utilisateurDTOs.stream().filter(e->e.etatPaiementAdhesion==EtatPaiementAdhesion.A_FOURNIR).collect(Collectors.toList());
		

	}
	
	
	@Override
	protected void configure()
	{
		add(()->addFieldSaisie(),()->writeToModel());
	}

	private void addFieldSaisie()
	{
		if(lines.size()==0)
		{
			addHtml("Il n'y a pas de d'adhesion à réceptionner");
			setWidth(60);
			return;
		}
		
		builder = new ComplexTableBuilder<PeriodeCotisationUtilisateurDTO>(lines);
		
		builder.addString("Nom", false, 200, e->e.nomUtilisateur);
		builder.addString("Prenom", false, 200, e->e.prenomUtilisateur);
		builder.addCurrency("Montant", "mnt", false, 100, e->e.montantAdhesion);
		builder.addCheckBox("Réceptionné", "cb", true, 200, e->false,null);
		builder.addString("Type du paiement", "type", false, 200, e->e.typePaiementAdhesion+"");
		builder.setPageLength(13);
		
		addComplexTable(builder);
		
		Button b = new Button("Tout cocher",e->handleToutCocher());
		form.addComponent(b);
	}
	
	private void handleToutCocher() 
	{
		for (int i = 0; i < lines.size(); i++)
		{ 
			CheckBox cb = (CheckBox) builder.getComponent(i, "cb");	
			cb.setValue(true);
		}
	}
	

	@Override
	protected void performSauvegarder() throws OnSaveException
	{
		for (PeriodeCotisationUtilisateurDTO pcu : lines) 
		{
			if (pcu.etatPaiementAdhesion == EtatPaiementAdhesion.ENCAISSE)
			{
				new GestionCotisationService().createOrUpdateCotisation(false,pcu);
			}
		}	
	}

	/**
	 * Copie les données saisies dans le modele
	 */
	private String writeToModel() 
	{
		for (int i = 0; i < lines.size(); i++)
		{
			PeriodeCotisationUtilisateurDTO line = lines.get(i);
						
			CheckBox cb = (CheckBox)  builder.getComponent(i, "cb");
			
			if (cb.getValue()==true)
			{
				line.etatPaiementAdhesion = EtatPaiementAdhesion.ENCAISSE;
				line.dateReceptionCheque = DateUtils.getDate();
			}
		}
		
		return null;
	}
}
