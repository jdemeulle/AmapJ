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
 package fr.amapj.view.views.saisiecontrat.step1qte.abo;

import java.text.SimpleDateFormat;
import java.util.List;

import fr.amapj.service.services.mescontrats.ContratColDTO;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.mescontrats.ContratLigDTO;
import fr.amapj.service.services.mescontrats.ContratLigDTO.AboLigStatus;
import fr.amapj.view.engine.grid.GridHeaderLine;
import fr.amapj.view.engine.grid.GridHeaderLine.GridHeaderLineStyle;
import fr.amapj.view.engine.grid.GridSizeCalculator;
import fr.amapj.view.engine.grid.integergrid.IntegerGridLine;
import fr.amapj.view.engine.grid.integergrid.PopupIntegerGrid;
import fr.amapj.view.engine.tools.BaseUiTools;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;

/**
 * Popup pour le detail des dates pour les contrats de type abonnement 
 *  
 */
public class PopupDetailDate extends PopupIntegerGrid
{	
	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	
	private ContratDTO contratDTO;
	
	/**
	 * 
	 */
	public PopupDetailDate(ContratDTO contratDTO)
	{
		super();
		this.contratDTO = contratDTO;
		
		//
		popupTitle = "Détails des dates";
	}
	
	
	
	public void loadParam()
	{
		//
		param.readOnly = true;
		param.libButtonSave = "OK";
		
		//
		param.nbLig = contratDTO.contratLigs.size();
		param.nbCol = contratDTO.contratColumns.size();
		param.cell = contratDTO.extractCells();
		List<ContratLigDTO> contratLigs = contratDTO.contratLigs;
		for (int i = 0; i < contratLigs.size(); i++)
		{
			ContratLigDTO lig = contratLigs.get(i);
			String s = null;
			if (lig.status==AboLigStatus.FORCED_TO_0)
			{
				s= "------";
			}
			if (lig.status==AboLigStatus.JOKER)
			{
				s= "JOKER";
			}
			if (lig.status==AboLigStatus.REPORT)
			{
				s= "REPORT";
			}
			if (s!=null)
			{
				for (int j = 0; j < param.nbCol; j++)
				{
					param.cell[i][j].isStaticText = true;
					param.cell[i][j].staticText = s;
				}
			}
		}
		
		// Largeur des colonnes
		param.largeurCol = 110;
				
		// Construction du header 1
		GridHeaderLine line1  =new GridHeaderLine();
		line1.addCell("Produit");
				
		for (ContratColDTO col : contratDTO.contratColumns)
		{
			line1.addCell(col.nomProduit);
		}
		GridSizeCalculator.autoSize(line1,param.largeurCol,"Arial",16);
		
	
		// Construction du header 2
		GridHeaderLine line2  =new GridHeaderLine();
		line2.style = GridHeaderLineStyle.PRIX;
		line2.addCell("prix unitaire");
				
		for (ContratColDTO col : contratDTO.contratColumns)
		{
			line2.addCell(new CurrencyTextFieldConverter().convertToString(col.prix));
		}

		// Construction du header 3
		GridHeaderLine line3  =new GridHeaderLine();
		line3.addCell("Dates");
				
		for (ContratColDTO col : contratDTO.contratColumns)
		{
			line3.addCell(col.condtionnementProduit);
		}
		GridSizeCalculator.autoSize(line3,param.largeurCol,"Arial",16);
		
		param.headerLines.add(line1);
		param.headerLines.add(line2);
		param.headerLines.add(line3);
		
		
		// Partie gauche de chaque ligne
		param.leftPartLineLargeur = 110;
		param.leftPartLineStyle = "date-saisie";
		for (ContratLigDTO lig : contratDTO.contratLigs)
		{
			IntegerGridLine line = new IntegerGridLine();
			line.isVisible = true;
			line.leftPart = df.format(lig.date);
			param.lines.add(line);
		}	
	}
	
	@Override
	public boolean performSauvegarder()
	{
		// Do nothing - ne sera pas appele
		return false;
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
