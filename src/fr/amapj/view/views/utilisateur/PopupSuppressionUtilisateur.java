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

import com.vaadin.data.util.BeanItem;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Notification;

import fr.amapj.service.services.utilisateur.UtilisateurDTO;
import fr.amapj.service.services.utilisateur.UtilisateurService;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.errorpopup.ErrorPopup;
import fr.amapj.view.engine.popup.formpopup.FormPopup;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;
import fr.amapj.view.engine.popup.suppressionpopup.UnableToSuppressException;

/**
 * Popup pour la saisie de l'état d'un utilisateur
 *  
 */
@SuppressWarnings("serial")
public class PopupSuppressionUtilisateur extends FormPopup
{
	private UtilisateurDTO utilisateurDTO;


	/**
	 * 
	 */
	public PopupSuppressionUtilisateur(UtilisateurDTO utilisateurDTO)
	{
		popupTitle = "Suppression d'un utilisateur";
		this.utilisateurDTO = utilisateurDTO;
		
		setModel(utilisateurDTO);
		
	}
	
	
	protected void addFields()
	{
		addText("Êtes-vous sûr de vouloir supprimer l' utilisateur "+utilisateurDTO.nom+" "+utilisateurDTO.prenom+" ?");
	}

	protected void performSauvegarder()
	{
		try
		{
			new UtilisateurService().deleteUtilisateur(utilisateurDTO.id);
			Notification.show("Suppression", "Suppression faite", Notification.Type.HUMANIZED_MESSAGE);	
		}
		catch(UnableToSuppressException e)
		{
			String title = "Erreur à la suppression";
			String t1="Impossible de supprimer cet élément. Raison :";
			String t2 = e.getMessage();
			MessagePopup popup = new MessagePopup(title,ContentMode.HTML,ColorStyle.RED,t1,t2);
			CorePopup.open(popup);
		}
		catch(Exception e)
		{
			ErrorPopup.open("Impossible de supprimer cet élément.",e);
		}
	}
}

