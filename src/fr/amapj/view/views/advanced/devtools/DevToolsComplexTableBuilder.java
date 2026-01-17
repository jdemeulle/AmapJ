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

 package fr.amapj.view.views.advanced.devtools;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;

import fr.amapj.service.services.gestioncontrat.LigneContratDTO;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.tools.table.complex.ComplexTableBuilder;

/**
 * Permet de tester le fonctionnement du Complex Table Builder
 * 
 *
 */
public class DevToolsComplexTableBuilder extends WizardFormPopup
{

	/**
	 * 
	 */
	public DevToolsComplexTableBuilder()
	{
		setWidth(80);
		popupTitle = "Complex Table Builder ";
	}
	
	@Override
	protected void configure()
	{
		add(()->addTest1());
		/*add(Step.TEST2,()->addTest2());
		add(Step.TEST3,()->addTest3());
		add(Step.TEST4,()->addTest4());*/
	}

	
	static private class Bean1
	{
		public String nom;
		
		public String conditionnement;
		
		public Integer prix;

		public Bean1(String nom, String conditionnement, Integer prix)
		{
			this.nom = nom;
			this.conditionnement = conditionnement;
			this.prix = prix;
		}
	}

	private void addTest1()
	{
		// Titre
		setStepTitle("Cas 1 ");
		
		// Construction de la liste des beans
		List<Bean1> ls = Arrays.asList(new Bean1("a0","a1",2));
		
		
		ComplexTableBuilder<Bean1> builder = new ComplexTableBuilder<Bean1>(ls);
		builder.setPageLength(7);
		
		builder.addString("Nom du produit", false, 300, e->e.nom);
		builder.addString("Conditionnement", false, 300,  e->e.conditionnement);
		builder.addCurrency("Prix 1", "prix1",true , 100,  e->e.prix);
		builder.addCurrency("Prix 2", "prix2",true , 100,  e->e.prix);
		
		addComplexTable(builder);
		
		
		
		String content ="Dans ce cas, tout fonctionne bien";
		addHtml(content);
		
	}
	
	private void addTest2()
	{
		// Titre
		setStepTitle("UNIQUEMENT DES TEXTFIELDS EDITABLES");
		
		
		
		final Table table = new Table("La table");

		// 
		table.addContainerProperty("col1", TextField.class, null);
		table.addContainerProperty("col2",  TextField.class, null);

		// Ligne 1
		TextField tf1 = new TextField();
		tf1.setValue("Canopus");
		tf1.addFocusListener(e->table.select(1));   // IMPORTANT !! 

		TextField tf2 = new TextField();
		tf2.setValue("0.75");
		tf2.addFocusListener(e->table.select(1));   // IMPORTANT !!
		
		table.addItem(new Object[]{tf1,tf2}, 1);
		
		// Ligne 2
		tf1 = new TextField();
		tf1.setValue("Actarus");
		tf1.addFocusListener(e->table.select(2));   // IMPORTANT !!
			
		tf2 = new TextField();
		tf2.setValue("0.85");
		tf2.addFocusListener(e->table.select(2));   // IMPORTANT !!
			
		table.addItem(new Object[]{tf1,tf2}, 2);
		
		// Ligne 3
		tf1 = new TextField();
		tf1.setValue("Venus");
		tf1.addFocusListener(e->table.select(3));   // IMPORTANT !!
			
		tf2 = new TextField();
		tf2.setValue("1.45");
		tf2.addFocusListener(e->table.select(3));   // IMPORTANT !!
			
		table.addItem(new Object[]{tf1,tf2}, 3);
		
		
		// Show exactly the currently contained rows (items)
		table.setPageLength(table.size());
		
		// Allow selecting items from the table.
		table.setSelectable(true);
		table.setSortEnabled(false);

		// Send changes in selection immediately to server.
		table.setImmediate(true);

		// Shows feedback from selection.
		final Label current = new Label("Selected: -");

		
		// Handle selection change.
		table.addValueChangeListener(new Property.ValueChangeListener() 
		{
		    public void valueChange(ValueChangeEvent event) 
		    {
		        current.setValue("Selected: " + table.getValue());
		    }
		});
		
		
		form.addComponent(table);
		form.addComponent(current);
		
		String content ="Dans ce cas, tout fonctionne bien, on peut bien sélectionner la ligne comme on veut, mais il faut avoir ajouté un listener sur chaque textfield ";
		addHtml(content);
		
	}
	
	private void addTest3()
	{
		// Titre
		setStepTitle("UNIQUEMENT DES TEXTFIELDS ENABLE = FALSE");
		
		
		
		final Table table = new Table("La table");

		// 
		table.addContainerProperty("col1", TextField.class, null);
		table.addContainerProperty("col2",  TextField.class, null);

		// Ligne 1
		TextField tf1 = new TextField();
		tf1.setValue("Canopus");
		tf1.addFocusListener(e->table.select(1));   // IMPORTANT !! 
		tf1.setEnabled(false);

		TextField tf2 = new TextField();
		tf2.setValue("0.75");
		tf2.addFocusListener(e->table.select(1));   // IMPORTANT !!
		tf2.setEnabled(false);
		
		table.addItem(new Object[]{tf1,tf2}, 1);
		
		// Ligne 2
		tf1 = new TextField();
		tf1.setValue("Actarus");
		tf1.addFocusListener(e->table.select(2));   // IMPORTANT !!
		tf1.setEnabled(false);
			
		tf2 = new TextField();
		tf2.setValue("0.85");
		tf2.addFocusListener(e->table.select(2));   // IMPORTANT !!
		tf2.setEnabled(false);
			
		table.addItem(new Object[]{tf1,tf2}, 2);
		
		// Ligne 3
		tf1 = new TextField();
		tf1.setValue("Venus");
		tf1.addFocusListener(e->table.select(3));   // IMPORTANT !!
		tf1.setEnabled(false);
			
		tf2 = new TextField();
		tf2.setValue("1.45");
		tf2.addFocusListener(e->table.select(3));   // IMPORTANT !!
		tf2.setEnabled(false);
			
		table.addItem(new Object[]{tf1,tf2}, 3);
		
		
		// Show exactly the currently contained rows (items)
		table.setPageLength(table.size());
		
		// Allow selecting items from the table.
		table.setSelectable(true);
		table.setSortEnabled(false);

		// Send changes in selection immediately to server.
		table.setImmediate(true);

		// Shows feedback from selection.
		final Label current = new Label("Selected: -");

		
		// Handle selection change.
		table.addValueChangeListener(new Property.ValueChangeListener() 
		{
		    public void valueChange(ValueChangeEvent event) 
		    {
		        current.setValue("Selected: " + table.getValue());
		    }
		});
		
		
		form.addComponent(table);
		form.addComponent(current);
		
		String content ="Dans ce cas, impossible de le faire fonctionner. On n'arrive pas à sélectionner les lignes. Le seul moyen est de cliquer juste au milieu entre les 2 colonnes, mais pas pratique du tout  ";
		addHtml(content);
		
	}
	
	
	private void addTest4()
	{
		// Titre
		setStepTitle("UNIQUEMENT DES TEXTFIELDS READONLY = TRUE ");
		
		
		
		final Table table = new Table("La table");

		// 
		table.addContainerProperty("col1", TextField.class, null);
		table.addContainerProperty("col2",  TextField.class, null);

		// Ligne 1
		TextField tf1 = new TextField();
		tf1.setValue("Canopus");
		//tf1.addFocusListener(e->table.select(1));   // A NE PAS METTRE - IMPORTANT !! 
		tf1.setReadOnly(true);

		TextField tf2 = new TextField();
		tf2.setValue("0.75");
		//tf2.addFocusListener(e->table.select(1));   // A NE PAS METTRE - IMPORTANT !!
		tf2.setReadOnly(true);
		
		table.addItem(new Object[]{tf1,tf2}, 1);
		
		// Ligne 2
		tf1 = new TextField();
		tf1.setValue("Actarus");
		//tf1.addFocusListener(e->table.select(2));   // A NE PAS METTRE - IMPORTANT !!
		tf1.setReadOnly(true);
			
		tf2 = new TextField();
		tf2.setValue("0.85");
		//tf2.addFocusListener(e->table.select(2));   // A NE PAS METTRE - IMPORTANT !!
		tf2.setReadOnly(true);
			
		table.addItem(new Object[]{tf1,tf2}, 2);
		
		// Ligne 3
		tf1 = new TextField();
		tf1.setValue("Venus");
		//tf1.addFocusListener(e->table.select(3));   // A NE PAS METTRE - IMPORTANT !!
		tf1.setReadOnly(true);
			
		tf2 = new TextField();
		tf2.setValue("1.45");
		//tf2.addFocusListener(e->table.select(3));   // A NE PAS METTRE - IMPORTANT !!
		tf2.setReadOnly(true);
			
		table.addItem(new Object[]{tf1,tf2}, 3);
		
		
		// Show exactly the currently contained rows (items)
		table.setPageLength(table.size());
		
		// Allow selecting items from the table.
		table.setSelectable(true);
		table.setSortEnabled(false);

		// Send changes in selection immediately to server.
		table.setImmediate(true);

		// Shows feedback from selection.
		final Label current = new Label("Selected: -");

		
		// Handle selection change.
		table.addValueChangeListener(new Property.ValueChangeListener() 
		{
		    public void valueChange(ValueChangeEvent event) 
		    {
		        current.setValue("Selected: " + table.getValue());
		    }
		});
		
		
		form.addComponent(table);
		form.addComponent(current);
		
		String content ="Dans ce cas, ca fonctionne, mais il est obligatoire DE NE PAS METTRE DE FOCUS LISTENER SUR LES TEXTFIELDS, SINON CA NE MARCHE PAS   ";
		addHtml(content);
		
	}
	

	@Override
	protected void performSauvegarder()
	{
		
	}
	
}
