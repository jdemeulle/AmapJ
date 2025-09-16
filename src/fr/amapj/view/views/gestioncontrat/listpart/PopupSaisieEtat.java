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
 package fr.amapj.view.views.gestioncontrat.listpart;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.models.contrat.modele.EtatModeleContrat;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.gestioncontrat.ModeleContratSummaryDTO;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;

/**
 * Popup pour la saisie de l'état du contrat
 *  
 */
public class PopupSaisieEtat extends WizardFormPopup
{
	private ModeleContratSummaryDTO mcDto;
	
	private EtatModeleContrat selectedValue;
	
	
	@Override
	protected void configure()
	{
		add(()->addSaisie());
		add(()->addConfirmation());
	}
	
	

	/**
	 * 
	 */
	public PopupSaisieEtat(ModeleContratSummaryDTO mcDto)
	{
		popupTitle = "Changement de l'état d'un contrat";
		this.mcDto = mcDto;
	}
	
	
	protected void addSaisie()
	{
		String intro = computeIntro();
		addHtml(intro);	
	}

	private String computeIntro()
	{
		String str = "Votre contrat est actuellement dans l'état "+mcDto.etat+".<br/>";
		
		switch (mcDto.etat)
		{
		case CREATION:
			str = str +"Pour le moment, votre contrat n'est pas visible par les amapiens. Si vous le passez à l'état ACTIF, alors les amapiens pourront s'inscrire à ce contrat.";
			break;

		case ACTIF:
			str = str +"Il est donc visible par tous les amapiens. Si vous le passez à l'état CREATION, alors il ne sera plus visible par les amapiens.<br/> ATTENTION : ceci bloquera aussi l'envoi automatique des feuilles de distribution au producteur.";
			break;
			
		default:
			throw new AmapjRuntimeException("etat="+mcDto.etat);
		}
		
		str = str +"<br/><br/>";
		
		return str;
	}
	
	
	
	private void addConfirmation()
	{
		selectedValue = computeNewEtat();
		String str = "Vous allez passer votre contrat du statut "+mcDto.etat+" au statut "+selectedValue+".<br/><br/>Cliquez sur Sauvegardez pour confirmer cette modification, ou Annuler pour ne rien faire.";
		addHtml(str);
	}
	
	private EtatModeleContrat computeNewEtat() 
	{
		switch (mcDto.etat)
		{
		case CREATION:
			return EtatModeleContrat.ACTIF;

		case ACTIF:
			return EtatModeleContrat.CREATION;
			
		default:
			throw new AmapjRuntimeException("etat="+mcDto.etat);
		}
	}

	protected void performSauvegarder()
	{
		new GestionContratService().updateEtat(selectedValue,mcDto.id);
	}
}
