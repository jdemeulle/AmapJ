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
 package fr.amapj.view.views.contratsamapien;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.model.models.param.paramecran.PEContratAmapien;
import fr.amapj.model.models.param.paramecran.PEContratAmapien.GestionAjoutContrat;
import fr.amapj.service.services.gestioncontratsigne.GestionContratSigneService;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.popup.PopupListener;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;
import fr.amapj.view.engine.popup.okcancelpopup.OKCancelPopup;
import fr.amapj.view.views.common.contratselector.ContratSelectorPart;
import fr.amapj.view.views.contratsamapien.ContratsAmapienListPart.AjouterData;

/**
 * Popup pour la saisie du producteur et du contrat
 *  
 */
public class PopupSaisieProducteurContrat extends OKCancelPopup implements PopupListener
{
	
	private ContratSelectorPart contratSelectorPart;
	
	private AjouterData data;
	
	private PEContratAmapien peEcran;
	
	/**
	 * 
	 */
	public PopupSaisieProducteurContrat(AjouterData data)
	{
		this.data = data;
		
		popupTitle = "Selection du contrat";
		saveButtonTitle = "Continuer ...";
		setType(PopupType.CENTERFIT);
		
		peEcran = (PEContratAmapien) new ParametresService().loadParamEcran(MenuList.CONTRATS_AMAPIEN);
	}
	
	
	@Override
	protected void createContent(VerticalLayout contentLayout)
	{
		// Partie choix du contrat
		contratSelectorPart = new ContratSelectorPart(this,true);
		HorizontalLayout toolbar1 = contratSelectorPart.getChoixContratComponent();
		
		contentLayout.addComponent(toolbar1);
		
		contratSelectorPart.fillAutomaticValues();
	}

	protected boolean performSauvegarder()
	{
		Long idModeleContrat = contratSelectorPart.getModeleContratId();
		if (idModeleContrat==null)
		{
			return false;
		}
		
		boolean hasContrat = new GestionContratSigneService().checkIfUserHasContrat(idModeleContrat,data.userId);
		if (hasContrat)
		{
			new MessagePopup("Impossible",ColorStyle.RED,"Cet utilisateur possède déjà ce contrat.").open();
			return false;
		}

		if (peEcran.gestionAjoutContrat==GestionAjoutContrat.VERIFICATION_ADHESION)
		{
			boolean hasAdhesion = new GestionContratSigneService().checkIfUserHasAdhesion(idModeleContrat,data.userId);
			if (hasAdhesion==false)
			{
				new MessagePopup("Impossible",ColorStyle.RED,"Cet utilisateur n'est pas à jour de son adhésion pour ce contrat.").open();
				return false;
			}
		}
		 
		
		
		data.validate();
		data.idModeleContrat =  idModeleContrat;
		return true;
	}


	@Override
	public void onPopupClose()
	{
		// Nothing to do 
	}

}
