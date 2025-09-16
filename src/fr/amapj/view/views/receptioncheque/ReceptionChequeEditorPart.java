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
 package fr.amapj.view.views.receptioncheque;

import java.text.SimpleDateFormat;
import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextField;

import fr.amapj.common.CollectionUtils;
import fr.amapj.common.FormatUtils;
import fr.amapj.model.models.contrat.reel.EtatPaiement;
import fr.amapj.model.models.param.ChoixOuiNon;
import fr.amapj.model.models.param.paramecran.PEReceptionCheque;
import fr.amapj.service.services.mespaiements.reception.ReceptionChequeDTO;
import fr.amapj.service.services.mespaiements.reception.ReceptionPaiementsService;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.popup.formpopup.FormPopup;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.tools.table.complex.ComplexTableBuilder;

/**
 * Popup pour la réception des chèques (soit d'un contrat, soit en masse)
 *  
 */
public class ReceptionChequeEditorPart extends FormPopup
{
	static public enum Mode
	{
		// Réception des cheques d'un contrat d'un adherent
		CONTRAT,
		
		// Recepetion de tous les chèques d'un modele de contrat (multiples adherents)
		MODELE_CONTRAT;
	}
	
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
	
	
	private SimpleDateFormat df = FormatUtils.getLiteralMonthDate();
	
	private Long idContrat;
	
	private String nomUtilisateur;
	
	private String prenomUtilisateur;
	
	private List<ReceptionChequeDTO> paiements;
	
	private PEReceptionCheque peConf;

	private ComplexTableBuilder<ReceptionChequeDTO> builder;

	private Mode mode;

	private Long idModeleContrat;
	
	private SortMode sortMode;

	private Button tri;
		
		
	/**
	 * Réception pour un contrat
	 */
	public ReceptionChequeEditorPart(Long idContrat, String nomUtilisateur, String prenomUtilisateur)
	{
		super();
		this.mode = Mode.CONTRAT;
		this.idContrat = idContrat;
		this.nomUtilisateur = nomUtilisateur;
		this.prenomUtilisateur = prenomUtilisateur;
		
		popupTitle = "Réception chèques";
		setHeight("90%");
		
	}
	
	
	/**
	 * Réception en masse pour un modele de contrat
	 */
	public ReceptionChequeEditorPart(Long idModeleContrat)
	{
		super();
		this.idModeleContrat = idModeleContrat;
		this.mode = Mode.MODELE_CONTRAT;
		
		popupTitle = "Réception des chèques en masse";
		setHeight("100%");
		
	}

	

	@Override
	protected void addFields()
	{
		peConf = (PEReceptionCheque) new ParametresService().loadParamEcran(MenuList.RECEPTION_CHEQUES);
		if (mode==Mode.CONTRAT)
		{
			paiements = new ReceptionPaiementsService().getPaiementAReceptionnerContrat(idContrat);
		}
		else
		{
			paiements = new ReceptionPaiementsService().getPaiementAReceptionnerModeleContrat(idModeleContrat);
		}
		
		sortMode = SortMode.NOM;
		sortPaiement();
		
		// Premiere ligne de texte
		String msg;
		if (mode==Mode.CONTRAT)
		{
			msg = "<h2> Réception des chèques de "+prenomUtilisateur+" "+nomUtilisateur+"</h2>";
		}
		else
		{
			msg = "<h2>Réception en masse des chèques</h2>";
		}
		addHtml(msg);
		
		if(paiements.size()==0)
		{
			addHtml("Il n'y a pas de chèques à réceptionner.");
			setWidth(60);
			return;
		}
		
		
		builder = new ComplexTableBuilder<ReceptionChequeDTO>(paiements);
		
		if (mode==Mode.MODELE_CONTRAT)
		{
			builder.addString("Nom", false, 150, e->e.nomUtilisateur);
			builder.addString("Prenom", false, 150, e->e.prenomUtilisateur);
		}
		
		builder.addString("Date", false, 150, e->df.format(e.datePaiement));
		builder.addCurrency("Montant €", false, 150,  e->e.montant);
		builder.addCheckBox("Cocher la case si le chèque a été reçu à l'AMAP","cb",true,150,e->e.etatPaiement==EtatPaiement.AMAP,null);
		
		if (peConf.saisieCommentaire1==ChoixOuiNon.OUI)
		{
			builder.addString(peConf.libSaisieCommentaire1, "c1",true,200,e->e.commentaire1);
		}
		if (peConf.saisieCommentaire2==ChoixOuiNon.OUI)
		{
			builder.addString(peConf.libSaisieCommentaire2, "c2",true,200,e->e.commentaire2);
		}
		if (peConf.saisieCommentaire3==ChoixOuiNon.OUI)
		{
			builder.addString(peConf.libSaisieCommentaire3, "c3",true,200,e->e.commentaire3);
		}
		if (peConf.saisieCommentaire4==ChoixOuiNon.OUI)
		{
			builder.addString(peConf.libSaisieCommentaire4, "c4",true,200,e->e.commentaire4);
		}
		
		addComplexTable(builder);
		setWidth(60,100+builder.getTotalWidth());
	
	}
	


	private void sortPaiement() 
	{
		if (sortMode==SortMode.NOM)
		{
			CollectionUtils.sort(paiements, e->e.nomUtilisateur,e->e.prenomUtilisateur,e->e.datePaiement);
		}
		else
		{
			CollectionUtils.sort(paiements, e->e.datePaiement,e->e.nomUtilisateur,e->e.prenomUtilisateur);
		}
	}


	@Override
	protected void createButtonBar()
	{
		if (mode==Mode.MODELE_CONTRAT)
		{
			tri = addButton("Trier par date", e->handleTri());
		}
		
		addButtonBlank();
		addButton("J'ai bien reçu tous les chèques", e->handleToutSelectionner());
		
		super.createButtonBar();
	}
	
	
	private void handleTri() 
	{
		sortMode = sortMode.toogle();
		tri.setCaption(sortMode==SortMode.DATE ? "Trier par nom" : "Trier par date");
		writeToModel();
		sortPaiement();
		builder.reload(paiements);
	}


	protected void handleToutSelectionner()
	{
		for (int i = 0; i <paiements.size(); i++)
		{
			CheckBox tf = (CheckBox) builder.getComponent(i, "cb");
			tf.setValue(Boolean.TRUE);
		}
		
	}



	@Override
	protected void performSauvegarder() throws OnSaveException
	{
		writeToModel();
		
		new ReceptionPaiementsService().receptionCheque(paiements);
	}

	/**
	 * Copie les données saisies dans le modele
	 */
	private void writeToModel() 
	{
		for (int i = 0; i < paiements.size(); i++)
		{
			ReceptionChequeDTO paiement = paiements.get(i);
						
			// case à cocher
			CheckBox cb = (CheckBox)  builder.getComponent(i, "cb");
			if (cb.getValue().booleanValue()==true)
			{
				paiement.etatPaiement=EtatPaiement.AMAP;
			}
			else
			{
				paiement.etatPaiement=EtatPaiement.A_FOURNIR;
			}
			
			// Commentaire 1
			if (peConf.saisieCommentaire1==ChoixOuiNon.OUI)
			{
				TextField tf = (TextField) builder.getComponent(i, "c1");
				paiement.commentaire1 = tf.getValue();
			}	
			
			// Commentaire 2
			if (peConf.saisieCommentaire2==ChoixOuiNon.OUI)
			{
				TextField tf = (TextField) builder.getComponent(i, "c2");
				paiement.commentaire2 = tf.getValue();
			}
			
			// Commentaire 3
			if (peConf.saisieCommentaire3==ChoixOuiNon.OUI)
			{
				TextField tf = (TextField) builder.getComponent(i, "c3");
				paiement.commentaire3 = tf.getValue();
			}	
			
			// Commentaire 4
			if (peConf.saisieCommentaire4==ChoixOuiNon.OUI)
			{
				TextField tf = (TextField) builder.getComponent(i, "c4");
				paiement.commentaire4 = tf.getValue();
			}
		}
		
	}
	
}
