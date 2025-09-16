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
 package fr.amapj.view.views.archivage.contrat;

import fr.amapj.common.FormatUtils;
import fr.amapj.service.services.archivage.ArchivageContratService;
import fr.amapj.service.services.archivage.tools.SuppressionState;
import fr.amapj.service.services.gestioncontrat.ModeleContratSummaryDTO;
import fr.amapj.view.engine.popup.archivagepopup.SuppressionApresArchivagePopup;

/**
 * Suppression d'un contrat archivé 
 */
public class PopupContratSuppression extends SuppressionApresArchivagePopup
{

	private ModeleContratSummaryDTO mcDto;
	
	public PopupContratSuppression(ModeleContratSummaryDTO mcDto)
	{
		super();
		popupTitle = "Suppression d'un contrat archivé";
		this.mcDto = mcDto;		
	}
	
	@Override
	protected String getInfo()
	{
		return 	"Informations sur ce contrat<br/>"+
				"Nom du contrat : "+mcDto.nom+"<br/>"+
				"Nom du producteur : "+mcDto.nomProducteur+"<br/>"+
				"Date de dernière livraison"+FormatUtils.getStdDate().format(mcDto.dateFin)+"<br/><br/>"+
				"Avec cet outil, vous allez pouvoir supprimer complètement ce contrat vierge et tous les contrats signés associés.";
		
	}

	@Override
	protected String computeSuppressionLib()
	{
		return new ArchivageContratService().computeSuppressionLib(param);
	}
	
	@Override
	protected SuppressionState computeSuppressionState()
	{
		return new ArchivageContratService().computeSuppressionState(mcDto, param);
	}
	
	@Override
	protected void suppressElement()
	{
		new ArchivageContratService().deleteModeleContratAndContrats(mcDto.id);
	}
}

