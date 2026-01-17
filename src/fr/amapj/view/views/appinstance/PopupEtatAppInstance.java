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

import com.vaadin.ui.ComboBox;

import fr.amapj.model.engine.db.DbManager;
import fr.amapj.service.services.appinstance.AppInstanceDTO;
import fr.amapj.view.engine.popup.formpopup.FormPopup;

/**
 * Changement de l'état d'une ou plusieurs instances
 *
 */
@SuppressWarnings("serial")
public class PopupEtatAppInstance extends FormPopup
{

	private List<AppInstanceDTO> dtos;
	
	private AppInstanceDTO state = new AppInstanceDTO();

	/**
	 * 
	 */
	public PopupEtatAppInstance(List<AppInstanceDTO> dtos)
	{
		popupTitle = "État des instances";
		this.dtos = dtos;

		setModel(state);

	}
	
	
	protected void addFields()
	{
		String str = "Vous allez modifier l'état de "+dtos.size()+" bases<br/><br/>";
		str = str +"Liste des bases <br/>";
		
		for (AppInstanceDTO appInstanceDTO : dtos)
		{
			str = str+ appInstanceDTO.nomInstance+"<br/>";
		}
					
		
		addHtml(str);
		
		ComboBox box = addComboEnumField("Nouvel état", "state");
		box.setRequired(true);	
	}
	

	
	@Override
	protected void performSauvegarder()
	{
		for (AppInstanceDTO dto : dtos)
		{
			DbManager.get().setDbState(dto.nomInstance,state.state);
		}
	}

	
}
