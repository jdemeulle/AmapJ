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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.DateUtils;
import fr.amapj.model.models.contrat.modele.GestionDocEngagement;
import fr.amapj.model.models.param.paramecran.PEMesContrats;
import fr.amapj.service.engine.generator.CoreGenerator;
import fr.amapj.service.services.docengagement.DocEngagementGeneralService;
import fr.amapj.service.services.docengagement.DocEngagementGeneralService.DocInfo;
import fr.amapj.service.services.edgenerator.excel.feuilledistribution.amapien.EGFeuilleDistributionAmapien;
import fr.amapj.service.services.edgenerator.excel.feuilledistribution.amapien.EGFeuilleDistributionAmapien.EGMode;
import fr.amapj.service.services.mescontrats.small.SmallContratDTO;
import fr.amapj.view.engine.excelgenerator.TelechargerPopup;
import fr.amapj.view.engine.popup.PopupListener;
import fr.amapj.view.engine.popup.corepopup.CorePopup;

public class TelechargerMesContrat
{
	
	TelechargerPopup popup;
	PEMesContrats peMesContrats;
	List<SmallContratDTO> existingContrats;
	
	/**
	 * Permet l'affichage d'un popup avec tous les fichiers à télécharger dans mes contrats 
	 * @param peMesContrats 
	 */
	public void displayPopupTelechargerMesContrat(PEMesContrats peMesContrats, List<SmallContratDTO> existingContrats,PopupListener listener)
	{
		this.existingContrats = existingContrats;
		popup = new TelechargerPopup("Télécharger les documents relatifs à mes contrats",80);
		
		this.peMesContrats = peMesContrats;
		
		
		switch (peMesContrats.presentationImpressionContrat)
		{
		case CONTRAT_FIRST:
			displayBlocContrat();
			displayBlocEngagement(true);
			displayBlocEngagement(false);
			break;

		case ENGAGEMENT_FIRST:
			displayBlocEngagement(true);
			displayBlocEngagement(false);
			displayBlocContrat();
			break;

		case MELANGE:
			displayMelange();
			break;
			
		default:
			throw new AmapjRuntimeException();
		}
		
		CorePopup.open(popup,listener);
	}

	

	private void displayBlocContrat()
	{
		List<CoreGenerator> gs = new ArrayList<>();
		for (SmallContratDTO c : existingContrats)
		{
			if (canPrintContrat(c))
			{
				gs.add(new EGFeuilleDistributionAmapien(EGMode.STD,c.modeleContratId,c.contratId));
			}
		}
		
		if (gs.size()>0)
		{
			popup.addLabel("<b>Mes feuilles de distribution (format tableur)</b>");
			addAll(gs);
		}
	}
	





	private boolean canPrintContrat(SmallContratDTO c)
	{
		switch (peMesContrats.canPrintContrat)
		{
		case TOUJOURS:
			return true;
			
		case JAMAIS:
			return false;
			
		case APRES_DATE_FIN_DES_INSCRIPTIONS:
			// Pour les contrats de type prepayée
			if (c.dateFinInscription==null)
			{
				return true;
			}
			Date dateRef = DateUtils.getDateWithNoTime();
			return dateRef.after(c.dateFinInscription);

		default:
			throw new AmapjRuntimeException();
		}
	}

	private void displayBlocEngagement(boolean signOnLine)
	{
		List<CoreGenerator> gs = new ArrayList<>();
		for (SmallContratDTO c : existingContrats)
		{
			if (canPrintContratEngagement(c))
			{
				DocInfo docInfo = new DocEngagementGeneralService().getDocumentEngagementForContrat(c.contratId);
				
				// Dans le cas de la signature en ligne 
				if (signOnLine)
				{
					// On affiche uniquement les documents signés
					if (docInfo.gestionDocEngagement==GestionDocEngagement.SIGNATURE_EN_LIGNE && docInfo.isSigned)
					{
						gs.add(docInfo.generator);
					}
				}
				// Dans le cas de la signature papier 
				else
				{
					if (docInfo.gestionDocEngagement==GestionDocEngagement.GENERATION_DOCUMENT_SEUL)
					{
						gs.add(docInfo.generator);
					}
				}
			}
		}
		
		if (gs.size()>0)
		{
			if (signOnLine)
			{
				popup.addLabel("<b>Mes documents d'engagement signés en ligne</b>");
			}
			else
			{
				popup.addLabel("<b>Mes documents d'engagement à signer au format papier</b>");
			}
			addAll(gs);
		}
		
		
	}

	
	private boolean canPrintContratEngagement(SmallContratDTO c)
	{
		switch (peMesContrats.canPrintContratEngagement)
		{
		case TOUJOURS:
			return true;
			
		case JAMAIS:
			return false;
			
		case APRES_DATE_FIN_DES_INSCRIPTIONS:
			// Pour les contrats de type prepayée
			if (c.dateFinInscription==null)
			{
				return true;
			}
			Date dateRef = DateUtils.getDateWithNoTime();
			return dateRef.after(c.dateFinInscription);

		default:
			throw new AmapjRuntimeException();
		}
	}
		
	private void displayMelange()
	{
		for (SmallContratDTO c : existingContrats)
		{
			if (canPrintContrat(c))
			{
				popup.addGenerator(new EGFeuilleDistributionAmapien(EGMode.STD,c.modeleContratId,c.contratId));
			}
			if (canPrintContratEngagement(c))
			{
				DocInfo docInfo = new DocEngagementGeneralService().getDocumentEngagementForContrat(c.contratId);
				
				if (docInfo.gestionDocEngagement==GestionDocEngagement.GENERATION_DOCUMENT_SEUL)
				{
					popup.addGenerator(docInfo.generator);
				}
				else if (docInfo.gestionDocEngagement==GestionDocEngagement.SIGNATURE_EN_LIGNE)
				{
					if (docInfo.isSigned)
					{
						popup.addGenerator(docInfo.generator);
					}
				}
			}
			
			popup.addLabel("");
		}
		
	}	
	
	// 
	
	private void addAll(List<CoreGenerator> gs) 
	{
		for (CoreGenerator g : gs) 
		{
			popup.addGenerator(g);
		}		
	}
	
}
