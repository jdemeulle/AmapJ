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
 package fr.amapj.view.views.gestioncontrat.editorpart;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.TextField;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.models.contrat.modele.GestionPaiement;
import fr.amapj.model.models.contrat.modele.NatureContrat;
import fr.amapj.model.models.contrat.modele.TypJoker;
import fr.amapj.model.models.param.EtatModule;
import fr.amapj.service.services.gestioncontrat.DateModeleContratDTO;
import fr.amapj.service.services.gestioncontrat.DatePaiementModeleContratDTO;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.gestioncontrat.LigneContratDTO;
import fr.amapj.service.services.gestioncontrat.ModeleContratDTO;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.collectioneditor.FieldType;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.searcher.Searcher;
import fr.amapj.view.views.gestioncontrat.editorpart.utils.GestionContratPaiementUtils;
import fr.amapj.view.views.searcher.SDProduit;
import fr.amapj.view.views.searcher.SearcherList;

/**
 * Permet de visualiserles contrats
 */
public class ModeleContratVisualiserPart extends WizardFormPopup
{

	private ModeleContratDTO modeleContrat;

	private Searcher prod;
	
	
	@Override
	protected void configure()
	{
		add(()->drawInfoGenerales());
		add(()->drawAccess());
		add(()->drawDateLivraison());
		add(()->drawFinInscription());
		add(()->drawChoixProduits());
		add(()->drawTypePaiement());
		add(()->drawDetailPaiement());
		add(()->drawSignatureContrat());
	}
	

	/**
	 * 
	 */
	public ModeleContratVisualiserPart(Long id)
	{
		setWidth(80);
		popupTitle = "Visualisation d'un modèle de contrat";

		modeleContrat = new GestionContratService().loadModeleContrat(id);
		setModel(modeleContrat);
		
		saveButtonTitle = "OK";
	}
	
	

	private void drawInfoGenerales()
	{
		// Titre
		setStepTitle("les informations générales");
		
		addTextField("Nom du contrat", "nom");

		addTextField("Description du contrat", "description");

		addComboEnumField("Nature du contrat", "nature");

		prod = addSearcher("Producteur", "producteur", SearcherList.PRODUCTEUR,null);
		
		addComboEnumField("Fréquence des livraisons", "frequence");
		
		setReadOnlyAll();
	}
	
	protected void drawAccess() 
	{
		setStepTitle("les conditions d'accès à ce contrat");
		
		ParametresDTO param = new ParametresService().getParametres();
		if (param.etatGestionCotisation==EtatModule.INACTIF)
		{
			addHtml("Ce contrat sera accessible par tous les amapiens. Vous pouvez cliquer sur Etape suivante");
			return;
		}
		
		//
		addSearcher("Pour pouvoir souscrire à ce contrat, l'amapien doit être inscrit sur la période d'adhésion :", "idPeriodeCotisation", SearcherList.PERIODE_COTISATION, null);
		
		setReadOnlyAll();
	}



	private void drawDateLivraison()
	{
		// Titre
		setStepTitle("les dates de livraison");
	
		//
		addCollectionEditorField("Liste des dates", "dateLivs", DateModeleContratDTO.class);
		addColumn("dateLiv", "Date",FieldType.DATE, null);		
		
		setReadOnlyAll();

	}
	
	
	protected void drawFinInscription()
	{
		if (modeleContrat.nature==NatureContrat.CARTE_PREPAYEE)
		{
			// Titre
			setStepTitle("Contrat Carte prépayée - Délai pour modification du contrat");
			
			addIntegerField("Délai en jour pour modification du contrat avant livraison", "cartePrepayeeDelai");
		}
		else
		{	
			// Titre
			setStepTitle("la date de fin des inscriptions");
			
			// Champ 4
			addDateField("Date de fin des inscriptions", "dateFinInscription");
			
			addComboEnumField("L'inscription en tant que retardataire est autorisé", "retardataireAutorise");
			
			if (modeleContrat.nature==NatureContrat.ABONNEMENT)
			{
				addHtml("<br/>");
				addBlocGestionJoker();
			}
		}
		
		setReadOnlyAll();
	}
	
	
	protected void addBlocGestionJoker()
	{
		addComboEnumField("Activer la gestion des jokers", "typJoker");
		
		if (modeleContrat.typJoker!=TypJoker.SANS_JOKER)
		{
			
			addIntegerField("Nombre minimum de reports ou absences autorisés pour ce contrat", "jokerNbMin");
			
			addIntegerField("Nombre maximum de reports ou absences autorisés pour ce contrat", "jokerNbMax");
			
			addComboEnumField("Choix des dates de jokers", "jokerMode");
			
			addIntegerField("Délai de prévenance (en jours) pour modifiation des dates jokers", "jokerDelai");
		}
		
		setReadOnlyAll();
	}
	
	



	private void drawChoixProduits()
	{
		// Titre
		setStepTitle("la liste des produits et des prix");
				
		//
		addCollectionEditorField("Produits", "produits", LigneContratDTO.class);
		
		addColumnSearcher("produitId", "Nom du produit",FieldType.SEARCHER, null,new SDProduit(prod),prod);
		addColumn("prix", "Prix du produit", FieldType.CURRENCY, null);	
		
		setReadOnlyAll();
	}

	
	
	
	protected void drawTypePaiement()
	{
		new GestionContratPaiementUtils().checkAndUpdateStrategieAndSaisiePaiementCalculDate(modeleContrat);
		
		setStepTitle("genéralités sur le paiement");
			
		addComboEnumField("Mode de gestion des paiements", "strategiePaiement").setWidth("500px");
		
		switch (modeleContrat.strategiePaiement) 
		{
		case NON_GERE:
		case UN_PAIEMENT_PAR_LIVRAISON:
			// Nothing to do 
			break;
			
		case UN_CHEQUE_PAR_MOIS_LISSE_MODIFIABLE_1ER_MOIS:
		case UN_CHEQUE_PAR_MOIS_LISSE_NON_MODIFIABLE_1ERE_LIVRAISON:
		case UN_CHEQUE_PAR_MOIS_LISSE_MODIFIABLE_1ERE_LIVRAISON:
			addCurrencyField("Montant minimum des chèques", "montantChequeMiniCalculProposition", false);
			break;
		
		case PLUS_DE_CHOIX:
			addComboEnumField("Mode de calcul de la proposition de paiement", "saisiePaiementProposition");
			addCurrencyField("Montant minimum des chèques", "montantChequeMiniCalculProposition",false);
			addComboEnumField("La proposition de paiement est elle modifiable ? ", "saisiePaiementModifiable");
			addComboEnumField("Mode de calcul des dates de paiements ", "saisiePaiementCalculDate");
			break;

		default:
			throw new AmapjRuntimeException();
		}
		
		setReadOnlyAll();
	}
	
	private void drawDetailPaiement()
	{
		setStepTitle("les informations sur le paiement");
		
		
		if (modeleContrat.gestionPaiement==GestionPaiement.GESTION_STANDARD)
		{	
			addTextField("Ordre du chèque", "libCheque");
			
			addDateField("Date limite de remise des chèques", "dateRemiseCheque");
			
			//
			addCollectionEditorField("Liste des dates de paiements", "datePaiements", DatePaiementModeleContratDTO.class);
			addColumn("datePaiement", "Date",FieldType.DATE, null);		
		}
		else
		{
			TextField f = (TextField) addTextField("Texte affiché dans la fenêtre paiement", "textPaiement");
			f.setMaxLength(2048);
			f.setHeight(5, Unit.CM);
		}
		
		setReadOnlyAll();
	}
	
	private  void drawSignatureContrat() 
	{
		// Titre
		setStepTitle("la gestion des documents d'engagement et leurs signatures");
		
		addComboEnumField("Gestion des documents d'engagement", "gestionDocEngagement");
		
		if (modeleContrat.idEngagement!=null)
		{
			addSearcher("Document d'engagement (à signer) ", "idEngagement", SearcherList.ENGAGEMENT ,null);
		}
		
		setReadOnlyAll();
	}
	



	@Override
	protected void performSauvegarder()
	{
	}
}
