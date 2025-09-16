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
 package fr.amapj.view.views.saisiecontrat.step1qte.std.vueliste;

import java.text.SimpleDateFormat;
import java.util.List;

import com.vaadin.ui.TextField;

import fr.amapj.common.FormatUtils;
import fr.amapj.model.models.contrat.modele.NatureContrat;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.mescontrats.ContratLigDTO;
import fr.amapj.view.engine.grid.integergrid.IntegerGridCell;
import fr.amapj.view.engine.grid.integergrid.IntegerGridLine;
import fr.amapj.view.engine.grid.integertable.IntegerTableCell;
import fr.amapj.view.engine.grid.libbutton.LibButtonLine;
import fr.amapj.view.engine.grid.libbutton.LibButtonParam;
import fr.amapj.view.engine.grid.libbutton.PopupLibButton;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.ModeSaisie;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.SaisieContratData;
import fr.amapj.view.views.saisiecontrat.step1qte.std.grid.PopupSaisieQteContrat;
import fr.amapj.view.views.saisiecontrat.step1qte.std.vueliste.VueListeLineData.DateCommandable;
import fr.amapj.view.views.saisiecontrat.step1qte.utils.QteTableUtils;
import fr.amapj.view.views.saisiecontrat.step1qte.utils.checkonend.CheckOnEndSaisieQte;

/**
 * Popup pour la saisie des quantites, en vue liste 
 *  
 */
public class PopupSaisieQteVueListe extends PopupLibButton<VueListeLineData>
{	
	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	
	private ContratDTO contratDTO;
	
	private SaisieContratData data;
	
	/**
	 * 
	 *  
	 */
	public PopupSaisieQteVueListe(SaisieContratData data)
	{
		super();
		this.data = data;
		this.contratDTO = data.contratDTO;
		
		
		//
		popupTitle = "Mon contrat "+contratDTO.nom;
		
		// 
		param.readOnly = (data.modeSaisie==ModeSaisie.READ_ONLY);
		param.messageSpecifique = s(data.messageSpecifique);
		param.libButtonSave = "Continuer ...";
		
	}
	
	
	
	public void loadParam()
	{		
		//
		param.nbLig = contratDTO.contratLigs.size();
		param.nbCol = contratDTO.contratColumns.size();

		//
		param.allowedEmpty = false;
		param.libPrixTotal = contratDTO.nature==NatureContrat.CARTE_PREPAYEE ? "Prix des livraisons à venir" : "Prix Total";
		
		// Largeur des colonnes
		param.largeurLib1 = 110;
		param.largeurLib2 = 220;
		
		// Remplissage des lignes
		IntegerGridCell[][] cs = contratDTO.extractCells();
		for (int i = 0; i < param.nbLig; i++)
		{
			LibButtonLine<VueListeLineData> line = constructLibButtonLine(param,i,cs);		
			param.lines.add(line);
		}	
		
		//
		param.switchButtonAction = ()->handleSwitchButton();
		
		//
		param.copyFirstLineAction = ()->handleCopyFirstLine();
		
	}


	/**
	 */
	private void handleCopyFirstLine() 
	{
		int index = getIndexFirstLineCommandable();
		if (index==-1)
		{
			return;
		}
		
		LibButtonLine<VueListeLineData> lineRef = param.lines.get(index);
		for (int i = index; i < param.nbLig; i++)
		{
			LibButtonLine<VueListeLineData> line = param.lines.get(i);
			if (line.isVisible)
			{
				for (int j = 0; j < param.nbCol; j++)
				{
					IntegerTableCell c = line.data.cells[j];
					if (c.isStaticText==false)
					{
						c.qte = lineRef.data.cells[j].qte;
					}
				}
				endOfModifier(line);
			}
		}
		
		// 
		displayMontantTotal();
	}

	private int getIndexFirstLineCommandable() 
	{
		List<LibButtonLine<VueListeLineData>> lines = param.lines;
		for (int i = 0; i < lines.size(); i++) 
		{
			LibButtonLine<VueListeLineData> line = lines.get(i);
			if (line.data.dateCommandable==DateCommandable.OUI)
			{
				return i;
			}
		}
		return -1;
	}



	private void handleSwitchButton() 
	{
		// On recopie les données dans le contratDto
		for (int i = 0; i < param.nbLig; i++)
		{
			for (int j = 0; j < param.nbCol; j++)
			{
				contratDTO.cell[i][j].qte = param.lines.get(i).data.cells[j].qte;
			}
		}
		
		// On calcule la nouvelle fenetre 
		CorePopup popup = new PopupSaisieQteContrat(data);
		
		// On insere la nouvelle fenetre dans le cascading popup 
		data.insertPopup(popup);
	}
	
	public LibButtonLine<VueListeLineData> constructLibButtonLine(LibButtonParam<VueListeLineData> param,int ligIndex, IntegerGridCell[][] cs)
	{
		ContratLigDTO lig = contratDTO.contratLigs.get(ligIndex);
		
		LibButtonLine<VueListeLineData> line = new LibButtonLine<>();
		line.data = new VueListeLineData();
		line.data.contratDTO = contratDTO;
		line.data.cells = new QteTableUtils().convertOneLineOfCells(contratDTO, cs, ligIndex); 
		line.data.dateLiv = lig.date;
		line.data.dateCommandable = computeDateCommandable(contratDTO,ligIndex);		
		
		line.isVisible = computeVisible(lig,param);
		line.lib1 = FormatUtils.getStdDate().format(lig.date);			
		line.lib2 = line.data.computeLib2();
		line.montant = line.data.computeMontant();
		line.qteTotale = line.data.computeQteTotale();
		if (param.readOnly)
		{
			line.buttonLib = "Détails";
			line.hasButton = line.qteTotale!=0;
		}
		else
		{
			line.buttonLib = "Modifier";
			line.hasButton = (line.data.dateCommandable == DateCommandable.OUI) || line.qteTotale!=0;
		}
		
		return line;
	}
	
	

	private DateCommandable computeDateCommandable(ContratDTO contratDTO, int ligIndex)
	{
		if (contratDTO.isFullExcludedLine(ligIndex))
		{
			return DateCommandable.PAS_DE_LIVRAISON;
		}
		if (contratDTO.isFullNotAvailableLine(ligIndex))
		{
			return DateCommandable.NO_STOCK;
		}
		return DateCommandable.OUI;
	}

	private boolean computeVisible(ContratLigDTO lig, LibButtonParam<VueListeLineData> param)
	{
		if (param.readOnly)
		{
			return true;
		}
		return lig.isModifiable;
	}	


	@Override
	protected void handleButton(LibButtonLine<VueListeLineData> line)
	{
		new PopupSaisieQteVueListeOneDate(line, param.readOnly).open(()->endOfModifier(line));
	}


	private void endOfModifier(LibButtonLine<VueListeLineData> line)
	{
		// Mise à jour du libellé 2, du montant et de la quantité totale de la ligne
		line.lib2 = line.data.computeLib2();
		line.montant = line.data.computeMontant();
		line.qteTotale = line.data.computeQteTotale();
		refreshLine(line);
		displayMontantTotal();
	}


	@Override
	protected void handleContinuer()
	{
		data.validate();
		close();
	}

	@Override
	public boolean performSauvegarder()
	{
		// On recopie les données dans le contratDto
		for (int i = 0; i < param.nbLig; i++)
		{
			for (int j = 0; j < param.nbCol; j++)
			{
				contratDTO.cell[i][j].qte = param.lines.get(i).data.cells[j].qte;
			}
		}
		
		// On verifie les regles de fin de saisie des quantités 
		if (new CheckOnEndSaisieQte().check(data)==false)
		{
			return false;
		}
		// 
		data.validate();
		
		return true;
	}
	
}
