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
 package fr.amapj.service.services.importdonnees;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import fr.amapj.common.DateUtils;
import fr.amapj.common.SQLUtils;
import fr.amapj.common.StringUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.fichierbase.Producteur;
import fr.amapj.model.models.fichierbase.ProducteurStockGestion;
import fr.amapj.model.models.fichierbase.Produit;
import fr.amapj.model.models.param.ChoixOuiNon;
import fr.amapj.service.engine.tools.DbToDto;
import fr.amapj.service.services.produit.ProduitService;
import fr.amapj.service.services.web.WebPageDTO;

/**
 * Service pour l'import des données
 */
public class ImportDonneesService
{
	/*
	 * Produits et producteurs
	 */
	
	@DbWrite
	public void insertDataProduits(List<ImportProduitProducteurDTO> prods)
	{
		RdbLink em = RdbLink.get();
		
		for (ImportProduitProducteurDTO importProduitProducteurDTO : prods)
		{
			insertDataProduits(em,importProduitProducteurDTO);
		}
	}

	private void insertDataProduits(RdbLink em, ImportProduitProducteurDTO dto)
	{
		TypedQuery<Producteur> q = em.createQuery("select p from Producteur p WHERE p.nom=:nom",Producteur.class);
		q.setParameter("nom",dto.producteur);
		
		Producteur p = SQLUtils.oneOrZero(q);
		if (p==null)
		{
			p = new Producteur();
			p.nom = dto.producteur;
			p.delaiModifContrat = 3;
			p.feuilleDistributionGrille = ChoixOuiNon.OUI;
			p.feuilleDistributionListe = ChoixOuiNon.NON;
			p.dateCreation = DateUtils.getDate();
			p.gestionStock = ProducteurStockGestion.NON;
			em.persist(p);
		}
		
		TypedQuery<Produit> q1 = em.createQuery("select p from Produit p WHERE p.producteur=:producteur AND p.nom=:nom AND p.conditionnement=:conditionnement ",Produit.class);
		q1.setParameter("producteur",p);
		q1.setParameter("nom",dto.produit);
		q1.setParameter("conditionnement",dto.conditionnement);
		
		Produit pr = SQLUtils.oneOrZero(q1);
		if (pr==null)
		{
			pr = new Produit();
			pr.conditionnement = dto.conditionnement;
			pr.nom = dto.produit;
			pr.producteur = p;
			em.persist(pr);
		}
		processDescription(pr.id,dto.description);
	}

	private void processDescription(Long idProduit, String description) 
	{
		// Si pas de description saisie : pas de mise à jour 
		if (description==null || description.trim().length()==0)
		{
			return;
		}
		
		//
		WebPageDTO dto = new WebPageDTO();
		dto.content = StringUtils.s(description);
		new ProduitService().saveWebPage(idProduit, dto);
	}

	@DbRead
	public List<ImportProduitProducteurDTO> getAllProduits()
	{
		RdbLink em = RdbLink.get();
		Query q = em.createQuery("select p from Produit p order by p.producteur.nom,p.nom,p.conditionnement");
		return DbToDto.transform(q, (Produit e)->createImportProduitProducteurDTO(e));
	}
	
	
	private ImportProduitProducteurDTO createImportProduitProducteurDTO(Produit prod) 
	{
		ImportProduitProducteurDTO dto = new ImportProduitProducteurDTO();
		dto.producteur = prod.producteur.nom;
		dto.produit = prod.nom;
		dto.conditionnement = prod.conditionnement;
		dto.idProduit = prod.id;
		return dto;
	}

	@DbRead
	public List<ImportProduitProducteurDTO> getAllProduits(Long idProducteur)
	{
		RdbLink em = RdbLink.get();
		Query q = em.createQuery("select p from Produit p where p.producteur.id=:id order by p.producteur.nom,p.nom,p.conditionnement");
		q.setParameter("id", idProducteur);
		return DbToDto.transform(q, (Produit e)->createImportProduitProducteurDTO(e));
	}
}
