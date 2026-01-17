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
 package fr.amapj.model.models.produitextended.qtedispostock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.DateUtils;
import fr.amapj.model.models.contrat.modele.StockIdentiqueDate;

/**
 * Quantité disponible en stock 
 */
public class QteDispoStock
{
	
	public List<QteDispoStockProduit> qteDispoStockProduits;
	
	
	/**
	 * Positionne les paramètres par défaut
	 */
	public void setDefault()
	{
		if (qteDispoStockProduits==null)
		{
			qteDispoStockProduits = new ArrayList<>();
		}
	}
	
	
	public int findQteDispo(StockIdentiqueDate stockIdentiqueDate, Date date, Long idProduit)
	{
		switch (stockIdentiqueDate)
		{
		case OUI: return findQteDispoStockIdentiqueDate(idProduit);
		
		case NON : return findQteDispoStockDifferentDate(date, idProduit);
		
		default: throw new AmapjRuntimeException();
		}

	}
	
	// PARTIE MEME DATE

	private int findQteDispoStockIdentiqueDate(Long produitId)
	{
		QteDispoStockProduit infoProduit = findInfoProduit(null,produitId);
		if (infoProduit==null)
		{
			return 0;
		}
		return infoProduit.qte;
	}
	
	public void updateQteMaxiStockIdentiqueDate(Long produitId, int qteSaisie)
	{
		QteDispoStockProduit infoProduit = findInfoProduit(null,produitId);
		if (infoProduit==null)
		{
			infoProduit = new QteDispoStockProduit();
			infoProduit.idProduit = produitId;
			infoProduit.date = null;
			qteDispoStockProduits.add(infoProduit);
			
					
		}
		infoProduit.qte = qteSaisie;
	}
	
	
	// PARTIE DIFFERENTE DATE

	private int findQteDispoStockDifferentDate(Date date, Long produitId)
	{
		QteDispoStockProduit infoProduit = findInfoProduit(date,produitId);
		if (infoProduit==null)
		{
			return 0;
		}
		return infoProduit.qte;
	}
	
	
	public void updateQteMaxiStockDifferentDate(Date date,Long produitId, int qteSaisie)
	{
		QteDispoStockProduit infoProduit = findInfoProduit(date,produitId);
		
		// Gestion du cas qte = 0
		if (qteSaisie==0)
		{
			if (infoProduit==null)
			{
				// Nothing to do 
				return;
			}
			else
			{
				qteDispoStockProduits.remove(infoProduit);
				return;
			}
		}
		
		// 
		if (infoProduit==null)
		{
			infoProduit = new QteDispoStockProduit();
			infoProduit.idProduit = produitId;
			infoProduit.date = date;
			qteDispoStockProduits.add(infoProduit);
		}
		infoProduit.qte = qteSaisie;
	}

	
	// 

	private QteDispoStockProduit findInfoProduit(Date date,Long produitId)
	{
		for (QteDispoStockProduit stockInfoProduit : qteDispoStockProduits)
		{
			if (stockInfoProduit.idProduit.equals(produitId) && DateUtils.equals(stockInfoProduit.date,date) )
			{
				return stockInfoProduit;
			}
		}
		return null;
	}
	
}
