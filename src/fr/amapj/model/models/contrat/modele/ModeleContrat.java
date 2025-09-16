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
 package fr.amapj.model.models.contrat.modele;

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
import fr.amapj.model.models.cotisation.PeriodeCotisation;
import fr.amapj.model.models.editionspe.EditionSpecifique;
import fr.amapj.model.models.extendedparam.ExtendedParam;
import fr.amapj.model.models.fichierbase.Producteur;
import fr.amapj.model.models.param.ChoixOuiNon;

@Entity
@Table( uniqueConstraints=
{
   @UniqueConstraint(columnNames={"nom"})
})
public class ModeleContrat implements Identifiable
{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	@NotNull
	@Size(min = 1, max = 100)
	@Column(length = 100)
	public String nom;
	
	@NotNull
	@Size(min = 1, max = 255)
	@Column(length = 255)
	public String description;
	

	@NotNull
	@ManyToOne
	public Producteur producteur;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	// Permet de savoir l'état du modele de contrat
	public EtatModeleContrat etat = EtatModeleContrat.CREATION;
	
	@Temporal(TemporalType.DATE)
	public Date dateFinInscription;
	
	
	@NotNull
	@Enumerated(EnumType.STRING)
	// Indique si il est possible de s'inscrire en retardataire 
	public RetardataireAutorise retardataireAutorise = RetardataireAutorise.OUI;
	
	// Paiement
	
	@NotNull
	@Enumerated(EnumType.STRING)
	// Permet de savoir si on gere les chéques ou non 
	public GestionPaiement gestionPaiement = GestionPaiement.NON_GERE;
	
	@Size(min = 0, max = 2048)
	@Column(length = 2048)
	// Texte qui sera affiché dans le cas ou il n'y a pas de paiement
	public String textPaiement;
	
	
	@Enumerated(EnumType.STRING)
	// Est non null si gestionPaiement = GestionPaiement.NON_GERE, null sinon
	public AffichageMontant affichageMontant;
	
	// Libellé du chéque 
	@Size(min = 0, max = 255)
	@Column(length = 255)
	public String libCheque;
	
	// Date de remise des chéques
	@Temporal(TemporalType.DATE)
	public Date dateRemiseCheque;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	public StrategiePaiement strategiePaiement;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	public SaisiePaiementModifiable saisiePaiementModifiable;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	public SaisiePaiementProposition saisiePaiementProposition;
	
	// Montant minimum pour les chèques pour le calcul de la proposition 
	public int montantChequeMiniCalculProposition;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	public SaisiePaiementCalculDate saisiePaiementCalculDate;
	
	
	@NotNull
	@Enumerated(EnumType.STRING)
	// Permet de savoir l'état du modele de contrat
	public NatureContrat nature;
	
	// Delai en cas de contrat "carte prépayée" 
	public int cartePrepayeeDelai;
	
	// Parametre de mise en forme graphique
	@ManyToOne
	public ExtendedParam miseEnFormeGraphique;
	
	// regles de saisie 
	@ManyToOne
	public ExtendedParam regleSaisieModeleContrat;

	
	// Gestion des jokers
	
	@NotNull
	@Enumerated(EnumType.STRING)
	// Permet d'activer ou de desactiver les jokers, en mode ABSENCE ou mode REPORT
	public TypJoker typJoker;
	
	public int jokerNbMin = 0;
	
	public int jokerNbMax = 0;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	public JokerMode jokerMode;
	
	public int jokerDelai;
	
	// Periode de cotisation associée à ce contrat
	@ManyToOne
	public PeriodeCotisation periodeCotisation;
	
	
	// Gestion du stock 
	@NotNull
	@Enumerated(EnumType.STRING)
	public StockGestion stockGestion = StockGestion.NON;
		
	@Enumerated(EnumType.STRING)
	public StockIdentiqueDate stockIdentiqueDate;
	
	@Enumerated(EnumType.STRING)
	public StockMultiContrat stockMultiContrat;
	
	// Signature du contrat
	
	// Mode de gestion de la signature du contrat 
	@NotNull
	@Enumerated(EnumType.STRING)
	public GestionDocEngagement gestionDocEngagement;
	
	// Document d'engagement utilisé pour la signature du contrat - Peut être null  
	@ManyToOne
	public EditionSpecifique engagement;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	// Indique si l'envoi des documents d'engagement en fin de contrat a été fait ou non 
	// L'envoi se fait uniquement dans le cas de la signature en ligne 
	public ChoixOuiNon notificationDoneDocEngagement = ChoixOuiNon.NON;

	

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	
	
	
	
	

}
