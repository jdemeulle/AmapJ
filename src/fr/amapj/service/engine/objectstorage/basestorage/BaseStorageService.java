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
 package fr.amapj.service.engine.objectstorage.basestorage;

import fr.amapj.model.models.objectstorage.ObjectStorage;

public interface BaseStorageService
{
	/** 
	 * Sauvegarde de ce contenu 
	 * 
	 * @param initial doit être non null
	 * @param content doit être non null
	 */
	public void saveContent(ObjectStorage initial,byte[] content);
	
	/**
	 * Chargement de ce contenu
	 * 
	 * @param initial doit être non null  
	 */
	public byte[] loadContent(ObjectStorage initial) throws Exception;
	
	/**
	 * Suppression du contenu 
	 */
	public void deleteContent(Long idObjectStorage);

	/**
	 * Liberation des ressources (fermeture du cache principalement) 
	 */
	public void close();
	
	// 
	public String getStatistic();
	
}
