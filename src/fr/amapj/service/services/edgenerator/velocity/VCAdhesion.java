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
 package fr.amapj.service.services.edgenerator.velocity;

import java.text.SimpleDateFormat;

import fr.amapj.common.DateUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.cotisation.PeriodeCotisation;
import fr.amapj.model.models.cotisation.PeriodeCotisationUtilisateur;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;


public class VCAdhesion
{
	public String nomPeriode;
	
	public String dateDebut;
	
	public String dateFin;
	
	private String montantAdhesion;
	
	private String montantMini;
	
	private String montantConseille;
	
	private String textPaiement;
	
	private String libCheque;
	
	private String dateAdhesion;
	

	
	public void load(PeriodeCotisation pc, PeriodeCotisationUtilisateur pcu,int montant, RdbLink em)
	{
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		
		CurrencyTextFieldConverter ctc = new CurrencyTextFieldConverter();
	
		nomPeriode = s(pc.nom);
		dateDebut = df.format(pc.dateDebut);
		dateFin = df.format(pc.dateFin);
		montantMini = ctc.convertToString(pc.montantMini);
		montantConseille = ctc.convertToString(pc.montantConseille);
		textPaiement = s(pc.textPaiement);
		libCheque = s(pc.libCheque);
		
		if (pcu!=null)
		{
			montantAdhesion = ctc.convertToString(pcu.montantAdhesion);
			dateAdhesion = df.format(pcu.dateAdhesion);
		}
		else
		{
			montantAdhesion = ctc.convertToString(montant);
			dateAdhesion = df.format(DateUtils.getDate());
		}
		
	}

	/**
	 * Permet d'escaper les caracteres HTML  
	 */
	private String s(String value)
	{
		return VCBuilder.s(value);
	}

	
	// Getters and setters pour Velocity 
	
	
	public String getNomPeriode()
	{
		return nomPeriode;
	}

	public void setNomPeriode(String nomPeriode)
	{
		this.nomPeriode = nomPeriode;
	}

	public String getDateDebut()
	{
		return dateDebut;
	}

	public void setDateDebut(String dateDebut)
	{
		this.dateDebut = dateDebut;
	}

	public String getDateFin()
	{
		return dateFin;
	}

	public void setDateFin(String dateFin)
	{
		this.dateFin = dateFin;
	}

	public String getMontantAdhesion()
	{
		return montantAdhesion;
	}

	public void setMontantAdhesion(String montantAdhesion)
	{
		this.montantAdhesion = montantAdhesion;
	}

	public String getMontantMini()
	{
		return montantMini;
	}

	public void setMontantMini(String montantMini)
	{
		this.montantMini = montantMini;
	}

	public String getMontantConseille()
	{
		return montantConseille;
	}

	public void setMontantConseille(String montantConseille)
	{
		this.montantConseille = montantConseille;
	}

	public String getTextPaiement()
	{
		return textPaiement;
	}

	public void setTextPaiement(String textPaiement)
	{
		this.textPaiement = textPaiement;
	}

	public String getLibCheque()
	{
		return libCheque;
	}

	public void setLibCheque(String libCheque)
	{
		this.libCheque = libCheque;
	}

	public String getDateAdhesion()
	{
		return dateAdhesion;
	}

	public void setDateAdhesion(String dateAdhesion)
	{
		this.dateAdhesion = dateAdhesion;
	}	
}
