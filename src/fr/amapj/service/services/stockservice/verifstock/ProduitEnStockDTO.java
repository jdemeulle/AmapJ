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

import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.fichierbase.Produit;

/**
 * 
 */
public class ProduitEnStockDTO
{
	public Long idProduit;
	
	public int [] qteDispo;
	
	// Determine si ce produit en stock à au moins un produit commandé qui est commandable à cette date (non exclu)
	public boolean [] hasConsommateurCommandable;
	
	public List<Consommateur> consommateurs = new ArrayList<>();
	
	// Les 5 données suivantes sont optionnelles (chargées uniquement si fullInfo = true)  
	public String nomProduit;
	
	public String condtionnementProduit;
	
	// Champs qui serviront pour le classement
	// Si le produit vient de deux contrats différents, on garde les informations du contrat dont l'id est le plus petit 
	
	public Integer prix;
	
	public int indx;
	
	public Long idModeleContrat;
	
	
	/**
	 * Calcule de la quantité restante de ce produit, une fois qu'il a été consommé par les autres et par moi  
	 */
	public Qte getQteRestante(int indexDate) 
	{
		// On démarre avec la quantité disponible
		Qte qte = new Qte(qteDispo[indexDate]);
		
		// Pour chaque consommateur, on lui demande de consommer Me et Other
		for (Consommateur consommateur : consommateurs) 
		{		
			consommateur.consommerOther(qte,indexDate);
			consommateur.consommerMe(qte,indexDate);
		}
		return qte;
	}
	
	/**
	 * Retourne la quantité totale consommé
	 */
	public double getQteConsomme(int indexDate) 
	{
		// 
		Qte qte = new Qte(0);
		
		// Pour chaque consommateur, on lui demande de consommer Me et Other
		for (Consommateur consommateur : consommateurs) 
		{		
			consommateur.consommerOther(qte,indexDate);
			consommateur.consommerMe(qte,indexDate);
		}
		return Math.abs(qte.qte);

	}
	
	
	
	public boolean isStockSuffisant(int indexDate) 
	{
		// On démarre avec la quantité disponible
		Qte qte = new Qte(qteDispo[indexDate]);
		
		// Pour chaque consommateur, on lui demande de consommer Other
		for (Consommateur consommateur : consommateurs) 
		{		
			consommateur.consommerOther(qte,indexDate);
		}
		
		// On verifie ensuite si ma consommation est possible 
		for (Consommateur consommateur : consommateurs) 
		{	
			if (consommateur.isStockSuffisant(qte,indexDate)==false)
			{
				return false;
			}
		}
		return true;
	}
	
	

	public void computePrettyMessage(List<String> res, RdbLink em, SimpleDateFormat df,int indexDate, Date date) 
	{
		// On démarre avec la quantité disponible
		Qte qte = new Qte(qteDispo[indexDate]);
		
		// Pour chaque consommateur, on lui demande de consommer Other
		for (Consommateur consommateur : consommateurs) 
		{		
			consommateur.consommerOther(qte,indexDate);
		}
		
		// On verifie ensuite si ma consommation est possible 
		for (Consommateur consommateur : consommateurs) 
		{	
			consommateur.computePrettyMessage(res,em,df,qte,indexDate,date);	
		}
	}



	public Consommateur findConsommateur(Long idProduitCommande)
	{
		for (Consommateur consommateur : consommateurs)
		{
			if (consommateur.produitCdeDTO.idProduit.equals(idProduitCommande))
			{
				return consommateur;
			}
		}
		return null;
	}



	public void addDebugInfo(List<String> ls, int indexDate, RdbLink em) 
	{
		Produit p = em.find(Produit.class, idProduit);
		ls.add("Produit en stock : "+p.nom+","+p.conditionnement);
		ls.add("Qte dispo = "+qteDispo[indexDate]);
		ls.add("Consommé par");
		
		for (Consommateur consommateur : consommateurs) 
		{
			consommateur.addDebugInfo(ls,indexDate,em);
		}
		ls.add("");
		
	}



	
}
