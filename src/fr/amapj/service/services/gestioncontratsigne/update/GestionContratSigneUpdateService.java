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
 package fr.amapj.service.services.gestioncontratsigne.update;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import fr.amapj.common.DateUtils;
import fr.amapj.common.FormatUtils;
import fr.amapj.common.SQLUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.ModeleContratDate;
import fr.amapj.model.models.contrat.modele.ModeleContratProduit;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.model.models.fichierbase.Produit;
import fr.amapj.service.services.gestioncontrat.DateModeleContratDTO;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.gestioncontrat.ModeleContratDTO;
import fr.amapj.service.services.gestioncontratsigne.suivimodification.SuiviModificationContrat;
import fr.amapj.service.services.mescontrats.ContratLigDTO;
import fr.amapj.service.services.notification.DeleteNotificationService;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;

public class GestionContratSigneUpdateService
{
	@DbWrite
	public void addDates(ModeleContratDTO modeleContratDTO)
	{
		RdbLink em = RdbLink.get();
		
		ModeleContrat mc = em.find(ModeleContrat.class, modeleContratDTO.id);
		
		
		for (DateModeleContratDTO date : modeleContratDTO.dateLivs)
		{
			addOneDateLiv(em, date.dateLiv, mc);
		}
		
		markAllContratAsModified(mc,em);
		
		String details = "Liste des dates ajoutées : "+SuiviModificationContrat.listeDate(modeleContratDTO.dateLivs,e->e.dateLiv);
		SuiviModificationContrat.logModification("Ajouter des dates de livraison", mc, details);
	}
	
	
	public void addOneDateLiv(RdbLink em,Date datLiv,ModeleContrat mc)
	{
		ModeleContratDate md = new ModeleContratDate();
		md.modeleContrat = mc;
		md.dateLiv = datLiv;
		em.persist(md);
	}
	
	
	/**
	 * Cette méthode permet de supprimer une date de livraison d'un contrat 
	 * Cette date ne doit pas contenir de livraison pour un amapien 
	 *  
	 */
	@DbWrite
	public void suppressOneDateLiv(Long idModeleContratDate)
	{
		RdbLink em = RdbLink.get();
		
		suppressOneDateLiv(em,idModeleContratDate);
	}
	
	
	private void suppressOneDateLiv(RdbLink em,Long idModeleContratDate)
	{
		ModeleContratDate mcd = em.find(ModeleContratDate.class, idModeleContratDate);
		
		// On efface les exclusions relatives à cette date
		deleteAllDateBarrees(em, mcd);
				
		// On efface aussi toutes les notification relatives à cette date   
		new DeleteNotificationService().deleteAllNotificationDoneModeleContratDate(em, mcd);

		// On supprime la date 
		em.remove(mcd);
	}


	private void deleteAllDateBarrees(RdbLink em, ModeleContratDate mcd)
	{
		Query q = em.createQuery("select mce from ModeleContratExclude mce WHERE mce.date=:mcd");
		q.setParameter("mcd",mcd);
		
		SQLUtils.deleteAll(em, q);
	}


	/**
	 * Cette méthode permet de supprimer un produit  d'un contrat 
	 * Ce produit ne doit pas contenir de livraison pour un amapien 
	 *  
	 */
	@DbWrite
	public void suppressOneProduit(Long idModeleContratProduit)
	{
		RdbLink em = RdbLink.get();
		
		ModeleContratProduit mcp = em.find(ModeleContratProduit.class, idModeleContratProduit);
		
		// On efface les exclusions relatives à ce produit 
		deleteAllProduitBarres(em, mcp);
				
		// On supprime le produit  
		em.remove(mcp);
	}
	

	private void deleteAllProduitBarres(RdbLink em, ModeleContratProduit mcp)
	{
		Query q = em.createQuery("select mce from ModeleContratExclude mce WHERE mce.produit=:mcp");
		q.setParameter("mcp",mcp);
		
		SQLUtils.deleteAll(em, q);
	}


	public void addOneProduit(RdbLink em, Long produitId, Integer prix, int index, ModeleContrat mc)
	{	
		ModeleContratProduit mcp = new ModeleContratProduit();
		mcp.indx = index;
		mcp.modeleContrat = mc;
		mcp.prix = prix;
		mcp.produit = em.find(Produit.class, produitId);

		em.persist(mcp);
		
	}


	public void updateModeleContratProduit(RdbLink em, Long idModeleContratProduit, Integer prix, int index)
	{
		ModeleContratProduit mcp = em.find(ModeleContratProduit.class, idModeleContratProduit);

		mcp.indx = index;
		mcp.prix = prix;

	}

	/**
	 * Suppression d'une liste de date sur un contrat 
	 */
	@DbWrite
	public void suppressManyDateLivs(List<ContratLigDTO> dateToSuppress, Long idModeleContrat) throws OnSaveException
	{
		RdbLink em = RdbLink.get();
		ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);
		
		// On marque tous les contrats comme modifiés
		markAllContratAsModified(mc, em);
		
		// On supprime les dates
		for (ContratLigDTO contratLigDTO : dateToSuppress)
		{
			suppressOneDateLiv(em, contratLigDTO.modeleContratDateId);
		}
		
		// On vérifie à la fin qu'il reste au moins une date de livraison sur ce contrat
		if (new GestionContratService().getAllDates(em, mc).size()==0)
		{
			throw new OnSaveException("Vous ne pouvez pas supprimer toutes les dates d'un contrat");
		}
		
		String details = "Liste des dates supprimées : "+SuiviModificationContrat.listeDate(dateToSuppress,e->e.date);
		SuiviModificationContrat.logModification("Supprimer une ou plusieurs dates de livraison", mc, details);
				
				
	}
	
	
	// Outils techniques généraux 
	
	/**
	 * Réalise une mise à jour de la date de modification de tous les contrats de ce modele de contrat
	 */
	public void markAllContratAsModified(ModeleContrat mc, RdbLink em) 
	{
		Date now = DateUtils.getDate();
		em.createQuery("select c from Contrat c WHERE c.modeleContrat=:mc");
		em.setParameter("mc", mc);
		
		List<Contrat> cs = em.result().list(Contrat.class);
		for (Contrat contrat : cs) 
		{
			contrat.dateModification = now;
		}
	}
	
	
}
