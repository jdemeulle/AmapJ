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
 package fr.amapj.model.models.param.paramecran;

import fr.amapj.model.engine.metadata.MetaDataEnum;
import fr.amapj.model.models.param.paramecran.common.AbstractParamEcran;



/**
 * Parametrage de l'écran remises des chèques
 */
public class PERemiseProducteur  extends AbstractParamEcran
{
	
	// Acces à cet écran 
	public PERemiseProducteurAccess accesEcran = PERemiseProducteurAccess.REFERENT;
	
	static public enum PERemiseProducteurAccess
	{
		REFERENT , 
		
		PRODUCTEUR ;
		
		static public class MetaData extends MetaDataEnum
		{
			
			public void fill()
			{		
				add("Ce champ vous permet de choisir les personnes pouvant accéder à l'écran Remise des chèques");

				add(REFERENT, "Référent, Trésorier, Admin" , "Dans ce mode, cet écran est accessible aux  référents, trésoriers et admin. C'est le cas standard. Un référent ne peut accéder qu'à ses producteurs, il ne peut pas modifier les autres producteurs.");
				
				add(PRODUCTEUR, "Producteur, Référent, Trésorier, Admin" , "Dans ce mode, cet écran est accessible aux producteurs , référents, trésoriers et admin. C'est un cas assez rare, où le producteur gère lui même la remise des chèques. Un producteur peut accéder uniquement aux chèques qui le concernent directement.");
				
			}
		}	
	}

	public PERemiseProducteurAccess getAccesEcran() 
	{
		return accesEcran;
	}

	public void setAccesEcran(PERemiseProducteurAccess accesEcran) 
	{
		this.accesEcran = accesEcran;
	}
}
