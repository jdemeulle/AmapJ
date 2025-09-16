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

import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;

import fr.amapj.common.GenericUtils.Ret;
import fr.amapj.model.models.param.paramecran.PEMesAdhesions;
import fr.amapj.model.models.param.paramecran.PEMesAdhesions.GestionAdhesion;
import fr.amapj.service.services.edgenerator.pdf.PGBulletinAdhesion;
import fr.amapj.service.services.mesadhesions.AdhesionDTO;
import fr.amapj.service.services.mesadhesions.MesAdhesionsService;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.excelgenerator.LinkCreator;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.popup.okcancelpopup.OKCancelPopup;
import fr.amapj.view.engine.tools.BaseUiTools;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;

/**
 * Popup pour la saisie de l'adhesion
 *  
 */
public class PopupAdhesion extends OKCancelPopup
{		
	private AdhesionDTO dto;

	private State state;
	
	private TextField textField;

	
	public enum State
	{
		// L'adherent va saisir son adhesion normalement
		// L'adhesion sera créée en base de données
		SAISIE , 
				
		// On indique à l'adhérent les instructions pour qu'il remette son adhesion au tresorier
		// L'adherent ne peut pas saisir le montant de son adhesion 
		// L'adhesion ne sera pas créée en base de données
		TRESORIER
		
	}
	
	
	/**
	 * 
	 */
	public PopupAdhesion(AdhesionDTO adhesionDTO)
	{
		popupTitle = "Adhésion à l'AMAP";
		this.dto = adhesionDTO;
		this.state = computeState();
		
		
		if (state==State.SAISIE)
		{
			saveButtonTitle = "J'adhère";
		}
		else
		{
			saveButtonTitle = "OK";
			hasCancelButton = false;
		}	
		
		// 
		setWidth(40, 450);
	}
	
	private State computeState()
	{
		PEMesAdhesions peMesAdhesions = (PEMesAdhesions) new ParametresService().loadParamEcran(MenuList.MES_ADHESIONS);
		if (peMesAdhesions.gestionAdhesion==GestionAdhesion.INSCRIPTION_ADHERENTS)
		{
			return State.SAISIE;
		}
		else
		{
			return State.TRESORIER;
		}
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
		if (state==State.SAISIE)
		{
			FormLayout f = new FormLayout();
			textField = BaseUiTools.createCurrencyField("Montant de votre adhésion",false);
			textField.setConvertedValue(new Integer(getPropositionMontant()));
			textField.addStyleName("cell-saisie");
			f.addComponent(textField);
			contentLayout.addComponent(f);
		}
		
		//
		String bas = getBasPage();
		l = new Label(bas,ContentMode.HTML);
		l.addStyleName(ChameleonTheme.LABEL_BIG);
		contentLayout.addComponent(l);
		
		addLinkImpressionBulletin(contentLayout);
		
		
	}

	private int getPropositionMontant()
	{
		if (dto.idPeriodeUtilisateur!=null)
		{
			return dto.montantAdhesion;
		}
		else
		{
			return dto.montantConseille;
		}
		
	}

	@Override
	protected boolean performSauvegarder()
	{
		// Si on est en lecture seule
		if (state==State.TRESORIER)
		{
			return true;
		}
		
		Ret<Integer> qte = readMontant();
		if (qte.isOK()==false)
		{
			Notification.show(qte.msg());
			return false;
		}
		
		new MesAdhesionsService().createOrUpdateAdhesion(dto,qte.get());				
		
		return true;
	}
	
	private Ret<Integer> readMontant()
	{
		int qte = 0;
		try
		{
			Integer val = (Integer) textField.getConvertedValue();
			
			if (val != null)
			{
				qte = val.intValue();
			}
		}
		catch (ConversionException e)
		{
			return Ret.error("Erreur de saisie");
		}
		
		if (qte<dto.montantMini)
		{
			return Ret.error("Le montant est insuffisant. Le montant minimum est "+new CurrencyTextFieldConverter().convertToString(dto.montantMini)+" €");
		}
		return Ret.ok(qte);
	}
	


	private String getEntete()
	{
		String str = "";
		
		if (state==State.SAISIE)
		{
			str = "<b>Veuillez saisir ci dessous le montant de votre adhesion</b><br/>";
		}
		else
		{
			str = "<b>L'adhésion est à remettre à votre trésorier.</b><br/>";
		}
		
		str = str + "Le montant conseillé est de "+new CurrencyTextFieldConverter().convertToString(dto.montantConseille)+" €<br/>";
		
		if (dto.montantConseille!=dto.montantMini)
		{
			str = str + "Le montant minimum est de "+new CurrencyTextFieldConverter().convertToString(dto.montantMini)+" €<br/><br/>";
		}				
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
		
		Link l = LinkCreator.createLink(PGBulletinAdhesion.oneBulletinNotCreated(dto.idPeriode, dto.idUtilisateur,()->montantImpression()));
				
		l.setCaption("Imprimer mon bulletin d'adhésion");
		l.setStyleName("adhesion");
		
		vl.addComponent(l);
	}
	
	private Ret<Integer> montantImpression()
	{
		if (state==State.SAISIE)
		{
			return readMontant();
		}
		else
		{
			return Ret.ok(dto.montantConseille);
		}
	}

}
