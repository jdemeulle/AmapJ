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
 package fr.amapj.view.views.saisiecontrat.step1qte.utils.checkonend;

import java.util.List;

import fr.amapj.service.services.stockservice.verifstock.VerifStockService;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.SaisieContratData;

/**
 *   
 *
 */
public class CheckOnEndSaisieQte 
{
	/**
	 * Verification faite à la fin de la saisie des quantités
	 * On vérifie à la fois le stock et les régles de saisie
	 */
	public boolean check(SaisieContratData data)
	{
		// On verifie les regles de saisies
		if (PopupRegleSaisieVisuErreur.performCheck(data)==false)
		{
			return false;
		}
		
		// On verifie le stock
		data.verifStockDTO.setQteMe(data.contratDTO);
		if (data.verifStockDTO.isStockSuffisant()==false)
		{
			List<String> ls = new VerifStockService().computePrettyMessage(data.verifStockDTO);
			new MessagePopup("Impossible de continuer",ls).open();
			return false;
		}
		
		return true;
	}
}
