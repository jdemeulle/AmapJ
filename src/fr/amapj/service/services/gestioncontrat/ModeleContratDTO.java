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
 package fr.amapj.service.services.gestioncontrat;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.amapj.model.models.contrat.modele.AffichageMontant;
import fr.amapj.model.models.contrat.modele.GestionPaiement;
import fr.amapj.model.models.contrat.modele.GestionDocEngagement;
import fr.amapj.model.models.contrat.modele.JokerMode;
import fr.amapj.model.models.contrat.modele.NatureContrat;
import fr.amapj.model.models.contrat.modele.RetardataireAutorise;
import fr.amapj.model.models.contrat.modele.SaisiePaiementCalculDate;
import fr.amapj.model.models.contrat.modele.SaisiePaiementModifiable;
import fr.amapj.model.models.contrat.modele.SaisiePaiementProposition;
import fr.amapj.model.models.contrat.modele.StockGestion;
import fr.amapj.model.models.contrat.modele.StockIdentiqueDate;
import fr.amapj.model.models.contrat.modele.StockMultiContrat;
import fr.amapj.model.models.contrat.modele.StrategiePaiement;
import fr.amapj.model.models.contrat.modele.TypJoker;
import fr.amapj.view.views.gestioncontrat.editorpart.FrequenceLivraison;

/**
 * Bean permettant l'edition des modeles de contrats
 *
 */
public class ModeleContratDTO
{
	public ModeleContratDTO()
	{
		
	}
	
	public Long id;
	
	public String nom;
	
	public String description;

	public Long producteur;
	
	public Date dateFinInscription;
	
	public int cartePrepayeeDelai;
	
	public FrequenceLivraison frequence;
	
	public GestionPaiement gestionPaiement;
	
	public SaisiePaiementProposition saisiePaiementProposition; 
	
	public int montantChequeMiniCalculProposition;
	
	public SaisiePaiementModifiable saisiePaiementModifiable;
	
	public NatureContrat nature;
	
	public Date dateDebut;
	
	public Date dateFin;
	
	public String libCheque;
	
	public Date dateRemiseCheque;
	
	public String textPaiement;
	
	public AffichageMontant affichageMontant;
	
	public TypJoker typJoker;
	
	public int jokerNbMin = 0;
	
	public int jokerNbMax = 0;
	
	public JokerMode jokerMode;
	
	public int jokerDelai;
	
	public Long idPeriodeCotisation;
	
	public RetardataireAutorise retardataireAutorise;
	
	public StrategiePaiement strategiePaiement;
	
	public SaisiePaiementCalculDate saisiePaiementCalculDate;
	
	public List<DateModeleContratDTO> dateLivs = new ArrayList<DateModeleContratDTO>();

	public List<LigneContratDTO> produits = new ArrayList<LigneContratDTO>();
	
	public List<DatePaiementModeleContratDTO> datePaiements = new ArrayList<DatePaiementModeleContratDTO>();

	
	// Gestion du stock 
	public StockGestion stockGestion;
	public StockIdentiqueDate stockIdentiqueDate;
	public StockMultiContrat stockMultiContrat;
	
	// Gestion des documents d'engagement
	public GestionDocEngagement gestionDocEngagement;
	public Long idEngagement;
	
	
	// Getters and setters
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

	public Long getProducteur()
	{
		return producteur;
	}

	public void setProducteur(Long producteur)
	{
		this.producteur = producteur;
	}

	public Date getDateFinInscription()
	{
		return dateFinInscription;
	}

	public void setDateFinInscription(Date dateFinInscription)
	{
		this.dateFinInscription = dateFinInscription;
	}

	public Date getDateDebut()
	{
		return dateDebut;
	}

	public void setDateDebut(Date dateDebut)
	{
		this.dateDebut = dateDebut;
	}

	public Date getDateFin()
	{
		return dateFin;
	}

	public void setDateFin(Date dateFin)
	{
		this.dateFin = dateFin;
	}

	public List<LigneContratDTO> getProduits()
	{
		return produits;
	}

	public void setProduits(List<LigneContratDTO> produits)
	{
		this.produits = produits;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public FrequenceLivraison getFrequence()
	{
		return frequence;
	}

	public void setFrequence(FrequenceLivraison frequence)
	{
		this.frequence = frequence;
	}

	public String getLibCheque()
	{
		return libCheque;
	}

	public void setLibCheque(String libCheque)
	{
		this.libCheque = libCheque;
	}

	public Date getDateRemiseCheque()
	{
		return dateRemiseCheque;
	}

	public void setDateRemiseCheque(Date dateRemiseCheque)
	{
		this.dateRemiseCheque = dateRemiseCheque;
	}

	public GestionPaiement getGestionPaiement()
	{
		return gestionPaiement;
	}

	public void setGestionPaiement(GestionPaiement gestionPaiement)
	{
		this.gestionPaiement = gestionPaiement;
	}

	public String getTextPaiement()
	{
		return textPaiement;
	}

	public void setTextPaiement(String textPaiement)
	{
		this.textPaiement = textPaiement;
	}

	public List<DateModeleContratDTO> getDateLivs()
	{
		return dateLivs;
	}

	public void setDateLivs(List<DateModeleContratDTO> dateLivs)
	{
		this.dateLivs = dateLivs;
	}

	public List<DatePaiementModeleContratDTO> getDatePaiements()
	{
		return datePaiements;
	}

	public void setDatePaiements(List<DatePaiementModeleContratDTO> datePaiements)
	{
		this.datePaiements = datePaiements;
	}

	public NatureContrat getNature()
	{
		return nature;
	}

	public void setNature(NatureContrat nature)
	{
		this.nature = nature;
	}

	public int getCartePrepayeeDelai()
	{
		return cartePrepayeeDelai;
	}

	public void setCartePrepayeeDelai(int cartePrepayeeDelai)
	{
		this.cartePrepayeeDelai = cartePrepayeeDelai;
	}

	public TypJoker getTypJoker()
	{
		return typJoker;
	}

	public void setTypJoker(TypJoker typJoker)
	{
		this.typJoker = typJoker;
	}

	public int getJokerNbMin()
	{
		return jokerNbMin;
	}

	public void setJokerNbMin(int jokerNbMin)
	{
		this.jokerNbMin = jokerNbMin;
	}

	public int getJokerNbMax()
	{
		return jokerNbMax;
	}

	public void setJokerNbMax(int jokerNbMax)
	{
		this.jokerNbMax = jokerNbMax;
	}

	public JokerMode getJokerMode()
	{
		return jokerMode;
	}

	public void setJokerMode(JokerMode jokerMode)
	{
		this.jokerMode = jokerMode;
	}

	public int getJokerDelai()
	{
		return jokerDelai;
	}

	public void setJokerDelai(int jokerDelai)
	{
		this.jokerDelai = jokerDelai;
	}

	public Long getIdPeriodeCotisation() 
	{
		return idPeriodeCotisation;
	}

	public void setIdPeriodeCotisation(Long idPeriodeCotisation) 
	{
		this.idPeriodeCotisation = idPeriodeCotisation;
	}

	public RetardataireAutorise getRetardataireAutorise()
	{
		return retardataireAutorise;
	}

	public void setRetardataireAutorise(RetardataireAutorise retardataireAutorise)
	{
		this.retardataireAutorise = retardataireAutorise;
	}

	public SaisiePaiementModifiable getSaisiePaiementModifiable() {
		return saisiePaiementModifiable;
	}

	public void setSaisiePaiementModifiable(SaisiePaiementModifiable saisiePaiementModifiable) {
		this.saisiePaiementModifiable = saisiePaiementModifiable;
	}

	public SaisiePaiementProposition getSaisiePaiementProposition() {
		return saisiePaiementProposition;
	}

	public void setSaisiePaiementProposition(SaisiePaiementProposition saisiePaiementProposition) {
		this.saisiePaiementProposition = saisiePaiementProposition;
	}

	public int getMontantChequeMiniCalculProposition() {
		return montantChequeMiniCalculProposition;
	}

	public void setMontantChequeMiniCalculProposition(int montantChequeMiniCalculProposition) {
		this.montantChequeMiniCalculProposition = montantChequeMiniCalculProposition;
	}

	public StrategiePaiement getStrategiePaiement() {
		return strategiePaiement;
	}

	public void setStrategiePaiement(StrategiePaiement strategiePaiement) {
		this.strategiePaiement = strategiePaiement;
	}

	public SaisiePaiementCalculDate getSaisiePaiementCalculDate() {
		return saisiePaiementCalculDate;
	}

	public void setSaisiePaiementCalculDate(SaisiePaiementCalculDate saisiePaiementCalculDate) {
		this.saisiePaiementCalculDate = saisiePaiementCalculDate;
	}

	public StockGestion getStockGestion()
	{
		return stockGestion;
	}

	public void setStockGestion(StockGestion stockGestion)
	{
		this.stockGestion = stockGestion;
	}

	public StockIdentiqueDate getStockIdentiqueDate()
	{
		return stockIdentiqueDate;
	}

	public void setStockIdentiqueDate(StockIdentiqueDate stockIdentiqueDate)
	{
		this.stockIdentiqueDate = stockIdentiqueDate;
	}

	public StockMultiContrat getStockMultiContrat()
	{
		return stockMultiContrat;
	}

	public void setStockMultiContrat(StockMultiContrat stockMultiContrat)
	{
		this.stockMultiContrat = stockMultiContrat;
	}

	public AffichageMontant getAffichageMontant() 
	{
		return affichageMontant;
	}

	public void setAffichageMontant(AffichageMontant affichageMontant) 
	{
		this.affichageMontant = affichageMontant;
	}

	public Long getIdEngagement() 
	{
		return idEngagement;
	}

	public void setIdEngagement(Long idEngagement) 
	{
		this.idEngagement = idEngagement;
	}

	public GestionDocEngagement getGestionDocEngagement() 
	{
		return gestionDocEngagement;
	}

	public void setGestionDocEngagement(GestionDocEngagement gestionDocEngagement) 
	{
		this.gestionDocEngagement = gestionDocEngagement;
	}

	
	
	
}
