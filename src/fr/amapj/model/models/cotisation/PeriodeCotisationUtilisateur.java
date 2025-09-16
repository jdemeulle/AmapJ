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
 package fr.amapj.model.models.cotisation;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import fr.amapj.model.engine.Identifiable;
import fr.amapj.model.models.fichierbase.Utilisateur;

@Entity
@Table( uniqueConstraints=
{
   @UniqueConstraint(columnNames={"periodeCotisation_id" , "utilisateur_id"})
})
public class PeriodeCotisationUtilisateur  implements Identifiable
{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	@NotNull
	@ManyToOne
	public PeriodeCotisation periodeCotisation;
	
	@NotNull
	@ManyToOne
	public Utilisateur utilisateur;
	
	// Date d'adhesion par l'amapien
	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	public Date dateAdhesion;
	
	// Date de reception du chèque par le tresorier
	@Temporal(TemporalType.DATE)
	public Date dateReceptionCheque;

	// Montant de de l'adhesion en centimes
	@NotNull
	public int montantAdhesion=0;
	
	// Etat du paiement
	@NotNull
	public EtatPaiementAdhesion etatPaiementAdhesion= EtatPaiementAdhesion.A_FOURNIR;
	
	// Etat du paiement
	@NotNull
	public TypePaiementAdhesion typePaiementAdhesion= TypePaiementAdhesion.CHEQUE;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}
	
	
	
	
	
	
}
