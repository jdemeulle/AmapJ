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
 package fr.amapj.view.engine.template;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.common.StringUtils;
import fr.amapj.view.engine.ui.ValoMenuLayout;


/**
 * Page de base front office
 */
@SuppressWarnings("serial")
abstract public class FrontOfficeView extends VerticalLayout implements View
{
	abstract public void enter();
	
	abstract public String getMainStyleName();
	
	/**
	 * 
	 */
	@Override
	final public void enter(ViewChangeEvent event)
	{
		//
		ValoMenuLayout.setFrontOffice();
		
		enter();
		
		endOfRefresh();
		
		
		setMargin(false);
		setSpacing(true);
		setSizeUndefined();
		addStyleName("block-center");
		addStyleName(getMainStyleName());
		setResponsive(true);
	}
	
	/**
	 * Cette méthode doit être appelée à la fin du refresh de l'écran, pour garantir
	 * le bon alignement 
	 */
	public void endOfRefresh()
	{
		// Ceci permet de garantir que l'on sera bien centré et à 100% sur la largeur 960 px
		// Je ne comprends pas pourquoi il y a besoin de ca! 
		Label l =new Label();
		l.setSizeUndefined();
		addComponent(l);
	}
	
	

	/**
	 * Permet d'escaper les caracteres HTML, et remplace les retour chariot par des <br/>  
	 */
	static public String s(String value)
	{
		return StringUtils.s(value);
	}
	

}
