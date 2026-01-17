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
 package fr.amapj.view.views.appinstance;

import java.util.List;

import fr.amapj.service.services.appinstance.AppInstanceDTO;
import fr.amapj.service.services.appinstance.AppInstanceService;
import fr.amapj.view.engine.listpart.ButtonType;
import fr.amapj.view.engine.listpart.StandardListPart;
import fr.amapj.view.engine.popup.copypopup.CopyPopup;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.corepopup.CorePopup.ColorStyle;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;
import fr.amapj.view.engine.popup.suppressionpopup.SuppressionPopup;
import fr.amapj.view.engine.popup.swicthpopup.SwitchPopup;


/**
 * Gestion des instances
 *
 */
public class AppInstanceListPart extends StandardListPart<AppInstanceDTO>
{

	public AppInstanceListPart()
	{
		super(AppInstanceDTO.class,true);
	}
	
	
	@Override
	protected String getTitle() 
	{
		return "Liste des instances";
	}


	@Override
	protected void drawButton() 
	{
		addButton("Créer une nouvelle instance", ButtonType.ALWAYS, e->new AppInstanceEditorPart());
		addButton("État courant", ButtonType.EDIT_MODE, e->handleStart());
		addButton("État au démarrage", ButtonType.EDIT_MODE, e->handleStateOnStart());
		addButton("Se connecter", ButtonType.EDIT_MODE, e->handleConnect());
		addButton("Requête SQL", ButtonType.EDIT_MODE, e->handleSql());
		addButton("Sauvegarder", ButtonType.EDIT_MODE, e->handleSave());
		addButton("Supprimer", ButtonType.EDIT_MODE, e->handleSupprimer());
		addButton("PATCH V042", ButtonType.ALWAYS, e->new PatchEditorPart());
		addButton("Autre ...", ButtonType.ALWAYS, e->handleAutre());

		addSearchField("Rechercher par nom");
		
	}
	

	@Override
	protected void drawTable() 
	{
		addColumn("nomInstance","Nom");
		addColumnDateTime("dateCreation","Date de création");
		addColumn("state","État courant");
		addColumn("stateOnStart","État démarrage");
		addColumn("nbUtilisateurs","Nb utilisateurs");
		addColumn("nbMails","Mails envoyés");
	}



	@Override
	protected List<AppInstanceDTO> getLines() 
	{
		return new AppInstanceService().getAllInstances(true);
	}


	@Override
	protected String[] getSortInfos() 
	{
		return new String[] { "nomInstance" };
	}
	
	protected String[] getSearchInfos()
	{
		return new String[] { "nomInstance" };
	}


	private CorePopup handleAutre()
	{
		SwitchPopup popup = new SwitchPopup("Autres actions sur les instances",60);
			
		popup.addLine("Extraire les mails de tous les administrateurs", ()->new CopyPopup("Mails des administrateurs", ()->new AppInstanceService().getAllMails()));
		popup.addLine("Extraire les mails de tous les administrateurs + tresoriers + stats", ()->new CopyPopup("Mails admin + stats", ()->new AppInstanceService().getStatInfo()));
		popup.addLine("Extraire les schémas de toutes les bases", ()->new CopyPopup("Schemas des bases", ()->new AppInstanceService().getSchemaAllBases(false)));
		popup.addLine("Extraire la taille de toutes les bases", ()->new CopyPopup("Taille des bases", ()->new AppInstanceService().getSchemaAllBases(true)));
		popup.addLine("Vérifier la cohérence des modèles de contrats", ()->new PopupCheckCoherenceModeleContrat(getSelectedLines()));
		// popup.addLine("Charger une sauvegarde", ()->new PopupLoadBackup());
		popup.addLine("Paramétrer le mail d'envoi des backups", ()->new PopupSaisieMailBackup());
			
		return popup;
	}
	
	private CorePopup handleStart()
	{
		List<AppInstanceDTO> dtos = getSelectedLines();
		return new PopupEtatAppInstance(dtos);
	}
	
	private CorePopup handleStateOnStart()
	{
		List<AppInstanceDTO> dtos = getSelectedLines();
		return new PopupStateOnStartAppInstance(dtos);
	}
	
	private CorePopup handleConnect()
	{
		List<AppInstanceDTO> dtos = getSelectedLines();
		if (dtos.size()==1)
		{
			AppInstanceDTO dto = dtos.get(0);
			return new PopupConnectAppInstance(dto);
		}
		else
		{
			return new MessagePopup("Notification", ColorStyle.RED, "Vous devez sélectionner une et une seule instance");
		}
		
	}
	
	private CorePopup handleSql()
	{
		List<AppInstanceDTO> dtos = getSelectedLines();
		return new PopupSqlAppInstance(dtos);
	}
	
	private CorePopup handleSave()
	{
		List<AppInstanceDTO> dtos = getSelectedLines();
		return new PopupSaveAppInstance(dtos);
	}



	protected CorePopup handleSupprimer()
	{
		List<AppInstanceDTO> dtos = getSelectedLines();
		if (dtos.size()!=1)
		{
			return new MessagePopup("Notification", ColorStyle.RED, "Vous devez sélectionner une et une seule instance");
		}
		
		AppInstanceDTO dto = dtos.get(0);
		String text = "Êtes-vous sûr de vouloir supprimer l'instance "+dto.nomInstance+" ?";
		return new SuppressionPopup(text,dto.id,e->new AppInstanceService().delete(e));	
	}

	
}
