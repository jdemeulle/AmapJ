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

import fr.amapj.service.services.archivage.ArchivageUtilisateurService;
import fr.amapj.service.services.archivage.tools.SuppressionState;
import fr.amapj.service.services.utilisateur.UtilisateurDTO;
import fr.amapj.service.services.utilisateur.UtilisateurService;
import fr.amapj.view.engine.popup.archivagepopup.SuppressionApresArchivagePopup;

/**
 * Suppression d'un utilisateur archivé 
 */
public class PopupUtilisateurSuppression extends SuppressionApresArchivagePopup
{

	private UtilisateurDTO dto;
	
	public PopupUtilisateurSuppression(UtilisateurDTO dto)
	{
		super();
		popupTitle = "Suppression d'un utilisateur archivé";
		this.dto = dto;		
	}
	
	@Override
	protected String getInfo()
	{
		return "Informations sur cet utilisateur <br/>Nom : "+dto.nom+"<br/>Prénom : "+dto.prenom+"<br/><br/>";

		
	}

	@Override
	protected String computeSuppressionLib()
	{
		return new ArchivageUtilisateurService().computeSuppressionLib(param);
	}
	
	@Override
	protected SuppressionState computeSuppressionState()
	{
		return new ArchivageUtilisateurService().computeSuppressionState(dto.id, param);
	}
	
	@Override
	protected void suppressElement()
	{
		new UtilisateurService().deleteUtilisateur(dto.id);
	}
}

