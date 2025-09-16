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
 package fr.amapj.view.engine.menu;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.navigator.View;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.models.acces.RoleList;
import fr.amapj.model.models.param.EtatModule;
import fr.amapj.model.models.param.paramecran.PELivraisonAmapien;
import fr.amapj.model.models.param.paramecran.PELivraisonAmapien.PELivraisonAmapienAccess;
import fr.amapj.model.models.param.paramecran.PEReceptionCheque;
import fr.amapj.model.models.param.paramecran.PEReceptionCheque.PEReceptionChequeAccess;
import fr.amapj.model.models.param.paramecran.PERemiseProducteur;
import fr.amapj.model.models.param.paramecran.PERemiseProducteur.PERemiseProducteurAccess;
import fr.amapj.model.models.param.paramecran.common.AbstractParamEcran;
import fr.amapj.model.models.param.paramecran.common.ParamEcranConverter;
import fr.amapj.model.models.param.paramecran.producteur.PELivraisonProducteur;
import fr.amapj.model.models.param.paramecran.producteur.PELivraisonProducteur.PELivraisonProducteurAccess;
import fr.amapj.service.services.advanced.tenantnotification.TenantNotificationService;
import fr.amapj.service.services.parametres.ParamEcranDTO;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.session.SessionManager;
import fr.amapj.view.views.advanced.devtools.DevToolsView;
import fr.amapj.view.views.advanced.maintenance.MaintenanceView;
import fr.amapj.view.views.advanced.supervision.SupervisionView;
import fr.amapj.view.views.advanced.tenantnotification.TenantNotificationListPart;
import fr.amapj.view.views.appinstance.AppInstanceListPart;
import fr.amapj.view.views.archivage.contrat.ArchivageContratListPart;
import fr.amapj.view.views.archivage.gestion.GestionArchivageView;
import fr.amapj.view.views.archivage.producteur.ArchivageProducteurListPart;
import fr.amapj.view.views.archivage.utilisateur.ArchivageUtilisateurListPart;
import fr.amapj.view.views.compte.MonCompteView;
import fr.amapj.view.views.contratsamapien.ContratsAmapienListPart;
import fr.amapj.view.views.cotisation.bilan.BilanCotisationView;
import fr.amapj.view.views.cotisation.reception.ReceptionCotisationView;
import fr.amapj.view.views.droits.DroitsAdministrateurListPart;
import fr.amapj.view.views.droits.DroitsTresorierListPart;
import fr.amapj.view.views.editionspe.EditionSpeListPart;
import fr.amapj.view.views.gestioncontrat.listpart.GestionContratListPart;
import fr.amapj.view.views.gestioncontratsignes.GestionContratSignesListPart;
import fr.amapj.view.views.historiquecontrats.HistoriqueContratsView;
import fr.amapj.view.views.historiquepaiements.HistoriquePaiementsView;
import fr.amapj.view.views.importdonnees.ImportDonneesView;
import fr.amapj.view.views.listeadherents.ListeAdherentsView;
import fr.amapj.view.views.listeproducteurreferent.ListeProducteurReferentView;
import fr.amapj.view.views.livraisonamapien.LivraisonAmapienView;
import fr.amapj.view.views.logview.LogView;
import fr.amapj.view.views.logview.StatAccessView;
import fr.amapj.view.views.mesadhesions.MesAdhesionsView;
import fr.amapj.view.views.mescontrats.MesContratsView;
import fr.amapj.view.views.meslivraisons.MesLivraisonsView;
import fr.amapj.view.views.mespaiements.MesPaiementsView;
import fr.amapj.view.views.parametres.ParametresView;
import fr.amapj.view.views.permanence.detailperiode.DetailPeriodePermanenceListPart;
import fr.amapj.view.views.permanence.mespermanences.MesPermanencesView;
import fr.amapj.view.views.permanence.periode.PeriodePermanenceListPart;
import fr.amapj.view.views.permanence.permanencerole.PermanenceRoleListPart;
import fr.amapj.view.views.producteur.basicform.ProducteurListPart;
import fr.amapj.view.views.producteur.contrats.ProducteurContratListPart;
import fr.amapj.view.views.producteur.livraison.ProducteurLivraisonsView;
import fr.amapj.view.views.produit.ProduitListPart;
import fr.amapj.view.views.receptioncheque.ReceptionChequeListPart;
import fr.amapj.view.views.remiseproducteur.RemiseProducteurListPart;
import fr.amapj.view.views.sendmail.SendMailView;
import fr.amapj.view.views.suiviacces.SuiviAccesView;
import fr.amapj.view.views.synthesemulticontrat.SyntheseMultiContratView;
import fr.amapj.view.views.utilisateur.UtilisateurListPart;
import fr.amapj.view.views.visiteamap.VisiteAmapView;

/**
 * Contient la description de chaque menu
 *
 */
public class MenuInfo 
{
	private List<MenuDescription> menus = new ArrayList<MenuDescription>();
	private ParametresDTO param;
	private List<ParamEcranDTO> dtos;
	private List<RoleList> roles;
	

	/**
	 * Retourne la liste des menus accessibles par l'utilisateur courant
	 * 
	 */
	public List<MenuDescription> getMenu()
	{
		menus = new ArrayList<MenuDescription>();
		param = new ParametresService().getParametres();
		dtos = new ParametresService().getAllParamEcranDTO();
		roles = SessionManager.getSessionParameters().userRole;
		
		// Cas Particulier du master
		if (hasRole(RoleList.MASTER))
		{
			addCategorie("MASTER");
			addMenu(MenuList.LISTE_APP_INSTANCE, AppInstanceListPart.class);
			addMenu(MenuList.SUIVI_ACCES, SuiviAccesView.class);	
			addMenu(MenuList.VISU_LOG, LogView.class);
			addMenu(MenuList.STAT_ACCES, StatAccessView.class);
			addMenu(MenuList.SUPERVISION, SupervisionView.class);
			addMenu(MenuList.OUTILS_DEV, DevToolsView.class);
			
			
			addCategorie("DIVERS");
			addMenu(MenuList.UTILISATEUR, UtilisateurListPart.class);
			addMenu(MenuList.PARAMETRES, ParametresView.class);
			addMenu(MenuList.ENVOI_MAIL, SendMailView.class);
			
			return menus;
		}
		
		// Cas general
		
		addMenu(MenuList.MES_CONTRATS,MesContratsView.class);
		addMenu(MenuList.MES_LIVRAISONS,  MesLivraisonsView.class);
		addMenu(MenuList.MES_PAIEMENTS,  MesPaiementsView.class);
		addMenu(MenuList.MES_ADHESIONS,  MesAdhesionsView.class,ModuleList.GESTION_COTISATION);
		addMenu(MenuList.MON_COMPTE,  MonCompteView.class);
		addMenu(MenuList.VISITE_AMAP,  VisiteAmapView.class);
		addMenu(MenuList.LISTE_PRODUCTEUR_REFERENT,  ListeProducteurReferentView.class);
		addMenu(MenuList.LISTE_ADHERENTS,  ListeAdherentsView.class);
		addMenu(MenuList.MES_PERMANENCES,  MesPermanencesView.class , ModuleList.PLANNING_DISTRIBUTION);
		
		
		// Partie historique
		addCategorie("HISTORIQUE");
		addMenu(MenuList.HISTORIQUE_CONTRATS, HistoriqueContratsView.class);
		addMenu(MenuList.HISTORIQUE_PAIEMENTS,  HistoriquePaiementsView.class);
		
		// Partie extra 
		addCategorie("EXTRA");
		if (extraLivraisonAmapien())
		{
			addMenu(MenuList.LIVRAISON_AMAPIEN,  LivraisonAmapienView.class);
		}
		if (extraLivraisonProducteur())
		{
			addMenu(MenuList.LIVRAISONS_PRODUCTEUR,  ProducteurLivraisonsView.class);
		}
		
		
		// Partie producteurs
		if (hasRole(RoleList.PRODUCTEUR))
		{
			addCategorie("PRODUCTEUR");
			addMenu(MenuList.LIVRAISONS_PRODUCTEUR, ProducteurLivraisonsView.class);
			addMenu(MenuList.CONTRATS_PRODUCTEUR, ProducteurContratListPart.class);
			
			if (producteurCanAccessReceptionCheque())
			{
				addMenu(MenuList.RECEPTION_CHEQUES, ReceptionChequeListPart.class);
			}
			if (producteurCanAccessRemiseProducteur())
			{
				addMenu(MenuList.REMISE_PRODUCTEUR, RemiseProducteurListPart.class);
			}
			
			
		}
		
		// Partie référents
		if (hasRole(RoleList.REFERENT))
		{
			addCategorie("REFERENT");
			addMenu(MenuList.GESTION_CONTRAT, GestionContratListPart.class);
			addMenu(MenuList.GESTION_CONTRAT_SIGNES,  GestionContratSignesListPart.class );
			addMenu(MenuList.RECEPTION_CHEQUES, ReceptionChequeListPart.class);
			addMenu(MenuList.REMISE_PRODUCTEUR, RemiseProducteurListPart.class);
			addMenu(MenuList.PRODUIT, ProduitListPart.class);
			addMenu(MenuList.CONTRATS_AMAPIEN,  ContratsAmapienListPart.class);
			addMenu(MenuList.LIVRAISON_AMAPIEN,  LivraisonAmapienView.class);
			addMenu(MenuList.SYNTHESE_MULTI_CONTRAT,  SyntheseMultiContratView.class);
		}
		
		// Partie permanence
		if (hasRole(RoleList.REFERENT) && hasModule(ModuleList.PLANNING_DISTRIBUTION))
		{
			addCategorie("PERMANENCES");
			addMenu(MenuList.PERIODE_PERMANENCE, PeriodePermanenceListPart.class);
			addMenu(MenuList.DETAIL_PERIODE_PERMANENCE, DetailPeriodePermanenceListPart.class);
			addMenu(MenuList.ROLE_PERMANENCE, PermanenceRoleListPart.class);
		}
		
		// Partie trésorier
		if (hasRole(RoleList.TRESORIER))
		{
			addCategorie("TRESORIER");
			addMenu(MenuList.UTILISATEUR, UtilisateurListPart.class);
			addMenu(MenuList.PRODUCTEUR, ProducteurListPart.class);
			addMenu(MenuList.BILAN_COTISATION, BilanCotisationView.class,ModuleList.GESTION_COTISATION);
			addMenu(MenuList.RECEPTION_COTISATION, ReceptionCotisationView.class, ModuleList.GESTION_COTISATION);
			addMenu(MenuList.IMPORT_DONNEES, ImportDonneesView.class);
			addMenu(MenuList.LISTE_TRESORIER, DroitsTresorierListPart.class);
			addMenu(MenuList.ETIQUETTE, EditionSpeListPart.class);
			if (new TenantNotificationService().isActif())
			{
				addMenu(MenuList.TENANT_NOTIFICATION, TenantNotificationListPart.class);
			}
		}
		
		// Partie archives
		if (hasRole(RoleList.REFERENT))
		{
			addCategorie("ARCHIVES");
			addMenu(MenuList.CONTRAT_ARCHIVE,  ArchivageContratListPart.class);
			if (hasRole(RoleList.TRESORIER))
			{
				addMenu(MenuList.PRODUCTEUR_ARCHIVE,  ArchivageProducteurListPart.class);
				addMenu(MenuList.UTILISATEUR_ARCHIVE,  ArchivageUtilisateurListPart.class);
				addMenu(MenuList.GESTION_ARCHIVE,  GestionArchivageView.class);
			}
		}
		
		// Partie adminitrateur
		if (hasRole(RoleList.ADMIN))
		{
			addCategorie("ADMIN");
			addMenu(MenuList.PARAMETRES, ParametresView.class);
			addMenu(MenuList.LISTE_ADMIN, DroitsAdministrateurListPart.class);
			addMenu(MenuList.MAINTENANCE, MaintenanceView.class);
			addMenu(MenuList.ENVOI_MAIL, SendMailView.class);
		}
		
		//	On supprime enfin les categories vides
		removeEmptyCategories();
		
		return menus;
	}

	// Gestion particulière pour le menu extra des adherents de base

	private boolean extraLivraisonAmapien() 
	{
		ParamEcranDTO dto = findParamEcran(MenuList.LIVRAISON_AMAPIEN);
		if (dto==null)
		{
			return false;
		}
		PELivraisonAmapien ape = (PELivraisonAmapien) ParamEcranConverter.load(dto);
		
		return ape.accesEcran==PELivraisonAmapienAccess.ALL;	
	}
	
	private boolean extraLivraisonProducteur() 
	{
		ParamEcranDTO dto = findParamEcran(MenuList.LIVRAISONS_PRODUCTEUR);
		if (dto==null)
		{
			return false;
		}
		PELivraisonProducteur ape = (PELivraisonProducteur) ParamEcranConverter.load(dto);
		
		return ape.accesEcran==PELivraisonProducteurAccess.ALL;	
	}
	
	
	
	// Gestion particulière pour les droits exceptionnels des producteurs 
	
	private boolean producteurCanAccessReceptionCheque() 
	{
		ParamEcranDTO dto = findParamEcran(MenuList.RECEPTION_CHEQUES);
		if (dto==null)
		{
			return false;
		}
		PEReceptionCheque ape = (PEReceptionCheque) ParamEcranConverter.load(dto);
		
		return ape.accesEcran==PEReceptionChequeAccess.PRODUCTEUR;	
	}
	
	

	private boolean producteurCanAccessRemiseProducteur() 
	{
		ParamEcranDTO dto = findParamEcran(MenuList.REMISE_PRODUCTEUR);
		if (dto==null)
		{
			return false;
		}
		PERemiseProducteur ape = (PERemiseProducteur) ParamEcranConverter.load(dto);
		
		return ape.accesEcran==PERemiseProducteurAccess.PRODUCTEUR;	
	}
	
	
	

	//

	private void addCategorie(String categorie) 
	{ 
		menus.add(new MenuDescription(categorie));
	}
	

	private void addMenu(MenuList menu, Class<? extends View> viewClass) 
	{
		if (complyParamEcran(menu))
		{
			menus.add(new MenuDescription(menu, viewClass));
		}
	}
	
	private void addMenu(MenuList menu, Class<? extends View> viewClass,ModuleList module) 
	{
		if (hasModule(module))
		{
			addMenu(menu, viewClass);
		}
		
	}
	
	/**
	 * Retourne true si cet utilisateur a ce role 
	 */
	private boolean hasRole(RoleList role) 
	{
		return roles.contains(role);
	}
	
	/**
	 * Retourne true si ce module est actif 
	 */
	private boolean hasModule(ModuleList module)
	{
		switch (module)
		{
		case PLANNING_DISTRIBUTION:
			return param.etatPlanningDistribution.equals(EtatModule.ACTIF);
			
		case GESTION_COTISATION:
			return param.etatGestionCotisation.equals(EtatModule.ACTIF);

		default:
			throw new AmapjRuntimeException("Erreur de programmation");
		}
	}
	
	

	/**
	 * Recherche si l'écran est accessible à cet utilisateur en fonction des
	 * paramètrages écran effectués
	 */
	private boolean complyParamEcran(MenuList menu)
	{
		ParamEcranDTO dto = findParamEcran(menu);
		
		if (dto==null)
		{
			return true;
		}
		
		AbstractParamEcran ape = ParamEcranConverter.load(dto);
		
		return ape.complyParamEcan(roles);	
	}
	

	private ParamEcranDTO findParamEcran(MenuList menu)
	{
		for (ParamEcranDTO dto : dtos)
		{
			if (dto.menu.equals(menu))
			{
				return dto;
			}
		}
		return null;
	}
	
	/**
	 * Suppression des categories vides
	 */
	private void removeEmptyCategories() 
	{
		List<MenuDescription> toRemoves = new ArrayList<MenuDescription>();
		
		for (int i = 0; i < menus.size(); i++) 
		{
			MenuDescription menu = menus.get(i);
			if (menu.getCategorie()!=null)
			{
				if ( (i==menus.size()-1) || (menus.get(i+1).getCategorie()!=null)  )
				{
					toRemoves.add(menu);
				}
			}
		}
		menus.removeAll(toRemoves);
	}
}
