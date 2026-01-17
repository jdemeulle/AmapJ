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
 package fr.amapj.view.views.permanence.detailperiode;

import java.util.List;

import fr.amapj.service.services.edgenerator.excel.permanence.EGBilanInscriptionPermanence;
import fr.amapj.service.services.edgenerator.excel.permanence.EGPlanningPermanence;
import fr.amapj.service.services.permanence.periode.PeriodePermanenceDateDTO;
import fr.amapj.service.services.permanence.periode.PeriodePermanenceService;
import fr.amapj.view.engine.excelgenerator.TelechargerPopup;
import fr.amapj.view.engine.listpart.ButtonType;
import fr.amapj.view.engine.listpart.StandardListPart;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.swicthpopup.SwitchPopup;
import fr.amapj.view.views.permanence.PeriodePermanenceSelectorPart;
import fr.amapj.view.views.permanence.detailperiode.grille.ModifierInscriptionGrillePart;
import fr.amapj.view.views.permanence.detailperiode.mail.PopupEnvoiMailPlanningPermanence;
import fr.amapj.view.views.permanence.periode.grille.VisualiserPeriodePermanenceGrillePart;


/**
 * Gestion des inscriptions aux permanences
 *
 */
@SuppressWarnings("serial")
public class DetailPeriodePermanenceListPart extends StandardListPart<PeriodePermanenceDateDTO> 
{
	private PeriodePermanenceSelectorPart periodeSelector;
	
	public DetailPeriodePermanenceListPart()
	{
		super(PeriodePermanenceDateDTO.class,false);
		periodeSelector = new PeriodePermanenceSelectorPart(this);
	}
	
	
	@Override
	protected String getTitle() 
	{
		return "Gestion des inscriptions sur une période de permanence";
	}
	
	@Override
	protected void addSelectorComponent()
	{
		addComponent(periodeSelector.getChoixPeriodeComponent());	
	}
	

	@Override
	protected void drawButton() 
	{
	
		addButton("Visualiser grille",ButtonType.ALWAYS,e->new VisualiserPeriodePermanenceGrillePart(periodeSelector.getPeriodePermanenceId(),null));
		addButton("Modifier grille",ButtonType.ALWAYS,e->new ModifierInscriptionGrillePart(periodeSelector.getPeriodePermanenceId()));
		addButton("Modifier cette date",ButtonType.EDIT_MODE,e->new PopupModifPermanence(e.idPeriodePermanenceDate));
		addButton("Autre ...",ButtonType.ALWAYS,e->handleAutre());
		addButton("Télécharger",ButtonType.ALWAYS,e->handleTelecharger());
		
		addSearchField("Rechercher par nom");
		
	}



	@Override
	protected void drawTable() 
	{
		addColumnDate("datePerm","Date permanence ").width(160);
		addColumn("nbPlace","Nb souhaité").width(120);
		addColumn("nbInscrit","Nb inscrits").width(120);
		addColumn("complet","Complet").width(80);
		addColumn("nomInscrit","Noms des inscrits");
	}



	@Override
	protected List<PeriodePermanenceDateDTO> getLines() 
	{
		Long idPeriodePermanence = periodeSelector.getPeriodePermanenceId();
		if (idPeriodePermanence==null)
		{
			return null;
		}
		return new PeriodePermanenceService().loadPeriodePermanenceDTO(idPeriodePermanence).datePerms;
	}


	@Override
	protected String[] getSortInfos() 
	{
		return new String[] { "datePerm"};
	}
	
	protected String[] getSearchInfos()
	{
		return new String[] { "datePerm", "nomInscrit" };
	}

	
	
	private CorePopup handleAutre()
	{
		Long idPeriodePermanence = periodeSelector.getPeriodePermanenceId();
		
		SwitchPopup popup = new SwitchPopup("Autres actions sur les permanences",50);
		popup.addLine("Effacer les inscriptions sur une liste de date", ()->new PopupDeleteInscriptionPermanence(idPeriodePermanence));	
		popup.addLine("Calculer automatiquement les inscriptions", ()->new PopupCalculAutoPermanence(idPeriodePermanence));
		popup.addLine("Envoyer un mail à tous les participants avec le planning joint", ()->new PopupEnvoiMailPlanningPermanence(idPeriodePermanence));
		return popup;
	}

	
	private TelechargerPopup handleTelecharger()
	{
		Long idPeriodePermanence = periodeSelector.getPeriodePermanenceId();
		
		TelechargerPopup popup = new TelechargerPopup("Période de permanence",80);
		popup.addGenerator(new EGPlanningPermanence(idPeriodePermanence,null));
		popup.addGenerator(new EGBilanInscriptionPermanence(idPeriodePermanence));
		return popup;
	}
}
