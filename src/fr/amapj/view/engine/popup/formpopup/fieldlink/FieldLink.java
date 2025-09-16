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
 package fr.amapj.view.engine.popup.formpopup.fieldlink;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.ComboBox;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.view.engine.popup.formpopup.ValidatorManager;
import fr.amapj.view.engine.popup.formpopup.fieldlink.part.ComponentFieldLinkPart;
import fr.amapj.view.engine.popup.formpopup.fieldlink.part.FieldLinkPart;
import fr.amapj.view.engine.popup.formpopup.fieldlink.part.StdFieldLinkPart;
import fr.amapj.view.engine.popup.formpopup.validator.IValidator;
import fr.amapj.view.engine.popup.formpopup.validator.NotNullValidatorConditionnalFieldLink;

/**
 * Permet de créer un lien entre une combox box et l'activation / desactivation
 * d'un certain nombre d'élements liés
 */
public class FieldLink
{
	// Combo maitre 
	private ComboBox box;
	
	// Liste des éléments liés
	private List<FieldLinkPart<?>> fields = new ArrayList<>(); 
	
	// Validateur qui pourra être appliqué sur les searchers si besoin 
	private NotNullValidatorConditionnalFieldLink notNull;
	
	// Liste des valeurs activant les elements
	private List<Enum<?>> actives = new ArrayList<>();

	private ValidatorManager validatorManager;
	
	// Etat courant 
	private boolean currentState;
	
	private FieldLink parent;
	
	//
	private boolean doInvisible;
	
	
	public FieldLink(ValidatorManager validatorManager,List<Enum<?>> actives,ComboBox box)
	{
		this(validatorManager, actives, box, false);
	}
	
	public FieldLink(ValidatorManager validatorManager,List<Enum<?>> actives,ComboBox box,boolean doInvisible)
	{
		this.validatorManager = validatorManager;
		this.actives = actives;
		this.box = box;
		this.doInvisible = doInvisible;
		
		notNull = new NotNullValidatorConditionnalFieldLink();
		notNull.checkIf(this);
		box.addValueChangeListener(e->valueChanged());
	}
	
	
	public IValidator getValidator()
	{
		return notNull;
	}
	
	public void doLink()
	{
		//
		if (parent!=null)
		{
			throw new AmapjRuntimeException("Cette méthode doit etre appelée uniquement sur la racine");
		}
		//
		initialize();
		
	}
	
	private void initialize()
	{
		currentState = isActif();
		
		for (FieldLinkPart<?> fd : fields)
		{
			fd.enableField(currentState);

			if (currentState==false)
			{
				fd.setDefaultValueOnDesactivate();
			}
					
			// Si c'est un fieldLink lié, on l'initialize aussi 
			if (fd.getFieldLink()!=null)
			{
				fd.getFieldLink().initialize();
			}
		}
	}
	

	public boolean isActif()
	{
		if (parent!=null && parent.currentState==false)
		{
			return false;
		}
		
		Enum en = (Enum) box.getValue();
		return actives.contains(en);

	}

	
	
	public <T> void addField(AbstractField<T> f)
	{
		addField(f,null,null);
	}

	
	public <T> void  addField(AbstractField<T> f , T defaultValueOnActivate,T defaultValueOnDesactivate)
	{
		addField(f, defaultValueOnActivate, defaultValueOnDesactivate,null);
	}
	
	public <T> void  addField(FieldLink fieldLink , T defaultValueOnActivate,T defaultValueOnDesactivate)
	{
		fieldLink.parent = this;
		addField(fieldLink.box, defaultValueOnActivate, defaultValueOnDesactivate,fieldLink);
	}
	
	private <T> void  addField(AbstractField<T> field , T defaultValueOnActivate,T defaultValueOnDesactivate,FieldLink fieldLink)
	{	
		StdFieldLinkPart<T> fd = new StdFieldLinkPart<>(field, defaultValueOnActivate, defaultValueOnDesactivate, fieldLink, doInvisible);
		fields.add(fd);
	}
	
	
	
	public void addComponent(AbstractComponent component)
	{
		ComponentFieldLinkPart fd = new ComponentFieldLinkPart<>(component, doInvisible);
		fields.add(fd);
	}

	
	

	private void valueChanged()
	{
		boolean previousState = currentState;
		currentState = isActif();
		boolean changeState = previousState!=currentState;
		
		for (FieldLinkPart<?> fd : fields)
		{
			// Activation - desactivation 
			fd.enableField(currentState);

			// Si il y a un  changement d'état ACTIF - INACTIF 
			if (changeState)
			{	
				if (currentState==false)
				{
					fd.setDefaultValueOnDesactivate();
				}
				else
				{
					fd.setDefaultValueOnActivate();
				}
				
				// Si c'est un fieldLink lié, on lui demande de se remetre à jour
				// C'est necessaire car le setDefaultValueOnDesactivate n'est pas suffisant pour activer le valueChanged()
				if (fd.getFieldLink()!=null)
				{
					fd.getFieldLink().valueChanged();
				}
			}
		}
	}

	public List<Enum<?>> getActives()
	{
		return actives;
	}

	public ComboBox getBox()
	{
		return box;
	}

	public String getComboTitle()
	{
		return validatorManager.getTitle(box);
	}
	
	
	
}
