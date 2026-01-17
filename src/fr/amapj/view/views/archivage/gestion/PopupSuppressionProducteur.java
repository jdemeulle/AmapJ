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
 package fr.amapj.view.views.archivage.gestion;

import java.util.ArrayList;
import java.util.List;

import fr.amapj.service.services.parametres.ParametresArchivageDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.producteur.ProducteurDTO;
import fr.amapj.service.services.producteur.ProducteurService;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.tools.table.complex.ComplexTableBuilder;

/**
 * Permet de supprimer les producteurs archivés 
 * 
 *
 */
public class PopupSuppressionProducteur extends WizardFormPopup
{

	private ComplexTableBuilder<ProducteurDTO> builder;
	
	private List<ProducteurDTO> toSuppress;

	private List<ProducteurDTO> dtos;

	private ParametresArchivageDTO param;

	/**
	 * 
	 */
	public PopupSuppressionProducteur()
	{
		setWidth(80);
		popupTitle = "Suppression des producteurs archivés trop anciens";
		param = new ParametresService().getParametresArchivage();
		
	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldInfoGenerales());
		add(()->addFieldSaisieContrat(),()->readContratsToArchive());
		add(()->addFieldConfirmation());
	}

	private void addFieldInfoGenerales()
	{
		// Titre
		setStepTitle("les informations générales.");
		
		String str = "Cet outil va rechercher la liste des producteurs qu'il est souhaitable de supprimer";
		addHtml(str);
		
		str = new ProducteurService().computeSuppressionLib(param);
		addHtml(str);
		
	}
	
	

	private void addFieldSaisieContrat()
	{
		// Titre
		setStepTitle("les producteurs à supprimer");
		
		dtos = new ProducteurService().getAllProducteurSupprimables(param);
		toSuppress = new ArrayList<ProducteurDTO>();
		
		if (dtos.size()==0)
		{
			addHtml("Il n'y a pas de producteurs à supprimer.");
			setBackOnlyMode();
			return;
		}
		
			
		builder = new ComplexTableBuilder<ProducteurDTO>(dtos);
		builder.setPageLength(7);
		
		
		builder.addString("Nom du producteur", false, 300,  e->e.nom);
		builder.addDate("Date de création du producteur", false, 150,  e->e.dateCreation);
		
		builder.addCheckBox("Supprimer ce producteur", "cb",true, 150, e->true, null);
		
		addComplexTable(builder);
		
	}
	
	private String readContratsToArchive()
	{
		toSuppress = builder.getSelectedCheckBox("cb");
		
		if (toSuppress.size()==0)
		{
			return "Vous devez sélectionner au moins un producteur pour pouvoir continuer.";
		}
		
		return null;
	}
	
	
	private void addFieldConfirmation()
	{
		// Titre
		setStepTitle("confirmation");
		
		addHtml("Vous allez supprimer DÉFINITIVEMENT "+toSuppress.size()+" producteurs");
		
		addHtml("Appuyez sur Sauvegarder pour réaliser cette modification, ou Annuler pour ne rien modifier");
		
	}


	@Override
	protected void performSauvegarder() throws OnSaveException
	{
		for (ProducteurDTO mc : toSuppress) 
		{
			try
			{
				new ProducteurService().deleteWithProduit(mc.id);
			}
			catch(Exception e)
			{
				throw new OnSaveException("Impossible de supprimer le producteur "+mc.nom+". Raison : "+e.getMessage());
			}
		}
	}
}
