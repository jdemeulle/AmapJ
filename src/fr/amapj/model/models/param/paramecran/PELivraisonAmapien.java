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
 * Parametrage de l'écran Livraison d'un amapien
 */
public class PELivraisonAmapien  extends AbstractParamEcran
{
	// Mode de l'affichage  
	public GapViewer modeAffichage = GapViewer.WEEK;
	
	
	// Acces à cet écran 
	public PELivraisonAmapienAccess accesEcran = PELivraisonAmapienAccess.REFERENT;
	

	public GapViewer getModeAffichage()
	{
		return modeAffichage;
	}

	public void setModeAffichage(GapViewer modeAffichage)
	{
		this.modeAffichage = modeAffichage;
	}
	
	
	public PELivraisonAmapienAccess getAccesEcran() 
	{
		return accesEcran;
	}

	public void setAccesEcran(PELivraisonAmapienAccess accesEcran) 
	{
		this.accesEcran = accesEcran;
	}




	static public enum PELivraisonAmapienAccess
	{
		REFERENT , 
		
		ALL ;
		
		static public class MetaData extends MetaDataEnum
		{
			
			public void fill()
			{		
				add("Ce champ vous permet de choisir les personnes pouvant accéder à l'écran Livraisons d'un amapien");

				add(REFERENT, "Referent,Trésorier,Admin" , "Dans ce mode, cet écran est accessible aux référents, trésoriers et admin. C'est le cas standard.");
				
				add(ALL, "Tout le monde" , "Dans ce mode, cet écran est accessible à tous.");
				
				
			}
		}	
	}
}
