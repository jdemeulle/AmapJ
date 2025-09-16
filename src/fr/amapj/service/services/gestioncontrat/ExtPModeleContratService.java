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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.amapj.common.GzipUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.extendparam.MiseEnFormeGraphique;
import fr.amapj.model.models.contrat.modele.extendparam.reglesaisie.RegleSaisieModeleContrat;
import fr.amapj.model.models.extendedparam.ExtendedParam;


/**
 * Gestion des paramètres etendus du modele de contrat 
 *
 */
public class ExtPModeleContratService
{
	
	// Mise en forme graphique 

	@DbRead
	public MiseEnFormeGraphique loadMiseEnFormeGraphique(Long idModeleContrat)
	{
		RdbLink em = RdbLink.get();
		ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);
		return getMiseEnFormeGraphique(mc.miseEnFormeGraphique);
	}
	
	@DbWrite
	public void saveMiseEnFormeGraphique(Long idModeleContrat,MiseEnFormeGraphique pm)
	{
		RdbLink em = RdbLink.get();
		ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);
		setMiseEnFormeGraphique(em,mc,pm);
	}
	
	
	/**
	 * Permet de récuperer les parametres etendus d'un modele de contrat 
	 */
	private MiseEnFormeGraphique getMiseEnFormeGraphique(ExtendedParam extendedParam)
	{
		if (extendedParam==null)
		{
			MiseEnFormeGraphique pm = new MiseEnFormeGraphique();
			pm.setDefault();
			return pm;
		}
		
		
		MiseEnFormeGraphique res = (MiseEnFormeGraphique) new Gson().fromJson(GzipUtils.uncompress(extendedParam.content), MiseEnFormeGraphique.class);
		res.setDefault();
		return res;
	}
	
	
	/**
	 *  Permet de sauvegarder les parametres etendus d'un modele de contrat 
	 * @param em 
	 */
	private void setMiseEnFormeGraphique(RdbLink em, ModeleContrat mc,MiseEnFormeGraphique pm)
	{
		Gson gson = new Gson();
		
		String str = gson.toJson(pm);
		
		MiseEnFormeGraphique def = new MiseEnFormeGraphique();
		def.setDefault();
		String ref = gson.toJson(def);
		
		// Si les parametres sont les paramètres par défaut, on sauvegarde null pour gagner de l'espace
		if (ref.equals(str))
		{
			if (mc.miseEnFormeGraphique!=null)
			{
				em.remove(mc.miseEnFormeGraphique);
			}
			mc.miseEnFormeGraphique = null;
		}
		else
		{
			if (mc.miseEnFormeGraphique==null)
			{
				mc.miseEnFormeGraphique = new ExtendedParam();
				em.persist(mc.miseEnFormeGraphique);
			}
			
			mc.miseEnFormeGraphique.content = GzipUtils.compress(str);
		}
	}

	
	
	
	// Regle de saisie
	
	@DbRead
	public RegleSaisieModeleContrat loadRegleSaisieModeleContrat(Long idModeleContrat)
	{
		RdbLink em = RdbLink.get();
		ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);
		return getRegleSaisieModeleContrat(mc.regleSaisieModeleContrat);
	}
	
	@DbWrite
	public void saveRegleSaisieModeleContrat(Long idModeleContrat,RegleSaisieModeleContrat pm)
	{
		RdbLink em = RdbLink.get();
		ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);
		setRegleSaisieModeleContrat(em,mc,pm);
	}
	
	
	/**
	 * Permet de récuperer les parametres etendus d'un modele de contrat 
	 */
	public RegleSaisieModeleContrat getRegleSaisieModeleContrat(ExtendedParam extendedParam)
	{
		if (extendedParam==null)
		{
			RegleSaisieModeleContrat pm = new RegleSaisieModeleContrat();
			return pm;
		}
		
		
		RegleSaisieModeleContrat res = (RegleSaisieModeleContrat) createGson().fromJson(GzipUtils.uncompress(extendedParam.content), RegleSaisieModeleContrat.class);
		return res;
	}
	
	
	/**
	 *  Permet de sauvegarder les parametres etendus d'un modele de contrat 
	 */
	private void setRegleSaisieModeleContrat(RdbLink em, ModeleContrat mc,RegleSaisieModeleContrat pm)
	{
		Gson gson = createGson();		
		String str = gson.toJson(pm);
		
		
		// Si pas de regles, on sauvegarde null pour gagner de l'espace
		if (pm.regleSaisies.size()==0)
		{
			if (mc.regleSaisieModeleContrat!=null)
			{
				em.remove(mc.regleSaisieModeleContrat);
			}
			mc.regleSaisieModeleContrat = null;
		}
		else
		{
			if (mc.regleSaisieModeleContrat==null)
			{
				mc.regleSaisieModeleContrat = new ExtendedParam();
				em.persist(mc.regleSaisieModeleContrat);
			}
			
			mc.regleSaisieModeleContrat.content = GzipUtils.compress(str);
		}
	}
	
	
	// Partie commune

	@DbWrite
	public void copyExtendedParam(Long idOldContrat, Long idNewContrat)
	{
		RdbLink em = RdbLink.get();
		ModeleContrat mcOld = em.find(ModeleContrat.class, idOldContrat);
		ModeleContrat mcNew = em.find(ModeleContrat.class, idNewContrat);
		
		mcNew.miseEnFormeGraphique = duplicateExtendedParam(em,mcOld.miseEnFormeGraphique);
		mcNew.regleSaisieModeleContrat = duplicateExtendedParam(em,mcOld.regleSaisieModeleContrat);		
	}
	
	
	private ExtendedParam duplicateExtendedParam(RdbLink em, ExtendedParam epOld) 
	{
		if (epOld==null)
		{
			return null;
		}
		
		ExtendedParam ep = new ExtendedParam();
		ep.content = epOld.content;
		em.persist(ep);
		return ep;
	}
	
	
	/**
	 * Doit être appelée avant la suppression d'un contrat , pour nettoyer les 
	 * ExtendedParam de ce modele de contrat 
	 */
	public void suppressExtendedParameters(RdbLink em, ModeleContrat mc) 
	{
		remove(em,mc.regleSaisieModeleContrat);
		mc.regleSaisieModeleContrat = null;
		
		remove(em,mc.miseEnFormeGraphique);
		mc.miseEnFormeGraphique = null;
		
	}

	private void remove(RdbLink em, ExtendedParam ep) 
	{
		if (ep!=null)
		{
			em.remove(ep);
		}
	}

	// TOOLS
	
	private Gson createGson()
	{
		return new GsonBuilder().setDateFormat("dd/MM/yyyy").create();
	}

	
	
}
