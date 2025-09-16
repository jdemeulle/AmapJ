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
 package fr.amapj.model.models.fichierbase;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import fr.amapj.model.engine.Identifiable;

@Entity
@Table( uniqueConstraints=
{
   @UniqueConstraint(columnNames={"nom" , "prenom"}),
   @UniqueConstraint(columnNames={"email"})
})
public class Utilisateur  implements Identifiable
{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	@NotNull
	@Size(min = 1, max = 100)
	@Column(length = 100)
	public String prenom;
	
	@NotNull
	@Size(min = 1, max = 100)
	@Column(length = 100)
	public String nom;
	
	@Size(min = 1, max = 150)
	@Column(length = 150)
	// Contient l'adresse e mail
	public String email;
	
	@Size(min = 0, max = 150)
	@Column(length = 150)
	// Contient le password encrypté
	public String password;
	
	@Size(min = 0, max = 150)
	@Column(length = 150)
	// Contient le salt permettant d'encrypter le password 
	public String salt;
	
	@Size(min = 0, max = 150)
	@Column(length = 150)
	// Contient le slat calculé à la demande du reset du password 
	public String resetPasswordSalt;
	
	@Temporal(TemporalType.TIMESTAMP)
	// Contient le slat calculé à la demande du reset du password 
	public Date resetPasswordDate;
	
	
	@NotNull
	@Enumerated(EnumType.STRING)
	// Permet d'indiquer si cet utilisateur est actif ou inactif
	public EtatUtilisateur etatUtilisateur = EtatUtilisateur.ACTIF;
	
	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	public Date dateCreation;
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date dateModification;
	
	
	// Liste des élements d'informations générales
	
	
	@Size(min = 0, max = 25)
	// numéro de  téléphone 1
	public String numTel1;
	
	@Size(min = 0, max = 25)
	// numéro de  téléphone 2
	public String numTel2;
	
	@Size(min = 0, max = 150)
	public String libAdr1;
	
	@Size(min = 0, max = 150)
	public String codePostal;

	@Size(min = 0, max = 150)
	public String ville;
	

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
