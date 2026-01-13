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

 package fr.amapj.view.views.utilisateur;

import java.util.List;

import fr.amapj.model.models.fichierbase.EtatUtilisateur;
import fr.amapj.service.services.edgenerator.excel.EGListeAdherent;
import fr.amapj.service.services.edgenerator.excel.EGListeAdherent.Type;
import fr.amapj.service.services.utilisateur.UtilisateurDTO;
import fr.amapj.service.services.utilisateur.UtilisateurService;
import fr.amapj.view.engine.excelgenerator.LinkCreator;
import fr.amapj.view.engine.listpart.ButtonType;
import fr.amapj.view.engine.listpart.StandardListPart;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.swicthpopup.SwitchPopup;
import fr.amapj.view.views.common.amapientelecharger.TelechargerAmapien;


/**
 * Gestion des utilisateurs
 *
 */
@SuppressWarnings("serial")
public class UtilisateurListPart extends StandardListPart<UtilisateurDTO>
{

	public UtilisateurListPart()
	{
		super(UtilisateurDTO.class,false);
	}
	
	
	@Override
	protected String getTitle() 
	{
		return "Liste des utilisateurs";
	}


	@Override
	protected void drawButton() 
	{
		addButton("Créer un nouvel utilisateur", ButtonType.ALWAYS, e->new CreationUtilisateurEditorPart());
		addButton("Modifier", ButtonType.EDIT_MODE, e->new ModificationUtilisateurEditorPart(e.id));
		addButton("Voir", ButtonType.EDIT_MODE, e->new PopupUtilisateurVoirPart(e.id));
		addButton("Changer le mot de passe", ButtonType.EDIT_MODE, e->new PopupSaisiePassword(e.id));
		addButton("Archiver", ButtonType.EDIT_MODE, e->new PopupUtilisateurArchiver(e));
		addButton("Télécharger", ButtonType.EDIT_MODE, e->TelechargerAmapien.handleTelecharger(e.id));
		addButton("Autre...", ButtonType.ALWAYS, e->handleMore());

		addSearchField("Rechercher par nom ou prénom");
	}

	@Override
	protected void addExtraComponent() 
	{
		addComponent(LinkCreator.createLink(new EGListeAdherent(Type.STD)));
		
	}

	@Override
	protected void drawTable() 
	{	
		addColumn("nom","Nom");
		addColumn("prenom","Prénom");
		addColumn("roles","Rôle");
		addColumn("etatUtilisateur","État");
		addColumnDateTime("dateCreation","Date création");	
	}



	@Override
	protected List<UtilisateurDTO> getLines() 
	{
		return new UtilisateurService().getAllUtilisateurs(EtatUtilisateur.ACTIF);
	}


	@Override
	protected String[] getSortInfos() 
	{
		return new String[] { "nom" , "prenom" };
	}
	
	protected String[] getSearchInfos()
	{
		return new String[] { "nom" , "prenom" };
	}
	
	private CorePopup handleMore()
	{
		UtilisateurDTO dto = getSelectedLine();
		
		SwitchPopup popup = new SwitchPopup("Autres actions sur les utilisateurs",50);
		
		if (dto!=null)
		{
			popup.addLine("Supprimer l'utilisateur "+dto.nom+" "+dto.prenom, ()->new PopupSuppressionUtilisateur(dto));
			popup.addSeparator();
		}
		
		popup.addLine("Rendre actif les utilisateurs en masse", ()->new PopupRendreActifUtilisateurMasse(true));
		popup.addLine("Rendre inactif les utilisateurs en masse", ()->new PopupRendreActifUtilisateurMasse(false));
				
		popup.addSeparator();
		
		popup.addLine("Envoyer un e mail de bienvenue avec un mot de passe pour tous les utilisateurs sans mot de passe ", ()->new PopupEnvoiPasswordMasse());
		
		return popup;
	}
}
