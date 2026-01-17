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

import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.swicthpopup.SwitchPopup;

/**
 * Partie commune à Gestion des contrats vierges et des contrats signés, 
 * 
 * sur les stocks
 */
public class GestionContratStockPart 
{
	static public CorePopup handleStock(Long idModeleContrat)
	{
		CorePopup help = new SaisieQteStock().needHelp(idModeleContrat);
		if (help!=null)
		{
			return help;
		}
		
		
		SwitchPopup popup = new SwitchPopup("Gestion des quantités limites",60);
		
		popup.addLine("Saisir les quantités limites", ()->new SaisieQteStock().saisieQteStock(idModeleContrat));
		popup.addLine("Comparer les quantités commandées et les quantités limites", ()->new SaisieQteStock().compareQteStockQteCde(idModeleContrat));
		popup.addLine("Afficher le détail du calcul réalisé", ()->new SaisieQteStock().displayDetailCalculQteStockQteCde(idModeleContrat));
				
		return popup;
		
	}
}
