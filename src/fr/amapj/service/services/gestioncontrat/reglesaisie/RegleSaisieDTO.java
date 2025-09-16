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
 package fr.amapj.service.services.gestioncontrat.reglesaisie;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.IdContainer;
import fr.amapj.model.models.contrat.modele.extendparam.reglesaisie.RSChampApplication;
import fr.amapj.model.models.contrat.modele.extendparam.reglesaisie.RSContrainteDate;
import fr.amapj.model.models.contrat.modele.extendparam.reglesaisie.RSContrainteOperateur;
import fr.amapj.model.models.contrat.modele.extendparam.reglesaisie.RSContrainteProduit;
import fr.amapj.model.models.contrat.modele.extendparam.reglesaisie.RegleSaisie;
import fr.amapj.model.models.param.ChoixOuiNon;


/**
 * 
 *
 */
public class RegleSaisieDTO 
{
	// Le produit concerné
	public Long produitId;
	
	// La liste des produits concernés
	public List<IdContainer> produitIds = new ArrayList<>();
	
	// La date concernée
	public Long modeleContratDateId;
	
	// La liste des dates concernées
	public List<IdContainer> modeleContratDateIds = new ArrayList<>();
	
	//  
	public RSContrainteDate contrainteDate;
	
	//  
	public RSContrainteProduit contrainteProduit;
	
	//  
	public RSContrainteOperateur contrainteOperateur;
	
	// La valeur cible 
	public int val;
	
	// Libellé calculé à partir des autres valeurs saisies
	public String libelle;
	
	
	// Le champ d'application 
	public RSChampApplication champApplication;
		
	// Un libellé personnalisé
	public String libPersonnalise;
	
	public ChoixOuiNon activateLibPersonnalise;
	
	
	// Getters and setters
	

	public Long getProduitId() {
		return produitId;
	}


	public void setProduitId(Long produitId) {
		this.produitId = produitId;
	}


	public List<IdContainer> getProduitIds() {
		return produitIds;
	}


	public void setProduitIds(List<IdContainer> produitIds) {
		this.produitIds = produitIds;
	}


	public Long getModeleContratDateId() {
		return modeleContratDateId;
	}


	public void setModeleContratDateId(Long modeleContratDateId) {
		this.modeleContratDateId = modeleContratDateId;
	}


	public List<IdContainer> getModeleContratDateIds() {
		return modeleContratDateIds;
	}


	public void setModeleContratDateIds(List<IdContainer> modeleContratDateIds) {
		this.modeleContratDateIds = modeleContratDateIds;
	}


	public RSContrainteDate getContrainteDate() {
		return contrainteDate;
	}


	public void setContrainteDate(RSContrainteDate contrainteDate) {
		this.contrainteDate = contrainteDate;
	}


	public RSContrainteProduit getContrainteProduit() {
		return contrainteProduit;
	}


	public void setContrainteProduit(RSContrainteProduit contrainteProduit) {
		this.contrainteProduit = contrainteProduit;
	}


	public RSContrainteOperateur getContrainteOperateur() {
		return contrainteOperateur;
	}


	public void setContrainteOperateur(RSContrainteOperateur contrainteOperateur) {
		this.contrainteOperateur = contrainteOperateur;
	}


	public int getVal() {
		return val;
	}


	public void setVal(int val) {
		this.val = val;
	}


	public String getLibelle() {
		return libelle;
	}


	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}


	public RSChampApplication getChampApplication() {
		return champApplication;
	}


	public void setChampApplication(RSChampApplication champApplication) {
		this.champApplication = champApplication;
	}


	public String getLibPersonnalise() {
		return libPersonnalise;
	}


	public void setLibPersonnalise(String libPersonnalise) {
		this.libPersonnalise = libPersonnalise;
	}


	public ChoixOuiNon getActivateLibPersonnalise() {
		return activateLibPersonnalise;
	}


	public void setActivateLibPersonnalise(ChoixOuiNon activateLibPersonnalise) {
		this.activateLibPersonnalise = activateLibPersonnalise;
	}	
	
	
	
	
}
