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
 package fr.amapj.service.engine.objectstorage.basestorage.local;

import fr.amapj.common.Base64Utils;
import fr.amapj.model.models.objectstorage.ObjectStorage;
import fr.amapj.service.engine.objectstorage.basestorage.BaseStorageService;

public class LocalBaseObjectStorageService implements BaseStorageService
{
	@Override
	public void saveContent(ObjectStorage initial,byte[] content)
	{
		initial.content =  Base64Utils.toBase64(content);
	}
	
	/**
	 *  
	 */
	@Override
	public byte[] loadContent(ObjectStorage initial) throws Exception
	{
		return Base64Utils.fromBase64(initial.content); 
	}

	@Override
	public void deleteContent(Long idObjectStorage)
	{
		// Nothing to do 
	}	
	
	@Override
	public void close()
	{
		// Nothing to do
	}
	
	@Override
	public String getStatistic()
	{
		return "";
	}
}
