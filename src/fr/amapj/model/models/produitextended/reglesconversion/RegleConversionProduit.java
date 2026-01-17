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
 package fr.amapj.model.models.produitextended.reglesconversion;

import java.util.ArrayList;
import java.util.List;


/**
 * Toutes les regles de conversion stocks pour les produits 
 */
public class RegleConversionProduit
{
	
	public List<RegleConversionStock> regleStocks;
	
	
	/**
	 * Positionne les paramètres par défaut
	 */
	public void setDefault()
	{
		if (regleStocks==null)
		{
			regleStocks = new ArrayList<>();
		}
	}


	/**
	 * Recherche la régle
	 * Si elle n'existe pas , elle est créé et ajoutée dans la liste
	 */
	public RegleConversionStock findReglesConversionStock(Long produitId)
	{
		for (RegleConversionStock r : regleStocks)
		{
			if (r.idProduit.equals(produitId))
			{
				return r;
			}
		}
		RegleConversionStock r = new RegleConversionStock();
		r.idProduit = produitId;
	
		regleStocks.add(r);
		
		return r;
	}
	
	

}
