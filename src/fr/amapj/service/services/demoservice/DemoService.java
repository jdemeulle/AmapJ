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
 package fr.amapj.service.services.demoservice;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import fr.amapj.common.DateUtils;
import fr.amapj.common.FormatUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.contrat.modele.EtatModeleContrat;
import fr.amapj.model.models.contrat.modele.GestionPaiement;
import fr.amapj.model.models.contrat.modele.GestionDocEngagement;
import fr.amapj.model.models.contrat.modele.JokerMode;
import fr.amapj.model.models.contrat.modele.NatureContrat;
import fr.amapj.model.models.contrat.modele.RetardataireAutorise;
import fr.amapj.model.models.contrat.modele.SaisiePaiementCalculDate;
import fr.amapj.model.models.contrat.modele.SaisiePaiementModifiable;
import fr.amapj.model.models.contrat.modele.SaisiePaiementProposition;
import fr.amapj.model.models.contrat.modele.StockGestion;
import fr.amapj.model.models.contrat.modele.StrategiePaiement;
import fr.amapj.model.models.contrat.modele.TypJoker;
import fr.amapj.model.models.fichierbase.EtatNotification;
import fr.amapj.model.models.fichierbase.Producteur;
import fr.amapj.model.models.fichierbase.ProducteurReferent;
import fr.amapj.model.models.fichierbase.ProducteurStockGestion;
import fr.amapj.model.models.fichierbase.ProducteurUtilisateur;
import fr.amapj.model.models.fichierbase.Produit;
import fr.amapj.model.models.fichierbase.RoleAdmin;
import fr.amapj.model.models.fichierbase.RoleMaster;
import fr.amapj.model.models.fichierbase.RoleTresorier;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.model.models.param.ChoixOuiNon;
import fr.amapj.model.models.param.EtatModule;
import fr.amapj.model.models.param.Parametres;
import fr.amapj.model.models.param.paramecran.PEExtendedParametres;
import fr.amapj.model.models.saas.AppInstance;
import fr.amapj.model.models.saas.StateOnStart;
import fr.amapj.model.models.saas.TypDbExemple;
import fr.amapj.service.services.appinstance.AppInstanceDTO;
import fr.amapj.service.services.authentification.PasswordManager;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.gestioncontrat.LigneContratDTO;
import fr.amapj.service.services.gestioncontrat.ModeleContratDTO;
import fr.amapj.service.services.gestioncontrat.ModeleContratSummaryDTO;
import fr.amapj.service.services.gestioncotisation.GestionCotisationService;
import fr.amapj.service.services.gestioncotisation.PeriodeCotisationDTO;
import fr.amapj.service.services.mesadhesions.AdhesionDTO;
import fr.amapj.service.services.mesadhesions.MesAdhesionDTO;
import fr.amapj.service.services.mesadhesions.MesAdhesionsService;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.views.gestioncontrat.editorpart.FrequenceLivraison;
import fr.amapj.view.views.gestioncontrat.editorpart.utils.GestionContratDateUtils;

/**
 * Ce service permet de creer des données pour la base de démonstration (création de contrat)
 * 
 * 
 */
public class DemoService
{
	
	@DbWrite
	public Void generateDemoData(AppInstanceDTO dto)
	{
		RdbLink em = RdbLink.get();

		createParamGeneraux(em, dto);
		
		if (dto.typDbExemple==TypDbExemple.BASE_EXEMPLE)
		{
			createUtilisateurs(em,dto.password);
			
			createRoleUtilisateur(em);

			createProducteur(em);

			createProduit(em);
			
			Long idPeriodeCotisation = createPeriodeCotisation(em,dto.dateDebut, dto.dateFin);
			
			createCotisation(idPeriodeCotisation);	
	
			createContrat(em, dto.dateDebut, dto.dateFin, dto.dateFinInscription,idPeriodeCotisation);
			
		}
		else if (dto.typDbExemple==TypDbExemple.BASE_MANUAL_TEST)
		{
			createUtilisateurs(em,dto.password);
			
			createRoleUtilisateur(em);
	
			createProducteur(em);
	
			createProduit(em);
			
			Long idPeriodeCotisation = createPeriodeCotisation(em,dto.dateDebut, dto.dateFin);
	
			createContratManualTest(em, dto.dateDebut, dto.dateFin, dto.dateFinInscription,idPeriodeCotisation);
			
			createCotisation(idPeriodeCotisation);
		}
		else if (dto.typDbExemple==TypDbExemple.BASE_MINIMALE)
		{
			createOneAdminUtilisateur(em,dto);
		}
		else if (dto.typDbExemple==TypDbExemple.BASE_MASTER)
		{
			if (dto.user1Nom!=null)
			{
				createOneMasterUtilisateur(em,dto);
			}
			createOneAppInstance(em,dto);
			createPEStatusPageAsDemo(dto);
		} 
		
		return null;
	}


	private void createParamGeneraux(RdbLink em, AppInstanceDTO dto)
	{
		Parametres p = new Parametres();
		
		p.setId(1L);
		
		// Etape 1 - Nom et ville Amap
		p.nomAmap = dto.nomAmap;
		p.villeAmap = dto.villeAmap;
		
		// Etape 2 - Les mails 
		p.smtpType = dto.smtpType;
		p.sendingMailUsername = dto.adrMailSrc;
		p.sendingMailPassword = "";
		p.sendingMailFooter = "Merci de ne pas répondre à ce message.<br/>Pour toute demande, merci de contacter votre Amap avec son mail habituel.<br/>";
		p.sendingMailNbMax = dto.nbMailMax;
		p.url = dto.url;
		p.mailCopyTo = "";
		p.backupReceiver = "";
		
		// Etape 3
		p.etatPlanningDistribution = EtatModule.INACTIF;
		p.envoiMailRappelPermanence = ChoixOuiNon.NON;
		
		// Etape 4
		p.envoiMailPeriodique = ChoixOuiNon.NON;
		
		// Etape 5
		p.etatGestionCotisation = EtatModule.ACTIF;
		
		//
		p.etatGestionStock = EtatModule.INACTIF;
		
		// Parametres d'archivage 
		p.archivageContrat=180;
		p.suppressionContrat=730;
		p.archivageUtilisateur=90;
		p.archivageProducteur=365;
		p.suppressionPeriodePermanence=730;
		p.suppressionPeriodeCotisation=1095;
		
		em.persist(p);

	}
	
	// DEMO CLASSIQUE 

	private void createUtilisateurs(RdbLink em,String password)
	{
		insertUtilisateur(em, 1051, "TREMBLAY", "Antonin", "antonin.tremblay@example.fr", password);
		insertUtilisateur(em, 1052, "DUPUIS", "Romain", "romain.dupuis@example.fr", password);
		insertUtilisateur(em, 1507, "DUBOIS", "Rémi", "remi.dubois@example.fr", password);
		insertUtilisateur(em, 1508, "GAGNON", "Magali", "magali.gagnon@example.fr", password);
		insertUtilisateur(em, 1509, "ROY", "Gaelle", "gaelle.roy@example.fr", password);
		insertUtilisateur(em, 1510, "CÔTÉ", "Nathalie", "nathalie.cote@example.fr", password);
		insertUtilisateur(em, 1511, "BOUCHARD", "Benjamin", "benjamin.bouchard@example.fr", password);
		insertUtilisateur(em, 1512, "GAUTHIER", "Alex", "alex.gauthier@example.fr", password);
		insertUtilisateur(em, 1513, "MORIN", "Karine", "karine.morin@example.fr", password);
		insertUtilisateur(em, 1514, "LAVOIE", "Arthur", "arthur.lavoie@example.fr", password);
		insertUtilisateur(em, 1515, "FORTIN", "Sophie", "sophie.fortin@example.fr", password);
		insertUtilisateur(em, 1517, "OUELLET", "Mathis", "mathis.ouellet@example.fr", password);
		insertUtilisateur(em, 1518, "PELLETIER", "Matthieu", "matthieu.pelletier@example.fr", password);
		insertUtilisateur(em, 1519, "BÉLANGER", "David", "david.belanger@example.fr", password);
		insertUtilisateur(em, 1520, "LÉVESQUE", "Joelle", "joelle.levesque@example.fr", password);
		insertUtilisateur(em, 1522, "BERGERON", "Nadege", "nadege.bergeron@example.fr", password);
		insertUtilisateur(em, 1601, "LEBLANC", "Jeanne", "jeanne.leblanc@example.fr", password);
		insertUtilisateur(em, 1602, "PAQUETTE", "Emeline", "emeline.paquette@example.fr", password);
		insertUtilisateur(em, 1603, "GIRARD", "Florent", "florent.girard@example.fr", password);
		insertUtilisateur(em, 1604, "SIMARD", "Pascal", "pascal.simard@example.fr", password);
		insertUtilisateur(em, 1651, "BOUCHER", "Charles", "charles.boucher@example.fr", password);
		insertUtilisateur(em, 1652, "CARON", "Jean-Luc", "jean-luc.caron@example.fr", password);
		insertUtilisateur(em, 1653, "BEAULIEU", "Mylène", "mylene.beaulieu@example.fr", password);
		insertUtilisateur(em, 1654, "CLOUTIER", "Nadine", "nadine.cloutier@example.fr", password);
		insertUtilisateur(em, 1655, "DUBÉ", "Marine", "marine.dube@example.fr", password);
		insertUtilisateur(em, 1656, "POIRIER", "Frédéric", "frederic.poirier@example.fr", password);
		insertUtilisateur(em, 1657, "FOURNIER", "Yves", "yves.fournier@example.fr", password);
		insertUtilisateur(em, 1659, "LAPOINTE", "Bruno", "bruno.lapointe@example.fr", password);

	}
	
	
	private void createRoleUtilisateur(RdbLink em)
	{
		RoleTresorier rt = new RoleTresorier();
		rt.utilisateur = em.find(Utilisateur.class, new Long(1051));
		em.persist(rt);
		
		RoleAdmin ra = new RoleAdmin();
		ra.utilisateur = em.find(Utilisateur.class, new Long(1052));
		em.persist(ra);
		
	}

	private void insertUtilisateur(RdbLink em, int id, String nom, String prenom, String email, String password)
	{
		Utilisateur u = new Utilisateur();
		
		u.setId(new Long(id));
		u.nom = nom;
		u.prenom = prenom;
		u.email = email;
		u.dateCreation = DateUtils.suppressTime(DateUtils.getDate());
		
		em.persist(u);
		
		// Mot de passe 
		new PasswordManager().setUserPassword(u.getId(), password);
		
		
		
		
	}
	
	private void createOneAdminUtilisateur(RdbLink em, AppInstanceDTO dto)
	{
		insertUtilisateur(em, 1052, dto.user1Nom, dto.user1Prenom, dto.user1Email, dto.password);
		
		RoleAdmin ra = new RoleAdmin();
		ra.utilisateur = em.find(Utilisateur.class, new Long(1052));
		em.persist(ra);
		
	}
	
	
	private void createOneMasterUtilisateur(RdbLink em, AppInstanceDTO dto)
	{
		insertUtilisateur(em, 1052, dto.user1Nom, dto.user1Prenom, dto.user1Email, dto.password);
		
		RoleMaster ra = new RoleMaster();
		ra.utilisateur = em.find(Utilisateur.class, new Long(1052));
		em.persist(ra);
		
	}
	
	
	
	private void createOneAppInstance(RdbLink em, AppInstanceDTO dto)
	{
		AppInstance app = new AppInstance();
		
		app.dateCreation = DateUtils.getDate();
		app.nomInstance = "amap1";
		app.dbUserName = dto.dbUserName;
		app.dbPassword = dto.dbPassword;
		app.stateOnStart = StateOnStart.ON_START_BE_ON;
		em.persist(app);
		
	}
	

	private void createPEStatusPageAsDemo(AppInstanceDTO dto) 
	{
		SimpleDateFormat df = FormatUtils.getLiteralMonthDate();
		String str = "<ul>"
					+ "<li>Type de la base de données : base de DEMO</li>"
					+ "<li>Login à utiliser : romain.dupuis@example.fr</li>"
					+ "<li>Mot de passe à utiliser : a</li>"
					+ "</ul>";
		PEExtendedParametres pe = (PEExtendedParametres) new ParametresService().loadParamEcran(MenuList.EXTENDED_PARAMETRES);
		pe.masterDbLibAmap1 = str;
		new ParametresService().update(pe);
	}


	
	

	private void createProducteur(RdbLink em)
	{
		createProducteur(em,3002,"FERME DES CHEVRES",1601,1522);
		createProducteur(em,3011,"EARL LAIT VACHE",1653,1513);
		createProducteur(em,3019,"FERME des BREBIS",1515,1509);
		createProducteur(em,3029,"EARL du PAIN De SUC",1508,1514);
		createProducteur(em,3036,"GAEC du BEAU LEGUME",1656,1657);
		
	
	}

	private void createProducteur(RdbLink em, int idProducteur, String nomProducteur, int idUtilisateur, int idReferent)
	{
		Producteur p = new Producteur();
		p.setId(new Long(idProducteur));
		p.nom = nomProducteur;
		p.delaiModifContrat = 3;
		p.feuilleDistributionGrille = ChoixOuiNon.OUI;
		p.feuilleDistributionListe = ChoixOuiNon.NON;
		p.dateCreation = DateUtils.suppressTime(DateUtils.getDate());
		p.gestionStock = ProducteurStockGestion.NON;
		
		em.persist(p);
		
		ProducteurUtilisateur pu  =new ProducteurUtilisateur();
		pu.notification = EtatNotification.SANS_NOTIFICATION_MAIL;
		pu.producteur = p;
		pu.utilisateur = em.find(Utilisateur.class, new Long(idUtilisateur));
		
		em.persist(pu);
		
		ProducteurReferent pr = new ProducteurReferent();
		pr.producteur = p;
		pr.referent = em.find(Utilisateur.class, new Long(idReferent));
		pr.notification = EtatNotification.SANS_NOTIFICATION_MAIL;
		
		em.persist(pr);
	}

	private void createProduit(RdbLink em)
	{
		insertProduit(em,3003,"Tomme de chèvre - blanc","la pièce", 3002);
		insertProduit(em,3004,"Tomme de chèvre - crémeux","la pièce", 3002);
		insertProduit(em,3005,"Tomme de chèvre - sec","la pièce", 3002);
		insertProduit(em,3006,"Faisselle","le pot de 500 g", 3002);
		insertProduit(em,3007,"Yaourt","le pot de 140 g", 3002);
		insertProduit(em,3008,"Dessert lacté parfumé","le pot de 140 g", 3002);
		insertProduit(em,3009,"Savon au lait de chèvre","la pièce", 3002);
		insertProduit(em,3010,"Tomme pressée","la pièce de 200/230 g", 3002);
		
		
		insertProduit(em,3012,"Lait","le litre", 3011);
		insertProduit(em,3013,"Yaourt nature","le pot de 500 g", 3011);
		insertProduit(em,3014,"Yaourt nature","le pot de 1 kg", 3011);
		insertProduit(em,3015,"Yaourt aux fruits","le pot de 500 g", 3011);
		insertProduit(em,3016,"Faisselle","le pot de 1 kg", 3011);
		insertProduit(em,3017,"Crème fraiche","le pot de 25 cl", 3011);
		insertProduit(em,3018,"Confiture de lait","le pot de 500 g", 3011);
		
		
		insertProduit(em,3020,"Lait","le litre", 3019);
		insertProduit(em,3023,"Yaourt nature","le pot de 300 g", 3019);
		insertProduit(em,3024,"Yaourt vanille","le pot de 300 g", 3019);
		insertProduit(em,3025,"Tommette","la pièce de 100 g", 3019);
		insertProduit(em,3026,"Tomme pressée","la pièce entière (environ 700 g)", 3019);
		insertProduit(em,3027,"Tomme pressée","la 1/2 pièce (environ 350 g)", 3019);
		insertProduit(em,3028,"Tomme pressée","le 1/4 de pièce (environ 175 g)", 3019);
		insertProduit(em,4340,"Féta","la pièce entière (environ 400 g)", 3019);
		insertProduit(em,4341,"Féta","la 1/2 pièce (environ 200 g)", 3019);
		
		
		insertProduit(em,3030,"Pain de blé","la pièce de 900 g", 3029);
		insertProduit(em,3031,"Pain de blé moulé","la pièce de 900 g", 3029);
		insertProduit(em,3032,"Pain de campagne","la pièce de 900 g", 3029);
		insertProduit(em,3033,"Pain de campagne moulé","la pièce de 900 g ", 3029);
		insertProduit(em,3034,"Pain de seigle","la pièce de 900 g", 3029);
		insertProduit(em,3035,"Pain de seigle moulé","la pièce de 900 g ", 3029);
		
		insertProduit(em,3037,"Petit panier de légumes","la pièce", 3036);
		insertProduit(em,3038,"Grand panier de légumes","la pièce", 3036);


	}

	private void insertProduit(RdbLink em, int idProduit, String nom, String cond, int idProducteur)
	{
		Produit p = new Produit();
		
		p.setId(new Long(idProduit));
		p.nom = nom;
		p.conditionnement = cond;
		p.producteur = em.find(Producteur.class, new Long(idProducteur));
		
		em.persist(p);
		
	}
	
	
	private Long createPeriodeCotisation(RdbLink em, Date dateDebut, Date dateFin) 
	{
		Date debutPeriode;
		Date finPeriode;
		String nom;
		
		int y1 = DateUtils.getYear(dateDebut);
		int y2 = DateUtils.getYear(dateFin);
		if (y1==y2)
		{
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, y1);
			c.set(Calendar.MONTH, Calendar.JANUARY);
			c.set(Calendar.DAY_OF_MONTH, 1);
			
			debutPeriode = c.getTime();
			
			c.set(Calendar.MONTH, Calendar.DECEMBER);
			c.set(Calendar.DAY_OF_MONTH, 31);
			
			finPeriode = c.getTime();
			
			nom = "Saison "+y1;
		}
		else
		{
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, y1);
			c.set(Calendar.MONTH, Calendar.JULY);
			c.set(Calendar.DAY_OF_MONTH, 1);
			
			debutPeriode = c.getTime();
			
			c.set(Calendar.YEAR, y2);
			c.set(Calendar.MONTH, Calendar.JUNE);
			c.set(Calendar.DAY_OF_MONTH, 30);
			
			finPeriode = c.getTime();
			
			nom = "Saison "+y1+"/"+y2;
		}
		PeriodeCotisationDTO dto = new PeriodeCotisationDTO();
		
		dto.dateDebut = debutPeriode;
		dto.dateFin = finPeriode;
		dto.nom = nom;
		dto.libCheque = "lib cheque AMAP";
		dto.textPaiement = "";
		dto.montantMini = 800;
		dto.montantConseille = 1500;
		
		try 
		{
			return new GestionCotisationService().createOrUpdate(dto);
		}
		catch (OnSaveException e) 
		{
			throw new RuntimeException(e);
		}
	}
	

	private void createContrat(RdbLink em, Date dateDebut, Date dateFin, Date dateFinInscription,Long idPeriodeCotisation)
	{
		createContrat(em, "LEGUMES", "légumes bio et variés", 3036L, dateFinInscription, dateDebut, dateFin,idPeriodeCotisation,NatureContrat.ABONNEMENT);

		createContrat(em, "PRODUITS LAITIERS de VACHE", "lait, yaourts, faisselle, crème fraîche", 3011L, dateFinInscription, dateDebut, dateFin,idPeriodeCotisation,NatureContrat.LIBRE);

		createContrat(em, "PAIN", "pains complet, campagne ou seigle", 3029L, dateFinInscription, dateDebut, dateFin,idPeriodeCotisation,NatureContrat.LIBRE);

		
		// Ces deux contrats ne sont pas créés, ils seront créés par l'utilisateur en formation  
		// createContrat(em, "PRODUITS LAITIERS de CHEVRE", "fromages, yaourts, faisselles, savons", 3002L, dateFinInscription, dateDebut, dateFin,idPeriodeCotisation,NatureContrat.LIBRE);
		// createContrat(em, "PRODUITS LAITIERS de BREBIS", "lait, yaourts, et fromages de brebis", 3019L, dateFinInscription, dateDebut, dateFin,idPeriodeCotisation,NatureContrat.LIBRE);

		setAllContratActifs();

	}

	/**
	 * Positionne tous les contrats actifs
	 */
	private void setAllContratActifs()
	{
		GestionContratService service = new GestionContratService();
		List<ModeleContratSummaryDTO> modeles = service.getModeleContratInfo(EtatModeleContrat.CREATION);
		for (ModeleContratSummaryDTO dto : modeles)
		{
			service.updateEtat(EtatModeleContrat.ACTIF, dto.id);
		}

	}

	private void createContrat(RdbLink em, String nom, String description, Long idProducteur, Date dateFinInscription, Date dateDebut, Date dateFin,Long idPeriodeCotisation,NatureContrat nature)
	{
		ModeleContratDTO modeleContrat = new ModeleContratDTO();
		modeleContrat.nom = nom;
		modeleContrat.description = description;
		modeleContrat.producteur = idProducteur;
		modeleContrat.dateFinInscription = dateFinInscription;
		modeleContrat.frequence = FrequenceLivraison.UNE_FOIS_PAR_SEMAINE;
		
		//
		modeleContrat.strategiePaiement = StrategiePaiement.UN_CHEQUE_PAR_MOIS_LISSE_MODIFIABLE_1ERE_LIVRAISON;
		modeleContrat.gestionPaiement = GestionPaiement.GESTION_STANDARD;
		modeleContrat.saisiePaiementCalculDate = SaisiePaiementCalculDate.UN_PAIEMENT_PAR_MOIS_PREMIERE_LIVRAISON;
		modeleContrat.saisiePaiementModifiable = SaisiePaiementModifiable.MODIFIABLE;
		modeleContrat.saisiePaiementProposition = SaisiePaiementProposition.REPARTI_ARRONDI;
		modeleContrat.montantChequeMiniCalculProposition = 1500;
		
		modeleContrat.nature = nature;
		modeleContrat.typJoker = TypJoker.SANS_JOKER;
		modeleContrat.jokerMode = JokerMode.INSCRIPTION;
		modeleContrat.retardataireAutorise = RetardataireAutorise.NON;
		modeleContrat.stockGestion = StockGestion.NON;
		modeleContrat.gestionDocEngagement = GestionDocEngagement.AUCUNE_GESTION;

		modeleContrat.dateDebut = dateDebut;
		modeleContrat.dateFin = dateFin;
		new GestionContratDateUtils().computeDateLivraison(modeleContrat);

		modeleContrat.produits = getProduits(idProducteur.intValue(), em);

		modeleContrat.libCheque = em.find(Producteur.class, idProducteur).nom.toLowerCase();
		modeleContrat.dateRemiseCheque = dateFinInscription;
		
		new GestionContratDateUtils().computeDatePaiement(modeleContrat);
		
		modeleContrat.idPeriodeCotisation = idPeriodeCotisation;

		new GestionContratService().saveNewModeleContrat(modeleContrat);

	}

	private List<LigneContratDTO> getProduits(int idProducteur, RdbLink em)
	{
		List<LigneContratDTO> res = new ArrayList<>();

		switch (idProducteur)
		{
		// VACHE
		case 3011:
			add(res, em, 120, 3012);
			add(res, em, 200, 3013);
			add(res, em, 390, 3014);
			add(res, em, 250, 3015);
			add(res, em, 340, 3016);
			add(res, em, 205, 3017);
			break;

		// PAIN
		case 3029:
			add(res, em, 400, 3030);
			add(res, em, 400, 3031);
			add(res, em, 400, 3032);
			add(res, em, 400, 3033);
			add(res, em, 400, 3034);
			add(res, em, 400, 3035);
			break;
		// CHEVRE
		case 3002:

			add(res, em, 130, 3003);
			add(res, em, 130, 3004);
			add(res, em, 130, 3005);
			add(res, em, 240, 3006);
			add(res, em, 70, 3007);
			add(res, em, 80, 3008);
			add(res, em, 350, 3009);
			add(res, em, 440, 3010);
			break;
		// BREBIS
		case 3019:
			add(res, em, 350, 3020);
			add(res, em, 250, 3023);
			add(res, em, 270, 3024);
			add(res, em, 160, 3025);
			add(res, em, 1500, 3026);
			add(res, em, 750, 3027);
			add(res, em, 375, 3028);
			add(res, em, 1000, 4340);
			add(res, em, 500, 4341);
			break;
			
		// LEGUME
		case 3036:
			add(res, em, 1200, 3037);
			add(res, em, 2000, 3038);
			break;

		default:
			break;
		}

		return res;
	}

	private void add(List<LigneContratDTO> res, RdbLink em, int prix, int idProduit)
	{
		LigneContratDTO dto = new LigneContratDTO();

		dto.prix = prix;
		dto.produitId = new Long(idProduit);

		res.add(dto);

	}
	
	
	private void createCotisation(Long idPeriodeCotisation) 
	{		  
		insertCotisation(1051, idPeriodeCotisation);
		insertCotisation(1052, idPeriodeCotisation);
		insertCotisation(1509, idPeriodeCotisation);
		insertCotisation(1513, idPeriodeCotisation);
		insertCotisation(1514, idPeriodeCotisation);
		insertCotisation(1522, idPeriodeCotisation);
		insertCotisation(1659, idPeriodeCotisation);	
	}

	private void insertCotisation(int idUtilisateur, Long idPeriodeCotisation) 
	{
		Long userId = (long) idUtilisateur;
		MesAdhesionDTO mesAdhesionDTO = new MesAdhesionsService().computeAdhesionInfo(userId);
		if (mesAdhesionDTO.nouvelles.size()==0)
		{
			return ;
		}
		
		AdhesionDTO adhesionDTO = mesAdhesionDTO.nouvelles.get(0);
		new MesAdhesionsService().createOrUpdateAdhesion(adhesionDTO, 1500);
		
	}
	
	
	// MANUAL TEST - CONTRATS
	
	private void createContratManualTest(RdbLink em, Date dateDebut, Date dateFin, Date dateFinInscription,Long idPeriodeCotisation)
	{

		createContratManualTestAbo(em, "11 - ABO SANS RETARDATAIRE", "vache - lait, yaourts, faisselle, crème fraîche", 3011L, dateFinInscription, dateDebut, dateFin,idPeriodeCotisation,RetardataireAutorise.NON);
		
		createContratManualTestAbo(em, "12 - ABO AVEC RETARDATAIRE", "pains complet, campagne ou seigle", 3029L, dateFinInscription, dateDebut, dateFin,idPeriodeCotisation,RetardataireAutorise.OUI);
		
		createContratManualTestLibre(em, "21 - LIBRE SANS RETARDATAIRE",  "vache - lait, yaourts, faisselle, crème fraîche", 3011L, dateFinInscription, dateDebut, dateFin,idPeriodeCotisation,RetardataireAutorise.NON);
		
		createContratManualTestLibre(em, "22 - LIBRE AVEC RETARDATAIRE", "pains complet, campagne ou seigle", 3029L, dateFinInscription, dateDebut, dateFin,idPeriodeCotisation,RetardataireAutorise.OUI);

		createContratManualTestPrepayee(em, "31 - PREPAYEE", "chevres - fromages, yaourts, faisselles, savons", 3002L, dateFinInscription, dateDebut, dateFin,idPeriodeCotisation);

		setAllContratActifs();

	}
	
	
	private void createContratManualTestAbo(RdbLink em, String nom, String description, Long idProducteur, Date dateFinInscription, Date dateDebut, Date dateFin,Long idPeriodeCotisation,
			RetardataireAutorise retardataireAutorise)
	{
		ModeleContratDTO modeleContrat = new ModeleContratDTO();
		modeleContrat.nom = nom;
		modeleContrat.description = description;
		modeleContrat.producteur = idProducteur;
		modeleContrat.dateFinInscription = dateFinInscription;
		modeleContrat.frequence = FrequenceLivraison.UNE_FOIS_PAR_SEMAINE;
		modeleContrat.gestionPaiement = GestionPaiement.GESTION_STANDARD;
		modeleContrat.nature = NatureContrat.ABONNEMENT;
		modeleContrat.jokerMode = JokerMode.LIBRE;
		modeleContrat.jokerDelai = 4;
		modeleContrat.jokerNbMin = 2;
		modeleContrat.jokerNbMax = 3;
		modeleContrat.retardataireAutorise = retardataireAutorise;

		modeleContrat.dateDebut = dateDebut;
		modeleContrat.dateFin = dateFin;
		new GestionContratDateUtils().computeDateLivraison(modeleContrat);

		modeleContrat.produits = getProduits(idProducteur.intValue(), em);

		modeleContrat.libCheque = em.find(Producteur.class, idProducteur).nom.toLowerCase();
		modeleContrat.dateRemiseCheque = dateFinInscription;
		new GestionContratDateUtils().computeDatePaiement(modeleContrat);
		
		modeleContrat.idPeriodeCotisation = idPeriodeCotisation;

		new GestionContratService().saveNewModeleContrat(modeleContrat);

	}
	
	
	private void createContratManualTestLibre(RdbLink em, String nom, String description, Long idProducteur, Date dateFinInscription, Date dateDebut, Date dateFin,Long idPeriodeCotisation,
			RetardataireAutorise retardataireAutorise)
	{
		ModeleContratDTO modeleContrat = new ModeleContratDTO();
		modeleContrat.nom = nom;
		modeleContrat.description = description;
		modeleContrat.producteur = idProducteur;
		modeleContrat.dateFinInscription = dateFinInscription;
		modeleContrat.frequence = FrequenceLivraison.UNE_FOIS_PAR_SEMAINE;
		modeleContrat.gestionPaiement = GestionPaiement.GESTION_STANDARD;
		modeleContrat.nature = NatureContrat.LIBRE;
		modeleContrat.jokerMode = JokerMode.INSCRIPTION;
		modeleContrat.retardataireAutorise = retardataireAutorise;

		modeleContrat.dateDebut = dateDebut;
		modeleContrat.dateFin = dateFin;
		new GestionContratDateUtils().computeDateLivraison(modeleContrat);

		modeleContrat.produits = getProduits(idProducteur.intValue(), em);

		modeleContrat.libCheque = em.find(Producteur.class, idProducteur).nom.toLowerCase();
		modeleContrat.dateRemiseCheque = dateFinInscription;
		new GestionContratDateUtils().computeDatePaiement(modeleContrat);
		
		modeleContrat.idPeriodeCotisation = idPeriodeCotisation;

		new GestionContratService().saveNewModeleContrat(modeleContrat);

	}
	
	
	private void createContratManualTestPrepayee(RdbLink em, String nom, String description, Long idProducteur, Date dateFinInscription, Date dateDebut, Date dateFin,Long idPeriodeCotisation)
			
	{
		ModeleContratDTO modeleContrat = new ModeleContratDTO();
		modeleContrat.nom = nom;
		modeleContrat.description = description;
		modeleContrat.producteur = idProducteur;
		modeleContrat.dateFinInscription = dateFinInscription;
		modeleContrat.frequence = FrequenceLivraison.UNE_FOIS_PAR_SEMAINE;
		modeleContrat.gestionPaiement = GestionPaiement.GESTION_STANDARD;
		modeleContrat.nature = NatureContrat.CARTE_PREPAYEE;
		modeleContrat.cartePrepayeeDelai = 4;
		modeleContrat.jokerMode = JokerMode.INSCRIPTION;
		modeleContrat.retardataireAutorise = RetardataireAutorise.NON;

		modeleContrat.dateDebut = dateDebut;
		modeleContrat.dateFin = dateFin;
		new GestionContratDateUtils().computeDateLivraison(modeleContrat);

		modeleContrat.produits = getProduits(idProducteur.intValue(), em);

		modeleContrat.libCheque = em.find(Producteur.class, idProducteur).nom.toLowerCase();
		modeleContrat.dateRemiseCheque = dateFinInscription;
		new GestionContratDateUtils().computeDatePaiement(modeleContrat);
		
		modeleContrat.idPeriodeCotisation = idPeriodeCotisation;

		new GestionContratService().saveNewModeleContrat(modeleContrat);

	}
	
}
