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
 package fr.amapj.service.services.docengagement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.StackUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.models.contrat.modele.GestionDocEngagement;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.service.engine.generator.CoreGenerator;
import fr.amapj.service.services.docengagement.signonline.core.DocEngagementSignOnLineTools;
import fr.amapj.service.services.edgenerator.bin.BGDocEngagementSigne;
import fr.amapj.service.services.edgenerator.pdf.PGEngagement;
import fr.amapj.service.services.edgenerator.pdf.PGEngagement.PGEngagementMode;


/**
 * Gestion des documents d'engagement en général, 
 * qu'ils soient signés au format papier ou signés electroniquement 
 *
 */
public class DocEngagementGeneralService 
{
	private final static Logger logger = LogManager.getLogger();
	
	
	static public class DocInfo
	{
		// Est null si il n'y a pas de document d'engagement
		public CoreGenerator generator;
		
		// Valueur du modele de contrat
		public GestionDocEngagement gestionDocEngagement;
		
		// True si le document a été signé en ligne
		public boolean isSigned = false;
	}
	
	/**
	 * Retourne le document d'engagement relatif à ce contrat (signé ou à signer)
	 * 
	 */
	@DbRead
	public DocInfo getDocumentEngagementForContrat(Long idContrat)
	{
		RdbLink em = RdbLink.get();
		Contrat c = em.find(Contrat.class, idContrat);
		ModeleContrat mc =c.modeleContrat;
		
		DocInfo res = new DocInfo();
		res.gestionDocEngagement = mc.gestionDocEngagement;
		
		if (mc.gestionDocEngagement==GestionDocEngagement.AUCUNE_GESTION)
		{
			return res;
		}
		
		if (mc.gestionDocEngagement==GestionDocEngagement.GENERATION_DOCUMENT_SEUL)
		{
			res.generator = new PGEngagement(PGEngagementMode.UN_CONTRAT, mc.id, c.id,null);
			return res;
		}
		
		if (mc.gestionDocEngagement==GestionDocEngagement.SIGNATURE_EN_LIGNE)
		{
			// si le doc d'engagement a été signé
			if (c.docEngagementSigne!=null)
			{
				res.generator = new BGDocEngagementSigne(idContrat);
				res.isSigned = true;
				return res;
			}
			// Si il n'a pas été signé 
			else
			{
				res.generator = new PGEngagement(PGEngagementMode.UN_CONTRAT, mc.id, c.id,null);
				return res;
			}
		}
		throw new AmapjRuntimeException();
	}
	
	/**
	 * Verification de la taille des documents d'engagement dans le mode signature en ligne
	 * Méthode appelée à la sauvegarde des ModeleContrat
	 */
	public void checkSizeAndSignatureDocumentEngagement(RdbLink em,ModeleContrat mc)
	{
		//  
		if (mc.gestionDocEngagement!=GestionDocEngagement.SIGNATURE_EN_LIGNE)
		{
			return ;
		}
		
		byte[] pdf = getDocument(mc.id);
		if (pdf==null)
		{
			return;
		}
		
		// Verification de la taille 
		int len = pdf.length;
		logger.info("Taille du document : "+len);
		if (len>80_000)
		{
			throw new AmapjRuntimeException("La taille du document d'engagement est trop importante. Il faut diminuer sa taille en supprimant les logos et autres élements non nécessaires. Taille maximale autorisée: 80Ko - Taille actuelle : "+(len/100)+" Ko."); 
		}
		
		// Verification de la presence des ancres pour la signature amapien et producteur 
		if (new DocEngagementSignOnLineTools().hasSignatureAmapienAndProducteur(pdf)==false)
		{
			throw new AmapjRuntimeException("Le document d'engagement ne contient pas les champs  $contrat.signatureAmapien et $contrat.signatureProducteur qui permettent de poser la signature sur le contrat");
		}
		
	}

	private byte[] getDocument(Long id) 
	{
		try
		{
			return new PGEngagement(PGEngagementMode.UN_VIERGE,id,null,null).getByteArrayContent();
		}
		catch (Exception e) 
		{
			// Tant pis : pas de verification
			logger.info("Erreur verification taille signature contrat : "+StackUtils.asString(e));
			return null;
		}
	}
	
	
}
