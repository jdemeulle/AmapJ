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

import fr.amapj.model.models.param.paramecran.PEMesAdhesions;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.popup.formpopup.validator.NotNullValidator;

/**
 * Permet la saisie des paramètres de l'écran "Mes adhésions"
 * 
 */
public class PEMesAdhesionsEditorPart extends WizardFormPopup
{

	private PEMesAdhesions pe;

	/**
	 * 
	 */
	public PEMesAdhesionsEditorPart()
	{
		pe = (PEMesAdhesions) new ParametresService().loadParamEcran(MenuList.MES_ADHESIONS);
		
		setWidth(80);
		popupTitle = "Paramètrage de l'écran \""+pe.getMenu().getTitle()+"\"";

		setModel(this.pe);

	}
	
	@Override
	protected void configure()
	{
		add(()->addGeneral());
	
		
	}

	private void addGeneral()
	{
		// Titre
		setStepTitle("Gestion des adhésions");
		
		addComboEnumField("Mode de saisie des adhésions", "gestionAdhesion",  new NotNullValidator());
		
		addComboEnumField("Mode d'affichage du lien \"Imprimer son bulletin d'adhésion\"", "impressionBulletinMode",  new NotNullValidator());
		
	}
	

	@Override
	protected void performSauvegarder()
	{
		new ParametresService().update(pe);
	}
}
