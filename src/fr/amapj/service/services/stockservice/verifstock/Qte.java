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
 package fr.amapj.service.services.stockservice.verifstock;

import fr.amapj.common.AmapjRuntimeException;

public class Qte 
{
	public double qte;
	
	public Qte(double qte) 
	{
		this.qte = qte;
	}

	public void decr(double d)
	{
		qte = qte -d;
	}

	public boolean isNegative() 
	{
		return qte<-0.01d;
	}

	// Calcul le restant diponible
	// Retourne le double arrondi à l'entier inférieur
	// Retourne 0 si la valeur est négative 
	public int getRestant(double coefficient) 
	{
		// Ne devrait pas arriver 
		if (coefficient==0) 
		{
			throw new AmapjRuntimeException("Coefficient =0");
		}
		
		double a = qte / coefficient;
		
		int r = (int) Math.floor(a+0.01d);
		if (r<0)
		{
			r = 0;
		}
		return r;
	}

	public boolean greaterOrEqual(double ref) 
	{
		return qte>ref-0.01d;
	}
}
