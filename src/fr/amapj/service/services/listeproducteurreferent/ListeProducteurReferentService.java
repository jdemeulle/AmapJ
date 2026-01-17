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
 package fr.amapj.service.services.listeproducteurreferent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import fr.amapj.common.CollectionUtils;
import fr.amapj.common.StringUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.models.fichierbase.EtatProducteur;
import fr.amapj.model.models.fichierbase.Producteur;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.model.models.param.ChoixOuiNon;
import fr.amapj.service.services.producteur.ProducteurService;

/**
 */
public class ListeProducteurReferentService
{

	// PARTIE REQUETAGE POUR AVOIR LA LISTE DES PRODUCTEURS

	/**
	 * Retourne la liste des producteurs
	 */
	@DbRead
	public List<DetailProducteurDTO> getAllProducteurs(DetailRequestDTO request)
	{
		RdbLink em = RdbLink.get();
		
		List<DetailProducteurDTO> res = new ArrayList<DetailProducteurDTO>();
		
		TypedQuery<Producteur> q = em.createQuery("select p from Producteur p where p.etat = :etat",Producteur.class);
		q.setParameter("etat", EtatProducteur.ACTIF);
		
		for (Producteur producteur : q.getResultList())
		{
			DetailProducteurDTO dto = createDetailProducteurDTO(producteur,request,em);
			res.add(dto);
		}
		
		// Tri par ordre alphabetique 
		CollectionUtils.collatorSort(res, e->e.nom);
		return res;
	}
	
	
	@DbRead
	public DetailProducteurDTO getOneProducteur(Long idProducteur,DetailRequestDTO request)
	{
		RdbLink em = RdbLink.get();
		Producteur p = em.find(Producteur.class, idProducteur);
		return createDetailProducteurDTO(p,request, em);
	}
	

	private DetailProducteurDTO createDetailProducteurDTO(Producteur producteur,DetailRequestDTO request, RdbLink em)
	{
		DetailProducteurDTO dto = new DetailProducteurDTO();
		
		dto.nom = producteur.nom;
		dto.description = producteur.description;
		
		List<Utilisateur> referents = new ProducteurService().getProducteurReferent(em, producteur).stream().map(e->e.referent).collect(Collectors.toList());
		List<Utilisateur> producteurs = new ProducteurService().getProducteurUtilisateur(em, producteur).stream().map(e->e.utilisateur).collect(Collectors.toList());
		dto.contacts = "<b>Contacts :</b><br/>"+formatProducteur(producteurs,request)+formatReferent(referents,request);
		
		return dto;
	}


			
	private String formatProducteur(List<Utilisateur> utilisateurs, DetailRequestDTO request)
	{
		if (utilisateurs.size()==0)
		{
			return "";
		}
		
		String str = CollectionUtils.asString(utilisateurs, " et ", e->formatUtilisateur(e,request.producteurEmail,request.producteurTel1,request.producteurTel2));
		
		if (utilisateurs.size()==1)
		{
			return "Le producteur est "+s(str)+"<br/>";
		}
		else
		{
			return "Les producteurs sont  "+s(str)+"<br/>";
		}
	}
			
			
	


	private String formatReferent(List<Utilisateur> utilisateurs, DetailRequestDTO request)
	{
		if (utilisateurs.size()==0)
		{
			return "";
		}
		
		String str = CollectionUtils.asString(utilisateurs, " et ", e->formatUtilisateur(e,request.referentEmail,request.referentTel1,request.referentTel2));
		
		if (utilisateurs.size()==1)
		{
			return "Le référent est "+s(str)+"<br/>";
		}
		else
		{
			return "Les référents sont  "+s(str)+"<br/>";
		}
	}


	private String formatUtilisateur(Utilisateur u,ChoixOuiNon email, ChoixOuiNon tel1,ChoixOuiNon tel2)
	{
		List<String> strs = new ArrayList<>();
		
		if (email==ChoixOuiNon.OUI && u.email.length()>3)
		{
			strs.add("Email : "+u.email);
		}
		if (tel1==ChoixOuiNon.OUI && u.numTel1!=null && u.numTel1.trim().length()>2)
		{
			strs.add("Tel1 : "+u.numTel1);
		}
		if (tel2==ChoixOuiNon.OUI && u.numTel2!=null && u.numTel2.trim().length()>2)
		{
			strs.add("Tel2 : "+u.numTel2);
		}
		
		if (strs.size()==0)
		{
			return u.prenom+" "+u.nom;
		}
		else
		{
			return u.prenom+" "+u.nom+" ("+CollectionUtils.asString(strs, " ,")+")";
		}
	}
	
	private String s(String str)
	{
		return StringUtils.s(str);
	}

}
