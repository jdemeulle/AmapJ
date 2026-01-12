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

 package fr.amapj.service.services.produit;

import java.util.List;
import java.util.stream.Collectors;

import fr.amapj.common.CollectionUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.fichierbase.Producteur;
import fr.amapj.model.models.fichierbase.ProducteurStockGestion;
import fr.amapj.model.models.fichierbase.Produit;
import fr.amapj.model.models.produitextended.reglesconversion.ProduitLimiteQuantite;
import fr.amapj.model.models.produitextended.reglesconversion.RegleConversionProduit;
import fr.amapj.model.models.produitextended.reglesconversion.RegleConversionStock;
import fr.amapj.service.services.produitextended.ProduitExtendedService;
import fr.amapj.service.services.web.WebPageDTO;
import fr.amapj.service.services.web.WebPageService;
import fr.amapj.view.engine.popup.suppressionpopup.UnableToSuppressException;

/**
 * Permet la gestion des producteurs
 * 
 */
public class ProduitService
{
	
	
	// PARTIE REQUETAGE POUR AVOIR LA LISTE DES PRODUITS
	
	
	/**
	 * Permet de charger la liste de tous les produits
	 * dans une transaction en lecture
	 */
	@DbRead
	public List<ProduitDTO> getAllProduitDTO(Long idProducteur)
	{
		RdbLink em = RdbLink.get();
//		em.createQuery("select p from Produit p where p.producteur.id=:prd order by p.nom,p.conditionnement");
		em.createQuery("select p from Produit p where p.producteur.id=:prd");
		em.setParameter("prd", idProducteur);
		return em.result().listConverted(Produit.class,e->createProduitDto(e, em,false));
		
	}
	
	
	@DbRead
	public ProduitDTO loadProduit(Long idProduit) 
	{
		RdbLink em = RdbLink.get();
		Produit p = em.find(Produit.class, idProduit);
		return createProduitDto(p, em,true);
	}
	
	/**
	 * Permet de charger un produit
	 */
	private  ProduitDTO createProduitDto(Produit p,RdbLink em,boolean full)
	{			
		ProduitDTO dto = new ProduitDTO();
		
		dto.id = p.getId();
		dto.nom = p.nom;
		dto.conditionnement = p.conditionnement;
		dto.producteurId = p.producteur.getId();
		
		if (full)
		{
			// Chargement des informations de stocks
			if (p.producteur.gestionStock==ProducteurStockGestion.OUI)
			{
				RegleConversionStock r = new ProduitExtendedService().loadRegleConversionProduit(em,p.producteur).findReglesConversionStock(p.id);
				dto.limiteQuantite = r.limiteQuantite;
				dto.coefficient = r.coefficient;
				dto.idProduitEnStock = r.idProduitEnStock;
			}
		}
		
		return dto;
		
	}

	
	

	/**
	 * Mise à jour ou création d'un produit
	 * @param dto
	 * @param create
	 */
	@DbWrite
	public Long update(ProduitDTO dto,boolean create)
	{
		RdbLink em = RdbLink.get();
		
		Produit p;
		
		if (create)
		{
			p = new Produit();
		}
		else
		{
			p = em.find(Produit.class, dto.id);
		}
		
		p.nom = dto.nom;
		p.conditionnement = dto.conditionnement;
		p.producteur = em.find(Producteur.class, dto.producteurId);
		
		if (create)
		{
			em.persist(p);
		}
		
		// Mise à jour des informations stocks
		new ProduitExtendedService().saveRegleConversionOneProduit(p, dto, em);
		 
		
		return p.getId();
	}



	/**
	 * Permet la suppression d'un produit
	 */
	@DbWrite
	public void deleteProduit(Long idItemToSuppress) throws UnableToSuppressException
	{
		RdbLink em = RdbLink.get();
		Produit p = em.find(Produit.class, idItemToSuppress);
		
		// 
		verifContrat(p,em);
		
		//
		verifProduitExtendedParam(p,em);
		
		//
		if (p.webPage!=null)
		{
			new WebPageService().delete(em,p.webPage.id);
		}
		
		em.remove(p);
	}
	
	/**
	 * On verifie les régles de stock et on supprime les regles inutiles
	 */
	private void verifProduitExtendedParam(Produit p, RdbLink em) 
	{
		// Chargement des regles de stock 
		RegleConversionProduit regles = new ProduitExtendedService().loadRegleConversionProduit(em, p.producteur);
		
		// Verification si il y a une régle qui pointe sur le produit que l'on veut supprimer 
		for (RegleConversionStock r : regles.regleStocks) 
		{
			if (r.limiteQuantite==ProduitLimiteQuantite.SUIVI_AVEC_REGLE_CALCUL && r.idProduitEnStock!=null && r.idProduitEnStock.equals(p.id))
			{
				Produit plie = em.find(Produit.class, r.idProduit);
				throw new UnableToSuppressException("Cet produit est présent dans une régle de calcul pour le stock. Produit lié : "+plie.nom+", "+plie.conditionnement);
			}
		}
		
		// Suppression des régles devenues inutiles
		List<RegleConversionStock> toRemove = regles.regleStocks.stream().filter(e->e.idProduit.equals(p.id)).collect(Collectors.toList());
		regles.regleStocks.removeAll(toRemove);
		new ProduitExtendedService().saveRegleConversionProduit(regles, p.producteur, em);
	}


	private void verifContrat(Produit p, RdbLink em) throws UnableToSuppressException
	{
		em.createQuery("select distinct(c.modeleContrat) from ModeleContratProduit c WHERE c.produit=:p");
		em.setParameter("p", p);
			
		List<ModeleContrat> mcs = em.result().list(ModeleContrat.class);

		if (mcs.size()>0)
		{
			String str = CollectionUtils.asStdString(mcs, t -> t.nom);
			throw new UnableToSuppressException("Cet produit est présent dans "+mcs.size()+" contrats : "+str);
		}
	}
	
	/**
	 *
	 */
	@DbRead
	public String prettyString(Long idProduit)
	{
		RdbLink em = RdbLink.get();
		
		if (idProduit==null)
		{
			return "";
		}
		
		Produit p = em.find(Produit.class, idProduit);
		return p.nom+","+p.conditionnement;
		
	}
	
	
	
	@DbRead
	public WebPageDTO loadWebPage(Long idProduit)
	{
		RdbLink em = RdbLink.get();
		Produit p = em.find(Produit.class, idProduit);
		return new WebPageService().loadWebPage(em,p.webPage);
	}
	
	
	@DbWrite
	public void saveWebPage(Long idProduit,WebPageDTO dto)
	{
		RdbLink em = RdbLink.get();
		Produit p = em.find(Produit.class, idProduit);
		new WebPageService().saveWebPage(em,dto,e->p.webPage=e);
		
	}

	
	
}
