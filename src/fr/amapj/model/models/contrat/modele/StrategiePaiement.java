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


public enum StrategiePaiement
{
	// Pas de gestion des paiements
	NON_GERE,
	
	//
	UN_CHEQUE_PAR_MOIS_LISSE_MODIFIABLE_1ERE_LIVRAISON,
		
	//
	UN_CHEQUE_PAR_MOIS_LISSE_NON_MODIFIABLE_1ERE_LIVRAISON,
	
	//
	UN_PAIEMENT_PAR_LIVRAISON,
	
	// 
	UN_CHEQUE_PAR_MOIS_LISSE_MODIFIABLE_1ER_MOIS,
	
	PLUS_DE_CHOIX;
	
	static public class MetaData extends MetaDataEnum
	{
		
		public void fill()
		{		
			add("Ce champ vous permet de choisir comment vous allez gérer le paiement des adhérents");
		
			add(NON_GERE,"Pas de gestion des paiements","Dans ce mode, vous ne gérez pas les paiements avec AmapJ. Par contre, en fin de saisie du contrat, vous pouvez afficher un message spécifique qui vous permettra d'indiquer comment se fait le paiement.");
			
			add(UN_CHEQUE_PAR_MOIS_LISSE_MODIFIABLE_1ERE_LIVRAISON,"Un chèque chaque première livraison du mois, lissé, modifiable","Dans ce mode, AmapJ va proposer un paiement avec 1 chèque par mois, à la date de la première livraison du mois. "
					+ "Le montant des chèques sera calculé automatiquement par AmapJ, avec une répartition égale sur chaque mois, avec un arrondi à l'euro pour eviter les centimes."
					+ " Par exemple, si le montant à payer est 100 euros et il y a 3 dates de paiements, alors la proposition sera 34 € puis 34 € puis 32 €."
					+ " Un montant minimum des chèques est pris en compte pour éviter de faire trop de petits chèques."
					+ " L'adhérent peut modifier librement cette proposition de paiement s'il le souhaite.");
						
			add(UN_CHEQUE_PAR_MOIS_LISSE_NON_MODIFIABLE_1ERE_LIVRAISON,"Un chèque chaque première livraison du mois, lissé, non modifiable","Ce mode est identique au mode précédent \"Un chèque chaque première livraison, lissé, modifiable\", "
					+ "mais l'utilisateur ne peut pas modifier cette proposition de paiement.");
			
			add(UN_PAIEMENT_PAR_LIVRAISON,"Un paiement par livraison","Dans ce mode, AmapJ va proposer un paiement avec 1 chèque par date de livraison. "
					+ "Le montant des chèques sera calculé automatiquement par AmapJ, le montant du chèque est strictement égal au montant  la livraison du jour."
					+ " L'adhérent ne peut pas modifier cette proposition de paiement.");
			
			add(UN_CHEQUE_PAR_MOIS_LISSE_MODIFIABLE_1ER_MOIS,"Un chèque chaque 1er du mois, lissé, modifiable","Dans ce mode, AmapJ va proposer un paiement avec 1 chèque par mois, le 1er du mois. "
					+ "Le montant des chèques sera calculé automatiquement par AmapJ, avec une répartition égale sur chaque mois, avec un arrondi à l'euro pour eviter les centimes."
					+ " Par exemple, si le montant à payer est 100 euros et il y a 3 dates de paiements, alors la proposition sera 34 € puis 34 € puis 32 €."
					+ " Un montant minimum des chèques est pris en compte pour éviter de faire trop de petits chèques."
					+ " L'adhérent peut modifier librement cette proposition de paiement s'il le souhaite.");

			
			add(PLUS_DE_CHOIX,"Me laisser choisir ...","Dans ce mode, vous allez pouvoir régler plus finement la stratégie de paiement.");
			
		}
	}	
	
}
