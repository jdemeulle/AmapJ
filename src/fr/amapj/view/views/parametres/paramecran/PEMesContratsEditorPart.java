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

import fr.amapj.model.models.param.paramecran.PEMesContrats;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.popup.formpopup.validator.NotNullValidator;

/**
 * Permet la saisie des paramètres de l'écran "mes contrats"
 * 
 */
public class PEMesContratsEditorPart extends WizardFormPopup
{

	private PEMesContrats pe;

	/**
	 * 
	 */
	public PEMesContratsEditorPart()
	{
		pe = (PEMesContrats) new ParametresService().loadParamEcran(MenuList.MES_CONTRATS);
		
		setWidth(80);
		popupTitle = "Paramètrage de l'écran \""+pe.getMenu().getTitle()+"\"";

		setModel(this.pe);

	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldGeneral());
		add(()->addFieldImpressionContrat());
	}
	
	
	private void addFieldGeneral()
	{
		// Titre
		setStepTitle("Affichage des contrats");
		
		String help = " Si Choix NON : les contrats accessibles en tant que retardaire ne sont pas visibles sur l'écran Mes contrats. L'amapien doit aller dans l'écran Découverte/Visite pour s'inscrire en tant que retardataire.<br/><br/>"
					+" Si Choix OUI : les contrats retadataire sont visibles dans l'écran Mes contrats, dans un bloc séparé des autres, nommé \"Contrats en cours, encore disponibles\"";
		addComboEnumField("Les contrats accessibles en tant que retardataire sont affichés", "affichageContratRetardataire",  help,null,new NotNullValidator());
	}
	

	private void addFieldImpressionContrat()
	{
		// Titre
		setStepTitle("Impression des contrats");
		
		addComboEnumField("L'amapien peut imprimer ses contrats au format Excel ", "canPrintContrat",  new NotNullValidator());
		
		addComboEnumField("L'amapien peut imprimer ses contrats d'engagement au format Pdf ", "canPrintContratEngagement", new NotNullValidator());
	
		addComboEnumField("Présentation des contrats à imprimer", "presentationImpressionContrat", new NotNullValidator());	
		
		String msg = "Soyez prudent si vous autorisez l'impression des contrats d'engagements par les amapiens :<br>"+	
				" Le cas suivant peut se produire :<br/><ul>"
				+ "<li>L'amapien s'inscrit à un contrat puis l'imprime</li>"
				+ "<li>l'amapien modifie le contrat, volontairement ou par erreur </li></ul><br/>"
				+ "Il est préférable que le référent imprime lui même les contrats après la date limite d'inscription<br/>"
				+ "ou alors vous devez bien vérifier la cohérence des contrats";
				
	
		addHtml(msg);
		
	}
	

	@Override
	protected void performSauvegarder()
	{
		new ParametresService().update(pe);
	}

}
