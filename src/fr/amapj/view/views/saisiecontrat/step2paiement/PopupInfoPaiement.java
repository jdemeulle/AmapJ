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
 package fr.amapj.view.views.saisiecontrat.step2paiement;



import java.text.SimpleDateFormat;
import java.util.Date;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.common.DateUtils;
import fr.amapj.common.FormatUtils;
import fr.amapj.model.models.contrat.modele.AffichageMontant;
import fr.amapj.service.services.mescontrats.ContratLigDTO;
import fr.amapj.service.services.mescontrats.InfoPaiementDTO;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;
import fr.amapj.view.views.saisiecontrat.SaisieContrat;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.ModeSaisie;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.SaisieContratData;

/**
 * Popup d'informations sur le paiement
 *  
 */
public class PopupInfoPaiement extends CorePopup
{
	private SimpleDateFormat df = FormatUtils.getStdDate();
	private InfoPaiementDTO paiementDTO;
	
	private SaisieContratData data;
	
	
	public PopupInfoPaiement(SaisieContratData data)
	{
		super();
		this.data = data;
		this.paiementDTO = data.contratDTO.paiement;
		
		popupTitle = "Information sur les paiements pour le contrat "+data.contratDTO.nom;
		setWidth(50);
	}

	protected void createContent(VerticalLayout contentLayout)
	{
		addMontantPart(contentLayout);
		
		contentLayout.addComponent(new Label(paiementDTO.textPaiement));
		
		boolean readOnly = (data.modeSaisie == ModeSaisie.READ_ONLY); 
		
		if (readOnly==false)
		{
			String str = "<br/><br/>Veuillez maintenant cliquer sur "+data.getLibSaveButton()+" pour valider votre contrat, " +
				"ou sur Annuler si vous ne souhaitez pas conserver ce contrat<br/><br/>";
		
			contentLayout.addComponent(new Label(str,ContentMode.HTML));
		}
	}
	


	private void addMontantPart(VerticalLayout contentLayout) 
	{
		if (paiementDTO.affichageMontant==AffichageMontant.AUCUN_AFFICHAGE)
		{
			return;
		}
		
		// Si une seul date de livraison : mode simple 
		if (data.contratDTO.contratLigs.size()==1)
		{
			String mnt = getMontantTotalLib();
			contentLayout.addComponent(new Label(mnt,ContentMode.HTML));
			return;
		}
		
		// Si plusieurs dates de livraison : ajout d'un bouton pour avoir le détail 
		
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		
		String mnt = computeLibMontant();
		Label l = new Label(mnt,ContentMode.HTML);
		hl.addComponent(l);
		hl.setExpandRatio(l, 1f);
		
		Button details = new Button("Détails");
		details.addClickListener(e->handleDetails());
		hl.addComponent(details);
		
		contentLayout.addComponent(hl);
	}
	

	private String computeLibMontant() 
	{
		if (paiementDTO.affichageMontant==AffichageMontant.MONTANT_TOTAL)
		{
			return getMontantTotalLib();
		}
		
		Date nextDatLiv = getNextDateLiv();
		
		if (nextDatLiv!=null)
		{
			return "<b>Montant de la prochaine livraison ("+df.format(nextDatLiv)+")  : "+FormatUtils.prix(data.contratDTO.getMontantOf(nextDatLiv))+"</b><br/><br/>";
		}
		
		Date lastDateLiv = getLastDateLiv();
		
		return "<b>Montant de la dernière livraison ("+df.format(lastDateLiv)+")  : "+FormatUtils.prix(data.contratDTO.getMontantOf(lastDateLiv))+"</b><br/><br/>";
		
	}
	
	
	
	private Date getLastDateLiv() 
	{
		return data.contratDTO.contratLigs.get(data.contratDTO.contratLigs.size()-1).date;
	}

	private Date getNextDateLiv() 
	{
		Date ref = DateUtils.getDateWithNoTime();
		for (ContratLigDTO contratLigDTO : data.contratDTO.contratLigs) 
		{
			if (contratLigDTO.date.before(ref)==false)
			{
				return contratLigDTO.date;
			}
		}
		return null;
	}

	private String getMontantTotalLib()
	{
		return "<b>Montant total du contrat : "+FormatUtils.prix(data.contratDTO.getMontantTotal())+"</b><br/><br/>";
	}
	
	private void handleDetails() 
	{
		String msg = computeDetails();
		new MessagePopup("Détails du paiement par date",ContentMode.HTML,ColorStyle.GREEN,msg).open();
	}
	
	

	private String computeDetails() 
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < data.contratDTO.contratLigs.size(); i++)
		{
			ContratLigDTO lig = data.contratDTO.contratLigs.get(i);
			sb.append("Montant commandé pour le "+df.format(lig.date)+" : ");
			int mnt = 0;
			
			for (int j = 0; j < data.contratDTO.contratColumns.size(); j++)
			{
				int prix = data.contratDTO.contratColumns.get(j).prix;
				mnt = mnt + data.contratDTO.cell[i][j].qte * prix;
			}
			sb.append("<b>"+FormatUtils.prix(mnt)+"</b><br/>");
		}
		
		//
		sb.append("<br/>");
		sb.append("Motant total du contrat : ");
		sb.append("<b>"+FormatUtils.prix(data.contratDTO.getMontantTotal())+"</b><br/>");
		
		return sb.toString();
	}

	protected void createButtonBar()
	{
		addButtonBlank();
		if (data.modeSaisie == ModeSaisie.READ_ONLY)
		{
			addDefaultButton("OK", e->handleAnnuler());
		}
		else
		{
			addButton("Annuler", e->handleAnnuler());
			addDefaultButton(data.getLibSaveButton(), e->	handleSauvegarder());
		}

	}
	

	protected void handleAnnuler()
	{
		close();
	}
	
	
	public void handleSauvegarder()
	{
		// On memorise la validation  
		data.validate();

		//
		close();
	}
	
}
