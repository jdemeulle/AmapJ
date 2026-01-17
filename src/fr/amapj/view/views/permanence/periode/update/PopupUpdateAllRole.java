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
 package fr.amapj.view.views.permanence.periode.update;

import java.util.ArrayList;

import com.vaadin.data.util.BeanItem;

import fr.amapj.service.services.permanence.periode.PeriodePermanenceDTO;
import fr.amapj.service.services.permanence.periode.PeriodePermanenceRoleDTO;
import fr.amapj.service.services.permanence.periode.PeriodePermanenceService;
import fr.amapj.service.services.permanence.periode.role.UpdateRoleService;
import fr.amapj.view.engine.collectioneditor.CollectionEditor;
import fr.amapj.view.engine.collectioneditor.FieldType;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.views.searcher.SearcherList;

/**
 * Permet de positionner les roles  
 */
public class PopupUpdateAllRole extends WizardFormPopup
{
	
	protected PeriodePermanenceDTO dto;
	
	
	/**
	 * 
	 */
	public PopupUpdateAllRole(Long id)
	{
		super();
		popupTitle = "Positionner les rôles d'une période de permanence";
		setWidth(80);
				
		// Chargement de l'objet  à modifier
		dto = new PeriodePermanenceService().loadPeriodePermanenceDTO(id);
		dto.roles = new ArrayList<PeriodePermanenceRoleDTO>();
		
		//
		setModel(dto);
		
	}
	
	@Override
	protected void configure()
	{
		add(()->addAide());
		add(()->drawRole1(), ()->checkRole1());
	}
	
	
	private void addAide()
	{
		// Titre
		setStepTitle("explication sur le fonctionnement de cet outil");
		
		String str = 	"Cet outil va vous permettre de positionner les rôles sur une période de permanence complète.<br/>"+
						"<br/>"+
						"Vous allez indiquer une liste de rôles, et l'outil va l'appliquer à toutes les dates de la période.<br/>"+
						"Si il manque des rôles (par exemple une date posséde 6 places et vous avez indiqué 3 rôles) , alors l'outil va utiliser 2 fois la liste initiale";
			
								

		addHtml(str);

	}
	
	
	private void drawRole1()
	{
		// Titre
		setStepTitle("la saisie des rôles");

		// Les produits
		CollectionEditor<PeriodePermanenceRoleDTO> f1 = addCollectionEditorField("Rôle","roles", PeriodePermanenceRoleDTO.class);
		f1.addSearcherColumn("idRole", "Rôle",FieldType.SEARCHER, null,SearcherList.PERIODE_PERMANENCE_ROLE,null);
		
	}
	
	
	/**
	 * 
	 */
	private String checkRole1()
	{
		// On verifie que l'on a bien saisi une liste non vide
		if (dto.roles.size()==0)
		{
			return "La liste des rôles doit contenir au moins un élément";
		}
		
		for (PeriodePermanenceRoleDTO role : dto.roles)
		{
			if (role.idRole==null)
			{
				return "Il ne doit pas y avoir de ligne vide";
			}
		}
		
		return null;
	}
	
	
	
	


	protected void performSauvegarder()
	{	
		// Sauvegarde du contrat
		new UpdateRoleService().setRole(dto);
	}
	
}
