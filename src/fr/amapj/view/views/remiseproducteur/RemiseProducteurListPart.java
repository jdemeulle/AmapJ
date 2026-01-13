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

 package fr.amapj.view.views.remiseproducteur;

import java.text.SimpleDateFormat;
import java.util.List;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.HorizontalLayout;

import fr.amapj.service.services.edgenerator.excel.EGRemise;
import fr.amapj.service.services.remiseproducteur.PaiementRemiseDTO;
import fr.amapj.service.services.remiseproducteur.RemiseDTO;
import fr.amapj.service.services.remiseproducteur.RemiseProducteurService;
import fr.amapj.view.engine.excelgenerator.TelechargerPopup;
import fr.amapj.view.engine.listpart.ButtonType;
import fr.amapj.view.engine.listpart.StandardListPart;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.corepopup.CorePopup.ColorStyle;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;
import fr.amapj.view.engine.popup.suppressionpopup.SuppressionPopup;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;
import fr.amapj.view.views.common.contratselector.ContratSelectorPart;


/**
 * Gestion des remises 
 */
@SuppressWarnings("serial")
public class RemiseProducteurListPart extends StandardListPart<RemiseDTO>
{
	private ContratSelectorPart contratSelectorPart;

	public RemiseProducteurListPart()
	{
		super(RemiseDTO.class,false);
	}
	
	@Override
	protected String getTitle() 
	{
		return "Liste des remises de chèques aux producteurs";
	}


	@Override
	protected void drawButton() 
	{		
		addButton("Faire une remise de chèques",ButtonType.ALWAYS,e->handleAjouter());
		addButton("Visualiser une remise",ButtonType.EDIT_MODE,e->handleVoir());
		addButton("Télécharger ...",ButtonType.EDIT_MODE,e->handleTelecharger());
		addButton("Supprimer une remise",ButtonType.EDIT_MODE,e->handleSupprimer());
	}

	@Override
	protected void addSelectorComponent()
	{
		// Partie choix du contrat
		contratSelectorPart = new ContratSelectorPart(this,true);
		HorizontalLayout toolbar1 = contratSelectorPart.getChoixContratComponent();
		
		addComponent(toolbar1);
		
		contratSelectorPart.fillAutomaticValues();
	}
	

	@Override
	protected void drawTable() 
	{
		addColumn("moisRemise","Date de remise prévue au contrat");
		addColumnDateTime("dateCreation","Date de saisie de la remise");
		addColumnDate("dateReelleRemise","Date réelle de la remise");
		addColumnCurrency("mnt","Montant (en €)").right();
	}



	@Override
	protected List<RemiseDTO> getLines() 
	{
		Long idModeleContrat = contratSelectorPart.getModeleContratId();
		if (idModeleContrat==null)
		{
			return null;
		}
		return new RemiseProducteurService().getAllRemise(idModeleContrat);
	}


	@Override
	protected String[] getSortInfos() 
	{
		return new String[] { "dateTheoRemise" };
	}
	
	protected String[] getSearchInfos()
	{
		return null;
	}
	

	private TelechargerPopup handleTelecharger()
	{
		RemiseDTO remiseDTO = getSelectedLine();
		TelechargerPopup popup = new TelechargerPopup("Remise à un producteur");
		popup.addGenerator(new EGRemise(remiseDTO.id));
		return popup;
	}



	private MessagePopup handleVoir()
	{
		RemiseDTO remiseDTO = getSelectedLine();
		String str = formatRemise(remiseDTO.id);
		return new MessagePopup("Visualisation d'une remise", ContentMode.HTML, ColorStyle.GREEN,str);	
	}
	
	

	private CorePopup handleSupprimer()
	{
		RemiseDTO remiseDTO = getSelectedLine();
		String text = "Êtes-vous sûr de vouloir supprimer la remise du "+remiseDTO.moisRemise+" ?";
		return new SuppressionPopup(text,remiseDTO.id,true,e->new RemiseProducteurService().deleteRemise(e));
	}


	private CorePopup handleAjouter()
	{
		Long idModeleContrat = contratSelectorPart.getModeleContratId();
		RemiseDTO remiseDTO = new RemiseProducteurService().prepareRemise(idModeleContrat);
		if (remiseDTO.preparationRemiseFailed==false)
		{
			return new RemiseEditorPart(remiseDTO);
		}
		else
		{
			return new MessagePopup("Impossible de faire la remise.", ContentMode.HTML,ColorStyle.RED,"Il n'est pas possible de faire la remise à cause de :",remiseDTO.messageRemiseFailed);
		}
	}
	
	
	/**
	 * Formatage de la remise pour affichage dans un popup
	 * @param remiseDTO
	 * @return
	 */
	private String formatRemise(Long remiseId)
	{
		RemiseDTO remiseDTO = new RemiseProducteurService().loadRemise(remiseId);
				
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		StringBuffer buf = new StringBuffer();
		
		buf.append("Remise de chèques du "+remiseDTO.moisRemise+"<br/>");
		buf.append(remiseDTO.nbCheque+" chèques dans cette remise<br/>");
		buf.append("Montant total :  "+new CurrencyTextFieldConverter().convertToString(remiseDTO.mnt)+" € <br/><br/>");
		buf.append("Date de création :  "+df.format(remiseDTO.dateCreation)+"<br/>");
		buf.append("Date réelle de remise :  "+df.format(remiseDTO.dateReelleRemise)+"<br/><br/>");
		
		for (PaiementRemiseDTO paiement : remiseDTO.paiements)
		{
			String text = paiement.nomUtilisateur+" "+paiement.prenomUtilisateur+" - "+new CurrencyTextFieldConverter().convertToString(paiement.montant)+" € ";
			text = add(text,paiement.commentaire1);
			text = add(text,paiement.commentaire2);
			text = add(text,paiement.commentaire3);
			text = add(text,paiement.commentaire4);
			
			
			text = text+"<br/>";
			buf.append(text);
		}
		
		
		return buf.toString();
	}
	
	private String  add(String text, String commentaire) 
	{
		if (commentaire!=null)
		{
			return text+" - "+commentaire;
		}
		else
		{
			return text;
		}
	}

	
}
