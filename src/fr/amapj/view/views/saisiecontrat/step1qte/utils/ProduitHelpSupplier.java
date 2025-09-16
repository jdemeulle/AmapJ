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
import fr.amapj.service.services.web.WebPageService;
import fr.amapj.view.engine.grid.GridHeaderLine.HelpSupplier;

public class ProduitHelpSupplier implements HelpSupplier
{
	private ContratColDTO col;

	public ProduitHelpSupplier(ContratColDTO col)
	{
		this.col = col;
		
	}

	@Override
	public boolean hasHelp()
	{
		return col.produitWebPageId!=null;
	}

	@Override
	public String helpText1()
	{
		return col.nomProduit+", "+col.condtionnementProduit;
	}

	@Override
	public String helpText2()
	{
		return new WebPageService().loadWebPage(col.produitWebPageId).content;
				
	}
	
}