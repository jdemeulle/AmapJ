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
 package fr.amapj.view.engine.popup.archivagepopup;

import java.util.List;

import com.vaadin.ui.TextField;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.service.services.archivage.tools.ArchivableState;
import fr.amapj.service.services.parametres.ParametresArchivageDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;

/**
 * Popup generique pour l'archivage des élements
 * 
 *
 */
abstract public class ArchivagePopup extends WizardFormPopup
{
	
	/**
	 * Permet de fournir le texte de la première étape : des informations générales sur l'élement à archiver
	 */
	protected abstract String getInfo();

	/**
	 * Permet de fournir le texte de la deuxième étape : les conditions pour pouvoir archiver un element quelconque
	 */
	protected abstract String computeArchivageLib();
	
	/**
	 * Permet de fournir le status de la deuxième étape : le status de l'élement courant pour l'archivage 
	 */
	protected abstract ArchivableState computeArchivageState();
	
	/**
	 * Réalise l'archivage final de l'element 
	 */
	protected abstract void archiveElement();
	

	protected ParametresArchivageDTO param;

	private ArchivableState state;

	private TextField tf;


	/**
	 * 
	 */
	public ArchivagePopup()
	{
		setWidth(60);
		setHeight("50%");
		param = new ParametresService().getParametresArchivage();
		
	}
		
	@Override
	protected void configure()
	{
		add(()->addFieldInfo());
		add(()->addFieldConditions());
		add(()->addFieldConfirmation(),()->checkConfirmation());
	}
	
	
	private void addFieldInfo()
	{
		String str = getInfo();
		addHtml(str);
	}


	
	private void addFieldConditions()
	{
		String str = computeArchivageLib();
		addHtml(str);
		
	}
	
	

	private void addFieldConfirmation()
	{
		state = computeArchivageState();
		tf=null;
		switch (state.getStatus()) 
		{
		case OUI_SANS_RESERVE:
			addHtml("Ce élément respecte bien toutes les conditions pour pouvoir être archivé.");
			addHtml("Veuillez cliquer sur Sauvegarder pour réaliser l'archivage, ou Annuler pour ne rien faire.");
			return;

		case OUI_AVEC_RESERVE_MINEURE:
			addHtml("Ce élément peut être archivé, mais il ne respecte pas les conditions suivantes (réserves mineures) : ");
			addHtml(asHtmlList(state.reserveMineures));
			addHtml("Veuillez cliquer sur Sauvegarder pour réaliser l'archivage, ou Annuler pour ne rien faire.");
			return;
			
		case OUI_AVEC_RESERVE_MAJEURE:
			addHtml("Ce élément ne devrait pas être archivé car il ne respecte pas les conditions suivantes (réserves majeures) : ");
			addHtml(asHtmlList(state.reserveMajeures));
			addHtml("Néanmoins, si vous pensez que ce élement est bien archivable, vous pouvez l'archiver quand même en saisissant ci dessous le texte <br/>JE CONFIRME<br/> puis en cliquant sur Sauvegarder");
			
			tf = new TextField("");
			tf.setValue("");
			tf.setImmediate(true);
			form.addComponent(tf);
			return;
			
		case NON:
			addHtml("Ce élément ne peut pas être archivé pour les raisons suivantes : ");
			addHtml(asHtmlList(state.nonArchivables));
			setBackOnlyMode();
			return;
			
		default:
			throw new AmapjRuntimeException("state="+state.getStatus());
		}
	}



	private String asHtmlList(List<String> strs) 
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<ul>");
		for (String str : strs) 
		{
			sb.append("<li>");
			sb.append(str);
			sb.append("</li>");
		}
		sb.append("</ul>");
		return sb.toString();
	}
	
	
	private String checkConfirmation() 
	{
		if (tf==null)
		{
			return null;
		}
		
		if (tf.getValue().equals("JE CONFIRME"))
		{
			return null;
		}
		return "Pour pouvoir continuer, vous devez saisir le texte JE CONFIRME (après vous êtes assurés que cet élement est bel et bien archivable)"; 
	}
	
	

	@Override
	protected void performSauvegarder() throws OnSaveException
	{
		archiveElement();
	}

}
