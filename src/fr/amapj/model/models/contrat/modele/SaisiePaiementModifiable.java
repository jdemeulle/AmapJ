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
 * Permet de definir le mode de saisies des paiements 
 */
public enum SaisiePaiementModifiable 
{
	// L'adherent peut librement modifier les paiements lors de son inscription 
	MODIFIABLE ,
	
	//L'adherent ne peut pas modifier les paiements  lors de son inscription
	NON_MODIFIABLE ;
	
	// L'adhérent peut juste modifier le nombre de chèques
	// TODO MODIF_NB_CHEQUES;
	
	static public class MetaData extends MetaDataEnum
	{
		
		public void fill()
		{		
			add("Ce champ vous permet de choisir comment l'adhérent va pouvoir saisir ses paiements");
		
			
			add(MODIFIABLE,"Modifiable","Dans ce mode, une proposition de paiement est faite et est proposée à l'adhérent. Celui ci peut modifier librement cette proposition.");
			add(NON_MODIFIABLE,"Non modifiable","Dans ce mode, une proposition de paiement est faite et est proposée à l'adhérent. Celui ci NE peut PAS cette proposition.");
			
		}
	}	

}
