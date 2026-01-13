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

 package fr.amapj.view.views.cotisation.bilan;

import java.util.List;

import fr.amapj.service.services.edgenerator.excel.EGBilanAdhesion;
import fr.amapj.service.services.edgenerator.pdf.PGBulletinAdhesion;
import fr.amapj.service.services.gestioncotisation.GestionCotisationService;
import fr.amapj.service.services.gestioncotisation.PeriodeCotisationDTO;
import fr.amapj.view.engine.excelgenerator.TelechargerPopup;
import fr.amapj.view.engine.listpart.ButtonType;
import fr.amapj.view.engine.listpart.StandardListPart;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.suppressionpopup.SuppressionPopup;


/**
 * Affichage de la liste des périodes de cotisation 
 *
 */
public class BilanCotisationView extends StandardListPart<PeriodeCotisationDTO>
{

	public BilanCotisationView()
	{
		super(PeriodeCotisationDTO.class,false);
	}
	
	
	@Override
	protected String getTitle() 
	{
		return "Liste des périodes d'adhésion";
	}


	@Override
	protected void drawButton() 
	{
		addButton("Créer une période",ButtonType.ALWAYS,e->new PeriodeCotisationEditorPart(true,null));
		addButton("Modifier une période",ButtonType.EDIT_MODE,e->new PeriodeCotisationEditorPart(false,e));
		addButton("Supprimer une période",ButtonType.EDIT_MODE,e->handleSupprimer());
		addButton("Télécharger ...",ButtonType.EDIT_MODE,e->handleTelecharger());

		addSearchField("Rechercher par nom");
	}


	@Override
	protected void drawTable() 
	{
		addColumn("nom","Nom de la période");
		addColumn("nbAdhesion","Nombre d'adhérents").right();
		addColumnCurrency("mntTotalAdhesion","Montant total des adhésions (en €)").right();
		addColumn("nbPaiementDonnes","Nb de paiements réceptionnés").right();
		addColumn("nbPaiementARecuperer","Nb de paiements à récupérer").right();	
	}



	@Override
	protected List<PeriodeCotisationDTO> getLines() 
	{
		return new GestionCotisationService().getAll();
	}


	@Override
	protected String[] getSortInfos() 
	{
		return new String[] { "nom"  };
	}
	
	protected String[] getSearchInfos()
	{
		return new String[] { "nom" };
	}
	

	private CorePopup handleSupprimer()
	{
		PeriodeCotisationDTO dto = getSelectedLine();
		String text = "Êtes-vous sûr de vouloir supprimer la période d'adhésion "+dto.nom+" ?";
		return new SuppressionPopup(text,dto.id,e->new GestionCotisationService().delete(e));		
	}

	
	private CorePopup handleTelecharger()
	{
		PeriodeCotisationDTO dto = getSelectedLine();
		boolean hasBulletin = dto.idBulletinAdhesion!=null;
		
		TelechargerPopup popup = new TelechargerPopup("Bilan des adhésions");
		popup.addGenerator(new EGBilanAdhesion(dto.id));
		if (hasBulletin)
		{
			popup.addGenerator(PGBulletinAdhesion.allBulletinPeriode(dto.id));
		}
		return popup;
	}
}
