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
 * Suivi des accès à l'application, au niveau du master
 * 
 */
@Entity
public class LogAccess  implements Identifiable
{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	@Size(min = 0, max = 255)
	@Column(length = 255)
	public String ip;
	
	@Size(min = 0, max = 255)
	@Column(length = 255)
	public String browser;
	
	@Size(min = 0, max = 255)
	@Column(length = 255)
	public String nom;
	
	@Size(min = 0, max = 255)
	@Column(length = 255)
	public String prenom;
	
	public Long idUtilisateur;
	
	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	public Date dateIn;
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date dateOut;
	
	// Nombre de secondes pendant lequel l'utilisateur est actif
	public int activityTime;
	
	// Nom de la base de données associée
	@Size(min = 0, max = 255)
	@Column(length = 255)
	public String dbName;
	
	// Nom du fichier de log associé
	@Size(min = 0, max = 255)
	@Column(length = 255)
	public String logFileName;
	
	// Type du log : user ou deamon
	@NotNull
	@Enumerated(EnumType.STRING)
	public TypLog typLog = TypLog.USER;
	
	// nb d'erreur
	@NotNull
	public int nbError=0;
	
	// 0 si cas classique, 1 si sudo
	public int sudo=0;
	
	
	// Getters and setters

	public Long getId()
	{
		return id;
	}



	public void setId(Long id)
	{
		this.id = id;
	}
	
}
