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
 package fr.amapj.model.models.param.paramecran;

import fr.amapj.model.models.param.ChoixOuiNon;
import fr.amapj.model.models.param.paramecran.common.AbstractParamEcran;

/**
 * Parametrage de l'écran Mes contrats
 */
public class PEMesContrats  extends AbstractParamEcran
{
	// Indique si on affiche la liste des contrats accessibles en tant que retardataire 
	public ChoixOuiNon affichageContratRetardataire = ChoixOuiNon.NON;
	
	// Indique si l'amapein peut imprimer ses contrats (classique au format Excel) 
	public ImpressionContrat canPrintContrat = ImpressionContrat.TOUJOURS;

	// Indique si l'amapein peut imprimer ses contrats d'engagement 
	public ImpressionContrat canPrintContratEngagement = ImpressionContrat.TOUJOURS;
	
	// Indique le mode de présentation des impressions 
	public PresentationImpressionContrat presentationImpressionContrat = PresentationImpressionContrat.ENGAGEMENT_FIRST;
	
	
	public ChoixOuiNon getAffichageContratRetardataire()
	{
		return affichageContratRetardataire;
	}

	public void setAffichageContratRetardataire(ChoixOuiNon affichageContratRetardataire)
	{
		this.affichageContratRetardataire = affichageContratRetardataire;
	}

	public ImpressionContrat getCanPrintContrat()
	{
		return canPrintContrat;
	}

	public void setCanPrintContrat(ImpressionContrat canPrintContrat)
	{
		this.canPrintContrat = canPrintContrat;
	}

	public ImpressionContrat getCanPrintContratEngagement()
	{
		return canPrintContratEngagement;
	}

	public void setCanPrintContratEngagement(ImpressionContrat canPrintContratEngagement)
	{
		this.canPrintContratEngagement = canPrintContratEngagement;
	}

	public PresentationImpressionContrat getPresentationImpressionContrat()
	{
		return presentationImpressionContrat;
	}

	public void setPresentationImpressionContrat(PresentationImpressionContrat presentationImpressionContrat)
	{
		this.presentationImpressionContrat = presentationImpressionContrat;
	}
}
