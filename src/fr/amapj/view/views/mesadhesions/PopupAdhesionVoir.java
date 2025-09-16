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
 package fr.amapj.view.views.mesadhesions;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;

import fr.amapj.service.services.edgenerator.pdf.PGBulletinAdhesion;
import fr.amapj.service.services.mesadhesions.AdhesionDTO;
import fr.amapj.view.engine.excelgenerator.LinkCreator;
import fr.amapj.view.engine.popup.okcancelpopup.OKCancelPopup;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;

/**
 * Popup pour visualiser son adhesion
 *  
 */
public class PopupAdhesionVoir extends OKCancelPopup
{		
	private AdhesionDTO dto;

	private TextField textField;	
	
	/**
	 * 
	 */
	public PopupAdhesionVoir(AdhesionDTO adhesionDTO)
	{
		popupTitle = "Visualiser mon adhésion à l'AMAP";
		this.dto = adhesionDTO;

		saveButtonTitle = "OK";
		hasCancelButton = false;
			
		// 
		setWidth(40, 450);
	}
	
	@Override
	protected void createContent(VerticalLayout contentLayout)
	{
		// 
		String entete = getEntete();
		Label l = new Label(entete,ContentMode.HTML);
		l.addStyleName(ChameleonTheme.LABEL_BIG);
		contentLayout.addComponent(l);
			
		//
		String bas = getBasPage();
		l = new Label(bas,ContentMode.HTML);
		l.addStyleName(ChameleonTheme.LABEL_BIG);
		contentLayout.addComponent(l);
		
		addLinkImpressionBulletin(contentLayout);
		
	}

	

	@Override
	protected boolean performSauvegarder()
	{
		// Do nothing
		return true;
	}

	private String getEntete()
	{
		String str = "";
		
		str = 	"Vous avez renouvelé votre adhésion avec un montant de <b>"+
				new CurrencyTextFieldConverter().convertToString(dto.montantAdhesion)+
				" €</b><br/>";
						
		return str;
	}
	
	private String getBasPage()
	{
		String str = "";
		
		if (dto.libCheque!=null)
		{
			str = str + "Ordre du chèque : "+s(dto.libCheque)+"<br/><br/>";
		}
		
		if (dto.textPaiement!=null)
		{
			str = str +s(dto.textPaiement)+"<br/>";
		}		
		return str;
	}


	/**
	 * Ajoute si cela est nécessaire le lien vers l'impression des bulletins d'adhesion
	 */
	private void addLinkImpressionBulletin(VerticalLayout vl)
	{
		// Si il n'y a pas de modele de bulletin : on ne met pas le lien 
		if (dto.idBulletin==null)
		{
			return;
		}
		
		vl.addComponent(new Label("<br/>",ContentMode.HTML));
		
		Link l = LinkCreator.createLink(PGBulletinAdhesion.oneBulletinCreated(dto.idPeriode, dto.idPeriodeUtilisateur));
		l.setCaption("Imprimer mon bulletin d'adhésion");
		l.setStyleName("adhesion");
		
		vl.addComponent(l);
	}

}
