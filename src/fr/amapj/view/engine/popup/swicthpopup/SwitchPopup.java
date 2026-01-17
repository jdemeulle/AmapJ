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
 package fr.amapj.view.engine.popup.swicthpopup;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.vaadin.data.Container;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.view.engine.popup.corepopup.CorePopup;

/**
 * Permet de créer un popup avec une liste de choix, qui ménera ensuite 
 * à un autre popup 
 */
public class SwitchPopup extends CorePopup
{	
	
	private OptionGroup group;
	
	private int index;
	
	private List<SwitchPopupInfo> infos = new ArrayList<>();
	
	private String line1 = "Veuillez indiquer l'action que vous souhaitez réaliser :";
	
	// Permet de definir un texte additionnel qui sera affiché en haut 
	private String header;
	
	
	static public class SwitchPopupInfo
	{
		public String lib;
		public Supplier<CorePopup> popupSupplier;
		public Runnable runnable;
		
		public SwitchPopupInfo(String lib, Supplier<CorePopup> popupSupplier)
		{
			this.lib = lib;
			this.popupSupplier = popupSupplier;
		}
		
		public SwitchPopupInfo(String lib, Runnable runnable)
		{
			this.lib = lib;
			this.runnable = runnable;
		}		
	}
	
	
	public SwitchPopup(String title,int width)
	{
		popupTitle = title;
		setWidth(width);
	}
	
	public void setLine1(String line1)
	{
		this.line1 = line1;
	}
	
	public void setHeader(String header)
	{
		this.header = header;
	}

	/**
	 * Permet d'ajouter une ligne qui menera à l'ouverture d'un popup 
	 */
	public void addLine(String lib,Supplier<CorePopup> popupSupplier)
	{
		infos.add(new SwitchPopupInfo(s(lib),popupSupplier));
	}

	/**
	 * Permet d'ajouter une ligne qui menera à l'execution d'une action 
	 * Si il faut ouvrir un popup, il est nécessaire d'utiliser addLine 
	 */
	public void addLineAction(String lib,Runnable runnable)
	{
		infos.add(new SwitchPopupInfo(s(lib),runnable));
	}
	
	

	public void addSeparator()
	{
		// On ne fait rien si c'est la première ligne
		if(infos.size()==0)
		{
			return ;
		}
		SwitchPopupInfo info = infos.get(infos.size()-1);
		info.lib = info.lib+"<br/><br/>";
	}


	
	
	protected void createContent(VerticalLayout contentLayout)
	{
		contentLayout.addStyleName("popup-switch");
		
		if (header!=null)
		{
			contentLayout.addComponent(new Label(header,ContentMode.HTML));
		}
		
		group = new OptionGroup(line1);
		group.setHtmlContentAllowed(true);
		for (SwitchPopupInfo info : infos)
		{
			group.addItem(info.lib);
		}
		
		contentLayout.addComponent(group);
	}
	
	
	
	
	

	protected void createButtonBar()
	{
		addButtonBlank();
		addButton("Annuler", e->handleAnnuler());
		addDefaultButton("Continuer ...", e->handleContinuer());
	}
	
	
	protected void handleAnnuler()
	{
		close();
	}

	protected void handleContinuer()
	{
		index = ((Container.Indexed) group.getContainerDataSource()).indexOfId(group.getValue());
		
		if (index==-1)
		{
			close();
			return;
		}
		
		SwitchPopupInfo info = infos.get(index);
		
		// Si la ligne est de type ouverture de popup 
		if (info.popupSupplier!=null)
		{
			changeCloseListener(e->swithToNextPopup(info));
			close();	
		}
		// Si la ligne est de type action 
		else
		{
			info.runnable.run();
			close();	
		}
	}

	protected void swithToNextPopup(SwitchPopupInfo info)
	{
		CorePopup popup = info.popupSupplier.get();	
		popup.changeCloseListener(popupCloseListener);
		popup.open();
	}
}
