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
 package fr.amapj.view.views.saisiecontrat.abo.joker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.common.FormatUtils;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.mescontrats.ContratLigDTO.AboLigStatus;
import fr.amapj.service.services.stockservice.verifstock.VerifStockService;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;
import fr.amapj.view.engine.popup.okcancelpopup.OKCancelPopup;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.SaisieContratData;
import fr.amapj.view.views.saisiecontrat.abo.ContratAbo;
import fr.amapj.view.views.saisiecontrat.abo.ContratAbo.DateJokerInfo;
import fr.amapj.view.views.saisiecontrat.step1qte.abo.ContratAboManager;
import fr.amapj.view.views.saisiecontrat.step1qte.utils.checkonend.CheckOnEndSaisieQte;
import fr.amapj.view.views.saisiecontrat.step1qte.utils.checkonend.PopupRegleSaisieVisuErreur;

/**
 * Popup pour la gestion des jokers, en mode ABSENCE
 *  
 */
public class PopupSaisieJokerAbsence extends OKCancelPopup
{
	// 
	private ContratAbo abo;
	
	private List<ComboBox> combos;
	
	private List<DateJokerInfo> dateJokerInfos;
	
	private ContratDTO contratDTO;
	
	private Label titre;

	private boolean readOnly;

	private SaisieContratData data;

	/**
	 * 
	 */
	public PopupSaisieJokerAbsence(SaisieContratData data,boolean readOnly)
	{
		this.data = data;
		this.readOnly = readOnly;
		this.abo = data.contratDTO.contratAbo;
		this.contratDTO = data.contratDTO;
		
		combos = new ArrayList<>();
		dateJokerInfos = abo.getAllDates();
		
		popupTitle = "Gestion des jokers de mon contrat "+contratDTO.nom;
				
		if (readOnly)
		{
			hasSaveButton = false;
			cancelButtonTitle = "OK";
		}
		else
		{
			saveButtonTitle = "OK";
		}
	}
	
	
	@Override
	protected void createContent(VerticalLayout contentLayout)
	{
		String msg = getTitre(abo.getNbJokersUsed());
		
		titre = new Label(msg,ContentMode.HTML);
		contentLayout.addComponent(titre);
		
		int nb = Math.max(contratDTO.jokerNbMax,getNbDateJoker());
		
		GridLayout gl = new GridLayout(3,nb);
		gl.setMargin(true);
		gl.setSpacing(true);
		
		SimpleDateFormat df = FormatUtils.getFullDate();
		
		
		for (int i = 0; i < nb; i++)
		{
			DateJokerInfo dateJoker = getDateJoker(i);
			
			//
			Label l1 = new Label("Joker "+(i+1));
			l1.setWidth("80px");
			gl.addComponent(l1, 0, i);
			
			if (readOnly)
			{
				if (dateJoker==null)
				{
					gl.addComponent(new Label(""), 1, i);
					gl.addComponent(new Label(" (Non Utilisé)"), 2, i);
				}
				else
				{
					gl.addComponent(new Label(df.format(dateJoker.date)), 1, i);
					gl.addComponent(new Label(""), 2, i);
				}
			}
			else
			{
				if (dateJoker==null || dateJoker.isModifiable) 
				{
					ComboBox box = createComboBox(dateJoker,df);
					box.setWidth("220px");
					gl.addComponent(box, 1, i);
					combos.add(box);
					gl.addComponent(new Label(""), 2, i);
				}
				else
				{
					gl.addComponent(new Label(df.format(dateJoker.date)), 1, i);
					gl.addComponent(new Label(" (Non modifiable)"), 2, i);
				}
			}
		}
		contentLayout.addComponent(gl);
		
	}
	
	
	/**
	 * Retourne le nombre de date joker 
	 */
	private int getNbDateJoker() 
	{
		return (int) dateJokerInfos.stream().filter(e->e.status==AboLigStatus.JOKER).count();
	}
	

	/**
	 * Retourne la date qui est joker en position i 
	 */
	private DateJokerInfo getDateJoker(int i) 
	{
		return dateJokerInfos.stream().filter(e->e.status==AboLigStatus.JOKER).skip(i).findFirst().orElse(null);
	}

	private String getTitre(int nbJokers)
	{
		return new ContratAboManager().computeJokerMessage(contratDTO, nbJokers);
	}


	private ComboBox createComboBox(DateJokerInfo dateJoker, SimpleDateFormat df)
	{
		ComboBox comboBox = new ComboBox();
		comboBox.setImmediate(true);
		
		for (DateJokerInfo dateJokerInfo : dateJokerInfos)
		{
			// On ajoute uniquement les dates qui sont modifiables 
			if (dateJokerInfo.isModifiable)
			{
				String caption = df.format(dateJokerInfo.date);
				comboBox.addItem(dateJokerInfo);
				comboBox.setItemCaption(dateJokerInfo, caption);	
			}
		}

		if (dateJoker!=null)
		{
			comboBox.select(dateJoker);
		}
		
		comboBox.addValueChangeListener(e->updateTitre());
		
		return comboBox;
	}


	private void updateTitre()
	{
		int nbJokers = (int) combos.stream().filter(e->e.getValue()!=null).count();
		titre.setValue(getTitre(nbJokers));
	}


	@Override
	protected boolean performSauvegarder() throws OnSaveException
	{
		// On remet à zero le statut de toutes les dates jokers modifiables qui ont été proposées
		for (DateJokerInfo dateJokerInfo : dateJokerInfos) 
		{
			if (dateJokerInfo.isModifiable && dateJokerInfo.status==AboLigStatus.JOKER)
			{
				dateJokerInfo.status = AboLigStatus.NORMAL;
			}
		}
		
		// On met à jour le statut de celles qui ont été saisies
		for (ComboBox comboBox : combos)
		{
			DateJokerInfo lig = (DateJokerInfo) comboBox.getValue();
			if (lig!=null)
			{
				lig.status = AboLigStatus.JOKER;
			}
		}
		abo.updateJokers(dateJokerInfos);
		
		// On verifie les regles de fin de saisie des quantités 
		if (new CheckOnEndSaisieQte().check(data)==false)
		{
			return false;
		}
		
		data.validate();
		
		return true;
	}

	
}
