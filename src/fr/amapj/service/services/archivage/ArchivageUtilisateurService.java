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

 package fr.amapj.service.services.archivage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import fr.amapj.common.CollectionUtils;
import fr.amapj.common.DateUtils;
import fr.amapj.common.FormatUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.models.contrat.reel.EtatPaiement;
import fr.amapj.model.models.contrat.reel.Paiement;
import fr.amapj.model.models.fichierbase.EtatUtilisateur;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.service.services.access.AccessManagementService;
import fr.amapj.service.services.archivage.tools.ArchivableState;
import fr.amapj.service.services.archivage.tools.ArchivableState.AStatus;
import fr.amapj.service.services.archivage.tools.SuppressionState;
import fr.amapj.service.services.archivage.tools.SuppressionState.SStatus;
import fr.amapj.service.services.gestioncotisation.GestionCotisationService;
import fr.amapj.service.services.gestioncotisation.PeriodeCotisationUtilisateurDTO;
import fr.amapj.service.services.parametres.ParametresArchivageDTO;
import fr.amapj.service.services.utilisateur.UtilisateurDTO;
import fr.amapj.service.services.utilisateur.UtilisateurService;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;

/**
 * Permet la gestion des utilisateurs archivés
 * 
 */
public class ArchivageUtilisateurService
{

	// VERIFICATION SUR L ARCHIVAGE D UN UTILISATEUR 
	
	
	public String computeArchivageLib(ParametresArchivageDTO param) 
	{
		String str = "Il est souhaitable d'archiver un utilisateur qui remplit les conditions suivantes : <ul>"+
				"<li>tous les paiements ont été finalisés</li>"+
			 	"<li>la date de dernière livraison est plus vieille que "+param.archivageUtilisateur+" jours</li>"+
			 	"<li>la date de fin de sa dernière adhésion est plus vieille que "+param.archivageUtilisateur+" jours</li>"+
				"<li>cet utilisateur n'a pas le rôle de PRODUCTEUR ou RÉFÉRENT (sur un producteur actif)</li>"+
				"<li>cet utilisateur n'a pas le rôle de TRÉSORIER ou ADMIN</li>"+
				"<li>cet utilisateur n'est pas inscrit sur une permanence dans le futur</li>"+
			 	"<li>la date de création de cet utilisateur est plus vieille que 60 jours</li>"+
			 	"</ul><br/>";
		
		return str;
	}
	
	/**
	 * Vérifie si cet utilisateur peut être archivé 
	 */
	@DbRead
	public ArchivableState computeArchivageState(Long idUtilisateur,ParametresArchivageDTO param)
	{
		RdbLink em = RdbLink.get();
		ArchivableState res = new ArchivableState();
		
		Utilisateur u = em.find(Utilisateur.class, idUtilisateur);

		
		// Non archivable : le utilisateur ne doit pas être à l'état CREATION ou ARCHIVE
		if (u.etatUtilisateur!=EtatUtilisateur.ACTIF)
		{
			res.nonArchivables.add("L'utilisateur est à l'état "+u.etatUtilisateur+", il doit être à l'état ACTIF pour pouvoir être archivé.");
		}
		
		// Réserve majeure : les paiements doivent être finalisés
		em.createQuery("select p from Paiement p WHERE p.contrat.utilisateur.id=:id AND p.etat!=:etat");
		em.setParameter("id", idUtilisateur);
		em.setParameter("etat", EtatPaiement.PRODUCTEUR);

		List<Paiement> paiements = em.result().list(Paiement.class);
		if (paiements.size() != 0)
		{
			String lib = paiements.stream().map(e->formatPaiement(e)).collect(Collectors.joining("<br/>"));
			res.reserveMajeures.add("Il existe  " + paiements.size() + " chèques de cet utilisateur qui n'ont pas été remis à l'AMAP ou au producteur : <br/>"+lib);
		}
		
		// Réserve mineure : la date de dernière livraison est plus vieille que param.archivageUtilisateur jours
		Date ref = DateUtils.getDateWithNoTime();
		ref = DateUtils.addDays(ref, -param.archivageUtilisateur);
		em.createQuery("select count(distinct(c.modeleContratDate)) from ContratCell c WHERE c.modeleContratDate.dateLiv>=:d AND c.contrat.utilisateur.id=:id");
		em.setParameter("d", ref);
		em.setParameter("id", idUtilisateur);

		int nbLiv = em.result().singleInt();
		if (nbLiv != 0)
		{
			res.reserveMineures.add("Il existe " + nbLiv	+ " livraisons de moins de "+param.archivageUtilisateur+" jours.");
		}
		
		
		// Réserve mineure : la date de fin de la dernière adhesion est plus vieille que param.archivageUtilisateur jours
		em.createQuery("select count(c) from PeriodeCotisationUtilisateur c WHERE c.periodeCotisation.dateFin>=:d AND c.utilisateur.id=:id");
		em.setParameter("d", ref);
		em.setParameter("id", idUtilisateur);

		if (em.result().singleInt() != 0)
		{
			res.reserveMineures.add("La date de fin de sa dernière adhésion est récente (moins de "+param.archivageUtilisateur+" jours).");
		}
		
		// Non archivable : le utilisateur ne doit pas avoir le role de PRODUCTEUR ou REFERENT ou TRESORIER ou ADMIN
		String str = new AccessManagementService().canBeArchive(idUtilisateur); 
		if (str!=null)
		{
			res.nonArchivables.add(str);
		}
		
		// Réserve majeure si l'utilisateur est inscrit à une date de permanence dans le futur 
		str = checkPermanenceFuture(em,idUtilisateur);
		if (str!=null)
		{
			res.reserveMajeures.add(str);
		}
		
		
		// Réserve majeure si l'utilisateur a été créé il y a moins de 90 jours
		str = checkDateCreation(em,u,90);
		if (str!=null)
		{
			res.reserveMajeures.add(str);
		}
		
		
		return res;
	}
	
	private String formatPaiement(Paiement e) 
	{
		SimpleDateFormat df = FormatUtils.getStdDate();
		return "Contrat :"+e.modeleContratDatePaiement.modeleContrat.nom+" Date :"+df.format(e.modeleContratDatePaiement.datePaiement)+" Montant : "
				+new CurrencyTextFieldConverter().convertToString(e.montant)+" € Etat du paiement="+e.etat;
	}

	// PARTIE REQUETAGE POUR AVOIR LA LISTE DES UTILISATEURS ACTIFS QU'IL EST SOUHAITABLE D'ARCHIVER
	
	/**
	 * Récupère la liste des utilisateurs archivables 
	 * 
	 */
	public List<UtilisateurDTO> getAllUtilisateursArchivables(ParametresArchivageDTO param)
	{
		List<UtilisateurDTO> alls = new UtilisateurService().getAllUtilisateurs(EtatUtilisateur.ACTIF);
		
		List<UtilisateurDTO> res = new ArrayList<>();
		for (UtilisateurDTO a : alls) 
		{
			ArchivableState state = computeArchivageState(a.id, param);
			
			if (state.getStatus()==AStatus.OUI_SANS_RESERVE)
			{
				res.add(a);
			}
		}
		
		res.sort(Comparator.comparing(e->e.nom+" "+e.prenom));
		
		return res;

	}
	
	

	
	// PARTIE REQUETAGE POUR AVOIR LA LISTE DES UTILISATEURS ARCHIVES QU'IL EST SOUHAITABLE DE SUPPRIMER
	
	
	
	public String computeSuppressionLib(ParametresArchivageDTO param)
	{
		String str = "Il est souhaitable de supprimer un utilisateur archivé qui remplit les conditions suivantes : <ul>"+
				 	"<li>tous les contrats de cet utilisateur ont été supprimés</li>"+
				 	"<li>cet utilisateur n'est pas inscrit sur une période de cotisation</li>"+
				 	"<li>cet utilisateur n'a pas le rôle de PRODUCTEUR ou RÉFÉRENT ou TRÉSORIER ou ADMIN</li>"+
				 	"<li>cet utilisateur n'est pas inscrit sur une permanence dans le futur</li>"+
				 	"<li>la date de création de cet utilisateur est plus vieille que 90 jours</li></ul><br/>"+
				 	"</ul><br/>";
				 	
		return str;
	}
	
	/**
	 * Vérifie si cet utilisateur peut être supprimé
	 */
	@DbRead
	public SuppressionState computeSuppressionState(Long idUtilisateur,ParametresArchivageDTO param)
	{
		SuppressionState res = new SuppressionState();
		RdbLink em = RdbLink.get();
		Utilisateur u = em.find(Utilisateur.class, idUtilisateur);
		
		// Non supprimable si l'etat n'est pas Archive
		if (u.etatUtilisateur!=EtatUtilisateur.INACTIF)
		{
			res.nonSupprimables.add("Cet utilisateur est à l'état "+u.etatUtilisateur);
		}
		
		// Non supprimable si il reste des contrats
		int nb = new UtilisateurService().countContrat(idUtilisateur);
		if (nb>0) 
		{	
			res.nonSupprimables.add("Il existe "+nb+" contrats pour cet utilisateur");
		}
		
		// Non supprimable si l'utilisateur est cotisant sur une période de cotisation
		List<PeriodeCotisationUtilisateurDTO> ps = new GestionCotisationService().getPeriodeCotisation(idUtilisateur);
		if (ps.size()>0)
		{
			res.nonSupprimables.add("Cet utilisateur est indiqué comme cotisant sur les périodes de cotisation suivantes :"+CollectionUtils.asStdString(ps, e->e.periodeNom));
		}
		
		
		// Non supprimable si l'utilisateur a le role de PRODUCTEUR ou REFERENT ou TRESORIER ou ADMIN
		String str = new AccessManagementService().canBeDeleted(idUtilisateur); 
		if (str!=null)
		{
			res.nonSupprimables.add(str);
		}
		
		
		// Réserve majeure si l'utilisateur est inscrit à une date de permanence dans le futur 
		str = checkPermanenceFuture(em,idUtilisateur);
		if (str!=null)
		{
			res.reserveMajeures.add(str);
		}
		
		
		// Réserve majeure si l'utilisateur a été créé il y a moins de 90 jours
		str = checkDateCreation(em,u,90);
		if (str!=null)
		{
			res.reserveMajeures.add(str);
		}
		
		return res;
	}
	
	


	private String checkPermanenceFuture(RdbLink em,Long idUtilisateur) 
	{
		em.createQuery("select max(d.periodePermanenceDate.datePerm) from PermanenceCell d WHERE d.periodePermanenceUtilisateur.utilisateur.id=:id");
		em.setParameter("id",idUtilisateur);
		Date dernierePermance = em.result().single(Date.class);
		if ( (dernierePermance!=null) && dernierePermance.after(DateUtils.getDateWithNoTime()))
		{
			return "Cet utilisateur est indiqué  de permanence le "+FormatUtils.getStdDate().format(dernierePermance);
		}
		return null;
	}
	
	private String checkDateCreation(RdbLink em, Utilisateur u,int delai) 
	{
		Date ref2 = DateUtils.getDateWithNoTime();
		ref2 = DateUtils.addDays(ref2, -delai);
		if (u.dateCreation.after(ref2))
		{
			return "La création de cet utilisateur est trop récente : "+FormatUtils.getStdDate().format(u.dateCreation);
		}
		return null;
	}

	/**
	 * Récupère la liste des utilisateurs archivés supprimables
	 */
	public List<UtilisateurDTO> getAllUtilisateurSupprimables(ParametresArchivageDTO param) 
	{
		List<UtilisateurDTO> ps = new UtilisateurService().getAllUtilisateurs(EtatUtilisateur.INACTIF);
		
		List<UtilisateurDTO> res = new ArrayList<UtilisateurDTO>();
		for (UtilisateurDTO p : ps) 
		{
			SuppressionState state = computeSuppressionState(p.id, param);
			if (state.getStatus()==SStatus.OUI_SANS_RESERVE)
			{
				res.add(p);
			}
		}
		res.sort(Comparator.comparing(e->e.dateCreation));
		
		return res;
	}



}

