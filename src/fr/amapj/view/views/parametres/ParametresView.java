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
 package fr.amapj.view.views.parametres;

import java.util.function.Supplier;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.template.BackOfficeLongView;
import fr.amapj.view.views.parametres.paramecran.PEContratsAmapienEditorPart;
import fr.amapj.view.views.parametres.paramecran.PEGestionContratsSignesEditorPart;
import fr.amapj.view.views.parametres.paramecran.PEGestionContratsViergesEditorPart;
import fr.amapj.view.views.parametres.paramecran.PEListeAdherentEditorPart;
import fr.amapj.view.views.parametres.paramecran.PELivraisonAmapienEditorPart;
import fr.amapj.view.views.parametres.paramecran.PEMesAdhesionsEditorPart;
import fr.amapj.view.views.parametres.paramecran.PEMesContratsEditorPart;
import fr.amapj.view.views.parametres.paramecran.PEMesLivraisonsEditorPart;
import fr.amapj.view.views.parametres.paramecran.PEProducteurReferentEditorPart;
import fr.amapj.view.views.parametres.paramecran.PEReceptionChequeEditorPart;
import fr.amapj.view.views.parametres.paramecran.PERemiseProducteurEditorPart;
import fr.amapj.view.views.parametres.paramecran.PESyntheseMultiContratEditorPart;
import fr.amapj.view.views.parametres.paramecran.PEVisiteAmapEditorPart;
import fr.amapj.view.views.parametres.paramecran.producteur.PEContratProducteurEditorPart;
import fr.amapj.view.views.parametres.paramecran.producteur.PELivraisonProducteurEditorPart;


/**
 * Page permettant à l'administrateur de modifier les paramètres généraux
 */
public class ParametresView extends BackOfficeLongView
{

	ParametresDTO dto;
	
	TextField nomAmap;
	TextField villeAmap;
	
	
	@Override
	public String getMainStyleName()
	{
		return "parametres";
	}

	/**
	 * 
	 */
	@Override
	public void enterIn(ViewChangeEvent event)
	{		
		// Bloc identifiants
		FormLayout form1 = new FormLayout();
        form1.setMargin(false);
        form1.addStyleName("light");
        addComponent(form1);
        
        
        Label section = new Label("Paramètres de l'AMAP");
        section.addStyleName("h2");
        section.addStyleName("colored");
        form1.addComponent(section);
		
		nomAmap = addTextField("Nom de l'AMAP ",form1);
		villeAmap = addTextField("Ville de l'AMAP ",form1);
		

		
		addButton("Changer les paramètres généraux",()->new PopupSaisieParametres(dto));
		
		addButton("Activer / Désactiver les modules optionnels",()->new PopupModulesOptionnels(dto));
		
		addLabel("Amapien");
		
		addButton("Écran \"Mes contrats\"",() -> new PEMesContratsEditorPart());

		addButton("Écran \"Mes livraisons\"",() -> new PEMesLivraisonsEditorPart());
		
		addButton("Écran \"Mes adhésions\"",() -> new PEMesAdhesionsEditorPart());
		
		addButton("Écran \"Découverte / Visite\"",() -> new PEVisiteAmapEditorPart());
		
		addButton("Écran \"Producteurs / Référents\"",() -> new PEProducteurReferentEditorPart());
		
		addButton("Écran \"Liste des adhérents\"",() -> new PEListeAdherentEditorPart());
		
		addLabel("Producteur");
		
		addButton("Écran \"Livraison d'un producteur\"",() -> new PELivraisonProducteurEditorPart());
		
		addButton("Écran \"Contrats d'un producteur\"",() -> new PEContratProducteurEditorPart());
		
		addLabel("Référent");
		
		addButton("Écran \"Gestion des contrats vierges\"",() -> new PEGestionContratsViergesEditorPart());
		
		addButton("Écran \"Gestion des contrats signés\"",() -> new PEGestionContratsSignesEditorPart());
		
		addButton("Écran \"Réception des chèques\"",() -> new PEReceptionChequeEditorPart());
		
		addButton("Écran \"Remise aux producteurs\"",() -> new PERemiseProducteurEditorPart());
		
		addButton("Écran \"Contrats d'un amapien\"",() -> new PEContratsAmapienEditorPart());
		
		addButton("Écran \"Livraison d'un amapien\"",() -> new PELivraisonAmapienEditorPart());

		addButton("Écran \"Synthèse multi contrats\"",() -> new PESyntheseMultiContratEditorPart());
		
		
		addLabel("Autres");
		
		// POUR EXEMPLE DE PAGE POUR SAISIR DES DONNEES DE TYPE PARAMETRES
		// SANS LIEN DIRECT AVEC UN MENU 
		// PAR EXEMPLE : SAISIR LA LISTE DES TYPES D'UTILISATEUR, TYPE DE PAIEMENT, ... 
		// addButtonNavigate("Saisie des types de paiements globaux",TypPaiementMultiContratListPart.class);
		
		
		
		refresh();
		
	}


	private void refresh()
	{
		dto = new ParametresService().getParametres();
		
		setValue(nomAmap,dto.nomAmap);
		setValue(villeAmap,dto.villeAmap);		
	}
	
	
	
	// TOOLS

	
	private void addButton(String str,Supplier<CorePopup> popupSupplier)
	{
		Button newButton = new Button(str);
		newButton.addClickListener(e->popupSupplier.get().open(()->refresh()));
		addComponent(newButton);
	}
	

	private void setValue(TextField tf, String val)
	{
		tf.setReadOnly(false);
		tf.setValue(val);
		tf.setReadOnly(true);
	}
	
	
	private TextField addTextField(String lib,FormLayout form)
	{
		TextField name = new TextField(lib);
		name.setWidth("100%");
		name.setNullRepresentation("");
		name.setReadOnly(true);
		form.addComponent(name);

		return name;
	}
	
	private Label addLabel(String str)
	{
		Label tf = new Label(str,ContentMode.HTML);
		addComponent(tf);
		return tf;
	}
	
	/**
	 * A CONSERVER, VOIR PLUS HAUT 
	 * @param str
	 * @param viewClass
	 */
	private void addButtonNavigate(String str, Class viewClass)
	{
		Button newButton = new Button(str);
		newButton.addClickListener(e->handleButtonNavigate(viewClass));
		addComponent(newButton);
	}

	private void handleButtonNavigate(Class viewClass)
	{
		Navigator nav = UI.getCurrent().getNavigator();
		String name = "/"+Math.abs(viewClass.getName().hashCode());
		nav.addView(name, viewClass);
		nav.navigateTo(name);
	}

}
