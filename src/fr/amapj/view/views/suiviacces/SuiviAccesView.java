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
 package fr.amapj.view.views.suiviacces;

import java.util.List;

import fr.amapj.service.services.suiviacces.ConnectedUserDTO;
import fr.amapj.service.services.suiviacces.SuiviAccesService;
import fr.amapj.view.engine.listpart.ButtonType;
import fr.amapj.view.engine.listpart.StandardListPart;
import fr.amapj.view.engine.popup.formpopup.FormPopup;



/**
 * Page permettant de presenter la liste des utilisateurs
 * 
 *  
 *
 */
public class SuiviAccesView extends StandardListPart<ConnectedUserDTO>
{
	
	public SuiviAccesView()
	{
		super(ConnectedUserDTO.class,false);
	}
	
	
	@Override
	protected String getTitle() 
	{
		return "Liste des personnes connectées";
	}


	@Override
	protected void drawButton() 
	{
		addButton("Envoyer un message à tous",ButtonType.ALWAYS,e->new PopupSaisieMessage());
		addButtonAction("Rafraichir",ButtonType.ALWAYS,()->refreshTable());	
		
		addSearchField("Rechercher par le nom ou prénom");
	}


	@Override
	protected void drawTable() 
	{
		addColumn("nom","Nom");
		addColumn("prenom","Prénom");
		addColumn("email","E mail");
		addColumnDateTime("date","Date connexion");
		addColumn("agent","Browser");
		addColumn("dbName","Nom de la base");
	}



	@Override
	protected List<ConnectedUserDTO> getLines() 
	{
		return new SuiviAccesService().getConnectedUser();
	}


	@Override
	protected String[] getSortInfos() 
	{
		return new String[] { "date" };
	}
	
	@Override
	protected boolean[] getSortAsc()
	{
		return new boolean[] { false };
	}
	
	protected String[] getSearchInfos()
	{
		return new String[] { "nom" , "prenom" , "dbName"};
	}
	
}
