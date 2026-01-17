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
 package fr.amapj.view.views.archivage.gestion;

import fr.amapj.service.services.parametres.ParametresArchivageDTO;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;

/**
 * Permet la saisie d'un ou plusieurs nombre de jours (pour l'archivage)
 *
 */
public class PopupSaisieNombreJour extends WizardFormPopup
{

	private ParametresArchivageDTO dto;
	private Regle[] regles;
	

	/**
	 * 
	 */
	public PopupSaisieNombreJour(ParametresArchivageDTO dto,Regle... regles)
	{
		setWidth(80);
		popupTitle = "Modification d'une régle";

		this.dto = dto;
		setModel(dto);
		
		this.regles = regles;

		saveButtonTitle = "OK";
	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldRegle());
	}
	
	

	private void addFieldRegle()
	{
		for (Regle regle : regles) 
		{
			addTextField(regle.lib+" (en jours)", regle.field);	
		} 
	}
	

	

	@Override
	protected void performSauvegarder()
	{
		// Nothing to do 
	}



	
	static public class Regle
	{
		public String lib;
		public String field;
		
		public Regle(String lib, String field) 
		{
			this.lib = lib;
			this.field = field;
		}
		
		
	}
	
}
