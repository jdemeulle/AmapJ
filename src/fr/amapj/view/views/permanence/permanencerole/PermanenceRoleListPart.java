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

 package fr.amapj.view.views.permanence.permanencerole;

import java.util.List;

import fr.amapj.service.services.permanence.role.PermanenceRoleDTO;
import fr.amapj.service.services.permanence.role.PermanenceRoleService;
import fr.amapj.view.engine.listpart.ButtonType;
import fr.amapj.view.engine.listpart.StandardListPart;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.corepopup.CorePopup.ColorStyle;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;
import fr.amapj.view.engine.popup.suppressionpopup.SuppressionPopup;


/**
 * Gestion des roles de permanences
 *
 */
@SuppressWarnings("serial")
public class PermanenceRoleListPart extends StandardListPart<PermanenceRoleDTO>
{

	public PermanenceRoleListPart()
	{
		super(PermanenceRoleDTO.class,false);
	}
	
	
	@Override
	protected String getTitle() 
	{
		return "Liste des rôles de permanence";
	}


	@Override
	protected void drawButton() 
	{
		addButton("Créer un nouveau rôle",ButtonType.ALWAYS,e->new PermanenceRoleEditorPart(true,null));
		addButton("Modifier",ButtonType.EDIT_MODE,e->new PermanenceRoleEditorPart(false,e));
		addButton("Supprimer",ButtonType.EDIT_MODE,e->handleSupprimer());
		
		addSearchField("Rechercher par nom");
	}


	@Override
	protected void drawTable() 
	{
		addColumn("nom","Nom");
	}



	@Override
	protected List<PermanenceRoleDTO> getLines() 
	{
		return new PermanenceRoleService().getAllRoles();
	}


	@Override
	protected String[] getSortInfos() 
	{
		return new String[] { "nom" };
	}
	
	protected String[] getSearchInfos()
	{
		return new String[] { "nom" };
	}

	private CorePopup handleSupprimer()
	{
		PermanenceRoleDTO dto = getSelectedLine();
		
		if (dto.defaultRole==true)
		{
			String str = "Vous ne pouvez pas supprimer ce rôle, car c'est le role par défaut"; 
			return new MessagePopup("Impossible",ColorStyle.RED,str);
		}
				
		String text = "Êtes-vous sûr de vouloir supprimer le rôle "+dto.nom+" ?";
		return new SuppressionPopup(text,dto.id,e->new PermanenceRoleService().delete(e));
		
	}

}

