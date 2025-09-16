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
 package fr.amapj.view.engine.statuspage;


import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.model.engine.tools.SpecificDbUtils;
import fr.amapj.model.models.param.paramecran.PEExtendedParametres;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.ui.AmapUI;

public class StatusPageView 
{
	private AmapUI amapUI;

	private VerticalLayout vl  = new VerticalLayout();

	
	public StatusPageView(AmapUI amapUI) 
	{
		this.amapUI = amapUI;
	}

	public void open() 
	{		
		vl.removeAllComponents();
		
		PEExtendedParametres pe = SpecificDbUtils.executeInMaster(()->(PEExtendedParametres) new ParametresService().loadParamEcran(MenuList.EXTENDED_PARAMETRES));
		
		addText("<center><b>Bienvenue dans le logiciel AMAPJ.</b></center>");
		
		addText("");
		
		addText("Vous avez installé AMAPJ avec succès sur votre machine locale.");
		
		addText("Cette installation comprend une base de données, dont voici les détails :");
		
		addText(pe.masterDbLibAmap1);
		
		addText("Si vous souhaitez accéder à cet AMAPJ , merci de cliquer sur le lien ci dessous : ");
		
		Link link = new Link("Accèder à AMAPJ installé sur mon PC (local) ", new ExternalResource("/amapj/amap1"));
		link.setTargetName("_blank");
		link.addStyleName("statuspage");
		
		vl.addComponent(link);

		
		addText("");
		
		addText("");

		addText("Si vous le souhaitez, vous pouvez remplacer cette base de données avec une de vos sauvegardes.");
		addText("Vous pourrez alors consulter vos données comme elles étaient au moment de la sauvegarde.");
		
		addText("Pour lancer cette restauration, merci de cliquer sur le bouton ci dessous \"Restaurer une base de données\"");
		
		Button b = new Button("Restaurer une base de données");
		b.addClickListener(e->handleRestore());
		vl.addComponent(b);
		
		addText("");
		
		addText("");
						
		
		amapUI.setContent(vl);
	}

	private void handleRestore() 
	{
		new StatusPagePopupLoadBackup().open(()->open());
	}

	private void addText(String text) 
	{
		Label l = new Label(text,ContentMode.HTML);
		l.addStyleName("statuspage");
		vl.addComponent(l);
		
	}

}
