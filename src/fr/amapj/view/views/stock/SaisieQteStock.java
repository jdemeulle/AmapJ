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

import com.vaadin.shared.ui.label.ContentMode;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.CollectionUtils;
import fr.amapj.common.StringUtils;
import fr.amapj.model.models.contrat.modele.StockIdentiqueDate;
import fr.amapj.model.models.contrat.modele.StockMultiContrat;
import fr.amapj.service.services.stockservice.StockUtilService;
import fr.amapj.service.services.stockservice.saisie.SaisieStockDTO;
import fr.amapj.service.services.stockservice.saisie.SaisieStockService;
import fr.amapj.service.services.stockservice.verifstock.VerifStockService;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.corepopup.CorePopup.ColorStyle;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;

public class SaisieQteStock
{
	/**
	 * Retourne null si ce contrat est bien suivi en stock, sinon retourne un message d'aide
	 */
	public MessagePopup needHelp(Long modeleContratId) 
	{
		// On vérifie tout d'abord si ce contrat est suivi en stock 
		if (new StockUtilService().hasModeleContratGestionStock(modeleContratId)==true)
		{
			return null;
		}

		String msg = "La gestion des limites en quantités (stocks) n'est pas activé pour ce modèle de contrat.<br/>"
						+ "Si vous souhaitez activer cette fonctionnalité, il faut d'abord le déclarer dans le producteur </br>"
						+ "<ul>"
						+ "<li>Aller dans TRÉSORIER / Producteur </li>"
						+ "<li>Cliquer sur Modifier </li>"
						+ "<li>A l'étape 1, dans le champ \"Activer la gestion des limites en quantité pour ce producteur\" Mettre OUI</li>"
						+ "</ul>"
						+ "Ensuite, il faut le déclarer dans le modèle de contrat :</br>"
						+ "<ul>"
						+ "<li>Aller dans RÉFÉRENT / Gestion des contrats vierges </li>"
						+ "<li>Cliquer sur Modifier </li>"
						+ "<li>Cliquer sur les limites en quantités </li>"
						+ "<li>A l'étape 1, dans le champ \"Activer la gestion des limites en quantité pour ce contrat\" Mettre OUI</li>"
						+ "</ul>";
						
				
		return new MessagePopup("Info stock",ContentMode.HTML,ColorStyle.GREEN,msg);
	}
	
	
	/**
	 * Retourne un popup pour la saisie des quantités en stock 
	 */
	public CorePopup saisieQteStock(Long modeleContratId)
	{
		SaisieStockDTO saisieStockDTO = new SaisieStockService().loadStockInfo(modeleContratId,false,0);
		
		
		// On verifie si il y a des quantités à saisir
		if (saisieStockDTO.verifStockDTO.dates.size()==0)
		{
			String msg = "Il n'y a pas de stock à saisir (0 date).";
			return new MessagePopup("Info stock",ContentMode.HTML,ColorStyle.GREEN,msg);
		}
		
		
		
		switch (saisieStockDTO.modeleContratStockIdentiqueDate) 
		{
		case OUI : return new PopupSaisieQteStockMemeDate(saisieStockDTO);
		case NON : return new PopupSaisieQteStockMultiDate(saisieStockDTO);

		default: throw new AmapjRuntimeException();
		}
	}
	

	

	/**
	 * Retourne un popup pour l'affichage de la comparaison entre les quantités commandés et les stocks
	 */
	public CorePopup compareQteStockQteCde(Long modeleContratId) 
	{
		// On visualise avec 30 jours en arriére
		SaisieStockDTO saisieStockDTO = new SaisieStockService().loadStockInfo(modeleContratId,true,-30);
		return new PopupVisualiserQteStock(saisieStockDTO);
	}


	public CorePopup displayDetailCalculQteStockQteCde(Long modeleContratId) 
	{
		List<String> ls = new VerifStockService().showDebug(modeleContratId);
		return new MessagePopup("Info stock",ColorStyle.GREEN,ls);
	}

	
	public static String computeHeader(SaisieStockDTO saisieStockDTO) 
	{
		String str = "";
		
		//
		if (saisieStockDTO.modeleContratStockIdentiqueDate==StockIdentiqueDate.OUI)
		{
			str = str+"Les quantités disponibles sont identiques à chaque livraison.<br/>";
		}
		
		// 
		if (saisieStockDTO.modeleContratStockMultiContrat==StockMultiContrat.NON)
		{
			str = str+"Ces quantités s'appliquent uniquement au contrat "+StringUtils.s(saisieStockDTO.nomModeleContrats.get(0));
		}
		else
		{
			str = str+"Ces quantités sont partagées sur les contrats suivants :<ul>";
			str = str+CollectionUtils.asString(saisieStockDTO.nomModeleContrats, "",e->"<li>"+StringUtils.s(e)+"</li>");
			str = str+"</ul>"; 
		}
		return str;
	}
	
}
