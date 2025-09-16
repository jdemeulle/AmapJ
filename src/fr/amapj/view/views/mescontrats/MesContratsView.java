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
 package fr.amapj.view.views.mescontrats;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.model.models.param.ChoixOuiNon;
import fr.amapj.model.models.param.paramecran.PEMesContrats;
import fr.amapj.service.services.docengagement.signonline.DocEngagementSignOnLineDTO;
import fr.amapj.service.services.docengagement.signonline.DocEngagementSignOnLineService;
import fr.amapj.service.services.mescontrats.small.SmallContratDTO;
import fr.amapj.service.services.mescontrats.small.SmallContratsService;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.session.SessionManager;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.popup.PopupListener;
import fr.amapj.view.engine.template.FrontOfficeView;
import fr.amapj.view.engine.tools.BaseUiTools;


/**
 * Page permettant à l'utilisateur de gérer ses contrats
 * 
 */
public class MesContratsView extends FrontOfficeView implements  PopupListener
{
	
	static public String LABEL_RUBRIQUE = "rubrique";
	static public String LABEL_TITRECONTRAT = "titrecontrat";
	static public String PANEL_UNCONTRAT = "uncontrat";
	static public String BUTTON_PRINCIPAL = "principal";
	
	private PEMesContrats peMesContrats;
	
	@Override
	public String getMainStyleName()
	{
		return "contrat";
	}
	
	/**
	 * 
	 */
	@Override
	public void enter()
	{	
		peMesContrats = (PEMesContrats) new ParametresService().loadParamEcran(MenuList.MES_CONTRATS);
		refresh();
	}

	
	/**
	 * Ajoute un label sur toute la largeur à la ligne indiquée
	 */
	private Label addLabel(VerticalLayout layout, String str)
	{
		Label tf = new Label(str);
		tf.addStyleName(LABEL_RUBRIQUE);
		layout.addComponent(tf);
		return tf;
		
	}

	public void refresh()
	{
		this.removeAllComponents();
		InscriptionButtonModule inscriptionButtonModule = new InscriptionButtonModule(this);
		
		// Récupération des données
		Long userId = SessionManager.getUserId();
		List<SmallContratDTO> mesContratsDTO = new SmallContratsService().getMesContrats(userId);
		List<DocEngagementSignOnLineDTO> docEngagements = new DocEngagementSignOnLineService().getAllContratASignerByAmapien(userId);
		
		
		// Bloc 0 - la liste des documents d'engagement à signer 
		if (docEngagements.size()>0)
		{
			addBlocDocEngagement(docEngagements,userId);
		}
		
		
		// Bloc 1 - les nouveaux contrats - tri par date de fin des inscription puis par nom - Les cartes prépayées sont en dernier  
		Comparator<SmallContratDTO> cmp = Comparator.comparing( (SmallContratDTO e)->e.dateFinInscription, Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(e->e.nom);
		List<SmallContratDTO> newContrats = mesContratsDTO.stream().filter(e->e.contratId==null && e.inscriptionDTO.isRetardataire==false).sorted(cmp).collect(Collectors.toList());
		addOneBloc("Les nouveaux contrats disponibles",newContrats,inscriptionButtonModule);
		
		// Bloc 2 - Les contrats accessibles en tant que retardataire - même tri que les nouveaux contrats
		if (peMesContrats.affichageContratRetardataire==ChoixOuiNon.OUI)
		{
			List<SmallContratDTO> contratRetardataires = mesContratsDTO.stream().filter(e->e.contratId==null && e.inscriptionDTO.isRetardataire==true).sorted(cmp).collect(Collectors.toList());
			if (contratRetardataires.size()>0)
			{
				addOneBloc("Contrats en cours, encore disponibles",contratRetardataires,inscriptionButtonModule);
			}
		}

		// Bloc 3 - Les contrats déjà signés  
		List<SmallContratDTO> existingContrats = mesContratsDTO.stream().filter(e->e.contratId!=null).sorted(Comparator.comparing(e->e.dateFin)).collect(Collectors.toList()); 
		addOneBloc("Mes contrats existants",existingContrats,inscriptionButtonModule);
		
		// Le bouton pour télécharger les contrats
		if (existingContrats.size()>0)
		{
			Button telechargerButton = new Button("Télécharger les documents relatifs à mes contrats ...");
			telechargerButton.setIcon(FontAwesome.PRINT);
			telechargerButton.addStyleName("borderless");
			telechargerButton.addStyleName("large");
			telechargerButton.addClickListener(e->handleTelecharger(existingContrats));
					
			this.addComponent(telechargerButton);
			this.setComponentAlignment(telechargerButton, Alignment.MIDDLE_LEFT);
		}
		
		endOfRefresh();
	}
	

	
	/**
	 * Bloc sur des documents d'engagements à signer 
	 * @param userId 
	 */
	private void addBlocDocEngagement(List<DocEngagementSignOnLineDTO> docEngagements, Long userId) 
	{
		// Le titre
		addLabel(this,"🔔 Les documents d'engagement à signer");
		
		Panel p = new Panel();
		p.addStyleName(PANEL_UNCONTRAT);
		
		HorizontalLayout hl = new HorizontalLayout();
		hl.setMargin(true);
		hl.setSpacing(true);
		hl.setWidth("100%");
		
		VerticalLayout vl = new VerticalLayout();
		BaseUiTools.addHtmlLabel(vl, getLibelleEngagement(docEngagements), "libelle-contrat");
		
		hl.addComponent(vl);
		hl.setExpandRatio(vl, 1);
		
		VerticalLayout vl2 = new VerticalLayout();
		vl2.setWidth("115px");
		vl2.setSpacing(true);	
		

		Button b = new Button("Signer");
		b.setWidth("100%");
		b.addStyleName("signature");
		b.setIcon(FontAwesome.PENCIL);
		b.addClickListener(e ->	handleSignature(userId));
		vl2.addComponent(b);
		
		
		hl.addComponent(vl2);
		hl.setComponentAlignment(vl2, Alignment.MIDDLE_CENTER);		
		
		p.setContent(hl);
		
		this.addComponent(p);

		
	}

	private void handleSignature(Long userId) 
	{
		new PopupSignatureAmapienOneByOne(userId).open(()->refresh());
	}

	private String getLibelleEngagement(List<DocEngagementSignOnLineDTO> docEngagements) 
	{
		String str = "Vous avez "+docEngagements.size()+" documents d'engagement à signer en ligne pour les contrats suivants : ";
		if (docEngagements.size()==1)
		{
			str = "Vous avez un document d'engagement à signer en ligne pour le contrat suivant : ";
		}
		str = str+"<ul>";
		for (DocEngagementSignOnLineDTO eng : docEngagements) 
		{
			str = str+"<li>"+s(eng.nomModeleContrat)+"</li>";
		}
		str = str+"</ul>";
		return str;
	}

	private void addOneBloc(String lib,List<SmallContratDTO> contrats,InscriptionButtonModule inscriptionButtonModule)
	{
		// Le titre
		addLabel(this,lib);
	
		
		// la liste des contrats  
		for (SmallContratDTO c : contrats)
		{
			Panel p = new Panel();
			p.addStyleName(PANEL_UNCONTRAT);
			
			HorizontalLayout hl = new HorizontalLayout();
			hl.setMargin(true);
			hl.setSpacing(true);
			hl.setWidth("100%");
			
			VerticalLayout vl = new VerticalLayout();
			Label lab = new Label(c.nom);
			lab.addStyleName(LABEL_TITRECONTRAT);
			vl.addComponent(lab);
						
			BaseUiTools.addHtmlLabel(vl, c.inscriptionDTO.libContrat, "libelle-contrat");
			
			hl.addComponent(vl);
			hl.setExpandRatio(vl, 1);
			
			inscriptionButtonModule.buildBloc(hl, c);
			
			p.setContent(hl);
			
			this.addComponent(p);
			
		}
	}

	private void handleTelecharger(List<SmallContratDTO> existingContrats)
	{
		new TelechargerMesContrat().displayPopupTelechargerMesContrat(peMesContrats,existingContrats, this);
	}


	@Override
	public void onPopupClose()
	{
		refresh();
		
	}


}
