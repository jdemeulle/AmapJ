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
 package fr.amapj.view.views.saisiecontrat.abo;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.model.models.contrat.modele.TypJoker;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.mescontrats.ContratLigDTO;
import fr.amapj.service.services.mescontrats.ContratLigDTO.AboLigStatus;

/**
 * Permet de "deviner" les regles jokers 
 *
 */
public class ContratAboGuesser 
{
	private final static Logger logger = LogManager.getLogger();
	
	private int[] qte;
	
	private ContratDTO dto;
	

	public ContratAboGuesser(ContratDTO dto) 
	{
		this.dto = dto;
		this.qte = new int [dto.contratColumns.size()];
	}

	/**
	 * 
	 */	
	public String guessContratAboInfo()
	{
		// On recherche les quantités condensées
		findQte(dto);
		
		// On essaye ensuite de deviner les lignes JOKER
		if (dto.typJoker==TypJoker.JOKER_ABSENCE || dto.typJoker==TypJoker.SANS_JOKER)
		{
			return guessJokerAbsence();
		}
		else
		{
			return guessJokerReport();
		}
	}

	// PARTIE JOKER ABSENCE 

	private String guessJokerAbsence() 
	{
		int nbFirstLineToZero = countNbFirstLineToZero();
		for (int i = nbFirstLineToZero; i >= 0; i--) 
		{
			String str = guessJokerAbsence(i);
			if (str!=null)
			{
				return str;
			}
		}
		logger.debug("GuessJokerAbsence : NOT FOUND");
		return null;
	}


	private int countNbFirstLineToZero() 
	{
		for (int i = 0; i < dto.contratLigs.size(); i++) 
		{
			if (dto.isEmptyLine(i)==false)
			{
				return i;
			}
		}

		// Si tout est vide : on retourne 0
		return 0;
	}

	/**
	 * On fait un essai en forcant les nbLineToZero premières lignes à 0 et en mettant des jokers ailleurs 
	 */
	private String guessJokerAbsence(int nbLineToZero) 
	{
		for (int indexCol = 0; indexCol < qte.length; indexCol++) 
		{
			if (qte[indexCol]!=0)
			{
				String str = guessJokerAbsence(nbLineToZero, indexCol);
				if (str!=null)
				{
					return str;
				}
			}
		}
		return null;
	}
	
	
	/**
	 * 
	 */
	private String guessJokerAbsence(int nbLineToZero,int indexRefCol) 
	{
		for (int i = 0; i < dto.contratLigs.size(); i++) 
		{
			ContratLigDTO lig = dto.contratLigs.get(i);
			if (i<nbLineToZero)
			{
				lig.status = AboLigStatus.FORCED_TO_0;
			}
			else
			{
				if (dto.isExcluded(i,indexRefCol)==false && dto.cell[i][indexRefCol].qte==0)
				{
					lig.status=AboLigStatus.JOKER;
				}
				else
				{
					lig.status=AboLigStatus.NORMAL;
				}
			}
		}
		
		ContratAbo abo = new ContratAbo(dto, qte);;
		String str = abo.checkValid();
		if (str!=null)
		{
			return null;
		}
		
		logger.debug("GuessJokerAbsence : "+abo.dump());
		
		return abo.getAboInfo();
	}


	
	// PARTIE JOKER REPORT

	private String guessJokerReport() 
	{
		int nbFirstLineToZero = countNbFirstLineToZero();
		for (int i = nbFirstLineToZero; i >= 0; i--) 
		{
			String str = guessJokerReport(i);
			if (str!=null)
			{
				return str;
			}
		}
		logger.debug("GuessJokerReport : NOT FOUND");
		return null;
	}

	
	/**
	 * 
	 */
	private String guessJokerReport(int nbLineToZero) 
	{
		for (int indexCol = 0; indexCol < qte.length; indexCol++) 
		{
			if (qte[indexCol]!=0)
			{
				String str = guessJokerReport(nbLineToZero, indexCol);
				if (str!=null)
				{
					return str;
				}
			}
		}
		return null;
	}


	/**
	 * On fait un essai en forcant les nbLineToZero premières lignes à 0 et en mettant des reports ailleurs 
	 */
	private String guessJokerReport(int nbLineToZero,int refCol) 
	{
		for (int i = 0; i < dto.contratLigs.size(); i++) 
		{
			ContratLigDTO lig = dto.contratLigs.get(i);
			if (i<nbLineToZero)
			{
				lig.status = AboLigStatus.FORCED_TO_0;
			}
			else
			{
				lig.status = findReportStatus(i,refCol);
			}
		}
		

		for (int i = 0; i < dto.contratLigs.size(); i++) 
		{
			ContratLigDTO lig = dto.contratLigs.get(i);
			if (lig.status==AboLigStatus.REPORT)
			{
				lig.reportDateDestination = findDateDestination(refCol);
			}
		}
				
		
		ContratAbo abo = new ContratAbo(dto, qte);;
		String str = abo.checkValid();
		if (str!=null)
		{
			return null;
		}
		
		logger.debug("GuessJokerReport : "+abo.dump());
		
		return abo.getAboInfo();
	}

	private AboLigStatus findReportStatus(int i, int refCol) 
	{
		if (dto.isExcluded(i, refCol)==false && dto.cell[i][refCol].qte==0)
		{
			return AboLigStatus.REPORT;
		}
		
		return AboLigStatus.NORMAL;
	}
	
	private Date findDateDestination(int refCol) 
	{
		for (int i = 0; i < dto.contratLigs.size(); i++) 
		{
			ContratLigDTO lig = dto.contratLigs.get(i);
			if (lig.status==AboLigStatus.NORMAL && needQte(lig.i,refCol))
			{
				return lig.date;
			}
		}
		return null;
	}

	/**
	 * Est ce que cette ligne aurait besoin d'un report
	 */
	private boolean needQte(int indexLine,int refCol) 
	{
		int qteNow = dto.cell[indexLine][refCol].qte;
		
		int qteCal = computeQte(indexLine,refCol);
		
		if (qteCal<qteNow)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	private int computeQte(int indexLine,int refCol) 
	{		
		// Valeur de base 
		int q = qte[refCol];
		
		// On fait le calcul des reports
		for (int i = 0; i < dto.contratLigs.size(); i++)
		{
			ContratLigDTO lig = dto.contratLigs.get(i);
			if (lig.reportDateDestination!=null)
			{
				ContratLigDTO ligDest = dto.findLig(lig.reportDateDestination);
				if (ligDest!=null && ligDest.i==indexLine)
				{
					q = q+ qte[refCol];
				}
			}
		}
		return q;
	}

	
	
	
	// CALCUL INITIAL DES QUANTITES 

	private void findQte(ContratDTO dto)
	{
		for (int j = 0; j < dto.contratColumns.size(); j++)
		{
			qte[j] = extractQte(j,dto);
		}
	}
	
	/**
	 * Extrait la quantité minimum non nulle , pour le produit indiqué par cette colone
	 * 
	 * Retourne 0 si toutes les lignes ont une quantité de 0 
	 * 
	 */
	private int extractQte(int col,ContratDTO dto)
	{
		// 
		int min = 0;
		
		for (int i = 0; i < dto.contratLigs.size(); i++)
		{
			if (dto.cell[i][col].qte!=0)
			{
				if (min==0)
				{
					min = dto.cell[i][col].qte;
				}
				else
				{
					min = Math.min(min, dto.cell[i][col].qte);
				}
			}
		}
		return min;
	}
	
	
	
	
}
