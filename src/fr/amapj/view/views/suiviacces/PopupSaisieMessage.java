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

 package fr.amapj.view.views.suiviacces;

import fr.amapj.service.services.session.SessionManager;
import fr.amapj.view.engine.popup.formpopup.FormPopup;
import fr.amapj.view.engine.popup.formpopup.genericmodel.StringItem;

/**
 * Popup pour la saisie d'un message a envoyer a tout le monde
 *  
 */
public class PopupSaisieMessage extends FormPopup
{
	

	private StringItem dto;

	/**
	 * 
	 */
	public PopupSaisieMessage()
	{
		popupTitle = "Envoi d'un message à tous les utilisateurs";
		saveButtonTitle = "Envoyer";
		
		dto = new StringItem();
		dto.value = "ATTENTION - ARRÊT DU SERVEUR DANS UNE MINUTE";
		setModel(dto);
	}
	
	
	protected void addFields()
	{
		// Construction des champs
		addTextAeraField("Message", "value");	
	}

	protected void performSauvegarder()
	{
		SessionManager.broadcast(dto.value);
	}

	
	

}
