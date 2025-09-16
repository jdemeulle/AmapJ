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
 package fr.amapj.service.services.mespaiements.reception;


import java.util.Date;

import fr.amapj.model.models.contrat.reel.EtatPaiement;

/**
 * Informations sur les paiements d'un contrat ou d'un modele contrat
 * pour permettre la réception (soit d'un contrat, soit d'un modele de contrat)
 *
 */
public class ReceptionChequeDTO
{
	// 
	public Long idPaiement;

	// 
	public Date datePaiement;
	
	// 
	public int montant;
	
	// Contient l'état du paiement
	public EtatPaiement etatPaiement;
	
	
	public String commentaire1;
	
	public String commentaire2;
	
	public String commentaire3;
	
	public String commentaire4;

	
	public String nomUtilisateur;
	
	public String prenomUtilisateur; 
}
