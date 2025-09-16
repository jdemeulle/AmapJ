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

import fr.amapj.model.models.contrat.modele.GestionPaiement;
import fr.amapj.service.services.gestioncontrat.DatePaiementModeleContratDTO;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.gestioncontrat.ModeleContratDTO;
import fr.amapj.service.services.mespaiements.PaiementService;
import fr.amapj.view.engine.collectioneditor.FieldType;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.popup.formpopup.validator.CollectionNoDuplicates;
import fr.amapj.view.engine.popup.formpopup.validator.CollectionSizeValidator;
import fr.amapj.view.engine.popup.formpopup.validator.ColumnNotNull;
import fr.amapj.view.engine.popup.formpopup.validator.IValidator;

/**
 * Permet de modifier les dates de paiements
 * 
 *
 */
public class PopupPaiementDate extends WizardFormPopup
{
	private ModeleContratDTO modeleContrat;

	/**
	 * 
	 */
	public PopupPaiementDate(Long id)
	{
		setWidth(80);
		popupTitle = "Modification des dates de paiement d'un contrat (ajout / suppression)";
		
		// Chargement de l'objet  à modifier
		modeleContrat = new GestionContratService().loadModeleContrat(id);
		
		setModel(modeleContrat);

	}
	
	
	protected String checkInitialCondition()
	{
		if (modeleContrat.gestionPaiement==GestionPaiement.NON_GERE)
		{
			String str = "Ce contrat ne gère pas les paiements, vous ne pouvez donc pas modifier les dates de paiements.<br/>"+
						"Si vous souhaitez que ce contrat gére les paiements, vous devez aller dans \"Gestion des contrats signés\", puis vous cliquez sur le bouton \"Modifier en masse\" puis \"Gérer / Ne plus gérer les paiements\"<br/>";
			return str;
		}
		return null;
	}
	
	
	
	@Override
	protected void configure()
	{
		add(()->addFieldInfo());
		add(()->addFieldDatePaiement());
		
	}

	private void addFieldInfo()
	{
		// Titre
		setStepTitle("information");
		
		//
		String content = 	"Cet outil vous permet d'ajouter des dates de paiement ou d'en supprimer<br/>"+
							"même si il y déjà des contrats signés.<br/><br/>"+
							"Par contre, une date  ne pourra pas être supprimée si un adhérent a positionné un chèque dessus<br/>";
		addHtml(content);

	}

	private void addFieldDatePaiement()
	{
		setStepTitle("la modification des dates de paiements");
		
		IValidator size = new CollectionSizeValidator<DatePaiementModeleContratDTO>(1, null);
		IValidator noDuplicates = new CollectionNoDuplicates<DatePaiementModeleContratDTO>(e->e.datePaiement);
							
		//
		addCollectionEditorField("Liste des dates de paiement", "datePaiements", DatePaiementModeleContratDTO.class,size,noDuplicates);
		addColumn("datePaiement", "Date",FieldType.DATE, null,new ColumnNotNull<DatePaiementModeleContratDTO>(e->e.datePaiement));		
	}


	@Override
	protected void performSauvegarder() throws OnSaveException
	{
		new PaiementService().updateDatePaiement(modeleContrat);
	}
	
}
