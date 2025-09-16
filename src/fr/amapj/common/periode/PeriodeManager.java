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
 package fr.amapj.common.periode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.threeten.extra.YearQuarter;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.DateUtils;
import fr.amapj.common.FormatUtils;

public class PeriodeManager
{
	private TypPeriode typPeriode;
	private LocalDateTime now;
	private int nbJourAvant;
	private int nbJourApres;
	private BiFunction<LocalDate, LocalDate, List<LocalDate>> allowedDateProvider;

	
	static public class Periode
	{
		public TypPeriode typPeriode;
		public LocalDate startDate;
		public LocalDate endDate;
		
		public String getLib()
		{
			switch (typPeriode)
			{
			case JOUR:
				return FormatUtils.getFullDate().format(DateUtils.asDate(startDate)); 
				
			case MOIS:
				return FormatUtils.getMoisFullText().format(DateUtils.asDate(startDate)); 
				
			case TRIMESTRE:
				return FormatUtils.formatTrimestreFullText(DateUtils.asDate(startDate)); 
				
			default:
				throw new AmapjRuntimeException();
			}
		}
	}
	
	/**
	 * Les periodes sont disponibles x jours avant leur date de début.  x = nbJourAvant
	 * 
	 * Les periodes sont disponibles y jours après leur date de fin.  y = nbJourApres
		
	 * @param now
	 * @param typPeriode
	 * @param nbJourAvant
	 * @param nbJourApres
	 */
	public PeriodeManager(LocalDateTime now,TypPeriode typPeriode,int nbJourAvant,int nbJourApres,BiFunction<LocalDate,LocalDate,List<LocalDate>> allowedDateProvider)
	{
		this.now = now;
		this.typPeriode = typPeriode;
		this.nbJourAvant = nbJourAvant;
		this.nbJourApres = nbJourApres;
		this.allowedDateProvider = allowedDateProvider;
	}
	
	public PeriodeManager(LocalDateTime now,TypPeriode typPeriode,int nbJourAvant,int nbJourApres)
	{
		this(now, typPeriode, nbJourAvant, nbJourApres, null);
	}
	
	public List<Periode> getAllPeriodes()
	{
		switch (typPeriode)
		{
		
		case JOUR:
			return getJour();
		
		case MOIS:
			return getMois();
			
		case TRIMESTRE:
			return getTrimestre();
			
		default:
			throw new AmapjRuntimeException();
		}
	}
	
	
	


	private List<Periode> getJour()
	{
		if (allowedDateProvider==null)
		{
			throw new AmapjRuntimeException();
		}
		
		
		LocalDate first = now.toLocalDate().minusDays(nbJourApres);
		LocalDate last = now.toLocalDate().plusDays(nbJourAvant);
		
		
		List<Periode> res = new ArrayList<>();
		List<LocalDate> ls = allowedDateProvider.apply(first, last);
		for (LocalDate l : ls)
		{
			Periode p = new Periode();
			p.typPeriode = TypPeriode.JOUR;
			p.startDate = l;
			p.endDate = l;
			
			res.add(p);
		}
		return res;
	}

	private List<Periode> getMois()
	{
		List<Periode> res = new ArrayList<>();
		
		// Premiere periode
		YearMonth first = now.toLocalDate().minusDays(nbJourApres).query(YearMonth::from);
		
		// Derniere periode
		YearMonth last = now.toLocalDate().plusDays(nbJourAvant).query(YearMonth::from);
		
		while(first.isBefore(last) || first.equals(last))
		{
			Periode p = new Periode();
			p.typPeriode = TypPeriode.MOIS;
			p.startDate = first.atDay(1);
			p.endDate = first.atEndOfMonth();
			
			res.add(p);
			
			first = first.plusMonths(1);
		}
		
		return res;
	}
	
	
	private List<Periode> getTrimestre()
	{
		List<Periode> res = new ArrayList<>();
		
		// Premiere periode
		YearQuarter first = now.toLocalDate().minusDays(nbJourApres).query(YearQuarter::from);
		
		// Derniere periode
		YearQuarter last = now.toLocalDate().plusDays(nbJourAvant).query(YearQuarter::from);
		
		while(first.isBefore(last) || first.equals(last))
		{
			Periode p = new Periode();
			p.typPeriode = TypPeriode.TRIMESTRE;
			p.startDate = first.atDay(1);
			p.endDate = first.atEndOfQuarter();
			
			res.add(p);
			
			first = first.plusQuarters(1);
		}
		
		return res;
	}
	
	
	 
}
