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
 package fr.amapj.service.services.mescontrats;


import java.util.Date;

/**
 * 
 *
 */
public class ContratLigDTO
{
	public Date date;
	
	public Long modeleContratDateId;
	
	public int i;
	
	
	// Champs relatifs à l'inscription

	// Champ utilisé uniquement en mode CARTE_PREPAYEE et LIBRE - non utilisé en mode ABO 
	public boolean isModifiable;
	
	// Champ uniquement en mode ABO
	public AboLigStatus status;
	
	
	static public enum AboLigStatus
	{
		// Correspond aux lignes initiales forcées à 0
		FORCED_TO_0 , 
		
		// Ligne avec livraison (pouvant quand même être barrée, pouvant aussi recevoir des quantités REPORT)  
		NORMAL, 
		
		// Ligne sans livraison, reporté à une autre date 
		REPORT,
		
		// Ligne sans livraison, non reporté  
		JOKER
	}
	
	// Si status == REPORT : cette date est reportée vers celle ci 
	public Date reportDateDestination;

}
