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

import java.util.List;

import fr.amapj.service.services.appinstance.AppInstanceDTO;
import fr.amapj.service.services.appinstance.AppInstanceService;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;

/**
 * Permet de sauvegarder les instances
 * 
 *
 */
public class PopupSaveAppInstance extends WizardFormPopup
{

	private List<AppInstanceDTO>  appInstanceDTOs;
	

	/**
	 * 
	 */
	public PopupSaveAppInstance(List<AppInstanceDTO> appInstanceDTOs )
	{
		
		popupTitle = "Sauvegarder les bases";
		saveButtonTitle = "OK";
		this.appInstanceDTOs = appInstanceDTOs;
		
		
	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldGeneral());
		add(()->addSave());
	}


	private void addFieldGeneral()
	{
		
		// Titre
		setStepTitle("informations");
		
		// Champ 1
		
		String str = "Cet outil permet de sauvegarder les bases sélectionnées<br/><br/>";
		
		str = str +"Le nombre de bases est : "+appInstanceDTOs.size()+" bases<br/><br/>";
		str = str +"Liste des bases <br/>";
		
		for (AppInstanceDTO appInstanceDTO : appInstanceDTOs)
		{
			str = str+ appInstanceDTO.nomInstance+"<br/>";
		}
					
		addHtml(str);
		
		
	}


	private void addSave()
	{
		List<String> res = new AppInstanceService().saveInstance(appInstanceDTOs);
		
		// Titre
		setStepTitle("visualisation du résultat : ");
		
		// Champ 1
		String str = "";
		for (String string : res)
		{
			str = str+string+"<br/>";
		}
		
		addHtml(str);
		
	}
	
	

	@Override
	protected void performSauvegarder() throws OnSaveException
	{
		// Do nothing
	}

}
