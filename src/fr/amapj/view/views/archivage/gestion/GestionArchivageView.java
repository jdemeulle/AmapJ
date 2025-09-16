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
 package fr.amapj.view.views.archivage.gestion;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.service.services.parametres.ParametresArchivageDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.popup.PopupListener;
import fr.amapj.view.engine.template.BackOfficeLongView;

/**
 * Ecran de gestion de l'archivage
 *
 */
public class GestionArchivageView extends BackOfficeLongView implements PopupListener
{

	@Override
	public String getMainStyleName()
	{
		return "import-donnees";
	}

	@Override
	public void enterIn(ViewChangeEvent event)
	{
		refresh();
	}
	
	public void refresh()
	{
		removeAllComponents();
		
		ParametresArchivageDTO dto = new ParametresService().getParametresArchivage();
		
		addLabelH1(this, "Gestion des archives");
		
		addLabel(this, "Cet outil vous permet de transférer vers les archives les contrats terminés, les utilisateurs qui n'ont pas ré adherés et les producteurs qui ne livrent plus<br/>");
		
		addLabel(this, "Les éléments archivés sont toujours accessibles (dans le menu ARCHIVES) et il est possible de les remettre à l'état actif");
		
		addLabel(this, "Cet outil vous permet également de supprimer les anciennes périodes de cotisation et de permanence, et aussi de nettoyer les archives (suppression définitive des éléments).");

		addLabel(this, "<b>Pour utiliser cet outil , le plus simple est de cliquer sur les boutons dans l'ordre (de haut en bas)</b>");
		
		addLabel(this, "Vous cliquez d'abord sur \"Vérifier / Modifier les paramètres d'archivage\" pour vérifier votre paramétrage, puis vous cliquez sur \"Archiver les contrats terminés\" puis ...");
		
		addLabel(this, "Pour chaque action, l'outil vous présentera ce qu'il va faire et vous demandera confirmation avat d'agir.");
		
		Button b = addButton(this, "Vérifier / Modifier les paramètres d'archivage", e->new PopupSaisieParametresArchivage().open(this));
		b.setWidth("60%");
		
		
		Panel p1 = new Panel("Transfert vers les archives");
		p1.addStyleName("action");
		p1.setContent(getTransfertPanel(dto));
		p1.setWidth("60%");
		
		Panel p2 = new Panel("Suppression directe");
		p2.addStyleName("action");
		p2.setContent(getSuppressionDirectePanel(dto));
		p2.setWidth("60%");
		
		Panel p3 = new Panel("Nettoyage des archives");
		p3.addStyleName("action");
		p3.setContent(getSuppressionPanel(dto));
		p3.setWidth("60%");
		
		
		addEmptyLine(this);
		addComponent(p1);

		addEmptyLine(this);
		addComponent(p3);
	
		addEmptyLine(this);
		addComponent(p2);

	
	}

	private Component getTransfertPanel(ParametresArchivageDTO dto)
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		
		addButton(layout,"Archiver les contrats terminés depuis "+dto.archivageContrat+" jours",e->new PopupArchivageContrat().open());

		addButton(layout,"Archiver les utilisateurs qui n'ont pas ré adhérés",e->new PopupArchivageUtilisateur().open());
		
		addButton(layout,"Archiver les producteurs qui ne livrent plus",e->new PopupArchivageProducteur().open());
		
		
		return layout;
	}
	
	
	private Component getSuppressionDirectePanel(ParametresArchivageDTO dto)
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
	
		addButton(layout,"Supprimer les périodes de cotisations terminées depuis "+dto.suppressionPeriodeCotisation+" jours",e->new PopupSuppressionPeriodeCotisation().open());
	
		addButton(layout,"Supprimer les périodes de permanence terminées depuis "+dto.suppressionPeriodePermanence+" jours",e->new PopupSuppressionPeriodePermanence().open());
		return layout;
	}
	

	

	private Component getSuppressionPanel(ParametresArchivageDTO dto)
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		
		addButton(layout,"Supprimer les contrats archivés et terminés depuis "+dto.suppressionContrat+" jours",e->new PopupSuppressionContrat().open());
		
		addButton(layout,"Supprimer les utilisateurs archivés trop anciens",e->new PopupSuppressionUtilisateur().open());
		
		addButton(layout,"Supprimer les producteurs archivés trop anciens",e->new PopupSuppressionProducteur().open());
	
		return layout;
	}

	// Partie technique 
	
	private Button addButton(VerticalLayout layout, String caption, ClickListener listener) 
	{
		Button b = new Button(caption);
		b.setWidth("100%");
		b.addClickListener(listener);
		layout.addComponent(b);
		return b;
	}
	

	private Label addLabelH1(VerticalLayout layout, String str)
	{
		Label tf = new Label(str);
		tf.addStyleName("titre");
		layout.addComponent(tf);
		return tf;

	}

	private Label addLabel(VerticalLayout layout, String str)
	{
		Label tf = new Label(str,ContentMode.HTML);
		layout.addComponent(tf);
		return tf;

	}
	
	private Label addEmptyLine(VerticalLayout layout)
	{
		Label tf = new Label("<br/>",ContentMode.HTML);
		layout.addComponent(tf);
		return tf;

	}
	
	@Override
	public void onPopupClose() 
	{
		refresh();
	}

	
}
