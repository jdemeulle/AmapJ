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
 package fr.amapj.view.engine.listpart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Or;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.view.engine.popup.PopupListener;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.template.ListPartView;
import fr.amapj.view.engine.tools.BaseUiTools;
import fr.amapj.view.engine.tools.DateTimeToStringConverter;
import fr.amapj.view.engine.tools.DateToStringConverter;
import fr.amapj.view.engine.tools.TableItem;
import fr.amapj.view.engine.tools.TableTools;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;


/**
 * Ecran classique avec une liste d'elements 
 *
 */
@SuppressWarnings("serial")
abstract public class StandardListPart<T extends TableItem> extends ListPartView implements ComponentContainer ,  PopupListener
{

	private TextField searchField;

	private List<ButtonHandler> buttons = new ArrayList<ButtonHandler>();
	
	private String textFilter;

	private BeanItemContainer<T> mcInfos;

	private Table cdesTable;
	
	private Class<T> beanClazz;
	
	// Par defaut, une seule ligne est selectionnable à la fois 
	private boolean multiSelect;

	public StandardListPart(Class<T> beanClazz,boolean multiSelect)
	{
		this.beanClazz = beanClazz;
		this.multiSelect = multiSelect;
	}
	
	abstract protected String getTitle();
	
	abstract protected void drawButton();
	
	abstract protected void drawTable();
	
	
	/**
	 * Composant entre le titre et la barre des boutons
	 */
	protected void addSelectorComponent()
	{
		
	}
	
	/**
	 * Composant entre la barre des boutons et la table
	 */
	protected void addExtraComponent()
	{
		
	}
	
	
	/**
	 * Retourne la liste des lignes à afficher 
	 * 
	 * Retourne null si l'ensemble des boutons doivent être desactivés (par exemple,si le selector est dans un etat tel que 
	 * l'ensemble des boutons doivent être inactif)
	 *  
	 * @return
	 */
	abstract protected List<T> getLines();
	
	abstract protected String[] getSortInfos();
	
	/**
	 * Permet à la classe fille d'indiquer si le tri est ascendant ou descendant 
	 * La taille du tableau retournée doit être identique à la taille retournée par String[] getSortInfos()
	 * 
	 * Par défaut, le tri est ascendant (true)
	 * @return
	 */
	protected boolean[] getSortAsc()
	{
		return null;
	}
	
	
	abstract protected String[] getSearchInfos();
	
	
	/**
	 * Permet à l'implémentation fille d'indiquer si l'utilisateur a le droit d'éditer cette ligne en particulier
	 * Dans le cas génaral, l'édition est autorisée
	 * @return
	 */
	protected boolean isEditAllowed()
	{
		return true;
	}
	
	
	@Override
	public void enterIn(ViewChangeEvent event)
	{
		buildMainArea();
	}
	
	// Gestion des colonnes
	
	static public class ColInfo
	{
		private Object propertyId;
		private String header;
		private Converter<String, ?> converter;
		private Align alignment = Align.LEFT;
		private int width = -1;
		private ColumnGenerator generator;
		
		public ColInfo center()
		{
			alignment = Align.CENTER;
			return this;
		}
		
		public ColInfo right()
		{
			alignment = Align.RIGHT;
			return this;
		}
		
		public ColInfo width(int width)
		{
			this.width = width;
			return this;
		} 
	}
	
	
	
	/**
	 * Ajout d'une colonne simple, de type String
	 */
	public ColInfo addColumn(Object propertyId, String header) 
	{
		return addColumnInternal(propertyId,header,null);
	}
	
	/**
	 * Ajout d'une colonne de type Date Time
	 */
	public ColInfo addColumnDateTime(Object propertyId, String header) 
	{
		return addColumnInternal(propertyId,header,new DateTimeToStringConverter());
	}
	
	/**
	 * Ajout d'une colonne de type Date Time
	 */
	public ColInfo addColumnDate(Object propertyId, String header) 
	{
		return addColumnInternal(propertyId,header,new DateToStringConverter());
	}
	
	
	/**
	 * Ajout d'une colonne de type Currency
	 */
	public ColInfo addColumnCurrency(Object propertyId, String header) 
	{
		return addColumnInternal(propertyId,header,new CurrencyTextFieldConverter());
	}

	
	/**
	 * Ajout d'une colonne de type Generator
	 */
	public ColInfo addColumnGenerator(Object propertyId, String header,ColumnGenerator generator) 
	{
		ColInfo colInfo = new ColInfo();
		colInfo.propertyId = propertyId;
		colInfo.header = header;
		colInfo.generator = generator;
		
		cols.add(colInfo);
		
		return colInfo;
	}


	
	private List<ColInfo> cols = new ArrayList<ColInfo>();
	
	private ColInfo addColumnInternal(Object propertyId, String header,Converter<String, ?> converter) 
	{
		ColInfo colInfo = new ColInfo();
		colInfo.propertyId = propertyId;
		colInfo.header = header;
		colInfo.converter = converter;
		
		cols.add(colInfo);
		
		return colInfo;
	}
	
	
	
	
	
	// Gestion des boutons 
	
	/**
	 * Peremt l'ajout d'un bouton pour lancer une action 
	 * 
	 * Si il faut ouvrir un popup, il faut utiliser en priorité addButton
	 */
	public void addButtonAction(String label,ButtonType type, Runnable listener)
	{
		Button newButton = new Button(label);
		newButton.addClickListener(new Button.ClickListener()
		{

			@Override
			public void buttonClick(ClickEvent event)
			{
				listener.run();
			}
		});
		
		//
		ButtonHandler handler = new ButtonHandler();
		handler.button = newButton;
		handler.type = type;
		buttons.add(handler);
	}
	
	
	/**
	 * Permet l'ajout d'un bouton qui conduira à l'ouverture d'un popup 
	 */
	public void addButton(String label,ButtonType type, Function<T, CorePopup> popupSupplier)
	{
		Button newButton = new Button(label);
		newButton.addClickListener(new Button.ClickListener()
		{

			@Override
			public void buttonClick(ClickEvent event)
			{
				T t = null;
				if (multiSelect)
				{
					Set s = (Set) cdesTable.getValue();
					if (s.size()==1)
					{
						t = (T) s.iterator().next();
					}
				}
				else
				{
					t = (T) cdesTable.getValue();
				}
				
				CorePopup popup = popupSupplier.apply(t);
				popup.open(()->onPopupClose());
			}
		});
		
		//
		ButtonHandler handler = new ButtonHandler();
		handler.button = newButton;
		handler.type = type;
		buttons.add(handler);
	}
	
	
	public void addSearchField(String label)
	{
		searchField = new TextField();
		searchField.setInputPrompt(label);
		searchField.addTextChangeListener(new TextChangeListener()
		{

			@Override
			public void textChange(TextChangeEvent event)
			{
				textFilter = event.getText();
				updateFilters();
			}
		});
	}
	
	

	private void buildMainArea()
	{
		// Lecture dans la base de données
		mcInfos = new BeanItemContainer<T>(beanClazz);
		mcInfos.setItemSorter(new ListPartItemSorter());
			
		// Bind it to a component
		cdesTable = createTable(mcInfos);
		
		
		// Dessin des colonnes
		drawTable();
		
		// On écrit les données collectées dans drawTable dans la table 
		List<Object> colPropertyIds = new ArrayList<>();
		for (ColInfo col : cols) 
		{
			cdesTable.setColumnHeader(col.propertyId,col.header);
			if (col.converter!=null)
			{
				cdesTable.setConverter(col.propertyId, col.converter);
			}	
			cdesTable.setColumnAlignment(col.propertyId, col.alignment);
			
			if (col.width!=-1)
			{
				cdesTable.setColumnWidth(col.propertyId, col.width);
			}
			if (col.generator!=null)
			{
				cdesTable.addGeneratedColumn(col.propertyId, col.generator);
			}
			
			colPropertyIds.add(col.propertyId);
		}
		cdesTable.setVisibleColumns(colPropertyIds.toArray());
		
		
		
		
		
		//
		cdesTable.setSelectable(true);
		cdesTable.setMultiSelect(multiSelect);
		cdesTable.setImmediate(true);

		// Activation ou desactivation des boutons delete et edit
		cdesTable.addValueChangeListener(new Property.ValueChangeListener()
		{
			@Override
			public void valueChange(ValueChangeEvent event)
			{
				if (multiSelect)
				{
					Set s = (Set) event.getProperty().getValue();
					buttonBarEditMode( s.size()>0);
				}
				else
				{
					buttonBarEditMode(event.getProperty().getValue() != null);
				}
			}
		});

		cdesTable.setSizeFull();

		cdesTable.addItemClickListener(new ItemClickListener()
		{
			@Override
			public void itemClick(ItemClickEvent event)
			{
				if (event.isDoubleClick())
				{
					cdesTable.select(event.getItemId());
				}
			}
		});
	
		// Dessin du titre 
		Label title2 = new Label(getTitle());
		title2.setSizeUndefined();
		title2.addStyleName("title");	
		addComponent(title2);
		
		// Dessin du selecteur si besoin
		addSelectorComponent();
		
		// Dessin de la barre des boutons
		drawButtonBar();
		
		addExtraComponent();
		addComponent(cdesTable);
		setExpandRatio(cdesTable, 1);
		
		refreshTable();

	}


	/**
	 * Suivant la taille de l'écran, le dessin de la barre est différent
	 */
	private void drawButtonBar() 
	{
		HorizontalLayout gtool = new HorizontalLayout();
		gtool.setWidth("100%");
		
		HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.addStyleName("wrapping");
		gtool.addComponent(toolbar);
		gtool.setExpandRatio(toolbar, 1f);
		
	
		drawButton();
		
		for (ButtonHandler handler : buttons) 
		{
			toolbar.addComponent(handler.button);	
		}
		
		if (searchField!=null)
		{
			if (BaseUiTools.isWidthBelow(960))
			{
				toolbar.addComponent(searchField);
			}
			else
			{
				gtool.addComponent(searchField);	
			}
		}
		addComponent(gtool);
	}

	private void updateFilters()
	{
		mcInfos.removeAllContainerFilters();
		if (textFilter != null && !textFilter.equals(""))
		{
			String[] searchInfos = getSearchInfos();
			Filter[] filters = new Filter[searchInfos.length];
			
			for (int i = 0; i < searchInfos.length; i++) 
			{
				String search = searchInfos[i];
				filters[i] = new Like(search, "%"+textFilter + "%", false);
			}
			Or or = new Or(filters);
			mcInfos.addContainerFilter(or);
		}
	}
	
	
	
	
	/**
	 * Permet de rafraichir la table
	 */
	public void refreshTable()
	{
		String[] sortColumns = getSortInfos(); 
		boolean[] ascColumns = getSortAsc();
		boolean[] sortAscending;
		if (ascColumns==null)
		{
			sortAscending = new boolean[sortColumns.length];
			Arrays.fill(sortAscending,true);
		}
		else 
		{
			if (ascColumns.length!=sortColumns.length)
			{
				throw new AmapjRuntimeException("Les deux méthodes getSortAsc et getSortInfos doivent retourner deux tableaux de même taille");
			}
			sortAscending = ascColumns;
		}
		
		List<T> res = getLines();
		
		if (res==null)
		{
			mcInfos.removeAllItems();
			buttonBarFull(false);
			return ;
		}
		
		
		boolean enabled;
		
		if (multiSelect==true)
		{
			enabled = TableTools.updateTableMultiselect(cdesTable, res, sortColumns, sortAscending);
		}
		else
		{
			enabled = TableTools.updateTable(cdesTable, res, sortColumns, sortAscending);
		}
		
		buttonBarFull(true);
		buttonBarEditMode(enabled);		
	}

	
	
	@Override
	public void onPopupClose()
	{
		refreshTable();
		
	}
	
	
	/**
	 * Permet d'activer ou de désactiver toute la barre des boutons
	 * 
	 */
	private void buttonBarFull(boolean enable)
	{
		for (ButtonHandler handler : buttons) 
		{
			handler.button.setEnabled(enable);
		}
	}
	
	/**
	 * Permet d'activer ou de désactiver les boutons de la barre 
	 * qui sont relatifs au mode édition, c'est à dire les boutons 
	 * Edit et Delete
	 */
	private void buttonBarEditMode(boolean enable)
	{
		if ( (enable==true) && isEditAllowed()==false)
		{
			enable = false;
		}
		
		
		for (ButtonHandler handler : buttons) 
		{
			if (handler.type==ButtonType.EDIT_MODE)
			{
				handler.button.setEnabled(enable);
			}
		}		
	}
	
	
	/**
	 * Retourne la liste des lignes selectionnées
	 * @return
	 */
	protected List<T> getSelectedLines()
	{
		if (multiSelect==false)
		{
			throw new AmapjRuntimeException("Vous ne pouvez pas utiliser cette methode en mono selection ");
		}
		
		List<T> res = new ArrayList<T>();
		Set s = (Set) cdesTable.getValue();
		res.addAll(s);
		return res;
	}
	
	
	/**
	 * Retourne la ligne selectionnée
	 * 
	 * Peux retourner null si il n'y a pas de ligne selectionnée
	 * @return
	 */
	protected T getSelectedLine()
	{
		if (multiSelect==true)
		{
			throw new AmapjRuntimeException("Vous ne pouvez pas utiliser cette methode en multi selection ");
		}
		
		T dto = (T) cdesTable.getValue();
		return dto;
	}
	
	
	/**
	 * Retourne les lignes visibles à l'écran, après filtrage
	 * 
	 */
	protected List<T> getVisiblesLines()
	{
		return mcInfos.getItemIds();
	}
	
	

	public BeanItemContainer<T> getBeanItemContainer()
	{
		return mcInfos;
	}
	
	
	
	
}
