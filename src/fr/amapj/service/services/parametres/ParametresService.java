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
 package fr.amapj.service.services.parametres;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.acces.RoleList;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.StockGestion;
import fr.amapj.model.models.fichierbase.Producteur;
import fr.amapj.model.models.fichierbase.ProducteurStockGestion;
import fr.amapj.model.models.param.EtatModule;
import fr.amapj.model.models.param.Parametres;
import fr.amapj.model.models.param.paramecran.PEListeAdherent;
import fr.amapj.model.models.param.paramecran.common.AbstractParamEcran;
import fr.amapj.model.models.param.paramecran.common.ParamEcran;
import fr.amapj.model.models.param.paramecran.common.ParamEcranConverter;
import fr.amapj.model.models.produitextended.reglesconversion.RegleConversionProduit;
import fr.amapj.service.services.parametres.paramecran.PEListeAdherentDTO;
import fr.amapj.service.services.producteur.ProducteurService;
import fr.amapj.service.services.produitextended.ProduitExtendedService;
import fr.amapj.service.services.session.SessionManager;
import fr.amapj.view.engine.menu.MenuList;

/**
 * 
 * 
 */
public class ParametresService
{
	
	static private Long ID_PARAM = new Long(1);
	
	// PARTIE REQUETAGE 
	
	/**
	 * Permet de charger les paramètres
	 */
	@DbRead
	public ParametresDTO getParametres()
	{
		RdbLink em = RdbLink.get();
		Parametres param = findParam(em);
		
		ParametresDTO dto = new ParametresDTO();
		dto.nomAmap = param.nomAmap;
		dto.villeAmap = param.villeAmap;
		dto.sendingMailUsername = param.sendingMailUsername;
		dto.sendingMailPassword = param.sendingMailPassword;
		dto.sendingMailNbMax = param.sendingMailNbMax;
		dto.sendingMailFooter = param.sendingMailFooter;
		dto.mailCopyTo = param.mailCopyTo;
		dto.url = param.url;
		dto.backupReceiver = param.backupReceiver;
		
		dto.etatPlanningDistribution = param.etatPlanningDistribution;
		dto.etatGestionCotisation = param.etatGestionCotisation;
		dto.delaiMailRappelPermanence = param.delaiMailRappelPermanence;
		dto.envoiMailRappelPermanence = param.envoiMailRappelPermanence;
		dto.titreMailRappelPermanence = param.titreMailRappelPermanence;
		dto.contenuMailRappelPermanence = param.contenuMailRappelPermanence;
		
		dto.envoiMailPeriodique = param.envoiMailPeriodique;
		dto.numJourDansMois = param.numJourDansMois;
		dto.titreMailPeriodique = param.titreMailPeriodique;
		dto.contenuMailPeriodique = param.contenuMailPeriodique;
		
		dto.etatGestionStock = param.etatGestionStock;
			
		// Champs calculés
		dto.serviceMailActif = false;
		if ((param.sendingMailUsername!=null) && (param.sendingMailUsername.length()>0))
		{
			dto.serviceMailActif = true;
		}
		
		return dto;
		
	}
	
	
	/**
	 * Permet de charger les paramètres relatifs à l'archivage
	 */
	@DbRead
	public ParametresArchivageDTO getParametresArchivage()
	{
		RdbLink em = RdbLink.get();
		Parametres param = findParam(em);
		
		ParametresArchivageDTO dto = new ParametresArchivageDTO();
		dto.archivageContrat = param.archivageContrat;
		dto.suppressionContrat = param.suppressionContrat;
		dto.archivageUtilisateur = param.archivageUtilisateur;
		dto.archivageProducteur = param.archivageProducteur;
		dto.suppressionPeriodeCotisation = param.suppressionPeriodeCotisation;
		dto.suppressionPeriodePermanence = param.suppressionPeriodePermanence;
		
		return dto;
	}
	
	


	private Parametres findParam(RdbLink em) 
	{
		Parametres param = em.find(Parametres.class, ID_PARAM);
		
		if (param==null)
		{
			throw new RuntimeException("Il faut insérer les paramètres généraux dans la base");
		}
		return param;
	}


	// PARTIE MISE A JOUR 

	
	@DbWrite
	public void update(ParametresDTO dto)
	{
		RdbLink em = RdbLink.get();
		
		Parametres param = findParam(em);
		
		param.nomAmap = dto.nomAmap;
		param.villeAmap = dto.villeAmap;
		param.sendingMailUsername = dto.sendingMailUsername;
		param.sendingMailPassword = dto.sendingMailPassword;
		param.sendingMailNbMax = dto.sendingMailNbMax;
		param.sendingMailFooter = dto.sendingMailFooter;
		param.mailCopyTo = dto.mailCopyTo;
		param.url = dto.url;
		param.backupReceiver = dto.backupReceiver;
		
		param.etatPlanningDistribution = dto.etatPlanningDistribution;
		param.etatGestionCotisation = dto.etatGestionCotisation;
		param.delaiMailRappelPermanence = dto.delaiMailRappelPermanence;
		param.envoiMailRappelPermanence = dto.envoiMailRappelPermanence;
		param.titreMailRappelPermanence = dto.titreMailRappelPermanence;
		param.contenuMailRappelPermanence = dto.contenuMailRappelPermanence;
		
		param.envoiMailPeriodique = dto.envoiMailPeriodique;
		param.numJourDansMois = dto.numJourDansMois;
		param.titreMailPeriodique = dto.titreMailPeriodique;
		param.contenuMailPeriodique = dto.contenuMailPeriodique;
		
		param.etatGestionStock = dto.etatGestionStock;
		
		// La gestion des stocks
		updateStockModeleContrat(em,param);
	}
	
	private void updateStockModeleContrat(RdbLink em, Parametres param) 
	{
		if (param.etatGestionStock==EtatModule.INACTIF)
		{
			TypedQuery<Producteur> q2 = em.createQuery("select p from Producteur p",Producteur.class);
			for (Producteur p : q2.getResultList()) 
			{
				p.gestionStock = ProducteurStockGestion.NON;
				new ProducteurService().updateStockInfoModeleContratAndProduit(em, p);
			}
		}
	}
	

	
	
	@DbWrite
	public void updateParametresArchivage(ParametresArchivageDTO dto)
	{
		RdbLink em = RdbLink.get();
		
		Parametres param = findParam(em);
		
		param.archivageContrat = dto.archivageContrat;
		param.suppressionContrat = dto.suppressionContrat;
		param.archivageUtilisateur = dto.archivageUtilisateur;
		param.archivageProducteur = dto.archivageProducteur;
		param.suppressionPeriodeCotisation = dto.suppressionPeriodeCotisation;
		param.suppressionPeriodePermanence = dto.suppressionPeriodePermanence;
	}
	
	
	// PARTIE REQUETAGE POUR AVOIR LA LISTE DU PARAMETRAGE DE CHAQUE ECRAN
	

	/**
	 * Permet de charger la liste de tous les parametrages ecrans
	 */
	@DbRead
	public List<ParamEcranDTO> getAllParamEcranDTO()
	{
		RdbLink em = RdbLink.get();

		List<ParamEcranDTO> res = new ArrayList<>();

		Query q = em.createQuery("select p from ParamEcran p");

		List<ParamEcran> ps = q.getResultList();
		for (ParamEcran p : ps)
		{
			ParamEcranDTO dto = createParamEcranDTO(em, p);
			res.add(dto);
		}

		return res;

	}

	public ParamEcranDTO createParamEcranDTO(RdbLink em, ParamEcran p)
	{
		ParamEcranDTO dto = new ParamEcranDTO();

		dto.id = p.getId();
		dto.menu = p.getMenu();
		dto.content = p.getContent();

		return dto;
	}

	

	// PARTIE MISE A JOUR 
	@DbWrite
	public void update(AbstractParamEcran abstractParamEcran)
	{
		RdbLink em = RdbLink.get();
		
		boolean create = abstractParamEcran.getId()==null;
		
		ParamEcranDTO dto = ParamEcranConverter.save(abstractParamEcran);

		ParamEcran p;

		if (create)
		{
			p = new ParamEcran();
			p.setMenu(dto.menu);
		} 
		else
		{
			p = em.find(ParamEcran.class, dto.id);
		}

		p.setContent(dto.content);
		

		if (create)
		{
			em.persist(p);
		}

	}


	/**
	 * Permet de charger le parametrage d'un écran 
	 * dans le but de l'utiliser fonctionnellement
	 */
	@DbRead
	public AbstractParamEcran loadParamEcran(MenuList menuList)
	{
		RdbLink em = RdbLink.get();
		
		ParamEcranDTO p = getParamEcranDTO(menuList,em);

		AbstractParamEcran pe;
		if (p!=null)
		{
			pe = ParamEcranConverter.load(p);
		}
		else
		{
			pe = ParamEcranConverter.getNew(menuList);
		}
		return pe;
	}
	
	
	/**
	 * Permet de charger le parametrage d'un écran particulier, sous la forme d'un DTO
	 * @param em 
	 */
	private ParamEcranDTO getParamEcranDTO(MenuList menu, RdbLink em)
	{
		Query q = em.createQuery("select p from ParamEcran p where p.menu=:m ");
		q.setParameter("m", menu);

		List<ParamEcran> ps = q.getResultList();
		
		if (ps.size()==0)
		{
			return null;
		}
		else if (ps.size()==1)
		{
			return createParamEcranDTO(em, ps.get(0));
		}
		else
		{
			throw new AmapjRuntimeException("Erreur : il y a deux param ecrans pour "+menu);
		}
	}

	
	
	
	/**
	 * Permet de charger le parametrage de l'écran liste adhérent
	 * dans le but de l'utiliser fonctionnellement
	 * 
	 * Cas specifique à cet écran, dans le cas normal utiliser la methode 
	 * public AbstractParamEcran loadParamEcran(MenuList menuList)
	 */
	public PEListeAdherentDTO getPEListeAdherentDTO()
	{
		PEListeAdherent pe = (PEListeAdherent) loadParamEcran(MenuList.LISTE_ADHERENTS);
		
		List<RoleList> roles = SessionManager.getSessionParameters().userRole;
		
		PEListeAdherentDTO ret = new PEListeAdherentDTO();
		ret.canAccessEmail = roles.contains(pe.canAccessEmail);
		ret.canAccessTel1 = roles.contains(pe.canAccessTel1);
		ret.canAccessTel2 = roles.contains(pe.canAccessTel2);
		ret.canAccessAdress = roles.contains(pe.canAccessAdress);	
		
		return ret;
	}
		
}
