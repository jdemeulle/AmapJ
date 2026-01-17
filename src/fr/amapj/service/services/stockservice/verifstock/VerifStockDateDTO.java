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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.contrat.modele.ModeleContratProduit;


/**
 * 
 */
public class VerifStockDateDTO
{
	// Les produits geres en stock 
	public List<ProduitEnStockDTO> prodStocks = new ArrayList<>();
	
	// Cle : id du produit commandé, valeur : consommateur associé
	public Map<Long,Consommateur> mapIdProdCdeConsommateur= new HashMap<>();
	
	// Cle : id du produit en stock, valeur : prodstock
	public Map<Long,ProduitEnStockDTO> mapIdProdStockPodStock= new HashMap<>();

	
	
	// Exploitation de la structure  
	
	public boolean isStockSuffisant(int indexDate)
	{
		for (ProduitEnStockDTO prodStock : prodStocks) 
		{
			if (prodStock.isStockSuffisant(indexDate)==false)
			{
				return false;
			}
		}
		return true;
	}
	
	public void computePrettyMessage(List<String> res, RdbLink em, SimpleDateFormat df, int indexDate, Date date) 
	{
		for (ProduitEnStockDTO prodStock : prodStocks) 
		{
			prodStock.computePrettyMessage(res,em,df,indexDate,date);	
		}	
	}
	
	/**
	 * Retourne true si il est possible de commander en plus une unité de ce produit à cette date 
	 * false sinon  
	 */
	public boolean isOneMoreAvailable(Long produitId,int indexDate) 
	{
		// On recherche le consommateur correspondant à ce produit commandé
		Consommateur consommateur = findConsommateur(produitId);
		if (consommateur==null)
		{
			return true;
		}
		ProduitEnStockDTO produitEnStockDTO = consommateur.produitCdeDTO.produitEnStockDTO;

		Qte qteRestante = produitEnStockDTO.getQteRestante(indexDate);
		
		return qteRestante.greaterOrEqual(consommateur.coefficient);  
	}	
	
	public List<Long> computeProduitCommandeIds()
	{
		List<Long> res = new ArrayList<>();
		
		for (ProduitEnStockDTO p : prodStocks) 
		{
			for (Consommateur consommateur : p.consommateurs)
			{
				res.add(consommateur.produitCdeDTO.idProduit);
			}
		}
		return res;
	}

	public void addDebugInfo(List<String> ls, int indexDate, RdbLink em) 
	{
		for (ProduitEnStockDTO produitEnStockDTO : prodStocks) 
		{
			produitEnStockDTO.addDebugInfo(ls, indexDate,em);
		}
		
	}
	


	// Navigation dans la structure
	

	/**
	 * Retourne le consommateur correspondant à ce produit commande 
	 * 
	 * Peut être null
	 */
	public Consommateur findConsommateur(Long produitIdCommande)
	{
		return mapIdProdCdeConsommateur.get(produitIdCommande);
		
	}
	
	
	public ProduitEnStockDTO findProduitEnStockDTO(Long idProduitEnStock) 
	{
		return mapIdProdStockPodStock.get(idProduitEnStock);
	}
	
	// Création de la structure 

	/**
	 * Crée le produit en stock si il n'est pas déjà créé
	 * 
	 * Ajoute systematiquement le consommateur proposé (le produit commandé)
	 */
	public void createProduitEnStockAndConsommateur(Long idProduitEnStock, Long idProduitCommande, double coefficient, int datesSize,boolean fullInfo,ModeleContratProduit mcp)
	{
		ProduitEnStockDTO prodStock = findProduitEnStockDTO(idProduitEnStock);
		
		boolean create = false;
		if (prodStock==null)
		{
			create = true;
			prodStock = new ProduitEnStockDTO();
			prodStock.idProduit = idProduitEnStock;
			prodStock.qteDispo = new int[datesSize];
			prodStock.hasConsommateurCommandable = new boolean[datesSize]; // Initialisé à false par défaut
			
			prodStocks.add(prodStock);
			mapIdProdStockPodStock.put(idProduitEnStock, prodStock);
		}
		
		
		// Mise à jour des informations du produit en stock si besoin
		if (fullInfo)
		{
			completeFullInfo(prodStock,create,mcp);
		}
		
		addConsommateur(idProduitCommande, coefficient, datesSize, prodStock);
		
	}

	private void completeFullInfo(ProduitEnStockDTO prodStock, boolean create,ModeleContratProduit mcp)
	{
		if (create)
		{
			prodStock.indx = mcp!=null ? mcp.indx : -1;
			prodStock.prix = mcp!=null ? mcp.prix : 0;
			prodStock.idModeleContrat = mcp!=null ? mcp.modeleContrat.id : null;
		}
		else
		{
			if (prodStock.idModeleContrat==null && mcp!=null)
			{
				prodStock.idModeleContrat = mcp.modeleContrat.id;
				prodStock.prix = mcp.prix;
				prodStock.indx = mcp.indx;
			}
			
			if (prodStock.idModeleContrat!=null && mcp!=null)
			{
				// On ceonserve les informations du modele de contrat avec le plus petit id 
				if (mcp.modeleContrat.id.longValue()<prodStock.idModeleContrat.longValue())
				{
					prodStock.idModeleContrat = mcp.modeleContrat.id;
					prodStock.prix = mcp.prix;
					prodStock.indx = mcp.indx;
				}
			}
		}
	}

	/**
	 * Ajoute ce consommateur , uniquement si il y a déjà un produit en stock pour l'accueillir 
	 */
	public void addConsommateurIfNeeded(Long idProduitEnStock, Long idProduitCommande, double coefficient,int datesSize)
	{
		ProduitEnStockDTO prodStock = findProduitEnStockDTO(idProduitEnStock);
		
		// Cas d'un produit qui n'impacte pas nos stocks
		if (prodStock==null)
		{
			return;
		}
		
		Consommateur c = prodStock.findConsommateur(idProduitCommande);
		
		if (c==null)
		{
			addConsommateur(idProduitCommande, coefficient, datesSize, prodStock);
		}	
	}
	
	private void addConsommateur(Long idProduitCommande, double coefficient, int datesSize,ProduitEnStockDTO prodStock)
	{
		ProduitCdeDTO produitCdeDTO = new ProduitCdeDTO();
		produitCdeDTO.produitEnStockDTO = prodStock;
		produitCdeDTO.idProduit = idProduitCommande;
		produitCdeDTO.qteMe = new int[datesSize];
		produitCdeDTO.qteOther = new int[datesSize];
		
		Consommateur c = new Consommateur();
		c.coefficient = coefficient;
		c.produitCdeDTO = produitCdeDTO;
		
		prodStock.consommateurs.add(c);
		
		mapIdProdCdeConsommateur.put(idProduitCommande, c);
	}	
}
