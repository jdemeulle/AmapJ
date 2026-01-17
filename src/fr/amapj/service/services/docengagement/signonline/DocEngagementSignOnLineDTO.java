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
 package fr.amapj.service.services.docengagement.signonline;

import java.util.Date;

/**
 * Represente un document d'engagement, pour le faire signer en ligne 
 * par le producteur ou l'amapien   
 *
 */
public class DocEngagementSignOnLineDTO 
{
	public String nomModeleContrat;
	
	public String nomUtilisateur;
	
	public String prenomUtilisateur;
	
	public Long idUtilisateur;
	
	public Long idContrat;
	
	public Long idModeleContrat;
	
	public Date signedByAmapien;
	
	public Date signedByProducteur;
		
}
