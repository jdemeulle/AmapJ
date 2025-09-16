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
 package fr.amapj.view.views.parametres.paramecran;

import fr.amapj.model.models.param.paramecran.PEGestionContratsVierges;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.popup.formpopup.validator.NotNullValidator;

/**
 * Permet la saisie des paramètres de l'écran "Gestion des contrtas vierges"
 * 
 */
public class PEGestionContratsViergesEditorPart extends WizardFormPopup
{

	private PEGestionContratsVierges pe;

	/**
	 * 
	 */
	public PEGestionContratsViergesEditorPart()
	{
		pe = (PEGestionContratsVierges) new ParametresService().loadParamEcran(MenuList.GESTION_CONTRAT);
		
		setWidth(80);
		popupTitle = "Paramètrage de l'écran \""+pe.getMenu().getTitle()+"\"";

		setModel(this.pe);

	}
	
	@Override
	protected void configure()
	{
		add(()->addEtape2());
		add(()->addEtape4());
		
	}

	private void addEtape2()
	{
		// Titre
		setStepTitleNoPrefix("Paramétrage de l'étape 2 : les conditions d'accès à ce contrat");
		
		addComboEnumField("La saisie d'une période de cotisation est obligatoire ", "periodeCotisationObligatoire",  new NotNullValidator());
		
	}
	
	private void addEtape4()
	{
		// Titre
		setStepTitleNoPrefix("Paramétrage de l'étape 4 : la date de fin des inscriptions");
		
		addComboEnumField("Le champ \"L'inscription en tant que retardataire est autorisée\" a pour valeur", "gestionRetardataire",  new NotNullValidator());
		
		addComboEnumField("Le champ \"Activer les jokers\" a pour valeurs possibles", "gestionJoker",  new NotNullValidator());
		
	}
	

	@Override
	protected void performSauvegarder()
	{
		new ParametresService().update(pe);
	}
}
