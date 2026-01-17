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
 package fr.amapj.view.views.logview;

import java.util.List;
import java.util.function.Supplier;

import com.vaadin.server.StreamResource;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.service.services.appinstance.LogAccessDTO;
import fr.amapj.service.services.logview.AllLogFileResource;
import fr.amapj.service.services.logview.LogFileResource;
import fr.amapj.view.engine.popup.corepopup.CorePopup;

/**
 * Télécharger le fichier global.log avec le popup 
 *  
 */
public class TelechargerLogPopup extends CorePopup
{
	private Supplier<List<LogAccessDTO>> lines;

	public TelechargerLogPopup(Supplier<List<LogAccessDTO>> lines)
	{			
		this.lines = lines;
		popupTitle = "Fichier de log global";
	}
	

	protected void createContent(VerticalLayout contentLayout)
	{
		contentLayout.addComponent(new Label("Veuillez cliquer sur le lien du fichier que vous souhaitez télécharger"));
		
		
		String name = "global";
		LogFileResource logFileResource = new LogFileResource(name);	
		Link extractFile = new Link("Télécharger le fichier global.log",new StreamResource(logFileResource, name+".log"));
		contentLayout.addComponent(extractFile);
		

		AllLogFileResource allLogFileResource = new AllLogFileResource(lines);	
		extractFile = new Link("Télécharger tous les fichiers de log visibles",new StreamResource(allLogFileResource, "all-files.log"));
		contentLayout.addComponent(extractFile);
		
		
	}
	
	

	protected void createButtonBar()
	{		
		addButtonBlank();
		addButton("Quitter", e->close());
	}	
}
