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
 package fr.amapj.service.services.docengagement.signonline.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Données concernant les documents d'engagements et qui seront serialisés en base au format GSON
 * 
 * Attention : faire des modifs avec précaution ! 
 *
 */
public class DocEngagementData
{	
	// Nombre de page de la partie 1 du document (le document d'engagement initial)
	// Doit être strictement positif 
	public int part1NbPage;
	
	// On sauvegarde la liste des champs utilisés dans la partie 1 du document , et la valeur associée à chaque champ , lors de la création du document initial
	public List<DocEngagementVar> part1Vars = new ArrayList<>();
	
	// Hash de la partie 2 du document (l'avenant)
	// Est null si il n'y a pas d'avenant 
	// Le hash correspond au hash du html ayant permis la generation du pdf de l'avenant 
	public String part2Hash;

}
