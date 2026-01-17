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
 package fr.amapj.view.views.advanced.devtools;

import java.io.ByteArrayInputStream;
import java.util.List;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Link;

import fr.amapj.messagingcenter.miniproxy.model.mailer.RemoteMailerAttachement;
import fr.amapj.messagingcenter.miniproxy.model.mailer.RemoteMailerMessage;
import fr.amapj.view.engine.popup.formpopup.FormPopup;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;

/**
 * Permet d'afficher les pièces jointes d'un mail
 * 
 *
 */
public class PopupAttachementDisplay extends FormPopup
{

	
	private RemoteMailerMessage message;

	/**
	 * 
	 */
	public PopupAttachementDisplay(RemoteMailerMessage message)
	{
		this.message = message;
		setWidth(80);	
		popupTitle = "Listes des pièces jointes";
	}
	
	@Override
	protected void addFields()
	{		
		int count =   message.attachements.size();
		
		if (count==0)
		{
			addHtml("Pas de pièces jointes");
			return;
		}
		
		
		List<RemoteMailerAttachement> attachements = message.attachements;
		for (int i = 0; i < attachements.size(); i++) 
		{
			RemoteMailerAttachement rma = attachements.get(i);
			Link l = createLink(i, new ByteArrayInputStream(rma.data),rma.name);
			form.addComponent(l);
			}
	}

	public Link createLink(int pieceNumber,ByteArrayInputStream bais,String fileName)
	{
		 StreamResource streamResource = new StreamResource(()->bais, fileName);
		 streamResource.setCacheTime(1000);
	
		 Link extractFile = new Link("Pièce "+pieceNumber+" - "+fileName,streamResource);
		 extractFile.setIcon(FontAwesome.DOWNLOAD);
		 extractFile.setTargetName("_blank");
	
		 return extractFile;
	}



	@Override
	protected void performSauvegarder() throws OnSaveException
	{	
		// Nothing to do 
	}
}
