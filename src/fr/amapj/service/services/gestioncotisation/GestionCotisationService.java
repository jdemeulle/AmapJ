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
 package fr.amapj.service.services.gestioncotisation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.common.CollectionUtils;
import fr.amapj.common.DateUtils;
import fr.amapj.common.LongUtils;
import fr.amapj.common.SQLUtils;
import fr.amapj.model.engine.IdentifiableUtil;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.cotisation.EtatPaiementAdhesion;
import fr.amapj.model.models.cotisation.PeriodeCotisation;
import fr.amapj.model.models.cotisation.PeriodeCotisationUtilisateur;
import fr.amapj.model.models.editionspe.EditionSpecifique;
import fr.amapj.model.models.fichierbase.EtatUtilisateur;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.service.engine.tools.DbToDto;
import fr.amapj.service.services.archivage.tools.SuppressionState;
import fr.amapj.service.services.archivage.tools.SuppressionState.SStatus;
import fr.amapj.service.services.parametres.ParametresArchivageDTO;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.suppressionpopup.UnableToSuppressException;

/**
 * Permet la gestion des cotisations
 * 
 */
public class GestionCotisationService
{
	
	private final static Logger logger = LogManager.getLogger();
	
	
	// PARTIE SEARCHER
	
	/**
	 * Permet de charger les périodes d'adhesion en cours, 
	 * c'est à dire dont la date de fin est dans le futur
	 */
	@DbRead
	public List<PeriodeCotisation> getAllEnCours()
	{
		RdbLink em = RdbLink.get();
		
		// Récupération de la liste des cotisations en cours 
		TypedQuery<PeriodeCotisation> q = em.createQuery("select p from PeriodeCotisation p WHERE p.dateFin>=:d ORDER BY p.dateFin",PeriodeCotisation.class);
		Date d = DateUtils.getDateWithNoTime();
		q.setParameter("d",d);
		return q.getResultList();
	}
	

	// PARTIE REQUETAGE POUR AVOIR LA LISTE DE TOUTES LES PERIODES DE COTISATIONS

	/**
	 * Permet de charger la liste de toutes les periodes de cotisations
	 */
	@DbRead
	public List<PeriodeCotisationDTO> getAll()
	{
		RdbLink em = RdbLink.get();
		TypedQuery<PeriodeCotisation> q = em.createQuery("select a from PeriodeCotisation a",PeriodeCotisation.class);
		return DbToDto.convert(q, e->createPeriodeCotisationDto(em, e));
	}
	
	@DbRead
	public PeriodeCotisationDTO load(Long idPeriodeCotisation)
	{
		RdbLink em = RdbLink.get();
		PeriodeCotisation a = em.find(PeriodeCotisation.class, idPeriodeCotisation);
		return createPeriodeCotisationDto(em, a);
	}

	public PeriodeCotisationDTO createPeriodeCotisationDto(RdbLink em, PeriodeCotisation a)
	{
		PeriodeCotisationDTO dto = new PeriodeCotisationDTO();
		
		dto.id = a.getId();
		dto.nom = a.nom;
		dto.montantMini = a.montantMini;
		dto.montantConseille = a.montantConseille;
		dto.textPaiement = a.textPaiement;
		dto.libCheque = a.libCheque;
		dto.dateDebut = a.dateDebut;
		dto.dateFin = a.dateFin;
		dto.idBulletinAdhesion = IdentifiableUtil.getId(a.bulletinAdhesion);
		
		// Champs calculés
		dto.nbAdhesion = getNbAdhesion(em,a);
		dto.mntTotalAdhesion = getMntTotalAdhesion(em,a);
		dto.nbPaiementDonnes = getNbPaiement(em,a,EtatPaiementAdhesion.ENCAISSE);
		dto.nbPaiementARecuperer = getNbPaiement(em,a,EtatPaiementAdhesion.A_FOURNIR);
		
		return dto;
	}

	
	public PeriodeCotisationUtilisateurDTO createPeriodeCotisationUtilisateurDto(RdbLink em, PeriodeCotisationUtilisateur a)
	{
		PeriodeCotisationUtilisateurDTO dto = new PeriodeCotisationUtilisateurDTO();

		dto.dateAdhesion = a.dateAdhesion;
		dto.dateReceptionCheque = a.dateReceptionCheque;
		dto.etatPaiementAdhesion = a.etatPaiementAdhesion;
		dto.id = a.getId();
		dto.idUtilisateur = a.utilisateur.getId();
		dto.nomUtilisateur = a.utilisateur.nom;
		dto.prenomUtilisateur = a.utilisateur.prenom;
		dto.montantAdhesion = a.montantAdhesion;
		dto.typePaiementAdhesion = a.typePaiementAdhesion;

		// 
		dto.idPeriodeCotisation = a.periodeCotisation.getId();
		dto.periodeNom = a.periodeCotisation.nom;
		dto.periodeDateDebut = a.periodeCotisation.dateDebut;
		dto.periodeDateFin = a.periodeCotisation.dateFin;
		
		return dto;
	}

	private int getNbAdhesion(RdbLink em, PeriodeCotisation pc)
	{
		Query q = em.createQuery("select count(p.id) from PeriodeCotisationUtilisateur p WHERE p.periodeCotisation=:pc");
		q.setParameter("pc", pc);
		return LongUtils.toInt(q.getSingleResult());
	}

	private int getMntTotalAdhesion(RdbLink em, PeriodeCotisation pc)
	{
		Query q = em.createQuery("select sum(p.montantAdhesion) from PeriodeCotisationUtilisateur p WHERE p.periodeCotisation=:pc");
		q.setParameter("pc", pc);
		return LongUtils.toInt(q.getSingleResult());
	}

	private int getNbPaiement(RdbLink em, PeriodeCotisation pc,EtatPaiementAdhesion epc)
	{
		Query q = em.createQuery("select count(p.id) from PeriodeCotisationUtilisateur p WHERE p.periodeCotisation=:pc and p.etatPaiementAdhesion=:epc");
		q.setParameter("pc", pc);
		q.setParameter("epc", epc);
		return LongUtils.toInt(q.getSingleResult());
	}

	

	// CREATION D'UNE PERIODE DE COTISATION
	@DbWrite
	public Long createOrUpdate(PeriodeCotisationDTO dto) throws OnSaveException
	{
		RdbLink em = RdbLink.get();

		PeriodeCotisation a;
		if (dto.id==null)
		{
			a = new PeriodeCotisation();
		}
		else
		{
			a = em.find(PeriodeCotisation.class, dto.id);
		}

		a.nom = dto.nom;
		a.montantMini = dto.montantMini;
		a.montantConseille = dto.montantConseille;
		a.textPaiement = dto.textPaiement;
		a.libCheque = dto.libCheque;
		a.dateDebut = dto.dateDebut;
		a.dateFin = dto.dateFin;
		a.bulletinAdhesion = IdentifiableUtil.findIdentifiableFromId(EditionSpecifique.class, dto.idBulletinAdhesion, em);
				
		if (dto.id==null)
		{
			em.persist(a);
		}
		
		return a.id;
	}



	// PARTIE SUPPRESSION  D'UNE PERIODE DE COTISATION

	/**
	 * Permet de supprimer une periode de cotisation
	 */
	@DbWrite
	public void delete(Long id)
	{
		RdbLink em = RdbLink.get();
		PeriodeCotisation p = em.find(PeriodeCotisation.class, id);
		
		int r = countPeriodeCotisationUtilisateur(em,p);
		if (r>0)
		{
			throw new UnableToSuppressException("Il y a des utilisateurs inscrits sur cette période de cotisation :  "+r+" inscrits.");
		}
		
		String str = countPeriodeCotisationContrat(em,p);
		if (str!=null)
		{
			throw new UnableToSuppressException("Certains contrats font référence à cette période de cotisation. Noms des contrats : "+str);
		}
		
		em.remove(p);
	}
	
	
	private String countPeriodeCotisationContrat(RdbLink em, PeriodeCotisation p) 
	{
		TypedQuery<ModeleContrat> q = em.createQuery("select mc from ModeleContrat mc WHERE mc.periodeCotisation=:p",ModeleContrat.class);
		q.setParameter("p", p);

		List<ModeleContrat> ls = q.getResultList();
		if (ls.size()==0)
		{
			return null;
		}
		return CollectionUtils.asStdString(ls, e->e.nom);
	}


	private int countPeriodeCotisationUtilisateur(RdbLink em,PeriodeCotisation p)
	{
		Query q = em.createQuery("select count(pu) from PeriodeCotisationUtilisateur pu WHERE pu.periodeCotisation=:p");
		q.setParameter("p", p);
		return LongUtils.toInt(q.getSingleResult());
	}
	
	
	/**
	 * Permet de charger la liste de toutes les periodes de cotisations d'un utilisateur,
	 * la plus récente en premier 
	 */
	@DbRead
	public List<PeriodeCotisationUtilisateurDTO> getPeriodeCotisation(Long idUtilisateur)
	{
		RdbLink em = RdbLink.get();
		

		TypedQuery<PeriodeCotisationUtilisateur> q = em.createQuery("select pu from PeriodeCotisationUtilisateur pu " +
														 "WHERE pu.utilisateur.id=:idUtilisateur "+
														 "ORDER BY pu.periodeCotisation.dateFin desc",PeriodeCotisationUtilisateur.class);
		q.setParameter("idUtilisateur", idUtilisateur);
		
		return DbToDto.convert(q, e->createPeriodeCotisationUtilisateurDto(em, e));
	}
	
	
	/*
	 * PARTIE BILAN GLOBAL POUR UNE PERIODE
	 */
	@DbRead
	public BilanAdhesionDTO loadBilanAdhesion(Long idPeriodeCotisation)
	{
		BilanAdhesionDTO res = new BilanAdhesionDTO();
		RdbLink em = RdbLink.get();

		PeriodeCotisation p = em.find(PeriodeCotisation.class, idPeriodeCotisation);

		res.periodeCotisationDTO = createPeriodeCotisationDto(em, p);
		
		
		// Récupération de la cotisation
		Query q = em.createQuery("select pu from PeriodeCotisationUtilisateur pu " +
								"WHERE pu.periodeCotisation=:p order by pu.utilisateur.nom, pu.utilisateur.prenom");
		q.setParameter("p",p);
		
		List<PeriodeCotisationUtilisateur> periodeCotisationUtilisateurs = q.getResultList();
		for (PeriodeCotisationUtilisateur pcu : periodeCotisationUtilisateurs)
		{
			PeriodeCotisationUtilisateurDTO pcuDTO = createPeriodeCotisationUtilisateurDto(em, pcu);
			res.utilisateurDTOs.add(pcuDTO);
		}
		
		return res;
	}
	
	
	/**
	 * Récupère la liste de tous les utilisateurs qui ne sont pas adherents sur la periode
	 * indiquée et qui sont actifs 
	 * 
	 * @param idPeriodeCotisation
	 * @return
	 */
	@DbRead
	public List<Utilisateur> getAllUtilisateurSansAdhesion(Long idPeriodeCotisation)
	{
		RdbLink em = RdbLink.get();
		
		Query q = em.createQuery("select u from Utilisateur u where "
				+ " u.etatUtilisateur=:etat and "
				+ " u.id not in (select p.utilisateur.id from PeriodeCotisationUtilisateur p where p.periodeCotisation.id=:idPeriode) "
				+ " order by u.nom,u.prenom");
		q.setParameter("etat", EtatUtilisateur.ACTIF);
		q.setParameter("idPeriode", idPeriodeCotisation);
		List<Utilisateur> us = q.getResultList();
		
		
		return us;
	}
	
	
	/**
	 * Récupère la liste de tous les utilisateurs qui sont adherents sur la periode
	 * indiquée et qui sont actifs 
	 * 
	 * @param idPeriodeCotisation
	 * @return
	 */
	@DbRead
	public List<Utilisateur> getAllUtilisateurAvecAdhesion(Long idPeriodeCotisation)
	{
		RdbLink em = RdbLink.get();
		
		PeriodeCotisation p = em.find(PeriodeCotisation.class, idPeriodeCotisation);
		
		Query q = em.createQuery("select pu.utilisateur from PeriodeCotisationUtilisateur pu " 
									+ "WHERE pu.periodeCotisation=:p  AND "
									+ " pu.utilisateur.etatUtilisateur=:etat "
									+ " order by pu.utilisateur.nom, pu.utilisateur.prenom");
		q.setParameter("p",p);
		q.setParameter("etat", EtatUtilisateur.ACTIF);
		
		List<Utilisateur> us = q.getResultList();
		
		return us;
	}
	
	
	

	
	/**
	 * Permet l'ajout ou la mise à jour d'une cotisation dans l'écran de gestion des cotisations
	 * @param dto
	 */
	@DbWrite
	public void createOrUpdateCotisation(boolean create, PeriodeCotisationUtilisateurDTO dto)
	{
		RdbLink em = RdbLink.get();
		
		PeriodeCotisationUtilisateur pcu;
		if (create==false)
		{
			pcu = em.find(PeriodeCotisationUtilisateur.class, dto.id); 
		}
		else
		{
			pcu = new PeriodeCotisationUtilisateur();
			pcu.periodeCotisation = em.find(PeriodeCotisation.class, dto.idPeriodeCotisation);
			pcu.utilisateur = em.find(Utilisateur.class, dto.idUtilisateur);
			pcu.dateAdhesion = DateUtils.getDate();
		}
		
		pcu.etatPaiementAdhesion = dto.etatPaiementAdhesion;
		pcu.montantAdhesion = dto.montantAdhesion;
		pcu.typePaiementAdhesion = dto.typePaiementAdhesion;
		
		// On met à jour la date de réception du chèque si elle n'est pas connu et que l'état est ENCAISSE
		if (pcu.dateReceptionCheque==null && dto.etatPaiementAdhesion==EtatPaiementAdhesion.ENCAISSE)
		{
			pcu.dateReceptionCheque = DateUtils.getDate();
		}
		
		
		if (create==true)
		{
			em.persist(pcu);
		}	
	}
	
	
	// PARTIE REQUETAGE POUR AVOIR LA LISTE DES PERIODES DE COTISATION QU'IL EST SOUHAITABLE DE SUPPRIMER
	
	
	
	public String computeSuppressionLib(ParametresArchivageDTO param)
	{
		String str = "Il est souhaitable de supprimer une période d'adhésion qui remplit les conditions suivantes : <ul>"+
				 	"<li>la date de fin de cette période d'adhésion est plus vieille que "+param.suppressionPeriodeCotisation+" jours</li>"+
				 	"<li>tous les contrats faisant référence à cette période d'adhésion ont été supprimés</li>"+
				 	"</ul><br/>";
				 	
		return str;
	}
	
	/**
	 * Vérifie si cette période peut être supprimée
	 */
	@DbRead
	public SuppressionState computeSuppressionState(PeriodeCotisationDTO dto,ParametresArchivageDTO param)
	{
		RdbLink em = RdbLink.get();
		SuppressionState res = new SuppressionState();
		
		// Non supprimable si la date de fin n'est pas assez ancienne
		Date ref2 = DateUtils.getDateWithNoTime();
		ref2 = DateUtils.addDays(ref2, -param.suppressionPeriodeCotisation);
		if (dto.dateFin.after(ref2)) 
		{	
			res.nonSupprimables.add("La date de fin de cette période d'adhésion est trop récente");
		}
		
		// Non supprimable si il y a des contrats qui font référence à cette periode
		PeriodeCotisation p = em.find(PeriodeCotisation.class, dto.id);
		String str = countPeriodeCotisationContrat(em, p);
		if (str!=null)
		{
			res.nonSupprimables.add("Certains contrats font référence à cette période de cotisation. Noms des contrats : "+str);
		}
		
		
		return res;
	}
	
	
	/**
	 * Récupère la liste des périodes de cotisation supprimables
	 */
	public List<PeriodeCotisationDTO> getAllPeriodeCotisationSupprimables(ParametresArchivageDTO param) 
	{
		List<PeriodeCotisationDTO> ps = getAll();
		
		List<PeriodeCotisationDTO> res = new ArrayList<PeriodeCotisationDTO>();
		for (PeriodeCotisationDTO p : ps) 
		{
			SuppressionState state = computeSuppressionState(p, param);
			if (state.getStatus()==SStatus.OUI_SANS_RESERVE)
			{
				res.add(p);
			}
		}
		res.sort(Comparator.comparing(e->e.dateFin));
		
		return res;
	}

	/**
	 * Effacement complet de la période de cotisation
	 */
	@DbWrite
	public void deleteWithInscrits(Long id) 
	{
		RdbLink em = RdbLink.get();
		PeriodeCotisation p = em.find(PeriodeCotisation.class, id);
		
		Query q = em.createQuery("select pu from PeriodeCotisationUtilisateur pu WHERE pu.periodeCotisation=:p");
		q.setParameter("p", p);
		SQLUtils.deleteAll(em, q);
		
		em.remove(p);
	}
	
	// CREATION EN MASSE DES COTISATIONS
	@DbRead
	public List<PeriodeCotisationUtilisateurDTO> getAllForAjouterEnMasse(Long idPeriodeCotisation) 
	{
		List<PeriodeCotisationUtilisateurDTO> res = new ArrayList<PeriodeCotisationUtilisateurDTO>();
		
		List<Utilisateur> us = getAllUtilisateurSansAdhesion(idPeriodeCotisation);
				
		for (Utilisateur u : us) 
		{
			PeriodeCotisationUtilisateurDTO dto = new PeriodeCotisationUtilisateurDTO();
			dto.idUtilisateur = u.id;
			dto.nomUtilisateur = u.nom;
			dto.prenomUtilisateur = u.prenom;
			dto.idPeriodeCotisation = idPeriodeCotisation;
			
			res.add(dto);
		}
		
		return res;
	}

	
}
