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

 package fr.amapj.service.services.edgenerator.pdf;

import java.util.List;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.velocity.VelocityTools;
import fr.amapj.common.velocity.VelocityVar;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.contrat.modele.GestionDocEngagement;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.model.models.editionspe.AbstractEditionSpeJson;
import fr.amapj.model.models.editionspe.AbstractPdfEditionSpeJson;
import fr.amapj.model.models.editionspe.EditionSpecifique;
import fr.amapj.model.models.editionspe.engagement.EngagementJson;
import fr.amapj.model.models.fichierbase.Producteur;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.service.engine.generator.pdf.PdfGeneratorTool;
import fr.amapj.service.engine.generator.pdf.TestablePdfGenerator;
import fr.amapj.service.services.edgenerator.velocity.VCBuilder;
import fr.amapj.service.services.editionspe.EditionSpeService;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.mescontrats.MesContratsService;
import fr.amapj.service.services.producteur.ProdUtilisateurDTO;
import fr.amapj.service.services.producteur.ProducteurService;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;

/**
 * Permet la generation des engagements au format PDF
 * 
 * Cette classe génére systèmatiquement un PDF à partir des données, elle ne réutilise 
 * jamais un pdf signé qui aurait été stocké en base 
 * 
 *  Pour afficher un pdf signé stocke en base, voir par exemple la classe DocEngagementGeneralService
 * 
 */
public class PGEngagement extends TestablePdfGenerator
{
	
	//  
	private Long modeleContratId;
	
	private Long contratId;
	
	private PGEngagementMode mode;

	private ContratDTO contratDTO;

	private Long idSignatureUtilisateur;
	
	// Utilisé uniquement en mode CONTRAT_A_SIGNER et UN_CONTRAT
	// Contient la liste des variables qui ont été utilisées
	private List<VelocityVar> usedVars;


	public enum PGEngagementMode
	{
		TOUS_LES_CONTRATS,
		
		UN_CONTRAT,
		
		UN_VIERGE,
		
		TOUS_LES_CONTRATS_EN_MODE_TEST,
		
		CONTRAT_A_SIGNER
		
	}
	
	
	/**
	 * 5 modes sont possibles :
	 *  
	 * tous les contrats d'un modele (TOUS_LES_CONTRATS,id,null,null,null,null) 
	 * 
	 * un contrat d'un amapien d'un modele (UN_CONTRAT,id,id,null,null,null)
	 * 
	 * un vierge d'un modele de contrat (UN_VIERGE,id,null,null,null,null)
	 * 
	 * le mode test (TOUS_LES_CONTRATS_EN_MODE_TEST,null,null,fortest,null,null)
	 * 
	 *  un contrat d'un amapien à signer (CONTRAT_A_SIGNER,id,null,null,not null,not null)
	 * 
	 * @param modeleContratId
	 * @param forTest
	 * @param idSignatureUtilisateur utilisateur qui va signer le contrat 
	 */
	public PGEngagement(PGEngagementMode mode,Long modeleContratId,Long contratId,EngagementJson forTest,ContratDTO contratDTO,Long idSignatureUtilisateur)
	{
		super(forTest);
		this.mode = mode;
		this.modeleContratId = modeleContratId;
		this.contratId = contratId;
		this.contratDTO = contratDTO;
		this.idSignatureUtilisateur = idSignatureUtilisateur;
	}
	
	
	/**
	 */
	public PGEngagement(PGEngagementMode mode,Long modeleContratId,Long contratId,EngagementJson forTest)
	{
		this(mode, modeleContratId, contratId, forTest, null,null);
	}
	
	
	
	@Override
	public String readDataInTestMode(RdbLink em, AbstractEditionSpeJson forTest)
	{
		EngagementJson engJson = (EngagementJson) forTest;
		
		if (engJson.idModeleContrat ==null)
		{
			return "<p>Vous devez selectionner un contrat pour pouvoir tester !</p>";
		}
		
		modeleContratId = engJson.idModeleContrat;
		contratId = null;
		return null;
	}
	
	
	@Override
	public AbstractPdfEditionSpeJson getEditionInNormalMode(RdbLink em)
	{
		ModeleContrat mc =  em.find(ModeleContrat.class, modeleContratId);
		EditionSpecifique editionSpecifique = mc.engagement;
		EngagementJson engagement = (EngagementJson)  new EditionSpeService().load(editionSpecifique.id);
		return engagement;
	}
	
	
	
	@Override
	public void fillPdfFile(RdbLink em, PdfGeneratorTool et, String htmlContent)
	{
		switch (mode)
		{
		case TOUS_LES_CONTRATS:
		case TOUS_LES_CONTRATS_EN_MODE_TEST:
			performAllContratOfModele(em,et,htmlContent);
			break;
			
		case UN_CONTRAT:
			performOneContrat(em,et,htmlContent);
			break;
			
		case UN_VIERGE:
			performOneContratVierge(em,et,htmlContent);
			break;
			
		case CONTRAT_A_SIGNER:
			performOneContratASigner(em,et,htmlContent);
			break;
			
		default:
			throw new AmapjRuntimeException();
		}
	}

	
	private void performOneContratVierge(RdbLink em, PdfGeneratorTool et, String htmlContent)
	{
		ModeleContrat mc =  em.find(ModeleContrat.class, modeleContratId);
		Producteur producteur = mc.producteur;
		
		addOneContrat(em,mc,null,null,et,htmlContent,producteur);
	}
	

	
	private void performOneContrat(RdbLink em, PdfGeneratorTool et, String htmlContent)
	{
		// 
		Contrat c = em.find(Contrat.class, contratId);
		Utilisateur utilisateur = c.utilisateur;
		Producteur producteur = c.modeleContrat.producteur;
		
		//
		if (c.modeleContrat.getId().equals(modeleContratId)==false)
		{
			throw new AmapjRuntimeException("Incoherence");
		}
		VelocityTools vt = addOneContrat(em,c.modeleContrat,c,utilisateur,et,htmlContent,producteur);
		usedVars = vt.getUsedVars();
	}
	
	
	private void performOneContratASigner(RdbLink em, PdfGeneratorTool et, String htmlContent)
	{
		ModeleContrat mc =  em.find(ModeleContrat.class, modeleContratId); 
		Producteur producteur = mc.producteur;
		
		// L'utilisateur peut être null dans le cas du mode TEST des contrats de "Gestion des contrats vierges" 
		Utilisateur utilisateur = em.findOrNull(Utilisateur.class, idSignatureUtilisateur);
		
		VelocityTools vt = addOneContrat(em,mc,null,utilisateur,et,htmlContent,producteur);
		usedVars = vt.getUsedVars();
	}
	

	private void performAllContratOfModele(RdbLink em, PdfGeneratorTool et, String htmlContent)
	{
		ModeleContrat mc =  em.find(ModeleContrat.class, modeleContratId);
		Producteur producteur = mc.producteur;
		
		// Avec une sous requete, on obtient la liste de tous les utilisateur ayant commandé au moins un produit
		List<Utilisateur> utilisateurs = new MesContratsService().getUtilisateur(em, mc);
		int nb = utilisateurs.size();
		for (int i = 0; i < nb; i++)
		{
			Utilisateur utilisateur = utilisateurs.get(i);
		
			Contrat c = new MesContratsService().getContrat(mc.getId(), em, utilisateur);
			addOneContrat(em,mc,c,utilisateur,et,htmlContent,producteur);
		
			if (i!=nb-1)
			{
				et.addSautPage();
			}
		}
		
		// On positionne un message d'avertissement si besoin 
		if(nb==0)
		{
			et.addContent("<p>Aucun utilisateur n'a souscrit à ce contrat !! </p>");
		}
	}



	private VelocityTools addOneContrat(RdbLink em, ModeleContrat mc,Contrat c, Utilisateur utilisateur, PdfGeneratorTool et, String htmlContent,Producteur producteur)
	{
		VelocityTools ctx = generateContext(em,mc,c,utilisateur,producteur);		
		String res = ctx.evaluate(htmlContent);
		et.addContent(res);
		return ctx;
	}
	

	/**
	 * 
	 * @param em
	 * @param c peut être null dans le cas de la generation d'un vierge ou d'un contrat à signer 
	 * @param utilisateur peut être null dans le cas de la generation d'un vierge
	 * @param producteur n'est jamais null 
	 * @return
	 */
	private VelocityTools generateContext(RdbLink em, ModeleContrat mc,Contrat c, Utilisateur utilisateur,Producteur producteur)
	{
		VelocityTools ctx = new VelocityTools();
		
		VCBuilder.addContrat(ctx, mc, c,contratDTO, em);
		VCBuilder.addAmap(ctx);
		VCBuilder.addDateInfo(ctx);
		VCBuilder.addAmapien(ctx, utilisateur);
		VCBuilder.addProducteur(ctx, producteur);
		List<ProdUtilisateurDTO> refs=new ProducteurService().getReferents(em, producteur);
		if (refs.size()>=1)
		{
			ProdUtilisateurDTO ref = refs.get(0);
			Utilisateur r = em.find(Utilisateur.class, ref.idUtilisateur);
			VCBuilder.addReferent(ctx, r);
		}
		else
		{
			VCBuilder.addReferent(ctx, null);
		}
		
		List<ProdUtilisateurDTO> contactProds=new ProducteurService().getUtilisateur(em, producteur);
		if (contactProds.size()>=1)
		{
			ProdUtilisateurDTO contactProd = contactProds.get(0);
			Utilisateur r = em.find(Utilisateur.class, contactProd.idUtilisateur);
			VCBuilder.addContactProducteur(ctx, r);
		}
		else
		{
			VCBuilder.addContactProducteur(ctx, null);
		}
		
		
		return ctx;
	}

	@Override
	public String getFileNameStandard(RdbLink em)
	{
		ModeleContrat mc = em.find(ModeleContrat.class,modeleContratId);
		ParametresDTO param = new ParametresService().getParametres();
		
		switch (mode)
		{
		case TOUS_LES_CONTRATS:
			return "engagements-"+param.nomAmap+"-"+mc.nom;	

		case TOUS_LES_CONTRATS_EN_MODE_TEST:
			return "test-"+param.nomAmap+"-"+mc.nom;	
			
		case UN_CONTRAT:
			Utilisateur u = em.find(Contrat.class,contratId).utilisateur;
			return "document-engagement-"+param.nomAmap+"-"+mc.nom+"-"+u.nom+" "+u.prenom;
			
		case UN_VIERGE:
			return "engagement-vierge-"+param.nomAmap+"-"+mc.nom;	
			
		case CONTRAT_A_SIGNER:
			return "signature-contrat-"+param.nomAmap;
			
		default:
			throw new AmapjRuntimeException();
		}		
	}

	@Override
	public String getNameToDisplayStandard(RdbLink em)
	{
		switch (mode)
		{
		case TOUS_LES_CONTRATS:
			return "tous les documents d'engagement (à imprimer et à signer)";

		case TOUS_LES_CONTRATS_EN_MODE_TEST:
			return "mode test";
			
		case UN_CONTRAT:
			Contrat c = em.find(Contrat.class,contratId);
			Utilisateur u = c.utilisateur;
			if (c.modeleContrat.gestionDocEngagement==GestionDocEngagement.SIGNATURE_EN_LIGNE)
			{
				return "le document d'engagement "+c.modeleContrat.nom+" pour "+u.nom+" "+u.prenom+" (à signer en ligne)";
			}
			else
			{
				return "le document d'engagement "+c.modeleContrat.nom+" pour "+u.nom+" "+u.prenom+" (à imprimer et à signer)";
			}
			
		case UN_VIERGE:
			return "un document d'engagement vierge";
			
		case CONTRAT_A_SIGNER:
			return "signature du contrat";
			
			
		default:
			throw new AmapjRuntimeException();
		}
	}
	
	public List<VelocityVar> getUsedVars() 
	{
		return usedVars;
	}
	
	
	public static void main(String[] args) throws Exception
	{
		new PGEngagement(PGEngagementMode.TOUS_LES_CONTRATS,10011L,null,null,null,null).test();
	}

}
