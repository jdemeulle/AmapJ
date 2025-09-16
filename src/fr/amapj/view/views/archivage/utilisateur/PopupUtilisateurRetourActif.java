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

import fr.amapj.model.models.fichierbase.EtatUtilisateur;
import fr.amapj.service.services.utilisateur.UtilisateurDTO;
import fr.amapj.service.services.utilisateur.UtilisateurService;
import fr.amapj.view.engine.popup.formpopup.FormPopup;

/**
 * Popup pour remettre actif  un utilisateur
 *  
 */
public class PopupUtilisateurRetourActif extends FormPopup
{
	private UtilisateurDTO utilisateurDTO;


	/**
	 * 
	 */
	public PopupUtilisateurRetourActif(UtilisateurDTO utilisateurDTO)
	{
		popupTitle = "Rendre actif un utilisateur";
		this.utilisateurDTO = utilisateurDTO;
		
		setModel(utilisateurDTO);
		
	}
	
	
	protected void addFields()
	{
		addHtml("Voulez vous rendre actif l'utilisateur "+utilisateurDTO.nom+" "+utilisateurDTO.prenom+" ? ");
			
	}

	protected void performSauvegarder()
	{
		new UtilisateurService().updateEtat(EtatUtilisateur.ACTIF,utilisateurDTO.id);
	}
}
