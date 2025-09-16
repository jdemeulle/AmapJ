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
 package fr.amapj.view.engine.grid;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.view.engine.grid.utils.HelpPopupSupplier;
import fr.amapj.view.engine.popup.corepopup.CorePopup.ColorStyle;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;



/**
 * Description d'une ligne de header
 *
 */
public class GridHeaderLine
{
	//
	public int height = -1;
	
	public GridHeaderLineStyle style = GridHeaderLineStyle.STANDARD;
	
	public List<GridHeaderLineCell> cells = new ArrayList<>();
	
	public void addCell(String content)
	{
		cells.add(new GridHeaderLineCell(content));
	}
	
	public void addCell(String content,HelpSupplier helpSupplier)
	{
		cells.add(new GridHeaderLineCell(content,helpSupplier));
	}
	
	
	static public class GridHeaderLineCell
	{
		public String content;
		
		// Le titre de la fenetre d'aide
		// Le contenu de la fenetre d'aide
		public HelpSupplier helpSupplier;

		public GridHeaderLineCell(String content) 
		{
			this.content = content;
		}

		public GridHeaderLineCell(String content, HelpSupplier helpSupplier) 
		{
			this.content = content;
			this.helpSupplier = helpSupplier;
		}

		public boolean hasHelp() 
		{
			if (helpSupplier==null)
			{
				return false;
			}
			return helpSupplier.hasHelp();
		}
	}

	/**
	 * L aligne peut être soit de type classique (noir sur fond vert)
	 * soit de type prix (rouge sur fond vert) 
	 *
	 */
	static public enum GridHeaderLineStyle
	{
		STANDARD , 
		
		PRIX;
	}
	
	static public interface HelpSupplier
	{
		public boolean hasHelp();
		
		// Titre de la fenetre d'aide 
		public String helpText1();
		
		// Contenu de la fenetre d'aide
		public String helpText2();
	}

	public boolean hasHelp() 
	{
		for (GridHeaderLineCell cell : cells) 
		{
			if (cell.hasHelp())
			{
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Permet la construction de cette ligne de header dans le layout indiqué 
	 */
	public void constructHeaderLine(VerticalLayout mainLayout,int leftPartLineLargeur,int largeurCol)
	{
		//
		boolean hasHelp = hasHelp();
	
		//
		HorizontalLayout header1 = new HorizontalLayout();
		if (height != -1)
		{
			int h = (hasHelp ? 28 : 0)+height;  // 28 correspond à la hauteur du bouton d'aide 
			header1.setHeight(h + "px");
		}

		int index=0;
		for (GridHeaderLineCell cell : cells)
		{
			VerticalLayout vl = new VerticalLayout();
			vl.setMargin(false);
			vl.setSpacing(false);
			vl.addStyleName("gridheaderline");
			vl.setHeight("100%");
			if (index==0)
			{
				vl.setWidth((leftPartLineLargeur+5)+"px");
			}
			else
			{
				vl.setWidth((largeurCol+2)+"px");
			}
			
			Label dateLabel = new Label(cell.content);
			dateLabel.setSizeFull();
			dateLabel.addStyleName("gridheaderline");
			if (style==GridHeaderLineStyle.PRIX)
			{
				dateLabel.addStyleName("gridheaderline-prix");
				
			}
			vl.addComponent(dateLabel);
			vl.setExpandRatio(dateLabel, 1f);
			
			if (cell.hasHelp())
			{
				Button btn = new Button(FontAwesome.QUESTION_CIRCLE);
				btn.addStyleName("gridheaderline");
				btn.addStyleName("borderless-colored");
				btn.addStyleName("question-mark");
				btn.addClickListener(e->HelpPopupSupplier.displayHelpPopup(cell.helpSupplier));
				vl.addComponent(btn);
				vl.setComponentAlignment(btn, Alignment.BOTTOM_CENTER);
			}
			
			
			header1.addComponent(vl);
			
			index++;
		}
		mainLayout.addComponent(header1);
	}
	
}
