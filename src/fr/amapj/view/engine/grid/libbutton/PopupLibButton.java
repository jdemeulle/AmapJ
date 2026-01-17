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
 package fr.amapj.view.engine.grid.libbutton;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.common.FormatUtils;
import fr.amapj.view.engine.notification.NotificationHelper;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.okcancelpopup.OKCancelMessagePopup;
import fr.amapj.view.engine.tools.BaseUiTools;

/**
 * Popup pour la saisie des quantites , en vue liste 
 *  
 */
abstract public class PopupLibButton<T> extends CorePopup
{

	private Map<LibButtonLine<T>,Panel> panels = new HashMap<>();
	
	private Label prixTotal;

	protected LibButtonParam<T> param = new LibButtonParam<T>();
	
	private Label labelMessageSpecifiqueBottom;

	abstract public void loadParam();
	
	abstract protected void handleButton(LibButtonLine<T> line);
	
	private boolean isCompactMode;

	/**
	 * Retourne true si il faut fermer le popup
	 */
	abstract public boolean performSauvegarder();

	protected void createContent(VerticalLayout mainLayout)
	{
		setType(PopupType.FILL);
		setWidth(60, 960);
		setHeight("100%");
		loadParam();
		
		isCompactMode = BaseUiTools.isCompactMode();

		if (param.messageSpecifique != null && param.messageSpecifique.length()>0)
		{
			Label messageSpeLabel = new Label(param.messageSpecifique,ContentMode.HTML);
			messageSpeLabel.addStyleName("popup-integer-grid-message");
			mainLayout.addComponent(messageSpeLabel);
		}
		
		if (param.switchButtonAction!=null)
		{
			Button btn = new Button(FontAwesome.ROTATE_LEFT);
			btn.addStyleName("leftpart");  
			btn.addStyleName("borderless-colored");
			btn.addStyleName("question-mark");
			btn.addClickListener(e->param.switchButtonAction.run());
			mainLayout.addComponent(btn);
			mainLayout.setComponentAlignment(btn, Alignment.TOP_RIGHT);
			addStyleNameForMainLayout("no-padding-top");
		}
		
		// Construction des lignes
		for (int i = 0; i < param.nbLig; i++)
		{
			LibButtonLine<T> line = param.lines.get(i);
			
			if (line.isVisible)
			{
				mainLayout.addComponent(createLine(line));
			}
		}

		// Footer 0 pour avoir un espace
		mainLayout.addComponent(new Label("<br/>",ContentMode.HTML));

		// Footer 1 avec le prix total
		if (param.hasLignePrixTotal)
		{
			prixTotal = new Label("",ContentMode.HTML);
			displayMontantTotal();	
			mainLayout.addComponent(prixTotal);
		}
		
		// Message spécifique en bas de popup
		if (param.messageSpecifiqueBottom != null && param.messageSpecifiqueBottom.length()>0)
		{
			labelMessageSpecifiqueBottom = new Label(param.messageSpecifiqueBottom,ContentMode.HTML);
			labelMessageSpecifiqueBottom.addStyleName("popup-integer-grid-message");
			mainLayout.addComponent(labelMessageSpecifiqueBottom);
		}

	}

	private Component createLine(LibButtonLine<T> line) 
	{
		Panel p = new Panel();
		fillPanel(p, line);
		panels.put(line, p);
		return p;
	}
	
	
	protected void refreshLine(LibButtonLine<T> line)
	{
		Panel p = panels.get(line);
		fillPanel(p,line);
	}
	
	private void fillPanel(Panel p, LibButtonLine<T> line)
	{
		if (isCompactMode)
		{
			p.addStyleName("popup-lib-button-panel-compactmode");
			fillPanelCompactMode(p,line);
		}
		else
		{
			p.addStyleName("popup-lib-button-panel");
			fillPanelStdMode(p,line);
		}
	}
	
	/**
	 * Dans le mode standard, date à gauche, au milieu le contenu du panier et à droite le bouton Modifier
	 */
	private void fillPanelStdMode(Panel p, LibButtonLine<T> line)
	{	
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setMargin(true);

		// La date sous le calendrier
		Label tf1 = new Label(line.lib1,ContentMode.HTML);
		tf1.addStyleName("popup-lib-button-lib1");
		tf1.setIcon(FontAwesome.CALENDAR_O);
		tf1.setWidth("80px");
		hl.addComponent(tf1);
		hl.setComponentAlignment(tf1, Alignment.MIDDLE_CENTER);
		
		
		// Libellé 2
		Label tf2 = new Label(line.lib2.lib,ContentMode.HTML);
		tf2.addStyleName(line.lib2.styleName);
		hl.addComponent(tf2);
		hl.setExpandRatio(tf2, 1f);
		hl.setComponentAlignment(tf2, Alignment.MIDDLE_CENTER);

		// Le button modifier
		if (line.hasButton)
		{
			Button b = new Button(line.buttonLib);
			b.addClickListener(e->handleButton(line));
			hl.addComponent(b);
			hl.setComponentAlignment(b, Alignment.MIDDLE_CENTER);
		}
		
		
		p.setContent(hl);
	}

	/**
	 * Dans le mode compact, à gauche la date et le bouton modifier dessous , puis au milieu le contenu du panier 
	 */
	private void fillPanelCompactMode(Panel p, LibButtonLine<T> line)
	{	
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setMargin(true);
		hl.addStyleName("popup-lib-button-compactmode");
				
		VerticalLayout vl = new VerticalLayout();
		vl.setSizeUndefined();

		// La date et le bouton modifier 
		Label tf1 = new Label(line.lib1,ContentMode.HTML);
		tf1.addStyleName("popup-lib-button-lib1-compactmode");
		tf1.setIcon(FontAwesome.CALENDAR_O);
		tf1.setWidth("80px");
		vl.addComponent(tf1);
		
		// Le button modifier
		if (line.hasButton)
		{
			Button b = new Button(line.buttonLib);
			b.addClickListener(e->handleButton(line));
			vl.addComponent(b);
			vl.setComponentAlignment(b, Alignment.MIDDLE_CENTER);
		}
		
		hl.addComponent(vl);
		
		// Libellé 2
		Label tf2 = new Label(line.lib2.lib,ContentMode.HTML);
		tf2.addStyleName(line.lib2.styleName);
		hl.addComponent(tf2);
		hl.setExpandRatio(tf2, 1f);
		hl.setComponentAlignment(tf2, Alignment.MIDDLE_CENTER);

		p.setContent(hl);
	}
	
	
	
	protected void createButtonBar()
	{
		if (param.readOnly)
		{
			addButtonBlank();
			addDefaultButton(param.libButtonSave, e->handleContinuer());
		}
		else
		{
			if ((param.copyFirstLineAction!=null) && (param.getNbLineVisible() > 1))
			{
				addButtonCopyFirstLine();
			}

			addButtonBlank();
			
			addButton("Annuler", e->handleAnnuler());
			
			Button saveButton = addDefaultButton(param.libButtonSave, e->handleSauvegarder());
			saveButton.addStyleName("primary");
			
		}
	}


	private void addButtonCopyFirstLine() 
	{
		if (BaseUiTools.isCompactMode())
		{
			addButton("Copier...", e->handleCopyWithConfirm());
		}
		else
		{
			addButton("Copier la 1ère ligne partout", e->param.copyFirstLineAction.run());
		}
	}

	private void handleCopyWithConfirm() 
	{
		CorePopup popup = new OKCancelMessagePopup("Copier ...","Êtes-vous sûr de vouloir copier le contenu du panier de la première date sur toutes les dates ? ", ()->param.copyFirstLineAction.run());
		popup.open();
	}

	protected void displayMontantTotal()
	{
		if (prixTotal!=null)
		{
			prixTotal.setValue("<b>Montant total : " +FormatUtils.prix(param.getMontantTotal())+"</b>");	
		}
	}

	
	protected void handleAnnuler()
	{
		close();
	}
	
	/**
	 * Cette méthode est appelée quand l'utilisateur clique sur "Continuer" et que l'on est en read Only 
	 */
	protected void handleContinuer()
	{
		close();
	}
	
	/**
	 * Cette méthode est appelée quand l'utilisateur clique sur "Continuer" et que l'on N'est PAS en read Only 
	 */	
	protected void handleSauvegarder()
	{
		if ((param.allowedEmpty==false) && (param.isEmpty()==true))
		{
			NotificationHelper.displayNotification("Vous devez saisir une quantité avant de continuer");
			return;
		}

		boolean ret = performSauvegarder();
		if (ret==true)
		{
			close();
		}
	}
}
