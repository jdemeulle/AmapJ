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

import fr.amapj.model.models.fichierbase.EtatProducteur;
import fr.amapj.service.services.parametres.ParametresArchivageDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.producteur.ProducteurDTO;
import fr.amapj.service.services.producteur.ProducteurService;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.tools.table.complex.ComplexTableBuilder;

/**
 * Permet d'archiver les producteurs 
 * 
 *
 */
public class PopupArchivageProducteur extends WizardFormPopup
{

	private ComplexTableBuilder<ProducteurDTO> builder;
	
	private List<ProducteurDTO> producteurToArchive;

	private List<ProducteurDTO> producteurs;

	private ParametresArchivageDTO param;

	/**
	 * 
	 */
	public PopupArchivageProducteur()
	{
		setWidth(80);
		popupTitle = "Archivage des producteurs";
		param = new ParametresService().getParametresArchivage();
		
	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldInfoGenerales());
		add(()->addFieldSaisie(),()->readSaisie());
		add(()->addFieldConfirmation());
	}

	private void addFieldInfoGenerales()
	{
		// Titre
		setStepTitle("les informations générales.");
		
		String str = "Cet outil va rechercher la liste des producteurs qu'il est souhaitable d'archiver";
		addHtml(str);
		
		str = new ProducteurService().computeArchivageLib(param);
		addHtml(str);
		
		str = "Vous pourrez alors choisir dans cette liste ceux que vous voulez archiver et ceux que vous voulez conserver.";
		addHtml(str);
	}
	
	

	private void addFieldSaisie()
	{
		// Titre
		setStepTitle("les producteurs à archiver");
		
		producteurs = new ProducteurService().getAllProducteursArchivables(param);
		producteurToArchive = new ArrayList<ProducteurDTO>();
		
		if (producteurs.size()==0)
		{
			addHtml("Il n'y a pas de producteurs à archiver.");
			setBackOnlyMode();
			return;
		}
		
			
		builder = new ComplexTableBuilder<ProducteurDTO>(producteurs);
		builder.setPageLength(7);
		
		builder.addString("Nom du producteur", false, 300,  e->e.nom);
		builder.addDate("Date de création du producteur", false, 150,  e->e.dateCreation);
		builder.addDate("Date de dernière livraison du producteur", false, 150,  e->e.dateDerniereLivraison);
		
		
		builder.addCheckBox("Archiver ce producteur", "cb",true, 150, e->false, null);
		
		addComplexTable(builder);
		
	}
	
	private String readSaisie()
	{
		producteurToArchive = builder.getSelectedCheckBox("cb");
		
		if (producteurToArchive.size()==0)
		{
			return "Vous devez sélectionner au moins un producteur pour pouvoir continuer.";
		}
		
		return null;
	}
	
	
	private void addFieldConfirmation()
	{
		// Titre
		setStepTitle("confirmation");
		
		addHtml("Vous allez archiver "+producteurToArchive.size()+" producteurs");
		
		addHtml("Appuyez sur Sauvegarder pour réaliser cette modification, ou Annuler pour ne rien modifier");
		
	}


	@Override
	protected void performSauvegarder() throws OnSaveException
	{
		for (ProducteurDTO p : producteurToArchive) 
		{
			try
			{
				new ProducteurService().updateEtat(p.id,EtatProducteur.ARCHIVE);
			}
			catch(Exception e)
			{
				throw new OnSaveException("Impossible d'archiver le producteur "+p.nom+". Raison : "+e.getMessage());
			}
		}
	}
}
