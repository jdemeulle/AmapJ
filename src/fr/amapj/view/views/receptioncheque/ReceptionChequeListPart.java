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
 package fr.amapj.view.views.receptioncheque;

import java.util.List;

import com.vaadin.ui.HorizontalLayout;

import fr.amapj.service.services.gestioncontratsigne.ContratSigneDTO;
import fr.amapj.service.services.gestioncontratsigne.GestionContratSigneService;
import fr.amapj.view.engine.listpart.ButtonType;
import fr.amapj.view.engine.listpart.StandardListPart;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.swicthpopup.SwitchPopup;
import fr.amapj.view.views.common.contratselector.ContratSelectorPart;
import fr.amapj.view.views.common.contrattelecharger.TelechargerContrat;
import fr.amapj.view.views.saisiecontrat.SaisieContrat;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.ModeSaisie;


/**
 * Réception des chéques
 */
@SuppressWarnings("serial")
public class ReceptionChequeListPart extends StandardListPart<ContratSigneDTO> 
{
	private ContratSelectorPart contratSelectorPart;

	public ReceptionChequeListPart()
	{
		super(ContratSigneDTO.class,false);
	}
	
	@Override
	protected String getTitle() 
	{
		return "Réception des chèques";
	}


	@Override
	protected void drawButton() 
	{		
		addButtonAction("Visualiser",ButtonType.EDIT_MODE,()->handleVoir());
		addButton("Réceptionner les chèques",ButtonType.EDIT_MODE,e->new ReceptionChequeEditorPart(e.idContrat,e.nomUtilisateur,e.prenomUtilisateur));
		addButtonAction("Modifier les chèques",ButtonType.EDIT_MODE,()->handleModifierCheque());
		addButton("Saisir un avoir",ButtonType.EDIT_MODE,e->new PopupSaisieAvoir(e));
		addButton("Réception en masse",ButtonType.ALWAYS,e->new ReceptionChequeEditorPart(contratSelectorPart.getModeleContratId()));
		addButton("Autre...",ButtonType.ALWAYS,e->handleMore());
		addButton("Télécharger ...",ButtonType.ALWAYS,e->handleTelecharger());
		
		addSearchField("Rechercher par nom ou prénom");
		
	}

	@Override
	protected void addSelectorComponent()
	{
		// Partie choix du contrat
		contratSelectorPart = new ContratSelectorPart(this,true);
		HorizontalLayout toolbar1 = contratSelectorPart.getChoixContratComponent();
		
		addComponent(toolbar1);
		
		contratSelectorPart.fillAutomaticValues();
	}
	

	@Override
	protected void drawTable() 
	{
		addColumn("nomUtilisateur","Nom");
		addColumn("prenomUtilisateur","Prénom");				
		addColumnCurrency("mntCommande","Commandé(en €)").right();
		
		addColumn("nbChequePromis","Chèques à fournir à l'AMAP").center();
		addColumn("nbChequeRecus","Chèques à l'AMAP").center();
		addColumn("nbChequeRemis","Chèques chez le producteur").center();

		addColumnCurrency("mntSolde","Solde final(en €)").right();
		addColumnCurrency("mntAvoirInitial","Avoir initial(en €)").right();
	}



	@Override
	protected List<ContratSigneDTO> getLines() 
	{
		Long idModeleContrat = contratSelectorPart.getModeleContratId();
		if (idModeleContrat==null)
		{
			return null;
		}
		return new GestionContratSigneService().getAllContratSigne(idModeleContrat);
	}


	@Override
	protected String[] getSortInfos() 
	{
		return new String[] { "nomUtilisateur" , "prenomUtilisateur" };
	}
	
	protected String[] getSearchInfos()
	{
		return new String[] { "nomUtilisateur" , "prenomUtilisateur" };
	}
	
	private CorePopup handleMore()
	{
		Long idModeleContrat = contratSelectorPart.getModeleContratId();
		
		SwitchPopup popup = new SwitchPopup("Autres actions sur les paiements",50);
		popup.addLine("Chercher les chèques à rendre aux amapiens", ()->new PopupChercherChequeARendre(idModeleContrat));
		
		return popup;
	}

	private CorePopup handleTelecharger()
	{
		Long idModeleContrat = contratSelectorPart.getModeleContratId();
		ContratSigneDTO contratSigneDTO = getSelectedLine();
		Long idContrat = null;
		if (contratSigneDTO!=null)
		{
			idContrat = contratSigneDTO.idContrat;
		}
		return TelechargerContrat.displayPopupTelechargerContrat(idModeleContrat, idContrat);
	}


	

	private void handleModifierCheque()
	{
		ContratSigneDTO c = getSelectedLine();
		
		String message = "Modification des chèques de "+c.prenomUtilisateur+" "+c.nomUtilisateur;
	
		SaisieContrat.saisieContrat(c.idModeleContrat,c.idContrat,c.idUtilisateur,message,ModeSaisie.CHEQUE_SEUL,this);
		
	}


	private void handleVoir()
	{
		ContratSigneDTO c = getSelectedLine();
		
		String message = "Visualisation du contrat de "+c.prenomUtilisateur+" "+c.nomUtilisateur;
	
		SaisieContrat.saisieContrat(c.idModeleContrat,c.idContrat,c.idUtilisateur,message,ModeSaisie.READ_ONLY,this);
		
	}
	
}
