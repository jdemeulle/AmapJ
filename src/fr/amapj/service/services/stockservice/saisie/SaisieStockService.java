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
 package fr.amapj.service.services.stockservice.saisie;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.DateUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.ModeleContratProduit;
import fr.amapj.model.models.contrat.modele.StockGestion;
import fr.amapj.model.models.contrat.modele.StockMultiContrat;
import fr.amapj.model.models.produitextended.qtedispostock.QteDispoStock;
import fr.amapj.model.models.produitextended.qtedispostock.QteDispoStockProduit;
import fr.amapj.model.models.produitextended.reglesconversion.RegleConversionProduit;
import fr.amapj.service.services.produitextended.ProduitExtendedService;
import fr.amapj.service.services.stockservice.verifstock.VerifStockDTO;

/**
 * Contient toutes les méthodes pour la saisie des stocks et leur sauvegarde en base de données 
 *
 */
public class SaisieStockService
{
	
	/**
	 * Chargement des données de stock pour un ou plusieurs contrats,
	 * dans le but de faire la saisie des quantités disponibles en stock 
	 * 
	 */
	@DbRead
	public SaisieStockDTO loadStockInfo(Long idModeleContrat,boolean loadStockOther,int nbJourRef)
	{
		RdbLink em = RdbLink.get();
		ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);
		
		SaisieStockDTO dto = new SaisieStockDTO();
		
		Date ref = DateUtils.addDays(DateUtils.getDateWithNoTime(),nbJourRef);
		List<Long> idModeleContrats = findAllModeleContrat(em,mc,ref);
	
		dto.modeleContratId = mc.id;
		dto.nomModeleContrats = computeNom(mc,idModeleContrats,em);
		
		dto.modeleContratStockGestion = mc.stockGestion;
		dto.modeleContratStockIdentiqueDate = mc.stockIdentiqueDate;
		dto.modeleContratStockMultiContrat = mc.stockMultiContrat;
		
		VerifStockDTO verif = new VerifStockDTO();
		
		// On charge les regles de conversion 
		RegleConversionProduit regles = new ProduitExtendedService().loadRegleConversionProduit(em, mc.producteur);
		
		// On calcule toutes les dates concernées par cette vérification de stock et on initialize le VerifStockDTO 
		verif.addAllDates(findAllDates(em,mc,ref));
		
		// On calcule les produits commandés et les produits en stocks concernés par cette vérification de stock
		List<ModeleContratProduit> mcps = getAllProduitInFutur(em,idModeleContrats);
		List<Long> additionalProduits = new ArrayList<>();
		verif.addAllProduits(mcps, true, em, mc.producteur, additionalProduits,regles);
		
		// On calcule le champ hasConsommateurCommandable
		verif.computeHasConsommateurCommandable(idModeleContrats, regles, em, ref);
				
		// On récupère les quantités commandées par les autres et on les insere
		if (loadStockOther)
		{
			verif.insertQteCommandeOther(em, mc, null);
		}
						
		// On récupére les quantités de stock et on les insere
		QteDispoStock qteDispoStock = verif.insertQteDispoStock(em,mc); 
		
		// On trie les produits en stock 
		verif.completeInfoProduitAndSortProduit(em);
		
		
		dto.verifStockDTO = verif;
		dto.qteDispoStock = qteDispoStock;
		
		return dto;
	}
	


	private List<String> computeNom(ModeleContrat mc, List<Long> idModeleContrats, RdbLink em) 
	{
		List<String> res = new ArrayList<>();
		for (Long id : idModeleContrats) 
		{
			res.add(em.find(ModeleContrat.class, id).nom);
		}
		return res;
	}


	// PARTIE CHARGEMENT DES DONNEES DES CONTRATS pour récuperer les dates et les produits 
	
	private List<Long> findAllModeleContrat(RdbLink em, ModeleContrat mc, Date ref) 
	{
		switch (mc.stockMultiContrat) 
		{
		case NON: return computeProdsAndDatesMonoContrat(em,mc);
		case OUI: return computeProdsAndDatesMultiContrat(em,mc,ref);
		default:  throw new AmapjRuntimeException();
		}
	}

	
	private List<Long> computeProdsAndDatesMonoContrat(RdbLink em, ModeleContrat mc) 
	{
		List<Long> ids = new ArrayList<>();
		ids.add(mc.id);
		return ids;
	}

	private List<Long> computeProdsAndDatesMultiContrat(RdbLink em, ModeleContrat mc, Date ref) 
	{
		 TypedQuery<Long> q = em.createQuery("select distinct(c.modeleContrat.id) from ModeleContratDate c "
					+ "where c.modeleContrat.producteur=:p AND "
					+ "c.modeleContrat.stockGestion=:g AND "
					+ "c.modeleContrat.stockMultiContrat=:m AND "
					+ "c.dateLiv >= :dateRef "
					+ "ORDER BY c.modeleContrat.id",Long.class);

		
		q.setParameter("p", mc.producteur);
		q.setParameter("g", StockGestion.OUI);
		q.setParameter("m", StockMultiContrat.OUI);
		q.setParameter("dateRef", ref);
		
		return q.getResultList();
	}
	
	
	
	
	private List<Date> findAllDates(RdbLink em, ModeleContrat mc, Date ref) 
	{
		switch (mc.stockMultiContrat) 
		{
		case NON: return findAllDatesMonoContrat(em,mc,ref);
		case OUI: return findAllDatesMultiContrat(em,mc,ref);
		default:  throw new AmapjRuntimeException();
		}
	}

	
	private List<Date> findAllDatesMonoContrat(RdbLink em, ModeleContrat mc,Date ref) 
	{
		 TypedQuery<Date> q = em.createQuery("select c.dateLiv from ModeleContratDate c "
					+ "where c.modeleContrat=:mc AND "
					+ "c.dateLiv >= :dateRef "
					+ "ORDER BY c.dateLiv",Date.class);

		
		q.setParameter("mc", mc);
		q.setParameter("dateRef", ref);
		
		return q.getResultList();
	}

	private List<Date> findAllDatesMultiContrat(RdbLink em, ModeleContrat mc, Date ref) 
	{
		 TypedQuery<Date> q = em.createQuery("select distinct(c.dateLiv) from ModeleContratDate c "
					+ "where c.modeleContrat.producteur=:p AND "
					+ "c.modeleContrat.stockGestion=:g AND "
					+ "c.modeleContrat.stockMultiContrat=:m AND "
					+ "c.dateLiv >= :dateRef "
					+ "ORDER BY c.dateLiv",Date.class);

		
		q.setParameter("p", mc.producteur);
		q.setParameter("g", StockGestion.OUI);
		q.setParameter("m", StockMultiContrat.OUI);
		q.setParameter("dateRef", ref);
		
		return q.getResultList();
	}


	private List<ModeleContratProduit> getAllProduitInFutur(RdbLink em, List<Long> idModeleContrats)
	{
		// See https://stackoverflow.com/questions/2488930/passing-empty-list-as-parameter-to-jpa-query-throws-error
		if (idModeleContrats.size()==0)
		{
			return new ArrayList<>();
		}
		
		TypedQuery<ModeleContratProduit> q = em.createQuery("select c from ModeleContratProduit c where c.modeleContrat.id IN :ids ",ModeleContratProduit.class);
		q.setParameter("ids", idModeleContrats);
		return q.getResultList();
	}
	
	
	

	@DbWrite
	public void saveStockInfo(SaisieStockDTO s) 
	{
		RdbLink em = RdbLink.get();
		
		// On supprime toutes les dates dans le passé de plus de 30 jours 
		Date ref = DateUtils.addDays(DateUtils.getDateWithNoTime(),-30);
		List<QteDispoStockProduit> toRemove = new ArrayList<>();
		for (QteDispoStockProduit p : s.qteDispoStock.qteDispoStockProduits) 
		{
			if (p.date!=null && p.date.before(ref))
			{
				toRemove.add(p);
			}
		}
		s.qteDispoStock.qteDispoStockProduits.removeAll(toRemove);
		
		//
		new ProduitExtendedService().saveQteDispoStock(s.qteDispoStock, s.modeleContratId, em);
	}
	
}
