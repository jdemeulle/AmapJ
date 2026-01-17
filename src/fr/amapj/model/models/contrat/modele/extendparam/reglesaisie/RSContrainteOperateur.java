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

import fr.amapj.model.engine.metadata.MetaDataEnum;

/**
 * Mode de gestion des stocks pour ce produit 
 */
public enum RSContrainteOperateur
{
	
	EGAL,
	 
	SUPERIEUR_OU_EGAL,
	
	INFERIEUR_OU_EGAL,
	
	MULTIPLE_DE,
	;
	
	
	static public class MetaData extends MetaDataEnum
	{
		public void fill()
		{		
			add(EGAL, "Egale à ","");
			
			add(SUPERIEUR_OU_EGAL, "Supérieur ou égal à ","");
			
			add(INFERIEUR_OU_EGAL, "Inférieur ou égal à ","");
			
			add(MULTIPLE_DE, "Multiple de ","");
			
		}
	}		
}
