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

 package fr.amapj.view.views.produit;

import java.util.List;

import fr.amapj.service.services.edgenerator.excel.EGListeProduitProducteur;
import fr.amapj.service.services.edgenerator.excel.EGListeProduitProducteur.Type;
import fr.amapj.service.services.produit.ProduitDTO;
import fr.amapj.service.services.produit.ProduitService;
import fr.amapj.service.services.web.WebPageDTO;
import fr.amapj.view.engine.excelgenerator.TelechargerPopup;
import fr.amapj.view.engine.listpart.ButtonType;
import fr.amapj.view.engine.listpart.StandardListPart;
import fr.amapj.view.engine.popup.suppressionpopup.SuppressionPopup;
import fr.amapj.view.views.producteur.ProducteurSelectorPart;
import fr.amapj.view.views.webpage.WebPageEditorPart;


/**
 * Gestion des utilisateurs
 *
 */
@SuppressWarnings("serial")
public class ProduitListPart extends StandardListPart<ProduitDTO>
{

	private ProducteurSelectorPart producteurSelector;
	
	public ProduitListPart()
	{
		super(ProduitDTO.class,false);
	}
	
	
	@Override
	protected String getTitle() 
	{
		return "Liste des produits";
	}
	
	@Override
	protected void addSelectorComponent()
	{
		producteurSelector = new ProducteurSelectorPart(this,true,true);
		addComponent(producteurSelector.getChoixProducteurComponent());
	}


	@Override
	protected void drawButton() 
	{
		addButton("Ajouter un produit", ButtonType.ALWAYS, e->handleAjouter());
		addButton("Modifier", ButtonType.EDIT_MODE, e->new ProduitEditorPart(false,e.id,null));
		addButton("Supprimer", ButtonType.EDIT_MODE, e->handleSupprimer());
		addButton("Description", ButtonType.EDIT_MODE, e->handleDescription(e));
		addButton("Telecharger", ButtonType.ALWAYS, e->handleTelecharger(e));

		addSearchField("Rechercher par nom ou conditionnement");
	}

	@Override
	protected void drawTable() 
	{
		addColumn("nom","Nom");
		addColumn("conditionnement","Conditionnement");
	}



	@Override
	protected List<ProduitDTO> getLines() 
	{
		Long idProducteur = producteurSelector.getProducteurId();
		if (idProducteur==null)
		{
			return null;
		}
		return new ProduitService().getAllProduitDTO(idProducteur);
	}


	@Override
	protected String[] getSortInfos() 
	{
//		return new String[] { "nom" , "conditionnement" };
		return new String[] { "" , "" };
	}
	
	protected String[] getSearchInfos()
	{
		return new String[] { "nom" , "conditionnement" };
	}
	
	
	private ProduitEditorPart handleAjouter()
	{
		Long idProducteur = producteurSelector.getProducteurId();
		return new ProduitEditorPart(true,null,idProducteur);
	}
	
	
	protected SuppressionPopup handleSupprimer()
	{
		ProduitDTO dto = getSelectedLine();
		String text = "Etes vous sûr de vouloir supprimer le produit "+dto.nom+", "+dto.conditionnement+" ?";
		return new SuppressionPopup(text,dto.id,e->new ProduitService().deleteProduit(e));
	}
	
	private WebPageEditorPart handleDescription(ProduitDTO p)
	{
		WebPageDTO webPageDto = new ProduitService().loadWebPage(p.id);
		return new WebPageEditorPart(p.nom+", "+p.conditionnement, webPageDto, ()->new ProduitService().saveWebPage(p.id, webPageDto));		
	}
	
	private TelechargerPopup handleTelecharger(ProduitDTO p) 
	{
		Long idProducteur = producteurSelector.getProducteurId();
		
		TelechargerPopup popup = new TelechargerPopup("Liste des produits et des producteurs",80);
		popup.addGenerator(new EGListeProduitProducteur(idProducteur,Type.STD));
		popup.addGenerator(new EGListeProduitProducteur(null,Type.STD));
		return popup;
	}

	
}
