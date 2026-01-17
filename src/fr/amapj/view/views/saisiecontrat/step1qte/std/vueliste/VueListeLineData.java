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

import java.util.Date;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.StringUtils;
import fr.amapj.common.mtext.MTextLabel;
import fr.amapj.service.services.mescontrats.ContratColDTO;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.view.engine.grid.integertable.IntegerTableCell;

/**
 * Données pour chaque ligne de la vue liste 
 *
 */
public class VueListeLineData
{
	public ContratDTO contratDTO;
	
	public DateCommandable dateCommandable; 
	
	public Date dateLiv;
	
	public IntegerTableCell[] cells;

	public boolean isEmptyLine() 
	{
		for (int i = 0; i < cells.length; i++) 
		{
			if (cells[i].qte!=0)
			{
				return false;
			}
		}
		
		return true;
	}

	static public enum DateCommandable
	{
		// Il est possible de commander
		OUI , 
		
		// Il est possible de commander, mais le stock est vide 
		NO_STOCK,
		
		// Le producteur ne livre pas à cette date 
		PAS_DE_LIVRAISON
	}
	
	
	/**
	 * Calcul du lib2 et du styleName
	 */
	public MTextLabel computeLib2()
	{		
		// Si pas de commande de la part de l'amapien
		if (isEmptyLine())
		{
			switch (dateCommandable)
			{
				case OUI: return MTextLabel.html("Votre panier est vide.","popup-lib-button-lib2-margin25px");
				case NO_STOCK : return MTextLabel.html("Il n'y a plus de produits disponibles.","popup-lib-button-lib2-margin25px");
				case PAS_DE_LIVRAISON : return MTextLabel.html("Pas de livraison ce jour là.","popup-lib-button-lib2-margin25px");
				default : throw new AmapjRuntimeException();
			}
		}
		
		// Si l'amapien a commandé 
		StringBuilder sb = new StringBuilder();
		
		// Ce cas ne devrait pas arriver, mais il peut se produire quand même - on notifie alors l'utilisateur 
		if (dateCommandable==DateCommandable.PAS_DE_LIVRAISON)
		{
			sb.append("<b>ATTENTION : pas de livraison normalement !!!</b> <br/>");
		}
		sb.append("<ul>");
		for (ContratColDTO contratColDTO : contratDTO.contratColumns) 
		{
			int qte = cells[contratColDTO.j].qte;
			if (qte!=0)
			{
				sb.append("<li>"+qte+" "+s(contratColDTO.nomProduit)+", "+s(contratColDTO.condtionnementProduit)+"</li>");
			}
		}
		sb.append("</ul>");
		return MTextLabel.html(sb.toString());
	}
	
	private String s(String value)
	{
		return StringUtils.s(value);
	}

	public int computeMontant()
	{
		int mnt=0;
		for (ContratColDTO contratColDTO : contratDTO.contratColumns) 
		{
			int qte = cells[contratColDTO.j].qte;
			mnt=mnt+qte*contratColDTO.prix;
		}
		return mnt;
	}

	public int computeQteTotale()
	{
		int qteTotale = 0;
		for (int i = 0; i < cells.length; i++)
		{
			qteTotale = qteTotale+cells[i].qte;
		}
		return qteTotale;
	}
}
