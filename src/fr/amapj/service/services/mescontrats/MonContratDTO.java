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
 package fr.amapj.service.services.mescontrats;

import fr.amapj.service.services.gestioncontrat.reglesaisie.VerifRegleSaisieModeleContratDTO;
import fr.amapj.service.services.mescontrats.inscription.InscriptionDTO;
import fr.amapj.service.services.stockservice.verifstock.VerifStockDTO;


/**
 * Cet objet est utilisé pour le chargement complet d'un contrat dans le but de le modifier (par l'adherent ou un tresorier) 
 *
 */
public class MonContratDTO 
{
	//
	public ContratDTO contratDTO;
	
	// Regle d'inscription dans le cas d'un adherent standard 
	public InscriptionDTO inscriptionDTO;
	
	// Les informations de verification des stocks
	public VerifStockDTO verifStockDTO;
	
	// Les informations de verification des quantités saisies
	public VerifRegleSaisieModeleContratDTO verifRegleSaisieDTO;

}
