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
 package fr.amapj.view.engine.popup.formpopup;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;

import fr.amapj.common.GenericUtils;
import fr.amapj.view.engine.popup.errorpopup.ErrorPopup;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;
import fr.amapj.view.engine.tools.BaseUiTools;

/**
 * Popup contenant un formulaire basé sur un PropertysetItem ou sur un BeanItem
 * avec la gestion couplée d'un wizard
 *  
 */
abstract public class WizardFormPopup extends AbstractFormPopup
{
	protected String nextButtonTitle = "Etape suivante ...";
	protected Button nextButton;
	
	protected String previousButtonTitle = "Etape précédente ...";
	protected Button previousButton;
	
	protected Button cancelButton;
	
	protected String saveButtonTitle = "Sauvegarder";
		
	private VerticalLayout contentLayout;
	
	protected Label hTitre;
	
	private int pageNumber;
	
	// Liste des pages de ce wizard 
	private List<DetailStepInfo> pages = new ArrayList<>();
	

	private boolean errorInInitialCondition = false;
	
	
	public WizardFormPopup()
	{
		// Par défaut, la taille est à 80%  pour tous les wizards popup 
		setHeight("80%");
	}
	
	
	protected void createContent(VerticalLayout contentLayout)
	{
		//
		this.contentLayout = contentLayout;
		contentLayout.addStyleName("wizard-popup");
		
		
		// Chargement de la définition de chaque page   
		configure();
		
		
		// Vérification des conditions initiales
		String str = checkInitialCondition();
		if (str!=null)
		{
			errorInInitialCondition = true;
			displayErrorOnInitialCondition(str);
			return;
		}
		
		
		// Mise en place du titre
		hTitre = new Label("");
		hTitre.addStyleName("wizard-popup-etape");
		contentLayout.addComponent(hTitre);
		
		//
		updateForm();
		
	}
	
	/**
	 * Should be overriden
	 * @return
	 */
	protected String checkInitialCondition()
	{
		return null;
	}

	private void displayErrorOnInitialCondition(String str)
	{
		Label label = new Label(str,ContentMode.HTML);
		label.setStyleName(ChameleonTheme.LABEL_BIG);
		contentLayout.addComponent(label);
	}

	private void updateForm()
	{
		if (form!=null)
		{
			contentLayout.removeComponent(form);
		}
		
		// Construction de la forme
		form = new FormLayout();
		form.setWidth("100%");
		form.setImmediate(true);


		//
		binder = new FieldGroup();
		binder.setBuffered(true);
		binder.setItemDataSource(getModelAsItem());
		
		//
		validatorManager.reset();
		

		// Récupration des informations de la page
		DetailStepInfo stepInfo = pages.get(pageNumber);

		// Construction de la page
		stepInfo.drawScreen.action();  
		
		contentLayout.addComponent(form);
		contentLayout.setComponentAlignment(form, Alignment.MIDDLE_LEFT);
		
	}

	protected void createButtonBar()
	{
		addButtonBlank();
		
		if (errorInInitialCondition)
		{
			addDefaultButton("OK", e->close());
			return ;
		}
		
		//	
		cancelButton = addButton("Annuler", e->handleAnnuler());
		
		//
		previousButton = addButton(previousButtonTitle, e->handlePrevious());
		previousButton.setEnabled(false);
		
		//
		nextButton = addDefaultButton(nextButtonTitle, e->handleNext());
		
		// On rend invisible le bouton précédent dans le cas ou il y a une seule page
		if (pages.size()==1)
		{
			previousButton.setVisible(false);		
			nextButton.setCaption(saveButtonTitle);
		}
	}
	

	private void handleAnnuler()
	{
		// binder.discard();
		close();
	}
	
	private void handlePrevious()
	{
		boolean ret = doCommit();
		if (ret==false)
		{
			return;
		}
		
		pageNumber--;
		updateButtonStatus();
		updateForm();
	}

	private void updateButtonStatus()
	{
		// Gestion de l'état et du libellé du bouton suivant
		if (pageNumber==pages.size()-1)
		{
			nextButton.setCaption(saveButtonTitle);		
		}
		else	
		{
			nextButton.setCaption(nextButtonTitle);
		}
		nextButton.setEnabled(true);
		
		// Gestion de l'état et du libellé du bouton précédent
		if (pageNumber==0)
		{
			previousButton.setEnabled(false);		
			
		}
		else	
		{
			previousButton.setEnabled(true);
		}
		
	}

	private void handleNext()
	{
		boolean ret = doCommit();
		if (ret==false)
		{
			return;
		}
		
		// On verifie d'abord les champs 1 par 1
		List<String> msg = validatorManager.validate();
		if (msg.size()>0)
		{
			msg.add(0, "Merci de corriger les points suivants :");
			MessagePopup.open(new MessagePopup("Notification", msg));
			return;
		}
		
		// On fait ensuite la verification globale 
		DetailStepInfo stepInfo = pages.get(pageNumber);
		if (stepInfo.check!=null)
		{
			String verifGlobal = stepInfo.check.action();
			if (verifGlobal!=null)
			{
				MessagePopup.open(new MessagePopup("Notification", ContentMode.HTML,ColorStyle.RED,verifGlobal));
				return;
			}
		}
		
		
		// Soit on sauvegarde
		if (pageNumber==pages.size()-1)
		{
			handleSauvegarder();
		}
		// Soit on passe à la page suivante 
		else
		{
			pageNumber++;
			updateButtonStatus();
			updateForm();
		}
	}
	

	private void handleSauvegarder()
	{
		try
		{
			// Sauvegarde 
			performSauvegarder();
		}
		catch(OnSaveException e)
		{
			List<String> msgs = new ArrayList<String>();
			msgs.add("Une erreur est survenue durant la sauvegarde.");
			msgs.addAll(e.getAllMessages());
			MessagePopup.open(new MessagePopup("Erreur", msgs));
			return ;
		}
		catch(Exception e)
		{
			ErrorPopup.open(e);
			return;
		}

		close();
	}	
	
	/**
	 * Dans ce mode, on ne que revenir à la page précédente
	 */
	protected void setBackOnlyMode()
	{
		nextButton.setEnabled(false);
	}
	
	
	/**
	 * Retourne null si tout est ok, sinon retourne une liste de messages d'erreur
	 * @return
	 */ 
	abstract protected void  performSauvegarder() throws OnSaveException;
	

	/**
	 * Permet de déclarer le contenu des écrans 
	 */
	abstract protected void configure();
	
	static public class DetailStepInfo
	{
		public GenericUtils.VoidAction drawScreen;
		public GenericUtils.StringAction check;
	}
	
	
	/**
	 * 
	 */
	protected void add(GenericUtils.VoidAction drawScreen)
	{
		DetailStepInfo detail = new DetailStepInfo();
		detail.drawScreen = drawScreen;
		
		pages.add(detail);
	}
	
	
	protected void add(boolean actif,GenericUtils.VoidAction drawScreen)
	{
		if (actif)
		{
			add(drawScreen);
		}
	}
	
	
	
	/**
	 * 
	 */
	public void add(GenericUtils.VoidAction drawScreen,GenericUtils.StringAction check)
	{
		DetailStepInfo detail = new DetailStepInfo();
		detail.drawScreen = drawScreen;
		detail.check = check;
		
		pages.add(detail);
	}
	
	public void add(boolean actif,GenericUtils.VoidAction drawScreen,GenericUtils.StringAction check)
	{
		if (actif)
		{
			add(drawScreen,check);
		}
	}
	
	
	
	protected void setStepTitle(String message)
	{
		hTitre.setValue("Etape "+(pageNumber+1)+" : "+message);
	}
	
	protected void setStepTitleNoPrefix(String message)
	{
		hTitre.setValue(message);
	}
	
	
	/**
	 * Permet de changer le bouton "Etape suivante" en "Sauvegarder" 
	 */
	protected void setNextButtonAsSave()
	{
		nextButton.setCaption(saveButtonTitle);
	}
	
	
	/**
	 * Permet de changer tous les boutons du bas en un seul bouton OK 
	 */
	protected void setAllButtonsAsOK()
	{
		nextButton.setCaption("OK");
		previousButton.setVisible(false);
		cancelButton.setVisible(false);
	}
	
	/**
	 * Mets tous les elements de la forme dans l'état indique 
	 */
	protected void setReadOnlyAll()
	{
		BaseUiTools.setReadOnlyRecursively(form);
	}
	
}
