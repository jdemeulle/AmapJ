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
 package fr.amapj.service.services.stockservice.verifstock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import fr.amapj.common.DateUtils;
import fr.amapj.common.FormatUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.ModeleContratProduit;
import fr.amapj.model.models.contrat.modele.StockGestion;
import fr.amapj.model.models.contrat.modele.StockMultiContrat;
import fr.amapj.model.models.produitextended.reglesconversion.RegleConversionProduit;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.mescontrats.MesContratsService;
import fr.amapj.service.services.mescontrats.MonContratDTO;
import fr.amapj.service.services.produitextended.ProduitExtendedService;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.ModeSaisie;


/**
 * Contient toutes les méthodes pour la vérification du stock lors de la saisie des contrats
 *
 */
public class VerifStockService
{
	
	/**
	 * 
	 * Insertion des informations de stock, lors du chargement du contrat
	 * 
	 * On coche les cases comme stock disponible ou indisponible 
	 * 
	 * Ceci a une valeur informative seulement, car le stock peut évoluer entre le début de la saisie
	 * et la fin de celle ci
	 * 
	 */
	public void insertInfoStock(RdbLink em, ModeleContrat mc, MonContratDTO m, ModeSaisie modeSaisie)
	{
		// 
		if (mc.stockGestion==StockGestion.NON || modeSaisie==ModeSaisie.CHEQUE_SEUL || modeSaisie==ModeSaisie.READ_ONLY)
		{
			m.verifStockDTO = new VerifStockDTO();
			return;
		}
		
		// 
		ContratDTO dto = m.contratDTO;

		// 
		VerifStockDTO verif = loadVerifStock(em, mc, dto,dto.contratId);
		m.verifStockDTO = verif;
		
		// Calcul des cases disponibles
		for (int i = 0; i < dto.contratLigs.size(); i++)
		{
			Date date = dto.contratLigs.get(i).date;
			int indexDate = verif.findDate(date);
			
			if (indexDate!=-1)
			{
				for (int j = 0; j < dto.contratColumns.size(); j++)
				{
					// On verifie uniquement sur les cases pour lesquelles l'utilisateur n'a pas commandé
					if (dto.cell[i][j].qte==0)
					{
						Long produitId = dto.contratColumns.get(j).produitId;
						dto.cell[i][j].available = verif.verifStockDateDTO.isOneMoreAvailable(produitId,indexDate);
					}
				}	
			}
		}
		
	}


	/**
	 * Permet de charger un VerifStockDTO pour verifier les quantités en stock lors de la saisie d'un contrat.
	 * 
	 * Si le contrat ne gere pas les stocks, alors retourne un VerifStockDTO vide 
	 * 
	 * Les quantites Me ne sont pas chargées, il faudra les charger plus tard avec un setQteMe , à faire au bon moment 
	 */
	public VerifStockDTO loadVerifStock(RdbLink em ,ModeleContrat mc,ContratDTO contratDTO,Long idContrat)
	{
		if (mc.stockGestion==StockGestion.NON)
		{
			return new VerifStockDTO();
		}
		
		VerifStockDTO res = new VerifStockDTO();
		
		// On charge les regles de conversion 
		RegleConversionProduit regles = new ProduitExtendedService().loadRegleConversionProduit(em, mc.producteur);
		
		// On calcule toutes les dates concernées par cette vérification de stock et on initialize le VerifStockDTO 
		res.addAllDates(computeDates(contratDTO));
		
		// On calcule les produits commandés et les produits en stocks concernés par cette vérification de stock
		List<ModeleContratProduit> mcps = getAllProduit(em,mc);
		List<Long> additionalProduits = getAdditionnalProduits(em,mc);
		res.addAllProduits(mcps, false, em, mc.producteur, additionalProduits,regles);
				
		// On récupère les quantités commandées par les autres et on les insere
		res.insertQteCommandeOther(em, mc, idContrat);		
		
		// On récupére les quantités de stock et on les insere
		res.insertQteDispoStock(em,mc); 
		
		return res;
	}



	private List<Date> computeDates(ContratDTO dto)
	{
		Date ref = DateUtils.getDateWithNoTime();
		List<Date> ds = new ArrayList<>();
		
		for (int i = 0; i < dto.contratLigs.size(); i++)
		{
			Date date = dto.contratLigs.get(i).date;
			
			if (date.before(ref)==false)
			{
				ds.add(date);
			}
		}
		return ds;
	}
	
	/**
	 * Retrouve la liste des produits
	 */
	public List<ModeleContratProduit> getAllProduit(RdbLink em, ModeleContrat mc)
	{
		TypedQuery<ModeleContratProduit> q = em.createQuery("select mcp from ModeleContratProduit mcp where mcp.modeleContrat=:mc",ModeleContratProduit.class); 
		q.setParameter("mc", mc);
		
		return q.getResultList();
	}
	
	// Dans le cas du multi contrat, il est aussi necessaire d'ajouter en consommateur tous les produits du producteur, car ils
	// peuvent avoir des consequences sur le stock même si ils ne sont pas dans le contrat que l'on observe
	private List<Long> getAdditionnalProduits(RdbLink em, ModeleContrat mc)
	{
		if (mc.stockMultiContrat==StockMultiContrat.NON)
		{
			return new ArrayList<>();
		}
		
		TypedQuery<Long> q = em.createQuery("select p.id from Produit p " +
				"where p.producteur.id=:prd " +
				"order by p.nom,p.conditionnement",Long.class);
		q.setParameter("prd", mc.producteur.id);
		
		return q.getResultList();
		
	}

	/**
	 * Calcul d'un message expliquant clairement l'erreur de stock 
	 */
	@DbRead
	public List<String> computePrettyMessage(VerifStockDTO verifStockDTO) 
	{
		RdbLink em = RdbLink.get();
		List<String> res = new ArrayList<>();
		SimpleDateFormat df = FormatUtils.getStdDate();
		
		res.add("Les quantités commandées sont trop importantes.");
		
		verifStockDTO.computePrettyMessage(res, em, df);
	
		return res;
	}
	
	
	/**
	 * Donne pour un modele de contrat donné toutes les informations de stock pour debug 
	 */
	@DbRead
	public List<String> showDebug(Long modeleContratId) 
	{
		RdbLink em = RdbLink.get();
		MonContratDTO m = new MesContratsService().loadMonContratDTO(modeleContratId, null, ModeSaisie.STANDARD);
		return m.verifStockDTO.showDebug(em);
	}
	
	
}
