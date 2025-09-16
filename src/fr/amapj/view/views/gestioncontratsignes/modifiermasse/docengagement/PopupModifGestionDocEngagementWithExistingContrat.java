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
 package fr.amapj.view.views.gestioncontratsignes.modifiermasse.docengagement;

import java.util.Arrays;
import java.util.List;

import com.vaadin.ui.ComboBox;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.engine.metadata.MetaDataEnum;
import fr.amapj.model.engine.metadata.MetaDataEnum.HelpInfo;
import fr.amapj.model.models.contrat.modele.GestionDocEngagement;
import fr.amapj.service.services.docengagement.signonline.DocEngagementSignOnLineDTO;
import fr.amapj.service.services.docengagement.signonline.DocEngagementSignOnLineService;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.gestioncontrat.ModeleContratDTO;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.popup.formpopup.fieldlink.FieldLink;
import fr.amapj.view.engine.popup.formpopup.validator.NotNullValidator;
import fr.amapj.view.views.searcher.SearcherList;

/**
 * Permet la modification de la gestion de la signature en ligne des documents d'engagement,
 * alors qu'il y a déjà des contrats existants 
 */
public class PopupModifGestionDocEngagementWithExistingContrat extends WizardFormPopup
{
	private ModeleContratDTO modeleContrat;
	private GestionDocEngagement currentGestionDocEngagement;
	private Long currentIdEngagement;

	@Override
	protected void configure()
	{
		add(()->drawIntro());
		add(()->drawChoixAction(),()->checkChoixAction());
	}
	
	/**
	 * 
	 */
	public PopupModifGestionDocEngagementWithExistingContrat(Long id)
	{
		super();
		popupTitle = "Modification de la gestion des documents d'engagement";
		setWidth(80);
				
		// Chargement de l'objet  à modifier
		modeleContrat = new GestionContratService().loadModeleContrat(id);
		setModel(modeleContrat);
		
		currentGestionDocEngagement = modeleContrat.gestionDocEngagement;
		currentIdEngagement = modeleContrat.idEngagement;
	}
	
	private void drawIntro() 
	{
		setStepTitle("Introduction");
		
		addText("Cet outil permet de modifier les régles de gestion des documents d'engagement pour ce contrat.");
		
		addText("L'outil vous guidera pas à pas et vous indiquera à l'avance les opérations qui seront réalisées.");
		
		HelpInfo m = MetaDataEnum.getHelpInfo(GestionDocEngagement.class);
		
		addText("Vous allez modifier le contrat "+modeleContrat.nom+", pour l'instant ce contrat utilise la régle de gestion :  "+m.getLib(modeleContrat.gestionDocEngagement));
		
		addHtml(m.getAide(modeleContrat.gestionDocEngagement));
		
	}
	
	private void drawChoixAction() 
	{
		// Titre
		setStepTitle("Saisie de la nouvelle régle de gestion des documents d'engagement pour ce contrat");
		
		ComboBox b1 = addComboEnumField("Gestion des documents d'engagement", "gestionDocEngagement",new NotNullValidator());
		
		FieldLink f1 = new FieldLink(validatorManager,Arrays.asList(GestionDocEngagement.SIGNATURE_EN_LIGNE,GestionDocEngagement.GENERATION_DOCUMENT_SEUL),b1,true);
		
		f1.addField(addSearcher("Document d'engagement (à signer) ", "idEngagement", SearcherList.ENGAGEMENT ,null,f1.getValidator()));
		
		f1.doLink();
	}
	
	private String checkChoixAction() 
	{
		switch (modeleContrat.gestionDocEngagement) 
		{
		case AUCUNE_GESTION:
			switch (currentGestionDocEngagement) 
			{
				case GENERATION_DOCUMENT_SEUL: return null;
				case SIGNATURE_EN_LIGNE : return checkNoDocs();
				case AUCUNE_GESTION : return null;
				default : throw new AmapjRuntimeException();
			}
			
		case GENERATION_DOCUMENT_SEUL:
			switch (currentGestionDocEngagement) 
			{
				case AUCUNE_GESTION: return null;
				case SIGNATURE_EN_LIGNE : return checkNoDocs();
				case GENERATION_DOCUMENT_SEUL: return null; 			
				default : throw new AmapjRuntimeException();
			}

			
		case SIGNATURE_EN_LIGNE:
			switch (currentGestionDocEngagement) 
			{
				case AUCUNE_GESTION: return null;
				case GENERATION_DOCUMENT_SEUL : return null;
				case SIGNATURE_EN_LIGNE:
					if (currentIdEngagement.equals(modeleContrat.idEngagement))
					{
						return null;
					}
					else
					{
						return checkNoDocs();
					}			
				default : throw new AmapjRuntimeException();
			}
			
		default: throw new AmapjRuntimeException();
		}
	}
	
	private String checkNoDocs() 
	{
		List<DocEngagementSignOnLineDTO> docs = new DocEngagementSignOnLineService().getBilanSignature(modeleContrat.id);
		
		long nbDocSignes = docs.stream().filter(e->e.signedByAmapien!=null).count();
		
		if (nbDocSignes==0)
		{
			return null;
		}
		return "Cette opération est impossible car ce contrat comprend "+nbDocSignes+" documents d'engagement déjà signés en ligne. Vous devez d'abord les supprimer en allant dans \"Gestion des contrats signés\", puis \"Modifier en masse\" puis \"Supprimer tous les documents d'engagement déjà signés en ligne\"."; 
	}


	protected void performSauvegarder()
	{	
		// Sauvegarde 
		new GestionContratService().updateSignatureEnLigne(modeleContrat);
	}
}
