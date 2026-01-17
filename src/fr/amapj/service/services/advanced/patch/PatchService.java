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
 package fr.amapj.service.services.advanced.patch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.CollectionUtils;
import fr.amapj.common.DateUtils;
import fr.amapj.model.engine.db.DbManager;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.tools.SpecificDbUtils;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.engine.transaction.NewTransaction;
import fr.amapj.model.models.acces.RoleList;
import fr.amapj.model.models.contrat.modele.EtatModeleContrat;
import fr.amapj.model.models.contrat.modele.GestionPaiement;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.NatureContrat;
import fr.amapj.model.models.contrat.modele.TypJoker;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.model.models.contrat.reel.Paiement;
import fr.amapj.model.models.fichierbase.EtatUtilisateur;
import fr.amapj.model.models.fichierbase.Producteur;
import fr.amapj.model.models.fichierbase.RoleAdmin;
import fr.amapj.model.models.fichierbase.RoleTresorier;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.model.models.param.paramecran.PEGestionContratsVierges;
import fr.amapj.model.models.param.paramecran.PEGestionContratsVierges.GestionJoker;
import fr.amapj.model.models.param.paramecran.PEListeAdherent;
import fr.amapj.model.models.param.paramecran.PEListeAdherent.PEListeAdherentAccess;
import fr.amapj.model.models.param.paramecran.common.AbstractParamEcran;
import fr.amapj.model.models.param.paramecran.common.ParamEcran;
import fr.amapj.model.models.permanence.periode.PeriodePermanenceUtilisateur;
import fr.amapj.model.models.web.WebPage;
import fr.amapj.service.engine.objectstorage.basestorage.remote.RemoteBaseObjectStorageService;
import fr.amapj.service.services.appinstance.AppInstanceService;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.gestioncontrat.datebarree.DateBarreCheckService;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.mescontrats.MesContratsService;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.ui.AppConfiguration;
import fr.amapj.view.views.saisiecontrat.abo.ContratAboGuesser;

/**
 * Permet la gestion des pacths pour les migrations 
 */
public class PatchService
{
	
	private final static Logger logger = LogManager.getLogger();
	
	public PatchService()
	{

	}


//	/**
//	 * Application du patch V019
//	 */
//	public String applyPatchV019()
//	{
//		StringBuffer str = new StringBuffer();
//		SpecificDbUtils.executeInAllDb(()->patch(str),false);
//		return str.toString();
//	}
//	
//	@DbWrite
//	private Void patch(StringBuffer str)
//	{
//		EntityManager em = TransactionHelper.getEm();
//		
//		String dbName = DbUtil.getCurrentDb().getDbName();
//		
//		Query q = em.createQuery("select p from EditionSpecifique p");
//
//		List<EditionSpecifique> ps = q.getResultList();
//		for (EditionSpecifique p : ps)
//		{
//			zipContent(p);
//		}
//		
//		str.append("ok pour "+dbName+"<br/>");
//		
//		return null;
//	}
//
//
//	/**
//	 * On zippe uniquement si cela n'a pas déja été fait 
//	 * @param p
//	 */
//	private void zipContent(EditionSpecifique p)
//	{
//		if (p.content==null)
//		{
//			return ;
//		}
//		if (p.content.startsWith("{")==false)
//		{
//			return;
//		}
//		
//		p.content = GzipUtils.compress(p.content);
//	}

	
//	/**
//	 * Application du patch V020
//	 */
//	public String applyPatchV020()
//	{
//		StringBuffer str = new StringBuffer();
//		SpecificDbUtils.executeInAllDb(()->patch(str),false);
//		return str.toString();
//	}
//	
//	@DbWrite
//	private Void patch(StringBuffer str)
//	{
//		EntityManager em = TransactionHelper.getEm();
//		
//		String dbName = DbUtil.getCurrentDb().getDbName();
//		
//		int nb= 0;
//		
//		str.append("Nombre de données transférées="+nb+" - ok pour "+dbName+"<br/>");
//		
//		logger.info("Nombre de données transférées="+nb+" - ok pour "+dbName);
//		
//		return null;
//	}
	
	
//	/**
//	 * Application du patch V026
//	 *
//	 */
//	public String applyPatchV026()
//	{
//		StringBuffer str = new StringBuffer();
//		SpecificDbUtils.executeInAllDb(()->patch(str),false);
//		return str.toString();
//	}
//	
//	@DbWrite
//	private Void patch(StringBuffer str)
//	{
//		EntityManager em = TransactionHelper.getEm();
//		
//		String dbName = DbUtil.getCurrentDb().getDbName();
//		
//		int nb = processProducteur(em);		
//		String msg = "Nombre de producteur trouvés ="+nb+" - ok pour "+dbName;
//		
//		str.append(msg+"<br/>");
//		logger.info(msg);
//		
//		nb = processUtilisateur(em);		
//		msg = "Nombre de utilisateurs trouvés ="+nb+" - ok pour "+dbName;
//		
//		str.append(msg+"<br/>");
//		logger.info(msg);
//		
//		return null;
//	}
//
//	/**
//	 * Ajout d'une date de creation pour les producteurs
//	 */
//	private int processProducteur(EntityManager em) 
//	{
//		TypedQuery<Producteur> q = em.createQuery("select p from Producteur p ORDER BY p.id DESC",Producteur.class);
//		List<Producteur> ps = q.getResultList();
//		
//		for (int i = 0; i < ps.size(); i++) 
//		{
//			Producteur producteur = ps.get(i);
//			
//			// on détermine la plus vielle date de livraison d'un producteur
//			Date creation = findDateCreationProdcuteur(producteur,em);
//			
//			// Si elle existe, on l'utilise
//			if (creation!=null)
//			{
//				producteur.dateCreation = creation;
//			}
//			// Sinon on utilise celle du producteur ayant l'id juste plus grand (précédent)  
//			else
//			{
//				if (i!=0)
//				{
//					producteur.dateCreation = ps.get(i-1).dateCreation;
//				}
//			}
//		}
//		
//		
//		return ps.size();
//	}
//
//
//	private Date findDateCreationProdcuteur(Producteur producteur, EntityManager em) 
//	{
//		TypedQuery<Date> q = em.createQuery("select p.dateCreation from Contrat p where p.modeleContrat.producteur=:p ORDER BY p.dateCreation",Date.class);
//		q.setParameter("p", producteur);
//		List<Date> ps = q.getResultList();
//		if (ps.size()==0)
//		{
//			return null;
//		}
//		else
//		{
//			return  ps.get(0);
//		}
//	}
//	
//	
//	
//	/**
//	 * Ajout d'une date de creation pour les utilisateurs
//	 */
//	private int processUtilisateur(EntityManager em) 
//	{
//		TypedQuery<Utilisateur> q = em.createQuery("select p from Utilisateur p ORDER BY p.id DESC",Utilisateur.class);
//		List<Utilisateur> ps = q.getResultList();
//		
//		for (int i = 0; i < ps.size(); i++) 
//		{
//			Utilisateur utilisateur = ps.get(i);
//			
//			// on détermine la plus vielle date de contrat de cet utilisateur
//			Date creation = findDateCreationUtilisateur(utilisateur,em);
//			
//			// On determine la date de creation de l'utilisateur ayant l'id juste plus grand (précédent)  
//			Date otherUtilisateur = i==0 ? null : ps.get(i-1).dateCreation;
//			
//			// On determine le min de ces 2 dates 
//			Date ref = min(creation,otherUtilisateur);
//			
//			// Si elle existe, on l'utilise
//			if (ref!=null)
//			{
//				utilisateur.dateCreation = ref;
//			}
//		}
//				
//		return ps.size();
//	}
//
//
//
//	private Date findDateCreationUtilisateur(Utilisateur utilisateur, EntityManager em) 
//	{
//		TypedQuery<Date> q = em.createQuery("select p.dateCreation from Contrat p where p.utilisateur=:u ORDER BY p.dateCreation",Date.class);
//		q.setParameter("u", utilisateur);
//		List<Date> ps = q.getResultList();
//		if (ps.size()==0)
//		{
//			return null;
//		}
//		else
//		{
//			return  ps.get(0);
//		}
//	}
//	
//	
//	// Methodes utilitaires 
//	
//	private Date min(Date d1, Date d2) 
//	{
//		if (d1==null && d2==null)
//		{
//			return null;
//		}
//		
//		if (d1==null)
//		{
//			return d2;
//		}
//		
//		if (d2==null)
//		{
//			return d1;
//		}
//		
//		if (d1.before(d2)) 
//		{
//			return d1;
//		}
//		else
//		{
//			return d2;
//		}
//	}

//	/**
//	 * Application du patch V031
//	 *
//	 */
//	public String applyPatchV031()
//	{
//		StringBuffer str = new StringBuffer();
//		
//		SpecificDbUtils.executeInAllDb(()->patch(str),false);
//		
//		return str.toString();
//	}
//	
//	private Void patch(StringBuffer buf)
//	{
//		String dbName = DbUtil.getCurrentDb().getDbName();
//		
//		msg(buf,"================> Traitement de la base "+dbName);
//		
//		doParametrageEcranListeAdherent(buf);
//		doParametrageEcranGestionContratVierge(buf);
//
//		doMontantMiniCheque(buf);
//		doSuppressDoublonsPaiement(buf);
//		doSuppressDoublonsPermanence(buf);
//		
//		doCalculReglesJoker(buf);
//
//		
//		msg(buf,"================> Fin de traitement de la base "+dbName);
//		
//		return null;
//	}
//
//
//
//
//	// PARTIE 1 
//
//	private void doParametrageEcranListeAdherent(StringBuffer buf) 
//	{
//		msg(buf,"Debut Parametrage Liste Adherent");
//		
//		PEListeAdherent pe = (PEListeAdherent) new ParametresService().loadParamEcran(MenuList.LISTE_ADHERENTS);
//		if (pe.getId()==null)
//		{
//			msg(buf,"Pas de paramétrage à patcher.");
//		}
//		else
//		{
//			pe.accesEcran = compute(pe.canAccessEcran);
//			new ParametresService().update(pe);
//			msg(buf,"Mise à jour du parametrage. Avant = "+pe.canAccessEcran+" Apres = "+pe.accesEcran);
//		}
//		msg(buf,"Fin Parametrage Liste Adherent");
//	}
//
//
//	private PEListeAdherentAccess compute(RoleList canAccessEcran) 
//	{
//		if (canAccessEcran==null)
//		{
//			return PEListeAdherentAccess.TRESORIER;
//		}
//		
//		switch (canAccessEcran) 
//		{
//		case ADHERENT : return PEListeAdherentAccess.ALL;
//		case PRODUCTEUR: return PEListeAdherentAccess.PRODUCTEUR;
//		case REFERENT : return PEListeAdherentAccess.REFERENT;
//		case TRESORIER : return PEListeAdherentAccess.TRESORIER;
//		case ADMIN : return PEListeAdherentAccess.TRESORIER;
//		case MASTER : return PEListeAdherentAccess.TRESORIER;
//
//		default: return PEListeAdherentAccess.TRESORIER;
//		}
//	}	
//	
//	// PARTIE 2
//	
//	private void doParametrageEcranGestionContratVierge(StringBuffer buf) 
//	{
//		msg(buf,"Debut EcranGestionContratVierge");
//		
//		PEGestionContratsVierges pe = (PEGestionContratsVierges) new ParametresService().loadParamEcran(MenuList.GESTION_CONTRAT);
//		
//		int nb = countNumberOfJoker();
//		msg(buf,"Il y a "+nb+" modele de contrat avec joker");
//		
//		if (nb!=0)
//		{
//			pe.gestionJoker = GestionJoker.TOUT_POSSIBLE;
//		}
//		else
//		{
//			pe.gestionJoker = GestionJoker.TOUJOURS_NON;
//		}
//		
//		new ParametresService().update(pe);
//		msg(buf,"Mise à jour du parametrage. Gestion Joker = "+pe.gestionJoker);
//		
//		msg(buf,"Fin EcranGestionContratVierge");
//	}
//	
//
//	@DbRead
//	public int countNumberOfJoker()
//	{
//		EntityManager em = TransactionHelper.getEm();
//		TypedQuery<ModeleContrat> q = em.createQuery("select mc from ModeleContrat mc WHERE mc.typJoker<>:typ",ModeleContrat.class);
//		q.setParameter("typ", TypJoker.SANS_JOKER);
//		return q.getResultList().size();
//	}
//	
//	
//	// PARTIE 3
//	
//	/**
//	 * On va charger tous les contrats de type abonnement, et on va essayer de deviner les regles joker pour chaque contrat
//	 */
//	public void doCalculReglesJoker(StringBuffer buf)
//	{	
//		msg(buf,"Debut CalculReglesJoker");
//		
//		List<Long> idContrats = calculReglesJokersGetContrat(buf);
//		
//		for (Long idContrat : idContrats) 
//		{
//			calculReglesJokersUpdateContrat(buf,idContrat);
//		}
//		
//		msg(buf,"Fin CalculReglesJoker");
//		
//	}
//	
//	
//
//	@DbRead
//	private List<Long> calculReglesJokersGetContrat(StringBuffer buf) 
//	{
//		EntityManager em = TransactionHelper.getEm();
//		
//		// Récuperation de tous les contrats de type abonnement
//		TypedQuery<Long> q = em.createQuery("select c.id from Contrat c WHERE c.modeleContrat.nature=:n",Long.class);
//		q.setParameter("n", NatureContrat.ABONNEMENT);
//		
//		List<Long> ls = q.getResultList();
//		
//		msg(buf,"Il y a "+ls.size()+" contrats de type abonnement");
//		
//		return ls;
//	}
//	
//	@DbWrite
//	private void calculReglesJokersUpdateContrat(StringBuffer buf, Long idContrat) 
//	{
//		EntityManager em = TransactionHelper.getEm();
//		
//		Contrat contrat = em.find(Contrat.class, idContrat);
//		
//		if (contrat.aboInfo==null || contrat.aboInfo.length()==0)
//		{
//			ContratDTO contratDTO = new MesContratsService().loadContrat(contrat.modeleContrat.id, contrat.id);
//			String str =   new ContratAboGuesser(contratDTO).guessContratAboInfo();
//			contrat.aboInfo = str;
//			
//			String details = str==null ? "Not found" : "Success";
//			msg(buf,"Traitement du contrat "+contrat.modeleContrat.nom+" pour "+contrat.utilisateur.nom+" "+contrat.utilisateur.prenom+"  ----------   "+details);	
//		}
//	}
//
//
//	// PARTIE 4
//	
//	/**
//	 * On va charger positionner le montant mini des chèques correctement
//	 */
//	@DbWrite
//	public void doMontantMiniCheque(StringBuffer buf)
//	{	
//		msg(buf,"Debut doMontantMiniCheque");
//		
//		PESaisiePaiement pe = (PESaisiePaiement) new ParametresService().loadParamEcran(MenuList.OUT_SAISIE_PAIEMENT);
//		
//		
//		if (pe.modeCalculPaiement==CalculPaiement.TOUS_EGAUX)
//		{
//			msg(buf,"*****************************************************************************************************");
//			msg(buf,"*****************************************************************************************************");
//			msg(buf,"ATTENTION : mode tous egaux ");
//			msg(buf,"*****************************************************************************************************");
//			return ;
//		}
//		
//		if (pe.montantChequeMiniCalculProposition==0)
//		{
//			msg(buf,"Rien à faire : montantChequeMiniCalculProposition=0");
//			return ;
//		}
//		
//		EntityManager em = TransactionHelper.getEm();
//		
//		msg(buf,"On applique :  montantChequeMiniCalculProposition="+pe.montantChequeMiniCalculProposition);
//		
//		TypedQuery<ModeleContrat> q = em.createQuery("select mc from ModeleContrat mc WHERE mc.gestionPaiement=:gest",ModeleContrat.class);
//		q.setParameter("gest", GestionPaiement.GESTION_STANDARD);
//		for (ModeleContrat modeleContrat : q.getResultList()) 
//		{
//			// On n'ecrase pas si il y a dejà une valeur 
//			if (modeleContrat.montantChequeMiniCalculProposition==0)
//			{
//				modeleContrat.montantChequeMiniCalculProposition = pe.montantChequeMiniCalculProposition;
//			}
//		}
//		
//		
//		msg(buf,"Fin doMontantMiniCheque");
//		
//	}
//	
//	// PARTIE 5
//	
//	@DbWrite
//	private void doSuppressDoublonsPaiement(StringBuffer buf)
//	{
//		msg(buf,"Debut doSuppressDoublonsPaiement");
//		
//		EntityManager em = TransactionHelper.getEm();
//		
//		TypedQuery<Paiement> q = em.createQuery("select p from Paiement p",Paiement.class);
//		
//		List<Paiement> paiements = q.getResultList();
//		List<Paiement> doublons = new ArrayList<>();
//		for (Paiement paiement : paiements)
//		{
//			if (isPaiementDoublon(paiement,paiements,buf))
//			{
//				doublons.add(paiement);
//			}
//		}
//		
//		msg(buf,"Il y a "+doublons.size()+" paiements doublons");
//		
//		for (Paiement paiement : doublons)
//		{
//			em.remove(paiement);
//		}
//	
//		msg(buf,"Fin doSuppressDoublonsPaiement");
//	}
//	
//	
//
//
//	
//	/**
//	 * Un paiement est doublon si il existe un autre paiement 
//	 * avec un id plus petit et meme modelecontratdate et meme utilisateur  
//	 * @param buf 
//	 */
//	private boolean isPaiementDoublon(Paiement paiement, List<Paiement> paiements, StringBuffer buf)
//	{
//		for (Paiement p : paiements)
//		{
//			if ( paiement.id.longValue() > p.id.longValue() && 
//				 paiement.modeleContratDatePaiement.id.longValue()==p.modeleContratDatePaiement.id.longValue() &&
//				 paiement.contrat.id.longValue()==p.contrat.id.longValue() )
//			{
//				
//				msg(buf,"DOUBLON : CONTRAT "+paiement.modeleContratDatePaiement.modeleContrat.nom+" PRODUCTEUR "+paiement.modeleContratDatePaiement.modeleContrat.producteur.nom+" UTILISATEUR "+paiement.contrat.utilisateur.nom);
//				
//				if (paiement.montant!=p.montant)
//				{
//					msg(buf,"ATTENTION 1 DOUBLON AVEC MONTANT DIFFERENT : mnt supprimé ="+paiement.montant+" mnt conservé="+p.montant);
//				}
//				
//				return true;
//			}
//		}
//		return false;
//	}
//	
//	
//	// PARTIE 6
//	
//	@DbWrite
//	private void doSuppressDoublonsPermanence(StringBuffer buf)
//	{
//		msg(buf,"Debut doSuppressDoublonsPermanence");
//		
//		EntityManager em = TransactionHelper.getEm();
//		
//		TypedQuery<PeriodePermanenceUtilisateur> q = em.createQuery("select p from PeriodePermanenceUtilisateur p",PeriodePermanenceUtilisateur.class);
//		
//		List<PeriodePermanenceUtilisateur> ppus = q.getResultList();
//		List<PeriodePermanenceUtilisateur> doublons = new ArrayList<>();
//		for (PeriodePermanenceUtilisateur ppu : ppus)
//		{
//			if (isPermanenceDoublon(ppu,ppus,buf))
//			{
//				doublons.add(ppu);
//			}
//		}
//		
//		msg(buf,"Il y a "+doublons.size()+" permanence utilisateur doublons");
//		
//		for (PeriodePermanenceUtilisateur ppu : doublons)
//		{
//			em.remove(ppu);
//		}
//	
//		msg(buf,"Fin doSuppressDoublonsPermanence");
//	}
//	
//	
//
//
//	
//	/**
//	 * Un paiement est doublon si il existe un autre paiement 
//	 * avec un id plus petit et meme modelecontratdate et meme utilisateur  
//	 * @param buf 
//	 */
//	private boolean isPermanenceDoublon(PeriodePermanenceUtilisateur ppu, List<PeriodePermanenceUtilisateur> ppus, StringBuffer buf)
//	{
//		for (PeriodePermanenceUtilisateur p : ppus)
//		{
//			if ( ppu.id.longValue() > p.id.longValue() && 
//				 ppu.periodePermanence.id.longValue()==p.periodePermanence.id.longValue() &&
//				 ppu.utilisateur.id.longValue()==p.utilisateur.id.longValue() )
//			{
//				
//				msg(buf,"DOUBLON : PERIODE PERMANENCE "+ppu.periodePermanence.nom+" UTILISATEUR "+ppu.utilisateur.nom);
//				
//				return true;
//			}
//		}
//		return false;
//	}
//	
//	
//	/**
//	 *  Recherche des contrats carte prépayée avec gestion des paiements actifs
//	 *
//	 */
//	public String checkCartePepayeeAvecPaiement()
//	{
//		StringBuffer str = new StringBuffer();
//		
//		SpecificDbUtils.executeInAllDb(()->checkCartePepayeeAvecPaiement(str),false);
//		
//		return str.toString();
//	}
//	
//	private Void checkCartePepayeeAvecPaiement(StringBuffer buf)
//	{
//		doCheckCartePepayeeAvecPaiement(buf);		
//		return null;
//	}
//	
//	
//	@DbRead
//	public void doCheckCartePepayeeAvecPaiement(StringBuffer buf)
//	{
//		EntityManager em = TransactionHelper.getEm();
//		TypedQuery<ModeleContrat> q = em.createQuery("select mc from ModeleContrat mc WHERE mc.nature=:nature AND mc.etat<>:etat AND mc.gestionPaiement=:gestionPaiement",ModeleContrat.class);
//		q.setParameter("nature", NatureContrat.CARTE_PREPAYEE);
//		q.setParameter("etat", EtatModeleContrat.ARCHIVE);
//		q.setParameter("gestionPaiement", GestionPaiement.GESTION_STANDARD);
//		
//		
//		if (q.getResultList().size()!=0)
//		{
//			String dbName = DbUtil.getCurrentDb().getDbName();
//			
//			
//			Query q2 = em.createQuery("select distinct(u) from Utilisateur u  where u.id in (select a.utilisateur.id from RoleAdmin a) and u.etatUtilisateur = :etat order by u.nom,u.prenom");
//			q2.setParameter("etat", EtatUtilisateur.ACTIF);
//			List<Utilisateur> us = q2.getResultList();
//			String str = CollectionUtils.asStringFinalSep(us, ",",t->"\""+dbName+"\" <"+t.email+">");
//						
//			logger.info(str);
//			buf.append(str);
//		}
//	}
	
	
//	
//		/**
//		 * Application du patch V036
//		 *
//		 */
//		public String applyPatchV036()
//		{
//			StringBuffer str = new StringBuffer();
//			
//			SpecificDbUtils.executeInAllDb(()->patch(str),false);
//			
//			return str.toString();
//		}
//		
//		private Void patch(StringBuffer buf)
//		{
//			String dbName = DbUtil.getCurrentDb().getDbName();
//			
//			msg(buf,"Traitement de la base "+dbName);
//			
//			doCheckQteSurDateBarrees(buf);
//	
//			
//			//msg(buf,"================> Fin de traitement de la base "+dbName);
//			
//			return null;
//		}
//	
//	
//	
//	
//		
//		
//		// PARTIE 3
//		
//		/**
//		 * 
//		 */
//		@DbRead
//		public void doCheckQteSurDateBarrees(StringBuffer buf)
//		{	
//			List<Long> idModeleContrats = getAllModeleContrats(buf);
//			for (Long idModeleContrat : idModeleContrats) 
//			{
//				checkQteSurDateBarrees(buf,idModeleContrat);
//			}
//		}
//		
//		
//	
//		
//		private List<Long> getAllModeleContrats(StringBuffer buf) 
//		{
//			EntityManager em = TransactionHelper.getEm();
//			
//			// Récuperation de tous les modeles de contrats
//			TypedQuery<Long> q = em.createQuery("select c.id from ModeleContrat c",Long.class);
//			List<Long> ls = q.getResultList();
//						
//			return ls;
//		}
//		
//		private void checkQteSurDateBarrees(StringBuffer buf, Long idModeleContrat) 
//		{
//			EntityManager em = TransactionHelper.getEm();
//			
//			ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);
//			
//			String msg = new DateBarreCheckService().checkCoherenceDateBarreesModeleContrat(idModeleContrat);
//			if (msg!=null)
//			{
//				msg = 	"===================================================================\n"+
//						"Mails="+getMailsAdmin(em)+","+getMailsTresorier(em)+"\n\n"+
//						"Il y a une incohérence pour le modele de contrat :"+mc.nom+"\n"+
//						"Producteur="+mc.producteur.nom+"\n"+
//						"Etat="+mc.etat+"\n"+
//						"Détails : \n"+
//						msg;
//				
//				msg(buf,msg);
//				
//			}
//					
//		}
//	
//	
//	
//
//
//	private String getMailsAdmin(EntityManager em) 
//	{
//		TypedQuery<RoleAdmin> q = em.createQuery("select r from RoleAdmin r",RoleAdmin.class);
//		return q.getResultList().stream().map(e->e.utilisateur.email).distinct().collect(Collectors.joining(","));
//	}
//	
//	private String getMailsTresorier(EntityManager em) 
//	{
//		TypedQuery<RoleTresorier> q = em.createQuery("select r from RoleTresorier r",RoleTresorier.class);
//		return q.getResultList().stream().map(e->e.utilisateur.email).distinct().collect(Collectors.joining(","));
//	}
	
	
	/**
	 * Application du patch V042
	 */
	public String applyPatchV042()
	{
		StringBuffer str = new StringBuffer();
		
		RemoteBaseObjectStorageService ross = new RemoteBaseObjectStorageService(AppConfiguration.getConf());
		
		SpecificDbUtils.executeInAllDb(()->patch(str,ross),false);
		return str.toString();
	}
	
	@DbRead
	private Void patch(StringBuffer str, RemoteBaseObjectStorageService ross)
	{
		RdbLink em = RdbLink.get();
		
		String dbName = DbManager.get().getCurrentDb().getDbName();
		
		em.createQuery("select p from WebPage p");
		List<WebPage> ws = em.result().list(WebPage.class);
		
		msg(str,"Pour la base :"+dbName+" il y a "+ws.size()+" webpage à transférer");

		for (WebPage w : ws)
		{
			// Transfert dans une nouvelle transaction 
			NewTransaction.write(em2->transferWebPage(em2,w.id,str,ross));
		}
		
		str.append("ok pour "+dbName+"<br/>");
		
		return null;
	}


	private void transferWebPage(RdbLink em, Long id, StringBuffer str, RemoteBaseObjectStorageService ross) 
	{
		msg(str,"Début de transfert WebPage id="+id);
		
		WebPage webPage = em.find(WebPage.class, id);
		if (webPage.getHtml()!=null)
		{
			msg(str,"WebPage id="+id+" ignorée, transfert déjà réalisé");
			return;
		}
		
		byte[] html = ross.onlyForPatchLoadWebPageAsHtml(id);
		webPage.setHtml(em, html);
		
		msg(str,"WebPage id="+id+" transfert effectué avec succés size html = "+html.length);
	}



	// PARTIE TECHNIQUE 
	
	/**
	 * Ajout d'un message
	 */
	private void msg(StringBuffer str, String msg) 
	{ 
		str.append(msg.replaceAll("\n", "<br/>")+"<br/>");
		logger.info(msg);
	}

}
