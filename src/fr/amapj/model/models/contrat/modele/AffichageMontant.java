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


public enum AffichageMontant
{
	//
	MONTANT_TOTAL,
	
	MONTANT_PROCHAINE_LIVRAISON,
	
	AUCUN_AFFICHAGE;
	
	static public class MetaData extends MetaDataEnum
	{
		
		public void fill()
		{		
			add("Ce champ vous permet d'indiquer si un libellé montant sera affiché.");
		
			add(MONTANT_TOTAL,"Montant total","Un texte \"Montant du total du contrat = XX € \" est affiché au dessus de votre texte personnalisé.");
			
			add(MONTANT_PROCHAINE_LIVRAISON,"Montant de la prochaine livraison","Un texte \"Montant de la livraison du JJ/MM/AA = XX € \" est affiché au dessus de votre texte personnalisé. La date affichée correspond à la date de la prochaine livraison.");
			
			add(AUCUN_AFFICHAGE,"Aucun affichage","Il n'y a pas de libellé montant.");
			
		}
	}	
	
}
