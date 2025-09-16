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
 package fr.amapj.view.views.gestioncontratsignes;

import java.util.List;

import com.vaadin.ui.HorizontalLayout;

import fr.amapj.model.models.param.EtatModule;
import fr.amapj.model.models.param.paramecran.PEGestionContratsSignes;
import fr.amapj.model.models.param.paramecran.PEGestionContratsVierges;
import fr.amapj.service.services.gestioncontratsigne.ContratSigneDTO;
import fr.amapj.service.services.gestioncontratsigne.GestionContratSigneService;
import fr.amapj.service.services.mescontrats.MesContratsService;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.utilisateur.UtilisateurService;
import fr.amapj.view.engine.listpart.ButtonType;
import fr.amapj.view.engine.listpart.StandardListPart;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.popup.cascadingpopup.CInfo;
import fr.amapj.view.engine.popup.cascadingpopup.CascadingData;
import fr.amapj.view.engine.popup.cascadingpopup.CascadingPopup;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.suppressionpopup.SuppressionPopup;
import fr.amapj.view.engine.popup.swicthpopup.SwitchPopup;
import fr.amapj.view.views.common.contratselector.ContratSelectorPart;
import fr.amapj.view.views.common.contrattelecharger.TelechargerContrat;
import fr.amapj.view.views.gestioncontratsignes.modifiermasse.ModifierEnMasseContratSigne;
import fr.amapj.view.views.saisiecontrat.SaisieContrat;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.ModeSaisie;
import fr.amapj.view.views.stock.GestionContratStockPart;


/**
 * Gestion des contrats signes
 *
 * 
 *
 */
@SuppressWarnings("serial")
public class GestionContratSignesListPart extends StandardListPart<ContratSigneDTO> 
{
	
	private ContratSelectorPart contratSelectorPart;
	private PEGestionContratsSignes peEcran;

	public GestionContratSignesListPart()
	{
		super(ContratSigneDTO.class,false);
		peEcran = (PEGestionContratsSignes) new ParametresService().loadParamEcran(MenuList.GESTION_CONTRAT_SIGNES);
	}
	
	@Override
	protected String getTitle() 
	{
		return "Liste des contrats signés";
	}


	@Override
	protected void drawButton() 
	{		
		addButtonAction("Ajouter un contrat signé",ButtonType.ALWAYS,()->	handleAjouter());
		addButtonAction("Visualiser",ButtonType.EDIT_MODE,()->handleVoir());
		addButtonAction("Modifier les quantités",ButtonType.EDIT_MODE,()->handleEditer());
		addButtonAction("Modifier les chèques",ButtonType.EDIT_MODE,()->handleModifierCheque());
		addButtonAction("Supprimer",ButtonType.EDIT_MODE,()->handleSupprimer());
		addButton("Modifier en masse ...",ButtonType.ALWAYS,e->handleModifMasse());
		if (new ParametresService().getParametres().etatGestionStock==EtatModule.ACTIF)
		{
			addButton("Stock",ButtonType.ALWAYS, e->GestionContratStockPart.handleStock(contratSelectorPart.getModeleContratId()));
		}
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
		addColumnDateTime("dateCreation","Date création");
		addColumnDateTime("dateModification","Date modification");
		addColumn("retardataire","Retardataire");
		addColumnCurrency("mntCommande","Commandé(en €)").right();
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
	
	
	private CorePopup handleModifMasse()
	{
		Long idModeleContrat = contratSelectorPart.getModeleContratId();
		return ModifierEnMasseContratSigne.createPopup(idModeleContrat);
	}
	

	
	private SwitchPopup handleMore()
	{
		Long idModeleContrat = contratSelectorPart.getModeleContratId();
		SwitchPopup popup = new SwitchPopup("Autres actions sur les contrats signés",50);
		popup.addLine("Envoyer un e mail à tous les adhérents de ce contrat", ()->new PopupCopyAllMailForContrat(idModeleContrat));
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



	private void handleVoir()
	{
		ContratSigneDTO c = getSelectedLine();
		
		String message = "Visualisation du contrat de "+c.prenomUtilisateur+" "+c.nomUtilisateur;
	
		
		SaisieContrat.saisieContrat(c.idModeleContrat,c.idContrat,c.idUtilisateur,message,ModeSaisie.READ_ONLY,this);
		
	}


	private void handleEditer()
	{
		ContratSigneDTO c = getSelectedLine();
		
		String message = "Contrat de "+c.prenomUtilisateur+" "+c.nomUtilisateur;
		
		SaisieContrat.saisieContrat(c.idModeleContrat,c.idContrat,c.idUtilisateur,message,ModeSaisie.QTE_SEUL,this);
	}
	
	private void handleModifierCheque()
	{
		ContratSigneDTO c = getSelectedLine();
		
		String message = "Modification des chèques de "+c.prenomUtilisateur+" "+c.nomUtilisateur;
	
		SaisieContrat.saisieContrat(c.idModeleContrat,c.idContrat,c.idUtilisateur,message,ModeSaisie.CHEQUE_SEUL,this);
		
	}

	private void handleSupprimer()
	{
		ContratSigneDTO contratSigneDTO = getSelectedLine();
		String text = "Etes vous sûr de vouloir supprimer le contrat de "+contratSigneDTO.prenomUtilisateur+" "+contratSigneDTO.nomUtilisateur+" ?";
		SuppressionPopup confirmPopup = new SuppressionPopup(text,contratSigneDTO.idContrat,e->new MesContratsService().deleteContrat(e));
		confirmPopup.open(this);		
	}
	
	private void handleAjouter()
	{
		AjouterData data= new AjouterData();
		data.idModeleContrat = contratSelectorPart.getModeleContratId();
		
		CascadingPopup cascading = new CascadingPopup(this,data);
		
		CInfo info = new CInfo();
		info.popup = new PopupSaisieUtilisateur(data,peEcran);
		info.onSuccess = ()->successSaisieUtilisateur(data);
		
		cascading.start(info);
	}
		
	private CInfo successSaisieUtilisateur(AjouterData data)
	{
		Long userId = data.userId;
		String message = "Contrat de "+new UtilisateurService().prettyString(userId);
					
		SaisieContrat.saisieContrat(data.idModeleContrat,null,userId,message,ModeSaisie.QTE_CHEQUE_REFERENT,this);
		
		return null;
		
	}

	public class AjouterData extends CascadingData
	{
		Long idModeleContrat;
		Long userId;
	}
	
}
