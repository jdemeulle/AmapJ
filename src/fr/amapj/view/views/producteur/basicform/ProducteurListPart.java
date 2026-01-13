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

 package fr.amapj.view.views.producteur.basicform;

import java.util.List;

import fr.amapj.model.models.fichierbase.EtatProducteur;
import fr.amapj.service.services.producteur.ProducteurDTO;
import fr.amapj.service.services.producteur.ProducteurService;
import fr.amapj.view.engine.listpart.ButtonType;
import fr.amapj.view.engine.listpart.StandardListPart;
import fr.amapj.view.engine.popup.suppressionpopup.SuppressionPopup;


/**
 * Gestion des producteurs
 *
 */
@SuppressWarnings("serial")
public class ProducteurListPart extends StandardListPart<ProducteurDTO>
{

	public ProducteurListPart()
	{
		super(ProducteurDTO.class,false);
	}
	
	
	@Override
	protected String getTitle() 
	{
		return "Liste des producteurs";
	}


	@Override
	protected void drawButton() 
	{
		addButton("Créer un nouveau producteur",ButtonType.ALWAYS,e->new ProducteurEditorPart(true,null));
		addButton("Modifier",ButtonType.EDIT_MODE,e->new ProducteurEditorPart(false,e.id));
		addButton("Voir",ButtonType.EDIT_MODE,e->new ProducteurVoirPart(e));
		addButton("Supprimer",ButtonType.EDIT_MODE,e->handleSupprimer());
		addButton("Archiver",ButtonType.EDIT_MODE,e->new PopupProducteurArchiver(e));
		
		addSearchField("Rechercher par nom");
	}


	@Override
	protected void drawTable() 
	{
		addColumn("nom","Nom");
		addColumn("utilisateurInfo","Producteurs");
		addColumn("referentInfo","Référents");
		addColumn("nbModeleContratActif","Nb contrats").center();
		addColumnDate("dateDerniereLivraison","Dernière liv");
		addColumnDate("dateCreation","Date création");		
	}



	@Override
	protected List<ProducteurDTO> getLines() 
	{
		return new ProducteurService().getAllProducteurs(EtatProducteur.ACTIF);
	}


	@Override
	protected String[] getSortInfos() 
	{
		return new String[] { "nom" };
	}
	
	protected String[] getSearchInfos()
	{
		return new String[] { "nom" };
	}
	

	private SuppressionPopup handleSupprimer()
	{
		ProducteurDTO dto = getSelectedLine();
		String text = "Êtes vous sûr de vouloir supprimer le producteur "+dto.nom+" ?";
		return new SuppressionPopup(text,dto.id,e->new ProducteurService().delete(e));
	}
}
