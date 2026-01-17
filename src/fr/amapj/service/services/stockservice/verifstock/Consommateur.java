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
import java.util.Date;
import java.util.List;

import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.fichierbase.Produit;

/**
 * 
 */
public class Consommateur
{
	public double coefficient;
	
	// Le produit commandé correspondant  
	public ProduitCdeDTO produitCdeDTO;
	
	
	public void consommerOther(Qte qte,int indexDate) 
	{
		// On décremente la quantité des autres 
		qte.decr(produitCdeDTO.qteOther[indexDate]*coefficient);		
	}
	
	
	public void consommerMe(Qte qte,int indexDate) 
	{	
		// On décremente la quantité de me
		qte.decr(produitCdeDTO.qteMe[indexDate]*coefficient);
	}
	
	
	
	
	public boolean isStockSuffisant(Qte qte,int indexDate) 
	{
		int qteMe = produitCdeDTO.qteMe[indexDate];
		
		// Si je ne consomme pas de ce produit, c'est toujours bon 
		if (qteMe==0)
		{
			return true;
		}
		
		// Je decremente le stock de ce que je consomme 
		qte.decr(qteMe*coefficient);
		
		if(qte.isNegative())
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	public void computePrettyMessage(List<String> res, RdbLink em, SimpleDateFormat df, Qte qte, int indexDate, Date date) 
	{
		int qteMe = produitCdeDTO.qteMe[indexDate];
		
		// Si je ne consomme pas de ce produit, c'est toujours bon 
		if (qteMe==0)
		{
			return;
		}
		
		// On memorise le restant avant ma consommation 
		int restant = qte.getRestant(coefficient);
		
		// Je decremente le stock de ce que je consomme 
		qte.decr(qteMe*coefficient);
		
		if(qte.isNegative())
		{
			Produit p = em.find(Produit.class, produitCdeDTO.idProduit);
			res.add("Date : "+df.format(date)+" Produit= "+p.nom+", "+p.conditionnement);
			res.add("La quantité maximum autorisée est "+restant);
			res.add("");
		}
	}


	public void addDebugInfo(List<String> ls, int indexDate, RdbLink em) 
	{
		Produit p = em.find(Produit.class, produitCdeDTO.idProduit);
		ls.add("	Produit Commandé = "+p.nom+","+p.conditionnement);
		ls.add("	Coefficient = "+coefficient);
		ls.add("	Qte Commandée = "+produitCdeDTO.qteOther[indexDate]);
		ls.add("");
		
	}
}
