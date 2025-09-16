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

import fr.amapj.service.services.gestioncotisation.GestionCotisationService;
import fr.amapj.service.services.gestioncotisation.PeriodeCotisationDTO;
import fr.amapj.service.services.parametres.ParametresArchivageDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.tools.table.complex.ComplexTableBuilder;

/**
 * Permet de supprimer lers periodes de cotisations trop anciennes
 * 
 *
 */
public class PopupSuppressionPeriodeCotisation extends WizardFormPopup
{

	private ComplexTableBuilder<PeriodeCotisationDTO> builder;
	
	private List<PeriodeCotisationDTO> toSuppress;

	private List<PeriodeCotisationDTO> dtos;

	private ParametresArchivageDTO param;

	/**
	 * 
	 */
	public PopupSuppressionPeriodeCotisation()
	{
		setWidth(80);
		popupTitle = "Suppression des périodes de cotisations trop anciennes";
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
		
		String str = "Cet outil va rechercher la liste des périodes de cotisations qu'il est souhaitable de supprimer";
		addHtml(str);
		
		str = new GestionCotisationService().computeSuppressionLib(param);
		addHtml(str);
		
	}
	
	

	private void addFieldSaisieContrat()
	{
		// Titre
		setStepTitle("les périodes de cotisation à supprimer");
		
		dtos = new GestionCotisationService().getAllPeriodeCotisationSupprimables(param);
		toSuppress = new ArrayList<PeriodeCotisationDTO>();
		
		if (dtos.size()==0)
		{
			addHtml("Il n'y a pas de périodes de cotisation à supprimer.");
			setBackOnlyMode();
			return;
		}
		
			
		builder = new ComplexTableBuilder<PeriodeCotisationDTO>(dtos);
		builder.setPageLength(7);
		
		
		builder.addString("Nom de la période", false, 300,  e->e.nom);
		builder.addDate("Date de Début de la période", false, 150,  e->e.dateDebut);
		builder.addDate("Date de fin de la période", false, 150,  e->e.dateFin);
		
		builder.addCheckBox("Supprimer cette période de cotisation", "cb",true, 150, e->true, null);
		
		addComplexTable(builder);
		
	}
	
	private String readContratsToArchive()
	{
		toSuppress = builder.getSelectedCheckBox("cb");
		
		if (toSuppress.size()==0)
		{
			return "Vous devez selectionner au moins une période de cotisation pour pouvoir continuer.";
		}
		
		return null;
	}
	
	
	private void addFieldConfirmation()
	{
		// Titre
		setStepTitle("confirmation");
		
		addHtml("Vous allez supprimer DEFINITIVEMENT "+toSuppress.size()+" periodes de cotisation");
		
		addHtml("Appuyez sur Sauvegarder pour réaliser cette modification, ou Annuler pour ne rien modifier");
		
	}


	@Override
	protected void performSauvegarder() throws OnSaveException
	{
		for (PeriodeCotisationDTO mc : toSuppress) 
		{
			try
			{
				new GestionCotisationService().deleteWithInscrits(mc.id);
			}
			catch(Exception e)
			{
				throw new OnSaveException("Impossible de supprimer la période de cotisation "+mc.nom+". Raison : "+e.getMessage());
			}
		}
	}
}
