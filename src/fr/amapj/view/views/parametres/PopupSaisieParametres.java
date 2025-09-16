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
 package fr.amapj.view.views.parametres;

import com.vaadin.data.util.BeanItem;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.RichTextArea;

import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.session.SessionManager;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.popup.formpopup.validator.EmailValidator;
import fr.amapj.view.engine.ui.AppConfiguration;
import fr.amapj.view.engine.ui.AppConfiguration.MailConfig;

/**
 * 
 */
public class PopupSaisieParametres extends WizardFormPopup
{

	private ParametresDTO dto;
	
	// L'utilisateur est il adminFull ? 
	private boolean adminFull;


	/**
	 * 
	 */
	public PopupSaisieParametres(ParametresDTO dto)
	{
		setWidth(95);
		popupTitle = "Modification des paramètres";

		this.dto = dto;
		setModel(dto);
		
		adminFull = SessionManager.getSessionParameters().isAdminFull();
	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldAmapInfo());
		add(()->addFieldMailInfo());
	}
	
	

	private void addFieldAmapInfo()
	{
		// Titre
		setStepTitle("identification de l'AMAP");
		
		// 
		addTextField("Nom de l'AMAP", "nomAmap");
		
		addTextField("Ville de l'AMAP", "villeAmap");		
	}
	
	private void addFieldMailInfo()
	{
		// Titre
		setStepTitle("information sur l'envoi des mails");
		
		
		MailConfig mailTarget = AppConfiguration.getConf().getMailConfig();
		
		if (mailTarget==MailConfig.NO_MAIL)
		{
			addHtml("L'envoi des mails est desactivé.");
			return ;
		}		
		
		
		if (mailTarget==MailConfig.GMAIL)
		{
			addTextField("Adresse mail qui enverra les messages", "sendingMailUsername");
			addPasswordTextField("Password de l'adresse mail qui enverra les messages", "sendingMailPassword");
		}	
		else
		{
			addTextField("Adresse mail qui enverra les messages", "sendingMailUsername").setEnabled(adminFull);
		}
		
		
		
		addIntegerField("Nombre maximum de mail par jour", "sendingMailNbMax").setEnabled(adminFull);
		
		addTextField("URL de l'application utilisée dans les mails", "url").setEnabled(adminFull);
		
		addTextField("Adresse mail qui sera en copie de tous les mails envoyés par le logiciel", "mailCopyTo");
		
		addTextField("Adresse mail du destinataire des sauvegardes quotidiennes", "backupReceiver",new EmailValidator(false));
		
		RichTextArea f =  addRichTextAeraField("Texte ajouté en bas des mails envoyés", "sendingMailFooter");
		f.setHeight(4, Unit.CM);
		
	}
	

	@Override
	protected void performSauvegarder()
	{
		new ParametresService().update(dto);
	}

}
