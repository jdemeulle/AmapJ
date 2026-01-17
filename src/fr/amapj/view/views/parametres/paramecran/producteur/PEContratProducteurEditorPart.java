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
 package fr.amapj.view.views.parametres.paramecran.producteur;

import fr.amapj.model.models.param.EtatModule;
import fr.amapj.model.models.param.paramecran.producteur.PEContratProducteur;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.popup.formpopup.validator.NotNullValidator;

/**
 * Permet la saisie des paramètres de l'écran "Contrat d'un producteur"
 * 
 */
public class PEContratProducteurEditorPart extends WizardFormPopup
{
	private PEContratProducteur pe;

	/**
	 * 
	 */
	public PEContratProducteurEditorPart()
	{
		pe = (PEContratProducteur) new ParametresService().loadParamEcran(MenuList.CONTRATS_PRODUCTEUR);
		
		setWidth(80);
		popupTitle = "Paramètrage de l'écran \""+pe.getMenu().getTitle()+"\"";
		
		setModel(this.pe);

	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldGeneralites());
	
	}

	private void addFieldGeneralites()
	{
		// Titre
		setStepTitle("Généralités");
		
		if (new ParametresService().getParametres().etatGestionStock==EtatModule.ACTIF)
		{
			addComboEnumField("Possibilité de modifier les quantités en stock", "modifierQteStock", new NotNullValidator());
		}
		
		addComboEnumField("Possibilité de télécharger la feuille de collecte des chèques", "telechargerFeuilleCollecteCheque", new NotNullValidator());
				
		
	}
	

	@Override
	protected void performSauvegarder()
	{
		new ParametresService().update(pe);
	}
}
