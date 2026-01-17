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
 package fr.amapj.service.services.docengagement.signonline;

import java.util.ArrayList;
import java.util.List;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.DateUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.contrat.modele.GestionDocEngagement;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.NatureContrat;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.model.models.contrat.reel.DocEngagementSigne;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.service.services.docengagement.signonline.core.CoreDocEngagementSignOnLineService;
import fr.amapj.service.services.gestioncontrat.ModeleContratDTO;
import fr.amapj.service.services.mescontrats.ContratStatusService;
import fr.amapj.service.services.utilisateur.util.UtilisateurUtil;


/**
 * Gestion des documents d'engagement signés electroniquement 
 *
 */
public class DocEngagementSignOnLineService 
{
	
	/**
	 * Permet de charger la liste de tous les documents d'engagement que cet amapien doit signer 
	 */
	@DbRead
	public List<DocEngagementSignOnLineDTO> getAllContratASignerByAmapien(Long idUser)
	{
		RdbLink em = RdbLink.get();
		em.createQuery("select c from Contrat c where c.utilisateur.id = :idUser "
				+ " AND c.modeleContrat.gestionDocEngagement=:g "
				+ " AND c.docEngagementSigne is NULL "
				+ " ORDER BY c.modeleContrat.nom");
		em.setParameter("idUser", idUser);
		em.setParameter("g", GestionDocEngagement.SIGNATURE_EN_LIGNE);
		return em.result().listConverted(Contrat.class, e->createDto(em,e));
	}
	
	
	/**
	 * Permet de charger la liste de tous les documents d'engagement que ce producteur doit signer pour ce modele de contrat 
	 */
	@DbRead
	public List<DocEngagementSignOnLineDTO> getAllDocumentsASignerByProducteur(Long idModeleContrat)
	{
		RdbLink em = RdbLink.get();
		ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);
			
		em.createQuery("select c from Contrat c where c.modeleContrat=:mc and c.docEngagementSigne IS NOT NULL AND c.docEngagementSigne.producteurDateSignature is NULL order by c.utilisateur.nom,c.utilisateur.prenom");
		em.setParameter("mc", mc);
		
		return em.result().listConverted(Contrat.class, e->createDto(em,e));
	}

	private DocEngagementSignOnLineDTO createDto(RdbLink em, Contrat c) 
	{
		DocEngagementSignOnLineDTO dto = new DocEngagementSignOnLineDTO();

		dto.nomModeleContrat =c.modeleContrat.nom;
		dto.nomUtilisateur = c.utilisateur.nom;
		dto.prenomUtilisateur = c.utilisateur.prenom;
		dto.idUtilisateur = c.utilisateur.getId();
		dto.idContrat = c.id;
		dto.idModeleContrat = c.modeleContrat.getId();
		
		DocEngagementSigne ds = c.docEngagementSigne;
		
		dto.signedByAmapien = ds==null ? null : ds.amapienDateSignature;
		dto.signedByProducteur = ds==null ? null : ds.producteurDateSignature;
		return dto;
	}
	
	
	
	/**
	 * Permet de charger un bilan de l'avancement de la signature en ligne des documents d'engagement  
	 */
	@DbRead
	public List<DocEngagementSignOnLineDTO> getBilanSignature(Long idModeleContrat)
	{
		RdbLink em = RdbLink.get();
		ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);
			
		if (mc.gestionDocEngagement!=GestionDocEngagement.SIGNATURE_EN_LIGNE)
		{
			throw new AmapjRuntimeException();
		}
			
		em.createQuery("select c from Contrat c where c.modeleContrat=:mc order by c.utilisateur.nom,c.utilisateur.prenom");
		em.setParameter("mc", mc);
		
		return em.result().listConverted(Contrat.class, e->createDto(em,e));
	}
	
	/**
	 * Permet de supprimer de façon définitive tous les documents d'engagement signé en ligne d'un contrat 
	 */
	@DbWrite
	public String deleteAllDocEngagement(ModeleContratDTO modeleContrat)
	{
		RdbLink em = RdbLink.get();
		em.createQuery("select c from Contrat c where c.modeleContrat.id=:id and c.docEngagementSigne IS NOT NULL");
		em.setParameter("id", modeleContrat.id);
			
		List<Contrat> cs = em.result().list(Contrat.class);
		List<Utilisateur> us = new ArrayList<>();
		for (Contrat c : cs) 
		{
			new CoreDocEngagementSignOnLineService().deleteDocEngagementSigne(em,c);
			us.add(c.utilisateur);
		}
		
		return UtilisateurUtil.getUtilisateurImpactes(us);
	}


	/**
	 * Retourne true si le producteur ne peut pas signer les documents 
	 * à cause de la date de fin des inscription qui n'est pas passée   
	 */
	@DbRead
	public boolean producteurIsNotAllowedToSignNow(Long idModeleContrat) 
	{
		RdbLink em = RdbLink.get();
		ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);
		
		if (mc.nature==NatureContrat.ABONNEMENT || mc.nature==NatureContrat.LIBRE)
		{
			// Dans le cas Abonnement et Libre, le producteur peut signer uniquement si la date de fin des inscription est passée
			return new ContratStatusService().isInscriptionNonTerminee(mc, DateUtils.getDate());
		}
		else
		{
			// Dans le cas des cartes pré payées, le producteur peut signer quand il veut
			// mais si sa signature sera perdue si l'amapien modifie son contrat  
			return false;
		}
	}
	
}
