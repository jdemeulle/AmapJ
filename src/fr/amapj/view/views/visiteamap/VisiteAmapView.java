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

import java.text.SimpleDateFormat;
import java.util.List;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.common.FormatUtils;
import fr.amapj.model.models.param.paramecran.PEVisiteAmap;
import fr.amapj.service.services.mescontrats.small.SmallContratDTO;
import fr.amapj.service.services.meslivraisons.QteProdDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.session.SessionManager;
import fr.amapj.service.services.visiteamap.VisiteAmapDTO;
import fr.amapj.service.services.visiteamap.VisiteAmapDTO.Contrat;
import fr.amapj.service.services.visiteamap.VisiteAmapDTO.Jour;
import fr.amapj.service.services.visiteamap.VisiteAmapDTO.LigneContrat;
import fr.amapj.service.services.visiteamap.VisiteAmapDTO.Producteur;
import fr.amapj.service.services.visiteamap.VisiteAmapService;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.template.FrontOfficeView;
import fr.amapj.view.engine.tools.BaseUiTools;
import fr.amapj.view.views.common.gapviewer.AbstractGapViewer;
import fr.amapj.view.views.common.gapviewer.GapViewerUtil;
import fr.amapj.view.views.mescontrats.InscriptionButtonModule;


/**
 * Viste de l'AMAP et des produits proposés
 *
 */
@SuppressWarnings("serial")
public class VisiteAmapView extends FrontOfficeView
{
	private VerticalLayout mainVerticalLayout;	
	private AbstractGapViewer semaineViewer;
	
	private SimpleDateFormat df1 = FormatUtils.getFullDate();
	
	private boolean compactMode = false;
	private Button toggleButtonCompactMode;

	
	@Override
	public String getMainStyleName()
	{
		return "visite";
	}

	/**
	 * 
	 */
	@Override
	public void enter()
	{
		PEVisiteAmap peVisiteAmap = (PEVisiteAmap) new ParametresService().loadParamEcran(MenuList.VISITE_AMAP);
		
		semaineViewer = GapViewerUtil.createGapWiever(peVisiteAmap.modeAffichage, ()->refresh());
		addComponent(semaineViewer.getComponent());
		
		VerticalLayout central = new VerticalLayout();
		addComponent(central);
		
		mainVerticalLayout = new VerticalLayout();
		central.addComponent(mainVerticalLayout);
		
		refresh();
	}

	
	private void refresh()
	{
		mainVerticalLayout.removeAllComponents();
		VisiteAmapDTO res = new VisiteAmapService().getAll(semaineViewer.getDateDebut(),semaineViewer.getDateFin(),SessionManager.getUserId());
		
		List<Jour> jours = res.jours;
		for (int i = 0; i < jours.size(); i++)
		{
			Jour jour = jours.get(i);
			displayBlocDate(jour,i==0);
		}
		
		endOfRefresh();
	}
	
	
	
		
	private void displayBlocDate(Jour jour, boolean displayButtonCompactMode) 
	{
		if (displayButtonCompactMode)
		{
			HorizontalLayout hl = new HorizontalLayout();
			hl.setMargin(false);
			hl.setSpacing(false);
			hl.setWidth("100%");
			
			Label l1 = BaseUiTools.addStdLabel(hl,df1.format(jour.dateLiv),"dateliv");
			hl.setExpandRatio(l1, 1);
			
			toggleButtonCompactMode = new Button("Vue compacte");
			toggleButtonCompactMode.addClickListener(e->toggleCompactMode());
			updateCaptionOfButtonCompactMode();
			
			hl.addComponent(toggleButtonCompactMode);	
			
			mainVerticalLayout.addComponent(hl);
		}
		else
		{
			BaseUiTools.addStdLabel(mainVerticalLayout,df1.format(jour.dateLiv),"dateliv");
		}
		
		// On affiche chaque bloc PRODUCTEUR
		for (Producteur producteur : jour.producteurs) 
		{
			displayBlocProducteur(producteur);
		}	
	}	
	
	private void toggleCompactMode()
	{
		compactMode = !compactMode;
		refresh();
	}

	private void updateCaptionOfButtonCompactMode()
	{
		if (compactMode)
		{
			toggleButtonCompactMode.setCaption("Vue détaillée");	
		}
		else
		{
			toggleButtonCompactMode.setCaption("Vue compacte");
		}
	}

	private void displayBlocProducteur(Producteur producteur) 
	{
		VerticalLayout vl = BaseUiTools.addPanel(mainVerticalLayout, "unproducteur");
		if (compactMode==false)
		{
			HorizontalLayout hl = new HorizontalLayout();
			hl.setWidth("100%");
			Label l = BaseUiTools.addStdLabel(hl, producteur.producteurNom, "nomproducteur");
			hl.setExpandRatio(l, 1f);
			Button btn = new Button();
			btn.setIcon(FontAwesome.QUESTION_CIRCLE);
			btn.addStyleName("more-info");
			btn.addStyleName("question-mark");
			btn.addClickListener(e->handleBtnInfo(producteur));
			hl.addComponent(btn);
			vl.addComponent(hl);
		}
		
		// On affiche chaque bloc CONTRAT
		for (Contrat contrat : producteur.contrats) 
		{
			displayBlocContrat(contrat,vl,producteur.producteurNom);
		}	
	}


	private void displayBlocContrat(Contrat contrat, VerticalLayout vl,String producteurNom) 
	{
		InscriptionButtonModule inscriptionButtonModule = new InscriptionButtonModule(()->refresh());
		
		if (compactMode)
		{
			BaseUiTools.addBandeau(vl, s(producteurNom) +" - "+s(contrat.contratNom), "nomcontrat");
		}
		else
		{
			BaseUiTools.addBandeau(vl, s(contrat.contratNom), "nomcontrat");
		}
		
		HorizontalLayout hl = new HorizontalLayout();
		hl.setMargin(true);
		hl.setSpacing(true);
		hl.setWidth("100%");
		hl.setStyleName("contrat-bloc");
		
		VerticalLayout vl1 = new VerticalLayout();
		
		if (compactMode==false)
		{
			String propose = formatContratPropose(contrat);
			BaseUiTools.addHtmlLabel(vl1, propose, "ligproduit");
		}
				
		String contenuMonContrat = formatMonContrat(contrat);
		BaseUiTools.addHtmlLabel(vl1, contenuMonContrat, "ligproduit");
		
		
		hl.addComponent(vl1);
		hl.setComponentAlignment(vl1, Alignment.MIDDLE_RIGHT);
		hl.setExpandRatio(vl1, 1);
		
		if (contrat.contratDTO!=null)
		{
			inscriptionButtonModule.buildBloc(hl, contrat.contratDTO);
		}
		
		vl.addComponent(hl);
			
	}

	private String formatContratPropose(Contrat contrat)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Ce contrat propose :<ul>");
		for (LigneContrat lig : contrat.ligneContrats) 
		{
			sb.append("<li>"+s(lig.produitNom)+", "+s(lig.produitConditionnement)+" - Prix : "+FormatUtils.prix(lig.produitPrix)+"</li>");
		}
		sb.append("</ul>");
		return sb.toString();
	}

	private String formatMonContrat(Contrat contrat)
	{
		SmallContratDTO m = contrat.contratDTO;
		if (m==null)
		{
			return  "Vous ne pouvez plus vous inscrire en ligne sur ce contrat, contactez le référent.";
		}
	
		if (m.contratId==null)
		{
			if (m.inscriptionDTO.isRetardataire)
			{
				return "Vous n'êtes pas inscrit sur ce contrat mais vous pouvez vous inscrire en tant que nouvel arrivant / retardataire";
			}
			else
			{
				return "Vous n'êtes pas inscrit sur ce contrat";
			}
		}
		
		List<QteProdDTO> qteProdDTOs = contrat.qteProdDTOs;
		if (qteProdDTOs.size()==0)
		{
			return "Vous êtes inscrit sur ce contrat mais vous n'avez rien pris ce jour là.";
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("Vous êtes inscrit sur ce contrat. Vous avez pris :<ul>");
		for (QteProdDTO lig : qteProdDTOs) 
		{
			sb.append("<li>"+lig.qte+" "+s(lig.nomProduit)+", "+s(lig.conditionnementProduit)+"</li>");
		}
		sb.append("</ul>");
		return sb.toString();
		
	}
	
	private void handleBtnInfo(Producteur producteur)
	{
		new ProducteurPopupInfo(producteur).open();
	}

}
