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
 package fr.amapj.view.engine.popup.okcancelpopup;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;

/**
 * Popup de base , avec deux boutons Sauvegarder et Annuler
 *  
 */
@SuppressWarnings("serial")
abstract public class OKCancelPopup extends CorePopup
{
	protected Button saveButton;
	protected String saveButtonTitle = "Sauvegarder";
	protected boolean hasSaveButton = true;
	protected Button cancelButton;
	protected String cancelButtonTitle = "Annuler";
	protected boolean hasCancelButton = true;
	
	
	
	protected void createButtonBar()
	{		
		addButtonBlank();
		if (hasCancelButton)
		{
			cancelButton = addButton(cancelButtonTitle, e->handleAnnuler());
		}
		
		if (hasSaveButton)
		{
			saveButton = addDefaultButton(saveButtonTitle, e->handleSauvegarder());
		}
				
	}
	

	protected void handleAnnuler()
	{
		close();
	}

	protected void handleSauvegarder()
	{
		try
		{
			boolean ret = performSauvegarder();
			if (ret)
			{
				close();
			}
		} 
		catch (OnSaveException e)
		{
			e.showInNewDialogBox();
			return;
			
		}	
	}
	
	/**
	 * Retourne true si on doit fermer la fenetre, false sinon
	 */
	abstract protected boolean performSauvegarder() throws OnSaveException;

	abstract protected void createContent(VerticalLayout contentLayout);
	
}
