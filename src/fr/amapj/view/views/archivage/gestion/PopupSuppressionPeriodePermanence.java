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

import fr.amapj.service.services.parametres.ParametresArchivageDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.permanence.periode.PeriodePermanenceService;
import fr.amapj.service.services.permanence.periode.SmallPeriodePermanenceDTO;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.tools.table.complex.ComplexTableBuilder;

/**
 * Permet de supprimer lers periodes de permanences trop anciennes
 * 
 *
 */
public class PopupSuppressionPeriodePermanence extends WizardFormPopup
{

	private ComplexTableBuilder<SmallPeriodePermanenceDTO> builder;
	
	private List<SmallPeriodePermanenceDTO> toSuppress;

	private List<SmallPeriodePermanenceDTO> dtos;

	private ParametresArchivageDTO param;

	/**
	 * 
	 */
	public PopupSuppressionPeriodePermanence()
	{
		setWidth(80);
		popupTitle = "Suppression des périodes de permanences trop anciennes";
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
		
		String str = "Cet outil va rechercher la liste des périodes de permanences qu'il est souhaitable de supprimer";
		addHtml(str);
		
		str = new PeriodePermanenceService().computeSuppressionLib(param);
		addHtml(str);
		
	}
	
	

	private void addFieldSaisieContrat()
	{
		// Titre
		setStepTitle("les périodes de permanence à supprimer");
		
		dtos = new PeriodePermanenceService().getAllPeriodePermanenceSupprimables(param);
		toSuppress = new ArrayList<SmallPeriodePermanenceDTO>();
		
		if (dtos.size()==0)
		{
			addHtml("Il n'y a pas de périodes de permanence à supprimer.");
			setBackOnlyMode();
			return;
		}
		
			
		builder = new ComplexTableBuilder<SmallPeriodePermanenceDTO>(dtos);
		builder.setPageLength(7);
		
		
		builder.addString("Nom de la période", false, 300,  e->e.nom);
		builder.addDate("Date de début de la période", false, 150,  e->e.dateDebut);
		builder.addDate("Date de fin de la période", false, 150,  e->e.dateFin);
		
		builder.addCheckBox("Supprimer cette période de permanence", "cb",true, 150, e->true, null);
		
		addComplexTable(builder);
		
	}
	
	private String readContratsToArchive()
	{
		toSuppress = builder.getSelectedCheckBox("cb");
		
		if (toSuppress.size()==0)
		{
			return "Vous devez sélectionner au moins une période de permanence pour pouvoir continuer.";
		}
		
		return null;
	}
	
	
	private void addFieldConfirmation()
	{
		// Titre
		setStepTitle("confirmation");
		
		addHtml("Vous allez supprimer DÉFINITIVEMENT "+toSuppress.size()+" périodes de permanence");
		
		addHtml("Appuyez sur Sauvegarder pour réaliser cette modification, ou Annuler pour ne rien modifier");
		
	}


	@Override
	protected void performSauvegarder() throws OnSaveException
	{
		for (SmallPeriodePermanenceDTO mc : toSuppress) 
		{
			try
			{
				new PeriodePermanenceService().deleteWithInscrits(mc.id);
			}
			catch(Exception e)
			{
				throw new OnSaveException("Impossible de supprimer la période de permanence "+mc.nom+". Raison : "+e.getMessage());
			}
		}
	}
}
