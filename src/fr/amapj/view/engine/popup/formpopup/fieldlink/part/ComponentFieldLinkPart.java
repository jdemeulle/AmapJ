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
 package fr.amapj.view.engine.popup.formpopup.fieldlink.part;

import com.vaadin.ui.AbstractComponent;

import fr.amapj.view.engine.popup.formpopup.fieldlink.FieldLink;

/**
 * Un fieldlinkpart pouvant contenir un label, un bouton, ...
 */
public class ComponentFieldLinkPart<T> implements FieldLinkPart<T>
{
	private AbstractComponent component;
	
	private boolean doInvisible;
	

	public ComponentFieldLinkPart(AbstractComponent component,boolean doInvisible)
	{
		super();
		this.component = component;
		this.doInvisible = doInvisible;
	}

	public void setDefaultValueOnDesactivate()
	{
		// Do nothing 
	}
	
	public void setDefaultValueOnActivate()
	{
		// Do nothing 
	}
	
	/**
	 * Methode utilitaire pour activer / desactiver un champ
	 */
	public void enableField(boolean enabled)
	{
		if (doInvisible)
		{
			component.setVisible(enabled);	
		}
		else
		{
			component.setEnabled(enabled);
		}
	}

	@Override
	public FieldLink getFieldLink()
	{
		return null;
	}
}
	

