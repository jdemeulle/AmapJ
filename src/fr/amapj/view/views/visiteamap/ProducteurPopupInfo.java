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
 package fr.amapj.view.views.visiteamap;

import com.vaadin.ui.VerticalLayout;

import fr.amapj.model.models.param.paramecran.PEVisiteAmap;
import fr.amapj.model.models.param.paramecran.PEVisiteAmap.InfoProducteurModeAffichage;
import fr.amapj.service.services.listeproducteurreferent.DetailProducteurDTO;
import fr.amapj.service.services.listeproducteurreferent.DetailRequestDTO;
import fr.amapj.service.services.listeproducteurreferent.ListeProducteurReferentService;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.visiteamap.VisiteAmapDTO.Producteur;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.tools.BaseUiTools;

/**
 * Popup pour l'affichge des infos d'un producteur 
 *  
 */
public class ProducteurPopupInfo extends CorePopup
{
		
	
	private Long idProducteur;


	public ProducteurPopupInfo(Producteur producteur)
	{
		setWidth(60,960);
		setCloseable(false);
				
		idProducteur = producteur.contrats.get(0).ligneContrats.get(0).producteurId;	
	}
	
	
	protected void createButtonBar()
	{		
		addButtonBlank();
		addButton("OK", e->close());
	}
	

	protected void createContent(VerticalLayout contentLayout)
	{
		PEVisiteAmap pe = (PEVisiteAmap) new ParametresService().loadParamEcran(MenuList.VISITE_AMAP);
		DetailRequestDTO r = new DetailRequestDTO();
		r.producteurEmail=pe.producteurEmail;
		r.producteurTel1=pe.producteurTel1 ;
		r.producteurTel2=pe.producteurTel2 ;
		r.referentEmail=pe.referentEmail ;
		r.referentTel1=pe.referentTel1;
		r.referentTel2=pe.referentTel2 ;
		
		DetailProducteurDTO detailProducteurDTO = new ListeProducteurReferentService().getOneProducteur(idProducteur,r);
		
		//
		contentLayout.addStyleName("popup-visite");
		
		// Le nom du producteur
		BaseUiTools.addBandeau(contentLayout, s(detailProducteurDTO.nom), "nomproducteur");
		BaseUiTools.addEmptyLine(contentLayout);
		
		if (pe.infoProducteurModeAffichage==InfoProducteurModeAffichage.DESCRIPTION_PUIS_CONTACT)
		{
			BaseUiTools.addHtmlLabel(contentLayout, s(detailProducteurDTO.description), "");
			BaseUiTools.addEmptyLine(contentLayout);
			BaseUiTools.addBandeau(contentLayout,detailProducteurDTO.contacts,"contact");
		}
		else
		{
			BaseUiTools.addBandeau(contentLayout,detailProducteurDTO.contacts,"contact");
			BaseUiTools.addEmptyLine(contentLayout);
			BaseUiTools.addHtmlLabel(contentLayout, s(detailProducteurDTO.description), "");
		}
	}	
}
