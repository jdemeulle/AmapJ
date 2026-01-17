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
 package fr.amapj.view.views.saisiecontrat.abo.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Données concernant les contrats de type abonnement et qui seront serialisés en base au format GSON
 * 
 * Attention : faire des modifs avec précaution ! 
 *
 */
public class AboData
{
	// On sauvegarde les quantités pour chaque produit , SAUF ceux qui ne sont pas commandés
	public List<AboDataProd> prods = new ArrayList<>();
	
	// On sauvegarde toutes les dates, SAUF celles de type normal 
	public List<AboDataLig> ligs = new ArrayList<>();
	

}
