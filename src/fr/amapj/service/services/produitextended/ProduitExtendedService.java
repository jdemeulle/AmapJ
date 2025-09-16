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
 package fr.amapj.service.services.produitextended;

import javax.persistence.TypedQuery;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.amapj.common.GzipUtils;
import fr.amapj.common.SQLUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.StockMultiContrat;
import fr.amapj.model.models.fichierbase.Producteur;
import fr.amapj.model.models.fichierbase.Produit;
import fr.amapj.model.models.produitextended.ProduitExtendedParam;
import fr.amapj.model.models.produitextended.ProduitExtendedTyp;
import fr.amapj.model.models.produitextended.qtedispostock.QteDispoStock;
import fr.amapj.model.models.produitextended.reglesconversion.RegleConversionProduit;
import fr.amapj.model.models.produitextended.reglesconversion.RegleConversionStock;
import fr.amapj.service.services.produit.ProduitDTO;

/**
 * Contient toutes les méthodes pour charger et sauvevarder en base de données
 * les données de la table produitextendedparam 
 *
 */
public class ProduitExtendedService
{
	
	
	// PARTIE QTEDISPOSTOCK
	
	public QteDispoStock loadQteDispoStock(RdbLink em, ModeleContrat mc)
	{
		ProduitExtendedParam p = findQteDispoStockExtendedParam(em, mc);
		return convertQteDispoStock(p);
	}
	
	private ProduitExtendedParam findQteDispoStockExtendedParam(RdbLink em, ModeleContrat mc)
	{
		Producteur p = mc.producteur;
		if (mc.stockMultiContrat==StockMultiContrat.NON)
		{
			return findQteDispoStockExtendedParamMonoContrat(em,mc,p);
		}
		else
		{
			return findQteDispoStockExtendedParamMultiContrat(em,p);
		}
	}
	
	private ProduitExtendedParam findQteDispoStockExtendedParamMonoContrat(RdbLink em, ModeleContrat mc,Producteur p) 
	{
		TypedQuery<ProduitExtendedParam> q1 = em.createQuery("select c from ProduitExtendedParam c where "
				+ "c.modeleContrat=:mc AND "
				+ "c.producteur=:p AND "
				+ "c.typ=:typ",ProduitExtendedParam.class);
		q1.setParameter("mc", mc);
		q1.setParameter("p", p);
		q1.setParameter("typ", ProduitExtendedTyp.QTE_DISPO_STOCK);
		
		return SQLUtils.oneOrZero(q1);
	}


	private ProduitExtendedParam findQteDispoStockExtendedParamMultiContrat(RdbLink em, Producteur p) 
	{
		TypedQuery<ProduitExtendedParam> q1 = em.createQuery("select c from ProduitExtendedParam c where "
				+ "c.modeleContrat IS NULL AND "
				+ "c.producteur=:p AND "
				+ "c.typ=:typ",ProduitExtendedParam.class);

		q1.setParameter("p", p);
		q1.setParameter("typ", ProduitExtendedTyp.QTE_DISPO_STOCK);
		
		return SQLUtils.oneOrZero(q1);
	}

	private QteDispoStock convertQteDispoStock(ProduitExtendedParam param)
	{
		QteDispoStock res;
		if (param==null)
		{
			res = new QteDispoStock();
		}
		else
		{
			res = (QteDispoStock)  createGson().fromJson(GzipUtils.uncompress(param.content),QteDispoStock.class);
		}
		res.setDefault();
		
		return res;
	}



	public void saveQteDispoStock(QteDispoStock qteDispoStock,Long modeleContratId,RdbLink em)
	{
		ModeleContrat mc = em.find(ModeleContrat.class,modeleContratId);
		
		ProduitExtendedParam param = findQteDispoStockExtendedParam(em, mc);
		if (param==null)
		{
			param = createStockExtendedParam(em, mc);
		}
		param.content = GzipUtils.compress(createGson().toJson(qteDispoStock));
	}



	private ProduitExtendedParam createStockExtendedParam(RdbLink em, ModeleContrat mc)
	{
		ProduitExtendedParam param = new ProduitExtendedParam();
		param.typ = ProduitExtendedTyp.QTE_DISPO_STOCK;
		param.producteur = mc.producteur;
		param.modeleContrat = mc.stockMultiContrat==StockMultiContrat.NON ? mc : null;
				
		em.persist(param);
		return param;
	}
	
	
	// PARTIE REGLES_CONVERSION	
	public RegleConversionProduit loadRegleConversionProduit(RdbLink em, Producteur p)
	{
		return convertRegleConversionProduit(findRegleConversionProduitExtendedParam(em, p));
	}
	
	private ProduitExtendedParam findRegleConversionProduitExtendedParam(RdbLink em, Producteur p)
	{
		
		
		TypedQuery<ProduitExtendedParam> q1 = em.createQuery("select c from ProduitExtendedParam c where "
												+ "c.modeleContrat IS NULL AND "
												+ "c.producteur=:p AND "
												+ "c.typ=:typ",ProduitExtendedParam.class);
		q1.setParameter("p", p);
		q1.setParameter("typ", ProduitExtendedTyp.REGLES_CONVERSION_STOCK);
		
		return SQLUtils.oneOrZero(q1);
	}
	

	private RegleConversionProduit convertRegleConversionProduit(ProduitExtendedParam param)
	{
		RegleConversionProduit res;
		if (param==null)
		{
			res = new RegleConversionProduit();
		}
		else
		{
			res = (RegleConversionProduit)  createGson().fromJson(GzipUtils.uncompress(param.content),RegleConversionProduit.class);
		}
		res.setDefault();
		
		return res;
	}



	public void saveRegleConversionProduit(RegleConversionProduit qteDispoStock,Producteur p,RdbLink em)
	{
		ProduitExtendedParam param = findRegleConversionProduitExtendedParam(em, p);
		if (param==null)
		{
			param = createRegleConversionProduitExtendedParam(em, p);
		}
		param.content = GzipUtils.compress(createGson().toJson(qteDispoStock));
	}
	
	
	
	private ProduitExtendedParam createRegleConversionProduitExtendedParam(RdbLink em,Producteur p)
	{
		ProduitExtendedParam param = new ProduitExtendedParam();
		param.typ = ProduitExtendedTyp.REGLES_CONVERSION_STOCK;
		param.producteur = p;
		param.modeleContrat = null;
				
		em.persist(param);
		return param;
	}
	
	
	public void saveRegleConversionOneProduit(Produit produit,ProduitDTO dto,RdbLink em)
	{
		RegleConversionProduit r = loadRegleConversionProduit(em,produit.producteur);
		
		RegleConversionStock stock = r.findReglesConversionStock(produit.id);
		
		stock.coefficient = dto.coefficient;
		stock.idProduitEnStock = dto.idProduitEnStock;
		stock.limiteQuantite = dto.limiteQuantite;
		
		saveRegleConversionProduit(r, produit.producteur,em);
		
	}
	
	

	private Gson createGson()
	{
		return new GsonBuilder().setDateFormat("dd/MM/yyyy").create();
	}
	
	
	
}
