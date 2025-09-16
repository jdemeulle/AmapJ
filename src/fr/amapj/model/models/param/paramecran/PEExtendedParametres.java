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
 package fr.amapj.model.models.param.paramecran;

import fr.amapj.model.models.param.paramecran.common.AbstractParamEcran;

/**
 * Paramètres généraux etendus
 */
public class PEExtendedParametres  extends AbstractParamEcran
{
	
	// Partie specifiques à master db 

	// Informations générales sur la base installée dans le slot amap1 
	public String masterDbLibAmap1;
	
	// Contenu du mail de backup 
	public String masterDbEmailBackupContent;

	
	
	
	
	
	
	// GETTERS AND SETTERS
	public String getMasterDbEmailBackupContent() 
	{
		return masterDbEmailBackupContent;
	}

	public void setMasterDbEmailBackupContent(String masterDbEmailBackupContent) 
	{
		this.masterDbEmailBackupContent = masterDbEmailBackupContent;
	}
	
	
	
	
}
