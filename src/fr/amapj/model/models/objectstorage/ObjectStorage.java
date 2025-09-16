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
 package fr.amapj.model.models.objectstorage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import fr.amapj.model.engine.Identifiable;

/**
 * Pour chaque objet créé par le ObjectStorageService, un enregistrement est créé dans cette table
 * quelque soit le choix fait entre stockage local ou distant (S3 storage) 
 * 
 * Dans le cas du stockage local : un enregistrement est  créé dans cette table, avec les données 
 * 
 * Dans le cas du stockage distant :  un enregistrement est aussi créé dans cette table, sans les données 
 * Ceci permet de garantir l'état transactionnel entre la base de données et le s3 storage  
 * 
 * La documentation est accessible ici : /docs/tech-notes/dev/objectstorage/objectstorage.txt
 *
 */
@Entity
public class ObjectStorage  implements Identifiable
{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;
	
	// Ce champ est utilisé uniquement dans le cas du stockage local  
	// Contient les données ( zip puis passage en base 64)
	@Size(min = 1, max = 4000000)
	@Column(length = 4000000)
	public String content;
	
	// Ce champ est utilisé dans tous les cas, local ou distant    
	// Si null : l'object est à l'état normal 
	@Enumerated(EnumType.STRING)
	public ObjectStorageState state;
		
	// Getters ans setters
	
	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}
}
