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
 package fr.amapj.view.engine.menu;

import com.vaadin.navigator.View;

/**
 * Contient la description d'une entrée menu
 *
 */
public class MenuDescription 
{
	// Nom du menu (element de la liste MenuList)
	private MenuList menuName;
	
	// Nom de la classe implementant la vue liée à ce menu
	private Class<? extends View> viewClass;
	
	private String categorie;
	
	
	
	
	public MenuDescription(MenuList menuName, Class<? extends View> viewClass )
	{
		this.menuName = menuName;
		this.viewClass = viewClass;
		
	}
	
	public MenuDescription(String categorie)
	{
		this.categorie = categorie;
	}
	


	public MenuList getMenuName()
	{
		return menuName;
	}

	public Class<? extends View> getViewClass()
	{
		return viewClass;
	}
	
	
	public String getCategorie()
	{
		return categorie;
	}
	
}
