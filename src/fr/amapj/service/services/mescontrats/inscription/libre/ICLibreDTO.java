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
 package fr.amapj.service.services.mescontrats.inscription.libre;

import java.util.Date;

public class ICLibreDTO 
{
	public enum FullLibreState
	{
		// Inscription, modification et suppression normale, avant la date de fin des inscriptions
		STANDARD, 
				
		// Inscription, modification et suppression en tant que retardataire 
		RETARDAIRE,
		
		// Rien n'est possible (ni inscription, ni modification ni suppression)
		NONE
	}
	
	public FullLibreState status;
	
	public Date startDateRetardataire;
	
}
