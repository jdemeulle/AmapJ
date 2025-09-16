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
 package fr.amapj.service.services.gestioncontrat.datebarree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;

import fr.amapj.common.FormatUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.ModeleContratExclude;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.model.models.contrat.reel.ContratCell;
import fr.amapj.model.models.fichierbase.Produit;
import fr.amapj.service.services.gestioncontrat.GestionContratService;

public class DateBarreCheckService 
{
	
	/**
	 * Permet de verifier la cohérence des dates barrées sur un modele de contrat
	 * 
	 *  Retourne null si tout est ok, sinon retourne un message d'erreur 
	 */
	@DbRead
	public String checkCoherenceDateBarreesModeleContrat(Long modeleContratId)
	{
		RdbLink em = RdbLink.get();
		ModeleContrat mc = em.find(ModeleContrat.class, modeleContratId);
		
		// Récuperation de tous cellules du modele de contrat
		TypedQuery<ContratCell> q = em.createQuery("select cc from ContratCell cc where cc.contrat.modeleContrat=:mc",ContratCell.class);
		q.setParameter("mc", mc);
		List<ContratCell> cells = q.getResultList();
		
		return checkAllCell(cells,em,mc);
	}
	
	
	/**
	 * Permet de verifier la cohérence des dates barrées sur un contrat
	 * 
	 *  Retourne null si tout est ok, sinon retourne un message d'erreur 
	 */
	@DbRead
	public String checkCoherenceDateBarreesContrat(Long contratId)
	{
		RdbLink em = RdbLink.get();
		Contrat c = em.find(Contrat.class, contratId);
		
		// Récuperation de tous cellules du modele de contrat
		TypedQuery<ContratCell> q = em.createQuery("select cc from ContratCell cc where cc.contrat=:c",ContratCell.class);
		q.setParameter("c", c);
		List<ContratCell> cells = q.getResultList();
		
		return checkAllCell(cells,em,c.modeleContrat);
	}
	
	
	
	private String checkAllCell(List<ContratCell> cells, RdbLink em, ModeleContrat mc)
	{
			
		// Récuparéation de la liste des dates ou des produits qui sont exclus si il y en a 
		List<ModeleContratExclude> excludeds = new GestionContratService().getAllExcludedDateProduit(em, mc);

		// On positionne les informations dans un HashSet
		Set<String> exclude = new HashSet<String>();
		for (ModeleContratExclude modeleContratExclude : excludeds) 
		{
			String ref = modeleContratExclude.date.id+"_";
			if (modeleContratExclude.produit!=null)
			{
				ref = ref+modeleContratExclude.produit.id;
			}
			exclude.add(ref);
		}
		
	
		// Vérification cellule par cellule 
		StringBuilder sb = new StringBuilder();
		for (ContratCell cell : cells) 
		{
			String msg = checkOneCell(cell,exclude,em);
			if (msg!=null)
			{
				sb.append(msg);
				sb.append("\n");
			}
		}
		
		if (sb.length()==0)
		{
			return null;
		}
		else
		{
			return sb.toString();
		}
	}
	
	
	private String checkOneCell(ContratCell cell, Set<String> exclude, RdbLink em) 
	{
		// On vérifie la date
		String ref = cell.modeleContratDate.id+"_";
		if (exclude.contains(ref))
		{
			return checkOneCellError(cell,em);			
		}
		
		// On vérifie le couple date + produit 
		ref = ref+cell.modeleContratProduit.id;
		if (exclude.contains(ref))
		{
			return checkOneCellError(cell,em);
		}

		return null;
	}


	private String checkOneCellError(ContratCell cell, RdbLink em) 
	{
		Produit p = cell.modeleContratProduit.produit;
		String libProduit = p.nom+","+p.conditionnement;
		
		return "L'utilisateur "+cell.contrat.utilisateur.nom+" "+cell.contrat.utilisateur.prenom+" a commandé \n"+
				"Date ="+FormatUtils.getStdDate().format(cell.modeleContratDate.dateLiv)+"\n"+
				"Produit ="+libProduit+"\n"+
				"Qte="+cell.qte+"\n"+
				"Ceci est à corriger car ce produit est barré pour cette date";

	}

}
