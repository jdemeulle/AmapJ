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
 package fr.amapj.service.services.archivage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import fr.amapj.common.DateUtils;
import fr.amapj.common.FormatUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.contrat.modele.EtatModeleContrat;
import fr.amapj.model.models.contrat.modele.GestionPaiement;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.model.models.contrat.reel.EtatPaiement;
import fr.amapj.model.models.contrat.reel.Paiement;
import fr.amapj.model.models.remise.RemiseProducteur;
import fr.amapj.service.services.archivage.tools.ArchivableState;
import fr.amapj.service.services.archivage.tools.ArchivableState.AStatus;
import fr.amapj.service.services.archivage.tools.SuppressionState;
import fr.amapj.service.services.archivage.tools.SuppressionState.SStatus;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.gestioncontrat.ModeleContratSummaryDTO;
import fr.amapj.service.services.mescontrats.MesContratsService;
import fr.amapj.service.services.parametres.ParametresArchivageDTO;
import fr.amapj.service.services.remiseproducteur.RemiseProducteurService;

/**
 * Permet la gestion des contrats archivés
 * 
 */
public class ArchivageContratService
{

	// VERIFICATION SUR L ARCHIVAGE UN CONTRAT 
	
	
	public String computeArchivageLib(ParametresArchivageDTO param) 
	{
		String str = "Il est souhaitable d'archiver un contrat qui remplit les conditions suivantes : <ul>"+
				"<li>le contrat a été entièrement livré</li>"+
				"<li>tous les paiements ont été finalisés</li>"+
			 	"<li>la date de dernière livraison est plus vieille que "+param.archivageContrat+" jours</li>"+
			 	"</ul><br/>";
		
		return str;
	}
	
	/**
	 * Vérifie si ce contrat peut être archivé 
	 */
	@DbRead
	public ArchivableState computeArchivageState(Long idModeleContrat,ParametresArchivageDTO param)
	{
		RdbLink em = RdbLink.get();
		ArchivableState res = new ArchivableState();
		
		ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);
		ModeleContratSummaryDTO dto = new GestionContratService().createModeleContratInfo(em, mc);
		
		// Non archivable : le contrat ne doit pas être à l'état CREATION ou ARCHIVE
		if (mc.etat!=EtatModeleContrat.ACTIF)
		{
			res.nonArchivables.add("Le contrat est à l'état "+mc.etat+", il doit être à l'état ACTIF pour pouvoir être archivé.");
		}
		
		// Non archivable : le contrat doit être entièrement livré 
		em.createQuery("select count(distinct(c.modeleContratDate)) from ContratCell c WHERE c.modeleContratDate.dateLiv>=:d AND c.modeleContratDate.modeleContrat.id=:mcid");
		em.setParameter("d", DateUtils.getDateWithNoTime());
		em.setParameter("mcid", idModeleContrat);

		int nbLiv = em.result().singleInt();
		if (nbLiv != 0)
		{
			res.nonArchivables.add("Il existe " + nbLiv	+ " livraisons à réaliser aujourd'hui ou dans le futur.");
		}
		
		// Réserve majeure : les cheques doivent bien étre tous remis
		if (mc.gestionPaiement == GestionPaiement.GESTION_STANDARD)
		{
			em.createQuery("select count(p) from Paiement p WHERE p.contrat.modeleContrat.id=:mcid AND p.etat!=:etat");
			em.setParameter("mcid", idModeleContrat);
			em.setParameter("etat", EtatPaiement.PRODUCTEUR);

			int nbCheque = em.result().singleInt();
			if (nbCheque != 0)
			{
				res.reserveMajeures.add("Il existe " + nbCheque	+ " chèques qui n'ont pas été remis au producteur.");
			}
		}
		
		// Reserve mineure : la dernière date de livraison doit être dépassée d'au moins param.archivageContrat jour 
		Date ref = DateUtils.getDateWithNoTime();
		ref = DateUtils.addDays(ref, -param.archivageContrat);
		if (dto.dateFin!=null && dto.dateFin.after(ref))
		{
			res.reserveMineures.add("La date de la dernière livraison est est assez récente : "+FormatUtils.getStdDate().format(dto.dateFin));
		}
		
		return res;
	}
	
	
	
	
	// PARTIE REQUETAGE POUR AVOIR LA LISTE DES CONTRATS ACTIFS QU'IL EST SOUHAITABLE D'ARCHIVER
	
	/**
	 * Récupère la liste des contrats archivables 
	 * 
	 */
	public List<ModeleContratSummaryDTO> getAllContratsArchivables(ParametresArchivageDTO param)
	{
		List<ModeleContratSummaryDTO> mcs = new GestionContratService().getModeleContratInfo(EtatModeleContrat.ACTIF);
		
		List<ModeleContratSummaryDTO> res = new ArrayList<ModeleContratSummaryDTO>();
		for (ModeleContratSummaryDTO mc : mcs) 
		{
			ArchivableState state = computeArchivageState(mc.id, param);
			
			if (state.getStatus()==AStatus.OUI_SANS_RESERVE)
			{
				res.add(mc);
			}
		}
		
		res.sort(Comparator.comparing(e->e.dateFin));
		
		return res;

	}
	
	
	// VERIFICATION SUR LA SUPPRESSION UN CONTRAT
	

	public String computeSuppressionLib(ParametresArchivageDTO param) 
	{
		String str = "Il est souhaitable de supprimer un contrat qui remplit les conditions suivantes : <ul>"+
				"<li>le contrat est à l'état Archivé</li>"+
			 	"<li>la date de dernière livraison est plus vieille que "+param.suppressionContrat+" jours</li>"+
			 	"</ul><br/>";
		
		return str;
	}
	
	/**
	 * Vérifie si ce contrat peut être supprimé 
	 */
	public SuppressionState computeSuppressionState(ModeleContratSummaryDTO mc,ParametresArchivageDTO param)
	{
		SuppressionState res = new SuppressionState();
		
		// Non supprimable : le contrat doit être à l'état ARCHIVE
		if (mc.etat!=EtatModeleContrat.ARCHIVE)
		{
			res.nonSupprimables.add("Le contrat est à l'état "+mc.etat+", il doit être à l'état ARCHIVE pour pouvoir être supprimé.");
		}
		
		// Reserve majeure : la dernière date de livraison doit être dépassée d'au moins param.suppressionContrat jour 
		Date ref = DateUtils.getDateWithNoTime();
		ref = DateUtils.addDays(ref, -param.suppressionContrat);
		if (mc.dateFin.after(ref))
		{
			res.reserveMajeures.add("La date de la dernière livraison est trop récente : "+FormatUtils.getStdDate().format(mc.dateFin));
		}
		
		return res;
	}
	
	
	
	
	
	// PARTIE REQUETAGE POUR AVOIR LA LISTE DES CONTRATS ARCHIVES QU'IL EST SOUHAITABLE DE SUPPRIMER
	
	/**
	 * Récupère la liste des contrats archivés qui peuvent être supprimés
	 */
	public List<ModeleContratSummaryDTO> getAllContratSupprimables(ParametresArchivageDTO param) 
	{
		List<ModeleContratSummaryDTO> mcs = new GestionContratService().getModeleContratInfo(EtatModeleContrat.ARCHIVE);

		List<ModeleContratSummaryDTO> res = new ArrayList<ModeleContratSummaryDTO>();
		for (ModeleContratSummaryDTO mc : mcs) 
		{
			SuppressionState state = computeSuppressionState(mc, param);
			if (state.getStatus()==SStatus.OUI_SANS_RESERVE)
			{
				res.add(mc);
			}
		}
		res.sort(Comparator.comparing(e->e.dateFin));
		
		return res;
	}
	
	
	

	// PARTIE SUPPRESSION D'UN MODELE DE CONTRAT ET DE TOUS LES CONTRATS
	// ASSOCIES

	/**
	 * Permet de supprimer un modele de contrat et TOUS les contrats associées
	 * Ceci est fait dans une transaction en ecriture
	 */
	@DbWrite
	public void deleteModeleContratAndContrats(Long modeleContratId)
	{
		RdbLink em = RdbLink.get();
		ModeleContrat mc = em.find(ModeleContrat.class, modeleContratId);

		// On supprime d'abord toutes les remises
		List<RemiseProducteur> remises = getAllRemises(em, mc);
		for (RemiseProducteur remiseProducteur : remises)
		{
			new RemiseProducteurService().deleteRemise(remiseProducteur.getId());
		}

		// On supprime ensuite tous les paiements
		List<Paiement> paiements = getAllPaiements(em, mc);
		for (Paiement paiement : paiements)
		{
			em.remove(paiement);
		}

		// On supprime ensuite tous les contrats
		List<Contrat> cs = getAllContrats(em, mc);
		for (Contrat contrat : cs)
		{
			new MesContratsService().deleteContrat(contrat.getId());
		}

		// On supprime ensuite le modele de contrat
		new GestionContratService().deleteContrat(modeleContratId);

	}

	private List<RemiseProducteur> getAllRemises(RdbLink em, ModeleContrat mc)
	{
		TypedQuery<RemiseProducteur> q = em.createQuery("select r from RemiseProducteur r  WHERE r.datePaiement.modeleContrat=:mc ORDER BY r.datePaiement.datePaiement desc",RemiseProducteur.class);
		q.setParameter("mc", mc);
		return q.getResultList();
	}

	private List<Paiement> getAllPaiements(RdbLink em, ModeleContrat mc)
	{
		TypedQuery<Paiement> q = em.createQuery("select p from Paiement p  WHERE p.contrat.modeleContrat=:mc",Paiement.class);
		q.setParameter("mc", mc);
		return q.getResultList();
	}

	/**
	 * 
	 */
	private List<Contrat> getAllContrats(RdbLink em, ModeleContrat mc)
	{
		TypedQuery<Contrat> q = em.createQuery("select c from Contrat c WHERE c.modeleContrat=:mc",Contrat.class);
		q.setParameter("mc", mc);
		return q.getResultList();
	}




}
