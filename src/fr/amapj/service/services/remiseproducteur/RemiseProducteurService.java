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
 package fr.amapj.service.services.remiseproducteur;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import fr.amapj.common.DateUtils;
import fr.amapj.common.FormatUtils;
import fr.amapj.common.StringUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.ModeleContratDatePaiement;
import fr.amapj.model.models.contrat.reel.EtatPaiement;
import fr.amapj.model.models.contrat.reel.Paiement;
import fr.amapj.model.models.remise.RemiseProducteur;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;

/**
 * Permet la gestion des remises aux producteurs
 * 
 * 
 * 
 */
public class RemiseProducteurService
{

	public RemiseProducteurService()
	{

	}

	// PARTIE REQUETAGE POUR AVOIR LA LISTE DES REMISES DEJA FAITES

	/**
	 * Permet de charger la liste de tous les remises dans une transaction en lecture
	 * 
	 * Retourne la liste des remises, ordonnée par ordre de date
	 */
	@DbRead
	public List<RemiseDTO> getAllRemise(Long idModeleContrat)
	{
		RdbLink em = RdbLink.get();

		ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);

		List<RemiseDTO> res = new ArrayList<RemiseDTO>();

		Query q = em.createQuery("select r from RemiseProducteur r " + "WHERE r.datePaiement.modeleContrat=:mc " + "ORDER BY r.datePaiement.datePaiement");
		q.setParameter("mc", mc);

		List<RemiseProducteur> remises = q.getResultList();

		SimpleDateFormat df = FormatUtils.getLiteralMonthDate();
		for (RemiseProducteur remise : remises)
		{
			RemiseDTO mcInfo = createRemiseInfo(em, remise, df);
			res.add(mcInfo);
		}

		return res;

	}

	/**
	 * 
	 * TODO on pourrait vérifier à ce niveau si il y a un écart entre la somme des montants des cheques et le montant global de la remise
	 * 
	 * @param em
	 * @param remise
	 * @param df
	 * @return
	 */
	public RemiseDTO createRemiseInfo(RdbLink em, RemiseProducteur remise, SimpleDateFormat df)
	{
		RemiseDTO info = new RemiseDTO();

		info.idModeleContrat = remise.datePaiement.modeleContrat.getId();
		info.dateCreation = remise.dateCreation;
		info.dateReelleRemise = remise.dateRemise;
		info.dateTheoRemise = remise.datePaiement.datePaiement;
		info.id = remise.getId();
		info.mnt = remise.montant;
		info.moisRemise = df.format(info.dateTheoRemise);
		info.nbCheque = getNbCheque(em, remise);

		return info;
	}

	private int getNbCheque(RdbLink em, RemiseProducteur remise)
	{
		Query q = em.createQuery("select count(p) from Paiement p WHERE p.remise=:r");
		q.setParameter("r", remise);

		return ((Long) q.getSingleResult()).intValue();
	}

	// PARTIE REQUETAGE POUR AVOIR LA LISTE DES REMISES A VENIR

	/**
	 * 
	 */
	@DbRead
	public List<RemiseDTO> getNextRemise(Long idModeleContrat)
	{
		RdbLink em = RdbLink.get();

		ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);

		List<ModeleContratDatePaiement> dates = getDatePaiementSansRemise(em, mc);
		SimpleDateFormat df = FormatUtils.getLiteralMonthDate();

		List<RemiseDTO> res = new ArrayList<RemiseDTO>();
		for (ModeleContratDatePaiement modeleContratDatePaiement : dates)
		{
			RemiseDTO dto = createVirtualRemise(modeleContratDatePaiement, em, idModeleContrat, df);
			res.add(dto);
		}
		return res;
	}

	private RemiseDTO createVirtualRemise(ModeleContratDatePaiement datePaiement, RdbLink em, Long idModeleContrat, SimpleDateFormat df)
	{
		RemiseDTO info = new RemiseDTO();
		info.idModeleContratDatePaiement = datePaiement.getId();
		info.dateTheoRemise = datePaiement.datePaiement;
		info.idModeleContrat = idModeleContrat;
		info.moisRemise = df.format(info.dateTheoRemise);

		List<Paiement> paiements = getPaiement(em, datePaiement);
		info.nbCheque = paiements.size();
		info.mnt = 0;

		for (Paiement paiement : paiements)
		{
			info.mnt = info.mnt + paiement.montant;

			PaiementRemiseDTO dto = new PaiementRemiseDTO();
			dto.idPaiement = paiement.getId();
			dto.montant = paiement.montant;
			dto.nomUtilisateur = paiement.contrat.utilisateur.nom;
			dto.prenomUtilisateur = paiement.contrat.utilisateur.prenom;
			dto.etatPaiement = paiement.etat;
			dto.commentaire1 = paiement.commentaire1;
			dto.commentaire2 = paiement.commentaire2;
			dto.commentaire3 = paiement.commentaire3;
			dto.commentaire4 = paiement.commentaire4;

			info.paiements.add(dto);
		}

		return info;
	}

	// PREPARATION DE LA REMISE A VENIR

	@DbRead
	public RemiseDTO prepareRemise(Long idModeleContrat)
	{
		List<RemiseDTO> nextRemises = getNextRemise(idModeleContrat);

		//
		if (nextRemises.size() == 0)
		{
			RemiseDTO dto = new RemiseDTO();
			dto.preparationRemiseFailed = true;
			dto.messageRemiseFailed = "Il n'y a plus de remise à faire pour ce contrat";
			return dto;
		}

		RemiseDTO nextRemise = nextRemises.get(0);

		List<String> chequesManquants = getChequesManquants(nextRemise);

		if (chequesManquants.size() != 0)
		{
			nextRemise.preparationRemiseFailed = true;
			nextRemise.messageRemiseFailed = "Il n'est pas possible de réaliser la remise. Il manque les chèques de : <br/>"
					+ StringUtils.asString(chequesManquants, "<br/>") + getInfo();
			return nextRemise;
		}

		nextRemise.preparationRemiseFailed = false;
		return nextRemise;
	}

	private String getInfo()
	{
		return "<br/><br/>Pour résoudre le problème, vous devez aller dans Réception des chèques, et supprimer les chèques qui vous manquent, ou alors les reporter sur un autre mois"; 
	}

	private List<String> getChequesManquants(RemiseDTO nextRemise)
	{
		List<String> strs = new ArrayList<String>();
		// On vérifie que tous les paiements sont bons
		for (PaiementRemiseDTO paiement : nextRemise.paiements)
		{
			if (paiement.etatPaiement != EtatPaiement.AMAP)
			{
				strs.add(paiement.nomUtilisateur + " " + paiement.prenomUtilisateur + " - "
						+ new CurrencyTextFieldConverter().convertToString(paiement.montant) + " €");
			}
		}
		return strs;
	}

	/**
	 * Retourne la liste des date de paiement sans remise
	 * 
	 * @return
	 */
	public List<ModeleContratDatePaiement> getDatePaiementSansRemise(RdbLink em, ModeleContrat mc)
	{
		Query q = em.createQuery("select d from ModeleContratDatePaiement d WHERE" + " d.modeleContrat=:mc and "
				+ " NOT EXISTS (select r from RemiseProducteur r where r.datePaiement = d) " + "order by d.datePaiement");

		q.setParameter("mc", mc);
		List<ModeleContratDatePaiement> us = q.getResultList();
		return us;
	}

	/**
	 * Retourne la liste des paiements pour une date, ordonnée par nom et prénom des utilisateurs
	 * 
	 * @param em
	 * @param datePaiement
	 * @return
	 */
	private List<Paiement> getPaiement(RdbLink em, ModeleContratDatePaiement datePaiement)
	{
		Query q = em.createQuery("select p from Paiement p WHERE p.modeleContratDatePaiement=:d "
				+ "ORDER BY p.contrat.utilisateur.nom , p.contrat.utilisateur.prenom");
		q.setParameter("d", datePaiement);

		return ((List<Paiement>) q.getResultList());
	}

	@DbWrite
	public Long performRemise(RemiseDTO remiseDTO)
	{
		RdbLink em = RdbLink.get();

		RemiseProducteur remiseProducteur = new RemiseProducteur();
		remiseProducteur.dateCreation = DateUtils.getDate();
		remiseProducteur.datePaiement = em.find(ModeleContratDatePaiement.class, remiseDTO.idModeleContratDatePaiement);
		remiseProducteur.dateRemise = remiseDTO.dateReelleRemise;
		remiseProducteur.montant = remiseDTO.mnt;
		em.persist(remiseProducteur);

		for (PaiementRemiseDTO paiement : remiseDTO.paiements)
		{
			Paiement p = em.find(Paiement.class, paiement.idPaiement);
			p.etat = EtatPaiement.PRODUCTEUR;
			p.remise = remiseProducteur;
		}
		
		return remiseProducteur.id;
	}

	// PARTIE EFFACEMENT
	@DbWrite
	public void deleteRemise(Long idItemToSuppress)
	{
		RdbLink em = RdbLink.get();

		RemiseProducteur remiseProducteur = em.find(RemiseProducteur.class, idItemToSuppress);

		List<Paiement> paiements = getPaiement(em, remiseProducteur);
		for (Paiement p : paiements)
		{
			p.etat = EtatPaiement.AMAP;
			p.remise = null;
		}
		em.remove(remiseProducteur);

	}

	/**
	 * Retourne la liste des paiements ordonnés par nom d'utilisateur
	 * 
	 * @param em
	 * @param remise
	 * @return
	 */
	private List<Paiement> getPaiement(RdbLink em, RemiseProducteur remise)
	{
		Query q = em.createQuery("select p from Paiement p " + "WHERE p.remise=:r " + "ORDER BY p.contrat.utilisateur.nom, p.contrat.utilisateur.prenom");
		q.setParameter("r", remise);

		return ((List<Paiement>) q.getResultList());
	}

	// PARTIE CHARGEMENT
	@DbRead
	public RemiseDTO loadRemise(Long idRemise)
	{
		RdbLink em = RdbLink.get();

		SimpleDateFormat df = FormatUtils.getLiteralMonthDate();
		RemiseProducteur remiseProducteur = em.find(RemiseProducteur.class, idRemise);
		RemiseDTO res = createRemiseInfo(em, remiseProducteur, df);

		List<Paiement> paiements = getPaiement(em, remiseProducteur);
		for (Paiement paiement : paiements)
		{
			PaiementRemiseDTO dto = new PaiementRemiseDTO();
			dto.idPaiement = paiement.getId();
			dto.etatPaiement = paiement.etat;
			dto.montant = paiement.montant;
			dto.nomUtilisateur = paiement.contrat.utilisateur.nom;
			dto.prenomUtilisateur = paiement.contrat.utilisateur.prenom;
			dto.commentaire1 = paiement.commentaire1;
			dto.commentaire2 = paiement.commentaire2;
			dto.commentaire3 = paiement.commentaire3;
			dto.commentaire4 = paiement.commentaire4;
			
			res.paiements.add(dto);

			if (paiement.etat != EtatPaiement.PRODUCTEUR)
			{
				throw new RuntimeException("Erreur pour " + paiement.contrat.utilisateur.nom
						+ paiement.contrat.utilisateur.prenom + " le cheque n'a pas le bon état");
			}

		}

		return res;

	}


	
	
	// Date de paiement d'un contrat avec le statut de la date de paiement vis à vis de la remise
	
	@DbRead
	public List<ModeleContratDatePaiementInfo> getAllDatesPaiementsInfo(RdbLink em, ModeleContrat mc)
	{
		// On récupère la liste de tous les paiements de ce contrat
		TypedQuery<ModeleContratDatePaiement> q = em.createQuery("select p from ModeleContratDatePaiement p WHERE p.modeleContrat=:mc order by p.datePaiement",ModeleContratDatePaiement.class);
		q.setParameter("mc",mc);
		List<ModeleContratDatePaiement> paiements = q.getResultList();
		
		// On récupère la liste de toutes les remises de ce contrat 
		TypedQuery<RemiseProducteur> q2 = em.createQuery("select r from RemiseProducteur r  WHERE r.datePaiement.modeleContrat=:mc",RemiseProducteur.class);
		q2.setParameter("mc", mc);
		List<RemiseProducteur> remises = q2.getResultList();
		
		
		List<ModeleContratDatePaiementInfo> res = new ArrayList<>();
		for (ModeleContratDatePaiement modeleContratDatePaiement : paiements)
		{
			ModeleContratDatePaiementInfo info = new ModeleContratDatePaiementInfo();
			info.modeleContratDatePaiement = modeleContratDatePaiement;
			info.remiseDone = computeRemiseDone(modeleContratDatePaiement,remises);
			res.add(info);
		}
	
		return res;
	}
	
	private boolean computeRemiseDone(ModeleContratDatePaiement modeleContratDatePaiement,List<RemiseProducteur> remises)
	{
		for (RemiseProducteur remiseProducteur : remises)
		{
			if (remiseProducteur.datePaiement.id.equals(modeleContratDatePaiement.id))
			{
				return true;
			}
		}
		return false;
	}

	static public class ModeleContratDatePaiementInfo
	{
		public ModeleContratDatePaiement modeleContratDatePaiement;
		
		public boolean remiseDone;
	}
	

}
