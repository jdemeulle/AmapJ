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
 package fr.amapj.view.views.gestioncontrat.editorpart;

import fr.amapj.service.services.stockservice.StockUtilService;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.swicthpopup.SwitchPopup;
import fr.amapj.view.views.gestioncontrat.editorpart.reglesaisie.RegleSaisieModeleContratEditorPart;

/**
 * Permet de choisir ce que l'on veut modifier
 * dans le contrat : l'entete, les dates ou les produits
 */
public class ChoixModifEditorPart
{
	
	public static CorePopup createPopup(Long id) 
	{
		if (id == null)
		{
			throw new RuntimeException("Le contrat a modifier ne peut pas etre null");
		}
		
		SwitchPopup popup = new SwitchPopup("Modification d'un contrat",50);
		
		popup.setLine1("Veuillez indiquer ce que vous souhaitez modifier :");

		popup.addLine("Les informations d'entete (nom,description, date limite d'inscription,nature)", ()->new ModifEnteteContratEditorPart(id));
		
		popup.addSeparator();
		

		popup.addLine("Les dates de livraisons", ()->new ModifDateContratEditorPart(id));

		popup.addLine("Les produits disponibles et les prix", ()->new ModifProduitContratEditorPart(id));

		popup.addLine("Barrer certaines dates ou certains produits", ()->new BarrerDateContratEditorPart(id));
		
		popup.addSeparator();
		
		popup.addLine("La gestion des jokers", ()->new ModifJokerContratEditorPart(id));
		
		popup.addSeparator();
		
		if (new StockUtilService().hasProducteurGestionStockFromModeleContrat(id))
		{
			popup.addLine("Les limites en quantités (stock) ", ()->new LimiteQuantiteContratEditorPart(id));
			
			popup.addSeparator();
		}
		
		popup.addLine("Les informations de paiement", ()->new ModifPaiementContratEditorPart(id));
		
		popup.addSeparator();
		
		popup.addLine("La gestion des documents d'engagement", ()->new ModifGestionDocEngagementEditorPart(id));
		
		popup.addSeparator();
		
		popup.addLine("La mise en forme graphique", ()->new MiseEnFormeModeleContratEditorPart(id));
		
		popup.addLine("Les règles de saisie", ()->new RegleSaisieModeleContratEditorPart(id));
	
		return popup;
	}
}
