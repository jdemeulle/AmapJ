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
 package fr.amapj.view.views.gestioncontrat.listpart;

import fr.amapj.common.FormatUtils;
import fr.amapj.model.models.contrat.modele.EtatModeleContrat;
import fr.amapj.service.services.archivage.ArchivageContratService;
import fr.amapj.service.services.archivage.tools.ArchivableState;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.gestioncontrat.ModeleContratSummaryDTO;
import fr.amapj.view.engine.popup.archivagepopup.ArchivagePopup;

/**
 * Archivage d'un contrat
 */
public class PopupContratArchiver extends ArchivagePopup
{

	private ModeleContratSummaryDTO mcDto;
	
	public PopupContratArchiver(ModeleContratSummaryDTO mcDto)
	{
		super();
		popupTitle = "Archivage d'un contrat";
		this.mcDto = mcDto;		
	}
	
	protected String getInfo()
	{
		return 	"Informations sur ce contrat<br/>"+
				"Nom du contrat : "+mcDto.nom+"<br/>"+
				"Nom du producteur : "+mcDto.nomProducteur+"<br/>"+
				"Date de dernière livraison"+FormatUtils.getStdDate().format(mcDto.dateFin);
	}

	protected String computeArchivageLib()
	{
		return new ArchivageContratService().computeArchivageLib(param);
	}
	
	protected ArchivableState computeArchivageState()
	{
		return new ArchivageContratService().computeArchivageState(mcDto.id, param);
	}
	
	protected void archiveElement()
	{
		new GestionContratService().updateEtat(EtatModeleContrat.ARCHIVE,mcDto.id);
	}
}

