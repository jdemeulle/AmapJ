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
 package fr.amapj.view.engine.grid.integergrid;

import fr.amapj.view.engine.grid.GridHeaderLine.HelpSupplier;

public class IntegerGridLine
{
	// Libellé à gauche
	public String leftPart;
	
	// le fournisseur de l'aide
	public HelpSupplier leftPartHelpSupplier;
	
	// Indique si la colonne de gauche dispose d'une aide (accessible par un point d'interrogation)
	public boolean hasLeftPartHelp()
	{
		return leftPartHelpSupplier!=null && leftPartHelpSupplier.hasHelp();
	}
	
	
	// La ligne est elle visible ?   
	public boolean isVisible;
	
	
}
