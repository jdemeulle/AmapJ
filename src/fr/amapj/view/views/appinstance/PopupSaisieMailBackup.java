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
 package fr.amapj.view.views.appinstance;

import fr.amapj.model.models.param.paramecran.PEExtendedParametres;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;

/**
 */
public class PopupSaisieMailBackup extends WizardFormPopup
{

	private PEExtendedParametres pe;

	/**
	 * 
	 */
	public PopupSaisieMailBackup()
	{
		setWidth(90);
		setHeight("90%");
		
		popupTitle = "Saisie du mail de backup";
		
		pe = (PEExtendedParametres) new ParametresService().loadParamEcran(MenuList.EXTENDED_PARAMETRES);
		
		setModel(pe);

	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldText());
	}
	
	private void addFieldText()
	{
		addCKEditorFieldForDocument("masterDbEmailBackupContent");    
	}

	@Override
	protected void performSauvegarder()
	{
		new ParametresService().update(pe);
	}
}
