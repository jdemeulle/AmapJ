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

 package fr.amapj.service.services.mescontrats;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import fr.amapj.common.DateUtils;
import fr.amapj.common.SQLUtils;
import fr.amapj.model.engine.IdentifiableUtil;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.ModeleContratDate;
import fr.amapj.model.models.contrat.modele.ModeleContratDatePaiement;
import fr.amapj.model.models.contrat.modele.ModeleContratExclude;
import fr.amapj.model.models.contrat.modele.ModeleContratProduit;
import fr.amapj.model.models.contrat.modele.NatureContrat;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.model.models.contrat.reel.ContratCell;
import fr.amapj.model.models.contrat.reel.EtatPaiement;
import fr.amapj.model.models.contrat.reel.Paiement;
import fr.amapj.model.models.cotisation.PeriodeCotisation;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.model.models.remise.RemiseProducteur;
import fr.amapj.service.services.docengagement.signonline.core.CoreDocEngagementSignOnLineService;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.gestioncontrat.datebarree.DateBarreCheckService;
import fr.amapj.service.services.gestioncontrat.reglesaisie.RegleSaisieModeleContratService;
import fr.amapj.service.services.mescontrats.inscription.InscriptionDTO;
import fr.amapj.service.services.mescontrats.inscription.InscriptionService;
import fr.amapj.service.services.producteur.ProducteurService;
import fr.amapj.service.services.remiseproducteur.RemiseProducteurService;
import fr.amapj.service.services.remiseproducteur.RemiseProducteurService.ModeleContratDatePaiementInfo;
import fr.amapj.service.services.stockservice.verifstock.VerifStockDTO;
import fr.amapj.service.services.stockservice.verifstock.VerifStockService;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.suppressionpopup.UnableToSuppressException;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.ModeSaisie;
import fr.amapj.view.views.saisiecontrat.abo.ContratAboGuesser;

/**
 * Permet l'affichage des contrats dans MesContratsView et VisiteAmapView
 */
public class MesContratsService
{
	/**
	 * Chargement complets des informations sur un contrat   
	 */
	@DbRead
	public MonContratDTO loadMonContratDTO(Long modeleContratId, Long contratId, ModeSaisie modeSaisie)
	{
		//
		RdbLink em = RdbLink.get();
		Date now = DateUtils.getDate();
		
		//
		ModeleContrat mc = em.find(ModeleContrat.class, modeleContratId);
		Contrat contrat=null;
		if (contratId != null)
		{
			contrat = em.find(Contrat.class, contratId);
		}
		
		// Avec une sous requete, on obtient la liste de toutes les dates de livraison
		List<ModeleContratDate> dates = new GestionContratService().getAllDates(em, mc);
		
		// 1 . Chargement complet du contrat 
		MonContratDTO m = new MonContratDTO();
		m.contratDTO = buildFullContratDTO(em, mc, contrat,dates);
		
		// 2. Chargement des informations de stock si besoin 
		new VerifStockService().insertInfoStock(em,mc,m,modeSaisie);
		
		// 3. Chargement des informations de verifications des saisies si besoin
		new RegleSaisieModeleContratService().insertInfoVerifSaisie(em,mc,m,modeSaisie);
		
		// 4. Calcul des informations d'inscriptions 
		m.inscriptionDTO = new InscriptionDTO();
		new InscriptionService(m.contratDTO,modeSaisie,now,contrat,mc,m.inscriptionDTO).buildInscriptionDTO();
		
		return m;
		
	}


	
	// PARTIE CHARGEMENT D'UN CONTRAT A PARTIR D'UN MODELE DE CONTRAT

	/**
	 * Permet de charger les informations complete d'un modele contrat dans une transaction en lecture
	 * sans les informations d'inscription 
	 */
	@DbRead
	public ContratDTO loadContrat(Long modeleContratId, Long contratId)
	{
		//
		RdbLink em = RdbLink.get();
		
		//
		ModeleContrat mc = em.find(ModeleContrat.class, modeleContratId);
		Contrat contrat=null;
		if (contratId != null)
		{
			contrat = em.find(Contrat.class, contratId);
		}
		
		// Avec une sous requete, on obtient la liste de toutes les dates de livraison
		List<ModeleContratDate> dates = new GestionContratService().getAllDates(em, mc);
		
		return buildFullContratDTO(em, mc, contrat,dates);
	}
	
	private ContratDTO buildFullContratDTO(RdbLink em, ModeleContrat mc,Contrat contrat, List<ModeleContratDate> dates)
	{
		// PHASE 1 - CHARGEMENT DES INFORMATIONS RELATIVES AU MODELE DE CONTRAT
		ContratDTO dto = new ContratDTO();
		
		dto.modeleContratId = mc.getId();
		dto.nom = mc.nom;
		dto.dateFinInscription = mc.dateFinInscription;
		dto.nature = mc.nature;
		
		// Chargement des informations de joker 
		dto.typJoker = mc.typJoker;
		dto.jokerNbMin = mc.jokerNbMin;
		dto.jokerNbMax = mc.jokerNbMax;
		dto.jokerMode = mc.jokerMode;
		dto.jokerDelai = mc.jokerDelai;
		

		// Avec une sous requete, on récupere la liste des produits
		List<ModeleContratProduit> prods = new GestionContratService().getAllProduit(em, mc);
		for (ModeleContratProduit prod : prods)
		{
			ContratColDTO col = new ContratColDTO();
			col.modeleContratProduitId = prod.getId();
			col.nomProduit = prod.produit.nom;
			col.condtionnementProduit = prod.produit.conditionnement;
			col.produitId = prod.produit.id;
			col.prix = prod.prix;
			col.j = dto.contratColumns.size();
			col.produitWebPageId = IdentifiableUtil.getId(prod.produit.webPage);

			dto.contratColumns.add(col);
		}

		// on charge la liste de toutes les dates de livraison
		for (ModeleContratDate date : dates)
		{
			ContratLigDTO lig = new ContratLigDTO();
			lig.date = date.dateLiv;
			lig.modeleContratDateId = date.getId();
			lig.i = dto.contratLigs.size();
			
			dto.contratLigs.add(lig);
		}
		
		// Avec une sous requete on recupere la liste des dates ou des produits qui sont exclus si il y en a 
		List<ModeleContratExclude> excludeds = new GestionContratService().getAllExcludedDateProduit(em, mc);
		dto.cell = new ContratCellDTO[dto.contratLigs.size()][dto.contratColumns.size()];
		for (int i = 0; i < dto.contratLigs.size(); i++)
		{
			for (int j = 0; j < dto.contratColumns.size(); j++)
			{
				dto.cell[i][j] = new ContratCellDTO();
				dto.cell[i][j].excluded = false ;
				dto.cell[i][j].available = true ;
			}
		}
		for (ModeleContratExclude exclude : excludeds)
		{
			insertExcluded(dto,exclude.date.getId(),exclude.produit);
		}
		
		// On récupère les informations liées au paiement
		dto.paiement = new InfoPaiementDTO();
		dto.paiement.gestionPaiement = mc.gestionPaiement;
		dto.paiement.saisiePaiementModifiable = mc.saisiePaiementModifiable;
		dto.paiement.saisiePaiementProposition = mc.saisiePaiementProposition;
		dto.paiement.montantChequeMiniCalculProposition = mc.montantChequeMiniCalculProposition;
		dto.paiement.textPaiement = mc.textPaiement;
		dto.paiement.affichageMontant = mc.affichageMontant;
		dto.paiement.libCheque = mc.libCheque;
		dto.paiement.referentsRemiseCheque = new ProducteurService().getReferents(em, mc.producteur);
		
		
		// On récupère la liste ordonnée des dates de paiements depuis le modele de contrat
		List<ModeleContratDatePaiementInfo> datePaiements = new RemiseProducteurService().getAllDatesPaiementsInfo(em, mc);
		for (ModeleContratDatePaiementInfo date : datePaiements)
		{
			DatePaiementDTO lig = new DatePaiementDTO();
			lig.datePaiement = date.modeleContratDatePaiement.datePaiement;
			lig.idModeleContratDatePaiement = date.modeleContratDatePaiement.getId();
			lig.montant = 0;
			lig.etatPaiement = date.remiseDone ? EtatPaiement.PRODUCTEUR : EtatPaiement.A_FOURNIR;
			lig.idPaiement = null;
			
			dto.paiement.datePaiements.add(lig);
		}
		
		// On récupère les informations liées  à la signature du contrat
		dto.gestionDocEngagement = mc.gestionDocEngagement;
		
		
		// PHASE 2 - CHARGEMENT DES INFORMATIONS LIES A CE CONTRAT SI IL Y EN A DEJA UN EXISTANT 
		if (contrat!=null)
		{
			// Les informations generales
			dto.contratId = contrat.id;
			dto.typInscriptionContrat = contrat.typInscriptionContrat;
			
			// on récupère les quantités déjà saisies
			List<ContratCell> qtes = getAllQte(em, contrat);
			for (ContratCell qte : qtes)
			{
				insert(dto, qte.modeleContratDate.getId(), qte.modeleContratProduit.getId(), qte.qte);
			}
			
			// on récupère l'avoir
			dto.paiement.avoirInitial = contrat.montantAvoir;
			
			// on récupère les  montants des chèques déjà saisis
			for (DatePaiementDTO lig : dto.paiement.datePaiements)
			{
				Paiement paiement = getPaiement(lig.idModeleContratDatePaiement, contrat, em);
				if (paiement!=null)
				{
					lig.idPaiement = paiement.getId();
					lig.montant = paiement.montant;
					lig.etatPaiement = paiement.etat;
				}
			}
		}

		return dto;
	}
	
	

	/**
	 * Permet de retrouver un paiement à partir du ModeleContratDatePaiement mcdp et du contrat
	 *
	 */
	private Paiement getPaiement(Long mcdp,Contrat c,RdbLink em)
	{
		TypedQuery<Paiement> q = em.createQuery("select p from Paiement p WHERE p.contrat=:c and p.modeleContratDatePaiement.id =:mcdp ",Paiement.class);
		q.setParameter("c",c);
		q.setParameter("mcdp",mcdp);
		
		return SQLUtils.oneOrZero(q);
	}
	

	private void insertExcluded(ContratDTO dto, Long modeleContratDateId, ModeleContratProduit modeleContratProduit)
	{
		if (modeleContratProduit==null)
		{
			int lig = findLigIndex(dto, modeleContratDateId);
			dto.excludeThisLine(lig);
		}
		else
		{
			int lig = findLigIndex(dto, modeleContratDateId);
			int col = findColIndex(dto, modeleContratProduit.getId());
			dto.cell[lig][col].excluded  = true;
		}
	}


	private void insert(ContratDTO dto, Long modeleContratDateId, Long modeleContratProduitId, int qte)
	{
		int lig = findLigIndex(dto, modeleContratDateId);
		int col = findColIndex(dto, modeleContratProduitId);

		dto.cell[lig][col].qte = qte;
	}

	private int findLigIndex(ContratDTO dto, Long modeleContratDateId)
	{
		int index = 0;
		for (ContratLigDTO lig : dto.contratLigs)
		{
			if (lig.modeleContratDateId.equals(modeleContratDateId))
			{
				return index;
			}
			index++;
		}
		throw new RuntimeException("Erreur inattendue");
	}

	private int findColIndex(ContratDTO dto, Long modeleContratProduitId)
	{
		int index = 0;
		for (ContratColDTO col : dto.contratColumns)
		{
			if (col.modeleContratProduitId.equals(modeleContratProduitId))
			{
				return index;
			}
			index++;
		}
		throw new RuntimeException("Erreur inattendue");
	}


	/**
	 * 
	 */
	public List<ContratCell> getAllQte(RdbLink em, Contrat c)
	{
		TypedQuery<ContratCell> q = em.createQuery("select cc from ContratCell cc where cc.contrat=:c",ContratCell.class);
		q.setParameter("c", c);
		return q.getResultList();
	}

	// PARTIE SAUVEGARDE D'UN NOUVEAU CONTRAT

	/**
	 * Permet de sauvegarder un nouveau contrat ou de modifier un contrat existant
	 */
	@DbWrite
	public Long saveNewContrat(ContratDTO contratDTO,Long userId) throws OnSaveException
	{
		RdbLink em = RdbLink.get();
		Date now = DateUtils.getDate();
		
		// On vérifie d'abord que le contrat n'est pas vide
		if (contratDTO.isEmpty())
		{
			throw new OnSaveException("Il est impossible de sauvegarder un contrat vide");
		}
		
		ModeleContrat mc = em.find(ModeleContrat.class, contratDTO.modeleContratId);

		// Chargement ou création du contrat
		Contrat c = null;
		if (contratDTO.contratId==null)
		{
			c = new Contrat();
			c.dateCreation = now;
			c.modeleContrat = mc;
			c.utilisateur = em.find(Utilisateur.class, userId);
			c.typInscriptionContrat = contratDTO.typInscriptionContrat; 
			em.persist(c);
		}
		else
		{
			c = em.find(Contrat.class, contratDTO.contratId);
			c.dateModification = now;
		}
		
		// Création ou modification de toutes les lignes quantités
		
		// Chargement des lignes depuis la base de données sous la forme d'une matrice
		ContratCell[][] matrix = getAllQteAsMatrix(em, c,contratDTO);
		
		// Ensuite on balaye chacune des cases pour agir si besoin 
		int nbLigs = contratDTO.contratLigs.size();
		int nbCols = contratDTO.contratColumns.size();
		for (int i = 0; i < nbLigs; i++)
		{
			for (int j = 0; j < nbCols; j++)
			{
				ContratCell dbCell = matrix[i][j];
				ContratColDTO colDto = contratDTO.contratColumns.get(j);
				ContratLigDTO ligDto = contratDTO.contratLigs.get(i);
				int qteDto = contratDTO.cell[i][j].qte;
				
				updateCellInDb(em,dbCell,qteDto,ligDto,colDto,c);
			}
		}
		
		
		// Création ou modification de toutes les lignes paiements
		for (DatePaiementDTO datePaiementDTO : contratDTO.paiement.datePaiements)
		{
			ModeleContratDatePaiement mcdp = em.find(ModeleContratDatePaiement.class, datePaiementDTO.idModeleContratDatePaiement);
			Paiement p = null;
			if (datePaiementDTO.idPaiement!=null)
			{
				p = em.find(Paiement.class, datePaiementDTO.idPaiement);
			}
			insertPaiement(p,datePaiementDTO,c,mcdp,em,contratDTO);
		}	
		
		// Sauvegarde des donnees specifiques aux contrats de type abonnement
		if (mc.nature==NatureContrat.ABONNEMENT)
		{
			if (contratDTO.contratAbo!=null)
			{
				c.aboInfo = contratDTO.contratAbo.getAboInfo();
			}
			else
			{
				 c.aboInfo = new ContratAboGuesser(contratDTO).guessContratAboInfo();
			}
		}
		
		// On verifie qu'il n'y a pas des quantités sur une date exclue avant le commit
		String msg = new DateBarreCheckService().checkCoherenceDateBarreesContrat(c.id);
		if (msg!=null)
		{
			throw new OnSaveException(msg);
		}
		
		// On fait la verification sur le stock disponible
		VerifStockDTO stockContainer= new VerifStockService().loadVerifStock(em, mc,contratDTO,c.id);
		stockContainer.setQteMe(contratDTO);
		if (stockContainer.isStockSuffisant()==false)
		{
			List<String> ls = new VerifStockService().computePrettyMessage(stockContainer);
			throw new OnSaveException(ls);
		}
		
		// On sauvegarde le contrat signé si il y en a un 
		new CoreDocEngagementSignOnLineService().saveDocEngagementSigneByAmapien(contratDTO.docEngagementDTO,c,em,now);
		
		//
		return c.getId();
	}
	
	private void insertPaiement(Paiement p, DatePaiementDTO datePaiementDTO,Contrat c,ModeleContratDatePaiement mcdp,RdbLink em, ContratDTO contratDTO) throws OnSaveException
	{
		if (p==null)
		{
			if (datePaiementDTO.montant==0)
			{
				// Rien à faire
			}
			else
			{
				// On vérifie que la remise n'a pas été faite pour cette date
				checkRemiseNonFaite(mcdp,em);
				
				// On crée la cellule dans la base
				p = new Paiement();
				p.contrat = c;
				p.modeleContratDatePaiement = mcdp;
				p.montant = datePaiementDTO.montant;
				em.persist(p);
			}
		}
		else	
		{
			// Existe il un changement entre le nouveau paiement et l'ancien ? 
			if (datePaiementDTO.montant==p.montant)
			{
				// Rien à faire 
			}
			else
			{
				// On verifie d'abord que ce paiement n'a pas été remis au producteur 
				if (p.etat==EtatPaiement.PRODUCTEUR)
				{
					SimpleDateFormat df = new SimpleDateFormat("MMMMM yyyy");
					throw new OnSaveException("La remise des chèques  a été faite au producteur pour le mois de "+df.format(p.modeleContratDatePaiement.datePaiement)+". Vous ne pouvez donc pas modifier le montant du paiement pour cette date");
				}
				
				if (p.etat==EtatPaiement.AMAP && contratDTO.modeSaisie==ModeSaisie.STANDARD)
				{
					SimpleDateFormat df = new SimpleDateFormat("MMMMM yyyy");
					throw new OnSaveException("Le chèque pour le mois de "+df.format(p.modeleContratDatePaiement.datePaiement)+" est marqué comme réceptionné à l'AMAP . Vous ne pouvez donc pas modifier le montant du paiement pour cette date");
				}
								
				if (datePaiementDTO.montant==0)
				{
					// On enleve la cellule dans la base
					em.remove(p);
				}
				else
				{
					// On met à jour la cellule dans la base
					p.montant = datePaiementDTO.montant;	
				}
			}
		}
		
	}

	private void checkRemiseNonFaite(ModeleContratDatePaiement mcdp, RdbLink em) throws OnSaveException
	{
		Query q = em.createQuery("select r from RemiseProducteur r WHERE r.datePaiement=:mcdp");
		q.setParameter("mcdp",mcdp);
		
		
		List<RemiseProducteur> rps = q.getResultList();
		if (rps.size()>0)
		{
			SimpleDateFormat df = new SimpleDateFormat("MMMMM yyyy");
			throw new OnSaveException("La remise des chèques  a été faite au producteur pour le mois de "+df.format(mcdp.datePaiement)+". Vous ne devez donc pas mettre de paiement pour cette date");
		}
		
	}

	private void updateCellInDb(RdbLink em, ContratCell dbCell, int qteDto, ContratLigDTO ligDto, ContratColDTO colDto,Contrat c)
	{
		if (qteDto==0)
		{
			if (dbCell==null)
			{
				// Rien à faire
			}
			else
			{
				// On supprime la cellule dans la base devenue inutile
				em.remove(dbCell);
			}
		}
		else
		{
			if (dbCell==null)
			{
				// On crée la cellule dans la base 
				ContratCell cl = new ContratCell();
				cl.contrat = c;
				cl.modeleContratDate = em.find(ModeleContratDate.class, ligDto.modeleContratDateId);
				cl.modeleContratProduit = em.find(ModeleContratProduit.class, colDto.modeleContratProduitId);
				cl.qte = qteDto;
				em.persist(cl);
			}
			else
			{
				// On met a jour la cellule dans la base 
				dbCell.qte = qteDto;
			}
		}
		
	}

	private ContratCell[][] getAllQteAsMatrix(RdbLink em, Contrat c, ContratDTO dto)
	{
		
		ContratCell[][] res = new ContratCell[dto.contratLigs.size()][dto.contratColumns.size()];
		
		List<ContratCell> ligs = getAllQte(em, c);
		for (ContratCell lig : ligs)
		{
			int i = getIndexLig(lig,dto);
			int j = getIndexCol(lig,dto);
			res[i][j] = lig;
		}
		
		
		return res;
	}

	private int getIndexLig(ContratCell lig, ContratDTO contratDTO)
	{
		int i = 0;
		for (ContratLigDTO dto : contratDTO.contratLigs)
		{
			if (dto.modeleContratDateId.equals(lig.modeleContratDate.getId()))
			{
				return i;
			}
			i++;
		}
		throw new RuntimeException("Erreur inattendue");
	}

	private int getIndexCol(ContratCell lig, ContratDTO contratDTO)
	{
		int j = 0;
		for (ContratColDTO dto : contratDTO.contratColumns)
		{
			if (dto.modeleContratProduitId.equals(lig.modeleContratProduit.getId()))
			{
				return j;
			}
			j++;
		}
		throw new RuntimeException("Erreur inattendue");
	}



	// PARTIE SUPPRESSION D'UN CONTRAT

	/**
	 * Permet de supprimer un contrat
	 * Ceci est fait dans une transaction en ecriture  
	 */
	@DbWrite
	public void deleteContrat(Long contratId)
	{
		RdbLink em = RdbLink.get();
		
		Contrat c = em.find(Contrat.class, contratId);
		List<ContratCell> qtes = getAllQte(em, c);
		for (ContratCell contratLig : qtes)
		{
			em.remove(contratLig);
		}
		
		List<Paiement> ps = getAllPaiements(em, c);
		for (Paiement paiement : ps)
		{
			if (paiement.etat.equals(EtatPaiement.A_FOURNIR))
			{
				em.remove(paiement);
			}
			else
			{
				String str = "Il existe un paiement de "+new CurrencyTextFieldConverter().convertToString(paiement.montant)+" €";
				if (paiement.etat.equals(EtatPaiement.AMAP))
				{
					str = str+" qui est receptionné à l'AMAP. Il faut rendre le chèque à l'AMAPIEN ,"
							+ "modifier l'état du chèque dans Réception des chèques , et vous pourrez ensuite supprimer le contrat";  
				}
				else
				{
					str = str+" qui a été donné au producteur (remise). Il n'est plus possible supprimer le contrat.";
				}
				
				throw new UnableToSuppressException(str);
			}
		}
		
		// On supprime le document engagement signé si il y en a 
		new CoreDocEngagementSignOnLineService().deleteDocEngagementSigne(em,c);
		
		em.remove(c);

	}

	
	/**
	 * Retourne la liste de tous les paiements pour un contrat particulier, tri par date
	 * @param em
	 * @param c
	 * @return
	 */
	private List<Paiement> getAllPaiements(RdbLink em, Contrat c)
	{
		// On récupère ensuite la liste de tous les paiements de cet utilisateur
		Query q = em.createQuery("select p from Paiement p WHERE p.contrat=:c order by p.modeleContratDatePaiement.datePaiement");
		q.setParameter("c",c);
		List<Paiement> paiements = q.getResultList();
		return paiements;
	}
	
	
	
	/**
	 * Permet de retrouver la liste de tous les utilisateurs ayant un contrat sur ce modele de contrat
	 * 
	 */
	public List<Utilisateur> getUtilisateur(RdbLink em, ModeleContrat mc)
	{
		Query q = em.createQuery("select u from Utilisateur u WHERE EXISTS (select c from Contrat c where c.utilisateur = u and c.modeleContrat=:mc) ORDER BY u.nom,u.prenom");
		q.setParameter("mc",mc);
		List<Utilisateur> us = q.getResultList();
		return us;
	}
	
	
	
	// PARTIE GESTION DES AVOIRS
	
	/**
	 * Permet de sauvegarder un avoir
	 * Ceci est fait dans une transaction en ecriture  
	 */
	@DbWrite
	public void saveAvoirInitial(final Long idContrat, final int mntAvoir)
	{
		RdbLink em = RdbLink.get();
		
		Contrat c = em.find(Contrat.class, idContrat);
		c.montantAvoir = mntAvoir;
	}
	
	// RECHERCHE D'UN CONTRAT
	
	/**
	 * Permet de retrouver un contrat à partir du modele et 
	 * de l'utilisateur 
	 * 
	 * Retourne une exception s'il n'y a pas un et un seul  
	 * 
	 * @param modeleContratId
	 * @param em
	 * @param utilisateur
	 * @return
	 */
	public Contrat getContrat(Long modeleContratId, RdbLink em, Utilisateur utilisateur)
	{
		ModeleContrat mc = em.find(ModeleContrat.class, modeleContratId);
		Query q = em.createQuery("select c from Contrat c where c.utilisateur =:u and c.modeleContrat=:mc");
		q.setParameter("mc",mc);
		q.setParameter("u",utilisateur);
		
		List<Contrat> cs = q.getResultList();
		if (cs.size()!=1)
		{
			throw new RuntimeException("Erreur inattendue pour "+utilisateur.nom+utilisateur.prenom);
		}
		
		return cs.get(0);
	}

	// VERIFICATION DES ACCES 
	
	/**
	 * Un amapien ne peut pas accéder à un contrat si il n'est cotisant sur la periode de cotisation du contrat
	 */
	@DbRead
	public String checkIfAccessAllowed(ContratDTO contratDTO, Long userId) 
	{
		RdbLink em = RdbLink.get();
		
		// Si il n'y a pas d'utilisateur (mode test) : on ne fait pas de verification 
		if (userId==null)
		{
			return null;
		}

		PeriodeCotisation periodeCotisation = em.find(ModeleContrat.class, contratDTO.modeleContratId).periodeCotisation;
		Utilisateur u = em.find(Utilisateur.class, userId);
		
		// Si il n'y a pas de periode de cotisation : on ne fait pas de verification 
		if (periodeCotisation==null)
		{
			return null;
		}
		
		// Récupération de la cotisation
		Query q = em.createQuery("select count(pu) from PeriodeCotisationUtilisateur pu WHERE pu.periodeCotisation=:p and pu.utilisateur=:u");
		q.setParameter("p",periodeCotisation);
		q.setParameter("u",u);
		long nb = SQLUtils.count(q);
			
		// L'utilisateur n'a pas adhéré
		if (nb==0)
		{
			return "Vous devez être cotisant sur la période "+periodeCotisation.nom+" pour pouvoir vous inscrire sur ce contrat. Pour cela, merci d'aller à la page \"Mes adhésions\"";
		}
		else
		{
			return null;
		}
	}
	
	
	
}
