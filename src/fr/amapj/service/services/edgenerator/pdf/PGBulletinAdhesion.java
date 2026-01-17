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
import java.util.function.Supplier;

import javax.persistence.Query;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.GenericUtils.Ret;
import fr.amapj.common.velocity.VelocityTools;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.cotisation.PeriodeCotisation;
import fr.amapj.model.models.cotisation.PeriodeCotisationUtilisateur;
import fr.amapj.model.models.editionspe.AbstractEditionSpeJson;
import fr.amapj.model.models.editionspe.AbstractPdfEditionSpeJson;
import fr.amapj.model.models.editionspe.EditionSpecifique;
import fr.amapj.model.models.editionspe.adhesion.BulletinAdhesionJson;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.service.engine.generator.pdf.PdfGeneratorTool;
import fr.amapj.service.engine.generator.pdf.TestablePdfGenerator;
import fr.amapj.service.services.edgenerator.velocity.VCBuilder;
import fr.amapj.service.services.editionspe.EditionSpeService;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;


/**
 * Permet la generation des bulletins d'adhesion au format PDF
 * 
 */
public class PGBulletinAdhesion extends TestablePdfGenerator
{
	
	
	public static PGBulletinAdhesion allBulletinPeriode(Long idPeriode)
	{
		return new PGBulletinAdhesion(Mode.ALL_BULLETIN_PERIODE,idPeriode,null,null,null,null);
	}
	
	public static PGBulletinAdhesion oneBulletinCreated(Long idPeriode,Long idPeriodeUtilisateur)
	{
		return new PGBulletinAdhesion(Mode.ONE_BULLETIN_CREATED,idPeriode,idPeriodeUtilisateur,null,null,null);
	}
	
	public static PGBulletinAdhesion oneBulletinNotCreated(Long idPeriode,Long idUtilisateur,Supplier<Ret<Integer>> montantSupplier)
	{
		return new PGBulletinAdhesion(Mode.ONE_BULLETIN_NOT_CREATED,idPeriode,null,idUtilisateur,null,montantSupplier);
	}
	
	public static PGBulletinAdhesion testMode(BulletinAdhesionJson forTest)
	{
		return new PGBulletinAdhesion(Mode.ALL_BULLETIN_PERIODE,null,null,null,forTest,null);
	}
	
	
	private enum Mode
	{
		ALL_BULLETIN_PERIODE, 
		
		ONE_BULLETIN_CREATED , 
		
		ONE_BULLETIN_NOT_CREATED , 
	}
	
	//  
	private Long idPeriode;
	
	private Long idPeriodeUtilisateur;
	
	private Long idUtilisateur;

	private Mode mode;

	// utilisé uniquement en mode ONE_BULLETIN_NOT_CREATED
	private Supplier<Ret<Integer>> montantSupplier;
	
	// utilisé uniquement en mode ONE_BULLETIN_NOT_CREATED
	private int montant;

	
	/**
	 */
	private PGBulletinAdhesion(Mode mode,Long idPeriode,Long idPeriodeUtilisateur,Long idUtilisateur,BulletinAdhesionJson forTest,Supplier<Ret<Integer>> montantSupplier)
	{
		super(forTest);
		this.mode = mode;
		this.idPeriode = idPeriode;
		this.idPeriodeUtilisateur = idPeriodeUtilisateur;
		this.idUtilisateur = idUtilisateur;
		this.montantSupplier = montantSupplier;
	}
	
	@Override
	public String readDataInTestMode(RdbLink em, AbstractEditionSpeJson forTest)
	{
		BulletinAdhesionJson engJson = (BulletinAdhesionJson) forTest;
		
		if (engJson.idPeriodeCotisation ==null)
		{
			return "<p>Vous devez sélectionner une période pour pouvoir tester !</p>";
		}
		
		idPeriode = engJson.idPeriodeCotisation;
		return null;
	}
	
	
	@Override
	public AbstractPdfEditionSpeJson getEditionInNormalMode(RdbLink em)
	{
		PeriodeCotisation pc =  em.find(PeriodeCotisation.class, idPeriode);
		EditionSpecifique editionSpecifique = pc.bulletinAdhesion;
		AbstractPdfEditionSpeJson bulletin = (AbstractPdfEditionSpeJson)  new EditionSpeService().load(editionSpecifique.id);
		return bulletin;
	}
	
	
	
	@Override
	public void fillPdfFile(RdbLink em, PdfGeneratorTool et, String htmlContent)
	{
		switch (mode)
		{
		case ALL_BULLETIN_PERIODE: performAllBulletins(em,et,htmlContent); return ;
		case ONE_BULLETIN_CREATED: performOneBulletinCreated(em,et,htmlContent); return ;
		case ONE_BULLETIN_NOT_CREATED : performOneBulletinNotCreated(em,et,htmlContent); return ;
		default: throw new AmapjRuntimeException();	
		}
	}
	

	private void performOneBulletinCreated(RdbLink em, PdfGeneratorTool et, String htmlContent)
	{
		PeriodeCotisationUtilisateur pcu = em.find(PeriodeCotisationUtilisateur.class, idPeriodeUtilisateur);
		PeriodeCotisation pc = pcu.periodeCotisation;
		Utilisateur u = pcu.utilisateur;
		
		addOneBulletin(em,pc,pcu,u,et,htmlContent);
	}
	
	private void performOneBulletinNotCreated(RdbLink em, PdfGeneratorTool et, String htmlContent)
	{
		Ret<Integer> ret = montantSupplier.get();
		if (ret.isOK()==false)
		{
			et.addContent("Il y a une erreur dans la saisie du montant de l'adhésion<br/>");
			et.addContent(ret.msg());
			return ;
		}
		montant = ret.get();
		
		
		PeriodeCotisation pc = em.find(PeriodeCotisation.class, idPeriode);
		Utilisateur u = em.find(Utilisateur.class, idUtilisateur);
		
		addOneBulletin(em,pc,null,u,et,htmlContent);
	}
	

	private void performAllBulletins(RdbLink em, PdfGeneratorTool et, String htmlContent)
	{
		PeriodeCotisation pc =  em.find(PeriodeCotisation.class, idPeriode);
		
		// Avec une sous requete, on obtient la liste de tous les utilisateurs ayant adheré 
		List<PeriodeCotisationUtilisateur> pcus = getAllUtilisateurAvecAdhesion(em,pc);
		int nb = pcus.size();
		for (int i = 0; i < nb; i++)
		{
			PeriodeCotisationUtilisateur pcu = pcus.get(i);
		
			addOneBulletin(em,pc,pcu,pcu.utilisateur,et,htmlContent);
		
			if (i!=nb-1)
			{
				et.addSautPage();
			}
		}
		
		// On positionne un message d'avertissement si besoin 
		if(nb==0)
		{
			et.addContent("<p>Aucun utilisateur n'a adhéré !! </p>");
		}
	}



	private List<PeriodeCotisationUtilisateur> getAllUtilisateurAvecAdhesion(RdbLink em,PeriodeCotisation p)
	{
		Query q = em.createQuery("select pu from PeriodeCotisationUtilisateur pu " 
				+ "WHERE pu.periodeCotisation=:p  "
				+ "order by pu.utilisateur.nom, pu.utilisateur.prenom");
		q.setParameter("p",p);
		
		List<PeriodeCotisationUtilisateur> us = q.getResultList();
		return us;
	}

	private void addOneBulletin(RdbLink em, PeriodeCotisation pc,PeriodeCotisationUtilisateur pcu, Utilisateur u,PdfGeneratorTool et, String htmlContent)
	{
		VelocityTools ctx = generateContext(em,pc,pcu,u);		
		String res = ctx.evaluate(htmlContent);
		et.addContent(res);
	}

	private VelocityTools generateContext(RdbLink em, PeriodeCotisation pc, PeriodeCotisationUtilisateur pcu, Utilisateur u)
	{
		VelocityTools ctx = new VelocityTools();
		
		VCBuilder.addAmap(ctx);
		VCBuilder.addDateInfo(ctx);
		VCBuilder.addAmapien(ctx, u);
		VCBuilder.addAdhesion(ctx, pc,pcu, em,montant);
		
		return ctx;
	}

	@Override
	public String getFileNameStandard(RdbLink em)
	{
		ParametresDTO param = new ParametresService().getParametres();
		
		if (mode==Mode.ALL_BULLETIN_PERIODE)
		{
			PeriodeCotisation pc =  em.find(PeriodeCotisation.class, idPeriode);
			return "bulletin-adhesion-"+param.nomAmap+"-"+pc.nom;	
		}
		else if (mode==Mode.ONE_BULLETIN_CREATED)
		{
			PeriodeCotisationUtilisateur pcu =  em.find(PeriodeCotisationUtilisateur.class, idPeriodeUtilisateur);
			Utilisateur u = pcu.utilisateur;
			return "bulletin-adhesion-"+param.nomAmap+"-"+pcu.periodeCotisation.nom+"-"+u.nom+" "+u.prenom;
		}
		else if (mode==Mode.ONE_BULLETIN_NOT_CREATED)
		{ 
			PeriodeCotisation pc =  em.find(PeriodeCotisation.class, idPeriode);
			Utilisateur u =  em.find(Utilisateur.class, idUtilisateur);
			return "bulletin-adhesion-"+param.nomAmap+"-"+pc.nom+"-"+u.nom+" "+u.prenom;
		}
		throw new AmapjRuntimeException();
		
	}

	@Override
	public String getNameToDisplayStandard(RdbLink em)
	{
		if (mode==Mode.ALL_BULLETIN_PERIODE)
		{
			PeriodeCotisation pc =  em.find(PeriodeCotisation.class, idPeriode);
			return "la liste des bulletins d'adhésion pour "+pc.nom;
		}
		else if (mode==Mode.ONE_BULLETIN_CREATED)
		{
			PeriodeCotisationUtilisateur pcu =  em.find(PeriodeCotisationUtilisateur.class, idPeriodeUtilisateur);
			Utilisateur u = pcu.utilisateur;
			return "le bulletin d'adhésion "+pcu.periodeCotisation.nom+" pour "+u.nom+" "+u.prenom;
		}
		else if (mode==Mode.ONE_BULLETIN_NOT_CREATED)
		{ 
			PeriodeCotisation pc =  em.find(PeriodeCotisation.class, idPeriode);
			Utilisateur u =  em.find(Utilisateur.class, idUtilisateur);
			return "le bulletin d'adhésion "+pc.nom+" pour "+u.nom+" "+u.prenom;
		}
		throw new AmapjRuntimeException();
	}

}
