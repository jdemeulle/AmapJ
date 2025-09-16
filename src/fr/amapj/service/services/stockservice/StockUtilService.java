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
 package fr.amapj.service.services.stockservice;

import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.StockGestion;
import fr.amapj.model.models.fichierbase.Producteur;
import fr.amapj.model.models.fichierbase.ProducteurStockGestion;

/**
 * Opérations sur les fichiers de base des stocks 
 *
 */
public class StockUtilService 
{
	
	/**
	 * Indique si ce modele de contrat 
	 * a la gestion de stock activée
	 * 
	 */
	@DbRead
	public boolean hasModeleContratGestionStock(Long idModeleContrat)
	{
		RdbLink em = RdbLink.get();
		ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);
		return mc.stockGestion==StockGestion.OUI;
	}

	
	
	
	/**
	 * Indique si le producteur de ce modele de contrat 
	 * a la gestion de stock activée , à partir d'un modele de contrat
	 * 
	 * Attention : ceci est different de la gestion de stock du modele de contrat 
	 */
	@DbRead
	public boolean hasProducteurGestionStockFromModeleContrat(Long idModeleContrat)
	{
		RdbLink em = RdbLink.get();
		Producteur p = em.find(ModeleContrat.class, idModeleContrat).producteur;
		return p.gestionStock == ProducteurStockGestion.OUI;
	}

	
	/**
	 * Indique si le producteur de ce modele de contrat 
	 * a la gestion de stock activée
	 * 
	 */
	@DbRead
	public boolean hasProducteurGestionStock(Long idProducteur)
	{
		RdbLink em = RdbLink.get();
		Producteur p = em.find(Producteur.class, idProducteur);
		return p.gestionStock == ProducteurStockGestion.OUI;
	}


	

}
