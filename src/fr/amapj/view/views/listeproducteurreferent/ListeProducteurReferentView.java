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
 package fr.amapj.view.views.listeproducteurreferent;

import java.util.List;

import com.vaadin.ui.VerticalLayout;

import fr.amapj.model.models.param.paramecran.PEProducteurReferent;
import fr.amapj.model.models.param.paramecran.PEProducteurReferent.InfoProducteurModeAffichage;
import fr.amapj.service.services.listeproducteurreferent.DetailProducteurDTO;
import fr.amapj.service.services.listeproducteurreferent.DetailRequestDTO;
import fr.amapj.service.services.listeproducteurreferent.ListeProducteurReferentService;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.template.FrontOfficeView;
import fr.amapj.view.engine.tools.BaseUiTools;


/**
 * Page permettant à l'utilisateur de visualiser tous les producteurs et les référents
 */
public class ListeProducteurReferentView extends FrontOfficeView
{
	public String getMainStyleName()
	{
		return "producteurreferent";
	}

	/**
	 * 
	 */
	@Override
	public void enter()
	{
		PEProducteurReferent pe = (PEProducteurReferent) new ParametresService().loadParamEcran(MenuList.LISTE_PRODUCTEUR_REFERENT);
		
		DetailRequestDTO r = new DetailRequestDTO();
		r.producteurEmail=pe.producteurEmail;
		r.producteurTel1=pe.producteurTel1 ;
		r.producteurTel2=pe.producteurTel2 ;
		r.referentEmail=pe.referentEmail ;
		r.referentTel1=pe.referentTel1;
		r.referentTel2=pe.referentTel2 ;
		
		
		List<DetailProducteurDTO> dtos = new ListeProducteurReferentService().getAllProducteurs(r);
		
		
		BaseUiTools.addStdLabel(this, "Liste des producteurs et des référents", "titre");
		
		for (DetailProducteurDTO detailProducteurDTO : dtos)
		{
			
			VerticalLayout vl1 = BaseUiTools.addPanel(this, "unproducteur");
			
			// Le nom du producteur
			BaseUiTools.addStdLabel(vl1, detailProducteurDTO.nom, "nomproducteur");
			BaseUiTools.addEmptyLine(vl1);
			
			if (pe.infoProducteurModeAffichage==InfoProducteurModeAffichage.DESCRIPTION_PUIS_CONTACT)
			{
				BaseUiTools.addHtmlLabel(vl1, s(detailProducteurDTO.description), "ligne");
				BaseUiTools.addEmptyLine(vl1);
				BaseUiTools.addBandeau(vl1,detailProducteurDTO.contacts,"contact");
			}
			else
			{
				BaseUiTools.addBandeau(vl1,detailProducteurDTO.contacts,"contact");
				BaseUiTools.addEmptyLine(vl1);
				BaseUiTools.addHtmlLabel(vl1, s(detailProducteurDTO.description), "ligne");
			}
		}
	}
}
