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
 package fr.amapj.view.views.archivage.utilisateur;

import java.util.List;

import fr.amapj.model.models.fichierbase.EtatUtilisateur;
import fr.amapj.service.services.edgenerator.excel.EGListeAdherent;
import fr.amapj.service.services.edgenerator.excel.EGListeAdherent.Type;
import fr.amapj.service.services.utilisateur.UtilisateurDTO;
import fr.amapj.service.services.utilisateur.UtilisateurService;
import fr.amapj.view.engine.excelgenerator.LinkCreator;
import fr.amapj.view.engine.listpart.ButtonType;
import fr.amapj.view.engine.listpart.StandardListPart;
import fr.amapj.view.views.utilisateur.PopupUtilisateurVoirPart;


/**
 * Gestion des utilisateurs archivés 
 *
 */
@SuppressWarnings("serial")
public class ArchivageUtilisateurListPart extends StandardListPart<UtilisateurDTO>
{

	public ArchivageUtilisateurListPart()
	{
		super(UtilisateurDTO.class,false);
	}
	
	
	@Override
	protected String getTitle() 
	{
		return "Utilisateurs archivés";
	}


	@Override
	protected void drawButton() 
	{
		addButton("Voir",ButtonType.EDIT_MODE,e->new PopupUtilisateurVoirPart(e.id));
		addButton("Remettre actif",ButtonType.EDIT_MODE, e->new PopupUtilisateurRetourActif(e));
		addButton("Supprimer définitivement",ButtonType.EDIT_MODE, e->new PopupUtilisateurSuppression(e));

		addSearchField("Rechercher par nom ou prénom");
	}

	@Override
	protected void addExtraComponent() 
	{
		addComponent(LinkCreator.createLink(new EGListeAdherent(Type.AVEC_INACTIF)));
		
	}

	@Override
	protected void drawTable() 
	{
		addColumn("nom","Nom");
		addColumn("prenom","Prenom");
		addColumn("roles","Role");
		addColumn("etatUtilisateur","Etat");
		addColumnDate("dateCreation","Date création");
	}



	@Override
	protected List<UtilisateurDTO> getLines() 
	{
		return new UtilisateurService().getAllUtilisateurs(EtatUtilisateur.INACTIF);
	}


	@Override
	protected String[] getSortInfos() 
	{
		return new String[] { "dateCreation"  };
	}
	
	protected String[] getSearchInfos()
	{
		return new String[] { "nom" , "prenom" };
	}
}
