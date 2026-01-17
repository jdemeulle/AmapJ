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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import fr.amapj.model.engine.Identifiable;
import fr.amapj.model.models.contrat.modele.EtatModeleContrat;
import fr.amapj.model.models.editionspe.EditionSpecifique;
import fr.amapj.model.models.param.ChoixOuiNon;
import fr.amapj.model.models.param.EtatModule;

@Entity
@Table( uniqueConstraints=
		{
		   @UniqueConstraint(columnNames={"nom"})
		})
public class Producteur implements Identifiable
{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	@NotNull
	@Size(min = 1, max = 100)
	@Column(length = 100)
	public String nom;
	
	
	@Size(min = 1, max = 2048)
	@Column(length = 2048)
	public String description;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	public ChoixOuiNon feuilleDistributionGrille;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	public ChoixOuiNon feuilleDistributionListe;
	
	/**
	 * Type etiquette utilisé par ce producteur
	 * null si il n'utilise pas d'étiquette  
	 */
	@ManyToOne
	public EditionSpecifique etiquette;

	// Libelle qui sera utilisé sur le contrat
	@Size(min = 0, max = 255)
	@Column(length = 255)
	public String libContrat;
	
	@NotNull
	public int delaiModifContrat;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	// Permet de savoir l'état du producteur 
	public EtatProducteur etat = EtatProducteur.ACTIF;
	
	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	public Date dateCreation;
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date dateModification;
	
	
	/**
	 * Activation ou désactivation du module "Gestion des stocks (quantités limites)" pour ce producteur 
	 */
	@NotNull
	@Enumerated(EnumType.STRING)
	public ProducteurStockGestion gestionStock;
	
	

	@Override
	public Long getId()
	{
		return id;
	}

	@Override
	public void setId(Long id)
	{
		this.id = id;
	}
	
	
	
}
