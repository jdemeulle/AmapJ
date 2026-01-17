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

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.DateUtils;
import fr.amapj.model.models.contrat.modele.JokerMode;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.ModeleContratDate;
import fr.amapj.model.models.contrat.modele.NatureContrat;
import fr.amapj.model.models.contrat.modele.RetardataireAutorise;
import fr.amapj.model.models.contrat.modele.TypJoker;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.model.models.contrat.reel.TypInscriptionContrat;


/**
 * Gestion des dates pour un contrat ou pour une ligne de contrat
 *
 */
public class ContratStatusService
{
	// Gestion de la fin des inscription 
	

	/**
	 * Retourne true si les inscriptions ne sont pas terminées pour ce contrat 
	 * 
	 * A utiliser uniquement avec les contrats de type ABONNEMENT et LIBRE 
	 */
	public boolean isInscriptionNonTerminee(ModeleContrat modeleContrat,Date now)
	{
		Date dateFinInscription = modeleContrat.dateFinInscription;
		Date d = DateUtils.addHour(dateFinInscription,23);
		d = DateUtils.addMinute(d, 59);
		return  d.after(now);
	}
		
	
	// Gestion d'une date d'un modele de contrat 


	/**
	 * Indique si cette date de modele de contrat est modifiable par un adherent quelconque à la date now 
	 * en prenant en compte le cas eventuel des retardataires et des jokers
	 * 
	 */
	public boolean isDateModifiable(ModeleContratDate mcd,Date now)
	{
		NatureContrat nature = mcd.modeleContrat.nature;
		switch (nature) 
		{
		case ABONNEMENT:
			return isDateModifiableAbonnement(mcd,now);
			
		case LIBRE:
			return isDateModifiableLibre(mcd,now);
		
		case CARTE_PREPAYEE:
			return isDateModifiableCartePrepayee(mcd,now);

		default:
			throw new AmapjRuntimeException();
		}
	}

	// ABONNEMENT
	
	/**
	 * Une date peut être modifié par une personne qui s'inscrit normalement ou par une personne qui pose un joker ou par un retardataire qui s'isncrit  
	 */
	private boolean isDateModifiableAbonnement(ModeleContratDate mcd, Date now) 
	{
		return isDateModifiableAbonnementInscription(mcd,now) || isDateModifiableAbonnementJoker(mcd,now) || isDateModifiableAbonnementRetardataire(mcd,now);
	}
	
	/**
	 * Est ce que cette date peut être modifiée par un adherent qui s'inscrt normalement avant la fin des inscriptions ?  
	 */
	private boolean isDateModifiableAbonnementInscription(ModeleContratDate mcd, Date now) 
	{
		return isInscriptionNonTerminee(mcd.modeleContrat, now);
	}

	/**
	 * Est ce que cette date peut être modifiée par un adherent qui pose un joker apres la fin des inscriptions ? 
	 */
	private boolean isDateModifiableAbonnementJoker(ModeleContratDate mcd, Date now) 
	{
		return isDateModifiableAbonnementJoker(mcd.dateLiv,now,mcd.modeleContrat);
	}
	
	public boolean isDateModifiableAbonnementJoker(Date mcd, Date now,ModeleContrat mc) 
	{
		if (mc.typJoker==TypJoker.SANS_JOKER)
		{
			return false;
		}
		
		if (mc.jokerMode==JokerMode.INSCRIPTION || mc.jokerNbMax==0)
		{
			return false;
		}
		LocalDate limit = DateUtils.asLocalDate(mcd);
		limit = limit.plusDays(-mc.jokerDelai);
		return DateUtils.asLocalDate(now).isBefore(limit);
	}
	

	/**
	 * Est ce que cette date peut être modifiée par un adherent qui s'inscrit en tant que retardataire ? 
	 */
	private boolean isDateModifiableAbonnementRetardataire(ModeleContratDate mcd, Date now) 
	{
		Date d = DateUtils.addDays(mcd.dateLiv, -mcd.modeleContrat.producteur.delaiModifContrat);
		return  d.after(now);
	}

 
	
	// LIBRE 
	
	
	/**
	 * Une date peut être modifié par une personne qui s'inscrit normalement ou par un retardataire qui s'inscrit  
	 */
	private boolean isDateModifiableLibre(ModeleContratDate mcd, Date now) 
	{
		return isDateModifiableLibreInscription(mcd,now) || isDateModifiableLibreRetardataire(mcd,now);
	}

	
	/**
	 * Est ce que cette date peut être modifiée par un adherent qui s'inscrt normalement avant la fin des inscriptions ?  
	 */
	private boolean isDateModifiableLibreInscription(ModeleContratDate mcd, Date now) 
	{
		return isInscriptionNonTerminee(mcd.modeleContrat, now);
	}

	/**
	 * Est ce que cette date peut être modifiée par un adherent qui s'inscrit en retardataire ? 
	 */
	private boolean isDateModifiableLibreRetardataire(ModeleContratDate mcd, Date now) 
	{
		Date d = DateUtils.addDays(mcd.dateLiv, -mcd.modeleContrat.producteur.delaiModifContrat);
		return  d.after(now);
	}

	// CARTE PREPAYEE


	public boolean isDateModifiableCartePrepayee(ModeleContratDate mcd, Date now) 
	{
		int cartePrepayeeDelai = mcd.modeleContrat.cartePrepayeeDelai;
		Date d = DateUtils.addDays(mcd.dateLiv, -cartePrepayeeDelai);
		return  d.after(now);	
	} 
	
	
	// 
	
	// Methode specifique pour les retardataires
	
	/**
	 * Indique si cette date de modele de contrat est modifiable par un retardataire à la date now 
	 * 
	 */
	public boolean isDateModifiableByRetardataire(ModeleContratDate mcd,Date now)
	{
		if (mcd.modeleContrat.retardataireAutorise==RetardataireAutorise.NON)
		{
			return false;
		}
		
		NatureContrat nature = mcd.modeleContrat.nature;
		switch (nature) 
		{
		case ABONNEMENT:
			return isDateModifiableAbonnementRetardataire(mcd,now);
			
		case LIBRE:
			return isDateModifiableLibreRetardataire(mcd,now);
		
		case CARTE_PREPAYEE:
			return false; // Pas de notion de retardataire en prépayée

		default:
			throw new AmapjRuntimeException();
		}
	}

	/**
	 * Indique si un retardataire peut s'inscrire à ce  modele de contrat à la date now 
	 */
	public boolean isInscriptionRetardatairePossible(ModeleContrat modeleContrat, Date now,List<ModeleContratDate> dates)
	{
		// Il suffit qu'il y ait encore une date modifiable en tant que retardataire 
		for (ModeleContratDate modeleContratDate : dates)
		{
			if (isDateModifiableByRetardataire(modeleContratDate, now))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Indique si on est dans la periode de modification possible d'un contrat retardataire
	 */
	public boolean isContratRetardataireModifiable(Contrat c, Date now)
	{
		if (c.typInscriptionContrat==TypInscriptionContrat.STANDARD)
		{
			return false;
		}
		
		// On peut modifier le jour même de la création du contrat en mode retardataire
		Date limit = DateUtils.addDays(DateUtils.suppressTime(c.dateCreation),1);
		if(now.before(limit))
		{
			return true;
		}
		else
		{
			return false;
		}
	}	
}
