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

import java.text.SimpleDateFormat;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.model.models.cotisation.EtatPaiementAdhesion;
import fr.amapj.model.models.param.paramecran.PEMesAdhesions;
import fr.amapj.model.models.param.paramecran.PEMesAdhesions.ImpressionBulletin;
import fr.amapj.service.services.edgenerator.pdf.PGBulletinAdhesion;
import fr.amapj.service.services.mesadhesions.AdhesionDTO;
import fr.amapj.service.services.mesadhesions.MesAdhesionDTO;
import fr.amapj.service.services.mesadhesions.MesAdhesionsService;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.session.SessionManager;
import fr.amapj.view.engine.excelgenerator.LinkCreator;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.popup.PopupListener;
import fr.amapj.view.engine.popup.suppressionpopup.SuppressionPopup;
import fr.amapj.view.engine.template.FrontOfficeView;
import fr.amapj.view.engine.tools.BaseUiTools;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;


/**
 * Page permettant à l'utilisateur de gérer ses adhesions
 * 
 */
public class MesAdhesionsView extends FrontOfficeView implements  PopupListener
{

	static private String LABEL_RUBRIQUE = "rubrique";
	static private String LABEL_TITRECONTRAT = "titrecontrat";
	static private String PANEL_UNCONTRAT = "uncontrat";
	static private String BUTTON_PRINCIPAL = "principal";
		
	@Override
	public String getMainStyleName()
	{
		return "mesadhesions";
	}
	
	/**
	 * 
	 */
	@Override
	public void enter()
	{
		removeAllComponents();
		MesAdhesionDTO mesAdhesionsDTO = new MesAdhesionsService().computeAdhesionInfo(SessionManager.getUserId());
		
		if (mesAdhesionsDTO.nouvelles.size()>0)
		{
			addLabel(this,"Renouvellement de votre adhésion à l'AMAP",LABEL_RUBRIQUE);
						
			for (AdhesionDTO adhesionDTO : mesAdhesionsDTO.nouvelles) 
			{
				displayAdhesion(adhesionDTO,true);
			}		
		}
		
		if (mesAdhesionsDTO.enCours.size()>0)
		{			
			addLabel(this,"Votre adhésion à l'AMAP",LABEL_RUBRIQUE);
			
			for (AdhesionDTO adhesionDTO : mesAdhesionsDTO.enCours) 
			{
				displayAdhesion(adhesionDTO,false);
			}			
		}		
		
		
		if (mesAdhesionsDTO.archives.size()>0)
		{			
			addLabel(this,"Vos adhésions passées",LABEL_RUBRIQUE);
			
			for (AdhesionDTO adhesionDTO : mesAdhesionsDTO.archives) 
			{
				displayAdhesionArchive(adhesionDTO);
			}			
		}		
		
		endOfRefresh();
	}
	

	
	private void displayAdhesion(AdhesionDTO adhesionDTO,boolean isInscription) 
	{
		Panel p = new Panel();
		p.addStyleName(MesAdhesionsView.PANEL_UNCONTRAT);
		
		HorizontalLayout hl = new HorizontalLayout();
		hl.setMargin(true);
		hl.setSpacing(true);
		hl.setWidth("100%");
		
		VerticalLayout vl = new VerticalLayout();
		addLabel(vl,"Adhésion pour "+adhesionDTO.nomPeriode,LABEL_TITRECONTRAT);
		
		String str = formatLibelleAdhesion(adhesionDTO,isInscription);
		BaseUiTools.addHtmlLabel(vl, str, "libelle-contrat");
		
		if (isInscription==false)
		{
			addLinkImpressionBulletin(adhesionDTO,vl);
		}
		
		hl.addComponent(vl);
		hl.setExpandRatio(vl, 1);
		
		VerticalLayout vl2 = new VerticalLayout();
		vl2.setWidth("115px");
		vl2.setSpacing(true);	

		hl.addComponent(vl2);
		hl.setComponentAlignment(vl2, Alignment.MIDDLE_CENTER);

		
		if (isInscription)
		{
			Button b = addButtonAdhesionAdherer("Adhérer",adhesionDTO);
			b.addStyleName(BUTTON_PRINCIPAL);
			vl2.addComponent(b);
		}
		else
		{	
			if (adhesionDTO.isModifiable)	
			{
				Button b = addButtonAdhesionAdherer("Modifier",adhesionDTO);				
				vl2.addComponent(b);				
			}
			
			if (adhesionDTO.isSupprimable)	
			{
			
				Button b = addButtonAdhesionSupprimer("Supprimer",adhesionDTO);
				vl2.addComponent(b);
			}
			
			Button v = addButtonAdhesionVoir("Voir",adhesionDTO);
			v.addStyleName(BUTTON_PRINCIPAL);
			vl2.addComponent(v);		
		}
	
		
		p.setContent(hl);
		
		addComponent(p);
		
	}
	
	private void displayAdhesionArchive(AdhesionDTO adhesionDTO) 
	{
		SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
		
		Panel p = new Panel();
		p.addStyleName(MesAdhesionsView.PANEL_UNCONTRAT);
		
		HorizontalLayout hl = new HorizontalLayout();
		hl.setMargin(true);
		hl.setSpacing(true);
		hl.setWidth("100%");
		
		
		VerticalLayout vl = new VerticalLayout();
		addLabel(vl,"Vous étiez adhérent pour "+adhesionDTO.nomPeriode,LABEL_TITRECONTRAT);
		
		String str = "Montant de l'adhésion : "+new CurrencyTextFieldConverter().convertToString(adhesionDTO.montantAdhesion)+" €<br/>"
				 + "Cette adhésion correspond à la période du "+df2.format(adhesionDTO.dateDebut)+" au "+df2.format(adhesionDTO.dateFin); 
				
		BaseUiTools.addHtmlLabel(vl, str, "libelle-contrat");
			
		hl.addComponent(vl);
		
		p.setContent(hl);
		
		addComponent(p);
	}

	

	

	@Override
	public void onPopupClose()
	{
		enter();	
	}
	
	
	
	private Button addButtonAdhesionAdherer(String str, AdhesionDTO adhesionDTO)
	{
		Button b = new Button(str);
		b.setWidth("100%");
		b.addClickListener(e ->new PopupAdhesion(adhesionDTO).open(this));
		return b;
	}
	

	private Button addButtonAdhesionVoir(String str,AdhesionDTO adhesionDTO)
	{
		Button b = new Button(str);
		b.setWidth("100%");
		b.addClickListener(e -> new PopupAdhesionVoir(adhesionDTO).open(this));
		return b;
	}
	
	
	private Button addButtonAdhesionSupprimer(String str, AdhesionDTO adhesionDTO)
	{
		Button b = new Button(str);
		b.setWidth("100%");
		b.addClickListener(e ->	handleAdhesionSupprimer(adhesionDTO));
		return b;
	}
	
	private void handleAdhesionSupprimer(AdhesionDTO adhesionDTO)
	{
		String text = "Êtes-vous sûr de vouloir supprimer votre adhésion ?";
		SuppressionPopup confirmPopup = new SuppressionPopup(text,adhesionDTO.idPeriodeUtilisateur,e->new MesAdhesionsService().deleteAdhesion(e,false));
		confirmPopup.open(this);		
	}

	
	/**
	 * Ajoute si cela est nécessaire le lien vers l'impression des bulletins d'adhesion
	 */
	private void addLinkImpressionBulletin(AdhesionDTO adhesionDTO, VerticalLayout vl)
	{
		// Si il n'y a pas de modele de bulletin : on ne met pas le lien 
		if (adhesionDTO.idBulletin==null)
		{
			return;
		}
		
		PEMesAdhesions peMesAdhesions = (PEMesAdhesions) new ParametresService().loadParamEcran(MenuList.MES_ADHESIONS);
		if (peMesAdhesions.impressionBulletinMode==ImpressionBulletin.FROM_POPUP)
		{
			return;
		}
		
		Link l = LinkCreator.createLink(PGBulletinAdhesion.oneBulletinCreated(adhesionDTO.idPeriode, adhesionDTO.idPeriodeUtilisateur));
		l.setCaption("Imprimer mon bulletin d'adhésion");
		l.setStyleName("adhesion");
		
		vl.addComponent(l);
	}

	
	
	private String formatLibelleAdhesion(AdhesionDTO adhesionDTO,boolean isInscription)
	{
		SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
		
		String str;
		//  
		if (isInscription)
		{
			str = "Il est temps d'adhérer pour la nouvelle saison !<br/>";		 
		}
		else
		{
			str = "Vous avez renouvelé votre adhésion à l'AMAP. Montant : "+new CurrencyTextFieldConverter().convertToString(adhesionDTO.montantAdhesion)+" €<br/>";
			
			if (adhesionDTO.etatPaiementAdhesion==EtatPaiementAdhesion.A_FOURNIR)
			{
				str = str+"Sauf si cela est déjà fait, le paiement est à fournir au trésorier.<br/><br/>";
			}
			else
			{
				str = str+"Votre paiement par "+adhesionDTO.typePaiementAdhesion+" a bien été réceptionné par le trésorier.<br/><br/>";
			}
		}
				
		str = str+"<b>Cette adhésion couvre la période du "+df2.format(adhesionDTO.dateDebut)+" au "+df2.format(adhesionDTO.dateFin)+"</b><br/>";
		
		return str;
	}
	
	
	/**
	 * Ajoute un label sur toute la largeur à la ligne indiquée
	 */
	private Label addLabel(VerticalLayout layout, String str,String style)
	{
		Label tf = new Label(str);
		tf.addStyleName(style);
		layout.addComponent(tf);
		return tf;
		
	}

}
