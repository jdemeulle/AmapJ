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
 * Parametrage de l'écran Mes livraisons
 */
public class PESyntheseMultiContrat  extends AbstractParamEcran
{
	
	// PARTIE BILAN LIVRAISON
	
	/** RECAP MENSUELLE */
	
	// Possibilité d'imprimer une récap mensuelle 
	public ChoixOuiNon mensuelImpressionRecap = ChoixOuiNon.OUI;
	
	// Format de l'impression de la récap mensuelle
	public ChoixImpressionBilanLivraison mensuelFormat = ChoixImpressionBilanLivraison.TABLEUR;
		
	// Edition à utiliser si pdf
	public Long mensuelPdfEditionId;
	
	public int mensuelNbJourAvant = 30;
	
	public int mensuelNbJourApres = 30;

	/** RECAP TRIMESTRIELLE */
	
	// Possibilité d'imprimer une récap trimestrielle
	public ChoixOuiNon trimestreImpressionRecap = ChoixOuiNon.NON;
	
	// Format de l'impression de la récap trimestrielle
	public ChoixImpressionBilanLivraison trimestreFormat;
		
	// Edition à utiliser si pdf
	public Long trimestrePdfEditionId;

	
	public int trimestreNbJourAvant;
	
	public int trimestreNbJourApres;
	
	
	
	
	// PARTIE BILAN MONTANTS LIVRES CONTRAT 

	/** RECAP JOURNALIERE */
	
	// Possibilité d'imprimer une récap journalière des montants livrés  
	public ChoixOuiNon mntLivreContratJournalierRecap = ChoixOuiNon.OUI;
		
	public int mntLivreContratJournalierNbJourAvant = 30;
	
	public int mntLivreContratJournalierNbJourApres = 30;

	
	
	/** RECAP MENSUELLE */
	
	// Possibilité d'imprimer une récap mensuel des montants livrés  
	public ChoixOuiNon mntLivreContratMensuelRecap = ChoixOuiNon.OUI;
		
	public int mntLivreContratMensuelNbJourAvant = 30;
	
	public int mntLivreContratMensuelNbJourApres = 30;
	
	// PARTIE BILAN MONTANTS LIVRES PRODUCTEUR 

	/** RECAP JOURNALIERE */
	
	// Possibilité d'imprimer une récap journalière des montants livrés  
	public ChoixOuiNon mntLivreProducteurJournalierRecap = ChoixOuiNon.OUI;
		
	public int mntLivreProducteurJournalierNbJourAvant = 30;
	
	public int mntLivreProducteurJournalierNbJourApres = 30;

	
	
	/** RECAP MENSUELLE */
	
	// Possibilité d'imprimer une récap mensuel des montants livrés  
	public ChoixOuiNon mntLivreProducteurMensuelRecap = ChoixOuiNon.OUI;
		
	public int mntLivreProducteurMensuelNbJourAvant = 30;
	
	public int mntLivreProducteurMensuelNbJourApres = 30;
	
	
	// Getters and setters
	

	public ChoixOuiNon getMensuelImpressionRecap()
	{
		return mensuelImpressionRecap;
	}

	public void setMensuelImpressionRecap(ChoixOuiNon mensuelImpressionRecap)
	{
		this.mensuelImpressionRecap = mensuelImpressionRecap;
	}

	public ChoixImpressionBilanLivraison getMensuelFormat()
	{
		return mensuelFormat;
	}

	public void setMensuelFormat(ChoixImpressionBilanLivraison mensuelFormat)
	{
		this.mensuelFormat = mensuelFormat;
	}

	public Long getMensuelPdfEditionId()
	{
		return mensuelPdfEditionId;
	}

	public void setMensuelPdfEditionId(Long mensuelPdfEditionId)
	{
		this.mensuelPdfEditionId = mensuelPdfEditionId;
	}

	public int getMensuelNbJourAvant()
	{
		return mensuelNbJourAvant;
	}

	public void setMensuelNbJourAvant(int mensuelNbJourAvant)
	{
		this.mensuelNbJourAvant = mensuelNbJourAvant;
	}

	public int getMensuelNbJourApres()
	{
		return mensuelNbJourApres;
	}

	public void setMensuelNbJourApres(int mensuelNbJourApres)
	{
		this.mensuelNbJourApres = mensuelNbJourApres;
	}

	public ChoixOuiNon getTrimestreImpressionRecap()
	{
		return trimestreImpressionRecap;
	}

	public void setTrimestreImpressionRecap(ChoixOuiNon trimestreImpressionRecap)
	{
		this.trimestreImpressionRecap = trimestreImpressionRecap;
	}

	public ChoixImpressionBilanLivraison getTrimestreFormat()
	{
		return trimestreFormat;
	}

	public void setTrimestreFormat(ChoixImpressionBilanLivraison trimestreFormat)
	{
		this.trimestreFormat = trimestreFormat;
	}

	public Long getTrimestrePdfEditionId()
	{
		return trimestrePdfEditionId;
	}

	public void setTrimestrePdfEditionId(Long trimestrePdfEditionId)
	{
		this.trimestrePdfEditionId = trimestrePdfEditionId;
	}

	public int getTrimestreNbJourAvant()
	{
		return trimestreNbJourAvant;
	}

	public void setTrimestreNbJourAvant(int trimestreNbJourAvant)
	{
		this.trimestreNbJourAvant = trimestreNbJourAvant;
	}

	public int getTrimestreNbJourApres()
	{
		return trimestreNbJourApres;
	}

	public void setTrimestreNbJourApres(int trimestreNbJourApres)
	{
		this.trimestreNbJourApres = trimestreNbJourApres;
	}

	public ChoixOuiNon getMntLivreContratJournalierRecap()
	{
		return mntLivreContratJournalierRecap;
	}

	public void setMntLivreContratJournalierRecap(ChoixOuiNon mntLivreContratJournalierRecap)
	{
		this.mntLivreContratJournalierRecap = mntLivreContratJournalierRecap;
	}

	public int getMntLivreContratJournalierNbJourAvant()
	{
		return mntLivreContratJournalierNbJourAvant;
	}

	public void setMntLivreContratJournalierNbJourAvant(int mntLivreContratJournalierNbJourAvant)
	{
		this.mntLivreContratJournalierNbJourAvant = mntLivreContratJournalierNbJourAvant;
	}

	public int getMntLivreContratJournalierNbJourApres()
	{
		return mntLivreContratJournalierNbJourApres;
	}

	public void setMntLivreContratJournalierNbJourApres(int mntLivreContratJournalierNbJourApres)
	{
		this.mntLivreContratJournalierNbJourApres = mntLivreContratJournalierNbJourApres;
	}

	public ChoixOuiNon getMntLivreContratMensuelRecap()
	{
		return mntLivreContratMensuelRecap;
	}

	public void setMntLivreContratMensuelRecap(ChoixOuiNon mntLivreContratMensuelRecap)
	{
		this.mntLivreContratMensuelRecap = mntLivreContratMensuelRecap;
	}

	public int getMntLivreContratMensuelNbJourAvant()
	{
		return mntLivreContratMensuelNbJourAvant;
	}

	public void setMntLivreContratMensuelNbJourAvant(int mntLivreContratMensuelNbJourAvant)
	{
		this.mntLivreContratMensuelNbJourAvant = mntLivreContratMensuelNbJourAvant;
	}

	public int getMntLivreContratMensuelNbJourApres()
	{
		return mntLivreContratMensuelNbJourApres;
	}

	public void setMntLivreContratMensuelNbJourApres(int mntLivreContratMensuelNbJourApres)
	{
		this.mntLivreContratMensuelNbJourApres = mntLivreContratMensuelNbJourApres;
	}

	public ChoixOuiNon getMntLivreProducteurJournalierRecap()
	{
		return mntLivreProducteurJournalierRecap;
	}

	public void setMntLivreProducteurJournalierRecap(ChoixOuiNon mntLivreProducteurJournalierRecap)
	{
		this.mntLivreProducteurJournalierRecap = mntLivreProducteurJournalierRecap;
	}

	public int getMntLivreProducteurJournalierNbJourAvant()
	{
		return mntLivreProducteurJournalierNbJourAvant;
	}

	public void setMntLivreProducteurJournalierNbJourAvant(int mntLivreProducteurJournalierNbJourAvant)
	{
		this.mntLivreProducteurJournalierNbJourAvant = mntLivreProducteurJournalierNbJourAvant;
	}

	public int getMntLivreProducteurJournalierNbJourApres()
	{
		return mntLivreProducteurJournalierNbJourApres;
	}

	public void setMntLivreProducteurJournalierNbJourApres(int mntLivreProducteurJournalierNbJourApres)
	{
		this.mntLivreProducteurJournalierNbJourApres = mntLivreProducteurJournalierNbJourApres;
	}

	public ChoixOuiNon getMntLivreProducteurMensuelRecap()
	{
		return mntLivreProducteurMensuelRecap;
	}

	public void setMntLivreProducteurMensuelRecap(ChoixOuiNon mntLivreProducteurMensuelRecap)
	{
		this.mntLivreProducteurMensuelRecap = mntLivreProducteurMensuelRecap;
	}

	public int getMntLivreProducteurMensuelNbJourAvant()
	{
		return mntLivreProducteurMensuelNbJourAvant;
	}

	public void setMntLivreProducteurMensuelNbJourAvant(int mntLivreProducteurMensuelNbJourAvant)
	{
		this.mntLivreProducteurMensuelNbJourAvant = mntLivreProducteurMensuelNbJourAvant;
	}

	public int getMntLivreProducteurMensuelNbJourApres()
	{
		return mntLivreProducteurMensuelNbJourApres;
	}

	public void setMntLivreProducteurMensuelNbJourApres(int mntLivreProducteurMensuelNbJourApres)
	{
		this.mntLivreProducteurMensuelNbJourApres = mntLivreProducteurMensuelNbJourApres;
	}

}
