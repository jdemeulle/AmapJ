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
 package fr.amapj.view.engine.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.validator.DateRangeValidator;
import com.vaadin.server.Page;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.DateUtils;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;
import fr.amapj.view.engine.widgets.DoubleTextFieldConverter;
import fr.amapj.view.engine.widgets.IntegerTextFieldConverter;
import fr.amapj.view.engine.widgets.QteTextFieldConverter;

public class BaseUiTools
{
	
	static public Label addStdLabel(Layout layout,String content,String styleName)
	{
		Label tf = new Label(content);
		tf.addStyleName(styleName);
		layout.addComponent(tf);
		return tf;
	}
	
	
	static public Label addHtmlLabel(Layout layout,String content,String styleName)
	{
		Label tf = new Label(content,ContentMode.HTML);
		tf.addStyleName(styleName);
		layout.addComponent(tf);
		return tf;
	}
	
	
	/**
	 * Crée un Panel avec un VerticalLayout à l'intérieur 
	 * 
	 * @param layout
	 * @param styleName
	 * @return
	 */
	static public VerticalLayout addPanel(Layout layout,String styleName)
	{
		Panel p0 = new Panel();
		p0.setWidth("100%");
		p0.addStyleName(styleName);
		
		VerticalLayout vl1 = new VerticalLayout();
		vl1.setMargin(true);
		p0.setContent(vl1);
		layout.addComponent(p0);
		
		return vl1;
	}
	
	/**
	 * Permet de créer un bandeau, c'est à dire avec un texte avec un fond, et ceci sur toute la longueur 
	 * 
	 */
	static public Label addBandeau(Layout layout,String content,String styleName)
	{
		Label l = new Label(content,ContentMode.HTML);
		l.setWidth("100%");
		
		Panel p1 = new Panel();
		p1.setContent(l);
		p1.addStyleName("bandeau-"+styleName);
		
		layout.addComponent(p1);
		
		return l;
	}
	
	
	/**
	 * Permet de créer un bandeau, c'est à dire avec un texte avec un fond, et ceci sur toute la longueur 
	 * 
	 */
	static public HorizontalLayout addBandeau(Layout layout,String textLeft,String textRight,String styleName)
	{
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		
		Label l1 = new Label(textLeft,ContentMode.HTML);
		l1.setWidthUndefined();
		hl.addComponent(l1);
		hl.setComponentAlignment(l1, Alignment.MIDDLE_LEFT);
		
		Label l2 = new Label(textRight,ContentMode.HTML);
		l2.setWidthUndefined();
		hl.addComponent(l2);
		hl.setComponentAlignment(l2, Alignment.MIDDLE_RIGHT);
		
		
		Panel p1 = new Panel();
		p1.setContent(hl);
		p1.addStyleName("bandeau-"+styleName);
		
		layout.addComponent(p1);
		
		return hl;
	}
	
	
	
	
	static public Label addEmptyLine(Layout layout)
	{
		Label l = new Label("<br/>",ContentMode.HTML);
		layout.addComponent(l);
		return l;
	}
	
	
	
	
	
	
	/**
	 * On distingue deux modes d'affichage dans le logiciel : un mode compact pour les smartphone,
	 * et un mode classique dans les autres cas
	 */
	static public boolean isCompactMode()
	{
		Page page = UI.getCurrent().getPage();
		int width = page.getBrowserWindowWidth();
		int height = page.getBrowserWindowHeight();
		
		if ((height<700) || (width<700))
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * 
	 * @param width en pixel 
	 * @return
	 */
	public static boolean isWidthBelow(int width)
	{
		Page page = UI.getCurrent().getPage();
		int currentWidth = page.getBrowserWindowWidth();
		return currentWidth<width;
	}
	
	/**
	 * Retourne la taille en pixel de la largeur de l'écran du navigateur 
	 */
	public static int getWidth()
	{
		Page page = UI.getCurrent().getPage();
		int currentWidth = page.getBrowserWindowWidth();
		return currentWidth;
	}
	
	/**
	 * Retourne la taille en pixel de la hauteur de l'écran du navigateur 
	 */
	public static int getHeight()
	{
		Page page = UI.getCurrent().getPage();
		return page.getBrowserWindowHeight();
	}
	
	
	
	/**
	 * Calcule la taille en pixel du popup, en prenant en compte le ratio du popup 
	 */
	public static int computePopupWidth(int widthRatio)
	{
		Page page = UI.getCurrent().getPage();
		int currentWidth = page.getBrowserWindowWidth();
		return (currentWidth*widthRatio)/100;
	}
	
	
	
	
	
	
	

	static public PopupDateField createDateField(FieldGroup binder, String propertyId, String title)
	{
		PopupDateField sample = createPopupDateField(title);
		binder.bind(sample, propertyId);
		return sample;
	}



	static public PopupDateField createDateField(String title)
	{
		PopupDateField sample = createPopupDateField(title);
		return sample;
	}

	
	private static PopupDateField createPopupDateField(String title)
	{
		PopupDateField sample = new PopupDateField(title)
		{
			protected Date handleUnparsableDateString(String dateString) throws Converter.ConversionException
			{
				List<String> strs = new ArrayList<>();
				strs.add("Nous ne comprenons pas la date \""+dateString+"\"");
				strs.add("Merci de cliquer sur l'icone de calendrier sur la gauche pour choisir la date");
				strs.add("ou de saisir la date au format 02/10/21 pour le 2 octobre 2021");
				MessagePopup.open(new MessagePopup("Erreur sur la date", strs));
				return null;
			}
		};
		
		try
		{
			String errorMessage = "La date est incorrecte. Elle doit être obligatoirement comprise entre 2000 et 2100";
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			Date minValue = df.parse("01/01/2000");
			Date maxValue = df.parse("01/01/2100");
			sample.addValidator(new DateRangeValidator(errorMessage, minValue, maxValue, null));
		}
		catch (ParseException e) 
		{
			throw new AmapjRuntimeException(e);
		}
		
		sample.setValue(DateUtils.getDate());
		sample.setImmediate(true);
		sample.setLocale(Locale.FRANCE);
		sample.setResolution(Resolution.DAY);
		
		return sample;
	}
	
	
	/**
	 * Permet la saisie d'une quantité, c'est à dire un nombre entier compris entre 0 et l'infini
	 * 
	 */
	static public TextField createQteField(String title)
	{
		TextField tf = new TextField(title);
		tf.setConverter(new QteTextFieldConverter());
		tf.setNullRepresentation("");
		tf.setImmediate(true);
		return tf;
	}

	/**
	 * Permet la saisie d'un Integer, c'est à dire un nombre entier compris entre moins l'infi et plus l'infini
	 * 
	 */
	static public TextField createIntegerField(String title)
	{
		TextField tf = new TextField(title);
		tf.setConverter(new IntegerTextFieldConverter());
		tf.setNullRepresentation("");
		tf.setImmediate(true);
		return tf;
	}
	
	/**
	 * Permet la saisie d'un Double
	 * 
	 */
	static public TextField createDoubleField(String title)
	{
		TextField tf = new TextField(title);
		tf.setConverter(new DoubleTextFieldConverter());
		tf.setNullRepresentation("");
		tf.setImmediate(true);
		return tf;
	}
	
	

	/**
	 * Permet la saisie d'un prix, c'est à dire un nombre allant de 0.00 à 999999999.99 avec deux chiffres après la virgule
	 * 
	 */

	static public TextField createCurrencyField(String title, boolean allowNegativeNumber)
	{
		TextField tf = new TextField(title);
		tf.setConverter(new CurrencyTextFieldConverter(allowNegativeNumber));
		tf.setNullRepresentation("");
		tf.setImmediate(true);
		return tf;
	}

	/**
	 * Permet la saisied'une check box
	 * 
	 */

	static public CheckBox createCheckBoxField(String title)
	{
		CheckBox checkbox = new CheckBox();
		checkbox.addStyleName("align-center");
		checkbox.setImmediate(true);

		return checkbox;
	}

	/**
	 * Permet de créer une table sans titre
	 * 
	 * @param nbCol
	 * @return
	 */
	static public Table createStaticTable(int nbCol)
	{
		Table t = new Table();
		t.setStyleName("big");
		t.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);

		for (int i = 0; i < nbCol; i++)
		{
			t.addContainerProperty("", String.class, null);
		}

		return t;
	}

	static public Table addLine(Table t, String... line)
	{
		t.addItem(line, null);
		return t;
	}


	static public void setReadOnlyRecursively(HasComponents a)
	{
		for (Component component : a) 
		{
			component.setReadOnly(true);
			if (component instanceof HasComponents)
			{
				setReadOnlyRecursively( (HasComponents) component); 
			}
		}
	}


	public static boolean isFirefoxAndroid() 
	{
		WebBrowser browser = UI.getCurrent().getPage().getWebBrowser();
		return browser.isFirefox() && browser.isAndroid();
	}

}
