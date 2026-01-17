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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.FormatUtils;
import fr.amapj.common.LongUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.ModeleContratExclude;
import fr.amapj.model.models.contrat.modele.ModeleContratProduit;
import fr.amapj.model.models.contrat.modele.StockGestion;
import fr.amapj.model.models.contrat.modele.StockMultiContrat;
import fr.amapj.model.models.fichierbase.Producteur;
import fr.amapj.model.models.fichierbase.Produit;
import fr.amapj.model.models.produitextended.qtedispostock.QteDispoStock;
import fr.amapj.model.models.produitextended.reglesconversion.ProduitLimiteQuantite;
import fr.amapj.model.models.produitextended.reglesconversion.RegleConversionProduit;
import fr.amapj.model.models.produitextended.reglesconversion.RegleConversionStock;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.produitextended.ProduitExtendedService;

/**
 * Pour utiliser cette classe : il faut tout d'abord appeler la méthode 
 * 
 *  addAllDates()
 *  
 *  puis 
 *  
 *  addAllPoduitCommandes()
 *  
 *  puis 
 *  
 *  setQteMe() , setQteOther()
 * 
 * 
 */
public class VerifStockDTO
{
	// 
	public List<Date> dates = new ArrayList<>();
	
	// Cle : date, valeur : son index dans la liste 
	public Map<Date,Integer> mapDateIndex= new HashMap<>();
	
	public VerifStockDateDTO verifStockDateDTO = new VerifStockDateDTO();
	
	// Exploitation de la structure
	
	public boolean isStockSuffisant()
	{
		for (int indexDate = 0; indexDate < dates.size(); indexDate++)
		{
			if (verifStockDateDTO.isStockSuffisant(indexDate)==false)
			{
				return false;
			}
		}
		return true;
	}
	
	public void computePrettyMessage(List<String> res, RdbLink em,SimpleDateFormat df) 
	{
		for (int indexDate = 0; indexDate < dates.size(); indexDate++)
		{
			verifStockDateDTO.computePrettyMessage(res,em,df,indexDate,dates.get(indexDate));	
		}
	}
	
	/**
	 * Affichage des informations de debug pour cette verification de stock
	 */
	public List<String> showDebug(RdbLink em) 
	{
		List<String> ls = new ArrayList<>();
		SimpleDateFormat df = FormatUtils.getStdDate();
		for (int i = 0; i < dates.size(); i++) 
		{
			Date date = dates.get(i);
			ls.add("Date : "+df.format(date));
			
			verifStockDateDTO.addDebugInfo(ls,i,em);
		}
		
		return ls;
	}


	// Navigation dans la strcuture

	public int findDate(Date date)
	{
		Integer indexDate =  mapDateIndex.get(date);
		return (indexDate!=null) ? indexDate : -1;
	}
	
	// Création initiale de la structure

	
	/**
	 * Ajout des dates à considerer
	 */
	public void addAllDates(List<Date> ds)
	{
		// La liste est triée, puis on calcule les index
		ds.sort(null);
		
		for (int i = 0; i < ds.size(); i++)
		{
			Date date = ds.get(i);
			dates.add(date);
			mapDateIndex.put(date, i);
		}
	}
	
	/**
	 * Ajout de la liste des produits commandés à suivre initialement
	 */
	public void addAllProduits(List<ModeleContratProduit> mcps,boolean fullProdInfo,RdbLink em ,Producteur producteur,List<Long> additionalProduits,RegleConversionProduit regles)
	{
		// On commence par créer la liste des produits en stock qui nous interesse, en partant de la liste des produits commandes sur ce contrat
		for (ModeleContratProduit mcp : mcps)
		{
			Long idProduit = mcp.produit.id;
			RegleConversionStock r = regles.findReglesConversionStock(idProduit);
			
			switch (r.limiteQuantite) 
			{
			case SUIVI_STANDARD: 			
				verifStockDateDTO.createProduitEnStockAndConsommateur(idProduit,idProduit,1d,dates.size(),fullProdInfo,mcp);
				break;
			case SUIVI_AVEC_REGLE_CALCUL : 	
				verifStockDateDTO.createProduitEnStockAndConsommateur(r.idProduitEnStock,idProduit,r.coefficient,dates.size(),fullProdInfo,null);
				break;
			default : 
				// Do nothing
				break;
			}
		}
		
		// Dans le cas du multi contrat, il est aussi necessaire d'ajouter en consommateur tous les produits du producteur, car ils
		// peuvent avoir des consequences sur le stock même si ils ne sont pas dans le contrat que l'on observe
		for (Long idProduitCde : additionalProduits) 
		{
			RegleConversionStock r = regles.findReglesConversionStock(idProduitCde);
			if (r.limiteQuantite==ProduitLimiteQuantite.SUIVI_AVEC_REGLE_CALCUL)
			{
				verifStockDateDTO.addConsommateurIfNeeded(r.idProduitEnStock,r.idProduit,r.coefficient,dates.size());
			}
		}
	}
	
	
	 
	
	
	// Peuplement de la structure avec les valeurs en base
	
	/**
	 * Permet de positionner les valeurs qteMe à partir de contratDTO 
	 */
	public void setQteMe(ContratDTO dto) 
	{
		for (int j = 0; j < dto.contratColumns.size(); j++)
		{
			Long produitId = dto.contratColumns.get(j).produitId;
			
			Consommateur consommateur = verifStockDateDTO.findConsommateur(produitId);
			
			if (consommateur!=null)
			{
				ProduitCdeDTO produitCdeDTO = consommateur.produitCdeDTO;
				
				for (int i = 0; i < dto.contratLigs.size(); i++)
				{
					Date date = dto.contratLigs.get(i).date;
					int indexDate = findDate(date);
					
					if (indexDate!=-1)
					{
						produitCdeDTO.qteMe[indexDate] = dto.cell[i][j].qte;
					}
				}
			}
		}
	}
	
	
	
	
	// Les informations des produits en stocks - ne sera utilisé que pour la saisie, pas d'interet pour la verification des stocks
	
	public void completeInfoProduitAndSortProduit(RdbLink em)
	{
		for (ProduitEnStockDTO p : verifStockDateDTO.prodStocks)
		{
			Produit prod = em.find(Produit.class, p.idProduit);
			p.nomProduit = prod.nom;
			p.condtionnementProduit = prod.conditionnement;
		}
		
		// Tri des produits 
		verifStockDateDTO.prodStocks.sort(Comparator.comparing((ProduitEnStockDTO e)->e.idModeleContrat,Comparator.nullsFirst(Comparator.naturalOrder())).thenComparing(e->e.indx).thenComparing(e->e.nomProduit+","+e.condtionnementProduit));
	}
	
	/**
	 * Calcule le champ HasConsommateurCommandable de prodstock à partir de la liste des contrats qui peuvent impacter 
	 */
	public void computeHasConsommateurCommandable(List<Long> idModeleContrats,RegleConversionProduit regles,RdbLink em,Date dateRef)
	{
		//  
		for (Long idModeleContrat : idModeleContrats) 
		{
			// On récupère les exclusions
			Map<Excluded, Integer> excluded = getAllExcludedDateProduit(em, idModeleContrat);
			
			// On récupère les dates qui nous interessent
			List<Date> dates = getAllDates(em, idModeleContrat, dateRef);
			
			// On récupère les produits du contrat
			List<ModeleContratProduit> prods = getAllProduit(em, idModeleContrat);
			
			// On traite chaque couple produit  / date
			for (Date date : dates) 
			{
				int indexDate = findDate(date);				
				if (indexDate!=-1)
				{
					for (ModeleContratProduit prod : prods) 
					{
						Consommateur consommateur = verifStockDateDTO.findConsommateur(prod.produit.id);
						if (consommateur!=null)
						{
							ProduitCdeDTO produitCdeDTO = consommateur.produitCdeDTO;
							if (isExcluded(excluded,date,produitCdeDTO)==false)
							{
								produitCdeDTO.produitEnStockDTO.hasConsommateurCommandable[indexDate] = true;
							}
						}
					}
				}
			}
		}	
	}
	
	
	private boolean isExcluded(Map<Excluded, Integer> excluded, Date date,ProduitCdeDTO produitCdeDTO) 
	{
		// On verifie au niveau de la date complete
		boolean b = excluded.containsKey(new Excluded(date,null));
		if (b==true)
		{
			return true;
		}
		
		// On verifie au niveau du couple date / produit 
		return excluded.containsKey(new Excluded(date,produitCdeDTO.idProduit));
	}
	
	
	static public class Excluded
	{
		// Jamais null
		public Date date;
		
		// Peut être null
		public Long idProduit;

		public Excluded(Date date, Long idProduit) 
		{
			this.date = date;
			this.idProduit = idProduit;
		}
		
		@Override
		public boolean equals(Object obj) 
		{
			if (obj instanceof Excluded) 
			{
				Excluded c = (Excluded) obj;
				
	            return LongUtils.equals(idProduit,c.idProduit) && date.equals(c.date);
	        }
	        return false; 
		}
		
		@Override
		public int hashCode() 
		{
			return (idProduit!=null ? idProduit.hashCode():0)+date.hashCode();
		}
	}
	
	private Map<Excluded,Integer> getAllExcludedDateProduit(RdbLink em, Long idModeleContrat)
	{
		TypedQuery<ModeleContratExclude> q = em.createQuery("select mce from ModeleContratExclude mce where mce.modeleContrat.id=:id",ModeleContratExclude.class); 
		q.setParameter("id", idModeleContrat);

		List<ModeleContratExclude> ls = q.getResultList();
		Map<Excluded,Integer> res = new HashMap<>();
		for (ModeleContratExclude e : ls) 
		{
			res.put(new Excluded(e.date.dateLiv,e.produit==null ? null : e.produit.produit.id), 1);
		}
		return res;
	}
	
	private List<Date> getAllDates(RdbLink em, Long idModeleContrat,Date dateRef)
	{
		TypedQuery<Date> q = em.createQuery("select mcd.dateLiv from ModeleContratDate mcd where mcd.modeleContrat.id=:id AND mcd.dateLiv >= :dateRef",Date.class); 
		q.setParameter("id", idModeleContrat);
		q.setParameter("dateRef", dateRef);
		
		return q.getResultList();
	}
	
	private List<ModeleContratProduit> getAllProduit(RdbLink em, Long idModeleContrat)
	{
		TypedQuery<ModeleContratProduit> q = em.createQuery("select mcp from ModeleContratProduit mcp where mcp.modeleContrat.id=:id",ModeleContratProduit.class); 
		q.setParameter("id", idModeleContrat);
		
		return q.getResultList();
	}
	
	// Chargement des quantités commandés par les autres depuis la base de données
	
	/**
	 * On insére les quantités commandés par les autres 
	 */
	public void insertQteCommandeOther(RdbLink em, ModeleContrat mc,Long contratId)
	{
		List<Long> produitCommandeIds = verifStockDateDTO.computeProduitCommandeIds();
		
		List<Object[]> ls = loadQteOther(produitCommandeIds,dates,em,mc,contratId);
		
		for (Object[] r : ls)
		{
			int qteCommande =  ((Long) r[0]).intValue();
			Date date = (Date) r[1];
			Long produitId = (Long) r[2];
			
			setQteCommandeOther(date,produitId,qteCommande);
		}
	}
	
	public void setQteCommandeOther(Date date, Long produitId, int qteCommande)
	{
		int indexDate = findDate(date);
		ProduitCdeDTO produitCdeDTO = verifStockDateDTO.findConsommateur(produitId).produitCdeDTO;
		produitCdeDTO.qteOther[indexDate] = qteCommande;
	}
	
	private List<Object[]> loadQteOther(List<Long> produitCommandeIds, List<Date> dates, RdbLink em,ModeleContrat mc,Long contratId)
	{
		switch (mc.stockMultiContrat)
		{
		case NON:
			return loadQteOtherMonoContrat(em,mc,contratId,produitCommandeIds,dates);

		case OUI:
			return loadQteOtherMultiContrat(em,mc,contratId,produitCommandeIds,dates);

		default:
			throw new AmapjRuntimeException();
		}
	}
	
	private List<Object[]> loadQteOtherMonoContrat(RdbLink em, ModeleContrat mc, Long contratId,List<Long> produitCommandeIds, List<Date> dates)
	{		
		if (produitCommandeIds.size()==0 || dates.size()==0)
		{
			return new ArrayList<>();
		}
		
		Query q = em.createQuery("select sum(c.qte),c.modeleContratDate.dateLiv,c.modeleContratProduit.produit.id from ContratCell c "
				+ "where c.contrat.modeleContrat=:mc AND "
				+ (contratId!=null ? "c.contrat.id<>:contratId AND " : " ")
				+ "c.modeleContratDate.dateLiv IN :dates AND "
				+ "c.modeleContratProduit.produit.id IN :produits "
				+ "group by c.modeleContratDate.dateLiv,c.modeleContratProduit.produit.id");
		
		if (contratId!=null)
		{
			q.setParameter("contratId", contratId);
		}
		q.setParameter("mc", mc);
		q.setParameter("dates", dates);
		q.setParameter("produits", produitCommandeIds);
		
		return q.getResultList();
		
	}
	
	private List<Object[]> loadQteOtherMultiContrat(RdbLink em, ModeleContrat mc, Long contratId,List<Long> produitCommandeIds, List<Date> dates)
	{
		// See https://stackoverflow.com/questions/2488930/passing-empty-list-as-parameter-to-jpa-query-throws-error
		if (produitCommandeIds.size()==0 || dates.size()==0)
		{
			return new ArrayList<>();
		}
				
		 Query q = em.createQuery("select sum(c.qte),c.modeleContratDate.dateLiv,c.modeleContratProduit.produit.id from ContratCell c "
				+ "where c.contrat.modeleContrat.producteur=:p AND "
				+ "c.contrat.modeleContrat.stockGestion=:g AND "
				+ "c.contrat.modeleContrat.stockMultiContrat=:m AND "
				+ (contratId!=null ? "c.contrat.id<>:contratId AND " : " ")
				+ "c.modeleContratDate.dateLiv IN :dates AND "
				+ "c.modeleContratProduit.produit.id IN :produits "
				+ "group by c.modeleContratDate.dateLiv,c.modeleContratProduit.produit.id");
		
		
		q.setParameter("p", mc.producteur);
		q.setParameter("g", StockGestion.OUI);
		q.setParameter("m", StockMultiContrat.OUI);
		if (contratId!=null)
		{
			q.setParameter("contratId", contratId);
		}
		q.setParameter("dates", dates);
		q.setParameter("produits", produitCommandeIds);
		
		return q.getResultList();
		
		
	}
	
	
	//
	/**
	 * On insere les quantites dispo en stock 
	 * @return 
	 */
	public QteDispoStock insertQteDispoStock(RdbLink em, ModeleContrat mc)
	{
		QteDispoStock qteDispoStock = new ProduitExtendedService().loadQteDispoStock(em, mc);
		for (ProduitEnStockDTO produitEnStockDTO : verifStockDateDTO.prodStocks)
		{
			for (int indexDate = 0; indexDate < dates.size(); indexDate++)
			{
				produitEnStockDTO.qteDispo[indexDate] = qteDispoStock.findQteDispo(mc.stockIdentiqueDate,dates.get(indexDate),produitEnStockDTO.idProduit);
			}
		}
		return qteDispoStock;
	}
	
}
