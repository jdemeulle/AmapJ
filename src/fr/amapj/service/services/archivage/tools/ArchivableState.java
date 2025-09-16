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
 package fr.amapj.service.services.archivage.tools;

import java.util.ArrayList;
import java.util.List;

public class ArchivableState
{
	static public enum AStatus
	{
	
		// cet element n'est pas archivable 
		NON ,
	
		// cet element est archivable avec réserve majeure
		OUI_AVEC_RESERVE_MAJEURE,
	
		// cet element est archivable avec réserve mineure
		OUI_AVEC_RESERVE_MINEURE,
	
		
		// cet element est archivable sans réserve
		OUI_SANS_RESERVE;
	}

	// Liste des raisons pour lesquelles cet element n'est pas archivable
	public List<String> nonArchivables = new ArrayList<>();
	
	// Liste des réserves majeures
	public List<String> reserveMajeures = new ArrayList<>();
	
	
	// Liste des réserves mineures
	public List<String> reserveMineures = new ArrayList<>();

	
	public AStatus getStatus()
	{
		if (nonArchivables.size()>0)
		{
			return AStatus.NON;
		}
		if (reserveMajeures.size()>0)
		{
			return AStatus.OUI_AVEC_RESERVE_MAJEURE;
		}
		if (reserveMineures.size()>0)
		{
			return AStatus.OUI_AVEC_RESERVE_MINEURE;
		}
		return AStatus.OUI_SANS_RESERVE;
	}
	

}
