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
 package fr.amapj.view.views.logview;

import java.util.List;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

import fr.amapj.service.services.authentification.PasswordManager;
import fr.amapj.service.services.edgenerator.excel.EGStatAccess;
import fr.amapj.service.services.logview.LogViewService;
import fr.amapj.service.services.logview.StatAccessDTO;
import fr.amapj.view.engine.excelgenerator.TelechargerPopup;
import fr.amapj.view.engine.listpart.ButtonType;
import fr.amapj.view.engine.listpart.StandardListPart;
import fr.amapj.view.engine.popup.corepopup.CorePopup.ColorStyle;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;



/**
 * Page permettant de presenter les statistiques d'acces
 * 
 *  
 *
 */
@SuppressWarnings("serial")
public class StatAccessView extends StandardListPart<StatAccessDTO>
{
	
	
	public StatAccessView()
	{
		super(StatAccessDTO.class,false);
	}
	
	
	@Override
	protected String getTitle() 
	{
		return "Statistiques des accès";
	}


	@Override
	protected void drawButton() 
	{
		addButtonAction("Rafraichir",ButtonType.ALWAYS,()->refreshTable());
		addButton("Télécharger",ButtonType.ALWAYS,e->handleTelecharger());
		addButton("Authentification",ButtonType.ALWAYS,e->handleInfoAuthentification());
	}
	
	@Override
	protected void addExtraComponent()
	{
		addComponent(new Label("Authentification : "+PasswordManager.authentificationCounter.getLastInfo()));
	}


	private MessagePopup handleInfoAuthentification()
	{
		String msg = PasswordManager.authentificationCounter.getAllInfos();
		return new MessagePopup("Info authentification",ContentMode.HTML,ColorStyle.GREEN,msg);
	}


	@Override
	protected void drawTable() 
	{
		addColumnDate("date","Date");
		addColumn("nbAcces","Nb d'accès");
		addColumn("nbVisiteur","Nb de visiteurs différents");
		addColumn("tempsTotal","temps total en minutes");
	}


	@Override
	protected List<StatAccessDTO> getLines() 
	{
		return new LogViewService().getStats();
	}


	@Override
	protected String[] getSortInfos() 
	{
		return new String[] { "nom" , "prenom" };
	}
	
	protected String[] getSearchInfos()
	{
		return new String[] { "nom" , "prenom" };
	}
	
	private TelechargerPopup handleTelecharger()
	{
		TelechargerPopup popup = new TelechargerPopup("Statistiques");
		popup.addGenerator(new EGStatAccess());
		return popup;
	}
}
