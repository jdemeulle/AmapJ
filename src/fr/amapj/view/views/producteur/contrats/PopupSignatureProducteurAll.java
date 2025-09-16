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
 package fr.amapj.view.views.producteur.contrats;

import java.util.List;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.service.services.docengagement.signonline.DocEngagementSignOnLineDTO;
import fr.amapj.service.services.docengagement.signonline.core.CoreDocEngagementSignOnLineService;
import fr.amapj.service.services.edgenerator.zip.ZGAllDocEngagementSigne;
import fr.amapj.service.services.gestioncontrat.ModeleContratSummaryDTO;
import fr.amapj.view.engine.excelgenerator.LinkCreator;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;

/**
 * Popup pour la signature des contrats par le producteur, en une fois  
 *  
 */
public class PopupSignatureProducteurAll extends CorePopup
{	
	private CheckBox cb;
	private List<DocEngagementSignOnLineDTO> dtos;
	private Long userId;
	private VerticalLayout contentLayout;
	
	// dtos a toujours une taille supérieur ou égale à 2 
	public PopupSignatureProducteurAll(ModeleContratSummaryDTO mc, List<DocEngagementSignOnLineDTO> dtos,Long userId)
	{
		super();
		if (dtos.size()<2)
		{
			throw new AmapjRuntimeException();
		}
		this.userId = userId;
		this.dtos = dtos;
		popupTitle = "Signature des documents pour le contrat "+mc.nom;
		setWidth(50);
	}

	protected void createContent(VerticalLayout contentLayout)
	{	
		this.contentLayout = contentLayout;
		
		addLabel("Vous avez "+dtos.size()+" documents à signer."); 
		
		addLabel("");
		
		addLabel("Merci de cliquer sur le lien ci dessous pour télécharger les "+dtos.size()+" documents à signer, puis de cliquer ensuite sur la case à cocher \"J'ai lu et je signe ces "+dtos.size()+" documents\"");
		
		addLabel("");
		
		ZGAllDocEngagementSigne zg = new ZGAllDocEngagementSigne(dtos);
		zg.setNameToDisplaySuffix(" à signer");
		contentLayout.addComponent(LinkCreator.createLink(zg));
		
		addLabel("");
	
		cb = new CheckBox("J'ai lu et je signe ces "+dtos.size()+" documents");
		cb.setValue(false);
		cb.addStyleName("signature");
		contentLayout.addComponent(cb);
		
		addLabel("");
		
	}

	
	private void addLabel(String str) 
	{
		Label l = new Label(str);
		contentLayout.addComponent(l);	
	}

	protected void createButtonBar()
	{
		addButtonBlank();
		addButton("Annuler", e->handleAnnuler());
		addDefaultButton("Sauvegarder", e->	handleSauvegarder());
	}
	

	protected void handleAnnuler()
	{
		close();
	}
	
	
	public void handleSauvegarder()
	{
		if (cb.getValue()==false)
		{
			new MessagePopup("Validation demandée", ContentMode.HTML,ColorStyle.RED,"Vous devez signer ces documents en cliquant sur la case à cocher \"J'ai lu et je signe tous ces documents\"").open();
			return;
		}
		
		dtos.forEach(e->new CoreDocEngagementSignOnLineService().saveDocEngagementSigneByProducteur(e.idContrat, userId));
		
		close();
	}
}
