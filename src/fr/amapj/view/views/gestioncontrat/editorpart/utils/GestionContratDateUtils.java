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
 package fr.amapj.view.views.gestioncontrat.editorpart.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.DateUtils;
import fr.amapj.model.models.contrat.modele.GestionPaiement;
import fr.amapj.model.models.contrat.modele.SaisiePaiementCalculDate;
import fr.amapj.service.services.gestioncontrat.DateModeleContratDTO;
import fr.amapj.service.services.gestioncontrat.DatePaiementModeleContratDTO;
import fr.amapj.service.services.gestioncontrat.ModeleContratDTO;

public class GestionContratDateUtils 
{
	/**
	 * Calcule le champ dateLivs d'un modele de contrat à partir des informations fréquence et date de début det date de fin  
	 */
	public void computeDateLivraison(ModeleContratDTO modeleContrat)
	{
		List<Date> dates;
		
		switch (modeleContrat.frequence) 
		{
			case UNE_SEULE_LIVRAISON: 		dates = getUneSeuleLivraison(modeleContrat); break;
			case UNE_FOIS_PAR_SEMAINE :  	dates = getUneFoisParSemaine(modeleContrat); break;
			case QUINZE_JOURS :  			dates = getQuinzeJours(modeleContrat); break;
			case UNE_FOIS_PAR_MOIS : 		dates = getUneFoisParMois(modeleContrat); break;
			case AUTRE:						dates = getAllDatesAutre(modeleContrat.dateLivs); break;
			default: throw new AmapjRuntimeException();
		}
		
		modeleContrat.dateLivs.clear();
		for (Date date : dates) 
		{
			DateModeleContratDTO dto = new DateModeleContratDTO();
			dto.dateLiv = date;
			modeleContrat.dateLivs.add(dto);
		}
		
		// 
		Collections.sort(modeleContrat.dateLivs,(p1,p2)->p1.dateLiv.compareTo(p2.dateLiv));
		
	}
	

	private List<Date> getUneSeuleLivraison(ModeleContratDTO modeleContrat) 
	{
		List<Date> dates = new ArrayList<Date>();
		dates.add(modeleContrat.dateDebut);
		return dates;
		 
	}
	
	
	private List<Date> getUneFoisParSemaine(ModeleContratDTO modeleContrat) 
	{
		return  computeDateWithDelta(modeleContrat.dateDebut,modeleContrat.dateFin,7);
	}
	
	private List<Date> getQuinzeJours(ModeleContratDTO modeleContrat) 
	{
		return  computeDateWithDelta(modeleContrat.dateDebut,modeleContrat.dateFin,14);
	}

	private List<Date> computeDateWithDelta(Date dateDebut, Date dateFin, int delta) 
	{
		List<Date> res = new ArrayList<Date>();
		int cpt = 0;
		
		while (dateDebut.before(dateFin) || dateDebut.equals(dateFin))
		{
			cpt++;
			res.add(dateDebut);
			dateDebut = DateUtils.addDays(dateDebut, delta);

			if (cpt > 1000)
			{
				throw new AmapjRuntimeException("Erreur dans la saisie des dates");
			}
		}

		return res;
	}
	
	/**
	 * Calcul permettant d'avoir par exemple tous les 1er jeudi du mois
	 */
	private List<Date> getUneFoisParMois(ModeleContratDTO modeleContrat) 
	{
		Date dateDebut = modeleContrat.dateDebut;
		Date dateFin = modeleContrat.dateFin;
		
		List<Date> res = new ArrayList<Date>();

		int cpt = 0;
		res.add(dateDebut);

		int rank = DateUtils.getDayOfWeekInMonth(dateDebut);
		int delta = 7;

		dateDebut = DateUtils.addDays(dateDebut, delta);

		while (dateDebut.before(dateFin) || dateDebut.equals(dateFin))
		{
			cpt++;
			if (DateUtils.getDayOfWeekInMonth(dateDebut) == rank)
			{
				res.add(dateDebut);
			}
			dateDebut = DateUtils.addDays(dateDebut, delta);

			if (cpt > 1000)
			{
				throw new RuntimeException("Erreur dans la saisie des dates");
			}
		}

		return res;
	}

	private List<Date> getAllDatesAutre(List<DateModeleContratDTO> dateLivs)
	{
		List<Date> res = new ArrayList<>();
		for (DateModeleContratDTO dto : dateLivs)
		{
			res.add(dto.dateLiv);
		}
		return res;
	}

	// PARTIE PAIEMENT 

	/**
	 * Calcul des dates de paiements en fonction du paramétrage
	 */
	public void computeDatePaiement(ModeleContratDTO modeleContrat) 
	{
		// 
		if (modeleContrat.gestionPaiement==GestionPaiement.NON_GERE)
		{
			modeleContrat.datePaiements.clear();
			return;
		}		
		
		// Pas de modif si AUTRE
		if (modeleContrat.saisiePaiementCalculDate==SaisiePaiementCalculDate.AUTRE)
		{
			return;
		}

		// Cas standard
		List<Date> res = calculDatePaiement(modeleContrat.saisiePaiementCalculDate,modeleContrat.dateLivs);
		
		// Copie dans le modele de contrat 
		modeleContrat.datePaiements.clear();
		for (Date date : res) 
		{
			DatePaiementModeleContratDTO dto = new DatePaiementModeleContratDTO();
			dto.datePaiement = date;
			modeleContrat.datePaiements.add(dto);
		}
	}
	
	
	public List<Date> calculDatePaiement(SaisiePaiementCalculDate saisiePaiementCalculDate, List<DateModeleContratDTO> dateLivs)
	{
		// Cas standard
		List<Date> res = new ArrayList<>();
		switch (saisiePaiementCalculDate) 
		{
		case UN_PAIEMENT_PAR_LIVRAISON :
			computeUnPaiementParLivraison(dateLivs,res);
			break;
		
		case UN_PAIEMENT_PAR_MOIS_PREMIERE_LIVRAISON:
			computeUnPaiementParMoisPremiereLivraison(dateLivs,res);
			break;
			
		case UN_PAIEMENT_PAR_MOIS_DEBUT_MOIS:
			computeUnPaiementParMoisDebutMois(dateLivs,res);
			break;
			
		case AUTRE:
		default:
			throw new AmapjRuntimeException();
		}
		return res;
	}
	
	
	
	
	private void computeUnPaiementParMoisDebutMois(List<DateModeleContratDTO> dateLivs,List<Date> res) 
	{
		Date dateCur = null;
		for (DateModeleContratDTO dateLiv :  dateLivs) 
		{
			if (dateCur==null || DateUtils.isSameMonthAndYear(dateCur,dateLiv.dateLiv)==false)
			{
				dateCur = DateUtils.firstDayInMonth(dateLiv.dateLiv);
				res.add(dateCur);
			}
		}
	}


	private void computeUnPaiementParMoisPremiereLivraison(List<DateModeleContratDTO> dateLivs,List<Date> res) 
	{
		Date dateCur = null;
		for (DateModeleContratDTO dateLiv :  dateLivs) 
		{
			if (dateCur==null || DateUtils.isSameMonthAndYear(dateCur,dateLiv.dateLiv)==false)
			{
				dateCur = dateLiv.dateLiv;
				res.add(dateCur);
			}
		}
	}

	
	private void computeUnPaiementParLivraison(List<DateModeleContratDTO> dateLivs, List<Date> res) 
	{
		for (DateModeleContratDTO dateLiv :  dateLivs) 
		{
			res.add(dateLiv.dateLiv);
		}
	}
}
