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
 package fr.amapj.view.views.gestioncontrat.editorpart.reglesaisie;

import java.util.List;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.model.models.param.ChoixOuiNon;
import fr.amapj.service.services.gestioncontrat.reglesaisie.RegleSaisieDTO;
import fr.amapj.service.services.gestioncontrat.reglesaisie.RegleSaisieModeleContratDTO;
import fr.amapj.service.services.gestioncontrat.reglesaisie.RegleSaisieModeleContratService;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;

/**
 * Permet de modifier les regles de saisies
 */
public class RegleSaisieModeleContratEditorPart extends WizardFormPopup
{
	private RegleSaisieModeleContratDTO dto;
	
	private VerticalLayout reglesPart;
	
	/**
	 * 
	 */
	public RegleSaisieModeleContratEditorPart(Long idModeleContrat)
	{
		setWidth(80);
		popupTitle = "Règles de saisie d'un modèle de contrat";
		
		dto = new RegleSaisieModeleContratService().getRegleSaisieModeleContratDTO(idModeleContrat);
		
		setModel(dto);

	}
	
	@Override
	protected void configure()
	{
		add(()->addInfo());
		add(()->addField());
	}

	
	 
	
	
	private void addInfo()
	{	
		setStepTitle("Informations générales");
		
		addHtml("Cet outil vous permet d'indiquer des règles qui devront être respectées lors de la saisie des quantités sur un contrat.<br/><br/>"
				+ "Par exemple, il est possible d'indiquer que tel produit devra être commandé au moins 3 fois sur la totalité du contrat.<br/><br/>"
				+ "Autre exemple : pour des raisons de conditionnement, vous pouvez indiquer que la somme des produits A,B et C doit être un multiple de 6.");
	}

	private void addField()
	{	
		setStepTitle("Règles de saisie");
	
		reglesPart = new VerticalLayout();
		form.addComponent(reglesPart);
		
		refreshReglesPart();
		
		Button add = new Button("Ajouter une règle");
		add.addClickListener(e->addRegle());
		form.addComponent(add);
		
	}
	
	
	private void refreshReglesPart()
	{
		reglesPart.removeAllComponents();
		
		if (dto.regleSaisies.size()==0)
		{
			reglesPart.addComponent(new Label("Aucune règle de saisie pour ce modèle de contrat"));
		}
		
		List<RegleSaisieDTO> regleSaisies = dto.regleSaisies;
		for (int i = 0; i < regleSaisies.size(); i++) 
		{
			RegleSaisieDTO regleSaisie = regleSaisies.get(i);
		
			HorizontalLayout hl = new HorizontalLayout();
			hl.setWidth("100%");
			
		
			Label l = new Label(getText(regleSaisie,i),ContentMode.HTML);
			hl.addComponent(l);
			hl.setExpandRatio(l, 1f);
			
			
			Button b1 = new Button("Modifier");
			b1.addClickListener(e->updateRegle(regleSaisie));
			hl.addComponent(b1);
			
			Button b2 = new Button("Supprimer");
			b2.addClickListener(e->suppressRegle(regleSaisie));
			hl.addComponent(b2);
			
			reglesPart.addComponent(hl);
			reglesPart.addComponent(new Label(""));
		}
	}

	

	private String getText(RegleSaisieDTO regleSaisie, int i) 
	{
		String s = "<b>Règle "+(i+1)+" :</b>"+regleSaisie.libelle+"<br/>Champ d'application:"+regleSaisie.champApplication;
		if (regleSaisie.activateLibPersonnalise==ChoixOuiNon.OUI)
		{
			s = s+"<br/>Cette règle posséde un libellé personnalisé."; 
		}
		return s;
	}

	private void suppressRegle(RegleSaisieDTO regleSaisie) 
	{
		dto.regleSaisies.remove(regleSaisie);
		refreshReglesPart();
	}

	private void updateRegle(RegleSaisieDTO regleSaisie) 
	{
		RegleSaisieOneRegleEditorPart popup = new RegleSaisieOneRegleEditorPart(dto,regleSaisie);
		popup.open(()->refreshReglesPart());
	}

	private void addRegle() 
	{
		RegleSaisieOneRegleEditorPart popup = new RegleSaisieOneRegleEditorPart(dto,null);
		popup.open(()->refreshReglesPart());
	}

	@Override
	protected void performSauvegarder()
	{
		new RegleSaisieModeleContratService().saveRegleSaisieModeleContrat(dto);
	}
}
