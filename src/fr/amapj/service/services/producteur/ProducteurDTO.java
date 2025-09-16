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
 package fr.amapj.service.services.producteur;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.amapj.common.CollectionUtils;
import fr.amapj.model.models.fichierbase.EtatProducteur;
import fr.amapj.model.models.fichierbase.ProducteurStockGestion;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.model.models.param.ChoixOuiNon;
import fr.amapj.service.services.utilisateur.util.UtilisateurUtil;
import fr.amapj.view.engine.tools.TableItem;

/**
 * Permet la gestion des utilisateurs en masse
 * ou du changement de son état
 * 
 */
public class ProducteurDTO implements TableItem
{
	public Long id;
	
	
	/* General */
	
	public String nom;
	
	public String description;
	
	public Date dateCreation;
	
	public Date dateModification;
	
	public EtatProducteur etat;
	
	
	
	/* Documents */ 
	
		// Feuille distribution
	
	public ChoixOuiNon feuilleDistributionGrille;
	
	public ChoixOuiNon feuilleDistributionListe;
	
	public ChoixOuiNon feuilleDistributionEtiquette;
	
	public Long idEtiquette;
	
	// Libelle qui sera utilisé sur le contrat d'engagement si il y en a un 
	public String libContrat;
	
	// Envoi des documents
	public int delaiModifContrat;
	
	
	/* referents */
	public List<ProdUtilisateurDTO> referents = new ArrayList<>();
	
	/* utilisateurs */
	
	public List<ProdUtilisateurDTO> utilisateurs = new ArrayList<>();
		

	
	// Pour affichage dans ProducteurListPart 
	public String getUtilisateurInfo()
	{
		return CollectionUtils.asString(utilisateurs, ",", e->e.nom+" "+e.prenom);
	}
	
	// Pour affichage dans ProducteurListPart 
	public String getReferentInfo()
	{
		return CollectionUtils.asString(referents, ",", e->e.nom+" "+e.prenom);
	}

	// Statistiques de livraison
	
	// Date de la dernière livraison 
	public Date dateDerniereLivraison;
	
	// Nombre de modele de contrats actifs 
	public int nbModeleContratActif;
	
	// Stock 
	public ProducteurStockGestion gestionStock;
	
	
	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getNom()
	{
		return nom;
	}

	public void setNom(String nom)
	{
		this.nom = nom;
	}

	public int getDelaiModifContrat()
	{
		return delaiModifContrat;
	}

	public void setDelaiModifContrat(int delaiModifContrat)
	{
		this.delaiModifContrat = delaiModifContrat;
	}

	public List<ProdUtilisateurDTO> getReferents()
	{
		return referents;
	}

	public void setReferents(List<ProdUtilisateurDTO> referents)
	{
		this.referents = referents;
	}

	public List<ProdUtilisateurDTO> getUtilisateurs()
	{
		return utilisateurs;
	}

	public void setUtilisateurs(List<ProdUtilisateurDTO> utilisateurs)
	{
		this.utilisateurs = utilisateurs;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public Long getIdEtiquette()
	{
		return idEtiquette;
	}

	public void setIdEtiquette(Long idEtiquette)
	{
		this.idEtiquette = idEtiquette;
	}

	public String getLibContrat()
	{
		return libContrat;
	}

	public void setLibContrat(String libContrat)
	{
		this.libContrat = libContrat;
	}

	public ChoixOuiNon getFeuilleDistributionGrille()
	{
		return feuilleDistributionGrille;
	}

	public void setFeuilleDistributionGrille(ChoixOuiNon feuilleDistributionGrille)
	{
		this.feuilleDistributionGrille = feuilleDistributionGrille;
	}

	public ChoixOuiNon getFeuilleDistributionListe()
	{
		return feuilleDistributionListe;
	}

	public void setFeuilleDistributionListe(ChoixOuiNon feuilleDistributionListe)
	{
		this.feuilleDistributionListe = feuilleDistributionListe;
	}

	public ChoixOuiNon getFeuilleDistributionEtiquette()
	{
		return feuilleDistributionEtiquette;
	}

	public void setFeuilleDistributionEtiquette(ChoixOuiNon feuilleDistributionEtiquette)
	{
		this.feuilleDistributionEtiquette = feuilleDistributionEtiquette;
	}

	public Date getDateCreation() 
	{
		return dateCreation;
	}

	public void setDateCreation(Date dateCreation) 
	{
		this.dateCreation = dateCreation;
	}

	public Date getDateDerniereLivraison() 
	{
		return dateDerniereLivraison;
	}

	public void setDateDerniereLivraison(Date dateDerniereLivraison) 
	{
		this.dateDerniereLivraison = dateDerniereLivraison;
	}

	public int getNbModeleContratActif() 
	{
		return nbModeleContratActif;
	}

	public void setNbModeleContratActif(int nbModeleContratActif) 
	{
		this.nbModeleContratActif = nbModeleContratActif;
	}

	public ProducteurStockGestion getGestionStock()
	{
		return gestionStock;
	}

	public void setGestionStock(ProducteurStockGestion gestionStock)
	{
		this.gestionStock = gestionStock;
	}
	
	
}
