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
 package fr.amapj.view.views.saisiecontrat.step1qte.std.onedatetable;

import java.text.SimpleDateFormat;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.FormatUtils;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.view.engine.grid.integergrid.IntegerGridCell;
import fr.amapj.view.engine.grid.integertable.PopupIntegerTable;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.ModeSaisie;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.SaisieContratData;
import fr.amapj.view.views.saisiecontrat.step1qte.std.grid.PopupSaisieQteContrat;
import fr.amapj.view.views.saisiecontrat.step1qte.utils.QteTableUtils;
import fr.amapj.view.views.saisiecontrat.step1qte.utils.checkonend.CheckOnEndSaisieQte;

/**
 * Popup pour la saisie des quantites sur une seule date, en mode table 
 *  
 */
public class PopupSaisieQteTable extends PopupIntegerTable
{	
	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	
	private ContratDTO contratDTO;
	
	private SaisieContratData data;

	private int indexLig;
	
	/**
	 * 
	 *  
	 */
	public PopupSaisieQteTable(SaisieContratData data)
	{
		super();
		this.data = data;
		this.contratDTO = data.contratDTO;
		
		
		//
		
		popupTitle = "Date de livraison : "+FormatUtils.getStdDate().format(contratDTO.contratLigs.get(0).date)+" - "+contratDTO.nom;
		
		// 
		param.readOnly = (data.modeSaisie==ModeSaisie.READ_ONLY);
		param.messageSpecifique = s(data.messageSpecifique);
		param.libButtonSave = "Continuer ...";
		
	}
	
	
	
	public void loadParam()
	{		
		//
		if (contratDTO.contratLigs.size()!=1)
		{
			throw new AmapjRuntimeException();
		}
		this.indexLig = 0;
		if (param.readOnly==false && contratDTO.contratLigs.get(indexLig).isModifiable==false)
		{
			throw new AmapjRuntimeException();
		}
		
		//
		param.nbCell = contratDTO.contratColumns.size();
		
		//
		IntegerGridCell[][] cs = contratDTO.extractCells();
		param.cell = new QteTableUtils().convertOneLineOfCells(contratDTO, cs, indexLig); 


		//
		param.allowedEmpty = false;
		param.libPrixTotal = "Montant pour la date du "+FormatUtils.getStdDate().format(contratDTO.contratLigs.get(indexLig).date);
		
		param.switchButtonAction = ()->handleSwitchButton();
		
	}


	private void handleSwitchButton() 
	{
		// On recopie les données dans le contratDto
		for (int j = 0; j < param.nbCell; j++)
		{
			contratDTO.cell[indexLig][j].qte = param.cell[j].qte;
		}
		
		// On calcule la nouvelle fenetre 
		CorePopup popup = new PopupSaisieQteContrat(data);
		
		// On insere la nouvelle fenetre dans le cascading popup 
		data.insertPopup(popup);
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
		for (int j = 0; j < param.nbCell; j++)
		{
			contratDTO.cell[indexLig][j].qte = param.cell[j].qte;
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
