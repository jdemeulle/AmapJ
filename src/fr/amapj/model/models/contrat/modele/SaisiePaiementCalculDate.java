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

import fr.amapj.model.engine.metadata.MetaDataEnum;

/**
 * Permet de definir le mode de calcul des dates
 */
public enum SaisiePaiementCalculDate 
{
	UN_PAIEMENT_PAR_MOIS_PREMIERE_LIVRAISON ,
	
	UN_PAIEMENT_PAR_MOIS_DEBUT_MOIS ,
	
	UN_PAIEMENT_PAR_LIVRAISON,
	
	AUTRE;
		
	
	static public class MetaData extends MetaDataEnum
	{
		
		public void fill()
		{		
			add("Ce champ vous permet de choisir comment seront calculés les dates de paiement");
	
			add(UN_PAIEMENT_PAR_MOIS_PREMIERE_LIVRAISON,"Un paiement chaque 1ere livraison du mois","Dans ce mode, les dates de paiements sont à la première livraison de chaque mois.");
			
			add(UN_PAIEMENT_PAR_MOIS_DEBUT_MOIS,"Un paiement chaque 1er jour du mois","Dans ce mode, les dates de paiements sont le 1er jour du mois.");
			
			add(UN_PAIEMENT_PAR_LIVRAISON,"Un paiement à chaque livraison","Dans ce mode, les dates de paiements sont toutes les dates de livraison.");
			
			add(AUTRE,"Saisie libre","Dans ce mode, les dates de paiements sont saisies librement.");
			
			
			
			
		}
	}	

}
