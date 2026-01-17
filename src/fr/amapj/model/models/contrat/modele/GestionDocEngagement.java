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
 * Mode de gestion de la signature du contrat 
 *
 */
public enum GestionDocEngagement
{
	
	// La gestion des documents d'engagement se fait sans le logiciel AMAPJ
	AUCUNE_GESTION ,
	
	// Il est possible de signer en ligne le document d'engagement  
	SIGNATURE_EN_LIGNE,
	
	// Correspond à l'ancien mode de fonctionnement : le logiciel AMAPJ génére seulement le document d'engagement
	// il est accessible par l'amapien dans "Imprimer mes contrats"
	GENERATION_DOCUMENT_SEUL;

	
	
	static public class MetaData extends MetaDataEnum
	{
		public void fill()
		{		
			add("Ce champ indique le mode de gestion des documents d'engagement entre le producteur et les amapiens, et la manière de les signer.");
			
			add(AUCUNE_GESTION, "Pas de gestion des documents d'engagement","Dans ce mode, le logiciel AmapJ ne gére pas les documents d'engagement et leurs signatures. "
					+ "L'Amap utilise d'autres moyens (documents papiers réalisés avec un traitement de texte par exemple) pour imprimer des documents d'engagement et les faire signer par les parties prenantes.");
			

			add(SIGNATURE_EN_LIGNE, "Signature en ligne des documents d'engagement","Dans ce mode, à la fin de la saisie d'un contrat, le logiciel AmapJ va proposer à l'amapien un document d'engagement qu'il pourra alors signer en ligne. "
					+ "Le document ainsi signé sera conservé dans AmapJ et pourra être consulté par le référent du contrat. Le document pourra aussi être signé ensuite par le producteur en ligne.");
			
						
			add(GENERATION_DOCUMENT_SEUL, "Génération seule du document d'engagement","Dans ce mode, le logiciel AmapJ va être capable de générer un document d'engagement.  Ce document est accessible par l'amapien dans \"Imprimer mes contrats\". "
					+ "L'amapien devra alors imprimer ce document, le document sera alors signé par l'amapien et le producteur. Ce document ne sera pas stocké dans AmapJ et devra être conservé au format papier.");
			
			
			addBrLink("La gestion des documents d'engagements.", "docs_utilisateur_document_engagement.html");
		}
	}		
}
