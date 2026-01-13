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

 package fr.amapj.view.views.saisiecontrat.step2paiement;

import java.util.List;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.models.contrat.modele.SaisiePaiementModifiable;
import fr.amapj.model.models.contrat.modele.extendparam.MiseEnFormeGraphique;
import fr.amapj.model.models.contrat.reel.EtatPaiement;
import fr.amapj.model.models.param.ChoixOuiNon;
import fr.amapj.service.services.mescontrats.DatePaiementDTO;
import fr.amapj.service.services.mescontrats.InfoPaiementDTO;
import fr.amapj.service.services.producteur.ProdUtilisateurDTO;
import fr.amapj.view.engine.grid.GridHeaderLine;
import fr.amapj.view.engine.grid.currencyvector.CurrencyVectorParam.CurrencyLine;
import fr.amapj.view.engine.grid.currencyvector.CurrencyVectorParam.CurrencyLineStatus;
import fr.amapj.view.engine.grid.currencyvector.PopupCurrencyVector;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;
import fr.amapj.view.views.saisiecontrat.SaisieContrat;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.ModeSaisie;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.SaisieContratData;

/**
 * Popup pour la saisie des paiements pour un contrat
 *  
 */
public class PopupSaisiePaiement extends PopupCurrencyVector
{
	private InfoPaiementDTO paiementDTO;
	
	private SaisieContratData data;

	/**
	 * 
	 */
	public PopupSaisiePaiement(SaisieContratData data)
	{
		super();
		this.data = data;
		this.paiementDTO = data.contratDTO.paiement;
			
		//
		popupTitle = "Vos paiements pour le contrat "+data.contratDTO.nom;
		setWidth(50);
	}
	

	public void loadParam()
	{
		// On calcule tout d'abord la proposition de paiement si cela est nécessaire
		new PropositionPaiement(data).computePropositionPaiement();
		
		//
		param.avoirInitial = paiementDTO.avoirInitial;
		param.montantCible = data.contratDTO.getMontantTotal();
		param.largeurCol = 170;
		param.espaceInterCol = 3;
		param.hasColComment = computeHasColComment();
			
		// Construction du header 
		GridHeaderLine line1  =new GridHeaderLine();
		line1.height = 40;
		line1.addCell("Date");
		line1.addCell("Montant €");
		if (param.hasColComment)
		{
			line1.addCell("État");
		}
		
		param.headerLines.add(line1);
		
		List<DatePaiementDTO> datePaiements = paiementDTO.datePaiements;
		int nbModifiable = paiementDTO.getNbModifiable();
		for (int i = 0; i < datePaiements.size(); i++) 
		{
			DatePaiementDTO datePaiement = datePaiements.get(i);
			CurrencyLine line = computeLine(datePaiement,i,nbModifiable);
			param.lines.add(line);
		}
		
		// 
		param.hasSaveButton = data.modeSaisie!=ModeSaisie.READ_ONLY;
		param.libSaveButton = data.getLibSaveButton();
		
		// Cacul des messages en haut de popup
		MiseEnFormeGraphique miseEnForme = data.miseEnForme;
		
		// Message 1 
		param.messageSpecifique = data.messageSpecifique;
		
		// Message 2
		if (miseEnForme.paiementStdLib1Modifier==ChoixOuiNon.NON)
		{
			param.messageSpecifique2 = getDefaultMessageOrdreCheque();
		}
		else
		{
			param.messageSpecifique2 = miseEnForme.paiementStdLib1;
		}
		
		// Message 3
		if (param.hasSaveButton)
		{
			if (miseEnForme.paiementStdLib2Modifier==ChoixOuiNon.NON)
			{
				param.messageSpecifique3 = getDefaultMessageIndicationRemplissage();
			}
			else
			{
				param.messageSpecifique3 = miseEnForme.paiementStdLib2;
			}
		}
	}


	private boolean computeHasColComment()
	{
		switch (data.modeSaisie)
		{
		case STANDARD: 
		case FOR_TEST:
			// Si il y a un paiement déjà réceptionné, alors on met la colonne commentaire 
			for (DatePaiementDTO datePaiement : paiementDTO.datePaiements)
			{
				if (datePaiement.montant!=0 && datePaiement.etatPaiement!=EtatPaiement.A_FOURNIR)
				{
					return true;
				}
			}
			return false;

		case READ_ONLY:
		case CHEQUE_SEUL:
		case QTE_CHEQUE_REFERENT:
			return true;
		
		case QTE_SEUL:
		case JOKER :  
		default: 	
			throw new AmapjRuntimeException();
		}
	}
	


	private String getDefaultMessageIndicationRemplissage()
	{
		// 
		if (param.isReadOnly())
		{
			return 	"Une proposition de paiement a été calculée et est affichée ci dessous.<br/>"+
					"Vous devez cliquer sur Sauvegarder pour accepter cette proposition et valider votre contrat<br/><br/>";
		}
		else
		{
			return 	"Une proposition de paiement a été calculée et est affichée ci dessous.<br/>"+
					"Vous pouvez modifier cette proposition en saisissant directement les montants en face de chaque mois<br/>"+
					"Le dernier mois est calculé automatiquement pour ajuster le contrat<br/><br/>";
		}
	}



	private String getDefaultMessageOrdreCheque()
	{
		String str = "<b>Ordre des chèques : "+paiementDTO.libCheque+"</b>";
		if (paiementDTO.referentsRemiseCheque.size()>0)
		{
			ProdUtilisateurDTO r = paiementDTO.referentsRemiseCheque.get(0);
			str = str + "<br/><b>Chèques à remettre à "+r.prenom+" "+r.nom+"</b>";
		}
		return str;
	}


	private String computeRightPart(DatePaiementDTO datePaiement)
	{
		if (datePaiement.montant==0)
		{
			return "";
		}
		switch (datePaiement.etatPaiement)
		{
		case A_FOURNIR: return "À fournir";
		case AMAP: return "À l'AMAP";
		case PRODUCTEUR: return "Chez le producteur";
			

		default: throw new AmapjRuntimeException();
		}
	}

	private CurrencyLine computeLine(DatePaiementDTO datePaiement, int index, int nbLineModifiable)
	{
		CurrencyLine line = new CurrencyLine();
		
		line.montant= datePaiement.montant;
		line.index = index;
		line.datePaiement = datePaiement.datePaiement;
		line.rightPart = computeRightPart(datePaiement);
		line.status = computeStatus(datePaiement,nbLineModifiable,paiementDTO.isLastLineModifiable(datePaiement));
		return line;
	}


	private CurrencyLineStatus computeStatus(DatePaiementDTO datePaiement, int nbLineModifiable,boolean isLastLineModifiable)
	{
		//
		if (datePaiement.isModifiable==false)
		{
			return CurrencyLineStatus.READ_ONLY;
		}
		
		switch (data.modeSaisie)
		{
		case STANDARD:
		case FOR_TEST:
			return computeStandard(nbLineModifiable,isLastLineModifiable);
			
		
		case CHEQUE_SEUL:
		case QTE_CHEQUE_REFERENT:
			return CurrencyLineStatus.EDITABLE;
		
		case READ_ONLY:
		case QTE_SEUL:
		case JOKER :  
		default: 	
			throw new AmapjRuntimeException();
		}
	}

	private CurrencyLineStatus computeStandard(int nbLineModifiable,boolean isLastLineModifiable) 
	{
		// Si le parametrage indique que la proposition n'est pas modifiable par l'adhérent 
		if (paiementDTO.saisiePaiementModifiable==SaisiePaiementModifiable.NON_MODIFIABLE)
		{
			return CurrencyLineStatus.READ_ONLY;
		}
		
		// Si il y a une seule ligne modifiable, elle sera calculée automatiquement , donc l'utilisateur n'y a pas accés 
		if (nbLineModifiable==1)
		{
			return CurrencyLineStatus.READ_ONLY;
		}
		else
		{
			// Si c'est la dernière ligne modifiable
			if (isLastLineModifiable)
			{
				return CurrencyLineStatus.ADJUST;
			}
			else
			{
				return CurrencyLineStatus.EDITABLE;	
			}
		}
	}


	public void performSauvegarder() throws OnSaveException
	{
		// Verification de la coherence
		for (CurrencyLine line : param.lines) 
		{
			if (line.montant<0)
			{
				throw new OnSaveException("Les paiements saisis sont incorrects. Il y a un trop payé de "+new CurrencyTextFieldConverter().convertToString(-line.montant)+" €");
			}
		}
		
		// Copie dans le DTO
		for (int i = 0; i < param.getNbLines(); i++)
		{
			CurrencyLine line = param.lines.get(i);
			paiementDTO.datePaiements.get(i).montant = line.montant;
		}
		
		// On memorise la validation  
		data.validate();
	}
	
}

