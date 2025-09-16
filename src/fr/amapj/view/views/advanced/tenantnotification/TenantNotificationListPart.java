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
 package fr.amapj.view.views.advanced.tenantnotification;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.shared.ui.label.ContentMode;

import fr.amapj.messagingcenter.miniproxy.core.ServiceNotAvailableException;
import fr.amapj.service.services.advanced.tenantnotification.SmallTenantNotificationDTO;
import fr.amapj.service.services.advanced.tenantnotification.TenantNotificationService;
import fr.amapj.view.engine.listpart.ButtonType;
import fr.amapj.view.engine.listpart.StandardListPart;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.corepopup.CorePopup.ColorStyle;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;


/**
 * Gestion des notifications
 *
 */
@SuppressWarnings("serial")
public class TenantNotificationListPart extends StandardListPart<SmallTenantNotificationDTO> 
{

	public TenantNotificationListPart()
	{
		super(SmallTenantNotificationDTO.class,false);
	}
	
	
	@Override
	protected String getTitle() 
	{
		return "Hébergement AmapJ - Liste des messages";
	}


	@Override
	protected void drawButton() 
	{
		addButton("Visualiser",ButtonType.EDIT_MODE,e->handleVisualiser());
		addButtonAction("Rafraichir",ButtonType.ALWAYS,()->refreshTable());
		addSearchField("Rechercher par titre");
	}


	@Override
	protected void drawTable() 
	{
		addColumnDateTime("date","Date").center().width(150);
		addColumn("title","Titre du message");
	}



	@Override
	protected List<SmallTenantNotificationDTO> getLines() 
	{
		try
		{
			return new TenantNotificationService().getAllNotifications();
		} 
		catch (ServiceNotAvailableException e)
		{
			popupServiceNonDisponible().open();
			return new ArrayList<>();
		}
	}


	@Override
	protected String[] getSortInfos() 
	{
		return new String[] { "date" };
	}
	
	protected boolean[] getSortAsc()
	{
		return new boolean[] { false };
	}
	
	protected String[] getSearchInfos()
	{
		return new String[] { "title" };
	}
	

	private CorePopup handleVisualiser()
	{
		SmallTenantNotificationDTO dto = getSelectedLine();
		try
		{
			return new TenantNotificationPopup(dto.id);
		} 
		catch (ServiceNotAvailableException e)
		{
			return popupServiceNonDisponible();
		} 			
	}
	
	
	private CorePopup popupServiceNonDisponible()
	{
		return new MessagePopup("Service non disponible",ContentMode.HTML,ColorStyle.RED,"La lecture des messages n'est pas disponible pour le moment.<br/><br/>Merci de ré essayer plus tard.");
	}
}
