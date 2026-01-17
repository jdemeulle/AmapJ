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
 package fr.amapj.service.engine.generator;

import java.io.InputStream;

import fr.amapj.model.engine.rdblink.RdbLink;


/**
 * Calsse de base de tous les outils générant des Excels, PDF, ...
 * 
 *  
 *
 */
public interface CoreGenerator
{
	/**
	 * Retourne les données contenant le document Excel, PDF, ... sous la forme d'un inputstream
	 */
	public InputStream getContent();
	
	/**
	 * Retourne les données contenant le document Excel, PDF, ... sous la forme d'un byte array
	 */
	public byte[] getByteArrayContent();
	
	public String getFileName(RdbLink em);
	
	public String getNameToDisplaySuffix();
	
	public String getNameToDisplay(RdbLink em);

	public String getExtension();
	
}
