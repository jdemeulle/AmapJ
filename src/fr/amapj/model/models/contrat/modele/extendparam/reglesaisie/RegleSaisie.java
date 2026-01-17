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
 package fr.amapj.model.models.contrat.modele.extendparam.reglesaisie;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class RegleSaisie
{
	// La liste des produits concernés
	public List<Long> produitIds = new ArrayList<>();
	
	// La liste des dates concernées
	public List<Long> modeleContratDateIds = new ArrayList<>();
	
	//  
	public RSContrainteDate contrainteDate;
	
	//  
	public RSContrainteProduit contrainteProduit;
	
	//  
	public RSContrainteOperateur contrainteOperateur;
	
	// La valeur cible 
	public int val;
	
	// Le champ d'application 
	public RSChampApplication champApplication;
	
	// Un libellé personnalisé
	public String libPersonnalise;
	

}
