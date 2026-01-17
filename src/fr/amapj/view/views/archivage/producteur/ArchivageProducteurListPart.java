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
 package fr.amapj.view.views.archivage.producteur;

import java.util.List;

import fr.amapj.model.models.fichierbase.EtatProducteur;
import fr.amapj.service.services.producteur.ProducteurDTO;
import fr.amapj.service.services.producteur.ProducteurService;
import fr.amapj.view.engine.listpart.ButtonType;
import fr.amapj.view.engine.listpart.StandardListPart;
import fr.amapj.view.views.producteur.basicform.ProducteurVoirPart;


/**
 *Liste des producteurs archivés 
 *
 */
public class ArchivageProducteurListPart extends StandardListPart<ProducteurDTO> 
{	
	
	public ArchivageProducteurListPart()
	{
		super(ProducteurDTO.class,false);
	}
	
	
	@Override
	protected String getTitle() 
	{
		return "Liste des producteurs archivés" ;
	}


	@Override
	protected void drawButton() 
	{
		addButton("Voir",ButtonType.EDIT_MODE,e->new ProducteurVoirPart(e));
		addButton("Remettre actif",ButtonType.EDIT_MODE, e->new PopupProducteurRetourActif(e));
		addButton("Supprimer définitivement",ButtonType.EDIT_MODE, e->new PopupProducteurSuppression(e));
			
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
		return new ProducteurService().getAllProducteurs(EtatProducteur.ARCHIVE);
	}


	@Override
	protected String[] getSortInfos() 
	{
		return new String[] { "dateCreation" };
	}
	
	protected String[] getSearchInfos()
	{
		return new String[] { "nom" };
	}
}
