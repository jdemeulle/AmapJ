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
 package fr.amapj.view.engine.popup.tenantnotificationpopup;

import com.vaadin.navigator.Navigator;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.view.engine.popup.corepopup.CorePopup;

/**
 *  
 */

public class OnStartTenantNotificationPopup extends CorePopup
{
	protected Button laterButton;
	
	protected Button nowButton;

	private Navigator nav;
	
	/**
	 * @param nav 
	 *  
	 */
	public OnStartTenantNotificationPopup(Navigator nav)
	{
		this.nav = nav;
		setHeight("50%");
		popupTitle = "Notifications";
		setColorStyle(ColorStyle.GREEN);
	}
	
	protected void createButtonBar()
	{		
		laterButton = addButton("Lire les messages plus tard",e->close());
		addButtonBlank();
		nowButton = addDefaultButton("Lire les messages maintenant",e->{ close(); nav.navigateTo("/tenant_notification");});
	}
	

	protected void createContent(VerticalLayout contentLayout)
	{
		String str1 = "Vous avez des nouveaux messages concernant votre hébergement AmapJ.<br/><br/>Vous pouvez les lire en allant dans TRESORIER / Hébergement AmapJ<br/><br/>";
		
		Label la = new Label(str1,ContentMode.HTML);	
		contentLayout.addComponent(la);
	}
	
}
