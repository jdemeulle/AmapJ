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
 package fr.amapj.service.services.mespaiements.reception;

import java.util.List;

import javax.persistence.TypedQuery;

import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.contrat.reel.EtatPaiement;
import fr.amapj.model.models.contrat.reel.Paiement;
import fr.amapj.service.engine.tools.DbToDto;

/**
 * Permet la réception des cheques 
 * 
 *  
 *
 */
public class ReceptionPaiementsService
{


	/**
	 * Permet de charger la liste de tous les paiements à réceptionner 
	 * pour un contrat d'un adherent
	 */
	@DbRead
	public List<ReceptionChequeDTO> getPaiementAReceptionnerContrat(Long contratId)
	{
		RdbLink em = RdbLink.get();

		TypedQuery<Paiement> q = em.createQuery("select p from Paiement p " + "WHERE p.etat<>:etat and p.contrat.id=:id order by p.modeleContratDatePaiement.datePaiement asc",Paiement.class);
		q.setParameter("etat", EtatPaiement.PRODUCTEUR);
		q.setParameter("id", contratId);
		
		return DbToDto.convert(q, e->createDto(e));
	}
	
	
	private ReceptionChequeDTO createDto(Paiement paiement) 
	{
		ReceptionChequeDTO dto = new ReceptionChequeDTO();
		dto.idPaiement = paiement.getId();
		dto.datePaiement = paiement.modeleContratDatePaiement.datePaiement;
		dto.montant = paiement.montant;
		dto.etatPaiement = paiement.etat;
		dto.commentaire1 = paiement.commentaire1;
		dto.commentaire2 = paiement.commentaire2;
		dto.commentaire3 = paiement.commentaire3;
		dto.commentaire4 = paiement.commentaire4;
		dto.nomUtilisateur = paiement.contrat.utilisateur.nom;
		dto.prenomUtilisateur = paiement.contrat.utilisateur.prenom;
		
		return dto;
	}
	
	

	/**
	 * Permet de charger la liste de tous les paiements à réceptionner 
	 * pour un modele de contrat 
	 */
	@DbRead
	public List<ReceptionChequeDTO> getPaiementAReceptionnerModeleContrat(Long modeleContratId)
	{
		RdbLink em = RdbLink.get();
		
		TypedQuery<Paiement> q = em.createQuery("select p from Paiement p " + "WHERE p.etat<>:etat and p.contrat.modeleContrat.id=:id order by p.modeleContratDatePaiement.datePaiement asc",Paiement.class);
		q.setParameter("etat", EtatPaiement.PRODUCTEUR);
		q.setParameter("id", modeleContratId);
		
		return DbToDto.convert(q, e->createDto(e));
	}


	/**
	 * 
	 */
	@DbWrite
	public void receptionCheque(List<ReceptionChequeDTO> paiementDto)
	{
		RdbLink em = RdbLink.get();
		
		for (ReceptionChequeDTO dto : paiementDto)
		{
			Paiement p = em.find(Paiement.class, dto.idPaiement);
			p.etat = dto.etatPaiement;
			p.commentaire1 = dto.commentaire1;
			p.commentaire2 = dto.commentaire2;
			p.commentaire3 = dto.commentaire3;
			p.commentaire4 = dto.commentaire4;
						
		}	
	}


}
