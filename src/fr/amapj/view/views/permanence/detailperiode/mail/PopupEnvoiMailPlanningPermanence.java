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
 package fr.amapj.view.views.permanence.detailperiode.mail;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.RichTextArea;

import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.permanence.periode.mail.PeriodePermanenceEnvoiMailService;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.popup.formpopup.genericmodel.StringItem;

/**
 * Popup pour l'envoi d'un mail avec le planning de permanence
 */
public class PopupEnvoiMailPlanningPermanence extends WizardFormPopup
{
	
	private Long idPeriodePermanence;
	
	private StringItem dto;

	/**
	 * 
	 */
	public PopupEnvoiMailPlanningPermanence(Long idPeriodePermanence)
	{
		setWidth(80);
		popupTitle = "Envoi d'un mail avec le planning des permanences";
		this.idPeriodePermanence = idPeriodePermanence;
 
		// Contruction de l'item
		dto = new StringItem();
		dto.value = getInitialText();
		setModel(dto);
	}
	
	

	@Override
	protected void configure()
	{
		add(()->addFieldInfoGenerales());
		add(()->addFieldTexteMail());
	}

	private void addFieldInfoGenerales()
	{
		// Titre
		setStepTitle("l'envoi d'un mail avec le planning  des permanences");
		
		addHtml("Avec cet outil,  vous allez pouvoir envoyer un mail avec le planning pour toutes les personnes ayant des permanences dans le futur");

	}

	private void addFieldTexteMail()
	{
		// Titre
		setStepTitle("le texte du mail");
		
	
		//
		RichTextArea f =  addRichTextAeraField("Texte du mail", "value");
		f.setHeight(10, Unit.CM);
		
		
		

	}

	

	@Override
	protected void performSauvegarder()
	{
		new PeriodePermanenceEnvoiMailService().sendMailAvecPlanning(dto.value, idPeriodePermanence);
	}
	
	private String getInitialText()
	{
		String lineSep="<br/>";
		
		ParametresDTO param = new ParametresService().getParametres();
		
		StringBuffer buf = new StringBuffer();
		buf.append("<h2>"+param.nomAmap+"</h2>");
		buf.append("Bonjour , ");
		buf.append(lineSep);
		buf.append(lineSep);
		buf.append("vous trouverez ci joint le planning de distribution pour l'année à venir.");
		buf.append(lineSep);
		buf.append(lineSep);
		buf.append("En ce qui vous concerne, vos dates de permanences sont :");
		buf.append(lineSep);
		buf.append("#DATES#");
		buf.append(lineSep);
		buf.append(lineSep);
		buf.append("Nous comptons sur votre participation active !!!");
		buf.append(lineSep);
		buf.append("Si vous n'êtes pas disponible à une date, merci d'échanger avec un autre AMAPIEN");
		buf.append(lineSep);
		buf.append("Pour cela, prenez contact avec un autre amapien, une fois que vous avez son accord, merci de corriger le planning affiché à l'AMAP (en barrant au stylo)");
		buf.append(lineSep);
		buf.append(lineSep);
		buf.append(lineSep);
		buf.append("Notez que vous pouvez désormais consulter le planning des permanences sur l'application WEB de l'AMAP");
		buf.append(lineSep);
		buf.append("#LINK#");
		buf.append(lineSep);
		buf.append(lineSep);
		buf.append("Bonne journée à tous !!");
		buf.append(lineSep);
		return buf.toString();
	}
	
	
}
