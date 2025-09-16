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
 package fr.amapj.view.views.saisiecontrat;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.models.contrat.modele.GestionPaiement;
import fr.amapj.model.models.contrat.modele.extendparam.MiseEnFormeGraphique;
import fr.amapj.model.models.contrat.modele.GestionDocEngagement;
import fr.amapj.service.services.gestioncontrat.ExtPModeleContratService;
import fr.amapj.service.services.gestioncontrat.reglesaisie.VerifRegleSaisieModeleContratDTO;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.mescontrats.MesContratsService;
import fr.amapj.service.services.mescontrats.MonContratDTO;
import fr.amapj.service.services.mescontrats.inscription.InscriptionDTO;
import fr.amapj.service.services.stockservice.verifstock.VerifStockDTO;
import fr.amapj.view.engine.popup.PopupListener;
import fr.amapj.view.engine.popup.cascadingpopup.CInfo;
import fr.amapj.view.engine.popup.cascadingpopup.CascadingData;
import fr.amapj.view.engine.popup.cascadingpopup.CascadingPopup;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.tools.BaseUiTools;
import fr.amapj.view.views.saisiecontrat.abo.joker.SaisieJoker;
import fr.amapj.view.views.saisiecontrat.step1qte.abo.PopupSaisieQteContratPanier;
import fr.amapj.view.views.saisiecontrat.step1qte.std.grid.PopupSaisieQteContrat;
import fr.amapj.view.views.saisiecontrat.step1qte.std.onedatetable.PopupSaisieQteTable;
import fr.amapj.view.views.saisiecontrat.step1qte.std.vueliste.PopupSaisieQteVueListe;
import fr.amapj.view.views.saisiecontrat.step1qte.utils.QtePopupSousType;
import fr.amapj.view.views.saisiecontrat.step2paiement.PopupInfoPaiement;
import fr.amapj.view.views.saisiecontrat.step2paiement.PopupSaisiePaiement;
import fr.amapj.view.views.saisiecontrat.step3signature.PopupSignatureAmapienDirectView;

public class SaisieContrat
{
	private SaisieContratData data;
	private CascadingPopup cascading;
	
	public SaisieContrat(SaisieContratData data,PopupListener listener)
	{
		super();
		this.data = data;
		cascading = new CascadingPopup(listener,data);
	}
	
	// Calcul des enchainements 

	public void doSaisie()
	{
		// On verifie d'abord si tout est OK 
		String msg = checkInitialCondition();
		if (msg!=null)
		{
			cascading.displayError(msg);
			return;
		}	
		
		// Si cheque seul : on fait uniquement la saisie des chèques puis on sauvegarde
		if (data.modeSaisie==ModeSaisie.CHEQUE_SEUL)
		{
			CInfo info = new CInfo();
			info.popup = getPopupPaiement();
			info.actionAfterOnSaveButton = ()->data.saveContrat(); 
			cascading.start(info);
			return;
		}
	
		// Si joker seul
		if (data.modeSaisie==ModeSaisie.JOKER)
		{
			CInfo info = new CInfo();
			info.popup = SaisieJoker.computePopupSaisieJoker(data, false);
			info.actionAfterOnSaveButton = ()->data.saveContrat(); 
			cascading.start(info);
			return;
		}
		
		// Dans tous les autres cas : saisie de la quantité
		CInfo info = new CInfo();
		info.popup = getPopupSaisieQuantite();
		info.actionAfterOnSaveButton = null; 
		info.onSuccess = ()->endOfSaisieQte();
		cascading.start(info);		
	}
	

	/**
	 * On a fini de saisir les quantités
	 */
	private CInfo endOfSaisieQte()
	{
		// Si que saisie des quantités : on sauvegarde tout de suite et on arrete 
		if (data.modeSaisie==ModeSaisie.QTE_SEUL)
		{
			CInfo info = new CInfo();
			info.actionAfterOnSaveButton = ()->data.saveContrat(); 
			return info;			
		}
		
		// Sinon on continue avec le popup de paiement 
		CInfo info = new CInfo();
		info.popup = getPopupPaiement();
		info.onSuccess = ()->endOfSaisiePaiement();
		info.libSaveButton = hasPopupSignatureContrat() ? "Continuer" : "Valider";
		
		return info;
	
	}

	/**
	 * On a fini de saisir les paiements
	 */
	private CInfo endOfSaisiePaiement()
	{ 
		CInfo info = new CInfo();
		info.popup = getPopupSignatureContrat(); 
		info.actionAfterOnSaveButton = ()->data.saveContrat(); 
		return info;
	}
	
	
	// Calcul des popup à utiliser
	
	/**
	 * Retourne le popup à utiliser pour la saisie des quantités  
	 */
	private CorePopup getPopupSaisieQuantite() 
	{
		switch (data.inscriptionDTO.popupType)
		{
		case POPUP_ABO: 
			return new PopupSaisieQteContratPanier(data); 
			
		case POPUP_LIBRE:
		{
			QtePopupSousType sousType = computeQtePopupSousType();
			switch (sousType) 
			{
				case POPUP_DATES_PRODUIT : return new PopupSaisieQteContrat(data);
				case POPUP_LISTES_DATES: return new PopupSaisieQteVueListe(data);
				case POPUP_PRODUIT: return new PopupSaisieQteTable(data);
				default: throw new AmapjRuntimeException();
			}
		}

		default:
			throw new AmapjRuntimeException();
		}
	}

	
	private QtePopupSousType computeQtePopupSousType() 
	{
		// Pour le cas particulier de Firefox sur Android, on préviligie
		// le format POPUP_DATES_PRODUIT (problème de défilement vertical) 
		if (BaseUiTools.isFirefoxAndroid())
		{
			return QtePopupSousType.POPUP_DATES_PRODUIT;
		}
		
		
		switch (data.miseEnForme.styleSaisieQteContrat) 
		{
		case CHOIX_AUTOMATIQUE : return computeChoixAutomatiqueStyleSaisieQteContrat();
		case GRILLE_DATE_PRODUIT : return QtePopupSousType.POPUP_DATES_PRODUIT;
		case LISTE_DATE : return QtePopupSousType.POPUP_LISTES_DATES;
		default: throw new AmapjRuntimeException();
		}
	}
	
	
	private QtePopupSousType computeChoixAutomatiqueStyleSaisieQteContrat() 
	{
		// Si il y a une seule date de commandes : saisie directe  
		if (data.contratDTO.contratLigs.size()==1)
		{
			return QtePopupSousType.POPUP_PRODUIT;
		}
		
		// Si la liste des produits est affichable sur la largeur de l'écran
		// 110 : largeur d'une colonne produit - 110 : largeur de la colonne Date - 120 : marges sur les bords  
		if ( (data.contratDTO.contratColumns.size()*110+110+120) <= BaseUiTools.getWidth())
		{
			return QtePopupSousType.POPUP_DATES_PRODUIT;
		}
		
		// Dans les autres cas
		return QtePopupSousType.POPUP_LISTES_DATES;
	}	

	/**
	 * Retourne le popup à utiliser pour le paiement
	 */
	private CorePopup getPopupPaiement()
	{
		if (data.contratDTO.paiement.gestionPaiement==GestionPaiement.GESTION_STANDARD)
		{
			return new PopupSaisiePaiement(data);
		}
		else
		{
			return new PopupInfoPaiement(data);
		}
	}
	
	private CorePopup getPopupSignatureContrat()
	{
		if (hasPopupSignatureContrat()) 
		{
			return new PopupSignatureAmapienDirectView(data);
		}
		else
		{
			return null;
		}
	}
	
	private boolean hasPopupSignatureContrat()
	{
		if (data.contratDTO.gestionDocEngagement==GestionDocEngagement.SIGNATURE_EN_LIGNE && (data.modeSaisie==ModeSaisie.STANDARD || data.modeSaisie==ModeSaisie.FOR_TEST) ) 
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	// VERIFIICATION DES CONDITIONS INTIALES 
	
	/**
	 * 
	 */
	private String checkInitialCondition()
	{
		//
		if (data.inscriptionDTO.errorMessage!=null)
		{
			return data.inscriptionDTO.errorMessage;
		}
		
		//
		switch (data.modeSaisie) 
		{
		case STANDARD:
			return checkInitialConditionSaisieStandard();

		// Dans les autres cas : aucune verif 
		default:
			return null;

		}
	}

	
	private String checkInitialConditionSaisieStandard()
	{	
		// On vérifie si le contrat est bien accessible par l'amapien   
		String msg = new MesContratsService().checkIfAccessAllowed(data.contratDTO,data.userId);
		return msg;
	}
	
	
	// 
	
	/**
	 * Permet de lancer le cyle de saisie d'un contrat, avec les quantités et les reglements 
	 */
	static public void saisieContrat(Long idModeleContrat,Long idContrat, Long userId, String messageSpecifique,ModeSaisie modeSaisie,PopupListener listener)
	{
		// Rechargement du contrat 
		MonContratDTO m = new MesContratsService().loadMonContratDTO(idModeleContrat,idContrat,modeSaisie);
				
		// Lancement de la saisie 
		SaisieContratData data = new SaisieContratData(m.contratDTO, m.inscriptionDTO,m.verifStockDTO,m.verifRegleSaisieDTO,userId, messageSpecifique,modeSaisie);
		SaisieContrat saisieContrat = new SaisieContrat(data, listener);
		saisieContrat.doSaisie();
	}
	
	public enum ModeSaisie
	{
		// Saisie standard faite par l'utilisateur
		STANDARD,
		
		// Saisie des jokers faite par l'utilisateur
		JOKER,
		
		// Mode test, correspond à la saisie standard faite par l'utilisateur 
		FOR_TEST,
		
		// Visualisation des informations du contrat 
		READ_ONLY,
		
		// Saisie de la quantité seule, pour correction par le referent
		QTE_SEUL,
		
		// Saisie des chéques seuls, pour correction par le referent
		CHEQUE_SEUL ,
		
		// Saisie de la quantité et des cheques , pour le referent qui saisit à la main le contrat des amapiens
		QTE_CHEQUE_REFERENT
		
	}
	
	
	public static class SaisieContratData extends CascadingData
	{
		public ContratDTO contratDTO;
		public InscriptionDTO inscriptionDTO;
		public VerifStockDTO verifStockDTO;
		public Long userId;
		public String messageSpecifique;
		public ModeSaisie modeSaisie;
		public VerifRegleSaisieModeleContratDTO verifRegleSaisieDTO;
		public MiseEnFormeGraphique miseEnForme;
				
		public SaisieContratData(ContratDTO contratDTO, InscriptionDTO inscriptionDTO,VerifStockDTO verifStockDTO, VerifRegleSaisieModeleContratDTO verifRegleSaisieDTO, Long userId, String messageSpecifique,ModeSaisie modeSaisie)
		{
			
			this.contratDTO = contratDTO;
			this.inscriptionDTO = inscriptionDTO;
			this.verifStockDTO = verifStockDTO;
			this.verifRegleSaisieDTO = verifRegleSaisieDTO;
			this.userId = userId;
			this.messageSpecifique = messageSpecifique;
			this.modeSaisie = modeSaisie;
			this.miseEnForme = new ExtPModeleContratService().loadMiseEnFormeGraphique(contratDTO.modeleContratId);
		}
		
		
		/**
		 * Sauvegarde d'un contrat en fin de saisie, jette une exception si il y a des erreurs  
		 */
		public void saveContrat() throws OnSaveException
		{
			if (modeSaisie==ModeSaisie.FOR_TEST)
			{
				return;
			}
			new MesContratsService().saveNewContrat(contratDTO,userId);
		}

	}

}
