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
 package fr.amapj.service.services.logview;

import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.vaadin.server.StreamResource;

import fr.amapj.common.SequenceFileInputStream;
import fr.amapj.service.services.appinstance.LogAccessDTO;
import fr.amapj.view.engine.ui.AmapJLogManager;


public class AllLogFileResource implements StreamResource.StreamSource
{
	private Supplier<List<LogAccessDTO>> logFileNameSuppliers;

	public AllLogFileResource(Supplier<List<LogAccessDTO>> logFileNameSuppliers)
	{
		this.logFileNameSuppliers = logFileNameSuppliers;
	}

	@Override
	public InputStream getStream()
	{
		List<LogAccessDTO> logFileNames = logFileNameSuppliers.get();
		
		// On trie par date de début croissante 
		List<String> fullLogFileNames = logFileNames.stream().
											sorted(Comparator.comparing(e->e.dateIn)).
											map(e->AmapJLogManager.getFullFileName(e.logFileName)).
											collect(Collectors.toList());
		
		return new SequenceFileInputStream(fullLogFileNames);
	}
}
