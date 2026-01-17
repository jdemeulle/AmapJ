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
 * Regle de saisie : contrainte sur la date 
 */
public enum RSContrainteDate
{
	// 
	POUR_CHAQUE_DATE,
	
	POUR_TOUT_CONTRAT,
	
	POUR_UNE_DATE,
	
	POUR_PLUSIEURS_DATES,
	
	;
	
	
	static public class MetaData extends MetaDataEnum
	{
		public void fill()
		{		
			add(POUR_CHAQUE_DATE, "Pour chaque date de livraison","La règle devra être respectée pour chaque date de livraison");
			
			add(POUR_TOUT_CONTRAT, "Pour tout le contrat","La régle s'applique aux quantités globales sur toutes les dates du contrat");
			
			add(POUR_UNE_DATE, "Pour une date de livraison précise","La régle s'applique sur une date bien précise que vous allez indiquer.");
			
			add(POUR_PLUSIEURS_DATES, "Pour plusieurs dates de livraisons précises","La régle s'applique aux quantités globales sur plusieurs dates du contrat, que vous allez préciser.");
			
		}
	}		
}
