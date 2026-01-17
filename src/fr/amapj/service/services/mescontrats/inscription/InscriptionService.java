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
 package fr.amapj.service.services.mescontrats.inscription;

import java.util.Date;
import java.util.List;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.DateUtils;
import fr.amapj.common.GenericUtils.Ret;
import fr.amapj.model.models.contrat.modele.GestionPaiement;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.NatureContrat;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.model.models.contrat.reel.TypInscriptionContrat;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.mescontrats.ContratLigDTO;
import fr.amapj.service.services.mescontrats.ContratStatusService;
import fr.amapj.service.services.mescontrats.DatePaiementDTO;
import fr.amapj.service.services.mescontrats.inscription.abo.ICAbonnementDTO;
import fr.amapj.service.services.mescontrats.inscription.abo.ICAbonnementService;
import fr.amapj.service.services.mescontrats.inscription.carteprepayee.ICCartePrepayeeService;
import fr.amapj.service.services.mescontrats.inscription.libre.ICLibreDTO;
import fr.amapj.service.services.mescontrats.inscription.libre.ICLibreService;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.ModeSaisie;
import fr.amapj.view.views.saisiecontrat.abo.ContratAbo;
import fr.amapj.view.views.saisiecontrat.step1qte.abo.ContratAboManager;
import fr.amapj.view.views.saisiecontrat.step1qte.utils.QtePopupType;


public class InscriptionService 
{	
	private ContratDTO contratDTO;
	private ModeSaisie modeSaisie;
	private Date now;
	private Contrat c;
	private ModeleContrat mc;
	private InscriptionDTO inscriptionDTO;
	
	private boolean isRetardataire;

	
	/**
	 * Ce service va remplir les 4 champs suivants
	 * 
	 * 
	 *  contratDTO.modeSaisie
	 *  
	 *  contratDTO.typInscriptionContrat 
	 *  
	 *  contratDTO.contratLigDTO.isModifiable
	 *  
	 *  inscriptionDTO.popupType
	 *  
	 *  
	 *  Pour contratDTO.typInscriptionContrat : 
	 *                   -> si contrat existant : ce champ n'est pas modifié , il a la valeur inscrite dans le contrat en base 
	 *                   -> si creation d'un nouveau contrat : on le valorise à la bonne valeur  
	 * 
	 */
	public InscriptionService(ContratDTO contratDTO,ModeSaisie modeSaisie,Date now,Contrat c,ModeleContrat mc, InscriptionDTO inscriptionDTO)
	{
		this.contratDTO = contratDTO;
		this.modeSaisie = modeSaisie;
		this.now = now;
		this.c = c;
		this.mc = mc;
		this.inscriptionDTO = inscriptionDTO;
		
		// On positione par defaut false - On ecrasera avec true dans les cas necessaires
		this.isRetardataire = false;
	}
	
	/**
	 * Le popup utilisé pour la saisie des quantités va dépendre :
	 * -> de la nature du contrat : LIBRE ou ABO ou CARTE_PREPAYEE
	 * -> du cas d'usage : est on en STANDARD ou FOR_TEST ou ..
	 * -> du type d'inscription : STANDARD ou RETARDATAIRE ou NONE ou ... 
	 * 
	 * $1/ Un certain nombre de verifications sont faites, 
	 * Le cas suivant est en particulier vérifié : le contrat peut ne plus être modifiable - Ceci arrive si : à 23h59 l'utilisateur affiche l'écran "Mes contrats"
	 * à 00:01 il clique sur Modifier - le bouton est bien là mais il n'a plus le droit de modifier
	 */
	public void buildInscriptionDTO() 
	{
		//
		contratDTO.modeSaisie = modeSaisie;
		
		// On calcule tout d'abord les informations relatives aux quantités
		switch (contratDTO.nature) 
		{
			case LIBRE: 			computeLibre(); break;
			case CARTE_PREPAYEE : 	computeCartePrepayee(); break;
			case ABONNEMENT:		computeAbonnement(); break;		
			default : throw new AmapjRuntimeException();
		}
		
		// On positionne ensuite le champ typInscriptionContrat dans le cas de la creation d'un contrat
		if (c==null)
		{
			contratDTO.typInscriptionContrat = isRetardataire ? TypInscriptionContrat.RETARDATAIRE : TypInscriptionContrat.STANDARD; 
		}
		
		// On verifie ensuite si il y a des lignes modifiables pour les quantités
		if (isNbQteLigModifiableOK()==false)
		{
			error("Il n'est plus possible de modifier ce contrat (aucune date n'est modifiable)");
			return;
		}
		
		// On calcule ensuite les informations relatives aux paiements
		computePaiementInfo();
		
		
		// On verifie ensuite si il y a des lignes modifiables pour les paiements 
		if (isNbPaiementModifiableOK()==false)
		{
			error("Il n'est plus possible de modifier ce contrat car aucun paiement n'est modifiable.");
			return;
		}

	}

	//==================== LIBRE
	
	private void computeLibre()  
	{
		switch (modeSaisie) 
		{
		case STANDARD:
			libreStandard();
			return;

		// Pour tous les autres cas : TEST, VISUALISATION , QTE_SEUL , CHEQUE_SEUL , QTE_CHEQUE_REFERENT , on voit tout (pas d'exclusion)
		// En effet, le référent a toujours le droit de modifier toutes les quantités à toutes les dates , par exemple pour les corrections  
		case FOR_TEST:
		case QTE_SEUL:
		case QTE_CHEQUE_REFERENT:
			inscriptionDTO.popupType = QtePopupType.POPUP_LIBRE;
			fullModifiable();
			return;
			
		case READ_ONLY:
		case CHEQUE_SEUL :
			inscriptionDTO.popupType = QtePopupType.POPUP_LIBRE;
			noModifiable();
			return;
			
			
		default: throw new AmapjRuntimeException();
		}
	}
	

	private void libreStandard()  
	{
		ICLibreDTO icLibre = new ICLibreService().computeLibre(contratDTO, now,c,mc);
		
		switch (icLibre.status) 
		{
			case STANDARD: 
				inscriptionDTO.popupType = QtePopupType.POPUP_LIBRE;
				fullModifiable();
				return;
				
			case RETARDAIRE :
				isRetardataire = true;
				inscriptionDTO.popupType = QtePopupType.POPUP_LIBRE;
				modifiableFrom(icLibre.startDateRetardataire);
				return;
				
			case NONE : 
				error("Il est trop tard pour modifier ce contrat ou pour s'inscrire."); // Voir $1
				return;
				
			default : 
				 throw new AmapjRuntimeException();
		}
	}

	

	

	//==================== CARTE PREPAYEE
	

	private void computeCartePrepayee()  
	{
		switch (modeSaisie) 
		{
		case STANDARD:
			cartePrepayeeStandard();
			return;
			
			
		// Pour tous les autres cas : TEST, VISUALISATION , QTE_SEUL , CHEQUE_SEUL , QTE_CHEQUE_REFERENT , on voit tout (pas d'exclusion)
		// En effet, le référent a toujours le droit de modifier toutes les quantités à toutes les dates , par exemple pour les corrections  
		case FOR_TEST:
		case QTE_SEUL :
		case QTE_CHEQUE_REFERENT:
			inscriptionDTO.popupType = QtePopupType.POPUP_LIBRE;
			fullModifiable();
			return;
			
			
		case READ_ONLY:
		case CHEQUE_SEUL:
			inscriptionDTO.popupType = QtePopupType.POPUP_LIBRE;
			noModifiable();
			return;
			
		default: throw new AmapjRuntimeException();
		}
	}
	
	
	private void cartePrepayeeStandard() 
	{
		Date startDate = new ICCartePrepayeeService().computeCartePrepayee(contratDTO, now,c,mc); 
		if (startDate==null)
		{
			error("Il est trop tard pour modifier ce contrat ou pour s'inscrire.");
			return;
		}
		else
		{
			inscriptionDTO.popupType = QtePopupType.POPUP_LIBRE;
			modifiableFrom(startDate);
			return;
		}
	}

	//==================== ABONNEMENT 

	private void computeAbonnement()  
	{
		switch (modeSaisie) 
		{
		case STANDARD: 
			abonnementStandard();
			return;
			
		// Pour le test : mode abonnement en permanence   
		case FOR_TEST:
			inscriptionDTO.popupType = QtePopupType.POPUP_ABO;
			contratDTO.contratAbo = new ContratAboManager().computeContratAboNewContrat(contratDTO,null);
			fullModifiable();
			return;
			
		// Pour QTE_SEUL , QTE_CHEQUE_REFERENT , on voit tout en mode grille 
		// En effet, le référent a toujours le droit de modifier toutes les quantités à toutes les dates , par exemple pour les corrections  
		case QTE_SEUL :
		case QTE_CHEQUE_REFERENT:
			inscriptionDTO.popupType = QtePopupType.POPUP_LIBRE;
			fullModifiable();
			return;

			
		// Quand on visualise : mode grille et on voit tout   
		case READ_ONLY:
			abonnementReadOnly();
			return;
			
		case CHEQUE_SEUL:
			inscriptionDTO.popupType = QtePopupType.POPUP_LIBRE;
			noModifiable();
			return;
			
			
		case JOKER:
			abonnementJoker();
			return;
			
		default: throw new AmapjRuntimeException();
		}
		
	}

	private void abonnementStandard()
	{
		ICAbonnementDTO ic = new ICAbonnementService().computeAbonnement(contratDTO, now,c,mc);
				
		switch (ic.status) 
		{
			case STANDARD:
				fullModifiable();
				abonnementStandardCreateFacadeAbo(ic);
				return;
				
				
			case RETARDAIRE : 	
				modifiableFrom(ic.startDateRetardataire);
				isRetardataire = true;
				abonnementStandardCreateFacadeAbo(ic);
				return;
				
				
			case JOKER_OU_NONE: 
				error("Il est trop tard pour modifier ce contrat ou pour s'inscrire."); // Voir $1
				return;
				
			default : 
				throw new AmapjRuntimeException();
				
		}
	}
		
	
	private void abonnementStandardCreateFacadeAbo(ICAbonnementDTO ic)
	{
		inscriptionDTO.popupType = QtePopupType.POPUP_ABO;
		
		// Si pas de contrat existant
		if (c==null)
		{
			contratDTO.contratAbo = new ContratAboManager().computeContratAboNewContrat(contratDTO, ic.startDateRetardataire);
			return ;
		}
		
		// Si un contrat existant
		Ret<ContratAbo> ret = new ContratAboManager().computeContratAboExistingContrat(contratDTO, c.aboInfo);
		if (ret.isOK())
		{
			contratDTO.contratAbo = ret.get();
		}
		else
		{
			error("Vous ne pouvez pas modifier ce contrat car il a  été modifié par le référent. Détails : "+ret.msg());
		}		
	}
	
	
	private void abonnementReadOnly() 
	{
		Ret<ContratAbo> ret = new ContratAboManager().computeContratAboExistingContrat(contratDTO,c.aboInfo);
		if (ret.isOK())
		{
			contratDTO.contratAbo = ret.get(); 
			inscriptionDTO.popupType = QtePopupType.POPUP_ABO;
			noModifiable();
		} 
		else
		{
			inscriptionDTO.popupType = QtePopupType.POPUP_LIBRE;
			noModifiable();
		}
	}
	

	private void abonnementJoker()
	{
		Ret<ContratAbo> ret = new ContratAboManager().computeContratAboExistingContrat(contratDTO,c.aboInfo);
		if (ret.isOK()==false)
		{
			error("Vous ne pouvez pas modifier vos jokers car ceux ci ont été modifiés par le référent. Détails : "+ret.msg());
			return;
		}
		
		contratDTO.contratAbo = ret.get();
		// Calcul des lignes modifiables par des jokers	
		for (ContratLigDTO lig : contratDTO.contratLigs) 
		{
			lig.isModifiable = new ContratStatusService().isDateModifiableAbonnementJoker(lig.date, now, mc);
		}
		
		return;
	}

	//====================

	
	private void error(String message) 
	{
		inscriptionDTO.popupType = null;
		inscriptionDTO.errorMessage = message;
	}
	
	private void fullModifiable()
	{
		for (ContratLigDTO lig : contratDTO.contratLigs)
		{
			lig.isModifiable = true;
		}	
	}
	
	private void noModifiable()
	{
		for (ContratLigDTO lig : contratDTO.contratLigs)
		{
			lig.isModifiable = false;
		}	
	}
	
	private void modifiableFrom(Date startDate)
	{
		for (ContratLigDTO lig : contratDTO.contratLigs)
		{
			lig.isModifiable = lig.date.after(startDate) || lig.date.equals(startDate); 
		}	
	}	
	
	
	// ======================== PARTIE PAIEMENT
	
	private void computePaiementInfo() 
	{	
		for (DatePaiementDTO datePaiementDTO : contratDTO.paiement.datePaiements) 
		{
			datePaiementDTO.isModifiable = computeDatePaiementModifiable(datePaiementDTO);
		}
		
		//
		setNonModifableDateInPast();
	}
	
	private boolean computeDatePaiementModifiable(DatePaiementDTO datePaiement)
	{
		switch (modeSaisie)
		{
		case STANDARD: 
			switch (datePaiement.etatPaiement)
			{
				case A_FOURNIR: 
					return true;
				case AMAP : 
				case PRODUCTEUR :
					return false;
				default: throw new AmapjRuntimeException();
			}
			
		case FOR_TEST:
			return true;

		case READ_ONLY:
			return false;
		
		case CHEQUE_SEUL:
		case QTE_CHEQUE_REFERENT:
			switch (datePaiement.etatPaiement)
			{
				case A_FOURNIR:
				case AMAP : 
					return true;
		
				case PRODUCTEUR :
					return false;
					
				default: 
					throw new AmapjRuntimeException();
			}
		
		case QTE_SEUL:
		case JOKER :  
			return false;
		default: 	
			throw new AmapjRuntimeException();
		}
	}

	
	/**
	 * si on est en saisie STANDARD, 
	 * dans le cas retardataire ou carte prepayée, 
	 * on passe les lignes dont la date est dans (le passé ou moins de 5 jours) à non modifiable, sauf la dernière ligne
	 */
	private void setNonModifableDateInPast() 
	{
		Date ref = DateUtils.addDays(now, 5);
		
		if (modeSaisie!=ModeSaisie.STANDARD)
		{
			return;
		}
		
		if (contratDTO.paiement.getNbModifiable()<=1)
		{
			return;
		}
		
		if (isRetardataire || contratDTO.nature==NatureContrat.CARTE_PREPAYEE) 
		{
			List<DatePaiementDTO> datePaiements = contratDTO.paiement.datePaiements;
			for (int i = 0; i < datePaiements.size()-1; i++)
			{
				DatePaiementDTO datePaiementDTO = datePaiements.get(i);
				if (datePaiementDTO.isModifiable && datePaiementDTO.datePaiement.before(ref))
				{
					datePaiementDTO.isModifiable = false;
				}
			}
		}
	}
	
	// VERIFICATION DE COHERENCE
	
	private boolean isNbQteLigModifiableOK()
	{
		// La verification est faite uniquement en mode STANDARD
		if (modeSaisie!=ModeSaisie.STANDARD)
		{
			return true;
		}
		
		long nb = contratDTO.contratLigs.stream().filter(e->e.isModifiable).count();
		if (nb==0)
		{
			return false;
		}
		else
		{
			return true;
		}
		
	}
	
	
	private boolean isNbPaiementModifiableOK()
	{
		// La verification est faite uniquement en mode STANDARD 
		if (modeSaisie!=ModeSaisie.STANDARD)
		{
			return true;
		}
		
		// La verification est faite uniquement si les paiements sont gérés
		if (mc.gestionPaiement==GestionPaiement.NON_GERE)
		{
			return true;
		}
		
		int nb = contratDTO.paiement.getNbModifiable();
		if (nb==0)
		{
			return false;
		}
		else
		{
			return true;
		}
		
	}
}
