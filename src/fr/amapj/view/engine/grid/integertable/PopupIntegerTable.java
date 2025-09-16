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
 package fr.amapj.view.engine.grid.integertable;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.common.FormatUtils;
import fr.amapj.view.engine.grid.ErreurSaisieException;
import fr.amapj.view.engine.grid.GridIJData;
import fr.amapj.view.engine.grid.GridSizeCalculator;
import fr.amapj.view.engine.grid.ShortCutManager;
import fr.amapj.view.engine.grid.ShortCutManager.ShortCutLine;
import fr.amapj.view.engine.grid.utils.HelpPopupSupplier;
import fr.amapj.view.engine.notification.NotificationHelper;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;
import fr.amapj.view.engine.tools.BaseUiTools;
import fr.amapj.view.engine.widgets.QteTextFieldConverter;

/**
 * Popup pour la saisie des quantites en mode Table (une seule date) 
 *  
 */
abstract public class PopupIntegerTable extends CorePopup
{

	private Label prixTotal;

	protected IntegerTableParam param = new IntegerTableParam();

	private ShortCutManager shortCutManager;
	
	private Label labelMessageSpecifiqueBottom;
	
	private List<TextField> tfs = new ArrayList<>();
	
	//
	private int nbCol;
	
	//
	private int nbRow;
	
	// Largeur des cellules en pixel, exemple 110
	public int cellWidth;
	
	// Hauteur des cellules en pixel, pour chaque ligne, par exemple 250
	// La taille du tableau est nbRow
	private int[] cellHeight;
	
	private int fullWidth;
	
	// True si la fenetre occupe tout l'écran en hauteur 
	private boolean isHeightFull;
	
	/**
	 * 
	 */
	public PopupIntegerTable()
	{

	}

	abstract public void loadParam();

	/**
	 * Retourne true si il faut fermer le popup
	 * @return
	 */
	abstract public boolean performSauvegarder();
	

	protected void createContent(VerticalLayout mainLayout)
	{
		//
		loadParam();
		
		// Calcul des tailles des cellules (nb de lignes, nb de colonnes, hauteur des cellules, ..) 
		computeWidthAndHeigth();
		
		
		setType(PopupType.FILL);
		setWidth(10,fullWidth);
		
		if (isHeightFull)
		{
			setHeight("100%");
		}
		

		if (param.messageSpecifique != null && param.messageSpecifique.length()>0)
		{
			Label messageSpeLabel = new Label(param.messageSpecifique,ContentMode.HTML);
			messageSpeLabel.addStyleName("popup-integer-table-message");
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
		
		//
		shortCutManager = new ShortCutManager(computeShortCutManager());
		shortCutManager.addShorcut(this.getWindow());
		
		GridLayout gd = new GridLayout(nbCol, nbRow);
		gd.setSpacing(true);
		mainLayout.addComponent(gd);
		
		for (int i = 0; i < param.nbCell; i++)
		{
			IntegerTableCell paramCell = param.cell[i];
			
			int row = i / nbCol;
			int col = i % nbCol;
			
			gd.addComponent(createCell(paramCell,row,col,i));			
		}

		// Footer 0 pour avoir un espace
		HorizontalLayout footer0 = new HorizontalLayout();
		footer0.setWidth("200px");
		footer0.setHeight("20px");
		mainLayout.addComponent(footer0);

		// Footer 1 avec le prix total
		if (param.hasLignePrixTotal)
		{
			prixTotal = new Label("",ContentMode.HTML);
			displayMontantTotal();	
			mainLayout.addComponent(prixTotal);
		}
	}
		
	private Component createCell(IntegerTableCell paramCell, int row, int col,int cellNumber)
	{
		Panel p = new Panel();
		p.addStyleName("popup-integer-table-panel");
		p.setWidth(cellWidth+"px");
		p.setHeight(cellHeight[row]+"px");
		
		// Libellé 1 avec le ? eventuellement 
		VerticalLayout vl = new VerticalLayout();
		vl.setSizeFull();
		vl.setMargin(true);
		
		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setSpacing(false);
		hl1.setMargin(false);
		hl1.setWidth("100%");
		
		Label lib1Label = new Label(paramCell.lib1);
		hl1.addComponent(lib1Label);
		hl1.setExpandRatio(lib1Label, 1f);
		
		if (paramCell.helpSupplier.hasHelp())
		{
			Button btn = new Button(FontAwesome.QUESTION_CIRCLE);
			btn.addStyleName("leftpart");  
			btn.addStyleName("borderless-colored");
			btn.addStyleName("question-mark");
			btn.addClickListener(e->HelpPopupSupplier.displayHelpPopup(paramCell.helpSupplier));
			hl1.addComponent(btn);
			hl1.setComponentAlignment(btn, Alignment.TOP_LEFT);
		}
		vl.addComponent(hl1);
		
		// Le libellé 2
		BaseUiTools.addStdLabel(vl, paramCell.lib2, "");
		
		// Un espace vide 
		VerticalLayout vl2 = new VerticalLayout();
		vl2.setSizeFull();
		vl.addComponent(vl2);
		vl.setExpandRatio(vl2, 1f);
		
		// Le prix et la quantité		
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);
		hl.setWidth("100%");

		Label prixLabel = BaseUiTools.addHtmlLabel(hl, "<b>"+FormatUtils.prix(paramCell.prix)+"</b>", "");
		hl.setComponentAlignment(prixLabel, Alignment.MIDDLE_LEFT);
		
		TextField tf = createTextField(paramCell,row,col,cellNumber);
		hl.addComponent(tf);
		hl.setComponentAlignment(tf, Alignment.MIDDLE_RIGHT);
		
		vl.addComponent(hl);
		vl.setExpandRatio(hl, 0);
				
		p.setContent(vl);
		
	
		
		
		return p;
	}

	private TextField createTextField(IntegerTableCell c, int row, int col, int cellNumber)
	{
		TextField tf = new TextField();
		tf.setNullRepresentation("");
		tf.setImmediate(true);
		tfs.add(tf);
		tf.setWidth("70px");
		tf.addStyleName("cell-saisie");
		tf.setData(new GridIJData(row, col));
		shortCutManager.registerTextField(tf);
			
		if (c.isStaticText)
		{
			tf.setValue(c.staticText);
			tf.setEnabled(false);
			return tf;
		}
		
		tf.setConverter(new QteTextFieldConverter());
		tf.setConvertedValue(new Integer(c.qte));
		
		if (param.readOnly)
		{
			tf.setReadOnly(true);
			return tf;
		}
		
		tf.addValueChangeListener(e->handleTextFieldChange(tf,cellNumber));
		return tf;	
	}

	private void handleTextFieldChange(TextField tf, int cellNumber)
	{
		try
		{
			GridIJData ij = (GridIJData) tf.getData();
			int qte = readValueInCell(tf);
			param.updateQte(cellNumber, qte);
			displayMontantTotal();
		}
		catch (ErreurSaisieException e)
		{
			NotificationHelper.displayNotificationQte();
		}
	}


	private List<ShortCutLine> computeShortCutManager()
	{
		List<ShortCutLine> res = new ArrayList<>();
		for (int i = 0; i < nbRow; i++)
		{
			ShortCutLine line = new ShortCutLine();
			line.isVisible = true;
			line.editable = new boolean[nbCol];
			
			for (int j = 0; j < nbCol; j++)
			{
				line.editable[j] = isEditable(i,j);
			}
			
			res.add(line);
		}
		return res;
	}


	private boolean isEditable(int i, int j)
	{
		int cellNumber = i*nbCol+j;
		if (cellNumber>=param.nbCell)
		{
			return false;
		}
		return param.cell[cellNumber].isStaticText==false;
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
			addButtonBlank();
			
			addButton("Annuler", e->handleAnnuler());
			
			Button saveButton = addDefaultButton(param.libButtonSave, e->handleSauvegarder());
			saveButton.addStyleName("primary");
			
		}
	}


	private void displayMontantTotal()
	{
		if (prixTotal!=null)
		{
			prixTotal.setValue("<b>"+param.libPrixTotal+" : "+FormatUtils.prix(param.getMontantTotal())+"</b>");	
		}
		
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
		for (int i = 0; i < param.nbCell; i++)
		{
			IntegerTableCell c = param.cell[i];
					
			if (c.isStaticText==false)
			{
				TextField tf = tfs.get(i);
				int qte = readValueInCell(tf);
				c.qte = qte;
			}
		}
	}
	
	protected void updateLabelMessageSpecifiqueBottom(String msg)
	{
		labelMessageSpecifiqueBottom.setValue(msg);
	}

	/**
	 * Cette methode doit etre appele apres la modification de la matrice de prix pour affichage correct du prix total  
	 */
	protected void updatePrixTotal()
	{
		displayMontantTotal();
	}
	
	
	// PARTIE SPECIFIQUE AU CALCUL DES TAILLES ( NB DE COLONNES, DE LIGNES , ...) 
	
	
	/**
	 * Calcul de la largeur et de la hauteur des cellules, en fonction du texte présent à l'intérieur
	 * @param param
	 */
	private void computeWidthAndHeigth() 
	{
		// Calcul du nombre de colonnes 
		int width = BaseUiTools.getWidth();
		if (width<600)
		{
			nbCol = 1;
			cellWidth = width-45;
		}
		else if (width<900)
		{
			nbCol = 2;
			cellWidth = 260;
		}
		else if (width<1200)
		{
			nbCol = 3;
			cellWidth = 260;
		}
		else 
		{
			nbCol = 4;
			cellWidth = 260;
			
			// Traitement de certains cas particuliers
			if (param.nbCell==6 || param.nbCell==9)
			{
				nbCol = 3;
				cellWidth = 260;
			}
		}
		
		// Calcul du nombre de lignes
		nbRow = getNbRow();	
		
		// Calcul de la hauteur de chaque ligne
		cellHeight = new int[nbRow];
		for (int i = 0; i < nbRow; i++) 
		{
			cellHeight[i] = computeHeight(i);
		}
		
		
		// Largeur totale , 64 pixel correspond à la marge à droite et gauche 
		fullWidth = nbCol*(cellWidth+11)+64;
		isHeightFull = computeIsHeightFull();
	}



	/**
	 * Caclul de la hauteur de la ligne i en pixel
	 */
	private int computeHeight(int i) 
	{
		int height = 0;
		for (int j = 0; j < nbCol; j++) 
		{
			height = Math.max(height, heightOf(i,j));
		}
		return height;
	}



	private int heightOf(int i, int j) 
	{
		int index = index(i,j);
		if (index==-1)
		{
			return 0;
		}
		IntegerTableCell c = param.cell[index];
		boolean hasHelp = c.helpSupplier.hasHelp();
		return height(c.lib1,hasHelp)+height(c.lib2,false)+80;
	}



	private int index(int i, int j) 
	{
		int index = i*nbCol+j;
		if (index>=param.nbCell)
		{
			return -1;
		}
		return index;
	}



	private int height(String lib,boolean hasHelp) 
	{
		// 26 : marges classique autour du texte
		// 20 : taille du bouton ? pour l'aide 
		int marge = 26 + (hasHelp ? 20 : 0) ;
		
		return new GridSizeCalculator().getHeightInPixel(lib,cellWidth-marge, "Arial",16);
	}



	private int getNbRow()
	{
		int r = param.nbCell % nbCol;
		if (r==0)
		{
			return param.nbCell / nbCol;
		}
		else
		{
			return param.nbCell / nbCol+1;
		}
	}
	
	private boolean computeIsHeightFull() 
	{
		// 170 : correspond à la hauteur du bandeau initial 
		int fullHeight = 170;
		
		if (param.messageSpecifique != null && param.messageSpecifique.length()>0)
		{
			fullHeight = fullHeight+30;
		}
		
		for (int i = 0; i < cellHeight.length; i++) 
		{
			// 11 px : marge entre les cellules
			fullHeight = fullHeight+cellHeight[i]+11;
		}
		
		return BaseUiTools.getHeight()<=fullHeight;
	}
	
}
