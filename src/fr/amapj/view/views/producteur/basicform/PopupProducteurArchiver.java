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
 package fr.amapj.view.views.producteur.basicform;

import fr.amapj.model.models.fichierbase.EtatProducteur;
import fr.amapj.service.services.archivage.tools.ArchivableState;
import fr.amapj.service.services.producteur.ProducteurDTO;
import fr.amapj.service.services.producteur.ProducteurService;
import fr.amapj.view.engine.popup.archivagepopup.ArchivagePopup;

/**
 * Archivage d'un producteur
 */
public class PopupProducteurArchiver extends ArchivagePopup
{

	private ProducteurDTO producteurDTO;
	
	public PopupProducteurArchiver(ProducteurDTO p)
	{
		super();
		popupTitle = "Archivage d'un producteur";
		this.producteurDTO = p;		
	}
	
	protected String getInfo()
	{
		return "Informations sur ce producteur<br/>Nom : "+producteurDTO.nom+"<br/>"+ProducteurVoirPart.getInfoProducteur(producteurDTO);
	}

	protected String computeArchivageLib()
	{
		return new ProducteurService().computeArchivageLib(param);
	}
	
	protected ArchivableState computeArchivageState()
	{
		return new ProducteurService().computeArchivageState(producteurDTO, param);
	}
	
	protected void archiveElement()
	{
		new ProducteurService().updateEtat(producteurDTO.id, EtatProducteur.ARCHIVE);
	}
}
