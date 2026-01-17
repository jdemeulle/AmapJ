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

import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.messagingcenter.miniproxy.core.ServiceNotAvailableException;
import fr.amapj.messagingcenter.miniproxy.model.notification.AttachedDocument;
import fr.amapj.messagingcenter.miniproxy.model.notification.NotificationTools;
import fr.amapj.messagingcenter.miniproxy.model.notification.TenantNotificationDTO;
import fr.amapj.service.services.advanced.tenantnotification.TenantNotificationService;
import fr.amapj.view.engine.popup.corepopup.CorePopup;

/**
 * Popup permettant d'afficher un message 
 *  
 */

public class TenantNotificationPopup extends CorePopup
{
	private TenantNotificationDTO dto;

	public TenantNotificationPopup(Long idNotification) throws ServiceNotAvailableException
	{
		setWidth(70);
		setHeight("80%");
		
		dto = new TenantNotificationService().loadNotification(idNotification);
		popupTitle = "Message";
		
	}

	
	protected void createButtonBar()
	{		
		addButtonBlank();
		addDefaultButton("OK",e->close());
	}
	
	protected void createContent(VerticalLayout contentLayout)
	{
		//
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
		Label l2 = new Label(" "+formatter.format(dto.refDate),ContentMode.TEXT);	
		contentLayout.addComponent(l2);
		
		//
		Label l0 = new Label(dto.title,ContentMode.TEXT);
		l0.setWidth("100%");
		
		Panel p1 = new Panel();
		p1.setContent(l0);
		p1.addStyleName("bandeau-titremail");
		contentLayout.addComponent(p1);
		
		//
		contentLayout.addComponent(new Label("<br/>",ContentMode.HTML));
		
		Label la = new Label(NotificationTools.byteToHtml(dto.content),ContentMode.HTML);	
		contentLayout.addComponent(la);
		
		
		contentLayout.addComponent(new Label("<br/><br/>",ContentMode.HTML));
		
		
		if (dto.documents.size()>0)
		{
			contentLayout.addComponent(new Label("<b>Pièces jointes : <b>",ContentMode.HTML));
		}
		
		for (AttachedDocument attachedDocument : dto.documents) 
		{
			Link link = createLink(attachedDocument);
			contentLayout.addComponent(link);
		}
	}
	
	
	private Link createLink(AttachedDocument ad)
	{
		 StreamResource streamResource = new StreamResource(()->new ByteArrayInputStream(ad.content), ad.name);
		 streamResource.setCacheTime(0);
	
		 Link extractFile = new Link(ad.name,streamResource);
		 extractFile.setIcon(FontAwesome.DOWNLOAD);
		 extractFile.setTargetName("_blank");
	
		 return extractFile;
	}
	
	
}
