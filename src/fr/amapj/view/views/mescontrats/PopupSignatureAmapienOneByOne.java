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

import java.util.List;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.service.services.docengagement.signonline.DocEngagementSignOnLineDTO;
import fr.amapj.service.services.docengagement.signonline.DocEngagementSignOnLineService;
import fr.amapj.service.services.docengagement.signonline.core.CoreDocEngagementSignOnLineService;
import fr.amapj.service.services.edgenerator.pdf.PGEngagement;
import fr.amapj.service.services.edgenerator.pdf.PGEngagement.PGEngagementMode;
import fr.amapj.service.services.mescontrats.DocEngagementDTO;
import fr.amapj.view.engine.excelgenerator.LinkCreator;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;

/**
 * Popup pour la signature des contrats par l'amapien 
 *  
 */
public class PopupSignatureAmapienOneByOne extends CorePopup
{	
	private CheckBox cb;
	private List<DocEngagementSignOnLineDTO> dtos;
	private int index; // Numéro du contrat en cours de signature
	private VerticalLayout contentLayout;
	private DocEngagementSignOnLineDTO dto;
	private Button btnSave;
	private byte[] pdfContent;
	private PGEngagement generator;

	 
	public PopupSignatureAmapienOneByOne(Long userId)
	{
		super();
		this.dtos = new DocEngagementSignOnLineService().getAllContratASignerByAmapien(userId);
		popupTitle = "Signature des documents d'engagement";
		
		
	}

	protected void createContent(VerticalLayout contentLayout)
	{
		this.contentLayout = contentLayout;

		dto = dtos.get(index);
		
		generator = new PGEngagement(PGEngagementMode.UN_CONTRAT, dto.idModeleContrat, dto.idContrat, null, null, null);
		pdfContent = generator.getByteArrayContent();

		addLabel("");
		addLabel("Signature du document d'engagement "+(index+1)+" / "+dtos.size());
		addLabel("");
		
		contentLayout.addComponent(LinkCreator.createLink("contrat.pdf", "Télécharger le document d'engagement "+dto.nomModeleContrat, pdfContent));
		
		addLabel("");
		cb = new CheckBox("J'ai lu et je signe ce document d'engagement");
		cb.setValue(false);
		cb.addStyleName("signature");
		contentLayout.addComponent(cb);
		setLibBtnSave();
	}

	protected void createButtonBar()
	{
		addButtonBlank();
		addButton("Annuler", e->handleAnnuler());
		btnSave = addDefaultButton("", e->	handleSauvegarder());
		setLibBtnSave();	
	}
	

	private void setLibBtnSave() 
	{
		// Ce cas se produit à la création initiale du popup
		if (btnSave==null)
		{
			return;
		}
		btnSave.setCaption(index == dtos.size()-1 ? "Terminer" : "Continuer ...");
	}
	

	protected void handleAnnuler()
	{
		close();
	}
	
	
	public void handleSauvegarder()
	{
		if (cb.getValue()==false)
		{
			new MessagePopup("Validation demandée", ContentMode.HTML,ColorStyle.RED,"Vous devez signer ce document d'engagement en cliquant sur la case à cocher \"J'ai lu et je signe ce document d'engagement\"").open();
			return;
		}
		
		DocEngagementDTO docEngagementDTO = new DocEngagementDTO();
		docEngagementDTO.pdfContent = pdfContent;
		docEngagementDTO.vars = generator.getUsedVars();
		
		new CoreDocEngagementSignOnLineService().saveDocEngagementSigneByAmapien(docEngagementDTO,dto.idContrat);
		
		
		if (index==dtos.size()-1)
		{
			close();
		}
		else
		{
			contentLayout.removeAllComponents();
			index++;
			createContent(contentLayout);
		}
	}
	
	

	private void addLabel(String str) 
	{
		Label l = new Label(str);
		contentLayout.addComponent(l);	
	}
}
