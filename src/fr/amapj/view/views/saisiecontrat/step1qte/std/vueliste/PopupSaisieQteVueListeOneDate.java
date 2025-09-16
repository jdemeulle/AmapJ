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
 package fr.amapj.view.views.saisiecontrat.step1qte.std.vueliste;

import com.rits.cloning.Cloner;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.FormatUtils;
import fr.amapj.view.engine.grid.integertable.IntegerTableCell;
import fr.amapj.view.engine.grid.integertable.PopupIntegerTable;
import fr.amapj.view.engine.grid.libbutton.LibButtonLine;

/**
 * Popup pour la saisie des quantites sur une seule date, en mode table 
 * Ce popup est appelé depuis PopupSaisieQteVueListe
 *  
 */
public class PopupSaisieQteVueListeOneDate extends PopupIntegerTable
{	
	
	
	private LibButtonLine<VueListeLineData> line;
	
	/**
	 * 
	 *  
	 */
	public PopupSaisieQteVueListeOneDate(LibButtonLine<VueListeLineData> line, boolean readOnly) 
	{
		super();
		this.line = line;
		
		//
		popupTitle = "Votre panier du "+FormatUtils.getStdDate().format(line.data.dateLiv);
		
		// 
		param.readOnly = readOnly;
		param.libButtonSave = "OK";
		
	}

	public void loadParam()
	{		
		if (line.isVisible==false)
		{
			throw new AmapjRuntimeException();
		}
		
		//
		param.nbCell = line.data.cells.length;
		
		//
		param.cell = copyFrom(line.data.cells);

		//
		param.allowedEmpty = true;
		param.libPrixTotal = "Montant pour cette date "; 
		
		
	}


	private IntegerTableCell[] copyFrom(IntegerTableCell[] cells)
	{
		return new Cloner().deepClone(cells);
	}

	@Override
	protected void handleContinuer()
	{
		close();
	}

	@Override
	public boolean performSauvegarder()
	{
		// On recopie les données dans le modele
		for (int j = 0; j < param.nbCell; j++)
		{
			line.data.cells[j].qte = param.cell[j].qte;
		}
		
		return true;
	}	
}
