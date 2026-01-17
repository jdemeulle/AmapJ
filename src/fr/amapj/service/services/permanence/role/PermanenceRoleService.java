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
 package fr.amapj.service.services.permanence.role;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import fr.amapj.common.CollectionUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.permanence.periode.PeriodePermanence;
import fr.amapj.model.models.permanence.periode.PermanenceRole;
import fr.amapj.view.engine.popup.suppressionpopup.UnableToSuppressException;

/**
 * Permet la gestion des roles de permanence 
 * 
 */
public class PermanenceRoleService
{
	
	
	// PARTIE REQUETAGE POUR AVOIR LA LISTE DES ROLES
	
	/**
	 * Permet de charger la liste de tous les roles
	 */
	@DbRead
	public List<PermanenceRoleDTO> getAllRoles()
	{
		RdbLink em = RdbLink.get();
		
		List<PermanenceRoleDTO> res = new ArrayList<>();
		
		Query q = em.createQuery("select p from PermanenceRole p order by p.nom");
			
		List<PermanenceRole> ps = q.getResultList();
		for (PermanenceRole p : ps)
		{
			PermanenceRoleDTO dto = createPermanenceRoleDto(em,p);
			res.add(dto);
		}
		
		return res;
		
	}

	
	public PermanenceRoleDTO createPermanenceRoleDto(RdbLink em, PermanenceRole p)
	{
		PermanenceRoleDTO dto = new PermanenceRoleDTO();
		
		dto.id = p.id;
		dto.nom = p.nom;
		dto.description = p.description;
		dto.defaultRole = p.defaultRole;
		
		return dto;
	}




	// PARTIE MISE A JOUR DES ROLES
	@DbWrite
	public Long update(final PermanenceRoleDTO dto,final boolean create)
	{
		RdbLink em = RdbLink.get();
		
		PermanenceRole p;
		
		if (create)
		{
			p = new PermanenceRole();
		}
		else
		{
			p = em.find(PermanenceRole.class, dto.id);
		}
		
		p.nom = dto.nom;
		p.description = dto.description;
		p.defaultRole = false;
		
		
		if (create)
		{
			em.persist(p);
		}
		
		return p.id;
		
	}



	// PARTIE SUPPRESSION

	/**
	 * Permet de supprimer un role de permanence 
	 */
	@DbWrite
	public void delete(final Long id)
	{
		RdbLink em = RdbLink.get();
		
		PermanenceRole p = em.find(PermanenceRole.class, id);

		List<PeriodePermanence> pps = findPermanence(p,em);
		if (pps.size()!=0)
		{
			throw new UnableToSuppressException("Ce rôle est utilisé dans les permanences suivantes :"+CollectionUtils.asStdString(pps, e->e.nom));
		}
				
		// On supprime le role
		em.remove(p);
	}


	private List<PeriodePermanence> findPermanence(PermanenceRole r, RdbLink em)
	{
		Query q = em.createQuery("select distinct(pc.periodePermanenceDate.periodePermanence) from PermanenceCell pc WHERE pc.permanenceRole=:r");
		q.setParameter("r", r);
			
		return (List<PeriodePermanence>) q.getResultList();
	}

	
	
	// GESTION DU ROLE PAR DEFAUT 

	public PermanenceRole getOrCreateDefaultRole(RdbLink em)
	{		
		Query q = em.createQuery("select p from PermanenceRole p where p.defaultRole=true order by p.nom");
		List<PermanenceRole> ps = q.getResultList();
		
		if (ps.size()>=1)
		{
			return ps.get(0);
		}
		
		PermanenceRole p = new PermanenceRole();
		p.nom = "Place ";
		p.description = "Place ";
		p.defaultRole = true;
		
		
		em.persist(p);
		
		return p;
	}
	
	
	@DbRead
	public Long getIdDefaultRole()
	{		
		RdbLink em = RdbLink.get();
		return getOrCreateDefaultRole(em).id;
	}
	

	
}
