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

 package fr.amapj.view.views.permanence.periode;

import java.util.List;

import fr.amapj.service.services.permanence.periode.PeriodePermanenceService;
import fr.amapj.service.services.permanence.periode.SmallPeriodePermanenceDTO;
import fr.amapj.view.engine.listpart.ButtonType;
import fr.amapj.view.engine.listpart.StandardListPart;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.suppressionpopup.SuppressionPopup;
import fr.amapj.view.engine.popup.swicthpopup.SwitchPopup;
import fr.amapj.view.views.permanence.periode.grille.ModifierPeriodePermanenceGrillePart;
import fr.amapj.view.views.permanence.periode.grille.VisualiserPeriodePermanenceGrillePart;
import fr.amapj.view.views.permanence.periode.update.PopupAddDateForPeriodePermanence;
import fr.amapj.view.views.permanence.periode.update.PopupAddUtilisateurForPeriodePermanence;
import fr.amapj.view.views.permanence.periode.update.PopupDeleteDateForPeriodePermanence;
import fr.amapj.view.views.permanence.periode.update.PopupDeleteUtilisateurForPeriodePermanence;
import fr.amapj.view.views.permanence.periode.update.PopupModifEnteteForPeriodePermanence;
import fr.amapj.view.views.permanence.periode.update.PopupRegleInscriptionPeriodePermanence;
import fr.amapj.view.views.permanence.periode.update.PopupUpdateAllRole;
import fr.amapj.view.views.permanence.periode.update.PopupUpdateNbParticipationForPeriodePermanence;


/**
 * Listes des periodes de permanences
 *
 */
@SuppressWarnings("serial")
public class PeriodePermanenceListPart extends StandardListPart<SmallPeriodePermanenceDTO>
{
	

	public PeriodePermanenceListPart()
	{
		super(SmallPeriodePermanenceDTO.class,false);
		
	}
	
	@Override
	protected String getTitle() 
	{
		return "Liste des périodes de permanences";
	}


	@Override
	protected void drawButton() 
	{		
		addButton("Créer une nouvelle période",ButtonType.ALWAYS,e->new PeriodePermanenceCreationEditorPart());
		addButton("Visualiser grille",ButtonType.EDIT_MODE,e->new VisualiserPeriodePermanenceGrillePart(e.id,null));
		addButton("Visualiser détail",ButtonType.EDIT_MODE,e->new PeriodePermanenceVisualiserPart(e.id));
		addButton("Modifier grille",ButtonType.EDIT_MODE,e->new ModifierPeriodePermanenceGrillePart(e.id));
		addButton("Modifier ...",ButtonType.EDIT_MODE,e->handleModifier(e.id));
		addButton("Changer l'état",ButtonType.EDIT_MODE,e->new PeriodePermanenceModifEtat(e));
		addButton("Supprimer",ButtonType.EDIT_MODE,e->handleSupprimer());
		
		addSearchField("Rechercher par nom");
		
	}

	@Override
	protected void drawTable() 
	{
		addColumn("etat","État");
		addColumn("nom","Nom de la période");
		addColumnDate("dateDebut","Date de début");
		addColumnDate("dateFin","Date de fin");
		addColumn("nbDatePerm","Nombre de dates");
		addColumn("pourcentageInscription","% Inscription");
		
	}


	@Override
	protected List<SmallPeriodePermanenceDTO> getLines() 
	{
		return new PeriodePermanenceService().getAllPeriodePermanence();
	}


	@Override
	protected String[] getSortInfos() 
	{
		return new String[] { "nom" };
	}
	
	protected String[] getSearchInfos()
	{
		return new String[] { "nom" };
	}
	
	private CorePopup handleModifier(Long idPeriodePermanence) 
	{
		SwitchPopup popup = new SwitchPopup("Modifications des périodes de permanence",50);

		popup.addLine("Modifier le nom, la description, la date de fin des inscriptions", ()->new PopupModifEnteteForPeriodePermanence(idPeriodePermanence));
		popup.addSeparator();
		
		popup.addLine("Ajouter des dates de permanences", ()->new PopupAddDateForPeriodePermanence(idPeriodePermanence));
		popup.addLine("Supprimer des dates de permanences", ()->new PopupDeleteDateForPeriodePermanence(idPeriodePermanence));
		popup.addSeparator();
		
		popup.addLine("Ajouter des participants", ()->new PopupAddUtilisateurForPeriodePermanence(idPeriodePermanence));
		popup.addLine("Supprimer des participants", ()->new PopupDeleteUtilisateurForPeriodePermanence(idPeriodePermanence));
		popup.addLine("Modifier le nombre de participations", ()->new PopupUpdateNbParticipationForPeriodePermanence(idPeriodePermanence));
		popup.addSeparator();
		
		popup.addLine("Positionner les rôles pour toutes les dates en masse", ()->new PopupUpdateAllRole(idPeriodePermanence));
		popup.addSeparator();
		
		popup.addLine("Modifier les régles d'inscriptions", ()->new PopupRegleInscriptionPeriodePermanence(idPeriodePermanence));
		
		return popup;
	}

	protected SuppressionPopup handleSupprimer()
	{
		SmallPeriodePermanenceDTO dto = getSelectedLine();
		String text = "Êtes vous sûr de vouloir supprimer la période de permanence "+dto.nom+" ?";
		return new SuppressionPopup(text,dto.id,e->new PeriodePermanenceService().deletePeriodePermanence(e));	
	}
}
