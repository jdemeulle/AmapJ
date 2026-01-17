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
 package fr.amapj.view.views.saisiecontrat.step1qte.utils;

import fr.amapj.service.services.mescontrats.ContratColDTO;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.view.engine.grid.integergrid.IntegerGridCell;
import fr.amapj.view.engine.grid.integertable.IntegerTableCell;

public class QteTableUtils 
{

	public IntegerTableCell[] convertOneLineOfCells(ContratDTO dto,IntegerGridCell[][] cs, int i) 
	{
		IntegerTableCell[] cells = new IntegerTableCell[dto.contratColumns.size()];
		
		for (int j = 0; j < cells.length; j++)
		{
			ContratColDTO col = dto.contratColumns.get(j);
			IntegerGridCell igc = cs[i][j];
			
			IntegerTableCell cell = new IntegerTableCell();
			cell.lib1 = col.nomProduit;
			cell.lib2 = col.condtionnementProduit;
			
			cell.prix = igc.prix;
			cell.isStaticText = igc.isStaticText;
			cell.qte = igc.qte;
			cell.staticText = igc.staticText;
			cell.helpSupplier = new ProduitHelpSupplier(col);
			
			cells[j] = cell;
		}
		
		return cells;
	}
}
