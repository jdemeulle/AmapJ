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
 * Permet de definir le mode de calcul de la proposition de paiements 
 */
public enum SaisiePaiementProposition 
{
	REPARTI_STRICT ,
	
	REPARTI_ARRONDI ,

	PAYE_AVANCE_STRICT,
	
	PAYE_AVANCE_ARRONDI;
	
	
	static public class MetaData extends MetaDataEnum
	{
		
		public void fill()
		{		
			add("Ce champ vous permet de choisir comment sera calculé la proposition de paiement");
	
			add(REPARTI_STRICT,"Réparti sans arrondi","Dans ce mode, le paiement est réparti sur les dates de paiements, sans arrondi."
					+ " Par exemple, si le montant à payer est 100 euros et il y a 3 dates de paiements, alors la proposition sera 33.33 € puis 33.33 € puis 33.34 €.");
			
			add(REPARTI_ARRONDI,"Réparti avec arrondi","Dans ce mode, le paiement est réparti sur les dates de paiements, avec un arrondi à l'euro pour eviter les centimes."
					+ " Par exemple, si le montant à payer est 100 euros et il y a 3 dates de paiements, alors la proposition sera 33 € puis 33 € puis 34 €.");
			
			
			add(PAYE_AVANCE_STRICT,"Payé d'avance sans arrondi","Dans ce mode, les paiements sont répartis de telle façon que tout ce qui est consommé est payé d'avance, sans arrondi."
					+ " Par exemple, l'adhérent a commandé pour 15 euros le 02/02 , pour 12 euros le 09/02, pour 13 euros le 16/02. Il y a deux dates de paiement le 02/02 et le 16/02."
					+ " La proposition sera 27 euros (15 + 12)  pour le 02/02 et 13 euros pour le 16/02.");
			
			add(PAYE_AVANCE_ARRONDI,"Payé d'avance avec arrondi","Dans ce mode, les paiements sont répartis de telle façon que tout ce qui est consommé est payé d'avance, avec un arrondi à l'euro pour eviter les centimes."
					+ " Par exemple, l'adhérent a commandé pour 15.50 euros le 02/02 , pour 12 euros le 09/02, pour 13.50 euros le 16/02. Il y a deux dates de paiement le 02/02 et le 16/02."
					+ " La proposition sera 27 euros  pour le 02/02 (15.50+12 arrondi à l'euro inférieur) et 14 euros pour le 16/02 (13.50 + 0.50 de reliquat).");
			
			
		}
	}	

}
