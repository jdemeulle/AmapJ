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
 package fr.amapj.service.services.mescontrats;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.models.contrat.modele.AffichageMontant;
import fr.amapj.model.models.contrat.modele.GestionPaiement;
import fr.amapj.model.models.contrat.modele.SaisiePaiementModifiable;
import fr.amapj.model.models.contrat.modele.SaisiePaiementProposition;
import fr.amapj.service.services.producteur.ProdUtilisateurDTO;

/**
 * Informations sur les paiements de ce contrat
 *
 */
public class InfoPaiementDTO
{
	// Lignes de paiement, ordonnées
	// Toutes les lignes sont présentes, même celles qui sont à zéro
	public List<DatePaiementDTO> datePaiements = new ArrayList<DatePaiementDTO>();
	
	public GestionPaiement gestionPaiement;
	
	public SaisiePaiementModifiable saisiePaiementModifiable;
	
	public SaisiePaiementProposition saisiePaiementProposition; 
	
	public int montantChequeMiniCalculProposition;
	
	public String textPaiement;
	
	public AffichageMontant affichageMontant;
	
	public int avoirInitial;
	
	
	// Ordre du chèque 
	public String libCheque;
	
	// Référents qui vont récolter les chèques 
	public List<ProdUtilisateurDTO> referentsRemiseCheque;
	
	
	/**
	 * Retourne le montant non modifiable (avoir + date non modifiable)
	 */
	public int getMontantNonModifiable()
	{
		int mnt = avoirInitial;
		for (DatePaiementDTO datePaiementDTO : datePaiements) 
		{
			if (datePaiementDTO.isModifiable==false)
			{
				mnt = mnt + datePaiementDTO.montant;
			}
		}
		return mnt;
	}

	/**
	 * Retourne le nombre de dates de paiements modifiables
	 */
	public int getNbModifiable() 
	{
		return (int) datePaiements.stream().filter(e->e.isModifiable).count();
	}
	
	/**
	 * Retourne la dernière date de paiement modifiable
	 */
	public DatePaiementDTO getLastModifiable() 
	{
		for (int i = datePaiements.size()-1;i>=0; i--) 
		{
			DatePaiementDTO line = datePaiements.get(i);
			if (line.isModifiable)
			{
				return line;
			}
		}
		return null;
	}


	/**
	 * Retourne le montant payé de la date 0 à la date line NON incluse
	 */
	public int getMontantPayeBefore(DatePaiementDTO line) 
	{
		int mnt = avoirInitial;
		for (DatePaiementDTO datePaiementDTO : datePaiements) 
		{
			if (datePaiementDTO==line)
			{
				return mnt;
			}
			mnt = mnt+datePaiementDTO.montant;
		}
		throw new AmapjRuntimeException();
	}
	
	
	/**
	 * 
	 */
	public int getMontantTotalPaiement()
	{
		int mnt = avoirInitial;
		for (DatePaiementDTO datePaiementDTO : datePaiements) 
		{
			mnt = mnt+datePaiementDTO.montant;
		}
		return mnt;
	}

	/**
	 * Retourne la prochaine date de paiement
	 * Retourne null si il n'y en a pas 
	 */
	public Date getNextDatePaiement(DatePaiementDTO line) 
	{
		int index = datePaiements.indexOf(line);
		if (index<datePaiements.size()-1)
		{
			return datePaiements.get(index+1).datePaiement;
		}
		return null;
	}

	/**
	 * Récupère toutes les lignes modifiables, sauf la dernière
	 * @return 
	 */
	public List<DatePaiementDTO> getAllModifiableLineExceptLast() 
	{
		List<DatePaiementDTO> ls = datePaiements.stream().filter(e->e.isModifiable).collect(Collectors.toList());
		ls.remove(ls.size()-1);
		return ls;
		
	}

	public boolean isLastLineModifiable(DatePaiementDTO datePaiement) 
	{
		return getLastModifiable()==datePaiement;
	}

}
