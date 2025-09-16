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
 package fr.amapj.service.services.web;

import java.util.function.Consumer;

import fr.amapj.common.StringUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.models.web.WebPage;

public class WebPageService
{
	
	// CHARGEMENT D'UNE WEB PAGE POUR UTILISATION (AFFICHAGE)
	
	@DbRead
	public WebPageDTO loadWebPage(Long idWebPage)
	{
		RdbLink em = RdbLink.get();
		WebPage webPage = em.find(WebPage.class, idWebPage);
		return loadWebPage(em, webPage);
	}
	
	
	// FONCTION POUR L'EDITION DES WEB PAGES DANS LES FICHIERS DE BASE 
	
	public WebPageDTO loadWebPage(RdbLink em,WebPage webPage)
	{
		if (webPage==null)
		{
			return new WebPageDTO();
		}
		
		WebPageDTO dto = new WebPageDTO();
		dto.id = webPage.id;
		dto.content = StringUtils.fromBytes(webPage.getHtml());
		
		return dto;
	}
	
	
	/**
	 * Méthode appelée lorsque l'utilisateur modifie / crée une page Web 
	 * 
	 * Cas 4 sont à considérer :
	 *      -> l'utilisateur a saisi rien
	 * 				-> et la page n'existait pas
	 * 				-> et la page existait avant
	 *      -> l'utilisateur a saisi du texte 
	 * 				-> et la page n'existait pas
	 * 				-> et la page existait avant      
	 * 
	 * Si dto.content = null, alors l'utilisateur a saisi rien 
	 * 
	 * Si dto.id ==null, alors la page n'existait pas avant 
	 */
	public WebPage saveWebPage(RdbLink em,WebPageDTO dto,Consumer<WebPage> updater)
	{		
		// Cas : l'utilisateur a saisi rien 
		if (dto.content==null)
		{
			if (dto.id==null)
			{
				// Nothing to do : la page n'existait pas , et elle n'a pas été saisie
				return null; 
			}
			else
			{
				// La page existait, l'utilisateur l'a remplacé par du vide, il faut la supprimer
				updater.accept(null); // On supprime la référence à cette page 
				delete(em, dto.id);   // On supprime la page 
				return null;
			}
		}
		
		// Cas : l'utilisateur a saisi un texte 
		WebPage p;
		if (dto.id==null)
		{
			// La page n'existait pas : on la crée 
			p = new WebPage();
			em.persist(p);
			updater.accept(p); // On crée une référence vers cette page nouvellement créée
		}
		else
		{
			p = em.find(WebPage.class, dto.id);
		}
		
		// Sauvegarde dans le storage
		p.setHtml(em, StringUtils.toBytes(dto.content));
		
		//
		return p;
	}
	
	
	public void delete(RdbLink em, Long webPageId)
	{
		WebPage p = em.find(WebPage.class, webPageId);
		p.setHtml(em, null);
		em.remove(p);
	}
}
