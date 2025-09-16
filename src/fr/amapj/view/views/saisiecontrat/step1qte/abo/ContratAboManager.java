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
 package fr.amapj.view.views.saisiecontrat.step1qte.abo;

import java.util.Date;

import fr.amapj.common.GenericUtils.Ret;
import fr.amapj.service.services.mescontrats.ContratColDTO;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.mescontrats.ContratLigDTO;
import fr.amapj.service.services.mescontrats.ContratLigDTO.AboLigStatus;
import fr.amapj.view.views.saisiecontrat.abo.ContratAbo;
import fr.amapj.view.views.saisiecontrat.abo.model.AboData;
import fr.amapj.view.views.saisiecontrat.abo.model.AboDataLig;
import fr.amapj.view.views.saisiecontrat.abo.model.AboDataManager;
import fr.amapj.view.views.saisiecontrat.abo.model.AboDataProd;


/**
 * Classe utilitaire permettant la gestion des contrats de type abonnement, avec prise en compte des jokers
 * et des contrats type retardataires
 *
 */
public class ContratAboManager
{
	
	/**
	 * Cette méthode doit être appelée pour un nouveau contrat (pas encore existant en base)
	 */	
	public ContratAbo computeContratAboNewContrat(ContratDTO dto,Date startDate)
	{
		// On calcule d'abord les quantités condensee, qui sont toutes à 0
		int[] qte = new int [dto.contratColumns.size()];
		
		// On positionne le status des lignes en fonction de la date de départ
		for (ContratLigDTO lig : dto.contratLigs)
		{
			if (startDate!=null && lig.date.before(startDate))
			{
				lig.status = AboLigStatus.FORCED_TO_0;
			}
			else
			{
				lig.status = AboLigStatus.NORMAL;
			}
		}
		
		//
		ContratAbo abo = new ContratAbo(dto, qte);
		
		return abo;
	}
	
	
	/**
	 * Cette méthode doit être appelée pour un contrat existant en base
	 */	
	public Ret<ContratAbo> computeContratAboExistingContrat(ContratDTO dto,String aboDataString)
	{
		if (aboDataString==null || aboDataString.length()==0)
		{
			return Ret.error("Pas de données en base");
		}
		
		AboData aboData = AboDataManager.fromString(aboDataString);
		
		// On positionne le status des lignes en fonction des données serialisees en base
		for (ContratLigDTO lig : dto.contratLigs)
		{
			AboDataLig aboDataLig = findAboDataLig(lig.date,aboData);
			if (aboDataLig!=null)
			{
				lig.status = aboDataLig.status;
				lig.reportDateDestination = aboDataLig.reportDateDestination;
			}
			else
			{
				lig.status = AboLigStatus.NORMAL;
			}
		}

		// On extrait ensuite les quantités condensee en fonction des données serialisees en base
		int[] qte = extractCondenseQte(dto,aboData);
		
		ContratAbo abo = new ContratAbo(dto, qte);
		
		// On verifie que tout est OK
		String str = abo.checkValid();
		if (str!=null)
		{
			return Ret.error(str);
		}
		return Ret.ok(abo);		
	}
	
	private int[] extractCondenseQte(ContratDTO dto, AboData aboData)
	{
		int[] res = new int [dto.contratColumns.size()];
		for (int j = 0; j < dto.contratColumns.size(); j++)
		{
			ContratColDTO col = dto.contratColumns.get(j);
			res[j] = findQte(col.modeleContratProduitId,aboData);
		}
		
		return res;
	}
	
	/**
	 * 
	 */
	private int findQte(Long idProduit, AboData aboData)
	{
		for (AboDataProd prod : aboData.prods) 
		{
			if (idProduit.equals(prod.idProduit))
			{
				return prod.qte;
			}
		}
		return 0;
	}
	
	private AboDataLig findAboDataLig(Date date, AboData aboData)
	{
		for (AboDataLig aboDataLig : aboData.ligs)
		{
			if (aboDataLig.date.equals(date)) 
			{
				return aboDataLig; 
			}
		}
		return null;
	}

	
	// BLOC 2 - MESSAGE D'AFFICHAGE
	
	public String computeJokerMessage(ContratDTO contratDTO,int used)
	{
		switch (contratDTO.typJoker) 
		{
			case JOKER_ABSENCE: return computeJokerMessageAbsenceMode(contratDTO,used);
			case JOKER_REPORT : return computeJokerMessageReportMode(contratDTO,used);
			default: return "";
		}
	}
	
	

	private String computeJokerMessageAbsenceMode(ContratDTO contratDTO, int used) 
	{
		StringBuilder sb = new StringBuilder();
		
		// Partie 1 de la phrase
		if (contratDTO.jokerNbMin==0)
		{
			if (contratDTO.jokerNbMax==1)
			{
				sb.append("Ce contrat autorise 1 joker maximum.");
			}
			else
			{
				sb.append("Ce contrat autorise "+contratDTO.jokerNbMax+" jokers maximum.");
			}
		}
		else
		{
			if (contratDTO.jokerNbMin==contratDTO.jokerNbMax)
			{
				sb.append("Ce contrat impose "+contratDTO.jokerNbMin+" joker(s).");
			}
			else
			{
				sb.append("Ce contrat impose "+contratDTO.jokerNbMin+" joker(s) au minimum et autorise "+contratDTO.jokerNbMax+" joker(s) au maximum.");
			}
		}
		
		// Partie 2 de la phrase
		if (used==0)
		{
			sb.append("Vous avez utilisé aucun joker.");
		}
		else if (used==1)
		{
			sb.append("Vous avez utilisé 1 joker.");
		}
		else
		{
			sb.append("Vous avez utilisé "+used+" jokers.");
		}
		
		return sb.toString();
	}
	
	private String computeJokerMessageReportMode(ContratDTO contratDTO, int used) 
	{
		StringBuilder sb = new StringBuilder();
		
		// Partie 1 de la phrase
		if (contratDTO.jokerNbMax==1)
		{
			sb.append("Ce contrat autorise 1 report (joker) maximum.");
		}
		else
		{
			sb.append("Ce contrat autorise "+contratDTO.jokerNbMax+" reports (jokers) maximum.");
		}
		
		// Partie 2 de la phrase
		if (used==0)
		{
			sb.append("Vous avez utilisé aucun report (joker).");
		}
		else if (used==1)
		{
			sb.append("Vous avez utilisé 1 report (joker).");
		}
		else
		{
			sb.append("Vous avez utilisé "+used+" reports (jokers).");
		}
		
		return sb.toString();
	}
}
