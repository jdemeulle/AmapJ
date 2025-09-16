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
 package fr.amapj.view.engine.grid.integergrid;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.view.engine.grid.ErreurSaisieException;
import fr.amapj.view.engine.grid.GridHeaderLine;
import fr.amapj.view.engine.grid.GridIJData;
import fr.amapj.view.engine.grid.ShortCutManager;
import fr.amapj.view.engine.grid.ShortCutManager.ShortCutLine;
import fr.amapj.view.engine.grid.integergrid.lignecumul.LigneCumulManager;
import fr.amapj.view.engine.grid.utils.HelpPopupSupplier;
import fr.amapj.view.engine.notification.NotificationHelper;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.tools.BaseUiTools;

/**
 * Popup pour la saisie des quantites 
 *  
 */
@SuppressWarnings("serial")
abstract public class PopupIntegerGrid extends CorePopup
{

	private Table table;

	protected IntegerGridParam param = new IntegerGridParam();

	private ShortCutManager shortCutManager;
	
	private Label labelMessageSpecifiqueBottom;
	
	private LigneCumulManager ligneCumulManager;

	/**
	 * 
	 */
	public PopupIntegerGrid()
	{

	}

	abstract public void loadParam();

	/**
	 * Retourne true si il faut fermer le popup
	 * @return
	 */
	abstract public boolean performSauvegarder();
	
	abstract public int getLineHeight(boolean readOnly);
	
	abstract public int getHeaderHeight();

	protected void createContent(VerticalLayout mainLayout)
	{
		setType(PopupType.CENTERFIT);
		loadParam();
		ligneCumulManager = new LigneCumulManager(param);

		if (param.messageSpecifique != null && param.messageSpecifique.length()>0)
		{
			Label messageSpeLabel = new Label(param.messageSpecifique,ContentMode.HTML);
			messageSpeLabel.addStyleName("popup-integer-grid-message");
			mainLayout.addComponent(messageSpeLabel);
		}
		
		if (param.switchButtonAction!=null)
		{
			Button btn = new Button(FontAwesome.ROTATE_RIGHT);
			btn.addStyleName("leftpart");  
			btn.addStyleName("borderless-colored");
			btn.addStyleName("question-mark");
			btn.addClickListener(e->param.switchButtonAction.run());
			mainLayout.addComponent(btn);
			mainLayout.setComponentAlignment(btn, Alignment.TOP_RIGHT);
			addStyleNameForMainLayout("no-padding-top");
		}

		// Construction des headers
		for (GridHeaderLine line : param.headerLines)
		{
			line.constructHeaderLine(mainLayout, param.leftPartLineLargeur,param.largeurCol);
		}

		// Construction de la table de saisie
		table = new Table();
		table.addStyleName("no-vertical-lines");
		table.addStyleName("no-horizontal-lines");
		table.addStyleName("no-stripes");
		

		table.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);

		
		// Colonne de gauche contenant un libellé
		table.addContainerProperty(new Integer(-1), HorizontalLayout.class, null);
		table.setColumnWidth(new Integer(-1), param.leftPartLineLargeur);
		

		// Les autres colonnes correspondant à la saisie des quantites
		for (int i = 0; i < param.nbCol; i++)
		{
			Class clzz;
			if (param.readOnly)
			{
				clzz = Label.class;
			}
			else
			{
				clzz = TextField.class;
			}
			table.addContainerProperty(new Integer(i), clzz, null);
			table.setColumnWidth(new Integer(i), param.largeurCol);
		}

		//
		if (param.readOnly==false)
		{
			shortCutManager = new ShortCutManager(computeShortCutManager());
			shortCutManager.addShorcut(this.getWindow());
		}
		
		// True si il y a une ligne qui a une aide dans la colonne de gauche 
		boolean hasHelpLeftPart = param.lines.stream().anyMatch(e->e.hasLeftPartHelp());

		// Creation de toutes les cellules pour la saisie
		for (int i = 0; i < param.nbLig; i++)
		{
			addRow(i,hasHelpLeftPart);
		}

		if (param.readOnly)
		{
			table.setEditable(false);
		}
		else
		{
			table.setEditable(true);
		}
		table.setSelectable(true);
		table.setSortEnabled(false);
		table.setPageLength(getPageLength());
		mainLayout.addComponent(table);

		// Footer 0 pour avoir un espace
		HorizontalLayout footer0 = new HorizontalLayout();
		footer0.setWidth("200px");
		footer0.setHeight("20px");
		mainLayout.addComponent(footer0);

		// Footer 1 avec le prix total
		ligneCumulManager.createContent(mainLayout);
		
		// Message spécifique en bas de popup
		if (param.messageSpecifiqueBottom != null && param.messageSpecifiqueBottom.length()>0)
		{
			labelMessageSpecifiqueBottom = new Label(param.messageSpecifiqueBottom,ContentMode.HTML);
			labelMessageSpecifiqueBottom.addStyleName("popup-integer-grid-message");
			mainLayout.addComponent(labelMessageSpecifiqueBottom);
		}

	}

	private List<ShortCutLine> computeShortCutManager()
	{
		List<ShortCutLine> res = new ArrayList<>();
		for (int i = 0; i < param.nbLig; i++)
		{
			ShortCutLine line = new ShortCutLine();
			line.isVisible = param.lines.get(i).isVisible;
			line.editable = new boolean[param.nbCol];
			
			for (int j = 0; j < param.nbCol; j++)
			{
				line.editable[j] = ! param.cell[i][j].isStaticText; 	
			}
			
			res.add(line);
		}
		return res;
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
			if ((param.buttonCopyFirstLine==true) && (param.getNbLineVisible() > 1))
			{
				addButton("Copier la 1ère ligne partout", e->	handleCopier());
			}

			addButtonBlank();
			
			addButton("Annuler", e->handleAnnuler());
			
			Button saveButton = addDefaultButton(param.libButtonSave, e->handleSauvegarder());
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
		for (int j = 0; j < param.nbCol; j++)
		{
			int qteRef = -1;
		
			for (int i = 0; i < param.nbLig; i++)
			{
				IntegerGridLine line = param.lines.get(i);
				IntegerGridCell c = param.cell[i][j];
				
				if (c.isStaticText==false && line.isVisible)
				{
					TextField tf = getTextField(i, j);
			
					if (qteRef==-1)
					{
						qteRef = readValueInCell(tf);
					}
					else
					{
						tf.setConvertedValue(qteRef);
					}
				}
			}
		}
	}
	
			

	private int getPageLength()
	{
		Page page = UI.getCurrent().getPage();
		int pageLength = 15;
		
		// On limite le nombre de ligne pour ne pas avoir une double scroolbar
		
		//
		int lineHeight = getLineHeight(param.readOnly);   	
		
		// On cacule la place consommée par les headers, boutons, ...
		int headerAndButtonHeight = getHeaderHeight();
		
		
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
		pageLength = Math.min(pageLength, param.getNbLineVisible());
		
		return pageLength;
	}

	/**
	 * Calcul de la largeur totale de la table
	 * @param hasHelpLeftPart 
	 * @return
	 */
	/*private String getLargeurTotal()
	{
		return ( (param.leftPartLineLargeur+param.espaceInterCol) + (param.nbCol) * (param.largeurCol + param.espaceInterCol) )+ "px";
	}*/


	private void addRow(int lig, boolean hasHelpLeftPart)
	{
		IntegerGridLine line = param.lines.get(lig);
		if (line.isVisible==false)
		{
			return;
		}
		
		
		List<Object> cells = new ArrayList<Object>();
		
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(false);
		hl.setMargin(false);
		
		if (hasHelpLeftPart)
		{
			
			HorizontalLayout h2 = new HorizontalLayout();
			h2.setSpacing(false);
			h2.setMargin(false);
			h2.setWidth("26px");
			h2.setHeight("100%");
			hl.addComponent(h2);
			
			if (line.hasLeftPartHelp())
			{
				Button btn = new Button(FontAwesome.QUESTION_CIRCLE);
				btn.addStyleName("leftpart");  
				btn.addStyleName("borderless-colored");
				btn.addStyleName("question-mark");
				btn.addClickListener(e->HelpPopupSupplier.displayHelpPopup(line.leftPartHelpSupplier));
				h2.addComponent(btn);
				h2.setComponentAlignment(btn, Alignment.MIDDLE_LEFT);
			}
			
		}

		Label dateLabel = new Label(line.leftPart,ContentMode.HTML);
		dateLabel.addStyleName(param.leftPartLineStyle);
		
		hl.addComponent(dateLabel);
		hl.setExpandRatio(dateLabel, 1f);
		

		cells.add(hl);
		for (int j = 0; j < param.nbCol; j++)
		{
			IntegerGridCell c = param.cell[lig][j];
			int qte = c.qte;

			// En lecture simple
			if (param.readOnly)
			{
				//
				String txt;

				if (c.isStaticText)
				{
					txt = c.staticText;
				}
				else if (qte == 0)
				{
					txt = "";
				}
				else
				{
					txt = "" + qte;
				}
				Label tf = new Label(txt);
				tf.addStyleName("cell-voir");
				tf.setWidth((param.largeurCol - 10) + "px");
				cells.add(tf);
			}
			// En mode normal
			else 
			{
				// Si la cellule est exclue
				if (c.isStaticText)
				{
					TextField tf = new TextField();
					tf.setValue(c.staticText);
					tf.setEnabled(false);
					tf.addStyleName("cell-voir");
					tf.setWidth((param.largeurCol - 10) + "px");
					cells.add(tf);
				}
				else
				{
					//
					final TextField tf = BaseUiTools.createQteField("");
					tf.setData(new GridIJData(lig, j));
					if (qte == 0)
					{
						tf.setConvertedValue(null);
					}
					else
					{
						tf.setConvertedValue(new Integer(qte));
					}
					tf.addValueChangeListener(new Property.ValueChangeListener()
					{
						@Override
						public void valueChange(ValueChangeEvent event)
						{
							try
							{
								GridIJData ij = (GridIJData) tf.getData();
								int qte = readValueInCell(tf);
								param.updateQte(ij.i(), ij.j(), qte);
								ligneCumulManager.displayMontantTotal();
							}
							catch (ErreurSaisieException e)
							{
								NotificationHelper.displayNotificationQte();
							}
						}
					});
	
					tf.addStyleName("cell-saisie");
					tf.setWidth((param.largeurCol - 10) + "px");
					shortCutManager.registerTextField(tf);
					cells.add(tf);
				}
			}
		}

		table.addItem(cells.toArray(), new Integer(lig));

	}



	/**
	 * Retourne la valeur dans la cellule sous la forme d'un entier
	 * jette une exception si il y a une erreur
	 */
	private int readValueInCell(TextField tf) throws ErreurSaisieException
	{
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
	
	
	private TextField getTextField(int i,int j)
	{
		Item item1 = table.getItem(new Integer(i));
		TextField tf1 = (TextField) item1.getItemProperty(new Integer(j)).getValue();
		return tf1;
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
		try
		{
			updateModele();
		}
		catch (ErreurSaisieException e)
		{
			NotificationHelper.displayNotificationQte();
			return;
		}
		
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

	

	/**
	 * Lecture de la table pour mettre à jour param.cell.qte
	 */
	private void updateModele() throws ErreurSaisieException
	{
		for (int i = 0; i < param.nbLig; i++)
		{
			IntegerGridLine line = param.lines.get(i);
			
			if (line.isVisible)
			{
				Item item = table.getItem(new Integer(i));
				for (int j = 0; j < param.nbCol; j++)
				{
					IntegerGridCell c = param.cell[i][j];
					
					if (c.isStaticText==false)
					{
						TextField tf = (TextField) item.getItemProperty(new Integer(j)).getValue();
						int qte = readValueInCell(tf);
						c.qte = qte;
					}
				}
			}
		}
	}
	
	protected void updateLabelMessageSpecifiqueBottom(String msg)
	{
		labelMessageSpecifiqueBottom.setValue(msg);
	}
	
	protected void updateFirstCol(int lineNumber,String msg)
	{
		Item item = table.getItem(new Integer(lineNumber));
		
		// Récupération du Horizontal Layout correspondant à la premier colonne  
		HorizontalLayout hl = (HorizontalLayout) item.getItemProperty(new Integer(-1)).getValue();
		
		// Le label est toujours positionné en dernier dans le horizontal layout 
		Label label = (Label) hl.getComponent(hl.getComponentCount()-1);
		
		label.setValue(msg);
	}

	/**
	 * Cette methode doit etre appele apres la modification de la matrice de prix pour affichage correct du prix total  
	 */
	protected void updatePrixTotal()
	{
		ligneCumulManager.displayMontantTotal();
	}
	
}
