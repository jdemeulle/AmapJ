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
 package fr.amapj.view.engine.grid.currencyvector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.server.Page;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.view.engine.grid.ErreurSaisieException;
import fr.amapj.view.engine.grid.GridHeaderLine;
import fr.amapj.view.engine.grid.GridHeaderLine.GridHeaderLineCell;
import fr.amapj.view.engine.grid.GridIJData;
import fr.amapj.view.engine.grid.ShortCutManager;
import fr.amapj.view.engine.grid.ShortCutManager.ShortCutLine;
import fr.amapj.view.engine.grid.currencyvector.CurrencyVectorParam.CurrencyLine;
import fr.amapj.view.engine.grid.currencyvector.CurrencyVectorParam.CurrencyLineStatus;
import fr.amapj.view.engine.notification.NotificationHelper;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.tools.BaseUiTools;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;

/**
 * Popup pour la saisie des montants des chéques 
 * 
 * A bien noter : on peut être en Mode read Only et avec le bouton Save
 * cela correspond au cas ou l'utilisateur peut uniquement accepter la proposition qui est faite  
 * 
 *  
 */
abstract public class PopupCurrencyVector extends CorePopup
{
	private SimpleDateFormat df = new SimpleDateFormat("d MMMMM yyyy");
	
	private Table table;

	protected CurrencyVectorParam param = new CurrencyVectorParam();

	private ShortCutManager shortCutManager;
	
	private Label montantTotalPaiement;
	
	abstract public void loadParam();

	abstract public void performSauvegarder() throws OnSaveException;

	protected void createContent(VerticalLayout mainLayout)
	{			
		//
		setType(PopupType.CENTERFIT);
		loadParam();
		
		//On verifie la coherence des données
		param.checkParam();


		if (param.messageSpecifique != null)
		{
			Label messageSpeLabel = new Label(param.messageSpecifique);
			messageSpeLabel.addStyleName("popup-currency-vector-message");
			mainLayout.addComponent(messageSpeLabel);
		}
		
		
		if (param.messageSpecifique2 != null)
		{
			Label messageSpeLabel = new Label(param.messageSpecifique2,ContentMode.HTML);
			messageSpeLabel.addStyleName("popup-currency-vector-message");
			mainLayout.addComponent(messageSpeLabel);
		}
		
		if (param.messageSpecifique3 != null)
		{
			Label messageSpeLabel = new Label(param.messageSpecifique3,ContentMode.HTML);
			messageSpeLabel.addStyleName("popup-currency-vector-message");
			mainLayout.addComponent(messageSpeLabel);
		}
		
		if (param.avoirInitial!=0)
		{
			// Footer 1 avec le montant total des paiements
			HorizontalLayout footer1 = new HorizontalLayout();
			footer1.setWidth("350px");
			fillFooter(footer1,"Avoir initial",param.avoirInitial);
			
			// Footer 2 pour avoir un espace
			HorizontalLayout footer2 = new HorizontalLayout();
			footer2.setWidth("200px");
			footer2.setHeight("20px");

			mainLayout.addComponent(footer1);
			mainLayout.addComponent(footer2);
		}
		
		

		// Construction des headers
		for (GridHeaderLine line : param.headerLines)
		{
			constructHeaderLine(mainLayout, line);
		}

		// Construction de la table de saisie
		table = new Table();
		table.addStyleName("no-vertical-lines");
		table.addStyleName("no-horizontal-lines");
		table.addStyleName("no-stripes");

		table.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);

		// Colonne de gauche contenant un libellé
		table.addContainerProperty(new Integer(-1), Label.class, null);
		table.setColumnWidth(new Integer(-1), param.largeurCol);

		// colonne du milieu correspondant à la saisie des quantites
		Class<?> clzz = param.isReadOnly() ? Label.class : TextField.class;
		table.addContainerProperty(new Integer(0), clzz, null);
		table.setColumnWidth(new Integer(0), param.largeurCol);
		
		// Colonne de droite contenant un libellé
		if (param.hasColComment)
		{
			table.addContainerProperty(new Integer(1), Label.class, null);
			table.setColumnWidth(new Integer(1), param.largeurCol);
		}
		
		//
		if (param.isReadOnly()==false)
		{
			shortCutManager = new ShortCutManager(computeShortCutManager());
			shortCutManager.addShorcut(this.getWindow());
		}

		// Creation de toutes les cellules pour la saisie
		for (int i = 0; i < param.getNbLines(); i++)
		{
			addRow(i);
		}

		
		table.setEditable(!param.isReadOnly());
		table.setSelectable(true);
		table.setSortEnabled(false);
		table.setPageLength(getPageLength());

		// Footer 0 pour avoir un espace
		HorizontalLayout footer0 = new HorizontalLayout();
		footer0.setWidth("200px");
		footer0.setHeight("20px");

		// Construction globale
		mainLayout.addComponent(table);
		mainLayout.addComponent(footer0);
		
		
		if (param.hasAdjust()==false)
		{
			// Footer 1 avec le montant total des paiements
			HorizontalLayout footer1 = new HorizontalLayout();
			footer1.setWidth("350px");
			montantTotalPaiement = fillFooter(footer1,"Montant total paiements",param.getMontantTotalPaiement());
			
			// Footer 2 pour avoir un espace
			HorizontalLayout footer2 = new HorizontalLayout();
			footer2.setWidth("200px");
			footer2.setHeight("20px");
			
			// Footer 3 avec le prix total du contrat
			HorizontalLayout footer3 = new HorizontalLayout();
			footer3.setWidth("350px");
			fillFooter(footer3,"Montant total dû",param.montantCible);


			mainLayout.addComponent(footer1);
			mainLayout.addComponent(footer2);
			mainLayout.addComponent(footer3);
		}
		else
		{
			// Footer 1 avec le prix total
			HorizontalLayout footer1 = new HorizontalLayout();
			footer1.setWidth("350px");
			fillFooter(footer1,"Montant total à régler",param.montantCible);
			mainLayout.addComponent(footer1);
		}
	}

	private List<ShortCutLine> computeShortCutManager()
	{
		List<ShortCutLine> res = new ArrayList<>();
		for (int i = 0; i < param.getNbLines(); i++)
		{
			
			CurrencyLine currencyLine = param.lines.get(i);
			
			ShortCutLine line = new ShortCutLine();
			line.isVisible = true;
			line.editable = new boolean[1];
			line.editable[0] = currencyLine.status==CurrencyLineStatus.EDITABLE;
			
			res.add(line);
		}
		return res;
	}
	

	private Label fillFooter(HorizontalLayout footer1, String message, int montantCible)
	{
		Label dateLabel = new Label(message);
		dateLabel.addStyleName("prix");
		dateLabel.setSizeFull();
		footer1.addComponent(dateLabel);
		footer1.setExpandRatio(dateLabel, 1.0f);

		Label prixTotal = new Label(new CurrencyTextFieldConverter().convertToString(montantCible));
		prixTotal.addStyleName("prix");
		prixTotal.setSizeFull();
		footer1.addComponent(prixTotal);
		footer1.setExpandRatio(prixTotal, 1.0f);
		
		return prixTotal;
	}

	protected void createButtonBar()
	{
		if (param.hasSaveButton==false)
		{
			addButtonBlank();
			Button ok = addDefaultButton("OK", e->handleAnnuler());
			ok.addStyleName("primary");
		}
		else
		{
			if (param.getNbEditable() > 1)
			{
				addButton("Copier la 1ère ligne partout", e->handleCopier());
			}
			
			addButtonBlank();
			
			addButton("Annuler", e->handleAnnuler());

			String lib = param.libSaveButton !=null ? param.libSaveButton : "Sauvegarder"; 
			Button saveButton = addDefaultButton(lib, e->	handleSauvegarder());
			saveButton.addStyleName("primary");
		}

	}

	protected void handleCopier()
	{
		try
		{
			doHandleCopier();
		}
		catch (ErreurSaisieException e)
		{
			NotificationHelper.displayNotification("Erreur de saisie sur la premiere ligne - Impossible de copier");
		}
	}

	private void doHandleCopier() throws ErreurSaisieException
	{ 
		// Copie de cette valeur dans toutes les cases en dessous
		int qteRef=0;
		boolean found = false;
		
		
		for (int i = 0; i < param.lines.size(); i++) 
		{
			CurrencyLine line = param.lines.get(i);
			if (line.status==CurrencyLineStatus.EDITABLE)
			{
				if (found==false)
				{
					found = true;	
					qteRef = readValueInCell(i);
				}
				else
				{
					Item item = table.getItem(new Integer(i));
					TextField tf = (TextField) item.getItemProperty(new Integer(0)).getValue();
					tf.setConvertedValue(qteRef);
				}
			}
		}	
	}

	private void constructHeaderLine(VerticalLayout mainLayout, GridHeaderLine line)
	{
		HorizontalLayout header1 = new HorizontalLayout();
		header1.setWidth(getLargeurTotal());
		if (line.height != -1)
		{
			header1.setHeight(line.height + "px");
		}

		for (GridHeaderLineCell cell : line.cells)
		{
			Label dateLabel = new Label(cell.content);
			dateLabel.addStyleName("tete");
			header1.addComponent(dateLabel);
			dateLabel.setSizeFull();
			header1.setExpandRatio(dateLabel, 1.0f);
		}
		mainLayout.addComponent(header1);
	}

	private int getPageLength()
	{
		Page page = UI.getCurrent().getPage();
		int pageLength = 15;
		
		// On limite le nombre de ligne pour ne pas avoir une double scroolbar
		
		// Une ligne fait 32 en mode edition , sinon 26
		int lineHeight = param.isReadOnly() ? 26 : 32;   	
		
		// On cacule la place cosommée par les headers, boutons, ...
		// 365 : nombre de pixel mesurée pour les haeders, les boutons, ... en mode normal, 270 en mode compact
		int headerAndButtonHeight = BaseUiTools.isCompactMode() ? 270 : 365;
		
		
		int maxLineAvailable = (page.getBrowserWindowHeight()-headerAndButtonHeight)/lineHeight;
		
		// Il y a au moins 4 lignes visibles
		maxLineAvailable = Math.max(maxLineAvailable, 4);  						
		pageLength = Math.min(pageLength,maxLineAvailable);

		// Pour ie 8 et inférieur : on se limite a 6 lignes, sinon ca rame trop
		WebBrowser webBrowser = UI.getCurrent().getPage().getWebBrowser();
		if (webBrowser.isIE() && webBrowser.getBrowserMajorVersion() < 9)
		{
			pageLength = Math.min(pageLength,6);
		}

		//
		pageLength = Math.min(pageLength, param.getNbLines());
		return pageLength;
		
	}

	/**
	 * Calcul de la largeur totale de la table
	 * @return
	 */
	private String getLargeurTotal()
	{
		int nbCol=2;
		if (param.hasColComment)
		{
			nbCol++;
		}
		
		return nbCol * (param.largeurCol + param.espaceInterCol) + "px";
	}

	

	private void addRow(int lig)
	{
		CurrencyLine currencyLine = param.lines.get(lig);
		List<Object> cells = new ArrayList<Object>();

		// Ajout de la colonne de gauche 
		Label dateLabel = new Label(df.format(currencyLine.datePaiement));
		dateLabel.addStyleName("date-saisie");
		dateLabel.setWidth(param.largeurCol + "px");
		cells.add(dateLabel);

		// Ajout de la colonne montant
		int qte = currencyLine.montant;
		if (param.isReadOnly())
		{
			//
			String txt;
			if (currencyLine.status==CurrencyLineStatus.TIRETS)
			{
				txt = "----";
			}
			else if (currencyLine.status==CurrencyLineStatus.READ_ONLY || currencyLine.status==CurrencyLineStatus.ADJUST)
			{
				txt = (qte == 0) ? "" : new CurrencyTextFieldConverter().convertToString(qte); 
			}
			else
			{
				throw new AmapjRuntimeException();
			}
			
			Label tf = new Label(txt);
			tf.addStyleName("cell-voir");
			tf.setWidth((param.largeurCol - 10) + "px");
			cells.add(tf);
		}
		else
		{
			if (currencyLine.status==CurrencyLineStatus.TIRETS)
			{
				TextField tf = new TextField();
				tf.setValue("----");
				tf.setEnabled(false);
				tf.addStyleName("cell-voir");
				tf.setWidth((param.largeurCol - 10) + "px");
				cells.add(tf);
			}
			else if (currencyLine.status==CurrencyLineStatus.READ_ONLY)
			{
				TextField tf = BaseUiTools.createCurrencyField("",true);
				tf.setConvertedValue(new Integer(qte));
				tf.setEnabled(false);
				tf.addStyleName("cell-voir");
				tf.setWidth((param.largeurCol - 10) + "px");
				cells.add(tf);
			}
			else if (currencyLine.status==CurrencyLineStatus.EDITABLE)
			{
				TextField tf = BaseUiTools.createCurrencyField("",false);
				tf.setData(new GridIJData(lig, 0));
				tf.setConvertedValue(new Integer(qte));
				tf.addValueChangeListener(e->handleUpdateModele());
				tf.addStyleName("cell-saisie");
				tf.setWidth((param.largeurCol - 10) + "px");
				shortCutManager.registerTextField(tf);
				cells.add(tf);
			}
			else if (currencyLine.status==CurrencyLineStatus.ADJUST)
			{
				// Les nombres negatifs sont autorisés pour la ligne d'ajustement 
				TextField tf = BaseUiTools.createCurrencyField("",true);
				tf.setData(new GridIJData(lig, 0));
				tf.setConvertedValue(new Integer(qte));
				tf.addValueChangeListener(e->handleUpdateModele());
				tf.addStyleName("cell-saisie");
				tf.setWidth((param.largeurCol - 10) + "px");
				shortCutManager.registerTextField(tf);
				cells.add(tf);
				
				//
				tf.setEnabled(false);
			}
			else
			{
				throw new AmapjRuntimeException();
			}
			
		}
		
		// Ajout de la colonne de droite si besoin 
		if (param.hasColComment)
		{
			Label rightLabel = new Label(currencyLine.rightPart);
			rightLabel.addStyleName("date-saisie");
			rightLabel.setWidth(param.largeurCol + "px");
			cells.add(rightLabel);
		}

		table.addItem(cells.toArray(), new Integer(lig));

	}


	private void handleUpdateModele() 
	{
		try
		{
			updateModele();
		}
		catch (ErreurSaisieException e)
		{
			NotificationHelper.displayNotificationMontant();
		}
	}
	
	
	/**
	 *  
	 */
	private TextField getTextField(int lineIndex) throws ErreurSaisieException
	{
		Item item = table.getItem(new Integer(lineIndex));
		TextField tf = (TextField) item.getItemProperty(new Integer(0)).getValue();
		return tf;
	}
	
	
	
	/**
	 *  Retourne la valeur dans la cellule à la ligne lineIndex sous la forme d'un entier
	 * jette une exception si il y a une erreur
	 */
	private int readValueInCell(int lineIndex) throws ErreurSaisieException
	{
		TextField tf = getTextField(lineIndex);
	
		try
		{
			Integer val = (Integer) tf.getConvertedValue();
			int qte = 0;
			if (val != null)
			{
				qte = val.intValue();
			}
			return qte;
		}
		catch (ConversionException e)
		{
			throw new ErreurSaisieException();
		}
	}

	protected void handleAnnuler()
	{
		close();
	}

	protected void handleSauvegarder()
	{
		try
		{
			if (param.isReadOnly()==false)
			{
				updateModele();
			}
		}
		catch (ErreurSaisieException e)
		{
			NotificationHelper.displayNotificationMontant();
			return;
		}

		
		try
		{
			performSauvegarder();
		} 
		catch (OnSaveException e)
		{
			e.showInNewDialogBox();
			return;
		}
		
		close();
	}

	/**
	 * Lecture de la table pour mettre à jour le modele
	 */
	private void updateModele() throws ErreurSaisieException
	{
		// On lit les lignes EDITABLES
		int cumul = param.avoirInitial;
		for (CurrencyLine line : param.lines) 
		{
			if (line.status==CurrencyLineStatus.EDITABLE)
			{
				int qte = readValueInCell(line.index);
				line.montant = qte;
				cumul = cumul+qte;
			}
		}
		
		// Traitement de la ligne ADJUST
		CurrencyLine line = param.findAdjustLine();
		if (line!=null)
		{
			param.performAdjust();
			TextField tf = getTextField(line.index);
			tf.setConvertedValue(line.montant);
		}
		
		// Si affichage du montant total
		if (montantTotalPaiement!=null)
		{
			montantTotalPaiement.setValue(new CurrencyTextFieldConverter().convertToString(param.getMontantTotalPaiement()));
		}
	}

}
