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
 package fr.amapj.service.services.mesadhesions;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.common.CollectorUtils;
import fr.amapj.common.DateUtils;
import fr.amapj.common.SQLUtils;
import fr.amapj.model.engine.IdentifiableUtil;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.model.models.cotisation.EtatPaiementAdhesion;
import fr.amapj.model.models.cotisation.PeriodeCotisation;
import fr.amapj.model.models.cotisation.PeriodeCotisationUtilisateur;
import fr.amapj.model.models.cotisation.TypePaiementAdhesion;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.service.services.gestioncotisation.GestionCotisationService;
import fr.amapj.view.engine.popup.suppressionpopup.UnableToSuppressException;

/**
 * Ecran mes adhesions
 * 
 */
public class MesAdhesionsService
{
	
	private final static Logger logger = LogManager.getLogger();


	@DbRead
	public MesAdhesionDTO computeAdhesionInfo(Long userId)
	{
		RdbLink em = RdbLink.get();
		Utilisateur user = em.find(Utilisateur.class, userId);
		
		MesAdhesionDTO mesAdhesionDTO = new MesAdhesionDTO();
		
		// Récupération de la liste des cotisations en cours 
		List<PeriodeCotisation> ps = new GestionCotisationService().getAllEnCours();
		logger.debug("Nombre de periodes de cotisation en cours = {} ",ps.size());
		
		
		// Récupération de la liste des cotisations de cet utilisateur 
		TypedQuery<PeriodeCotisationUtilisateur> q2 = em.createQuery("select pu from PeriodeCotisationUtilisateur pu WHERE pu.utilisateur=:u",PeriodeCotisationUtilisateur.class);
		q2.setParameter("u",user);
		List<PeriodeCotisationUtilisateur> pcus = q2.getResultList();
		
		
		for (PeriodeCotisation periodeCotisation : ps) 
		{
			PeriodeCotisationUtilisateur pcu = findMatching(periodeCotisation,pcus);
			AdhesionDTO dto = createAdhesionDTO(em,periodeCotisation,pcu,userId);
			
			if (pcu==null)
			{				
				mesAdhesionDTO.nouvelles.add(dto);
			}
			else
			{
				mesAdhesionDTO.enCours.add(dto);
			}
		}
		
		
		for (PeriodeCotisationUtilisateur pcu : pcus) 
		{
			PeriodeCotisation periodeCotisation = findMatching(pcu,ps);
			
			// Si cet adhesion utilisateur n'a pas été proposé dans les periodes de cotisation 
			if (periodeCotisation==null)
			{
				periodeCotisation = em.find(PeriodeCotisation.class, pcu.periodeCotisation.id);
				AdhesionDTO dto = createAdhesionDTO(em,periodeCotisation,pcu,userId);
				mesAdhesionDTO.archives.add(dto);
			}
		}
		
		// ON tri par ordre décroissant de date de fin
		mesAdhesionDTO.nouvelles.sort(Comparator.comparing( (AdhesionDTO e)->e.dateFin).reversed());
		mesAdhesionDTO.enCours.sort(Comparator.comparing( (AdhesionDTO e)->e.dateFin).reversed());
		mesAdhesionDTO.archives.sort(Comparator.comparing( (AdhesionDTO e)->e.dateFin).reversed());
		
		return mesAdhesionDTO;
			
	}


	private PeriodeCotisationUtilisateur findMatching(PeriodeCotisation p,List<PeriodeCotisationUtilisateur> pcus) 
	{
		return pcus.stream().filter(e->e.periodeCotisation.id.equals(p.id)).collect(CollectorUtils.oneOrZero());
	}
	
	private PeriodeCotisation findMatching(PeriodeCotisationUtilisateur pcu, List<PeriodeCotisation> ps) 
	{
		return ps.stream().filter(e->e.id.equals(pcu.periodeCotisation.id)).collect(CollectorUtils.oneOrZero());
	}

	private AdhesionDTO createAdhesionDTO(RdbLink em, PeriodeCotisation p, PeriodeCotisationUtilisateur pcu,Long idUtilisateur) 
	{
		
		AdhesionDTO dto = new AdhesionDTO();
		
		
		dto.isModifiable= computeIsModifiable(pcu);
		dto.isSupprimable = computeIsSupprimable(pcu,em); 
		dto.idUtilisateur =  idUtilisateur;
		dto.idPeriode = p.id;
		dto.nomPeriode = p.nom;
		dto.montantMini = p.montantMini;
		dto.montantConseille = p.montantConseille;
		dto.dateDebut = p.dateDebut;
		dto.dateFin = p.dateFin;
		dto.libCheque = p.libCheque;
		dto.textPaiement = p.textPaiement;
		
		if (pcu!=null)
		{
			dto.idPeriodeUtilisateur = pcu.id;
			dto.montantAdhesion = pcu.montantAdhesion;
			dto.etatPaiementAdhesion= pcu.etatPaiementAdhesion;
			dto.typePaiementAdhesion= pcu.typePaiementAdhesion;
		}
		
		//
		dto.idBulletin = IdentifiableUtil.getId(p.bulletinAdhesion);
		
		return dto;
	}
	
	
	/**
	 * Une adhesion est modifiable, sauf si le tresorier a receptionné les paiements
	 */
	private boolean computeIsModifiable(PeriodeCotisationUtilisateur pcu) 
	{
		if (pcu==null)
		{
			return false;
		}
		if (pcu.etatPaiementAdhesion==EtatPaiementAdhesion.ENCAISSE)
		{
			return false;
		}
		return true;
	}

	/**
	 * Une adhesion n'est pas supprimable, sauf si le tresorier n'a pas receptionné les paiements et l'adherent n'a souscrit a aucun contrat 
	 * lié à cette periode de cotisation
	 * @param em 
	 */
	private boolean computeIsSupprimable(PeriodeCotisationUtilisateur pcu, RdbLink em) 
	{
		if (pcu==null)
		{
			return false;
		}
		if (pcu.etatPaiementAdhesion==EtatPaiementAdhesion.ENCAISSE)
		{
			return false;
		}
		
		TypedQuery<Contrat> q = em.createQuery("select count(c) from Contrat c WHERE c.modeleContrat.periodeCotisation=:p and c.utilisateur=:u",Contrat.class);
		q.setParameter("p", pcu.periodeCotisation);
		q.setParameter("u", pcu.utilisateur);
		long nb = SQLUtils.count(q);
		if (nb>0)
		{
			return false;
		}
		
		return true;
	}


	@DbWrite
	public void createOrUpdateAdhesion(AdhesionDTO dto, int montant)
	{
		RdbLink em = RdbLink.get();
		
		PeriodeCotisationUtilisateur pcu;
		if (dto.idPeriodeUtilisateur!=null)
		{
			pcu = em.find(PeriodeCotisationUtilisateur.class, dto.idPeriodeUtilisateur); 
		}
		else
		{
			pcu = new PeriodeCotisationUtilisateur();
			pcu.periodeCotisation = em.find(PeriodeCotisation.class, dto.idPeriode);
			pcu.utilisateur = em.find(Utilisateur.class, dto.idUtilisateur);
		}
		
		pcu.dateAdhesion = DateUtils.getDate();
		pcu.montantAdhesion = montant;
		
		if (dto.idPeriodeUtilisateur==null)
		{
			em.persist(pcu);
		}
		
	}

	/**
	 * Si c'est le tresorier  qui supprime cette adhesion, alors on ne fait pas les verifications
	 */
	@DbWrite
	public void deleteAdhesion(Long idItemToSuppress,boolean isTresorier) throws UnableToSuppressException
	{
		RdbLink em = RdbLink.get();
		PeriodeCotisationUtilisateur pcu = em.find(PeriodeCotisationUtilisateur.class, idItemToSuppress);
		
		if (isTresorier==false && computeIsSupprimable(pcu, em)==false)
		{
			throw new UnableToSuppressException("Impossible de supprimer cette adhesion");
		}

		em.remove(pcu);
		
	}
	
	
	
}
