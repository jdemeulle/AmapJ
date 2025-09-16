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
 package fr.amapj.view.views.stock;

import java.util.Date;
import java.util.List;

import fr.amapj.service.services.stockservice.saisie.SaisieStockDTO;
import fr.amapj.service.services.stockservice.saisie.SaisieStockService;
import fr.amapj.service.services.stockservice.verifstock.ProduitEnStockDTO;
import fr.amapj.view.engine.grid.integergrid.IntegerGridCell;
import fr.amapj.view.engine.grid.integergrid.PopupIntegerGrid;
import fr.amapj.view.engine.grid.integergrid.lignecumul.TypLigneCumul;
import fr.amapj.view.engine.tools.BaseUiTools;

/**
 * Popup pour la saisie des quantites en stock sur N dates
 *  
 */
public class PopupSaisieQteStockMultiDate extends PopupIntegerGrid
{	
	private SaisieStockDTO saisieStockDTO;
	
	private List<ProduitEnStockDTO> prods;

	private List<Date> dates;
	
	/**
	 * 
	 *  
	 */
	public PopupSaisieQteStockMultiDate(SaisieStockDTO saisieStockDTO)
	{
		super();
		this.saisieStockDTO = saisieStockDTO;
		this.prods = saisieStockDTO.verifStockDTO.verifStockDateDTO.prodStocks;
		this.dates = saisieStockDTO.verifStockDTO.dates;
		
		
		//
		popupTitle = "Saisie des quantités limites";
		
		// 
		param.readOnly = false;
		param.libButtonSave = "Sauvegarder";
		param.messageSpecifique = SaisieQteStock.computeHeader(saisieStockDTO);
		
	}
	
	
	
	public void loadParam()
	{		
		//
		param.nbLig = dates.size();
		param.nbCol = prods.size();
		param.cell = extractCells();
		
		//
		param.allowedEmpty = true;
		param.ligneCumulParam.ligneCumul = TypLigneCumul.NO;
		
		// Largeur des colonnes
		param.largeurCol = 110;
		
		
		PopupVisualiserQteStock.buildHeaders(prods, dates, param);
	}

	
	
	
	private IntegerGridCell[][] extractCells() 
	{
		IntegerGridCell[][] res = new IntegerGridCell[dates.size()][prods.size()];
		
		for (int i = 0; i < dates.size(); i++)
		{
			for (int j = 0; j < prods.size(); j++)
			{			
				ProduitEnStockDTO prod = prods.get(j);
				
				IntegerGridCell c = new IntegerGridCell();
				c.qte = prods.get(j).qteDispo[i];
															
				// Si la case est exclue, alors elle est grisée, sauf si une quantité a été saisie à un moment suite à une erreur
				if (prod.hasConsommateurCommandable[i]==false && c.qte==0)
				{
					c.isStaticText = true;
					c.staticText = "XXXXXX";
				}
				else
				{
					c.isStaticText = false;
				}
				res[i][j] = c;
			}
		}	
		
		return res;
	}


	@Override
	public boolean performSauvegarder()
	{
		// On recopie les données dans le saisieStockDTO
		for (int i = 0; i < param.nbLig; i++)
		{
			for (int j = 0; j < param.nbCol; j++)
			{
				int qte = param.cell[i][j].qte;
				saisieStockDTO.qteDispoStock.updateQteMaxiStockDifferentDate(dates.get(i), prods.get(j).idProduit, qte);
			}
		}
		new SaisieStockService().saveStockInfo(saisieStockDTO);
		return true;
	}



	@Override
	public int getLineHeight(boolean readOnly)
	{
		// Une ligne fait 32 en mode edition , sinon 26
		return readOnly ? 26 : 32;
	}
	
	@Override
	public int getHeaderHeight()
	{
		// On cacule la place consommée par les headers, boutons, ...
		// 365 : nombre de pixel mesurée pour les haeders, les boutons, ... en mode normal, 270 en mode compact
		return BaseUiTools.isCompactMode() ? 270 : 365;

	}
	
}
