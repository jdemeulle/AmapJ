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
 package fr.amapj.model.models.web;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import fr.amapj.model.engine.Identifiable;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.objectstorage.ObjectStorage;
import fr.amapj.service.engine.objectstorage.ObjectStorageServiceProvider;

/**
 * Represente un fragment HTML, pour être affiché dans une page Web
 * 
 * La page en elle même est stockée dans objectstorage 
 *
 */
@Entity
public class WebPage  implements Identifiable
{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;
	
	
	// Le html de cette page 
	@ManyToOne
	private ObjectStorage html;

	// Getters ans setters
	
	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}
	
	// Parti specifique à ObjectStorage html;
	
	// Permet d'accéder au contenu du html 
	public byte[] getHtml()
	{
		return ObjectStorageServiceProvider.find().get(html);
	}
	
	// Permet de positionner le contenu du Pdf
	public void setHtml(RdbLink em,byte[] content)
	{
		html = ObjectStorageServiceProvider.find().set(em,html,content);
	}

	
}
