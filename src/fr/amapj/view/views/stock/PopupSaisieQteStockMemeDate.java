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

import java.util.List;

import fr.amapj.common.FormatUtils;
import fr.amapj.model.models.produitextended.qtedispostock.QteDispoStock;
import fr.amapj.service.services.stockservice.saisie.SaisieStockDTO;
import fr.amapj.service.services.stockservice.saisie.SaisieStockService;
import fr.amapj.service.services.stockservice.verifstock.ProduitEnStockDTO;
import fr.amapj.view.engine.grid.GridHeaderLine;
import fr.amapj.view.engine.grid.GridSizeCalculator;
import fr.amapj.view.engine.grid.integergrid.IntegerGridCell;
import fr.amapj.view.engine.grid.integergrid.IntegerGridLine;
import fr.amapj.view.engine.grid.integergrid.PopupIntegerGrid;
import fr.amapj.view.engine.grid.integergrid.lignecumul.TypLigneCumul;
import fr.amapj.view.engine.tools.BaseUiTools;

/**
 * Popup pour la saisie des quantites en stock, avec 
 * le stock egal pour chaque date 
 *  
 */
public class PopupSaisieQteStockMemeDate extends PopupIntegerGrid
{	
	
	// Largeur de la colonne description des produits 
	private int largeurColonne = 500;

	private QteDispoStock qteDispoStock; 
	
	private List<ProduitEnStockDTO> prods;

	private SaisieStockDTO saisieStockDTO;
	
	/**
	 * 
	 */
	public PopupSaisieQteStockMemeDate(SaisieStockDTO saisieStockDTO)
	{
		super();
		this.saisieStockDTO = saisieStockDTO;
		this.qteDispoStock = saisieStockDTO.qteDispoStock;
		this.prods = saisieStockDTO.verifStockDTO.verifStockDateDTO.prodStocks;
		
		//
		popupTitle = "Saisie des quantités limites";
		
		// 
		param.readOnly = false;
		param.allowedEmpty = true;
		param.libButtonSave = "Sauvegarder";
		param.messageSpecifique = SaisieQteStock.computeHeader(saisieStockDTO);
		
	}
	
	public void loadParam()
	{
		//
		param.nbLig = prods.size();
		param.nbCol = 1;
		computeCells();
		
		//
		param.buttonCopyFirstLine = false;
		param.ligneCumulParam.ligneCumul = TypLigneCumul.NO;
		

		// Largeur des colonnes
		param.largeurCol = 110;
				
		// Construction du header 1
		GridHeaderLine line1  =new GridHeaderLine();
		line1.addCell("Produit");
		line1.addCell("Qte");
		
		param.headerLines.add(line1);
				
		// Partie gauche de chaque ligne
		param.leftPartLineLargeur = largeurColonne; 
		param.leftPartLineStyle = "description-panier";
		for (ProduitEnStockDTO prod : prods)
		{
			IntegerGridLine line = new IntegerGridLine();
			line.isVisible = true;
			line.leftPart = getText(prod);
			
			param.lines.add(line);
		}	
		
	
	}


	private void computeCells()
	{
		param.cell = new IntegerGridCell [prods.size()][1];
		
		for (int j = 0; j < prods.size(); j++)
		{
			IntegerGridCell c= new IntegerGridCell();
			
			c.isStaticText = false;
			c.qte = prods.get(j).qteDispo[0]; // On prend sur la première date (même stock sur chaque date) 
			c.prix = 0; 
			
			param.cell[j][0] = c;
		}
	}
	


	private String getText(ProduitEnStockDTO prod)
	{
		String str = getLine1(prod)+"<br/>";
		str = str+"Prix unitaire : "+FormatUtils.prix(prod.prix);
		return str;
	}
	
	

	private String getLine1(ProduitEnStockDTO prod)
	{
		return prod.nomProduit+","+prod.condtionnementProduit;
	}

	@Override
	public boolean performSauvegarder()
	{
		updateFromSaisie();
	
		new SaisieStockService().saveStockInfo(saisieStockDTO);
		
		return true;
	}
	
	/**
	 * Copie depuis la grille de saisie vers le modele
	 */
	private void updateFromSaisie() 
	{
		// On lit dans la grille de saisie
		for (int j = 0; j < prods.size(); j++)
		{
			// On lit la quantité saisie
			int qteSaisie = param.cell[j][0].qte;
			
			//
			qteDispoStock.updateQteMaxiStockIdentiqueDate(prods.get(j).idProduit,qteSaisie);
		}
	}

	/**
	 * Une ligne de la table contient dans le cas standard 2 lignes de texte
	 * Exemple : 
	 * 	 Abonnement pour 1 Pain de blé
	 * 	 Prix unitaire : 7.00 €
	 * 
	 *  Une ligne de texte fait 16 de hauteur, et il y a 10 de marges en haut et bas
	 *  Le mode readOnly n'a pas d'impact 
	 *  
	 *  Par contre, la première ligne ("Abonnement pour 1 Pain de blé" dans l'exemple) peut faire 
	 *  plus de 50 pixels, il faut donc compter le nombre de ligne max que peut prendre cette 
	 *  premiere ligne
	 *  
	 */
	@Override
	public int getLineHeight(boolean readOnly)
	{
		int nbLineMax = getNbLineMax();
		return (nbLineMax+1)*16+10+10;
	}
	
	/**
	 * Retourne le nombre de ligne max sur toutes les cellules pour la ligne 1 
	 */
	private int getNbLineMax()
	{
		int nbLine = 1;
		GridSizeCalculator cal = new GridSizeCalculator();
		
		for (ProduitEnStockDTO col : prods)
		{
			String cell = getLine1(col);		
			nbLine = Math.max(nbLine, cal.getHeight(cell,  largeurColonne-22, "Arial",16));
		}
		return nbLine;
	}

	@Override
	public int getHeaderHeight()
	{
		// On cacule la place consommée par les headers, boutons, ...
		// 284 : nombre de pixel mesurée pour les haeders, les boutons, ... en mode normal, 185 en mode compact
		return BaseUiTools.isCompactMode() ? 185 : 284;
	}
	

	
	
}
