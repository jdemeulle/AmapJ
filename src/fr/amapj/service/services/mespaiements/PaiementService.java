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
 package fr.amapj.service.services.mespaiements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.DateUtils;
import fr.amapj.common.SQLUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.contrat.modele.GestionPaiement;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.ModeleContratDatePaiement;
import fr.amapj.model.models.contrat.modele.SaisiePaiementCalculDate;
import fr.amapj.model.models.contrat.modele.SaisiePaiementModifiable;
import fr.amapj.model.models.contrat.modele.SaisiePaiementProposition;
import fr.amapj.model.models.contrat.modele.StrategiePaiement;
import fr.amapj.model.models.contrat.reel.EtatPaiement;
import fr.amapj.model.models.contrat.reel.Paiement;
import fr.amapj.service.services.gestioncontrat.DatePaiementModeleContratDTO;
import fr.amapj.service.services.gestioncontrat.ModeleContratDTO;
import fr.amapj.service.services.gestioncontratsigne.update.GestionContratSigneUpdateService;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;

/**
 * Gestion des paiements d'un modele de contrat
 *
 */
public class PaiementService 
{
	
	
	public List<ModeleContratDatePaiement> getAllDatesPaiements(RdbLink em, ModeleContrat mc)
	{
		// On récupère ensuite la liste de tous les paiements de ce contrat
		Query q = em.createQuery("select p from ModeleContratDatePaiement p WHERE p.modeleContrat=:mc order by p.datePaiement");
		q.setParameter("mc",mc);
		List<ModeleContratDatePaiement> paiements = q.getResultList();
		return paiements;
	}
	
	
	/**
	 * Permet de mettre à jour uniquement les instructions de paiement et l'affichage du montant, pour le cas des contrats
	 * sans gestion des paiements 
	 */
	@DbWrite
	public void updateTextePaiement(ModeleContratDTO dto)
	{
		RdbLink em = RdbLink.get();
		ModeleContrat mc = em.find(ModeleContrat.class, dto.id);
		
		// Assertion
		if (mc.gestionPaiement!=GestionPaiement.NON_GERE)
		{
			throw new AmapjRuntimeException();
		}
		
		mc.affichageMontant = dto.affichageMontant;
		mc.textPaiement = dto.textPaiement;
		
	}
	
	
	/**
	 * Permet de mettre à jour toutes les informations de paiements
	 */
	@DbWrite
	public void updateInfoPaiement(ModeleContratDTO modeleContrat,boolean updateDate)
	{
		RdbLink em = RdbLink.get();
		
		// Assertion
		if (modeleContrat.gestionPaiement==GestionPaiement.NON_GERE)
		{
			if (modeleContrat.datePaiements.size()!=0)
			{
				throw new AmapjRuntimeException();
			}
		}
		
		
		ModeleContrat mc = em.find(ModeleContrat.class, modeleContrat.id);
		
		// Sauvegarde des info de paiements
		mc.gestionPaiement = modeleContrat.gestionPaiement;
		mc.textPaiement = modeleContrat.textPaiement;
		mc.affichageMontant = modeleContrat.affichageMontant;
		mc.dateRemiseCheque = modeleContrat.dateRemiseCheque;
		mc.libCheque = modeleContrat.libCheque;
		mc.saisiePaiementModifiable = modeleContrat.saisiePaiementModifiable;
		mc.saisiePaiementProposition = modeleContrat.saisiePaiementProposition;
		mc.montantChequeMiniCalculProposition = modeleContrat.montantChequeMiniCalculProposition;
		mc.strategiePaiement = modeleContrat.strategiePaiement;
		mc.saisiePaiementCalculDate = modeleContrat.saisiePaiementCalculDate;
		
		if (updateDate)
		{
	
			// Avec une sous requete, on obtient la liste de toutes les dates de
			// paiement  actuellement en base et on les efface
			List<ModeleContratDatePaiement> datesInBase = getAllDatesPaiements(em, mc);
			for (ModeleContratDatePaiement datePaiement : datesInBase)
			{
				em.remove(datePaiement);
			}
	
			// Création de toutes les lignes pour chacune des dates
			for (DatePaiementModeleContratDTO datePaiement : modeleContrat.datePaiements)
			{
				ModeleContratDatePaiement md = new ModeleContratDatePaiement();
				md.modeleContrat = mc;
				md.datePaiement = datePaiement.datePaiement;
				em.persist(md);
			}
		}
		
		// On marque tous les contrats comme modifiés 
		new GestionContratSigneUpdateService().markAllContratAsModified(mc,em);
	}
	
	
	/**
	 * Passe un contrat de l'etat AVEC_GESTION à SANS_GESTION
	 */
	@DbWrite
	public void deleteInfoPaiement(ModeleContratDTO modeleContrat) 
	{
		RdbLink em = RdbLink.get();		
		
		ModeleContrat mc = em.find(ModeleContrat.class, modeleContrat.id);
		
		// Sauvegarde des info de paiements
		mc.gestionPaiement = GestionPaiement.NON_GERE;
		mc.textPaiement = modeleContrat.textPaiement;
		mc.affichageMontant = modeleContrat.affichageMontant;
		mc.dateRemiseCheque = null;
		mc.libCheque = null;
		mc.saisiePaiementModifiable = SaisiePaiementModifiable.NON_MODIFIABLE;
		mc.saisiePaiementProposition = SaisiePaiementProposition.REPARTI_STRICT;
		mc.montantChequeMiniCalculProposition = 0;
		mc.strategiePaiement = StrategiePaiement.NON_GERE;
		mc.saisiePaiementCalculDate = SaisiePaiementCalculDate.AUTRE;
		
		// On recherche tous les paiements à l'état A Fournir et on les efface
		TypedQuery<Paiement> q = em.createQuery("select p from Paiement p WHERE p.contrat.modeleContrat.id=:id AND p.etat=:etat",Paiement.class);
		q.setParameter("id", mc.id);
		q.setParameter("etat", EtatPaiement.A_FOURNIR);
		SQLUtils.deleteAll(em, q);
		
		// on obtient la liste de toutes les dates de paiement  actuellement en base et on les efface
		List<ModeleContratDatePaiement> datesInBase = getAllDatesPaiements(em, mc);
		for (ModeleContratDatePaiement datePaiement : datesInBase)
		{
			em.remove(datePaiement);
		}
		
		// On marque tous les contrats comme modifiés 
		new GestionContratSigneUpdateService().markAllContratAsModified(mc,em);
	}
	
	
	
	
	/**
	 * Permet la mise à jour des dates de paiement, c'est à dire la suppression et l'ajout de date 
	 */
	@DbWrite
	public void updateDatePaiement(ModeleContratDTO modeleContrat) throws OnSaveException
	{
		RdbLink em = RdbLink.get();
		
		ModeleContrat mc = em.find(ModeleContrat.class, modeleContrat.id);
		
		// Avec une sous requete, on obtient la liste de toutes les dates de
		// paiement  actuellement en base 
		List<ModeleContratDatePaiement> dateInBases = getAllDatesPaiements(em, mc);
		
		// On calcule la liste des dates , en verifiant les doublons et en supprimant la notion d'heure 
		List<Date> dates = extractDates(modeleContrat.datePaiements);
		
		// On cherche les dates à supprimer
		for (ModeleContratDatePaiement dateInBase : dateInBases)
		{
			Date ref = DateUtils.suppressTime(dateInBase.datePaiement); 
					
			// La date en base n'est pas dans la liste des dates
			if (dates.contains(ref)==false)
			{
				int r = getNbPaiementForDate(em, dateInBase);
				if (r>0)
				{
					SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
					String message = "Impossible de supprimer la date de paiement "+df.format(dateInBase.datePaiement)+
										" car il y a "+r+" paiements prévus à cette date";
							
					throw new OnSaveException(message);
				}
				
				em.remove(dateInBase);
			}
		}
		
		// On cherche les dates à ajouter
		for (Date date : dates)
		{
			// La date en cours ne fait pas partie des dates en base
			if (contains(dateInBases,date)==false)
			{
				ModeleContratDatePaiement md = new ModeleContratDatePaiement();
				md.modeleContrat = mc;
				md.datePaiement = date;
				em.persist(md);
			}
		}
		
		// On marque tous les contrats comme modifiés 
		new GestionContratSigneUpdateService().markAllContratAsModified(mc,em);
		
	}
	
	private boolean contains(List<ModeleContratDatePaiement> dateInBases, Date date)
	{
		for (ModeleContratDatePaiement mcdp : dateInBases)
		{
			Date ref = DateUtils.suppressTime(mcdp.datePaiement);
			if (ref.equals(date))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Retourne le nombre d'adherent ayant souscrit à ce modele de contrat
	 * @param em
	 * @param mc
	 * @return
	 */
	private int getNbPaiementForDate(RdbLink em, ModeleContratDatePaiement mcdp)
	{
		Query q = em.createQuery("select count(p.id) from Paiement p WHERE p.modeleContratDatePaiement=:mcdp");
		q.setParameter("mcdp",mcdp);
		return ((Long) q.getSingleResult()).intValue();
	}
	

	
	private List<Date> extractDates(List<DatePaiementModeleContratDTO> datePaiements) throws OnSaveException
	{
		List<Date> res = new ArrayList<>();
		
		for (DatePaiementModeleContratDTO dto : datePaiements)
		{
			Date date = DateUtils.suppressTime(dto.datePaiement);
			if (res.contains(date)==true)
			{
				throw new OnSaveException("Il y a une date de paiement en doublon");
			}
			res.add(date);
		}
		
		return res;
	}
	
	
	
	/**
	 * Retourne true si tous les paiements de ce contrat sont à l'état A_FOURNIR
	 */
	@DbRead
	public boolean canSuppressPaiementModeleContrat(Long modeleContratId)
	{
		RdbLink em = RdbLink.get();
		
		TypedQuery<Paiement> q = em.createQuery("select p from Paiement p WHERE p.contrat.modeleContrat.id=:id",Paiement.class);
		q.setParameter("id", modeleContratId);
		
		
		for (Paiement paiement : q.getResultList()) 
		{
			if (paiement.etat!=EtatPaiement.A_FOURNIR)
			{
				return false;
			}
		}
		return true;
	}




}
