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
 * Regle de saisie : champ d'application 
 */
public enum RSChampApplication
{
	// 
	AMAPIEN,
	
	TOUS
		
	;
	
	
	static public class MetaData extends MetaDataEnum
	{
		public void fill()
		{		
			add("Ce champ vous permet d'indiquer à quel moment s'applique la régle.");

			add(AMAPIEN, "Amapien","La régle s'applique quand l'amapien saisit son contrat. "
					+ "La régle ne s'applique pas au référent ou trésorier, qui peut donc saisir des contrats qui ne respectent pas cette régle, en allant dans Gestion des contrats signés puis Ajouter un contrat");
			
			add(TOUS, "Tous","La régle s'applique à tous, y compris le référent et le trésorier");
			
			
		}
	}		
}
