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
 package fr.amapj.service.services.producteur;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import fr.amapj.common.CollectionUtils;
import fr.amapj.common.DateUtils;
import fr.amapj.common.FormatUtils;
import fr.amapj.common.LongUtils;
import fr.amapj.common.SQLUtils;
import fr.amapj.model.engine.IdentifiableUtil;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.contrat.modele.EtatModeleContrat;
import fr.amapj.model.models.contrat.modele.GestionDocEngagement;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.StockGestion;
import fr.amapj.model.models.editionspe.EditionSpecifique;
import fr.amapj.model.models.fichierbase.EtatNotification;
import fr.amapj.model.models.fichierbase.EtatProducteur;
import fr.amapj.model.models.fichierbase.Producteur;
import fr.amapj.model.models.fichierbase.ProducteurReferent;
import fr.amapj.model.models.fichierbase.ProducteurStockGestion;
import fr.amapj.model.models.fichierbase.ProducteurUtilisateur;
import fr.amapj.model.models.fichierbase.Produit;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.model.models.param.ChoixOuiNon;
import fr.amapj.model.models.produitextended.reglesconversion.RegleConversionProduit;
import fr.amapj.service.engine.tools.DbToDto;
import fr.amapj.service.services.archivage.tools.ArchivableState;
import fr.amapj.service.services.archivage.tools.ArchivableState.AStatus;
import fr.amapj.service.services.archivage.tools.SuppressionState;
import fr.amapj.service.services.archivage.tools.SuppressionState.SStatus;
import fr.amapj.service.services.docengagement.signonline.DocEngagementSignOnLineService;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.gestioncontrat.ModeleContratSummaryDTO;
import fr.amapj.service.services.parametres.ParametresArchivageDTO;
import fr.amapj.service.services.produitextended.ProduitExtendedService;
import fr.amapj.service.services.utilisateur.util.UtilisateurUtil;
import fr.amapj.view.engine.popup.suppressionpopup.UnableToSuppressException;

/**
 * Permet la gestion des producteurs
 * 
 */
public class ProducteurService
{
	
	
	// PARTIE REQUETAGE POUR AVOIR LA LISTE DES PRODUCTEURS
	
	/**
	 * Permet de charger la liste de tous les producteurs
	 */
	@DbRead
	public List<ProducteurDTO> getAllProducteurs(EtatProducteur etat)
	{
		RdbLink em = RdbLink.get();
		List<Producteur> ps = getAll(etat);
		return DbToDto.convert(ps, e->createProducteurDto(em,e));
	}

	
	/**
	 * Permet de charger un producteur
	 */
	@DbRead
	public ProducteurDTO loadProducteur(Long idProducteur)
	{
		RdbLink em = RdbLink.get();
		
		Producteur p = em.find(Producteur.class, idProducteur);
		ProducteurDTO dto = createProducteurDto(em,p);
		
		return dto;	
	}
	
	private ProducteurDTO createProducteurDto(RdbLink em, Producteur p)
	{
		ProducteurDTO dto = new ProducteurDTO();
		
		dto.id = p.id;
		dto.nom = p.nom;
		dto.description = p.description;
		dto.dateCreation = p.dateCreation;
		dto.dateModification = p.dateModification;
		dto.etat = p.etat;
		
		dto.feuilleDistributionGrille = p.feuilleDistributionGrille;
		dto.feuilleDistributionListe = p.feuilleDistributionListe;
		dto.feuilleDistributionEtiquette = p.etiquette==null ? ChoixOuiNon.NON : ChoixOuiNon.OUI;
		dto.idEtiquette = IdentifiableUtil.getId(p.etiquette);
		
		dto.libContrat = p.libContrat;
		
		dto.delaiModifContrat = p.delaiModifContrat;
		
		
		dto.referents = getReferents(em,p);
		dto.utilisateurs = getUtilisateur(em,p);
		
		dto.dateDerniereLivraison = findDerniereDateLivraison(em,p);
		dto.nbModeleContratActif = countModeleContratActif(em,p);
		
		dto.gestionStock = p.gestionStock;
		
		return dto;
	}
	
	private Date findDerniereDateLivraison(RdbLink em,Producteur p) 
	{
		TypedQuery<Date> q = em.createQuery("select max(c.dateLiv) from ModeleContratDate c WHERE c.modeleContrat.producteur=:p",Date.class);
		q.setParameter("p", p);
		return q.getSingleResult();
	}

	private int countModeleContratActif(RdbLink em,Producteur p)
	{
		Query q = em.createQuery("select count(c) from ModeleContrat c WHERE c.producteur=:p AND c.etat<>:etat");
		q.setParameter("p", p);
		q.setParameter("etat", EtatModeleContrat.ARCHIVE);
	
		return LongUtils.toInt(q.getSingleResult());
	}
	
	
	private int countModeleContrat(RdbLink em,Producteur p)
	{
		Query q = em.createQuery("select count(c) from ModeleContrat c WHERE c.producteur=:p");
		q.setParameter("p", p);
		return LongUtils.toInt(q.getSingleResult());
	}
	


	public List<ProdUtilisateurDTO> getReferents(RdbLink em, Producteur p)
	{
		List<ProdUtilisateurDTO> res = new ArrayList<>();
		
		List<ProducteurReferent> prs = getProducteurReferent(em, p);
		for (ProducteurReferent pr : prs)
		{
			ProdUtilisateurDTO dto = new ProdUtilisateurDTO();
			dto.idUtilisateur = pr.referent.id;
			dto.nom = pr.referent.nom;
			dto.prenom = pr.referent.prenom;
			dto.etatNotification = (pr.notification==EtatNotification.AVEC_NOTIFICATION_MAIL);
			res.add(dto);
		}
		return res;
	}

	public List<ProducteurReferent> getProducteurReferent(RdbLink em, Producteur p)
	{
		TypedQuery<ProducteurReferent> q = em.createQuery("select c from ProducteurReferent c WHERE c.producteur=:p order by c.indx",ProducteurReferent.class);
		q.setParameter("p", p);
		return q.getResultList();
	}
	
	

	public List<ProdUtilisateurDTO> getUtilisateur(RdbLink em, Producteur p)
	{
		List<ProdUtilisateurDTO> res = new ArrayList<>();
	
		List<ProducteurUtilisateur> pus =  getProducteurUtilisateur(em, p);
		for (ProducteurUtilisateur pu : pus)
		{
			ProdUtilisateurDTO dto = new ProdUtilisateurDTO();
			dto.idUtilisateur = pu.utilisateur.getId();
			dto.nom = pu.utilisateur.nom;
			dto.prenom = pu.utilisateur.prenom;
			dto.etatNotification = pu.notification==EtatNotification.AVEC_NOTIFICATION_MAIL;
			
			res.add(dto);
		}
		return res;
	}
	
	
	public List<ProducteurUtilisateur> getProducteurUtilisateur(RdbLink em, Producteur p)
	{
		TypedQuery<ProducteurUtilisateur> q = em.createQuery("select c from ProducteurUtilisateur c WHERE c.producteur=:p order by c.indx",ProducteurUtilisateur.class);
		q.setParameter("p", p);
		return q.getResultList();
	}
	
	/**
	 * Retourne la liste des emails du producteur
	 */
	@DbRead
	public String getEmailsProducteur(Long idProducteur)
	{
		RdbLink em = RdbLink.get();
		List<ProducteurUtilisateur> pus = getProducteurUtilisateur(em, em.find(Producteur.class, idProducteur));
		String res = "";
		for (ProducteurUtilisateur pu : pus) 
		{
			Utilisateur u = pu.utilisateur;
			if (UtilisateurUtil.canSendMailTo(u))
			{
				res = res+u.email+";";
			}
		}
		return res;
	}
	
	


	// PARTIE MISE A JOUR DES PRODUCTEURS
	@DbWrite
	public Long update(ProducteurDTO dto,boolean create)
	{
		RdbLink em = RdbLink.get();
		
		Producteur p;
		
		if (create)
		{
			p = new Producteur();
			p.dateCreation = DateUtils.getDate();
		}
		else
		{
			p = em.find(Producteur.class, dto.id);
			p.dateModification = DateUtils.getDate();
		}
		
		p.nom = dto.nom;
		p.description = dto.description;
		
		p.feuilleDistributionGrille = dto.feuilleDistributionGrille;
		p.feuilleDistributionListe = dto.feuilleDistributionListe;
		p.etiquette = IdentifiableUtil.findIdentifiableFromId(EditionSpecifique.class, dto.idEtiquette, em);
		
		p.libContrat = dto.libContrat;
		
		p.delaiModifContrat = dto.delaiModifContrat;
		
		p.gestionStock = dto.gestionStock;
		
		
		
		if (create)
		{
			em.persist(p);
		}
		
		// La liste des utilisateurs producteurs 
		updateUtilisateur(dto,em,p);
			
		
		// La liste des référents
		updateReferent(dto,em,p);
		
		// La gestion des stocks
		updateStockInfoModeleContratAndProduit(em,p);
		
		return p.id;
		
	}

	
	public void updateStockInfoModeleContratAndProduit(RdbLink em, Producteur p) 
	{
		if (p.gestionStock==ProducteurStockGestion.NON)
		{
			TypedQuery<ModeleContrat> q = em.createQuery("select mc from ModeleContrat mc WHERE mc.producteur=:p",ModeleContrat.class);
			q.setParameter("p", p);
			for (ModeleContrat mc : q.getResultList()) 
			{
				mc.stockGestion= StockGestion.NON;
			}	
			
			RegleConversionProduit regles = new ProduitExtendedService().loadRegleConversionProduit(em, p);
			regles.regleStocks.clear();
			new ProduitExtendedService().saveRegleConversionProduit(regles, p, em);
		}
	}


	private void updateUtilisateur(ProducteurDTO dto, RdbLink em, Producteur p)
	{
		// Suppression de tous les référents
		Query q = em.createQuery("select c from ProducteurUtilisateur c WHERE c.producteur=:p");
		q.setParameter("p", p);
		SQLUtils.deleteAll(em, q);
				
		// On recree les nouveaux
		int indx = 0;
		for (ProdUtilisateurDTO util : dto.utilisateurs)
		{
			ProducteurUtilisateur pr = new ProducteurUtilisateur();
			pr.producteur = p;
			pr.utilisateur = em.find(Utilisateur.class, util.idUtilisateur);
			pr.indx = indx;
			if (util.etatNotification==true)
			{
				pr.notification = EtatNotification.AVEC_NOTIFICATION_MAIL;
			}
			else
			{
				pr.notification = EtatNotification.SANS_NOTIFICATION_MAIL;
			}
			
			em.persist(pr);
			indx++;
		}	
	}

	
	private void updateReferent(ProducteurDTO dto, RdbLink em, Producteur p)
	{
		// Suppression de tous les référents
		Query q = em.createQuery("select c from ProducteurReferent c WHERE c.producteur=:p");
		q.setParameter("p", p);
		SQLUtils.deleteAll(em, q);
		
		// On recree les nouveaux
		int indx = 0;
		for (ProdUtilisateurDTO referent : dto.referents)
		{
			ProducteurReferent pr = new ProducteurReferent();
			pr.producteur = p;
			pr.referent = em.find(Utilisateur.class, referent.idUtilisateur);
			pr.indx = indx;
			if (referent.etatNotification==true)
			{
				pr.notification = EtatNotification.AVEC_NOTIFICATION_MAIL;
			}
			else
			{
				pr.notification = EtatNotification.SANS_NOTIFICATION_MAIL;
			}
			
			em.persist(pr);
			indx++;
		}	
	}


	// PARTIE SUPPRESSION

	/**
	 * Permet de supprimer un producteur 
	 */
	@DbWrite
	public void delete(Long id)
	{
		RdbLink em = RdbLink.get();
		
		Producteur p = em.find(Producteur.class, id);

		int r = countModeleContrat(em,p);
		if (r>0)
		{
			throw new UnableToSuppressException("Cet producteur posséde "+r+" modeles de contrats.");
		}
		
		r = countProduit(p,em);
		if (r>0)
		{
			throw new UnableToSuppressException("Cet producteur posséde "+r+" produits. Vous devez d'abord les supprimer.");
		}
		
		// Il faut d'abord supprimer les referents et les utilisateurs producteurs 
		Query q = em.createQuery("select c from ProducteurReferent c WHERE c.producteur=:p",ProducteurReferent.class);
		q.setParameter("p", p);
		SQLUtils.deleteAll(em, q);
		
		
		
		q = em.createQuery("select c from ProducteurUtilisateur c WHERE c.producteur=:p");
		q.setParameter("p", p);
		SQLUtils.deleteAll(em, q);
		
		// On supprime ensuite les quantités de stocks associées à ce producteur 
		q = em.createQuery("select c from ProduitExtendedParam c WHERE c.producteur=:p");
		q.setParameter("p", p);
		SQLUtils.deleteAll(em, q);
		
		
		// Puis on supprime le producteur
		em.remove(p);
	}
	
	private int countProduit(Producteur p, RdbLink em)
	{
		Query q = em.createQuery("select count(c) from Produit c WHERE c.producteur=:p");
		q.setParameter("p", p);
			
		return LongUtils.toInt(q.getSingleResult());
	}
	

	/**
	 * Permet de charger la liste de tous les modeles de contrats de ce producteur
	 * On affiche tous les contrats, y compris les archivés 
	 */
	@DbRead
	public List<ModeleContratSummaryDTO> getModeleContratInfo(Long idProducteur)
	{
		RdbLink em = RdbLink.get();
		
		Query q = em.createQuery("select mc from ModeleContrat mc WHERE mc.producteur.id=:id");
		q.setParameter("id",idProducteur);
		
		return DbToDto.transform(q, (ModeleContrat mc)->createModeleContratInfo(em, mc));
	}
	
	private ModeleContratSummaryDTO createModeleContratInfo(RdbLink em, ModeleContrat mc)
	{
		GestionContratService service = new GestionContratService();
		ModeleContratSummaryDTO res = service.createModeleContratInfo(em, mc);
		res.nbContratASignerProducteur = new DocEngagementSignOnLineService().getAllDocumentsASignerByProducteur(mc.id).size();
		return res;
	}
	
	
	// Partie Notification
	
	
	/**
	 * Retourne le délai de notification
	 */
	@DbRead
	public int getDelaiNotification(Long idProducteur)
	{
		RdbLink em = RdbLink.get();
		Producteur producteur = em.find(Producteur.class, idProducteur);
		return producteur.delaiModifContrat;
	}
	

	/**
	 * Retourne true si ce producteur demande à être notifié 
	 */
	public boolean needNotification(Producteur producteur, RdbLink em)
	{
		// On compte le nombre d'utilisateurs producteurs voulant être notifiés 
		Query q = em.createQuery("select count(c) from ProducteurUtilisateur c WHERE c.producteur=:p and c.notification=:etat");
		q.setParameter("p", producteur);
		q.setParameter("etat", EtatNotification.AVEC_NOTIFICATION_MAIL);
		long nbUtilisateurs = SQLUtils.count(q);
		
		// On compte le nombre de referent voulant être notifiés 
		q = em.createQuery("select count(c) from ProducteurReferent c WHERE c.producteur=:p and c.notification=:etat");
		q.setParameter("p", producteur);
		q.setParameter("etat", EtatNotification.AVEC_NOTIFICATION_MAIL);
		long nbReferents = SQLUtils.count(q);
		
		
		return (nbUtilisateurs+nbReferents)>0;
	}

	
	
	// Gestion de l'état du producteur 
	@DbWrite
	public void updateEtat(Long idProducteur, EtatProducteur etat) 
	{
		RdbLink em = RdbLink.get();
		Producteur producteur = em.find(Producteur.class, idProducteur);
		producteur.etat = etat;
	}
	
	
	// Gestion du searcher 
	@DbRead
	public List<Producteur> getAll(EtatProducteur etat) 
	{
		RdbLink em = RdbLink.get();
		return getAll(em,etat);
	}

	private List<Producteur> getAll(RdbLink em, EtatProducteur etat) 
	{
		TypedQuery<Producteur> q = em.createQuery("select p from Producteur p where p.etat=:etat",Producteur.class);
		q.setParameter("etat", etat);
		return q.getResultList();
	}
	
	
	@DbWrite
	public void deleteWithProduit(Long idProducteur) 
	{
		RdbLink em = RdbLink.get();
		
		// On supprime tous les produits de ce producteur
		TypedQuery<Produit> q = em.createQuery("select p from Produit p  where p.producteur.id=:prod",Produit.class);
		q.setParameter("prod", idProducteur);		
		for (Produit p : q.getResultList())
		{
			em.remove(p);
		}	
		
		// On supprime le producteur 
		delete(idProducteur);
		
	}
	
		
	// PARTIE REQUETAGE POUR AVOIR LA LISTE DES PRODUCTEURS ACTIFS QU'IL EST SOUHAITABLE D'ARCHIVER
	
	
	public String computeArchivageLib(ParametresArchivageDTO param)
	{
		String str = "Il est souhaitable d'archiver un producteur qui remplit les conditions suivantes : <ul>"+
				 	"<li>tous les contrats de ce producteur ont été archivés</li>"+
				 	"<li>la date de dernière livraison est plus vieille que "+param.archivageProducteur+" jours</li>"+
				 	"<li>la date de création du producteur est plus vieille que 90 jours</li></ul><br/>";
				 	
		return str;
	}
	
	/**
	 * Vérifie si ce producteur peut être archivé 
	 */
	public ArchivableState computeArchivageState(ProducteurDTO p,ParametresArchivageDTO param)
	{
		ArchivableState res = new ArchivableState();
		
		// Non archivable si le producteur est déjà archivé  
		if (p.etat==EtatProducteur.ARCHIVE)
		{
			res.nonArchivables.add("Le producteur est déjà archivé");
		}
		
		// Non archivable : le producteur ne doit pas avoir de contrat à l'état actif
		if (p.nbModeleContratActif!=0)
		{
			res.nonArchivables.add("Le producteur posséde "+p.nbModeleContratActif+" contrats vierges à l'état CREATION ou ACTIF");
		}
		
		// Mineure : la dernière date de livraison doit être dépassée d'au moins param.archivageProducteur jour 
		Date ref = DateUtils.getDateWithNoTime();
		ref = DateUtils.addDays(ref, -param.archivageProducteur);
		if (p.dateDerniereLivraison!=null && p.dateDerniereLivraison.after(ref))
		{
			res.reserveMineures.add("La dernière livraison est assez récente : "+FormatUtils.getStdDate().format(p.dateDerniereLivraison));
		}
		
		// Mineure : pour être archivable, le producteur doit être créé depuis au moins de 3 mois
		Date ref2 = DateUtils.getDateWithNoTime();
		ref2 = DateUtils.addDays(ref2, -90);
		if (p.dateCreation.after(ref2))
		{
			res.reserveMineures.add("La création de ce producteur est assez récente : "+FormatUtils.getStdDate().format(p.dateCreation));
		}
		
		return res;
	}
	
	
	/**
	 * Récupère la liste des producteurs archivables 
	 */
	public List<ProducteurDTO> getAllProducteursArchivables(ParametresArchivageDTO param)
	{
		List<ProducteurDTO> ps = getAllProducteurs(EtatProducteur.ACTIF);	
		
		List<ProducteurDTO> res = new ArrayList<ProducteurDTO>();
		for (ProducteurDTO p : ps) 
		{
			ArchivableState state = computeArchivageState(p, param);
			if (state.getStatus()==AStatus.OUI_SANS_RESERVE)
			{
				res.add(p);
			}
		}
		
		CollectionUtils.sort(res, e->e.dateDerniereLivraison);
		
		return res;

	}

	

	// PARTIE REQUETAGE POUR AVOIR LA LISTE DES PRODUCTEURS ARCHIVES QU'IL EST SOUHAITABLE DE SUPPRIMER
	
	
	
	public String computeSuppressionLib(ParametresArchivageDTO param)
	{
		String str = "Il est souhaitable de supprimer un producteur qui remplit les conditions suivantes : <ul>"+
				 	"<li>tous les contrats de ce producteur ont été supprimés</li>"+
				 	"<li>la date de création du producteur est plus vieille que 90 jours</li></ul><br/>";
				 	
		return str;
	}
	
	/**
	 * Vérifie si ce producteur peut être supprimé 
	 */
	@DbRead
	public SuppressionState computeSuppressionState(ProducteurDTO dto,ParametresArchivageDTO param)
	{
		SuppressionState res = new SuppressionState();
		
		RdbLink em = RdbLink.get();
		Producteur p = em.find(Producteur.class, dto.id);
		
		// Non supprimable si le producteur n'est pas à l'état archivé  
		if (p.etat!=EtatProducteur.ARCHIVE)
		{
			res.nonSupprimables.add("Le producteur n'est pas à l'état Archivé");
		}

		// Non supprimable si il y a encore des modeles de contrats
		int r = countModeleContrat(em, p);
		if (r>0)
		{
			res.nonSupprimables.add("Cet producteur posséde "+r+" modeles de contrats");
		}
				
		// Majeure : pour être supprimable, le producteur doit être créé depuis au moins 90 jours
		Date ref2 = DateUtils.getDateWithNoTime();
		ref2 = DateUtils.addDays(ref2, -90);
		if (p.dateCreation.after(ref2))
		{
			res.reserveMajeures.add("La création de ce producteur est trop récente : "+FormatUtils.getStdDate().format(p.dateCreation));
		}
		
		return res;
	}
	
	
	/**
	 * Récupère la liste des producteurs supprimables
	 */
	public List<ProducteurDTO> getAllProducteurSupprimables(ParametresArchivageDTO param) 
	{
		List<ProducteurDTO> ps = getAllProducteurs(EtatProducteur.ARCHIVE);
		
		List<ProducteurDTO> res = new ArrayList<ProducteurDTO>();
		for (ProducteurDTO p : ps) 
		{
			SuppressionState state = computeSuppressionState(p, param);
			if (state.getStatus()==SStatus.OUI_SANS_RESERVE)
			{
				res.add(p);
			}
		}
		res.sort(Comparator.comparing(e->e.dateCreation));
		
		return res;
	}
}
