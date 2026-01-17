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
 package fr.amapj.service.services.gestioncontrat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.CollectionUtils;
import fr.amapj.common.DateUtils;
import fr.amapj.common.SQLUtils;
import fr.amapj.model.engine.IdentifiableUtil;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.contrat.modele.EtatModeleContrat;
import fr.amapj.model.models.contrat.modele.GestionPaiement;
import fr.amapj.model.models.contrat.modele.GestionDocEngagement;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.ModeleContratDate;
import fr.amapj.model.models.contrat.modele.ModeleContratDatePaiement;
import fr.amapj.model.models.contrat.modele.ModeleContratExclude;
import fr.amapj.model.models.contrat.modele.ModeleContratProduit;
import fr.amapj.model.models.contrat.reel.ContratCell;
import fr.amapj.model.models.cotisation.PeriodeCotisation;
import fr.amapj.model.models.editionspe.EditionSpecifique;
import fr.amapj.model.models.fichierbase.Producteur;
import fr.amapj.model.models.fichierbase.Produit;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.service.engine.tools.DbToDto;
import fr.amapj.service.engine.tools.DtoToDb;
import fr.amapj.service.engine.tools.DtoToDb.ElementToAdd;
import fr.amapj.service.engine.tools.DtoToDb.ElementToUpdate;
import fr.amapj.service.engine.tools.DtoToDb.ListDiff;
import fr.amapj.service.services.archivage.ArchivageContratService;
import fr.amapj.service.services.archivage.tools.ArchivableState;
import fr.amapj.service.services.archivage.tools.ArchivableState.AStatus;
import fr.amapj.service.services.docengagement.DocEngagementGeneralService;
import fr.amapj.service.services.gestioncontrat.datebarree.DateBarreCheckService;
import fr.amapj.service.services.gestioncontratsigne.update.GestionContratSigneUpdateService;
import fr.amapj.service.services.mescontrats.ContratColDTO;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.mescontrats.ContratLigDTO;
import fr.amapj.service.services.mespaiements.PaiementService;
import fr.amapj.service.services.notification.DeleteNotificationService;
import fr.amapj.service.services.parametres.ParametresArchivageDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.suppressionpopup.UnableToSuppressException;
import fr.amapj.view.views.gestioncontrat.editorpart.FrequenceLivraison;

/**
 * Permet la gestion des modeles de contrat
 * 
 *  
 * 
 */
public class GestionContratService
{
	
	// PARTIE REQUETAGE POUR AVOIR LA LISTE DES CONTRATS

	/**
	 * Permet de charger la liste de tous les modeles de contrats à l'état indiqué
	 */
	@DbRead
	public List<ModeleContratSummaryDTO> getModeleContratInfo(EtatModeleContrat... etats)
	{
		RdbLink em = RdbLink.get();
		
		Query q = em.createQuery("select mc from ModeleContrat mc WHERE mc.etat IN :etats");
		q.setParameter("etats",Arrays.asList(etats));
		
		return DbToDto.transform(q, (ModeleContrat mc)->createModeleContratInfo(em, mc));
	}

	public ModeleContratSummaryDTO createModeleContratInfo(RdbLink em, ModeleContrat mc)
	{
		ModeleContratSummaryDTO info = new ModeleContratSummaryDTO();

		
		info.id = mc.getId();
		info.nom = mc.nom;
		info.nomProducteur = mc.producteur.nom;
		info.producteurId = mc.producteur.getId();
		info.finInscription = mc.dateFinInscription;
		info.etat = mc.etat;
		info.periodeCotisationId = IdentifiableUtil.getId(mc.periodeCotisation);
		info.gestionDocEngagement = mc.gestionDocEngagement;

		// Avec une sous requete, on obtient la liste de toutes les dates de
		// livraison
		List<ModeleContratDate> dates = getAllDates(em, mc);

		info.nbLivraison = dates.size()-getNbDateAnnulees(em,mc);
		
		info.nbInscrits = getNbInscrits(em, mc);

		if (dates.size() >= 1)
		{
			info.dateDebut = dates.get(0).dateLiv;
			info.dateFin = dates.get(dates.size() - 1).dateLiv;
		}

		info.nbProduit = getNbProduit(em, mc);

		return info;
	}


	static public class DateInfo
	{
		public Date dateDebut;
		public Date dateFin;
		public int nbDateLivs;
	}
	
	public DateInfo getDateDebutFin(RdbLink em, ModeleContrat mc)
	{
		DateInfo di = new DateInfo();
		
		Query q = em.createQuery("select min(c.dateLiv),max(c.dateLiv),count(c.dateLiv) from ModeleContratDate c WHERE c.modeleContrat=:mc");
		q.setParameter("mc",mc);
		
		Object[] res = (Object[]) q.getSingleResult();
		
		di.dateDebut = (Date) res[0];
		di.dateFin = (Date) res[1];
		di.nbDateLivs = SQLUtils.toInt(res[2]);
		
		return di;
	}
			
	
	
	/**
	 * Retourne le nombre d'adherent ayant souscrit à ce modele de contrat
	 * 
	 * @return
	 */
	@DbRead
	public int getNbInscrits(Long mcId)
	{
		RdbLink em = RdbLink.get();
		ModeleContrat mc = em.find(ModeleContrat.class, mcId);
		return getNbInscrits(em, mc);
	}
	

	/**
	 * Retourne le nombre d'adherent ayant souscrit à ce modele de contrat
	 * @param em
	 * @param mc
	 * @return
	 */
	private int getNbInscrits(RdbLink em, ModeleContrat mc)
	{
		Query q = em.createQuery("select count(c.id) from Contrat c WHERE c.modeleContrat=:mc");
		q.setParameter("mc",mc);
		
		return ((Long) q.getSingleResult()).intValue();
	}

	/**
	 * Retourne le nombre de dates annulées pour un modele de contrat
	 */
	public int getNbDateAnnulees(RdbLink em, ModeleContrat mc)
	{
		Query q = em.createQuery("select count(p.id) from ModeleContratExclude p WHERE p.modeleContrat=:mc and p.produit is null");
		q.setParameter("mc",mc);
		
		return ((Long) q.getSingleResult()).intValue();
	}

	private int getNbProduit(RdbLink em, ModeleContrat mc)
	{
		return getAllProduit(em, mc).size();
	}

	/**
	 * Retrouve la liste des produits, triés suivant la valeur indx
	 *
	 */
	public List<ModeleContratProduit> getAllProduit(RdbLink em, ModeleContrat mc)
	{
		Query q = em.createQuery("select mcp from ModeleContratProduit mcp where mcp.modeleContrat=:mc ORDER BY mcp.indx"); 
		q.setParameter("mc", mc);
		
		List<ModeleContratProduit> prods = q.getResultList();
		return prods;
	}

	public List<ModeleContratDate> getAllDates(RdbLink em, ModeleContrat mc)
	{
		Query q = em.createQuery("select mcd from ModeleContratDate mcd where mcd.modeleContrat=:mc ORDER BY mcd.dateLiv,mcd.id"); 
		q.setParameter("mc", mc);
		
		List<ModeleContratDate> dates = q.getResultList();
		return dates;
	}

	public List<ModeleContratExclude> getAllExcludedDateProduit(RdbLink em, ModeleContrat mc)
	{
		Query q = em.createQuery("select mce from ModeleContratExclude mce where mce.modeleContrat=:mc"); 
		q.setParameter("mc", mc);

		List<ModeleContratExclude> exclude = q.getResultList();
		return exclude;
	}
	
	
	// PARTIE CREATION D'UN MODELE DE CONTRAT
	/**
	 * Permet de pre charger les nouveaux modeles de contrat
	 */
	@DbRead
	public List<LigneContratDTO> getInfoProduitModeleContrat(Long idProducteur)
	{
		RdbLink em = RdbLink.get();
		
		List<LigneContratDTO> res = new ArrayList<LigneContratDTO>();
		
		Query q = em.createQuery("select p from Produit p " +
				"WHERE p.producteur=:producteur order by p.id");
		q.setParameter("producteur",em.find(Producteur.class, idProducteur));
		List<Produit> prods = q.getResultList();
		for (Produit prod : prods)
		{
			LigneContratDTO l =new LigneContratDTO();
			l.prix = new Integer(0);
			l.produitId = prod.getId();
			l.produitNom = prod.nom;
			l.produitConditionnement = prod.conditionnement;
			res.add(l);
		}
		return res;
	}
	

	// PARTIE CHARGEMENT D'UN MODELE DE CONTRAT

	/**
	 * Permet de charger les informations d'un modele contrat dans une
	 * transaction en lecture
	 */
	@DbRead
	public ModeleContratDTO loadModeleContrat(Long id)
	{
		RdbLink em = RdbLink.get();
		
		ModeleContrat mc = em.find(ModeleContrat.class, id);

		ModeleContratDTO info = new ModeleContratDTO();
		info.id = mc.getId();
		info.nom = mc.nom;
		info.description = mc.description;
		info.producteur = mc.producteur.getId();
		info.dateFinInscription = mc.dateFinInscription;
		info.retardataireAutorise = mc.retardataireAutorise;
		info.gestionPaiement = mc.gestionPaiement;
		info.saisiePaiementModifiable = mc.saisiePaiementModifiable;
		info.saisiePaiementProposition = mc.saisiePaiementProposition;
		info.montantChequeMiniCalculProposition = mc.montantChequeMiniCalculProposition;
		info.saisiePaiementCalculDate = mc.saisiePaiementCalculDate;
		info.strategiePaiement = mc.strategiePaiement;
		info.textPaiement = mc.textPaiement;
		info.affichageMontant = mc.affichageMontant;
		info.libCheque = mc.libCheque;
		info.dateRemiseCheque = mc.dateRemiseCheque;
		info.nature = mc.nature;
		info.cartePrepayeeDelai = mc.cartePrepayeeDelai;
		info.typJoker = mc.typJoker;
		info.jokerNbMin = mc.jokerNbMin;
		info.jokerNbMax = mc.jokerNbMax;
		info.jokerMode = mc.jokerMode;
		info.jokerDelai = mc.jokerDelai;
		info.idPeriodeCotisation = IdentifiableUtil.getId(mc.periodeCotisation);
		
		info.stockGestion = mc.stockGestion;
		info.stockIdentiqueDate = mc.stockIdentiqueDate;
		info.stockMultiContrat = mc.stockMultiContrat;
		
		info.idEngagement = IdentifiableUtil.getId(mc.engagement);
		info.gestionDocEngagement = mc.gestionDocEngagement;
		

		// Avec une sous requete, on obtient la liste de toutes les dates de
		// livraison
		List<ModeleContratDate> dates = getAllDates(em, mc);
		for (ModeleContratDate date : dates)
		{
			DateModeleContratDTO dto = new DateModeleContratDTO();
			dto.dateLiv = date.dateLiv;
			info.dateLivs.add(dto);
		}

		if (dates.size() >= 1)
		{
			info.dateDebut = dates.get(0).dateLiv;
			info.dateFin = dates.get(dates.size() - 1).dateLiv;
		}

		// Avec une sous requete, on récupere la liste des produits
		List<ModeleContratProduit> prods = getAllProduit(em, mc);
		for (ModeleContratProduit prod : prods)
		{
			LigneContratDTO lig = new LigneContratDTO();
			lig.idModeleContratProduit = prod.getId();
			lig.produitId = prod.produit.getId();
			lig.produitNom = prod.produit.nom;
			lig.produitConditionnement = prod.produit.conditionnement;
			lig.prix = prod.prix;

			info.produits.add(lig);
		}

		info.frequence = guessFrequence(dates);
		
		// Avec une sous requete, on récupere la liste des dates de paiements
		List<ModeleContratDatePaiement> datePaiements = new PaiementService().getAllDatesPaiements(em, mc);
		for (ModeleContratDatePaiement date : datePaiements)
		{
			DatePaiementModeleContratDTO dto = new DatePaiementModeleContratDTO();
			dto.datePaiement = date.datePaiement;
			info.datePaiements.add(dto);
		}
		

		return info;
	}

	private FrequenceLivraison guessFrequence(List<ModeleContratDate> dates)
	{
		if ((dates.size() == 0) || dates.size() == 1)
		{
			return FrequenceLivraison.UNE_SEULE_LIVRAISON;
		}
		int delta = DateUtils.getDeltaDay(dates.get(0).dateLiv, dates.get(1).dateLiv);
		if (delta == 7)
		{
			return FrequenceLivraison.UNE_FOIS_PAR_SEMAINE;
		} else if (delta == 14)
		{
			return FrequenceLivraison.QUINZE_JOURS;
		} else
		{
			return FrequenceLivraison.UNE_FOIS_PAR_MOIS;
		}

	}

	// PARTIE SAUVEGARDE D'UN NOUVEAU CONTRAT

	/**
	 * Permet de sauvegarder un nouveau modele de contrat 
	 * 
	 */
	@DbWrite
	public Long saveNewModeleContrat(final ModeleContratDTO modeleContrat)
	{
		RdbLink em = RdbLink.get();

		// Verifications
		if (modeleContrat.dateLivs.size()==0)
		{
			throw new AmapjRuntimeException("Vous ne pouvez pas créer un contrat avec 0 date de livraison");
		}
		
		if (modeleContrat.gestionPaiement==GestionPaiement.NON_GERE)
		{
			if (modeleContrat.datePaiements.size()!=0)
			{
				throw new AmapjRuntimeException();
			}
		}

		
		// On charge le producteur
		Producteur p = em.find(Producteur.class, modeleContrat.producteur);

		// Informations d'entete
		ModeleContrat mc = new ModeleContrat();
		mc.producteur = p;
		mc.nom = modeleContrat.nom;
		mc.description = modeleContrat.description;
		mc.dateFinInscription = modeleContrat.dateFinInscription;
		mc.retardataireAutorise = modeleContrat.retardataireAutorise;
		mc.nature = modeleContrat.nature;
		mc.cartePrepayeeDelai = modeleContrat.cartePrepayeeDelai;
		mc.typJoker = modeleContrat.typJoker;
		mc.jokerNbMin = modeleContrat.jokerNbMin;
		mc.jokerNbMax = modeleContrat.jokerNbMax;
		mc.jokerMode = modeleContrat.jokerMode;
		mc.jokerDelai = modeleContrat.jokerDelai;
		mc.periodeCotisation = IdentifiableUtil.findIdentifiableFromId(PeriodeCotisation.class,modeleContrat.idPeriodeCotisation,em);
		
		// Informations sur le paiement
		mc.gestionPaiement = modeleContrat.gestionPaiement;
		mc.textPaiement = modeleContrat.textPaiement;
		mc.affichageMontant = modeleContrat.affichageMontant;
		mc.dateRemiseCheque = modeleContrat.dateRemiseCheque;
		mc.libCheque = modeleContrat.libCheque;
		mc.saisiePaiementModifiable = modeleContrat.saisiePaiementModifiable;
		mc.saisiePaiementProposition = modeleContrat.saisiePaiementProposition;
		mc.montantChequeMiniCalculProposition = modeleContrat.montantChequeMiniCalculProposition;
		mc.strategiePaiement = modeleContrat.strategiePaiement;
		mc.saisiePaiementCalculDate = modeleContrat.saisiePaiementCalculDate;
		
		// stock 
		mc.stockGestion = modeleContrat.stockGestion;
		mc.stockIdentiqueDate = modeleContrat.stockIdentiqueDate;
		mc.stockMultiContrat = modeleContrat.stockMultiContrat;
		
		//signature
		mc.gestionDocEngagement = modeleContrat.gestionDocEngagement;
		mc.engagement = em.findOrNull(EditionSpecifique.class,modeleContrat.idEngagement);
		
		
		em.persist(mc);

		// Création de toutes les lignes pour chacune des dates de livraison		
		for (DateModeleContratDTO date : modeleContrat.dateLivs)
		{
			ModeleContratDate md = new ModeleContratDate();
			md.modeleContrat = mc;
			md.dateLiv = date.dateLiv;
			em.persist(md);
		}

		// Création de loutes les lignes pour chacun des produits
		List<LigneContratDTO> produits = modeleContrat.getProduits();
		int index = 0;
		for (LigneContratDTO lig : produits)
		{
			ModeleContratProduit mcp = new ModeleContratProduit();
			mcp.indx = index;
			mcp.modeleContrat = mc;
			mcp.prix = lig.getPrix().intValue();
			mcp.produit = em.find(Produit.class, lig.produitId);

			em.persist(mcp);

			index++;

		}
		
		// Informations de dates de paiement
		for (DatePaiementModeleContratDTO datePaiement : modeleContrat.datePaiements)
		{
			ModeleContratDatePaiement md = new ModeleContratDatePaiement();
			md.modeleContrat = mc;
			md.datePaiement = datePaiement.datePaiement;
			em.persist(md);
		}
		
		// Verification de la taille du document d'engagement
		new DocEngagementGeneralService().checkSizeAndSignatureDocumentEngagement(em, mc);
		
		return mc.getId();
	}
	


	// PARTIE SUPPRESSION

	/**
	 * Permet de supprimer un contrat
	 */
	@DbWrite
	public void deleteContrat(Long id)  throws UnableToSuppressException
	{
		RdbLink em = RdbLink.get();
		
		ModeleContrat mc = em.find(ModeleContrat.class, id);
		
		int nbInscrits = getNbInscrits(em, mc);
		if (nbInscrits>0)
		{
			String str = "Vous ne pouvez plus supprimer ce contrat<br/>"+
					 "car "+nbInscrits+" adhérents ont déjà souscrits à ce contrat<br/><br/>."+
					 "Si vous souhaitez réellement supprimer ce contrat,<br/>"+
					 "allez tout d'abord dans \"Gestion des contrats signés\", puis vous cliquez sur le bouton \"Supprimer un contrat signé\""+
					 "pour supprimer tous les contrats signés";
			throw new UnableToSuppressException(str);
		}

		suppressAllDatesPaiement(em, mc);
		deleteAllDateBarreesModeleContrat(em, mc);
		new DeleteNotificationService().deleteAllNotificationDoneModeleContrat(em, mc);
		suppressAllDates(em, mc);
		suppressAllProduits(em, mc);
		suppressStockInfo(em,mc);
		new ExtPModeleContratService().suppressExtendedParameters(em,mc);

		em.remove(mc);
	}

	private void suppressStockInfo(RdbLink em, ModeleContrat mc) 
	{
		Query q = em.createQuery("select pep from ProduitExtendedParam pep where pep.modeleContrat=:mc"); 
		q.setParameter("mc", mc);
		SQLUtils.deleteAll(em, q);	
	}


	private void suppressAllProduits(RdbLink em, ModeleContrat mc)
	{
		Query q = em.createQuery("select mcp from ModeleContratProduit mcp where mcp.modeleContrat=:mc"); 
		q.setParameter("mc", mc);
		SQLUtils.deleteAll(em, q);
	}

	private void suppressAllDates(RdbLink em, ModeleContrat mc)
	{
		Query q = em.createQuery("select mcd from ModeleContratDate mcd where mcd.modeleContrat=:mc ORDER BY mcd.dateLiv"); 
		q.setParameter("mc", mc);
		SQLUtils.deleteAll(em, q);
	}
	
	private void suppressAllDatesPaiement(RdbLink em, ModeleContrat mc)
	{
		Query q = em.createQuery("select d from ModeleContratDatePaiement d WHERE d.modeleContrat=:mc");
		q.setParameter("mc",mc);
		SQLUtils.deleteAll(em, q);
	}

	// PARTIE MISE A JOUR

	/**
	 * Permet de mettre à jour l'etat d'un contrat
	 * 
	 */
	@DbWrite
	public void updateEtat(EtatModeleContrat newValue, Long idModeleContrat)
	{
		RdbLink em = RdbLink.get();
		
		// Verification avant archivage 
		if (newValue==EtatModeleContrat.ARCHIVE)
		{
			ParametresArchivageDTO param = new ParametresService().getParametresArchivage();
			ArchivableState state = new ArchivageContratService().computeArchivageState(idModeleContrat, param);
			if (state.getStatus()==AStatus.NON)
			{
				throw new AmapjRuntimeException("Ce contrat n'est pas archivable : "+CollectionUtils.asStdString(state.nonArchivables,e->e));
			}
		}
		
		ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);
		mc.etat = newValue;
	}

	/**
	 * Permet de mettre à jour les elements d'entete d'un contrat
	 * y compris sa nature 
	 * 
	 * @param newValue
	 * @param idModeleContrat
	 */
	@DbWrite
	public void updateEnteteModeleContrat(ModeleContratDTO modeleContrat)
	{
		RdbLink em = RdbLink.get();
		
		ModeleContrat mc = em.find(ModeleContrat.class, modeleContrat.id);
		mc.dateFinInscription = modeleContrat.dateFinInscription;
		mc.retardataireAutorise = modeleContrat.retardataireAutorise;
		mc.nom = modeleContrat.nom;
		mc.description = modeleContrat.description;
		mc.cartePrepayeeDelai = modeleContrat.cartePrepayeeDelai;
		mc.nature = modeleContrat.nature;
		mc.periodeCotisation = IdentifiableUtil.findIdentifiableFromId(PeriodeCotisation.class,modeleContrat.idPeriodeCotisation,em);
	}

	/**
	 * Permet de mettre à jour les dates d'un contrat
	 */
	@DbWrite
	public void updateDateModeleContrat(ModeleContratDTO modeleContrat) throws OnSaveException
	{
		RdbLink em = RdbLink.get();
		
		ModeleContrat mc = em.find(ModeleContrat.class, modeleContrat.id);

		// Calcul de la liste des nouvelles dates 
		List<Date> newList = modeleContrat.dateLivs.stream().map(e->e.dateLiv).collect(Collectors.toList());
		if (newList.size()==0)
		{
			throw new OnSaveException("Vous ne pouvez pas créer un contrat avec 0 date de livraison");
		}
		
		// Calcul de la liste des anciennes dates
		List<ModeleContratDate> oldList = getAllDates(em, mc);

		// Calcul de la différence entre les deux listes
		ListDiff<ModeleContratDate, Date, Date> diff = DtoToDb.diffList(oldList, newList, e->e.dateLiv,e->e);
		
		// On efface les dates en trop
		GestionContratSigneUpdateService update  = new GestionContratSigneUpdateService();
		for (ModeleContratDate modeleContratDate : diff.toSuppress)
		{
			update.suppressOneDateLiv(modeleContratDate.getId());
		}
		
		// On crée les nouvelles dates 
		for (ElementToAdd<Date> dateLiv : diff.toAdd)
		{
			update.addOneDateLiv(em,dateLiv.dto,mc);
		}
	}


	/**
	 * Permet la mise à jour des dates barrées d'un contrat 
	 */
	@DbWrite
	public void updateDateBarreesModeleContrat(ContratDTO contratDTO) throws OnSaveException
	{
		RdbLink em = RdbLink.get();
		
		ModeleContrat mc = em.find(ModeleContrat.class, contratDTO.modeleContratId);
		
		// On commence par effacer toutes les dates exclues
		deleteAllDateBarreesModeleContrat(em, mc);

		// On recree ensuite toutes les exclusions
		for (int i = 0; i < contratDTO.contratLigs.size(); i++)
		{
			ContratLigDTO ligDto = contratDTO.contratLigs.get(i);
			if (contratDTO.isFullExcludedLine(i) == true)
			{
				ModeleContratExclude exclude = new ModeleContratExclude();
				exclude.modeleContrat = mc;
				exclude.date = em.find(ModeleContratDate.class, ligDto.modeleContratDateId);
				exclude.produit = null;
				em.persist(exclude);
			} 
			else
			{
				for (int j = 0; j < contratDTO.contratColumns.size(); j++)
				{
					if (contratDTO.cell[i][j].excluded == true)
					{
						ContratColDTO colDto = contratDTO.contratColumns.get(j);

						ModeleContratExclude exclude = new ModeleContratExclude();
						exclude.modeleContrat = mc;
						exclude.date = em.find(ModeleContratDate.class, ligDto.modeleContratDateId);
						exclude.produit = em.find(ModeleContratProduit.class, colDto.modeleContratProduitId);
						em.persist(exclude);
					}
				}
			}
		}
		
		// On verifie qu'il n'y a pas des quantités sur une date exclue avant le commit
		String msg = new DateBarreCheckService().checkCoherenceDateBarreesModeleContrat(mc.id);
		if (msg!=null)
		{
			msg = "Incohérence pour le modele de contrat :"+mc.nom+"\n"+msg;
			throw new OnSaveException(msg);
		}
	}



	/**
	 * Methode utilitaire permettant de supprimer toutes les dates barrées d'un modele de contrat
	 * @param em
	 * @param mc
	 */
	public void deleteAllDateBarreesModeleContrat(RdbLink em, ModeleContrat mc)
	{
		// On commence par effacer toutes les dates exclues
		List<ModeleContratExclude> excludes = getAllExcludedDateProduit(em, mc);
		for (ModeleContratExclude exclude : excludes)
		{
			em.remove(exclude);
		}
	}
	
	
	/**
	 * Permet la mise à jour des produits d'un contrat 
	 *  
	 */
	@DbWrite
	public void updateProduitModeleContrat(final ModeleContratDTO modeleContrat)
	{
		RdbLink em = RdbLink.get();
		
		ModeleContrat mc = em.find(ModeleContrat.class, modeleContrat.id);

		// Calcul de la liste des anciens produits en base 
		List<ModeleContratProduit> dbList = getAllProduit(em, mc);
				
		// Calcul de la liste des nouveaux produits
		List<LigneContratDTO> dtoList = modeleContrat.getProduits();
		if (dtoList.size()==0)
		{
			throw new AmapjRuntimeException("Vous ne pouvez pas créer un contrat avec 0 produits");
		}
		
		// Calcul de la différence entre les deux listes
		ListDiff<ModeleContratProduit, LigneContratDTO, Long> diff = DtoToDb.diffList(dbList, dtoList, e->e.produit.getId(),e->e.produitId);
		
		// On efface les produits en trop
		GestionContratSigneUpdateService update  = new GestionContratSigneUpdateService();
		for (ModeleContratProduit mcp : diff.toSuppress)
		{
			update.suppressOneProduit(mcp.getId());
		}
		
		// On crée les nouveaux produits avec le bon index 
		for (ElementToAdd<LigneContratDTO> toAdd : diff.toAdd)
		{
			update.addOneProduit(em,toAdd.dto.produitId,toAdd.dto.prix,toAdd.index,mc);
		}
		
		// On met à jour les produits existants 
		for (ElementToUpdate<ModeleContratProduit, LigneContratDTO> toUpdate : diff.toUpdate)
		{
			update.updateModeleContratProduit(em,toUpdate.db.getId(),toUpdate.dto.prix,toUpdate.index);
		}
	}
	
	// MISE A JOUR DES INFORMATIONS JOKERS
	
	@DbWrite
	public void updateJoker(ModeleContratDTO modeleContrat)
	{
		RdbLink em = RdbLink.get();
		
		ModeleContrat mc = em.find(ModeleContrat.class, modeleContrat.id);
		
		// On marque tous les contrats comme modifiés
		new GestionContratSigneUpdateService().markAllContratAsModified(mc, em);
		
		mc.typJoker = modeleContrat.typJoker;
		mc.jokerNbMin = modeleContrat.jokerNbMin;
		mc.jokerNbMax = modeleContrat.jokerNbMax;
		mc.jokerMode = modeleContrat.jokerMode;
		mc.jokerDelai = modeleContrat.jokerDelai;	
	}
	

	// Obtenir le montant total commandé pour un contrat
	public int getMontantCommande(RdbLink em, ModeleContrat mc)
	{
		Query q = em.createQuery("select sum(c.qte*c.modeleContratProduit.prix) " +
				"from ContratCell c " +
				"WHERE c.contrat.modeleContrat=:mc");
		q.setParameter("mc", mc);
		return SQLUtils.toInt(q.getSingleResult());
	}
	
	
	// Obtenir le montant total des avoirs initiaux pour un contrat
	public int getMontantAvoir(RdbLink em, ModeleContrat mc)
	{
		Query q = em.createQuery("select sum(c.montantAvoir) " +
				"from Contrat c " +
				"WHERE c.modeleContrat=:mc");
		q.setParameter("mc", mc);
		return SQLUtils.toInt(q.getSingleResult());
	}
	
	
	/**
	 * Permet d'obtenir le detail d'un contrat pour l'afficher dans la livraison d'un producteur
	 * 
	 * @return
	 */
	@DbRead
	public String getDetailContrat(Long modeleContratDateId)
	{
		String msg = "";
		Long user = 0L;
		RdbLink em = RdbLink.get();
				
		ModeleContratDate mcDate = em.find(ModeleContratDate.class, modeleContratDateId);
		
		Query q = em.createQuery("select c from ContratCell c WHERE " +
				"c.modeleContratDate=:mcDate "+
				"order by c.contrat.utilisateur.nom , c.contrat.utilisateur.prenom , c.modeleContratProduit.indx");
		q.setParameter("mcDate", mcDate);
		
		List<ContratCell> cells = q.getResultList();
		for (ContratCell cell : cells)
		{
			int qte =  cell.qte;
			Utilisateur u = cell.contrat.utilisateur;
			Produit produit = cell.modeleContratProduit.produit;
			
			if (u.getId().equals(user)==false)
			{
				user = u.getId();
				if (msg.length()!=0)
				{
					msg +="</ul>";
				}
				msg += "<b>"+u.nom+" "+u.prenom+"</b><ul>";
			}
			msg += "<li>"+qte+" "+produit.nom+" , "+produit.conditionnement+"</li>";
		}
		
		if (msg.length()!=0)
		{
			msg +="</ul>";
		}
		
		return msg;
	}

	/**
	 * Mise à jour en masse des informations sur les periodes de cotisation
	 */
	@DbWrite
	public void updatePeriodeCotisationMasse(List<ModeleContratSummaryDTO> mcs) 
	{
		RdbLink em = RdbLink.get();
		for (ModeleContratSummaryDTO mc : mcs) 
		{
			ModeleContrat m = em.find(ModeleContrat.class, mc.id);
			m.periodeCotisation = IdentifiableUtil.findIdentifiableFromId(PeriodeCotisation.class,mc.periodeCotisationId,em);
		}
		
	}


	@DbWrite
	public void updateLimiteQuantiteModeleContrat(ModeleContratDTO dto)
	{
		RdbLink em = RdbLink.get();
		ModeleContrat mc = em.find(ModeleContrat.class, dto.id);
		
		mc.stockGestion = dto.stockGestion;
		mc.stockIdentiqueDate = dto.stockIdentiqueDate;
		mc.stockMultiContrat = dto.stockMultiContrat;
		
	}

	@DbWrite
	public void updateSignatureEnLigne(ModeleContratDTO modeleContrat) 
	{
		RdbLink em = RdbLink.get();
		
		ModeleContrat mc = em.find(ModeleContrat.class, modeleContrat.id);
		mc.gestionDocEngagement = modeleContrat.gestionDocEngagement;
		mc.engagement = em.findOrNull(EditionSpecifique.class, modeleContrat.idEngagement);
		
		// Verification de la taille du document d'engagement
		new DocEngagementGeneralService().checkSizeAndSignatureDocumentEngagement(em, mc);
	}

}
