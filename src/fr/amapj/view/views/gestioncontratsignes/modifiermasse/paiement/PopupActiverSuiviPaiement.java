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
 package fr.amapj.view.views.gestioncontratsignes.modifiermasse.paiement;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.models.contrat.modele.GestionPaiement;
import fr.amapj.model.models.contrat.modele.StrategiePaiement;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.mespaiements.PaiementService;
import fr.amapj.view.views.gestioncontrat.editorpart.GestionContratEditorPart;
import fr.amapj.view.views.gestioncontrat.editorpart.utils.GestionContratPaiementUtils;

/**
 * Permet de d'activer la gestion des paiements sur un contrat
 * 
 *
 */
public class PopupActiverSuiviPaiement extends GestionContratEditorPart
{
	private int nbInscrits;	

	/**
	 * 
	 */
	public PopupActiverSuiviPaiement(Long id)
	{
		setWidth(80);
		popupTitle = "Activer le suivi des paiements pour un contrat";
		
		// Chargement de l'objet  à modifier
		modeleContrat = new GestionContratService().loadModeleContrat(id);
		new GestionContratPaiementUtils().checkAndUpdateStrategieAndSaisiePaiementCalculDate(modeleContrat);
		
		setModel(modeleContrat);
		
		if (modeleContrat.gestionPaiement==GestionPaiement.GESTION_STANDARD)
		{
			throw new AmapjRuntimeException();
		}
		
		nbInscrits =  new GestionContratService().getNbInscrits(id);

	}
	
	@Override
	protected void configure()
	{
		add(()->addIntro());
		add(()->addStep2(),()->checkStep2());
		add(()->addStep3());
	}

	private void addIntro()
	{
		// Titre
		setStepTitle("les informations générales.");

		String str = "Le suivi des paiements n'est pas activé pour ce contrat pour le moment. En continuant, vous allez pouvoir activer le suivi des paiement.<br/><br/>"+
					 "Par contre, "+nbInscrits+" adhérents ont déjà souscrits à ce contrat, pour ces personnes il sera IMPERATIF de saisir manuellement les paiements.<br/>"+
					 " Cliquer sur Continuer pour activer le suivi des paiements";
		addHtml(str);
		
	}

	
	private void addStep2() 
	{
		// Valeur par défaut
		modeleContrat.strategiePaiement = StrategiePaiement.UN_CHEQUE_PAR_MOIS_LISSE_MODIFIABLE_1ERE_LIVRAISON;
		
		//
		drawTypePaiement(new StrategiePaiement[] { StrategiePaiement.NON_GERE});
		
	}
	
	private String checkStep2() 
	{
		return checkTypePaiement();		
	}
	
	private void addStep3() 
	{
		drawDetailPaiement(true);
	}


	@Override
	protected void performSauvegarder()
	{
		new PaiementService().updateInfoPaiement(modeleContrat,true);
	}
	
	/**
	 * 
	 */
	@Override
	protected String checkInitialCondition()
	{
		if (nbInscrits==0)
		{
			String str = "Aucun adhérent n'est inscrit à ce contrat. Vous pouvez donc modifier les règles de paiements librement, "
					+ "en allant dans \"Gestion des contrats vierges\", puis vous cliquez sur le bouton \"Modifier\" puis \"les informations de paiements\"<br/>";
			return str;
		}
		
		return null;
	}
}
