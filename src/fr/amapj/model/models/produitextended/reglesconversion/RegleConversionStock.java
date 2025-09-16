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

/**
 * Le produit (idProduit) est equivalent à coefficient le produit idProduitConsomme
 */
public class RegleConversionStock
{
	// 
	public Long idProduit;
	
	//
	public ProduitLimiteQuantite limiteQuantite = ProduitLimiteQuantite.SUIVI_STANDARD;
	
	//  
	public double coefficient;
	
	// Le produit consommé (qui est le produit en stock)   
	public Long idProduitEnStock;
		
}
