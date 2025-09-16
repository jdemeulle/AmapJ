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


import fr.amapj.model.models.fichierbase.EtatUtilisateur;
import fr.amapj.service.services.archivage.ArchivageUtilisateurService;
import fr.amapj.service.services.archivage.tools.ArchivableState;
import fr.amapj.service.services.utilisateur.UtilisateurDTO;
import fr.amapj.service.services.utilisateur.UtilisateurService;
import fr.amapj.view.engine.popup.archivagepopup.ArchivagePopup;

/**
 * Archivage d'un utilisateur
 */
public class PopupUtilisateurArchiver extends ArchivagePopup
{

	private UtilisateurDTO dto;
	
	public PopupUtilisateurArchiver(UtilisateurDTO dto)
	{
		super();
		popupTitle = "Archivage d'un utilisateur";
		this.dto = dto;		
	}
	
	protected String getInfo()
	{
		return "Informations sur cet utilisateur<br/>Nom : "+dto.nom+"<br/>Prénom : "+dto.prenom+"<br/>"+PopupUtilisateurVoirPart.getInfoUtilisateur(dto);
	}

	protected String computeArchivageLib()
	{
		return new ArchivageUtilisateurService().computeArchivageLib(param);
	}
	
	protected ArchivableState computeArchivageState()
	{
		return new ArchivageUtilisateurService().computeArchivageState(dto.id, param);
	}
	
	protected void archiveElement()
	{
		new UtilisateurService().updateEtat(EtatUtilisateur.INACTIF, dto.id);
	}
}
