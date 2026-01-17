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

import com.vaadin.ui.ComboBox;

import fr.amapj.service.services.gestioncontrat.GestionContratService;

/**
 * Permet de modifier les regles de stock
 */
public class LimiteQuantiteContratEditorPart extends GestionContratEditorPart
{
	
	private ComboBox box;
	
	@Override
	protected void configure()
	{
		add(()->drawGestionStockGeneral());
	}
	

	
	
	/**
	 * 
	 */
	public LimiteQuantiteContratEditorPart(Long id)
	{
		super();
		popupTitle = "Modification régles de gestion des limites en quantité";
		setWidth(80);
				
		// Chargement de l'objet  à modifier
		modeleContrat = new GestionContratService().loadModeleContrat(id);
		
		setModel(modeleContrat);
		
	}
	

	

	protected void performSauvegarder()
	{	
		// Sauvegarde du contrat
		new GestionContratService().updateLimiteQuantiteModeleContrat(modeleContrat);
	}
	
}
