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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import fr.amapj.common.CollectionUtils;
import fr.amapj.model.models.contrat.modele.StockIdentiqueDate;
import fr.amapj.model.models.contrat.modele.StockMultiContrat;
import fr.amapj.service.services.stockservice.saisie.SaisieStockDTO;
import fr.amapj.service.services.stockservice.saisie.SaisieStockService;
import fr.amapj.service.services.stockservice.verifstock.ProduitEnStockDTO;
import fr.amapj.view.engine.grid.GridHeaderLine;
import fr.amapj.view.engine.grid.GridSizeCalculator;
import fr.amapj.view.engine.grid.GridHeaderLine.GridHeaderLineStyle;
import fr.amapj.view.engine.grid.integergrid.IntegerGridCell;
import fr.amapj.view.engine.grid.integergrid.IntegerGridLine;
import fr.amapj.view.engine.grid.integergrid.IntegerGridParam;
import fr.amapj.view.engine.grid.integergrid.PopupIntegerGrid;
import fr.amapj.view.engine.grid.integergrid.lignecumul.TypLigneCumul;
import fr.amapj.view.engine.tools.BaseUiTools;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;

/**
 * Popup pour la visualisation des quantites en stock sur N dates
 * et comparaison au stock commandé 
 *  
 */
public class PopupVisualiserQteStock extends PopupIntegerGrid
{	
	private SaisieStockDTO saisieStockDTO;
	
	private List<ProduitEnStockDTO> prods;

	private List<Date> dates;
	
	/**
	 * 
	 *  
	 */
	public PopupVisualiserQteStock(SaisieStockDTO saisieStockDTO)
	{
		super();
		this.saisieStockDTO = saisieStockDTO;
		this.prods = saisieStockDTO.verifStockDTO.verifStockDateDTO.prodStocks;
		this.dates = saisieStockDTO.verifStockDTO.dates;
		
		
		//
		popupTitle = "Visualisation des quantités limites";
		
		// 
		param.readOnly = true;
		param.libButtonSave = "OK";
		param.messageSpecifique = SaisieQteStock.computeHeader(saisieStockDTO); 
		param.messageSpecifiqueBottom = "Légende : A / B&nbsp;&nbsp;&nbsp;&nbsp;A : quantité commandée&nbsp;&nbsp;&nbsp;&nbsp;B : quantité totale disponible";
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
		
		buildHeaders(prods,dates,param);
		
				
		
	}

	
	
	
	static public void buildHeaders(List<ProduitEnStockDTO> prods,List<Date> dates,IntegerGridParam param) 
	{
		// Construction du header 1
		GridHeaderLine line1  =new GridHeaderLine();
		line1.addCell("Produit");
				
		for (ProduitEnStockDTO prod : prods)
		{
			line1.addCell(prod.nomProduit);
		}
		GridSizeCalculator.autoSize(line1,param.largeurCol,"Arial",16);
		
	
		// Construction du header 2
		GridHeaderLine line2  =new GridHeaderLine();
		line2.style = GridHeaderLineStyle.PRIX;
		line2.addCell("prix unitaire");
				
		for (ProduitEnStockDTO prod : prods)
		{
			line2.addCell(new CurrencyTextFieldConverter().convertToString(prod.prix));
		}

		// Construction du header 3
		GridHeaderLine line3  =new GridHeaderLine();
		line3.addCell("Dates");
				
		for (ProduitEnStockDTO prod : prods)
		{
			line3.addCell(prod.condtionnementProduit);
		}
		GridSizeCalculator.autoSize(line3,param.largeurCol,"Arial",16);
		
		param.headerLines.add(line1);
		param.headerLines.add(line2);
		param.headerLines.add(line3);
		
		
		// Partie gauche de chaque ligne
		param.leftPartLineLargeur = 110;
		param.leftPartLineStyle = "date-saisie";
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		for (int i = 0; i < param.nbLig; i++)
		{
			Date d = dates.get(i);
			
			IntegerGridLine line = new IntegerGridLine();
			
			line.leftPart = df.format(d);
			line.isVisible = true;
			param.lines.add(line);
		}	
	}



	private IntegerGridCell[][] extractCells() 
	{
		DecimalFormat df = new DecimalFormat("0.##");
		
		IntegerGridCell[][] res = new IntegerGridCell[dates.size()][prods.size()];
		
		for (int i = 0; i < dates.size(); i++)
		{
			for (int j = 0; j < prods.size(); j++)
			{			
				ProduitEnStockDTO prod = prods.get(j);
				
				IntegerGridCell c = new IntegerGridCell();
				c.qte = prods.get(j).qteDispo[i];
															
				c.isStaticText = true;
				if (prod.hasConsommateurCommandable[i]==false && c.qte==0)
				{
					c.staticText = "XXXXXX";
				}
				else
				{
					c.staticText = df.format(prod.getQteConsomme(i))+" / "+prod.qteDispo[i];
							
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
