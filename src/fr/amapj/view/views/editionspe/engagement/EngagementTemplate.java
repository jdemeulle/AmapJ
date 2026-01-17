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
 package fr.amapj.view.views.editionspe.engagement;

import fr.amapj.model.engine.metadata.MetaDataEnum;
import fr.amapj.model.models.editionspe.PageFormat;

/**
 * Liste des templates pour les editions
 *
 */
public enum EngagementTemplate 
{
	//
	SIMPLE(PageFormat.A4_PORTRAIT) ,
	
	// 
	DETAILLE(PageFormat.A4_PORTRAIT) ,
	
	
	TRES_DETAILLE(PageFormat.A4_PORTRAIT) ,
	
	//
	RECU_TRES_SIMPLE(PageFormat.A4_PORTRAIT) ,
	
	//
	RECU_SIMPLE(PageFormat.A4_PORTRAIT) ,

	
	//
	VIERGE_PORTRAIT(PageFormat.A4_PORTRAIT),
	
	//
	VIERGE_PAYSAGE(PageFormat.A4_PAYSAGE),
	
	//
	DOCUMENTATION(PageFormat.A4_PORTRAIT);
	
	
	
	static public class MetaData extends MetaDataEnum
	{
		
		public void fill()
		{	
			
			add(SIMPLE,"Contrat simple  - 1 page ","Contrat simple qui tient généralement en une page. Vous pouvez modifier à votre guise les textes de ce contrat. Ce contrat est utilisable pour la signature en ligne.");
			add(DETAILLE,"Contrat détaillé - 2 pages ","Contrat plus complet, qui tient en général sur deux pages.  Vous pouvez modifier à votre guise les textes de ce contrat. Ce contrat est utilisable pour la signature en ligne.");
			add(TRES_DETAILLE,"Contrat très détaillé , avec signature de l'AMAP - 2 pages ","Contrat plus complet, qui tient en général sur deux pages.  Ce contrat est signé par l'amapien, le producteur et l'AMAP. Ce contrat N'est PAS utilisable pour la signature en ligne.");
			add(RECU_TRES_SIMPLE,"Contrat très simple avec un reçu","Contrat très simple avec un reçu. A utiliser uniquement en cas de besoin. Déconseillé pour la signature en ligne.");
			add(RECU_SIMPLE,"Contrat simple  avec un reçu ","Contrat simple avec un reçu. A utiliser uniquement en cas de besoin. Déconseillé pour la signature en ligne.");
			add(VIERGE_PORTRAIT,"Modèle vierge - Portrait");
			add(VIERGE_PAYSAGE,"Modèle vierge - Paysage");
			add(DOCUMENTATION,"Documentation - Liste des champs","Exemple vous indiquant la liste de tous les champs disponibles avec leur signification.");
		}
	}	
	
	
	   
	private PageFormat pageFormat;
	  
	EngagementTemplate(PageFormat pageFormat) 
    {
        this.pageFormat = pageFormat;
    }

	public PageFormat getPageFormat()
	{
		return pageFormat;
	}
}
