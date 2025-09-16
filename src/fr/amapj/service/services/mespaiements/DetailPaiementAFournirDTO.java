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
 package fr.amapj.service.services.mespaiements;

import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;


/**
 * 
 *
 */
public class DetailPaiementAFournirDTO
{
	// liste des jours du paiement sous la forme "12 janvier 2014 , 01 fevrier 2014 , .."
	public String datePaiements;
	
	// Montant du paiement
	public int montant;
	
	// Nombre de cheque
	public int nbCheque;
	

	public String formatPaiement()
	{
		String mt = new CurrencyTextFieldConverter().convertToString(montant)+" €";
		String str ;
		if (nbCheque==1)
		{
			str = "1 chèque de "+mt+" qui sera débité le "+datePaiements;
			
		}
		else	
		{
			str = ""+nbCheque+" chèques de "+mt+" qui seront débités les "+datePaiements;
		}
		return str;
	}
	
			
}
