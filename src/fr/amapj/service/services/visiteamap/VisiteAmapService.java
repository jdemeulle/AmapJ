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
 package fr.amapj.service.services.visiteamap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import fr.amapj.common.CollectionUtils;
import fr.amapj.common.collections.G1D;
import fr.amapj.common.collections.G1D.Cell1;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.models.contrat.modele.EtatModeleContrat;
import fr.amapj.model.models.contrat.modele.ModeleContratDate;
import fr.amapj.model.models.contrat.modele.ModeleContratProduit;
import fr.amapj.model.models.contrat.reel.ContratCell;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.service.services.mescontrats.small.SmallContratDTO;
import fr.amapj.service.services.mescontrats.small.SmallContratsService;
import fr.amapj.service.services.meslivraisons.MesLivraisonsService;
import fr.amapj.service.services.meslivraisons.QteProdDTO;
import fr.amapj.service.services.visiteamap.VisiteAmapDTO.Contrat;
import fr.amapj.service.services.visiteamap.VisiteAmapDTO.Jour;
import fr.amapj.service.services.visiteamap.VisiteAmapDTO.LigneContrat;
import fr.amapj.service.services.visiteamap.VisiteAmapDTO.Producteur;

public class VisiteAmapService 
{

	@DbRead
	public VisiteAmapDTO getAll(Date dateDebut, Date dateFin, Long idUtilisateur) 
	{
		RdbLink em = RdbLink.get();
		List<LigneContrat> ligneContrats = new ArrayList<>();
		
		// On recupère la liste des modeles contrats dates (en excluant les dates barrées)
		TypedQuery<ModeleContratDate> q = em.createQuery("select c from ModeleContratDate c WHERE c.dateLiv>=:deb AND c.dateLiv<=:fin AND c.modeleContrat.etat<>:etat "
				+ " AND c NOT IN (select mce.date from ModeleContratExclude mce where mce.produit is null)",ModeleContratDate.class);
		q.setParameter("deb", dateDebut, TemporalType.DATE);
		q.setParameter("fin", dateFin, TemporalType.DATE);
		q.setParameter("etat", EtatModeleContrat.CREATION);
		List<ModeleContratDate> mcds = q.getResultList();
		
		
		// Pour chaque date, on retrouve la liste des produits , en prenant en compte les produits barrées
		for (ModeleContratDate modeleContratDate : mcds) 
		{
			TypedQuery<ModeleContratProduit> q2 = em.createQuery("select c from ModeleContratProduit c WHERE c.modeleContrat=:mc AND "
					+ " c NOT IN (select mce.produit from ModeleContratExclude mce where mce.date = :mcd) "
					+ " ORDER BY c.indx",ModeleContratProduit.class);
			q2.setParameter("mc", modeleContratDate.modeleContrat);
			q2.setParameter("mcd", modeleContratDate);
			
			List<ModeleContratProduit> mcps = q2.getResultList();
			for (ModeleContratProduit modeleContratProduit : mcps) 
			{
				LigneContrat line = createLineDto(modeleContratProduit,modeleContratDate);
				ligneContrats.add(line);
			}
		}
		
		// On segmente ensuite toutes ces lignes en JOUR / PRODUCTEUR / CONTRAT
		VisiteAmapDTO res = new VisiteAmapDTO();
		res.jours = getAllJours(ligneContrats);
		
		// On complete chaque contrat avec les informations de cet utilisateur 
		if (idUtilisateur!=null)
		{
			List<SmallContratDTO> contrats = new SmallContratsService().getMesContrats(idUtilisateur);
			
			Utilisateur utilisateur = em.find(Utilisateur.class, idUtilisateur);
			List<ContratCell> mesLivraisons = new MesLivraisonsService().getAllQte(em, dateDebut, dateFin, utilisateur);
			
			CollectionUtils.forEach(res.jours,e->e.producteurs,e->e.contrats,e->completeContrat(em,e,contrats,mesLivraisons));
		}
		
		return res;
	}

	private List<Jour> getAllJours(List<LigneContrat> ligneContrats) 
	{
		// On regroupe tout d'abord les informations par jour de livraison
		return G1D.groupBy(ligneContrats,e->e.dateLiv).sortLigNatural(true).compute().getAs(e->createJour(e));
	}

	private Jour createJour(Cell1<Date, LigneContrat> cell1)
	{
		Jour jour = new Jour();
		jour.dateLiv = cell1.lig;
		jour.producteurs = getAllProducteurs(cell1.values);
		return jour;
	}

	private List<Producteur> getAllProducteurs(List<LigneContrat> ligneContrats) 
	{
		// On regroupe ensuite les informations par producteur 
		return G1D.groupBy(ligneContrats,e->e.producteurNom).sortLigNatural(true).compute().getAs(e->createProducteur(e));
	}
	
	
	private Producteur createProducteur(Cell1<String, LigneContrat> cell1)
	{
		Producteur producteur = new Producteur();
		producteur.producteurNom = cell1.lig;
		producteur.contrats = getAllContrats(cell1.values);
		return producteur;
	}

	private List<Contrat> getAllContrats(List<LigneContrat> ligneContrats) 
	{
		// On regroupe ensuite les informations par contrat 
		return G1D.groupBy(ligneContrats,e->e.modeleContratNom).sortLigNatural(true).compute().getAs(e->createContrat(e));
	}
	

	private Contrat createContrat(Cell1<String, LigneContrat> cell1)
	{
		Contrat contrat = new Contrat();
		
		contrat.contratNom = cell1.lig;
		contrat.modeleContratId = cell1.ref.modeleContratId;
		contrat.modeleContratDateId = cell1.ref.modeleContratDateId;
		
		contrat.ligneContrats = cell1.values;
		return contrat;
	}

	private LigneContrat createLineDto(ModeleContratProduit mcp,ModeleContratDate mcd) 
	{
		LigneContrat line = new LigneContrat();
		
		line.dateLiv = mcd.dateLiv;
		line.producteurNom = mcd.modeleContrat.producteur.nom;
		line.producteurId = mcd.modeleContrat.producteur.id;
		line.modeleContratNom = mcd.modeleContrat.nom;
		line.modeleContratId = mcd.modeleContrat.id;
		line.modeleContratDateId = mcd.id;
		line.produitPrix = mcp.prix;
		line.produitNom = mcp.produit.nom;
		line.produitConditionnement = mcp.produit.conditionnement;
		line.produitId = mcp.produit.id;
		line.produitIndx = mcp.indx;
		
		return line;
	}
	
	
	private void completeContrat(RdbLink em, Contrat contrat, List<SmallContratDTO> contrats, List<ContratCell> mesLivraisons)
	{
		contrat.contratDTO = contrats.stream().filter(e->e.modeleContratId.equals(contrat.modeleContratId)).findFirst().orElse(null);
		if (contrat.contratDTO!=null && contrat.contratDTO.contratId!=null)
		{
			contrat.qteProdDTOs = mesLivraisons.stream().filter(e->e.modeleContratDate.id.equals(contrat.modeleContratDateId)).map(e->createQteProdDTO(e)).collect(Collectors.toList());
		}	
	}
	
	private QteProdDTO createQteProdDTO(ContratCell cell)
	{
		QteProdDTO qteProdDTO = new QteProdDTO();
		qteProdDTO.conditionnementProduit = cell.modeleContratProduit.produit.conditionnement;
		qteProdDTO.nomProduit = cell.modeleContratProduit.produit.nom;
		qteProdDTO.idProduit = cell.modeleContratProduit.produit.getId();
		qteProdDTO.qte = cell.qte;
		
		return qteProdDTO;
	}

}
