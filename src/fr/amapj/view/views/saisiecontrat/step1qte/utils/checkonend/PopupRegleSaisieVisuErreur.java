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
 package fr.amapj.view.views.saisiecontrat.step1qte.utils.checkonend;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.model.models.param.ChoixOuiNon;
import fr.amapj.service.services.gestioncontrat.reglesaisie.RegleSaisieDTO;
import fr.amapj.service.services.gestioncontrat.reglesaisie.VerifRegleSaisieResultDTO;
import fr.amapj.service.services.gestioncontrat.reglesaisie.VerifRegleSaisieResultDTO.ResultDTO;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;
import fr.amapj.view.engine.popup.okcancelpopup.OKCancelPopup;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.SaisieContratData;

/**
 *  
 */
public class PopupRegleSaisieVisuErreur extends OKCancelPopup
{		

	private VerifRegleSaisieResultDTO resultDTO;

	/**
	 * 
	 */
	public PopupRegleSaisieVisuErreur(VerifRegleSaisieResultDTO resultDTO)
	{
		this.resultDTO = resultDTO;
		popupTitle = "Contrat en anomalie";
		// setColorStyle(ColorStyle.RED);

		saveButtonTitle = "OK";
		hasCancelButton = false;
			
		// 
		setWidth(50,450);
	}
	
	@Override
	protected void createContent(VerticalLayout contentLayout)
	{
		if (resultDTO.errors.size()==1)
		{
			displayOneError(contentLayout);
		}
		else
		{
			displayMutipleErrors(contentLayout);
		}
		
		
		
	}

	

	private void displayOneError(VerticalLayout contentLayout) 
	{
		Label l1 = new Label("Ce contrat comporte une anomalie :<br/><br/>",ContentMode.HTML);
		contentLayout.addComponent(l1);
		
		ResultDTO r = resultDTO.errors.get(0);
		
		String finalLib = getLib(r.regleSaisieDTO);
		Label l = new Label(finalLib,ContentMode.HTML);
		contentLayout.addComponent(l);
		
		contentLayout.addComponent(new Label("<br/>",ContentMode.HTML));
		
		Button b1 = new Button("Détails");
		b1.addClickListener(e->showDetails(r));
		contentLayout.addComponent(b1);	
	}

	private void displayMutipleErrors(VerticalLayout contentLayout) 
	{
		Label l1 = new Label("Ce contrat comporte des anomalies, merci de corriger les points suivants :",ContentMode.HTML);
		contentLayout.addComponent(l1);
		
		List<ResultDTO> regleSaisies = resultDTO.errors;
		for (int i = 0; i < regleSaisies.size(); i++) 
		{
			ResultDTO r = regleSaisies.get(i);
		
			HorizontalLayout hl = new HorizontalLayout();
			hl.setWidth("100%");
		
			String finalLib = getLib(r.regleSaisieDTO);
		
			Label l = new Label("<b>"+(i+1)+" :</b>"+finalLib,ContentMode.HTML);
			hl.addComponent(l);
			hl.setExpandRatio(l, 1f);
			
			
			Button b1 = new Button("Détails");
			b1.addClickListener(e->showDetails(r));
			hl.addComponent(b1);
			
			contentLayout.addComponent(hl);
			contentLayout.addComponent(new Label(""));
		}		
	}

	private String getLib(RegleSaisieDTO regleSaisieDTO) 
	{
		if (regleSaisieDTO.activateLibPersonnalise==ChoixOuiNon.OUI)
		{
			return regleSaisieDTO.libPersonnalise;
		}
		else
		{
			return regleSaisieDTO.libelle;
		}
	}

	private void showDetails(ResultDTO r) 
	{
		List<String> ls = new ArrayList<>();
		// Dans e cas d'un libellé personnalisé, on ajoute le libellé standard pour que cela soit plus clair 
		if (r.regleSaisieDTO.activateLibPersonnalise==ChoixOuiNon.OUI)
		{
			ls.add(r.regleSaisieDTO.libelle);
		}
		ls.addAll(r.msgs);
		new MessagePopup("Détails",ColorStyle.GREEN,ls).open();
	}

	@Override
	protected boolean performSauvegarder()
	{
		// Do nothing
		return true;
	}
	
	
	
	static public boolean performCheck(SaisieContratData data)
	{
		if (data.verifRegleSaisieDTO==null)
		{
			return true;
		}
		
		VerifRegleSaisieResultDTO res = data.verifRegleSaisieDTO.performCheck();
		if (res.isValid()==false)
		{
			new PopupRegleSaisieVisuErreur(res).open();
			return false;
		}
		return true;
	}

}
