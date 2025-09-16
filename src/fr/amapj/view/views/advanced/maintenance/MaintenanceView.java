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
 package fr.amapj.view.views.advanced.maintenance;

import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;

import fr.amapj.common.DateUtils;
import fr.amapj.service.engine.deamons.DeamonsContext;
import fr.amapj.service.services.advanced.maintenance.MaintenanceService;
import fr.amapj.service.services.backupdb.BackupDatabaseService;
import fr.amapj.service.services.mailer.MailerCounter;
import fr.amapj.view.engine.template.BackOfficeLongView;
import fr.amapj.view.engine.ui.AppConfiguration;

/**
 * Outil de maintenance basique accessible aux adminitrateurs classiques
 * 
 *
 */
public class MaintenanceView extends BackOfficeLongView implements View
{

	private final static Logger logger = LogManager.getLogger();
	
	@Override
	public String getMainStyleName()
	{
		return "maintenance";
	}
	
	Label labelDateHeure;
	TextField textDateHeure;
	

	@Override
	public void enterIn(ViewChangeEvent event)
	{
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		

		addLabel(this, "Maintenance du système","titre");
	
		
		// Partie date 
		addLabel(this, "Date et heure courante :"+df.format(DateUtils.getDate()));
		addLabel(this, "Version de l'application : "+new MaintenanceService().getVersion());
		addLabel(this, "Type de serveur mail  : "+AppConfiguration.getConf().getMailConfig());
		addLabel(this, "Nombre d'emails envoyés aujourd'hui : "+MailerCounter.getNbMails());		
		
		
		Panel backupPanel = new Panel("Sauvegarde de la base et envoi par e mail");
		backupPanel.addStyleName("action");
		backupPanel.setContent(getBackupPanel());
		
		
		addComponent(backupPanel);
		addEmptyLine(this);

	}
	

	private Component getBackupPanel()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		
		addEmptyLine(layout);
		addLabel(layout, "Cet outil vous permet de faire une sauvegarde de la base et de l'envoyer par mail à l'adresse paramétrée dans les paramètres généraux.");
		addLabel(layout, "Cet outil peut être utilisé avant de faire des modifications importantes sur la base.");
		addLabel(layout, "Cet outil permet aussi de vérifier que les sauvegardes fonctionnent bien.");
		

		addEmptyLine(layout);
		
		Button b1 = new Button("Backup de la base et envoi par mail",e->new BackupDatabaseService().backupDatabase(new DeamonsContext(),"Sauvegarde manuelle"));
		
		layout.addComponent(b1);
				
		addEmptyLine(layout);
		
		return layout;
	}
	
	
	private Label addLabel(VerticalLayout layout, String str,String stylename)
	{
		Label tf = new Label(str);
		if (stylename!=null)
		{
			tf.addStyleName(stylename);
		}
		layout.addComponent(tf);
		return tf;
	}
	
	private Label addLabel(VerticalLayout layout, String str)
	{
		return addLabel(layout, str, null);
	}
	
	
	private Label addEmptyLine(VerticalLayout layout)
	{
		Label tf = new Label("<br/>",ContentMode.HTML);
		tf.addStyleName(ChameleonTheme.LABEL_BIG);
		layout.addComponent(tf);
		return tf;

	}
	
	
}
