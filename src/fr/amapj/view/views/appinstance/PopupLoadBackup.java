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
 package fr.amapj.view.views.appinstance;

import com.vaadin.ui.Upload;

import fr.amapj.common.DateUtils;
import fr.amapj.model.models.saas.StateOnStart;
import fr.amapj.service.services.appinstance.AppInstanceDTO;
import fr.amapj.service.services.appinstance.AppInstanceService;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;

/**
 * Permet de charger un backup 
 * 
 *
 */
public class PopupLoadBackup extends WizardFormPopup
{

	private BackupImporter backupImporter;

	/**
	 * 
	 */
	public PopupLoadBackup()
	{
		
		popupTitle = "Charger une sauvegarde";
		saveButtonTitle = "OK";		
	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldGeneral());
		add(()->addSave());
	}


	private void addFieldGeneral()
	{
		
		// Titre
		setStepTitle("informations");
		
		// Champ 1
		
		String str = "Cet outil permet de charger une sauvegarde d'une base<br/><br/>";
		addHtml(str);
		
		backupImporter = new BackupImporter();
		
		Upload upload = new Upload(null, backupImporter);
		upload.setImmediate(true);
		upload.setButtonCaption("Charger la sauvegarde de la base");
		
		form.addComponent(upload);
	}


	private void addSave()
	{
		
		AppInstanceDTO dto = new AppInstanceDTO();
		
		dto.nomInstance = backupImporter.getNomInstance();
		dto.dateCreation = DateUtils.getDate();
		dto.stateOnStart = StateOnStart.ON_START_BE_ON;
		
		
		new AppInstanceService().restoreFromBackup(dto,backupImporter);
		
		// Titre
		setStepTitle("visualisation du résultat : ");
		
		addHtml("OK");
	}
	
	

	@Override
	protected void performSauvegarder() throws OnSaveException
	{
		// Do nothing
	}

}
