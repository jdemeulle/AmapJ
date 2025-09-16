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
 package fr.amapj.view.engine.statuspage;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.model.engine.db.DbManager;
import fr.amapj.model.engine.tools.SpecificDbUtils;
import fr.amapj.model.engine.transaction.DataBaseInfo;
import fr.amapj.model.models.param.paramecran.PEExtendedParametres;
import fr.amapj.service.services.advanced.maintenance.MaintenanceService;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.statuspage.StatusPageBackupImporter.Result;

/**
 * Permet de charger un backup 
 * 
 *
 */
public class StatusPagePopupLoadBackup extends CorePopup
{

	private StatusPageBackupImporter backupImporter;
	private VerticalLayout contentLayout;
	private Button cancelButton;
	private Button saveButton;
	private Button continueAnywayButton;
	private Result r;

	/**
	 * 
	 */
	public StatusPagePopupLoadBackup()
	{
		popupTitle = "Charger une sauvegarde";	
		setWidth(70);
	}
	
	protected void createButtonBar()
	{		
		addButtonBlank();
		cancelButton = addButton("Annuler", e->close());
		saveButton = addDefaultButton("OK", e->close());
		saveButton.setVisible(false);
		continueAnywayButton = addButton("Continuer malgré la différence de version", e->doImport());
		continueAnywayButton.setVisible(false);
				
		
	}
	
		@Override
	protected void createContent(VerticalLayout contentLayout) 
	{
		this.contentLayout = contentLayout;
		
		String str = "Cet outil va vous permettre de charger la sauvegarde d'une base<br/>"+
				     "Pour cela, merci de cliquer sur le bouton ci dessous, puis de choisir le fichier que vous avez reçu par mail.<br/>"+
				     "Le nom du fichier est de type nom-de-votre-amap_V044_2025_02_28_05_02_03.tar.gz <br/><br/>";
		addHtml(str);
		
		backupImporter = new StatusPageBackupImporter();
		
		Upload upload = new Upload(null, backupImporter);
		upload.setImmediate(true);
		upload.setButtonCaption("Charger la sauvegarde de la base");
		upload.addSucceededListener(e->handleEndImport());
		
		contentLayout.addComponent(upload);
	}


	private void handleEndImport() 
	{
		contentLayout.removeAllComponents();
		
		r = backupImporter.getResult();
		if (r.errorMessage!=null)
		{
			addHtml("Une erreur est survenue.");
			addText(r.errorMessage);
			return;
		}
		
		if (r.isVersionOK==false)
		{
			addHtml("ATTENTION !!! ");
			addHtml("Le fichier de sauvegarde contient une base de données ayant pour version AMAPJ : "+r.version);
			addHtml("Vous avez installé sur votre PC la version AMAPJ : "+new MaintenanceService().getShortVersion());
			addHtml("Il serait souhaitable d'installer sur votre PC la version d'AMAPJ qui correspond à la version de cette sauvegarde.");
			addHtml("Vous risquez d'avoir des erreurs lors de l'utilisation d'AMAPJ");
			continueAnywayButton.setVisible(true);
			return;
		}
		
		doImport();
	}
	
	private void doImport() 
	{
		contentLayout.removeAllComponents();
		
		DataBaseInfo dataBaseInfo = DbManager.get().findDataBaseFromName("amap1");
	    DbManager.get().replaceDataBase(dataBaseInfo, r.fileProperties, r.fileScript);
	    SpecificDbUtils.executeInMaster(()->writePEstatusPage());
		
		addHtml("L'import a été réalisée avec succès.");
		addText("Détails de la base importée : ");
		addHtml(r.content);
		cancelButton.setVisible(false);
		saveButton.setVisible(true);
		continueAnywayButton.setVisible(false);
	}


	private Object writePEstatusPage() 
	{
		PEExtendedParametres pe = (PEExtendedParametres) new ParametresService().loadParamEcran(MenuList.EXTENDED_PARAMETRES);
		pe.masterDbLibAmap1 = r.content;
		new ParametresService().update(pe);
		return null;
	}

	private void addHtml(String str) 
	{
		contentLayout.addComponent(new Label(str,ContentMode.HTML));	
	}
	
	private void addText(String str) 
	{
		contentLayout.addComponent(new Label(str));	
	}
}

