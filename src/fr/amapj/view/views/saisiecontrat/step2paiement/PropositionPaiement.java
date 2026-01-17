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
 package fr.amapj.view.views.saisiecontrat.step2paiement;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.models.contrat.modele.SaisiePaiementProposition;
import fr.amapj.service.services.mescontrats.DatePaiementDTO;
import fr.amapj.service.services.mescontrats.InfoPaiementDTO;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.ModeSaisie;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.SaisieContratData;

/**
 * Calcul des propositions de paiements en fonction des différents paramétres 
 *  
 */
public class PropositionPaiement 
{
	private final static Logger logger = LogManager.getLogger();
	
	private InfoPaiementDTO paiementDTO;
	
	private SaisieContratData data; 
	
	private int montantCible;
	
	/**
	 *  
	 */
	public PropositionPaiement(SaisieContratData data)
	{
		super();
		this.data = data;
		this.paiementDTO = data.contratDTO.paiement;
		montantCible = data.contratDTO.getMontantTotal();
	}
	
	/**
	 * Calcul une proposition de paiement si cela est nécessaire
	 */
	public void computePropositionPaiement() 
	{
		// Si visualisation seule : pas de proposition de paiement
		if (data.modeSaisie==ModeSaisie.READ_ONLY)
		{
			return;
		}
		
		// Si cheque seul : pas proposition de paiement, on fait tout à la main 
		if (data.modeSaisie==ModeSaisie.CHEQUE_SEUL)
		{
			return;
		}
		
		// 
		int nbModifiable = paiementDTO.getNbModifiable();
		logger.info("nbModifiable = "+nbModifiable);
		
		// Si 0 ligne EDITABLE : ça coince 
		if (nbModifiable==0)
		{
			throw new AmapjRuntimeException("Impossible de continuer : il n'est plus possible de modifier les chéques");
		}
			
		// Si il y a une seule ligne modifiable : on affecte tout le paiement restant sur cette ligne 
		if (nbModifiable==1)
		{
			DatePaiementDTO line = paiementDTO.getLastModifiable();
			line.montant = line.montant+montantCible-paiementDTO.getMontantTotalPaiement();
			return ;
		}
		
		// Si il y a plusieurs lignes modifiables, on fait le calcul d'une proposition de paiement , en fonction du paramétrage 
		List<DatePaiementDTO> lines = paiementDTO.getAllModifiableLineExceptLast();
		logger.info("lines AllModifiableLineExceptLast - size = "+lines.size());
		
		switch (paiementDTO.saisiePaiementProposition) 
		{
		case PAYE_AVANCE_ARRONDI:
		case PAYE_AVANCE_STRICT:
			performPayeAvance(lines);
			break;
			
		case REPARTI_ARRONDI:
			performRepartiArrondi(lines);
			break;
			
		case REPARTI_STRICT:
			performRepartiStrict(lines);
			break;
			
		default:
			throw new AmapjRuntimeException();
		}
	}


	// PAYE AVANCE 

	private void performPayeAvance(List<DatePaiementDTO> lines) 
	{
		// On calcule tout d'abord les lignes modifiables, sauf la dernière 
		for (DatePaiementDTO line : lines) 
		{
			// Calcul de la prochaine date de paiement 
			Date nextPaiementDate = paiementDTO.getNextDatePaiement(line);
			
			// Calcul du montant qui sera consommé avant cette prochaine date de paiement  
			int montantConsomme = data.contratDTO.getMontantTotalBefore(nextPaiementDate);
			
			// Calcul du montant payé sur les dates précédentes (en prenant en compte l'avoir initial)
			int montantPaye = paiementDTO.getMontantPayeBefore(line);
			
			// Calcul du montant total restant à payer sur le contrat
			int montantRestantAPayer = montantCible-montantPaye;
			
			// Si le payé est supérieur au consommé : on laisse 0
			if (montantPaye>=montantConsomme)
			{
				line.montant = 0;
			}
			// Sinon on paye ce qui manque
			else
			{
				line.montant = roundIfNeedPayeAvance(montantConsomme-montantPaye,montantRestantAPayer); 
			}
		}
		
		// On fait l'ajustement sur la dernière ligne modifiable 
		performAdjust(montantCible);
		
	}


	/**
	 * Cet arrondi tient compte du montant mini, du montant restant à payer sur le contrat
	 */
	private int roundIfNeedPayeAvance(int montant,int montantRestantAPayer)
	{
		// Mode Strict : pas d'arrondi 
		if (paiementDTO.saisiePaiementProposition==SaisiePaiementProposition.PAYE_AVANCE_STRICT) 
		{
			return montant;
		}
		
		int montantMini = paiementDTO.montantChequeMiniCalculProposition;
		
		// On arrondit à l'euro supérieur
		int mntArrondi = arrondiEuroSuperieur(montant);
		
		// On prend au moins le montant mini 
		int m1 = Math.max(mntArrondi, montantMini);
		
		// si cela est supérieur au reste à payer, on prend le reste à payer
		int m2 = Math.min(montantRestantAPayer, m1);
		
		// Si le reliquat est plus petit que le montant mini, alors on affecte tout le reste à payer
		// En effet, in ne pourra pas affecter le reliquat sur une ligne suivante (non repect de la regle du montantMini)
		int reliquat = montantRestantAPayer-m2;
		if (reliquat<montantMini)
		{
			return montantRestantAPayer;
		}
		else
		{
			return m2;
		}
	}
	
	
	// REPARTI ARRONDI 

	private void performRepartiArrondi(List<DatePaiementDTO> lines) 
	{
		int nbModifiable = paiementDTO.getNbModifiable();
		int montantAPaye = montantCible-paiementDTO.getMontantNonModifiable();
		
		int montant = computeMontantPart();
		
		int montantAffecte = 0;
		
		logger.info("Montant Part="+montant);
		
		for (int i = 0; i < lines.size(); i++) 
		{
			DatePaiementDTO line = lines.get(i);
			
			// Calcul du montant total restant à payer sur le contrat
			int montantRestantAPayer = montantCible-paiementDTO.getMontantPayeBefore(line);
			
			// Calcul du montant qu'il faut avoir payé à cet instant (part moyenne) 
			int montantCible = ((i+1)*montantAPaye)/nbModifiable;
			
			//
			if (montantAffecte>=montantCible)
			{
				line.montant = 0;
			}
			// Sinon on paye ce qui manque
			else
			{
				line.montant = Math.min(montant,montantRestantAPayer); 
			}
			montantAffecte = montantAffecte+line.montant;
		}
		
		// On fait l'ajustement sur la cellule ADJUST  
		performAdjust(montantCible);
	}	
	


	/**
	 * Calcul du montant d'une part
	 */
	private int computeMontantPart() 
	{
		int montantMini = paiementDTO.montantChequeMiniCalculProposition;
		int nbModifiable = paiementDTO.getNbModifiable();
		int montantAPaye = montantCible-paiementDTO.getMontantNonModifiable();
		
		if (montantMini==0)
		{
			int montantPart = arrondiEuroSuperieur(montantAPaye/nbModifiable);
			return montantPart;
		}
		
		//
		int nbPaiementMaxi = montantAPaye/montantMini;
		int nbPaiement = Math.min(nbPaiementMaxi, nbModifiable);
		
		if (nbPaiement==0)
		{
			return montantAPaye;
		}
		else
		{
			int montantPart = arrondiEuroSuperieur(montantAPaye/nbPaiement);
			return montantPart;
		}
	}
		
	// REPARTI STRICT 

	private void performRepartiStrict(List<DatePaiementDTO> lines) 
	{
		// 
		int nbPart = paiementDTO.getNbModifiable();
		int montantAPaye = montantCible-paiementDTO.getMontantNonModifiable();
		
		//
		int montant = montantAPaye/nbPart;
		
		for (int i = 0; i < lines.size(); i++) 
		{
			DatePaiementDTO line = lines.get(i);
			line.montant = montant; 
		}
		
		// On fait l'ajustement sur la cellule ADJUST (pour corriger les erreurs sur les quarts de centimes)  
		performAdjust(montantCible);
	}	
	
	
	
	// OUTILS TECHNIQUES 

	private int arrondiEuroSuperieur(int montant) 
	{
		if (montant % 100 == 0)
		{
			return montant;
		}
		return (montant/100)*100+100;
	}
	
	/**
	 * Réalise l'ajustement : on modifie la dernière ligne modifiable pour que le montant des paiement soit egal au montant cible
	 */
	public void performAdjust(int montantCible) 
	{
		//		
		DatePaiementDTO line = paiementDTO.getLastModifiable();
		line.montant = line.montant+montantCible-paiementDTO.getMontantTotalPaiement();
	}

}
