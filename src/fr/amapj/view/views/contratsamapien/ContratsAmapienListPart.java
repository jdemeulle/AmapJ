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

 package fr.amapj.view.views.contratsamapien;

import java.util.List;

import fr.amapj.model.engine.IdentifiableUtil;
import fr.amapj.model.models.fichierbase.Producteur;
import fr.amapj.service.services.access.AccessManagementService;
import fr.amapj.service.services.contratsamapien.AmapienContratDTO;
import fr.amapj.service.services.contratsamapien.AmapienContratsService;
import fr.amapj.service.services.mescontrats.MesContratsService;
import fr.amapj.service.services.session.SessionManager;
import fr.amapj.service.services.utilisateur.UtilisateurService;
import fr.amapj.view.engine.listpart.ButtonType;
import fr.amapj.view.engine.listpart.StandardListPart;
import fr.amapj.view.engine.popup.cascadingpopup.CInfo;
import fr.amapj.view.engine.popup.cascadingpopup.CascadingData;
import fr.amapj.view.engine.popup.cascadingpopup.CascadingPopup;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.suppressionpopup.SuppressionPopup;
import fr.amapj.view.views.common.amapientelecharger.TelechargerAmapien;
import fr.amapj.view.views.common.utilisateurselector.UtilisateurSelectorPart;
import fr.amapj.view.views.receptioncheque.ReceptionChequeEditorPart;
import fr.amapj.view.views.saisiecontrat.SaisieContrat;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.ModeSaisie;


/**
 * Gestion des contrats d'un amapien en particulier 
 *
 */
@SuppressWarnings("serial")
public class ContratsAmapienListPart extends StandardListPart<AmapienContratDTO> 
{

	private UtilisateurSelectorPart utilisateurSelector;
	
	private List<Producteur> allowedProducteurs;	
	
	public ContratsAmapienListPart()
	{
		super(AmapienContratDTO.class,false);
		allowedProducteurs = new AccessManagementService().getAccessLivraisonProducteur(SessionManager.getUserRoles(),SessionManager.getUserId(),false);
	}
	
	
	@Override
	protected String getTitle() 
	{
		return "Liste des contrats d'un amapien";
	}
	
	@Override
	protected void addSelectorComponent()
	{
		utilisateurSelector = new UtilisateurSelectorPart(this);
		addComponent(utilisateurSelector.getChoixUtilisateurComponent());
	}


	@Override
	protected void drawButton() 
	{
		addButtonAction("Ajouter nouveau contrat", ButtonType.ALWAYS, ()->handleAjouter());
		addButtonAction("Visualiser", ButtonType.EDIT_MODE, ()->handleVisualiser());
		addButtonAction("Modifier quantités", ButtonType.EDIT_MODE, ()->handleModifier());
		addButton("Réceptionner chèques",ButtonType.EDIT_MODE,e->handleReceptionCheque());
		addButtonAction("Modifier chéques", ButtonType.EDIT_MODE, ()->handleModifierCheque());
		addButton("Supprimer contrat", ButtonType.EDIT_MODE, e->handleSupprimer());
		addButton("Télécharger ...", ButtonType.ALWAYS, e->handleTelecharger());

		addSearchField("Rechercher par nom");
	}


	@Override
	protected void drawTable() 
	{
		addColumn("nomProducteur","Producteur");
		addColumn("nomContrat","Contrat");
		addColumnDate("dateDebut","Première livraison");
		addColumnDate("dateFin","Dernière livraison");
		addColumnDateTime("dateCreation","Date création");
		addColumnDateTime("dateModification","Date modification");
		addColumnCurrency("montant","Montant (en €)").right();
	}



	@Override
	protected List<AmapienContratDTO> getLines() 
	{
		Long idUser = utilisateurSelector.getUtilisateurId();
		if (idUser==null)
		{
			return null;
		}
		return new AmapienContratsService().getAllContratsDTO(idUser);
	}


	@Override
	protected String[] getSortInfos() 
	{
		return new String[] { "dateFin" ,"nomProducteur" };
	}
	
	@Override
	protected boolean[] getSortAsc()
	{
		return new boolean[] { false ,  true};
	}
	
	protected String[] getSearchInfos()
	{
		return new String[] { "nomContrat" , "nomProducteur"  };
	}
	

	private void handleVisualiser()
	{
		AmapienContratDTO dto = getSelectedLine();
		String message = "Visualisation du contrat de "+dto.prenomUtilisateur+" "+dto.nomUtilisateur;
		Long idUtilisateur = utilisateurSelector.getUtilisateurId();
		
		SaisieContrat.saisieContrat(dto.idModeleContrat,dto.idContrat,idUtilisateur,message,ModeSaisie.READ_ONLY,this);
	}

	protected void handleModifier()
	{
		AmapienContratDTO dto = getSelectedLine();
		String message = "Contrat de "+dto.prenomUtilisateur+" "+dto.nomUtilisateur;
		Long idUtilisateur = utilisateurSelector.getUtilisateurId();
		
		SaisieContrat.saisieContrat(dto.idModeleContrat,dto.idContrat,idUtilisateur,message,ModeSaisie.QTE_SEUL,this);
	}

	protected CorePopup handleSupprimer()
	{
		AmapienContratDTO dto = getSelectedLine();
		
		String text = "Êtes-vous sûr de vouloir supprimer le contrat de "+dto.prenomUtilisateur+" "+dto.nomUtilisateur+" ?";
		return new SuppressionPopup(text,dto.idContrat,e->new MesContratsService().deleteContrat(e));			
	}
	

	
	/**
	 * Retourne true si l'utilisateur courant a le droit de manipuler ce contrat
	 * @return
	 */
	@Override
	protected boolean isEditAllowed()
	{
		AmapienContratDTO dto = getSelectedLine();
		if (dto==null)
		{
			return false;
		}
		
		return IdentifiableUtil.contains(allowedProducteurs, dto.idProducteur);
	}
	
	
	private void handleAjouter()
	{
		AjouterData data= new AjouterData();
		data.userId = utilisateurSelector.getUtilisateurId();
		
		CascadingPopup cascading = new CascadingPopup(this,data);
		
		CInfo info = new CInfo();
		info.popup = new PopupSaisieProducteurContrat(data);
		info.onSuccess = ()->successSaisieContrat(data);
		
		cascading.start(info);
	}
		
	private CInfo successSaisieContrat(AjouterData data)
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
	
	
	private void handleModifierCheque()
	{
		AmapienContratDTO dto = getSelectedLine();
		
		String message = "Modification des chèques de "+dto.prenomUtilisateur+" "+dto.nomUtilisateur;
	
		SaisieContrat.saisieContrat(dto.idModeleContrat,dto.idContrat,dto.idUtilisateur,message,ModeSaisie.CHEQUE_SEUL,this);
		
	}
	
	private CorePopup handleReceptionCheque()
	{
		AmapienContratDTO dto = getSelectedLine();
		return new ReceptionChequeEditorPart(dto.idContrat,dto.nomUtilisateur,dto.prenomUtilisateur);
	}
	
	private CorePopup handleTelecharger()
	{
		Long idUtilisateur = utilisateurSelector.getUtilisateurId();
		return TelechargerAmapien.handleTelecharger(idUtilisateur);
	}
	
}
