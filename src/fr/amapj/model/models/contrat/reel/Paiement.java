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
 package fr.amapj.model.models.contrat.reel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import fr.amapj.model.engine.Identifiable;
import fr.amapj.model.models.contrat.modele.ModeleContratDatePaiement;
import fr.amapj.model.models.remise.RemiseProducteur;

/**
 * Correspond à un chéque
 *
 */
@Entity
@Table( uniqueConstraints=
{
   @UniqueConstraint(columnNames={"modeleContratDatePaiement_id" , "contrat_id"})
})
public class Paiement  implements Identifiable
{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	@NotNull
	@ManyToOne
	public Contrat contrat;
	
	@NotNull
	@ManyToOne
	public ModeleContratDatePaiement modeleContratDatePaiement;

	// Montant du paiement en centimes
	@NotNull
	public int montant;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	// Permet de savoir l'état du modele de contrat
	public EtatPaiement etat = EtatPaiement.A_FOURNIR;
	
	@ManyToOne
	public RemiseProducteur remise;
	

	@Size(min = 0, max = 255)
	@Column(length = 255)
	public String commentaire1;

	@Size(min = 0, max = 255)
	@Column(length = 255)
	public String commentaire2;
	
	@Size(min = 0, max = 255)
	@Column(length = 255)
	public String commentaire3;

	@Size(min = 0, max = 255)
	@Column(length = 255)
	public String commentaire4;
		

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}
}
