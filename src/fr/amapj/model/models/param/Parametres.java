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
 package fr.amapj.model.models.param;

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
 * Paramètres généraux de l'application 
 * 
 *
 */
@Entity
public class Parametres implements Identifiable
{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	/**
	 * Nom de l'AMAP
	 */
	@Size(min = 0, max = 100)
	@Column(length = 100)
	public String nomAmap;
	
	/**
	 * Ville de l'AMAP
	 */
	@Size(min = 0, max = 200)
	@Column(length = 200)
	public String villeAmap;
	
	/**
	 * Envoi de mail
	 */
	@Size(min = 0, max = 255)
	@Column(length = 255)
	public String sendingMailUsername;
	
	@Size(min = 0, max = 255)
	@Column(length = 255)
	public String sendingMailPassword;
	
	// Nombre maximum de mail qu'il est possible d'envoyer par jour 
	public int sendingMailNbMax;
	
	@Size(min = 0, max = 2048)
	@Column(length = 2048)
	public String sendingMailFooter;
	
	@Size(min = 0, max = 255)
	@Column(length = 255)
	public String mailCopyTo;
	
	/**
	 * Type du serveur pour l'envoi des mails
	 */
	@NotNull
	@Enumerated(EnumType.STRING)
	public SmtpType smtpType;
	
	/**
	 * Url de l'application visible dans les mails
	 */
	@Size(min = 0, max = 255)
	@Column(length = 255)
	public String url;
	

	/**
	 * Destinataire de la sauvegarde
	 */
	@Size(min = 0, max = 255)
	@Column(length = 255)
	public String backupReceiver;

	
	// Partie gestion des permanences
	
	/**
	 * Activation ou désactivation du module "Planning de distribution"
	 */
	@NotNull
	@Enumerated(EnumType.STRING)
	public EtatModule etatPlanningDistribution;
	
	/**
	 * Activation ou désactivation du module "Gestion des cotisations"
	 */
	@NotNull
	@Enumerated(EnumType.STRING)
	public EtatModule etatGestionCotisation;
	
	/**
	 * Envoi des mails pour le rappel de permanence
	 */
	@NotNull
	@Enumerated(EnumType.STRING)
	public ChoixOuiNon envoiMailRappelPermanence;
	
	
	public int delaiMailRappelPermanence;
	
	/**
	 * Titre du mail pour le rappel de permanence
	 */
	@Size(min = 0, max = 2048)
	@Column(length = 2048)
	public String titreMailRappelPermanence;
	
	/**
	 * Contenu du mail pour le rappel de permanence
	 */
	@Size(min = 0, max = 20480)
	@Column(length = 20480)
	public String contenuMailRappelPermanence;
	
	
	// Partie envoi des mails périodiques
	
	@NotNull
	@Enumerated(EnumType.STRING)
	public ChoixOuiNon envoiMailPeriodique;
	
	
	/**
	 * Numéro du jour dans le mois 
	 */
	public int numJourDansMois;
	
	/**
	 * Titre du mail pour le mail periodique
	 */
	@Size(min = 0, max = 2048)
	@Column(length = 2048)
	public String titreMailPeriodique;
	
	/**
	 * Contenu du mail pour le mail periodique
	 */
	@Size(min = 0, max = 20480)
	@Column(length = 20480) 
	public String contenuMailPeriodique;
	
	
	/**
	 * Délai en jours avant archivage d'un contrat après la dernière livraison
	 * Valeur par défaut : 185 jours (6 mois) 
	 */
	@NotNull
	public Integer archivageContrat;
	
	/**
	 * Délai en jours avant suppression d'un contrat après la dernière livraison
	 * Valeur par défaut : 730 jours (2 an)  
	 */
	@NotNull
	public Integer suppressionContrat;
	
	/**
	 * Délai en jours avant archivage d'un utilisateur après la dernière livraison
	 * et après la fin de la dernière adhesion  
	 * Valeur par défaut : 90 jours (3 mois) 
	 */
	@NotNull
	public Integer archivageUtilisateur;
	
	/**
	 * Délai en jours avant archivage d'un producteur après la dernière livraison
	 * Valeur par défaut : 365 jours (1 an) 
	 */
	@NotNull
	public Integer archivageProducteur;
	
	
	/**
	 * Délai en jours avant suppression d'une période de permanence
	 * Valeur par défaut : 730 jours (2 an)  
	 */
	@NotNull
	public Integer suppressionPeriodePermanence;
	
	/**
	 * Délai en jours avant suppression d'une période de cotisation
	 * Valeur par défaut : 1095 jours (3 an) 
	 */
	@NotNull
	public Integer suppressionPeriodeCotisation;
	
	
	/**
	 * Activation ou désactivation du module "Gestion des stocks (quantités limites)"
	 */
	@NotNull
	@Enumerated(EnumType.STRING)
	public EtatModule etatGestionStock;
	
	
	public Long getId()
	{
		return id;
	}


	public void setId(Long id)
	{
		this.id = id;
	}

	
	
	
}
