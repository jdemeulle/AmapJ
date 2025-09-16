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
 package fr.amapj.service.services.docengagement.signonline.core;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.DateUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.engine.transaction.NewTransaction;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.model.models.contrat.reel.DocEngagementSigne;
import fr.amapj.model.models.fichierbase.Producteur;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.service.services.docengagement.signonline.avenant.AvenantManager;
import fr.amapj.service.services.docengagement.signonline.avenant.AvenantManager.ResultAvenant;
import fr.amapj.service.services.docengagement.signonline.model.DocEngagementBin;
import fr.amapj.service.services.mescontrats.DocEngagementDTO;

/**
 * Gestion de la signature en ligne des documents d'engagement 
 *
 */
public class CoreDocEngagementSignOnLineService 
{
	
	private final static Logger logger = LogManager.getLogger();
	
	/**
	 * Chargement du contenu pdf d'un document d'engagement d'un contrat 
	 * 
	 * Jette une exception si il n'y a pas de document d'engagement 
	 */
	@DbRead
	public byte[] loadDocEngagementSignePdf(Long idContrat) 
	{
		RdbLink em = RdbLink.get();
		Contrat c = em.find(Contrat.class, idContrat);
		DocEngagementSigne ds = c.docEngagementSigne;
		if (ds==null)
		{
			throw new AmapjRuntimeException("Pas de document d'engagement");
		}
		
		// Charge et deserialize le DocEngagementBin
		DocEngagementBin bin = new DocEngagementBin();
		bin.fromBytes(ds.getDocEngagementBin());	
		
		// Fait la mise à jour de l'avenant dans l'objet DocEngagementBin 
		ResultAvenant res = new AvenantManager().updateWithAvenant(em, c,bin);
		logger.info("Mise à jour avenant : "+res+" idContrat="+idContrat);	
		
		// Inscrit en base les résultats, pour eviter de les recalculer à chaque fois 
		// On fait l'inscription dans une transaction séparée pour éviter des transactions trop longues
		switch (res) 
		{
		case NOTHING_TO_DO : 
			return bin.pdfContent;
		
		case UPDATE_AVENANT_DATE :
			NewTransaction.write(em2->updateAvenantDate(em2,idContrat));
			return bin.pdfContent;
		
		case UPDATE_DOCENGAGEMENTBIN:
			NewTransaction.write(em2->updateDocEngagementBin(em2,idContrat,bin.toBytes()));
			return bin.pdfContent;

		default:
			throw new AmapjRuntimeException();
		}
	}
	
	private void updateDocEngagementBin(RdbLink em2, Long idContrat, byte[] bs) 
	{
		DocEngagementSigne ds = em2.find(Contrat.class, idContrat).docEngagementSigne;
		ds.avenantDate = DateUtils.getDate();			
		ds.setDocEngagementBin(em2, bs);
	}

	private void updateAvenantDate(RdbLink em2, Long idContrat) 
	{
		DocEngagementSigne ds = em2.find(Contrat.class, idContrat).docEngagementSigne;
		ds.avenantDate = DateUtils.getDate();			
	}

	/**
	 * Sauvegarde du document d'engagement pdf qui vient d'être signé par l'amapien (lors de la création du contrat)
	 */
	public void saveDocEngagementSigneByAmapien(DocEngagementDTO docEngagementDTO,Contrat c,RdbLink em, Date now) 
	{
		// Si pas contrat pdf signé sur cette saisie ( cas de la modification par le tresorier, ...)  
		if (docEngagementDTO==null)
		{
			return;
		}
		
		// On verifie que la taille du pdf est bien inférieure à 140Ko 
		if (docEngagementDTO.pdfContent.length>140_000)
		{
			throw new AmapjRuntimeException("La taille du pdf est trop importante : "+docEngagementDTO.pdfContent.length);
		}
		
		// Récupération du docengagementsigne si il y a un existant ou création si nécessaire
		boolean create = (c.docEngagementSigne==null);
		DocEngagementSigne ds = create ? new DocEngagementSigne() : c.docEngagementSigne;
		c.docEngagementSigne = ds;
		
		// Mise à jour 
		ds.amapienDateSignature = now;
		ds.amapienLibSignature = c.utilisateur.nom+" "+c.utilisateur.prenom; 
		ds.producteurDateSignature = null; 
		ds.producteurLibSignature = null;
		ds.avenantDate = now;
		
		// Enregistrement si besoin  
		if (create)
		{	
			em.persist(ds);
		}
				
		// Modification du pdf pour ajouter la signature
		byte[] pdfContent = new DocEngagementSignOnLineTools().addSignatureAmapien(docEngagementDTO.pdfContent,ds.amapienDateSignature,ds.amapienLibSignature);
		
		// Création de l'objet DocEngagementBin 
		DocEngagementBin bin = new DocEngagementBin();
		bin.pdfContent = pdfContent;
		bin.docEngagementData = new AvenantManager().computeAvenantData(docEngagementDTO);	
		
		// Stockage de l'objet DocEngagementBin serialisé
		ds.setDocEngagementBin(em, bin.toBytes());
	}
	
	
	/**
	 * Sauvegarde du document d'engagement pdf qui vient d'être signé par l'amapien 
	 * (signature réalisée en dehors de la création du contrat, ce cas arrive par exemple quand le tresorier efface les docs d'engagements)  
	 */
	@DbWrite
	public void saveDocEngagementSigneByAmapien(DocEngagementDTO docEngagementDTO,Long idContrat) 
	{
		RdbLink em = RdbLink.get();
		Contrat c = em.find(Contrat.class, idContrat);
		saveDocEngagementSigneByAmapien(docEngagementDTO, c, em,DateUtils.getDate());
	}
	
	
	
	/**
	 * Sauvegarde du contrat Pdf qui vient d'être signé par le producteur
	 * 
	 *  Lors de cet étape, il n'y a pas de calcul de l'avenant , ce n'est pas nécesaire
	 *  
	 *  En effet, si le producteur visualise le contrat pour le signer, alors l'avenant est calculé 
	 */
	@DbWrite
	public void saveDocEngagementSigneByProducteur(Long idContrat,Long userId) 
	{
		RdbLink em = RdbLink.get();
		
		// Récupération du docengagementsigne 
		Contrat c = em.find(Contrat.class, idContrat);
		DocEngagementSigne ds = c.docEngagementSigne;		
				
		// Mise à jour 
		Utilisateur u = em.find(Utilisateur.class, userId);
		Producteur p = c.modeleContrat.producteur;
		
		ds.producteurDateSignature = DateUtils.getDate();;
		ds.producteurLibSignature = u.nom+" "+u.prenom+" ("+p.nom+")"; 
		
		// Récupération du docEngagementBin et extraction du pdf 
		DocEngagementBin docEngagementBin = new DocEngagementBin();
		docEngagementBin.fromBytes(ds.getDocEngagementBin());
		
		// Modification du pdf pour ajouter la signature
		docEngagementBin.pdfContent = new DocEngagementSignOnLineTools().addSignatureProducteur(docEngagementBin.pdfContent,ds.producteurDateSignature,ds.producteurLibSignature);
		
		// Serialisation du docEngagementBin et stockage 
		ds.setDocEngagementBin(em, docEngagementBin.toBytes());
	}
	
	
	/**
	 * Suppression d'un DocEngagementSigne
	 */
	public void deleteDocEngagementSigne(RdbLink em, Contrat c) 
	{
		// Récupération du contrat signé    
		DocEngagementSigne cs = c.docEngagementSigne;
		if (cs==null)
		{
			// Nothing to do 
			return;
		}
		
		// Le contrat ne reférence plus l'objet DocEngagementSigne
		c.docEngagementSigne = null;
		
		// Suppression du pdf dans l'objet DocEngagementSigne - Obligatoire 
		cs.setDocEngagementBin(em, null);
		
		// Suppression de l'objet DocEngagementSigne
		em.remove(cs);
	}
}
