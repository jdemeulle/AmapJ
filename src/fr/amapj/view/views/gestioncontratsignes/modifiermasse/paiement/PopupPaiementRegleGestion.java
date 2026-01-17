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

import java.util.Arrays;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;

import fr.amapj.model.models.contrat.modele.GestionPaiement;
import fr.amapj.model.models.contrat.modele.SaisiePaiementProposition;
import fr.amapj.model.models.contrat.modele.StrategiePaiement;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.gestioncontrat.ModeleContratDTO;
import fr.amapj.service.services.mespaiements.PaiementService;
import fr.amapj.view.engine.popup.formpopup.fieldlink.FieldLink;
import fr.amapj.view.engine.popup.formpopup.validator.NotNullValidator;
import fr.amapj.view.views.gestioncontrat.editorpart.GestionContratEditorPart;
import fr.amapj.view.views.gestioncontrat.editorpart.utils.GestionContratPaiementUtils;

/**
 * Permet de modifier les infos de paiements
 * 
 *
 */
public class PopupPaiementRegleGestion extends GestionContratEditorPart
{
	private int nbInscrits;

	/**
	 * 
	 */
	public PopupPaiementRegleGestion(Long id)
	{
		setWidth(80);
		popupTitle = "Modification des conditions de paiement d'un contrat";
		
		// Chargement de l'objet  à modifier
		modeleContrat = new GestionContratService().loadModeleContrat(id);
		new GestionContratPaiementUtils().checkAndUpdateStrategieAndSaisiePaiementCalculDate(modeleContrat);
		
		setModel(modeleContrat);

	}
	
	@Override
	protected void configure()
	{
		add(()->addIntro());
		add(()->addPaiement(),()->checkTypePaiement());
		add(()->addDetailPaiement());
	}

	private void addIntro()
	{
		// Titre
		setStepTitle("les informations générales.");
		
		nbInscrits = new GestionContratService().getNbInscrits(modeleContrat.id);
		String str;
		
		if (nbInscrits==0)
		{
			str = "Aucun adhérent n'est inscrit à ce contrat. Vous pouvez donc modifier les règles de paiements librement.";
		}
		else
		{
			
			str = 	""+nbInscrits+" adhérents ont déjà souscrits à ce contrat.<br/>"+
					"La modification des règles de paiements vont donc impacter les contrats existants.<br/><br/>"+
					"Il est souhaitable de vérifier manuellement chaque contrat.<br/>";						 
		}
		addHtml(str);
	}

	
	private void addPaiement() 
	{
		if (nbInscrits==0)
		{
			drawTypePaiement(new StrategiePaiement[] { StrategiePaiement.NON_GERE});
			return ;
		}
	
		// Si il y a des inscrits, on force la strategie à PLUS_DE_CHOIX 
		modeleContrat.strategiePaiement = StrategiePaiement.PLUS_DE_CHOIX;
		
		setStepTitle("genéralités sur le paiement");
		
		addComboEnumField("Mode de gestion des paiements", "strategiePaiement").setReadOnly(true);
		
		ComboBox c = addComboEnumField("Mode de calcul de la proposition de paiement", "saisiePaiementProposition",new NotNullValidator());
		
		FieldLink f1 = new FieldLink(validatorManager,Arrays.asList(SaisiePaiementProposition.PAYE_AVANCE_ARRONDI,SaisiePaiementProposition.REPARTI_ARRONDI),c);
			
		TextField c2 = addCurrencyField("Montant minimum des chèques", "montantChequeMiniCalculProposition",false);
		f1.addField(c2);
			
		addComboEnumField("La proposition de paiement est elle modifiable ? ", "saisiePaiementModifiable",new NotNullValidator());		
	}

	
	
	private void addDetailPaiement() 
	{
		drawDetailPaiement(nbInscrits==0);
	}


	

	@Override
	protected void performSauvegarder()
	{
		new PaiementService().updateInfoPaiement(modeleContrat,nbInscrits==0);
	}
	
	/**
	 * 
	 */
	@Override
	protected String checkInitialCondition()
	{
		if (modeleContrat.gestionPaiement==GestionPaiement.NON_GERE)
		{
			String str = "Vous ne pouvez pas modifier les conditions de paiement de ce contrat<br/>"+
						 "car ce contrat ne gére pas les paiements.<br/><br/>."+
						 "Si vous souhaitez que ce contrat gére les paiements, vous devez aller dans \"Gestion des contrats signés\", puis vous cliquez sur le bouton \"Modifier en masse\" puis \"Gérer / Ne plus gérer les paiements\"<br/>";
			return str;
		}
		
		return null;
	}
}
