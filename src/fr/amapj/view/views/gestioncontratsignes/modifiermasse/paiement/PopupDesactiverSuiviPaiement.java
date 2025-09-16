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

import com.vaadin.ui.TextField;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.models.contrat.modele.GestionPaiement;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.mespaiements.PaiementService;
import fr.amapj.view.views.gestioncontrat.editorpart.GestionContratEditorPart;
import fr.amapj.view.views.gestioncontrat.editorpart.utils.GestionContratPaiementUtils;

/**
 * Permet de supprimer le suivi des paiements sur un modele de contrat
 * 
 *
 */
public class PopupDesactiverSuiviPaiement extends GestionContratEditorPart
{
	private int nbInscrits;
	private TextField tf;	

	/**
	 * 
	 */
	public PopupDesactiverSuiviPaiement(Long id)
	{
		setWidth(80);
		popupTitle = "Supprimer le suivi des paiements pour un contrat";
		
		// Chargement de l'objet  à modifier
		modeleContrat = new GestionContratService().loadModeleContrat(id);
		new GestionContratPaiementUtils().checkAndUpdateStrategieAndSaisiePaiementCalculDate(modeleContrat);
		
		setModel(modeleContrat);
		
		if (modeleContrat.gestionPaiement==GestionPaiement.NON_GERE)
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

		String str = "Le suivi des paiements est activé pour ce contrat pour le moment. En continuant, vous allez SUPPRIMER toutes les informations relatives au paiement<br/><br/>"+
				 	 nbInscrits+" adhérents ont déjà souscrits à ce contrat, TOUTES LES INFORMATIONS DE PAIEMENT SERONT EFFACEES<br/>"+
				 	 " Cliquer sur Continuer pour confirmer cette suppression";
		addHtml(str);
		
	}

	
	private void addStep2() 
	{
		setStepTitle("confirmation");
		
		boolean b = new PaiementService().canSuppressPaiementModeleContrat(modeleContrat.id);
		if (b==false)
		{
			addHtml("Ce contrat contient des chèques à l'état AMAP ou PRODUCTEUR. Il faut supprimer d'abord tous ces chèques pour pouvoir continuer.");
			setBackOnlyMode();
			return;
		}
		
		
		addHtml("Vous allez effacer toutes les informations de paiements. Si vous souhaitez continuer, vous devez saisir ci dessous le texte <br/>JE CONFIRME<br/> puis cliquer sur Continuer");
		
		tf = new TextField("");
		tf.setValue("");
		tf.setImmediate(true);
		form.addComponent(tf);
		
	}
	
	private String checkStep2() 
	{
		if (tf.getValue().equals("JE CONFIRME"))
		{
			return null;
		}
		else
		{
			return "Pour pouvoir continuer, vous devez saisir le texte JE CONFIRME"; 
		}		
	}
	
	private void addStep3() 
	{
		modeleContrat.gestionPaiement = GestionPaiement.NON_GERE;
		drawDetailPaiement(true);
	}

	
	
	

	@Override
	protected void performSauvegarder()
	{
		new PaiementService().deleteInfoPaiement(modeleContrat);
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
