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

import java.io.IOException;
import java.util.List;


import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.messagingcenter.miniproxy.model.mailer.RemoteMailerMessage;
import fr.amapj.service.services.mailer.MailerStorage;
import fr.amapj.view.engine.popup.formpopup.FormPopup;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;
import fr.amapj.view.engine.tools.table.complex.ComplexTableBuilder;

/**
 * Permet de lister les mails du storage
 * 
 *
 */
public class PopupMailStorage extends FormPopup
{
	private ComplexTableBuilder<RemoteMailerMessage> builder;
	
	/**
	 * 
	 */
	public PopupMailStorage()
	{
		setWidth(80);	
		popupTitle = "Listes des mails envoyés dans le storage";
	}
	
	@Override
	protected void addFields()
	{		
		List<RemoteMailerMessage> messages = MailerStorage.getAllMessages();
		
		
		builder = new ComplexTableBuilder<RemoteMailerMessage>(messages);
		builder.setPageLength(7);
		
		builder.addString("Envoyé à", false, 300, e->e.recipentsTo);
		builder.addString("Titre", false, 300,  e->e.title);
		builder.addButton("Corps du message", 150, e->"Voir", e->openCorpsMessage(e));
		builder.addButton("Liste des pièces jointes", 150, e->getNbPiecesJointes(e), e->openPieceJointe(e));
		
		
		
		addComplexTable(builder);
	}



	private void openPieceJointe(RemoteMailerMessage message)
	{
		PopupAttachementDisplay.open(new PopupAttachementDisplay(message));
	}

	private String getNbPiecesJointes(RemoteMailerMessage e)
	{	
		int count =   e.attachements.size();
		
		if (count==0)
		{
			return "Pas de pièce jointe";
		}
		else
		{
			return "Voir les "+count+" pièces";
		}
	
	}

	private void openCorpsMessage(RemoteMailerMessage e)
	{
		String html = e.htmlContent;
		MessagePopup.open(new MessagePopup("Corps du message",ColorStyle.GREEN,html));
	}


	@Override
	protected void performSauvegarder() throws OnSaveException
	{	
		// Nothing to do 
	}
}
