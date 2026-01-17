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
 package fr.amapj.model.models.saas;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import fr.amapj.model.engine.Identifiable;


/**
 * Instance de l'application,
 *
 */
@Entity
public class AppInstance  implements Identifiable
{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	@NotNull
	@Size(min = 0, max = 255)
	@Column(length = 255) 
	public String nomInstance;
	
	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	public Date dateCreation;
	
	// Utilisé uniquement pour les base de données externe
	@Size(min = 0, max = 255)
	@Column(length = 255)
	public String dbUserName;
	
	// Utilisé uniquement pour les base de données externe
	@Size(min = 0, max = 255)
	@Column(length = 255)
	public String dbPassword;
	
	
	@NotNull
	@Enumerated(EnumType.STRING)
	// Etat de la base au démarrage
	public StateOnStart stateOnStart;
	

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	

	
}
