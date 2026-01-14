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

import java.util.ArrayList;
import java.util.List;

import fr.amapj.service.services.archivage.ArchivageUtilisateurService;
import fr.amapj.service.services.parametres.ParametresArchivageDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.utilisateur.UtilisateurDTO;
import fr.amapj.service.services.utilisateur.UtilisateurService;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.tools.table.complex.ComplexTableBuilder;

/**
 * Permet de supprimer les utilisateurs archivés trop anciens
 * 
 *
 */
public class PopupSuppressionUtilisateur extends WizardFormPopup
{

	private ComplexTableBuilder<UtilisateurDTO> builder;
	
	private List<UtilisateurDTO> toSuppress;

	private List<UtilisateurDTO> dtos;

	private ParametresArchivageDTO param;

	/**
	 * 
	 */
	public PopupSuppressionUtilisateur()
	{
		setWidth(80);
		popupTitle = "Suppression des utilisateurs archivés trop anciens";
		param = new ParametresService().getParametresArchivage();
		
	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldInfoGenerales());
		add(()->addFieldSaisieContrat(),()->readContratsToArchive());
		add(()->addFieldConfirmation());
	}

	private void addFieldInfoGenerales()
	{
		// Titre
		setStepTitle("les informations générales.");
		
		String str = "Cet outil va rechercher la liste des utilisateurs archivés qu'il est souhaitable de supprimer";
		addHtml(str);
		
		str = new ArchivageUtilisateurService().computeSuppressionLib(param);
		addHtml(str);
		
	}
	
	

	private void addFieldSaisieContrat()
	{
		// Titre
		setStepTitle("les utilisateurs archivés à supprimer");
		
		dtos = new ArchivageUtilisateurService().getAllUtilisateurSupprimables(param);
		toSuppress = new ArrayList<UtilisateurDTO>();
		
		if (dtos.size()==0)
		{
			addHtml("Il n'y a pas d'utilisateurs à supprimer.");
			setBackOnlyMode();
			return;
		}
		
			
		builder = new ComplexTableBuilder<UtilisateurDTO>(dtos);
		builder.setPageLength(7);
		
		
		builder.addString("Nom ", false, 200,  e->e.nom);
		builder.addString("Prénom", false, 200,  e->e.prenom);
		builder.addDate("Date de création", false, 150,  e->e.dateCreation);
		
		builder.addCheckBox("Supprimer cet utilisateur", "cb",true, 150, e->true, null);
		
		addComplexTable(builder);
		
	}
	
	private String readContratsToArchive()
	{
		toSuppress = builder.getSelectedCheckBox("cb");
		
		if (toSuppress.size()==0)
		{
			return "Vous devez sélectionner au moins un utilisateur pour pouvoir continuer.";
		}
		
		return null;
	}
	
	
	private void addFieldConfirmation()
	{
		// Titre
		setStepTitle("confirmation");
		
		addHtml("Vous allez supprimer DÉFINITIVEMENT "+toSuppress.size()+" utilisateurs");
		
		addHtml("Appuyez sur Sauvegarder pour réaliser cette modification, ou Annuler pour ne rien modifier");
		
	}


	@Override
	protected void performSauvegarder() throws OnSaveException
	{
		for (UtilisateurDTO mc : toSuppress) 
		{
			try
			{
				new UtilisateurService().deleteUtilisateur(mc.id);
			}
			catch(Exception e)
			{
				throw new OnSaveException("Impossible de supprimer l'utilisateur "+mc.nom+" "+mc.prenom+". Raison : "+e.getMessage());
			}
		}
	}
}

