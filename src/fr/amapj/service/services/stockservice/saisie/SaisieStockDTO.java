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
 package fr.amapj.service.services.stockservice.saisie;

import java.util.List;

import fr.amapj.model.models.contrat.modele.StockGestion;
import fr.amapj.model.models.contrat.modele.StockIdentiqueDate;
import fr.amapj.model.models.contrat.modele.StockMultiContrat;
import fr.amapj.model.models.produitextended.qtedispostock.QteDispoStock;
import fr.amapj.service.services.stockservice.verifstock.VerifStockDTO;


/**
 * Permet la saisie du stock par le referent / le producteur 
 */
public class SaisieStockDTO 
{
	//
	public QteDispoStock qteDispoStock;
	
	public VerifStockDTO verifStockDTO;
		
	//
	public Long modeleContratId;
	
	public List<String> nomModeleContrats;;
	
	public StockGestion modeleContratStockGestion;
	
	public StockIdentiqueDate modeleContratStockIdentiqueDate;
	
	public StockMultiContrat modeleContratStockMultiContrat;

}
