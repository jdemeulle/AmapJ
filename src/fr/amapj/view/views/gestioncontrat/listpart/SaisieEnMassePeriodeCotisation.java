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
 package fr.amapj.view.views.gestioncontrat.listpart;

import java.text.SimpleDateFormat;
import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;

import fr.amapj.common.CollectionUtils;
import fr.amapj.model.models.contrat.modele.EtatModeleContrat;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.gestioncontrat.ModeleContratSummaryDTO;
import fr.amapj.view.engine.popup.formpopup.FormPopup;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.tools.table.complex.ComplexTableBuilder;
import fr.amapj.view.views.searcher.SDPeriodeCotisation;

/**
 * Popup pour la saisie en masse des periodes de cotisation des contrats
 *  
 */
public class SaisieEnMassePeriodeCotisation extends FormPopup
{
	static public enum SortMode
	{
		// Tri par date croissante (puis par nom)
		DATE,
		
		// Tri par nom (puis par date)
		NOM;

		SortMode toogle() 
		{
			if (this==DATE)
			{
				return NOM;
			}
			else
			{
				return DATE;
			}
		}
	}
	
	private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
	
	private List<ModeleContratSummaryDTO> lines;
	
	private ComplexTableBuilder<ModeleContratSummaryDTO> builder;
	
	private SortMode sortMode;

	private Button tri;
		
		
	/**
	 * Réception pour un contrat
	 */
	public SaisieEnMassePeriodeCotisation()
	{
		super();
			
		popupTitle = "Modification en masse des périodes de cotisation associées aux contrats";
		setHeight("100%");
		setWidth(100);
		
	}
	
	

	@Override
	protected void addFields()
	{
		lines = new GestionContratService().getModeleContratInfo(EtatModeleContrat.CREATION,EtatModeleContrat.ACTIF,EtatModeleContrat.ARCHIVE);
	
		sortMode = SortMode.DATE;
		sortLines();
		
		if(lines.size()==0)
		{
			addHtml("Il n'y a pas de contrats à modifier");
			setWidth(60);
			return;
		}
		
		builder = new ComplexTableBuilder<ModeleContratSummaryDTO>(lines);
		
		builder.addString("État", false, 100, e->e.etat);
		builder.addString("Nom", false, 300, e->e.nom);
		builder.addString("Producteur", false, 300, e->e.nomProducteur);
		builder.addString("Première livraison", false, 100, e->df.format(e.dateDebut));
		builder.addString("Dernière livraison", false, 100, e->df.format(e.dateFin));
		builder.addSearcher("Période de cotisation", "pc" ,true, 300, e->e.periodeCotisationId, new SDPeriodeCotisation());
		builder.setPageLength(12);
		
		
		addComplexTable(builder);
		
	}
	


	private void sortLines() 
	{
		if (sortMode==SortMode.NOM)
		{
			CollectionUtils.sort(lines, e->e.nom);
		}
		else
		{
			CollectionUtils.sort(lines, e->e.dateFin,false,e->e.nom,true);
		}
	}


	@Override
	protected void createButtonBar()
	{
		tri = addButton("Trier par nom", e->handleTri());		
		super.createButtonBar();
	}
	
	
	private void handleTri() 
	{
		sortMode = sortMode.toogle();
		tri.setCaption(sortMode==SortMode.DATE ? "Trier par nom" : "Trier par date");
		writeToModel();
		sortLines();
		builder.reload(lines);
	}



	@Override
	protected void performSauvegarder() throws OnSaveException
	{
		writeToModel();
		new GestionContratService().updatePeriodeCotisationMasse(lines);
	}

	/**
	 * Copie les données saisies dans le modele
	 */
	private void writeToModel() 
	{
		for (int i = 0; i < lines.size(); i++)
		{
			ModeleContratSummaryDTO line = lines.get(i);
						
			ComboBox cb = (ComboBox)  builder.getComponent(i, "pc");
			line.periodeCotisationId = (Long) cb.getValue();
		}
		
	}
	
}
