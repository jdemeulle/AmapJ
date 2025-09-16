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
 package fr.amapj.view.views.webpage;

import com.vaadin.server.Sizeable.Unit;

import fr.amapj.common.GenericUtils;
import fr.amapj.service.services.web.WebPageDTO;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.tools.BaseUiTools;

/**
 * 
 */
public class WebPageEditorPart extends WizardFormPopup
{
	private WebPageDTO dto;

	private GenericUtils.VoidAction saveAction;
	
	/**
	 * 
	 */
	public WebPageEditorPart(String popupTitle,WebPageDTO dto,GenericUtils.VoidAction saveAction)
	{
		this.popupTitle = popupTitle;
		this.saveAction = saveAction;
		this.dto = dto;
		
		setWidth(80);
		setHeight("90%");
		setModel(this.dto);
	}
	
	@Override
	protected void configure() 
	{
		add(()->addInfoGenerales(),()->checkDescription());
	}

	protected void addInfoGenerales()
	{
		int h = ((int) (BaseUiTools.getHeight()*0.9))-250;
		addCKEditorFieldForLabel("Description", "content").setHeight(h,Unit.PIXELS); 
	}
	

	private String checkDescription() 
	{
		// 
		if (dto.content!=null && dto.content.length()>30_000)
		{
			return "La description détaillée est trop longue (30 000 caractères maxi)";
		}
		
		// On normalise la donnée en fin de traitement
		if (dto.content!=null)
		{
			if (dto.content.replaceAll("&nbsp;","").replaceAll("<br />", "").trim().length()==0)
			{
				dto.content = null;
			}
		}
		return null;
	}


	@Override
	protected void performSauvegarder() throws OnSaveException
	{
		saveAction.action();
	}

	
}
