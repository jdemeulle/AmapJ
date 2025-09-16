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

import org.vaadin.openesignforms.ckeditor.CKEditorTextField;

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;

import fr.amapj.view.engine.popup.formpopup.fieldlink.FieldLink;

/**
 * Un fieldlinkpart standard, contenu dans un AbstractField
 */
public class StdFieldLinkPart<T> implements FieldLinkPart<T>
{
	private AbstractField<T> field;
	
	private T defaultValueOnActivate;
	
	private T defaultValueOnDesactivate;
	
	// Peut être null 
	// Correspond au cas ou cette part est un combo box qui est lui meme un LinkField
	private FieldLink fieldLink;
	
	private boolean doInvisible;
	

	public StdFieldLinkPart(AbstractField<T> field, T defaultValueOnActivate, T defaultValueOnDesactivate,FieldLink fieldLink, boolean doInvisible)
	{
		super();
		this.field = field;
		this.defaultValueOnActivate = defaultValueOnActivate;
		this.defaultValueOnDesactivate = defaultValueOnDesactivate;
		this.fieldLink = fieldLink;
		this.doInvisible = doInvisible;
		
		// Tres important : sinon la desactivation des IntegerTextField ne fonctionne pas
		if (field instanceof TextField && defaultValueOnActivate==null)
		{

			defaultValueOnActivate = (T) "";
		}
		if (field instanceof TextField && defaultValueOnDesactivate==null)
		{
			defaultValueOnDesactivate = (T) "";
		}
	}

	public void setDefaultValueOnDesactivate()
	{
		field.setValue(defaultValueOnDesactivate);
	}
	
	public void setDefaultValueOnActivate()
	{
		field.setValue(defaultValueOnActivate);
	}
	
	/**
	 * Methode utilitaire pour activer / desactiver un champ
	 */
	public void enableField(boolean enabled)
	{
		if (doInvisible)
		{
			// Cas des box et des dates qui sont imbriqués dans des HorinontalLayout 
			if ( (field.getParent() instanceof FormLayout)==false)
			{
				field.getParent().setVisible(enabled);
			}
			else
			{
				field.setVisible(enabled);	
			}
		}
		else
		{
			// Activation - desactivation 
			if (field instanceof CKEditorTextField)
			{
				// Attention : il y a un bug dans le wrapper ckeditor, le setEnabled ne fonctionne pas
				( (CKEditorTextField)field).setViewWithoutEditor(!enabled);
			}
			else
			{
				field.setEnabled(enabled);
			}
		}
	}

	@Override
	public FieldLink getFieldLink()
	{
		return fieldLink;
	}
}
	

