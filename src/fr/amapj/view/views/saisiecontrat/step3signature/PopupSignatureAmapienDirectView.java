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
 package fr.amapj.view.views.saisiecontrat.step3signature;

import java.io.ByteArrayInputStream;

import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.whitestein.vaadin.widgets.wtpdfviewer.WTPdfViewer;

import fr.amapj.service.services.edgenerator.pdf.PGEngagement;
import fr.amapj.service.services.edgenerator.pdf.PGEngagement.PGEngagementMode;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.mescontrats.DocEngagementDTO;
import fr.amapj.service.services.session.SessionManager;
import fr.amapj.view.engine.excelgenerator.LinkCreator;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;
import fr.amapj.view.engine.tools.BaseUiTools;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.SaisieContratData;

/**
 * Popup pour la signature du contrat, avec visualisation directe du pdf 
 * 
 * Il y a un bug avec Safari, qui ne supporte pas la visualisation du PDF 
 * Dans ce cas, on passe en mode dégradé, avec uniquement un lien pour telecharger le fichier
 *  
 */
public class PopupSignatureAmapienDirectView extends CorePopup
{
	private SaisieContratData data;
	
	private CheckBox cb;

	private PGEngagement generator;

	private byte[] pdfContent;
	
	private boolean standardMode;
	
	
	public PopupSignatureAmapienDirectView(SaisieContratData data)
	{
		super();
		this.standardMode = computeMode();
		this.data = data;
		popupTitle = "Signature du document d'engagement "+data.contratDTO.nom;
		ContratDTO dto = data.contratDTO;
		generator = new PGEngagement(PGEngagementMode.CONTRAT_A_SIGNER, dto.modeleContratId, null, null, dto, data.userId);
		
		if (standardMode)
		{
			setHeight("100%");
			setWidth(0,1200);
		}
	}

	private boolean computeMode() 
	{
		if (UI.getCurrent().getPage().getWebBrowser().isSafari())
		{
			return false;
		}
		return true;
	}

	protected void createContent(VerticalLayout contentLayout)
	{
		// Generation du contenu du pdf 
		pdfContent = generator.getByteArrayContent();
		
		if (standardMode)
		{	
			// Attention, il y a un bug dans le viewer, il est interdit de mettre des espaces dans le nom du fichier  
			WTPdfViewer pdfViewer = new WTPdfViewer();
			StreamResource sr = new StreamResource(()->new ByteArrayInputStream(pdfContent), "doc-engagement.pdf");
			sr.setCacheTime(0); 
			pdfViewer.setResource(sr);
		
			//
			int height = BaseUiTools.getHeight()-170;
			pdfViewer.setHeight(height+"px");
			pdfViewer.setWidth("100%");
			
			//
			contentLayout.addComponent(pdfViewer);
		}
		else
		{
			contentLayout.addComponent(LinkCreator.createLink("doc-engagement.pdf", "Télécharger le document", pdfContent));
		}
		
		// 
		if (BaseUiTools.isCompactMode())
		{
			contentLayout.addComponent(new Label(""));
			cb = new CheckBox("J'ai lu et je signe ce document d'engagement");
			cb.setValue(false);
			cb.addStyleName("signature");
			contentLayout.addComponent(cb);
		}
		else
		{
			// On laisse un espace en bas entre le pdf et la checkbox "jai lu et je signe" 
			HorizontalLayout hl = new HorizontalLayout();
			hl.setHeight("25px");
			contentLayout.addComponent(hl);
		}
	}
	

	protected void createButtonBar()
	{
		if (BaseUiTools.isCompactMode())
		{
			addButtonBlank();
		}
		else
		{
			cb = new CheckBox("J'ai lu et je signe ce document d'engagement");
			cb.setValue(false);
			cb.addStyleName("signature");
			popupButtonBarLayout.addComponent(cb);
			popupButtonBarLayout.setExpandRatio(cb, 1f);
			popupButtonBarLayout.setComponentAlignment(cb, Alignment.MIDDLE_CENTER);
		}
		
		addButton("Annuler", e->handleAnnuler());
		addDefaultButton("Valider", e->	handleSauvegarder());
	}
	

	protected void handleAnnuler()
	{
		close();
	}
	
	
	public void handleSauvegarder()
	{
		if (cb.getValue()==false)
		{
			new MessagePopup("Signature demandée", ContentMode.HTML,ColorStyle.RED,"Vous devez signer ce document d'engagement en cliquant sur la case à cocher \"J'ai lu et je signe ce document d'engagement\"").open();
			return;
		}
		
		DocEngagementDTO dto = new DocEngagementDTO();
		dto.pdfContent = pdfContent;
		dto.vars = generator.getUsedVars();
		
		data.contratDTO.docEngagementDTO = dto;
		
		data.validate();
		close();
	}
	
}
