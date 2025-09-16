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
 package fr.amapj.view.views.archivage.producteur;

import fr.amapj.service.services.archivage.tools.SuppressionState;
import fr.amapj.service.services.producteur.ProducteurDTO;
import fr.amapj.service.services.producteur.ProducteurService;
import fr.amapj.view.engine.popup.archivagepopup.SuppressionApresArchivagePopup;
import fr.amapj.view.views.producteur.basicform.ProducteurVoirPart;

/**
 * Suppression d'un producteur archivé 
 */
public class PopupProducteurSuppression extends SuppressionApresArchivagePopup
{

	private ProducteurDTO dto;
	
	public PopupProducteurSuppression(ProducteurDTO dto)
	{
		super();
		popupTitle = "Suppression d'un producteur archivé";
		this.dto = dto;		
	}
	
	@Override
	protected String getInfo()
	{
		return "Informations sur ce producteur<br/>Nom : "+dto.nom+"<br/>"+ProducteurVoirPart.getInfoProducteur(dto)+"<br/><br/>"+
				"Avec cet outil, vous allez pouvoir supprimer définitivement ce producteur et les produits associés";
		
	}

	@Override
	protected String computeSuppressionLib()
	{
		return new ProducteurService().computeSuppressionLib(param);
	}
	
	@Override
	protected SuppressionState computeSuppressionState()
	{
		return new ProducteurService().computeSuppressionState(dto, param);
	}
	
	@Override
	protected void suppressElement()
	{
		new ProducteurService().deleteWithProduit(dto.id);
	}
}

