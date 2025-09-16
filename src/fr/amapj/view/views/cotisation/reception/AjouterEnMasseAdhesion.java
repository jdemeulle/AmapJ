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
 package fr.amapj.view.views.cotisation.reception;

import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;

import fr.amapj.common.CollectionUtils;
import fr.amapj.model.models.cotisation.EtatPaiementAdhesion;
import fr.amapj.model.models.cotisation.TypePaiementAdhesion;
import fr.amapj.service.services.gestioncotisation.GestionCotisationService;
import fr.amapj.service.services.gestioncotisation.PeriodeCotisationDTO;
import fr.amapj.service.services.gestioncotisation.PeriodeCotisationUtilisateurDTO;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.popup.swicthpopup.SwitchPopup;
import fr.amapj.view.engine.tools.table.complex.ComplexTableBuilder;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;

/**
 * Popup pour la creation en masse des cotisations des adherents
 *  
 */
public class AjouterEnMasseAdhesion extends WizardFormPopup
{
	private List<PeriodeCotisationUtilisateurDTO> lines;
	
	private ComplexTableBuilder<PeriodeCotisationUtilisateurDTO> builder;

	private PeriodeCotisationDTO periode;

		
	/**
	 * Réception pour un contrat
	 */
	public AjouterEnMasseAdhesion(Long idPeriodeCotisation)
	{
		super();
			
		popupTitle = "Création en masse des adhésions";
		setHeight("100%");
		setWidth(100);
		
		lines = new GestionCotisationService().getAllForAjouterEnMasse(idPeriodeCotisation);
		CollectionUtils.sort(lines,e->e.nomUtilisateur,e->e.prenomUtilisateur);
		
		periode = new GestionCotisationService().load(idPeriodeCotisation);

		
	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldInfo());
		add(()->addFieldSaisie(),()->writeToModel());
	}
	
	protected void addFieldInfo()
	{
		String str = 	"Cet écran permet de créer en masse les adhésions<br/><br/>"+
						"Dans un cas standard, cet écran n'est pas nécessaire : en effet, les adhérents créent eux même leur adhésion en allant dans Mes Adhésions<br/><br/>"+
						"Cet écran est utile surtout quand vous mettez en place AMAPJ dans votre AMAP, pour créer toutes les adhésions déjà existantes<br/><br/>"+
						"Dans l'écran suivant, si vous remplissez le champ montant, alors l'adhésion sera créée<br/>"+
						"Toutes les lignes avec montant=0 seront ignorées";
		
		addHtml(str);
	}

	protected void addFieldSaisie()
	{
		if(lines.size()==0)
		{
			addHtml("Il n'y a pas de cotisation à créer");
			setWidth(60);
			return;
		}
		
		builder = new ComplexTableBuilder<PeriodeCotisationUtilisateurDTO>(lines);
		
		builder.addString("Nom", false, 200, e->e.nomUtilisateur);
		builder.addString("Prenom", false, 200, e->e.prenomUtilisateur);
		builder.addCurrency("Montant", "mnt", true, 100, e->e.montantAdhesion);
		builder.addCombo("Etat du paiement", "etat", true, 200, e->e.etatPaiementAdhesion , EtatPaiementAdhesion.class);
		builder.addCombo("Type du paiement", "type", true, 200, e->e.typePaiementAdhesion , TypePaiementAdhesion.class);
		builder.setPageLength(11);
		
		addComplexTable(builder);
		
		
		Button b = new Button("Mettre toutes colonnes à ...",e->handleToutMontant());
		form.addComponent(b);
	}
	

	private void handleToutMontant() 
	{
		SwitchPopup popup = new SwitchPopup("Actions possibles",60);
		
		popup.addLineAction("Positionner toute la colonne Montant avec la valeur "+new CurrencyTextFieldConverter().convertToString(periode.montantConseille)+" €",()->colonneMontant());
		popup.addSeparator();
		popup.addLineAction("Positionner toute la colonne Etat du paiement avec la valeur A FOURNIR",()->colonneEtatPaiement(EtatPaiementAdhesion.A_FOURNIR));
		popup.addLineAction("Positionner toute la colonne Etat du paiement avec la valeur ENCAISSE",()->colonneEtatPaiement(EtatPaiementAdhesion.ENCAISSE));
		popup.addSeparator();
		popup.addLineAction("Positionner toute la colonne Type du paiement avec la valeur CHEQUE",()->colonneTypePaiement(TypePaiementAdhesion.CHEQUE));
		popup.addLineAction("Positionner toute la colonne Type du paiement avec la valeur ESPECES",()->colonneTypePaiement(TypePaiementAdhesion.ESPECES));
		popup.addLineAction("Positionner toute la colonne Type du paiement avec la valeur VIREMENT",()->colonneTypePaiement(TypePaiementAdhesion.VIREMENT));
		popup.addLineAction("Positionner toute la colonne Type du paiement avec la valeur INTERNET",()->colonneTypePaiement(TypePaiementAdhesion.INTERNET));
		
		popup.open();
		
	}

	
	private void colonneTypePaiement(TypePaiementAdhesion type) 
	{
		for (int i = 0; i < lines.size(); i++)
		{ 
			ComboBox cb = (ComboBox) builder.getComponent(i, "type");	
			cb.setValue(type);
		}
	}
	
	private void colonneEtatPaiement(EtatPaiementAdhesion etat) 
	{
		for (int i = 0; i < lines.size(); i++)
		{ 
			ComboBox cb = (ComboBox) builder.getComponent(i, "etat");	
			cb.setValue(etat);
		}
	}

	private void colonneMontant() 
	{
		for (int i = 0; i < lines.size(); i++)
		{ 
			TextField cb = (TextField) builder.getComponent(i, "mnt");	
			cb.setConvertedValue(periode.montantConseille);
		}
	}


	@Override
	protected void performSauvegarder() throws OnSaveException
	{
		for (PeriodeCotisationUtilisateurDTO pcu : lines) 
		{
			if (pcu.montantAdhesion!=0)
			{
				new GestionCotisationService().createOrUpdateCotisation(true,pcu);
			}
		}	
	}

	/**
	 * Copie les données saisies dans le modele
	 */
	private String writeToModel() 
	{
		String str = "";
		for (int i = 0; i < lines.size(); i++)
		{
			PeriodeCotisationUtilisateurDTO line = lines.get(i);
						
			TextField mnt = (TextField)  builder.getComponent(i, "mnt");
			ComboBox etat = (ComboBox) builder.getComponent(i, "etat");
			ComboBox type = (ComboBox) builder.getComponent(i, "type");	
			
			
			line.montantAdhesion = (Integer) mnt.getConvertedValue();
			line.etatPaiementAdhesion = (EtatPaiementAdhesion) etat.getValue();
			line.typePaiementAdhesion = (TypePaiementAdhesion) type.getValue();
			
			if (line.montantAdhesion!=0)
			{
				if (line.etatPaiementAdhesion==null || line.typePaiementAdhesion==null)
				{
					str = str+"Erreur à la ligne "+line.nomUtilisateur+" "+line.prenomUtilisateur+" : Etat du paiement ou Type du paiement n'est pas renseigné<br/>";
				}
			}
		}
		
		return str.length()==0 ? null : str;
	}
	
}
