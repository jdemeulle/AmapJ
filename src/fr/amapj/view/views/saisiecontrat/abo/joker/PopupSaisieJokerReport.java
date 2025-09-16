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

import fr.amapj.common.AmapjRuntimeException;
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
 * Popup pour la gestion des jokers, en mode REPORT
 *  
 */
public class PopupSaisieJokerReport extends OKCancelPopup
{
	// 
	private ContratAbo abo;
	
	private List<Ligne> lignes;
	
	private List<DateJokerInfo> dateJokerInfos;
	
	private ContratDTO contratDTO;
	
	private Label titre;

	private boolean readOnly;

	private SaisieContratData data;

	/**
	 * 
	 */
	public PopupSaisieJokerReport(SaisieContratData data,boolean readOnly)
	{
		this.data = data;
		this.readOnly = readOnly;
		this.abo = data.contratDTO.contratAbo;
		this.contratDTO = data.contratDTO;
		
		lignes = new ArrayList<>();
		dateJokerInfos = abo.getAllDates();
		
		popupTitle = "Gestion des jokers de mon contrat "+contratDTO.nom;
		setWidth(60);
				
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
		
		int nb = Math.max(contratDTO.jokerNbMax,getNbDateJokerInitial());
		
		GridLayout gl = new GridLayout(4,nb);
		gl.setMargin(true);
		gl.setSpacing(true);
		
		if (readOnly)
		{
			drawReadOnly(gl,nb);
		}
		else
		{
			drawEditable(gl,nb); 
		}
		
		contentLayout.addComponent(gl);	
	}
	
	
	private void drawEditable(GridLayout gl, int nb) 
	{
		for (int i = 0; i < nb; i++)
		{
			DateJokerInfo dateJoker = getDateJoker(i);
			
			//
			Label l1 = new Label("Joker "+(i+1));
			l1.setWidth("80px");
			gl.addComponent(l1, 0, i);
			
			Ligne l = new Ligne();
			
			addCol1(l,gl,dateJoker,i);
			gl.addComponent(new Label("Reporté au "), 2, i);
			DateJokerInfo dateJokerDestination = findDestination(dateJoker);
			addCol2(l,gl,dateJokerDestination,i);
			lignes.add(l);
		}
		
	}


	private DateJokerInfo findDestination(DateJokerInfo dateJoker) 
	{
		if (dateJoker==null || dateJoker.reportDateDestination==null)
		{
			return null;
		}
		for (DateJokerInfo dateJokerInfo : dateJokerInfos) 
		{
			if (dateJokerInfo.date.equals(dateJoker.reportDateDestination))
			{
				return dateJokerInfo;
			}
		}
		throw new AmapjRuntimeException();
	}


	private void addCol1(Ligne l, GridLayout gl, DateJokerInfo dateJoker,int index) 
	{
		if (dateJoker!=null && dateJoker.isModifiable==false)
		{
			SimpleDateFormat df = FormatUtils.getFullDate();
			gl.addComponent(new Label(df.format(dateJoker.date)), 1, index);
			l.initialCol1 = dateJoker;
		}
		else
		{
			ComboBox box = createComboBox(dateJoker);
			box.setWidth("220px");
			gl.addComponent(box, 1, index);
			l.box1 = box;
		}
	}
	
	private void addCol2(Ligne l, GridLayout gl, DateJokerInfo dateJoker,int index) 
	{
		if (dateJoker!=null && dateJoker.isModifiable==false)
		{
			SimpleDateFormat df = FormatUtils.getFullDate();
			gl.addComponent(new Label(df.format(dateJoker.date)), 3, index);
			l.initialCol2 = dateJoker;
		}
		else
		{
			ComboBox box = createComboBox(dateJoker);
			box.setWidth("220px");
			gl.addComponent(box, 3, index);
			l.box2 = box;
		}
	}


	private void drawReadOnly(GridLayout gl,int nb) 
	{
		SimpleDateFormat df = FormatUtils.getFullDate();
		
		for (int i = 0; i < nb; i++)
		{
			DateJokerInfo dateJoker = getDateJoker(i);
			
			//
			Label l1 = new Label("Joker "+(i+1));
			l1.setWidth("80px");
			gl.addComponent(l1, 0, i);
			
			
			if (dateJoker==null)
			{
				gl.addComponent(new Label(" Non Utilisé"), 1, i);
			}
			else
			{
				gl.addComponent(new Label(df.format(dateJoker.date)), 1, i);
				gl.addComponent(new Label("Reporté au "), 2, i);
				gl.addComponent(new Label(df.format(dateJoker.reportDateDestination)), 3, i);
			}
		}
	}


	/**
	 * Retourne le nombre de date joker 
	 */
	private int getNbDateJokerInitial() 
	{
		return (int) dateJokerInfos.stream().filter(e->e.status==AboLigStatus.REPORT).count();
	}
	

	/**
	 * Retourne la date qui est joker en position i 
	 */
	private DateJokerInfo getDateJoker(int i) 
	{
		return dateJokerInfos.stream().filter(e->e.status==AboLigStatus.REPORT).skip(i).findFirst().orElse(null);
	}

	private String getTitre(int nbJokers)
	{
		return new ContratAboManager().computeJokerMessage(contratDTO, nbJokers);
	}


	private ComboBox createComboBox(DateJokerInfo dateJoker)
	{
		SimpleDateFormat df = FormatUtils.getFullDate();
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
		int nbJokers = (int) lignes.stream().filter(e->isComplete(e)).count();
		titre.setValue(getTitre(nbJokers));
	}


	private boolean isComplete(Ligne l) 
	{
		return l.getVal1()!=null && l.getVal2()!=null;
	}


	@Override
	protected boolean performSauvegarder() throws OnSaveException
	{
		// Verification de la coherence
		for (int i = 0; i < lignes.size(); i++) 
		{
			Ligne l = lignes.get(i);
			if ( (l.getVal1()==null && l.getVal2()!=null) || (l.getVal1()!=null && l.getVal2()==null)) 
			{
				throw new OnSaveException("La ligne "+(i+1)+" est incorrecte");
			}
		}
		
		// On enleve toutes les lignes REPORT
		for (DateJokerInfo dateJokerInfo : dateJokerInfos) 
		{
			if (dateJokerInfo.status==AboLigStatus.REPORT)
			{
				dateJokerInfo.status = AboLigStatus.NORMAL;
				dateJokerInfo.reportDateDestination = null;
			}
		}
		
		// On relit la vue pour la copier dans le modele
		for (Ligne l : lignes) 
		{			
			DateJokerInfo dateJokerInfo = l.getVal1();
			if (dateJokerInfo!=null)
			{
				dateJokerInfo.status = AboLigStatus.REPORT;
				dateJokerInfo.reportDateDestination = l.getVal2().date;
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

	
	static public class Ligne
	{
		public DateJokerInfo initialCol1;
		
		public ComboBox box1;
		
		public DateJokerInfo initialCol2;
		
		public ComboBox box2;
		
		public DateJokerInfo getVal1()
		{
			if (box1!=null)
			{
				return (DateJokerInfo) box1.getValue();
			}
			else
			{
				return initialCol1;
			}
		}
		
		public DateJokerInfo getVal2()
		{
			if (box2!=null)
			{
				return (DateJokerInfo) box2.getValue();
			}
			else
			{
				return initialCol2;
			}
		}
	}
	
}
