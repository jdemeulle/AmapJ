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
 package fr.amapj.view.views.parametres.paramecran;

import com.vaadin.data.util.BeanItem;

import fr.amapj.model.models.param.paramecran.PEVisiteAmap;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.popup.formpopup.validator.NotNullValidator;

/**
 * Permet la saisie des paramètres de l'écran "visite Amap"
 * 
 */
public class PEVisiteAmapEditorPart extends WizardFormPopup
{
	private PEVisiteAmap pe;

	/**
	 * 
	 */
	public PEVisiteAmapEditorPart()
	{
		pe = (PEVisiteAmap) new ParametresService().loadParamEcran(MenuList.VISITE_AMAP);
		
		setWidth(80);
		popupTitle = "Paramètrage de l'écran \""+pe.getMenu().getTitle()+"\"";
		
		setModel(this.pe);

	}
	
	@Override
	protected void configure()
	{
		add(()->addGeneralites());
		add(()->addDetailsProducteur());
	
	}

	private void addGeneralites()
	{
		// Titre
		setStepTitle("Généralités");
		
		//
		addComboEnumField("L'écran en entier est visible par ", "accesEcran", new NotNullValidator());
		
		// 
		addComboEnumField("Mode d'affichage", "modeAffichage",  new NotNullValidator());
				
	}
	
	
	private void addDetailsProducteur()
	{
		// Titre
		setStepTitle("Paramétrage du popup info producteur");
		
		//
		addComboEnumField("Contenu du popup info producteur (accessible par ?)", "infoProducteurModeAffichage", new NotNullValidator());
		
		// 
		addComboEnumField("Le mail du producteur est affiché", "producteurEmail", new NotNullValidator());
		addComboEnumField("Le téléphone 1 du producteur est affiché", "producteurTel1", new NotNullValidator());
		addComboEnumField("Le téléphone 2 du producteur est affiché", "producteurTel2", new NotNullValidator());
		
		addComboEnumField("Le mail du référent est affiché", "referentEmail", new NotNullValidator());
		addComboEnumField("Le téléphone 1 du référent est affiché", "referentTel1", new NotNullValidator());
		addComboEnumField("Le téléphone 2 du référent est affiché", "referentTel2", new NotNullValidator());		
	}
	

	@Override
	protected void performSauvegarder()
	{
		new ParametresService().update(pe);
	}

}
