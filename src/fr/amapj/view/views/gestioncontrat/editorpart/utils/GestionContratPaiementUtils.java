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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import fr.amapj.model.models.contrat.modele.GestionPaiement;
import fr.amapj.model.models.contrat.modele.SaisiePaiementCalculDate;
import fr.amapj.model.models.contrat.modele.SaisiePaiementModifiable;
import fr.amapj.model.models.contrat.modele.SaisiePaiementProposition;
import fr.amapj.model.models.contrat.modele.StrategiePaiement;
import fr.amapj.service.services.gestioncontrat.ModeleContratDTO;

public class GestionContratPaiementUtils 
{
	/**
	 * Verifie les champs strategiePaiement et saisiePaiementCalculDate
	 * et les mets à jour si besoin    
	 */
	public void checkAndUpdateStrategieAndSaisiePaiementCalculDate(ModeleContratDTO modeleContrat)
	{
		modeleContrat.saisiePaiementCalculDate = getSaisiePaiementCalculDate(modeleContrat);
		modeleContrat.strategiePaiement = getStrategie(modeleContrat);
	}
	
	
	private SaisiePaiementCalculDate getSaisiePaiementCalculDate(ModeleContratDTO modeleContrat) 
	{
		// 
		if (modeleContrat.gestionPaiement==GestionPaiement.NON_GERE)
		{
			return SaisiePaiementCalculDate.AUTRE;
		}
		
		// 
		if (modeleContrat.saisiePaiementCalculDate==SaisiePaiementCalculDate.AUTRE)
		{
			return SaisiePaiementCalculDate.AUTRE;
		}
		
		//  
		if (isSaisiePaiementCalculDateCorrect(modeleContrat))
		{
			return modeleContrat.saisiePaiementCalculDate;
		}
		
		return SaisiePaiementCalculDate.AUTRE;
	}

	private boolean isSaisiePaiementCalculDateCorrect(ModeleContratDTO modeleContrat) 
	{
		// Les dates en bases
		List<Date> datesInBase = modeleContrat.datePaiements.stream().map(e->e.datePaiement).collect(Collectors.toList());
		
		// Les dates calculées
		List<Date> datesCalculs = new GestionContratDateUtils().calculDatePaiement(modeleContrat.saisiePaiementCalculDate, modeleContrat.dateLivs);
		
		
		return datesCalculs.equals(datesInBase);
	}


	private StrategiePaiement getStrategie(ModeleContratDTO modeleContrat) 
	{
		if (modeleContrat.strategiePaiement==StrategiePaiement.NON_GERE)
		{
			return StrategiePaiement.NON_GERE;
		}
		
		if (modeleContrat.strategiePaiement==StrategiePaiement.PLUS_DE_CHOIX)
		{
			return StrategiePaiement.PLUS_DE_CHOIX;
		}
		
		
		StrategiePaiementEntry e = find(modeleContrat.strategiePaiement);
		
		if (isMatching(e,modeleContrat)==false)
		{
			return StrategiePaiement.PLUS_DE_CHOIX;
		}
		
		return modeleContrat.strategiePaiement;
	}


	/**
	 * Remplit les champs 
	 * 
	 * 		modeleContrat.gestionPaiement
			modeleContrat.saisiePaiementProposition 
			modeleContrat.montantChequeMiniCalculProposition 
			modeleContrat.saisiePaiementModifiable 
			modeleContrat.saisiePaiementCalculDate
			
			en fonction de la strategie
	 * 
	 * 
	 */
	public void fillFieldPaiementFromStrategie(ModeleContratDTO modeleContrat)
	{
		StrategiePaiement st = modeleContrat.strategiePaiement;
		
		// Tout a été saisi en direct, sauf gestionPaiement
		if (st==StrategiePaiement.PLUS_DE_CHOIX)
		{
			modeleContrat.gestionPaiement = GestionPaiement.GESTION_STANDARD;
			return;
		}
		
		StrategiePaiementEntry e = find(st);
		modeleContrat.gestionPaiement =e.gestionPaiement;
		modeleContrat.saisiePaiementProposition = e.saisiePaiementProposition;
		modeleContrat.saisiePaiementModifiable = e.saisiePaiementModifiable;
		modeleContrat.saisiePaiementCalculDate = e.saisiePaiementCalculDate;
		if (e.forceMontantMiniChequeToZero)
		{
			modeleContrat.montantChequeMiniCalculProposition = 0;
		}
	}

	
	// ====================================================
	
	private boolean isMatching(StrategiePaiementEntry e, ModeleContratDTO modeleContrat) 
	{
		if (e.strategiePaiement == modeleContrat.strategiePaiement &&
			e.saisiePaiementProposition==modeleContrat.saisiePaiementProposition && 
			e.saisiePaiementModifiable==modeleContrat.saisiePaiementModifiable &&
			e.saisiePaiementCalculDate==modeleContrat.saisiePaiementCalculDate)
		{
			return true;
		}
		return false;
	}
	
	private StrategiePaiementEntry find(StrategiePaiement strategiePaiement)
	{
		List<StrategiePaiementEntry> res = getAlls();
		for (StrategiePaiementEntry e : res) 
		{
			if (e.strategiePaiement==strategiePaiement)
			{
				return e;
			}
		}
		return null;
	}
	
	
	private List<StrategiePaiementEntry> getAlls()
	{
		List<StrategiePaiementEntry> res = new ArrayList<>();
		
		res.add(new StrategiePaiementEntry(StrategiePaiement.NON_GERE, GestionPaiement.NON_GERE , SaisiePaiementProposition.REPARTI_STRICT, SaisiePaiementModifiable.NON_MODIFIABLE, SaisiePaiementCalculDate.AUTRE,true));	
		res.add(new StrategiePaiementEntry(StrategiePaiement.UN_CHEQUE_PAR_MOIS_LISSE_MODIFIABLE_1ERE_LIVRAISON,GestionPaiement.GESTION_STANDARD,SaisiePaiementProposition.REPARTI_ARRONDI,SaisiePaiementModifiable.MODIFIABLE,SaisiePaiementCalculDate.UN_PAIEMENT_PAR_MOIS_PREMIERE_LIVRAISON,false));
		res.add(new StrategiePaiementEntry(StrategiePaiement.UN_CHEQUE_PAR_MOIS_LISSE_NON_MODIFIABLE_1ERE_LIVRAISON,GestionPaiement.GESTION_STANDARD,SaisiePaiementProposition.REPARTI_ARRONDI,SaisiePaiementModifiable.NON_MODIFIABLE,SaisiePaiementCalculDate.UN_PAIEMENT_PAR_MOIS_PREMIERE_LIVRAISON,false));
		res.add(new StrategiePaiementEntry(StrategiePaiement.UN_PAIEMENT_PAR_LIVRAISON,GestionPaiement.GESTION_STANDARD,SaisiePaiementProposition.PAYE_AVANCE_STRICT,SaisiePaiementModifiable.NON_MODIFIABLE,SaisiePaiementCalculDate.UN_PAIEMENT_PAR_LIVRAISON,true));
		res.add(new StrategiePaiementEntry(StrategiePaiement.UN_CHEQUE_PAR_MOIS_LISSE_MODIFIABLE_1ER_MOIS,GestionPaiement.GESTION_STANDARD,SaisiePaiementProposition.REPARTI_ARRONDI,SaisiePaiementModifiable.MODIFIABLE,SaisiePaiementCalculDate.UN_PAIEMENT_PAR_MOIS_DEBUT_MOIS,false));
		
		return res;
	}
	
	
	
	static public class StrategiePaiementEntry
	{
		public StrategiePaiement strategiePaiement;
		
		public GestionPaiement gestionPaiement;
		
		public SaisiePaiementProposition saisiePaiementProposition; 
		
		public SaisiePaiementModifiable saisiePaiementModifiable;
		
		public SaisiePaiementCalculDate saisiePaiementCalculDate;
		
		public boolean forceMontantMiniChequeToZero;

		public StrategiePaiementEntry(StrategiePaiement strategiePaiement,GestionPaiement gestionPaiement,SaisiePaiementProposition saisiePaiementProposition, SaisiePaiementModifiable saisiePaiementModifiable,
				SaisiePaiementCalculDate saisiePaiementCalculDate,boolean forceMontantMiniChequeToZero) 
		{
			this.strategiePaiement = strategiePaiement;
			this.gestionPaiement = gestionPaiement;
			this.saisiePaiementProposition = saisiePaiementProposition;
			this.saisiePaiementModifiable = saisiePaiementModifiable;
			this.saisiePaiementCalculDate = saisiePaiementCalculDate;
			this.forceMontantMiniChequeToZero = forceMontantMiniChequeToZero;
		}		
	}
	


}
