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

 package fr.amapj.view.views.producteur.contrats;

import java.util.List;

import fr.amapj.model.models.contrat.modele.GestionDocEngagement;
import fr.amapj.model.models.param.ChoixOuiNon;
import fr.amapj.model.models.param.EtatModule;
import fr.amapj.model.models.param.paramecran.producteur.PEContratProducteur;
import fr.amapj.service.services.access.AccessManagementService;
import fr.amapj.service.services.docengagement.signonline.DocEngagementSignOnLineDTO;
import fr.amapj.service.services.docengagement.signonline.DocEngagementSignOnLineService;
import fr.amapj.service.services.edgenerator.excel.EGCollecteCheque;
import fr.amapj.service.services.edgenerator.excel.feuilledistribution.producteur.EGFeuilleDistributionProducteur;
import fr.amapj.service.services.edgenerator.excel.feuilledistribution.producteur.EGSyntheseContrat;
import fr.amapj.service.services.edgenerator.excel.producteur.EGPaiementProducteur;
import fr.amapj.service.services.edgenerator.zip.ZGAllDocEngagementModeleContrat;
import fr.amapj.service.services.gestioncontrat.ModeleContratSummaryDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.producteur.ProducteurService;
import fr.amapj.service.services.session.SessionManager;
import fr.amapj.view.engine.excelgenerator.TelechargerPopup;
import fr.amapj.view.engine.listpart.ButtonType;
import fr.amapj.view.engine.listpart.StandardListPart;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.corepopup.CorePopup.ColorStyle;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;
import fr.amapj.view.engine.popup.swicthpopup.SwitchPopup;
import fr.amapj.view.views.producteur.ProducteurSelectorPart;
import fr.amapj.view.views.saisiecontrat.SaisieContrat;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.ModeSaisie;
import fr.amapj.view.views.stock.GestionContratStockPart;


/**
 * Affichage des contrats pour un producteur 
 *
 */
public class ProducteurContratListPart extends StandardListPart<ModeleContratSummaryDTO>
{
	private ProducteurSelectorPart producteurSelector;
	private PEContratProducteur pe;
	
	public ProducteurContratListPart()
	{
		super(ModeleContratSummaryDTO.class,false);
		pe = (PEContratProducteur) new ParametresService().loadParamEcran(MenuList.CONTRATS_PRODUCTEUR);
	}
	
	
	@Override
	protected String getTitle() 
	{
		return "Liste des contrats d'un producteur";
	}
	
	@Override
	protected void addSelectorComponent()
	{
		producteurSelector = new ProducteurSelectorPart(this,true,true);
		addComponent(producteurSelector.getChoixProducteurComponent());
	}


	@Override
	protected void drawButton() 
	{
		addButtonAction("Tester",ButtonType.EDIT_MODE,()->handleTester());
		if (new ParametresService().getParametres().etatGestionStock==EtatModule.ACTIF && pe.modifierQteStock==ChoixOuiNon.OUI)
		{
			addButton("Stock",ButtonType.EDIT_MODE,e->GestionContratStockPart.handleStock(e.id));
		}
		addButton("Télécharger ...",ButtonType.EDIT_MODE,e->handleTelecharger());
		
		addButton("Signer les documents ...",ButtonType.EDIT_MODE,e->handleSigner(e));

		addSearchField("Rechercher par nom");
	}
	
	


	private CorePopup handleSigner(ModeleContratSummaryDTO mc) 
	{
		// Verification : le contrat gère t'il la signature en ligne ?
		if (mc.gestionDocEngagement!=GestionDocEngagement.SIGNATURE_EN_LIGNE)
		{
			return new MessagePopup("Signature des documents", ColorStyle.GREEN, "Ce contrat ne gère pas la signature en ligne.");
		}
		
		// Verification : la date de fin des inscription est elle passée ?
		if (new DocEngagementSignOnLineService().producteurIsNotAllowedToSignNow(mc.id))
		{
			return new MessagePopup("Signature des documents", ColorStyle.GREEN, "Les amapiens peuvent encore s'inscrire sur ce contrat. Vous pourrez signer les contrats après la date de fin des inscriptions");
		}
		
		// Verification : l'utilisateur courant a t il le droit de signer ? 
		Long userId = SessionManager.getUserId();
		if (new AccessManagementService().isAllowToSign(userId, mc.producteurId)==false)
		{
			return new MessagePopup("Signature des documents", ColorStyle.RED, "Vous n'étes pas autorisé à signer ces documents");
		}
		
		//
		List<DocEngagementSignOnLineDTO> dtos = new DocEngagementSignOnLineService().getAllDocumentsASignerByProducteur(mc.id);
		if (dtos.size()==0)
		{
			return new MessagePopup("Signature des documents", ColorStyle.GREEN, "Il n'y a pas de documents à signer pour le contrat "+mc.nom); 
		}
		
		if (dtos.size()==1)
		{
			return new PopupSignatureProducteurOneByOne(mc,dtos,userId); 
		}
		
		
		SwitchPopup popup = new SwitchPopup("Signature des documents",60);
		popup.setLine1("Vous avez "+dtos.size()+" documents à signer pour le contrat "+mc.nom+". Comment voulez vous les signer?");
		
		popup.addLine("Signer tous les documents en une seule fois", ()->new PopupSignatureProducteurAll(mc,dtos,userId));
		popup.addLine("Signer les documents un par un ", ()->new PopupSignatureProducteurOneByOne(mc,dtos,userId));
		
		return popup;
	}


	@Override
	protected void drawTable() 
	{
		addColumn("etat","État");
		addColumn("nom","Nom");
		addColumnDate("finInscription","Fin inscription");
		addColumnDate("dateDebut","Première livraison");
		addColumnDate("dateFin","Dernière livraison");
		addColumn("nbLivraison","Nb de livraisons");
		addColumn("nbProduit","Nb de produits").center();
		addColumn("nbInscrits","Nb d'inscrits").center();
		addColumn("nbContratASignerProducteur","Nb documents à signer").center();
	}



	@Override
	protected List<ModeleContratSummaryDTO> getLines() 
	{
		Long idProducteur = producteurSelector.getProducteurId();
		// Si le producteur n'est pas défini : la table est vide et les boutons desactivés
		if (idProducteur==null)
		{
			return null;
		}
		
		return new ProducteurService().getModeleContratInfo(idProducteur);
	}


	@Override
	protected String[] getSortInfos() 
	{
		return new String[] { "etat" , "dateDebut"  };
	}
	
	protected boolean[] getSortAsc()
	{
		return new boolean[] { true , false  };
	}
	
	
	protected String[] getSearchInfos()
	{
		return new String[] { "nom"  };
	}
	
	private void handleTester()
	{
		ModeleContratSummaryDTO mcDto = getSelectedLine();
		SaisieContrat.saisieContrat(mcDto.id,null,null,"Mode Test",ModeSaisie.FOR_TEST,this);	
	}
	
		

	private CorePopup handleTelecharger()
	{
		ModeleContratSummaryDTO mcDto = getSelectedLine();
		
		TelechargerPopup popup = new TelechargerPopup("Producteur");
		
		// Les feuilles de distribution
		popup.addGenerator(new EGFeuilleDistributionProducteur(mcDto.id));
		popup.addGenerator(new EGSyntheseContrat(mcDto.id));
		popup.addLabel("");
		
		// Le paiement
		popup.addGenerator(new EGPaiementProducteur(mcDto.id));
		if (pe.telechargerFeuilleCollecteCheque==ChoixOuiNon.OUI)
		{
			popup.addGenerator(new EGCollecteCheque(mcDto.id));
		}
		popup.addLabel("");
		
		// Les documents d'engagements (uniquement si signature en ligne) 
		if (mcDto.gestionDocEngagement==GestionDocEngagement.SIGNATURE_EN_LIGNE)
		{
			popup.addGenerator(new ZGAllDocEngagementModeleContrat(mcDto.id));
		}
				
		return popup;
	}
}
