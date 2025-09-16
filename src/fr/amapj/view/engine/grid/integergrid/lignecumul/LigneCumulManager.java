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
 package fr.amapj.view.engine.grid.integergrid.lignecumul;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.view.engine.grid.integergrid.IntegerGridLine;
import fr.amapj.view.engine.grid.integergrid.IntegerGridParam;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;

/**
 * Gestion de la ligne prix total dans le bas de IntegerGrid 
 *
 */
public class LigneCumulManager 
{

	private IntegerGridParam param;
	
	private Label prixTotal;
	
	// Indique si la ligne de cumul est présente ou non 
	private boolean isPresent;
	
	// Contient le libellé de la ligne de cumul 
	private String libLigneCumul;
	
	// Contient l'index de la ligne à cumuler, -1 si c'est tout le tableau 
	private int indexLine;
	

	public LigneCumulManager(IntegerGridParam param) 
	{
		this.param = param;
		
		libLigneCumul = param.ligneCumulParam.libCustomLigneCumul!=null ? param.ligneCumulParam.libCustomLigneCumul : "Montant total";
		
		switch (param.ligneCumulParam.ligneCumul) 
		{
		case NO:
			isPresent = false;
			break;
		
		case STD :
			isPresent = true;
			indexLine = -1;
			break;
			
		case FIRST_LINE:
			indexLine = findIndexLine();
			isPresent = indexLine!=-2;
			break;

		default:
			break;
		}
		
		
	}

	/**
	 * Retrouve l'index de la première ligne visible
	 */
	private int findIndexLine() 
	{
		for (int i = 0; i < param.lines.size(); i++) 
		{
			IntegerGridLine line = param.lines.get(i);
			if (line.isVisible)
			{
				return i;
			}
		}
		return -2;
	}

	public void createContent(VerticalLayout mainLayout) 
	{
		if (isPresent)
		{
			HorizontalLayout footer1 = new HorizontalLayout();
	
			Label dateLabel = new Label(libLigneCumul);
			dateLabel.addStyleName("libprix");
			footer1.addComponent(dateLabel);
			
			prixTotal = new Label("");
			displayMontantTotal();
			prixTotal.addStyleName("prix");
			prixTotal.setWidth("100px");
			footer1.addComponent(prixTotal);
			
			mainLayout.addComponent(footer1);
		}	
	}

	
	public void displayMontantTotal()
	{
		if (isPresent)
		{
			int mnt = 0;
			if (indexLine==-1)
			{
				mnt = param.getMontantTotal();
			}
			else
			{
				mnt = param.getMontantOfLine(indexLine);
			}
			prixTotal.setValue(new CurrencyTextFieldConverter().convertToString(mnt));	
		}
	}
}
