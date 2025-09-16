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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.DateUtils;
import fr.amapj.common.FormatUtils;
import fr.amapj.model.models.contrat.modele.AffichageMontant;
import fr.amapj.model.models.contrat.modele.GestionDocEngagement;
import fr.amapj.model.models.contrat.modele.GestionPaiement;
import fr.amapj.model.models.contrat.modele.JokerMode;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.NatureContrat;
import fr.amapj.model.models.contrat.modele.RetardataireAutorise;
import fr.amapj.model.models.contrat.modele.SaisiePaiementProposition;
import fr.amapj.model.models.contrat.modele.StockGestion;
import fr.amapj.model.models.contrat.modele.StockIdentiqueDate;
import fr.amapj.model.models.contrat.modele.StockMultiContrat;
import fr.amapj.model.models.contrat.modele.StrategiePaiement;
import fr.amapj.model.models.contrat.modele.TypJoker;
import fr.amapj.model.models.fichierbase.Producteur;
import fr.amapj.model.models.param.ChoixOuiNon;
import fr.amapj.model.models.param.EtatModule;
import fr.amapj.model.models.param.paramecran.PEGestionContratsVierges;
import fr.amapj.model.models.param.paramecran.PEGestionContratsVierges.GestionJoker;
import fr.amapj.model.models.param.paramecran.PEGestionContratsVierges.GestionRetardataire;
import fr.amapj.service.services.gestioncontrat.DateModeleContratDTO;
import fr.amapj.service.services.gestioncontrat.DatePaiementModeleContratDTO;
import fr.amapj.service.services.gestioncontrat.ExtPModeleContratService;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.gestioncontrat.LigneContratDTO;
import fr.amapj.service.services.gestioncontrat.ModeleContratDTO;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.producteur.ProducteurService;
import fr.amapj.service.services.produit.ProduitService;
import fr.amapj.view.engine.collectioneditor.FieldType;
import fr.amapj.view.engine.menu.MenuList;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.popup.formpopup.fieldlink.FieldLink;
import fr.amapj.view.engine.popup.formpopup.validator.CollectionNoDuplicates;
import fr.amapj.view.engine.popup.formpopup.validator.CollectionSizeValidator;
import fr.amapj.view.engine.popup.formpopup.validator.ColumnNotNull;
import fr.amapj.view.engine.popup.formpopup.validator.DateRangeValidator;
import fr.amapj.view.engine.popup.formpopup.validator.IValidator;
import fr.amapj.view.engine.popup.formpopup.validator.IntegerRangeValidator;
import fr.amapj.view.engine.popup.formpopup.validator.NotNullValidator;
import fr.amapj.view.engine.popup.formpopup.validator.StringLengthValidator;
import fr.amapj.view.engine.popup.formpopup.validator.UniqueInDatabaseValidator;
import fr.amapj.view.engine.popup.formpopup.validator.ValidatorHolder;
import fr.amapj.view.engine.searcher.Searcher;
import fr.amapj.view.views.gestioncontrat.editorpart.utils.GestionContratDateUtils;
import fr.amapj.view.views.gestioncontrat.editorpart.utils.GestionContratPaiementUtils;
import fr.amapj.view.views.searcher.SDProduit;
import fr.amapj.view.views.searcher.SearcherList;

/**
 * Permet uniquement de creer des contrats
 */
public class GestionContratEditorPart extends WizardFormPopup
{
	private final static Logger logger = LogManager.getLogger();

	protected ModeleContratDTO modeleContrat;

	private boolean creerAPartirDeMode;
	
	private Long idCreerAPartir;
	
	private Searcher prod;
	
	private List<Producteur> allowedProducteurs;

	private PEGestionContratsVierges peEcran;
	
	
	@Override
	protected void configure()
	{
		ParametresDTO param = new ParametresService().getParametres();
		
		add(()->drawInfoGenerales(), ()->checkInfoGenerales());
		add(param.etatGestionCotisation==EtatModule.ACTIF,()->drawAccess());
		add(()->drawDateLivraison(), ()->checkDateLivraison());
		add(()->drawFinInscription(true), ()->checkJoker());
		add(()->drawChoixProduits());
		add(param.etatGestionStock==EtatModule.ACTIF,()->drawGestionStockGeneral());
		add(()->drawTypePaiement(null) , ()->checkTypePaiement());
		add(()->drawDetailPaiement(true));
		add(()->drawSignatureContrat());
	}

	

	public GestionContratEditorPart()
	{
		peEcran = (PEGestionContratsVierges) new ParametresService().loadParamEcran(MenuList.GESTION_CONTRAT);
	}
	
	/**
	 * 
	 */
	public GestionContratEditorPart(Long id,List<Producteur> allowedProducteurs)
	{
		this();
		
		this.allowedProducteurs = allowedProducteurs;
		
		setWidth(80);
		popupTitle = "Création d'un contrat";

		// Chargement de l'objet à créer
		// Si id est non null, alors on se sert de ce contenu pour précharger
		// les champs
		if (id == null)
		{
			modeleContrat = new ModeleContratDTO();
			modeleContrat.frequence = FrequenceLivraison.UNE_FOIS_PAR_SEMAINE;
			modeleContrat.gestionPaiement = GestionPaiement.NON_GERE;
			modeleContrat.affichageMontant = AffichageMontant.MONTANT_TOTAL;
			modeleContrat.nature = NatureContrat.LIBRE;
			modeleContrat.typJoker = TypJoker.SANS_JOKER;
			modeleContrat.jokerMode = JokerMode.INSCRIPTION;
			modeleContrat.stockGestion = StockGestion.NON;
			modeleContrat.gestionDocEngagement = GestionDocEngagement.AUCUNE_GESTION;
			
			creerAPartirDeMode = false;
		}
		else
		{
			modeleContrat = new GestionContratService().loadModeleContrat(id);
			modeleContrat.nom = modeleContrat.nom + "(Copie)";
			modeleContrat.id = null;
			
			//
			modeleContrat.dateLivs.clear();
			
			// Partie paiement
			modeleContrat.datePaiements.clear();
			
			//
			creerAPartirDeMode = true;
			idCreerAPartir = id;
		}
		
		setModel(modeleContrat);

	}
	
	

	private void drawInfoGenerales()
	{
		// Titre
		setStepTitle("les informations générales");
		
		// Liste des validators
		IValidator len_1_100 = new StringLengthValidator(1, 100);
		IValidator len_1_255 = new StringLengthValidator(1, 255);
		IValidator uniq = new UniqueInDatabaseValidator(ModeleContrat.class,"nom",null);
		IValidator notNull = new NotNullValidator();
		IValidator prodValidator = new ProducteurAvecProduitValidator();
		
		
		
		// Champ 1
		addTextField("Nom du contrat", "nom",len_1_100,uniq);


		// Champ 2
		addTextField("Description du contrat", "description",len_1_255);
		
		//
		addComboEnumField("Nature du contrat", "nature",notNull);

		// Champ 3
		prod = addSearcher("Producteur", "producteur", SearcherList.PRODUCTEUR, allowedProducteurs,notNull,prodValidator);
		// On ne peut pas changer le producteur quand on crée à partir d'un
		// autre contrat
		if (creerAPartirDeMode == true)
		{
			prod.setEnabled(false);
		}
		//
		addComboEnumField("Fréquence des livraisons", "frequence",notNull);
	}
	
	protected void drawAccess() 
	{
		setStepTitle("les conditions d'accès à ce contrat");
		
		IValidator[] validators = new IValidator[0];
		if (peEcran.periodeCotisationObligatoire==ChoixOuiNon.OUI)
		{
			validators = new IValidator[1];
			validators[0] = new NotNullValidator();
		}
		
		//
		addSearcher("Pour pouvoir souscrire à ce contrat, l'amapien doit être inscrit sur la période d'adhésion :", "idPeriodeCotisation", SearcherList.PERIODE_COTISATION, null, validators);
	}


	private String checkInfoGenerales()
	{
		if ((modeleContrat.nature==NatureContrat.CARTE_PREPAYEE) && (modeleContrat.frequence==FrequenceLivraison.UNE_SEULE_LIVRAISON))
		{
			return "Il n'est pas possible de faire un contrat Carte prépayée avec une seule date de livraison";
		}
		return null;
	}
	

	private void drawDateLivraison()
	{
		// Titre
		setStepTitle("les dates de livraison");
		
		// Liste des validators
		IValidator notNull = new NotNullValidator();

		
		if (modeleContrat.frequence==FrequenceLivraison.UNE_SEULE_LIVRAISON)
		{
			addDateField("Date de la livraison", "dateDebut",notNull);
		}
		else if (modeleContrat.frequence==FrequenceLivraison.AUTRE)
		{
			IValidator size = new CollectionSizeValidator<DateModeleContratDTO>(1, null);
			IValidator noDuplicates = new CollectionNoDuplicates<DateModeleContratDTO>(e->e.dateLiv);
								
			//
			addCollectionEditorField("Liste des dates", "dateLivs", DateModeleContratDTO.class,size,noDuplicates);
			addColumn("dateLiv", "Date",FieldType.DATE, null,new ColumnNotNull<DateModeleContratDTO>(e->e.dateLiv));			
		}
		else
		{
			addDateField("Date de la première livraison", "dateDebut",notNull);
			addDateField("Date de la dernière livraison", "dateFin",notNull);
		}
	}
	
	
	/**
	 * Verifie les dates de livraison et remplit le champ dateLivs
	 */
	private String checkDateLivraison()
	{
		String check = performCheckDateLivraison();
		if (check!=null)
		{
			return check;
		}
		else
		{
			new GestionContratDateUtils().computeDateLivraison(modeleContrat);
			return null;
		}
	}
	
	private String performCheckDateLivraison() 
	{
		if (modeleContrat.frequence==FrequenceLivraison.UNE_SEULE_LIVRAISON)
		{
			// C'est toujours bon 
			return null;
		}
		else if (modeleContrat.frequence==FrequenceLivraison.AUTRE)
		{
			// C'est toujours bon 
			return null;
		}
		else
		{
			if (modeleContrat.dateDebut.after(modeleContrat.dateFin))
			{
				return "La date de début doit être avant la date de fin ";
			}
			else
			{
				return null;
			}
		}
	}

	protected void drawFinInscription(boolean blocJoker)
	{
		if (modeleContrat.nature==NatureContrat.CARTE_PREPAYEE)
		{
			//
			modeleContrat.dateFinInscription = null;
			modeleContrat.retardataireAutorise = RetardataireAutorise.NON;
			
			
			// Titre
			setStepTitle("Contrat Carte prépayée - Délai pour modification du contrat");
			
			addIntegerField("Délai en jour pour modification du contrat avant livraison", "cartePrepayeeDelai");
			
			addHtml("Votre contrat est de type Carte prepayée, c'est à dire que l'adhérent peut modifier le contrat même après le début des livraisons.<br/>"
					+ "Ce champ vous permet d'indiquer le délai entre la dernière modification possible et la livraison<br/>"
					+ "Par exemple, si les livraisons sont le samedi et si vous mettez 2 dans ce champ, alors l'adhérent pourra alors modifier "
					+ "son contrat pour cette livraison jusqu'au mercredi soir minuit");
		}
		else
		{	
			// Titre
			setStepTitle("la date de fin des inscriptions");
			
			// Date de fin des inscription
			IValidator notNull = new NotNullValidator();
			Date firstLiv = modeleContrat.dateLivs.get(0).dateLiv;
			IValidator dateRange = new DateRangeValidator(null, firstLiv);
			String helpText = "Cette date de fin des inscriptions doit obligatoirement être avant la date de la première livraison (c'est à dire avant le "+FormatUtils.getStdDate().format(firstLiv)+")<br/><br/>"; 
			addDateField("Date de fin des inscriptions", "dateFinInscription",helpText,notNull,dateRange);
			
			addBr();
			
			// Retardataire 
			addBlocRetardataire();
			
			// Joker
			if (blocJoker==true && modeleContrat.nature==NatureContrat.ABONNEMENT)
			{
				addBr();
				addBlocGestionJoker();
			}
		}
	}
	
	
	protected void addBlocRetardataire()
	{
		// Pas de notion de retardataire dans le cas prépayée
		if (modeleContrat.nature==NatureContrat.CARTE_PREPAYEE)
		{
			return;
		}
		
		// Si une seule livraison : c'est toujours desactivé  
		if (modeleContrat.dateLivs.size()==1)
		{
			modeleContrat.retardataireAutorise = RetardataireAutorise.NON;
			String helpText = "Sur ce contrat, il n'est pas possible d'activer l'inscription en tant que retardataire car il y a une seule date de livraison"; 
			addComboEnumField("L'inscription en tant que retardataire est autorisée", "retardataireAutorise",helpText).setEnabled(false);
			return;
		}
				
		switch (peEcran.gestionRetardataire) 
		{
		case TOUJOURS_NON_AUTORISE:
			modeleContrat.retardataireAutorise = RetardataireAutorise.NON;
			String helpText1 = GestionRetardataire.MetaData.helpToujoursNonAutorise;
			addComboEnumField("L'inscription en tant que retardataire est autorisée", "retardataireAutorise",helpText1).setEnabled(false);
			return;
		
		case LIBRE_CHOIX:
			addComboEnumField("L'inscription en tant que retardataire est autorisée", "retardataireAutorise", new NotNullValidator());
			return;
			
		case TOUJOURS_AUTORISE:
			modeleContrat.retardataireAutorise = RetardataireAutorise.OUI;
			String helpText2 = GestionRetardataire.MetaData.helpToujoursAutorise;
			addComboEnumField("L'inscription en tant que retardataire est autorisée", "retardataireAutorise",helpText2).setEnabled(false);
			return;
			
		default:
			throw new AmapjRuntimeException();
		}
	}
	


	protected void addBlocGestionJoker()
	{
		
		
		//
		if (peEcran.gestionJoker==GestionJoker.TOUJOURS_NON) 
		{
			modeleContrat.typJoker =TypJoker.SANS_JOKER;
			addComboEnumField("Activer la gestion des jokers", "typJoker",GestionJoker.MetaData.helpToujoursNon).setEnabled(false);
			return;
		}
		
		TypJoker[] enumsToExcludes = computeEnumToExcludeJoker(peEcran.gestionJoker); 
		
		int delai = new ProducteurService().getDelaiNotification(modeleContrat.producteur);
		
		ComboBox b1 = addComboEnumField("Activer la gestion des jokers", "typJoker",  enumsToExcludes,new NotNullValidator());
		
		FieldLink f1 = new FieldLink(validatorManager,Arrays.asList(TypJoker.JOKER_ABSENCE,TypJoker.JOKER_REPORT),b1,true);
		
		f1.addField(addIntegerField("Nombre minimum de reports ou absences autorisés pour ce contrat", "jokerNbMin",new IntegerRangeValidator(0,null)),"0","0");
		
		f1.addField(addIntegerField("Nombre maximum de reports ou absences autorisés pour ce contrat", "jokerNbMax",new IntegerRangeValidator(0,null)),"1","0");
		
		
		ComboBox b2 = addComboEnumField("Choix des dates de jokers", "jokerMode",  f1.getValidator());
		FieldLink f2 = new FieldLink(validatorManager,Arrays.asList(JokerMode.LIBRE),b2,true);
		
		f1.addField(f2,JokerMode.INSCRIPTION,JokerMode.INSCRIPTION);
		
		f2.addField(addIntegerField("Délai de prévenance (en jours) pour modification des dates jokers", "jokerDelai",TypJoker.helpDelai),""+delai,"0");
		
		f1.doLink();
		
		addHelpButton("Aide sur les jokers", TypJoker.helpGeneral);
	}
	
	
	private TypJoker[] computeEnumToExcludeJoker(GestionJoker gestionJoker) 
	{
		switch (gestionJoker) 
		{
			case MODE_ABSENCE: return new  TypJoker[] { TypJoker.JOKER_REPORT };
			case MODE_REPORT: return new  TypJoker[] { TypJoker.JOKER_ABSENCE };
			case TOUT_POSSIBLE: return new  TypJoker[] { };
			default: throw new AmapjRuntimeException();
		}
	}

	protected String checkJoker()
	{
		if (modeleContrat.nature!=NatureContrat.ABONNEMENT)
		{
			return null;
		}
		
		// Pas de verification si les jokers ne sont pas activés
		// Nota : les valeurs jokerNbMin , jokerNbMax , jokerDelai et jokerMode sont bien remises à zéro par le fichier de base 
		if (modeleContrat.typJoker==TypJoker.SANS_JOKER)
		{
			return null;
		}
		
		if (modeleContrat.jokerNbMin>modeleContrat.jokerNbMax)
		{
			return "Le nombre de joker minimum ne peut pas être supérieur au nombre de joker maximum";
		}
		
		if (modeleContrat.jokerNbMax<1)
		{
			return "Le nombre de joker maximum doit être supérieur ou égal à 1";
		}
		
		
		// Verification du delai de prevenance
		if (modeleContrat.jokerMode==JokerMode.LIBRE)
		{
			int delai = new ProducteurService().getDelaiNotification(modeleContrat.producteur);
			
			if (modeleContrat.jokerDelai<delai)
			{
				return 	"Le délai de prévenance est trop court.<br/>"+
						"Dans la fiche producteur, il est indiqué que celui ci recoit par mail les feuilles de distribution "+delai+" jours avant la livraison<br/>"+
						"Par conséquent, vous ne pouvez pas autoriser la modification des jokers "+modeleContrat.jokerDelai+" jours avant la livraison<br/>";
			}
		}
		
		return null;
	}

	
	


	private void drawChoixProduits()
	{
		// Si liste vide
		Long idProducteur = (Long) prod.getConvertedValue();
		if (modeleContrat.produits.size()==0 && idProducteur!=null)
		{
			modeleContrat.produits.addAll(new GestionContratService().getInfoProduitModeleContrat(idProducteur));
		}
		
		
		// Titre
		setStepTitle("la liste des produits et des prix");
				
		// 
		
		IValidator size = new CollectionSizeValidator<LigneContratDTO>(1, null);
		IValidator noDuplicates = new CollectionNoDuplicates<LigneContratDTO>(e->e.produitId,e->new ProduitService().prettyString(e.produitId));
							
		//
		addCollectionEditorField("Produits", "produits", LigneContratDTO.class,size,noDuplicates);
		
		addColumnSearcher("produitId", "Nom du produit",FieldType.SEARCHER, null,new SDProduit(prod),prod,new ColumnNotNull<LigneContratDTO>(e->e.produitId));
		addColumn("prix", "Prix du produit", FieldType.CURRENCY, null,new ColumnNotNull<LigneContratDTO>(e->e.prix));	
		
	}
	
	
	protected void drawGestionStockGeneral()
	{
		// Titre
		setStepTitle("la gestion des limites en quantité - informations générales ");
		
		ComboBox b1 = addComboEnumField("Activer la gestion des limites en quantité pour ce contrat", "stockGestion",new NotNullValidator());
		
		FieldLink f1 = new FieldLink(validatorManager,Arrays.asList(StockGestion.OUI),b1,true);
		
		f1.addField(addComboEnumField("Le producteur fournit les mêmes quantités pour toutes les dates de livraison", "stockIdentiqueDate",f1.getValidator()),StockIdentiqueDate.OUI,null);
		
		f1.addField(addComboEnumField("Les quantités limites s'appliquent sur plusieurs contrats en même temps", "stockMultiContrat",f1.getValidator()),StockMultiContrat.NON,null);
			
		f1.addComponent(addHtml("Quand vous aurez validé votre contrat, vous pourrez saisir les quantités limites en cliquant sur le bouton Stock de l'écran REFERENT / Gestion des contrats vierges"));
		
		f1.doLink();
	}
	

	private List<Field<?>> paiementComponents;
	
	protected ComboBox boxPaiement;

	protected void drawTypePaiement(StrategiePaiement[] enumsToExcludes)
	{
		setStepTitle("genéralités sur le paiement");
			
		IValidator notNull = new NotNullValidator();
			
		boxPaiement = addComboEnumField("Mode de gestion des paiements", "strategiePaiement",enumsToExcludes,notNull);
		boxPaiement.addValueChangeListener(e->paiementChangeListener());
		boxPaiement.setWidth("500px");
		
		paiementComponents = new ArrayList<Field<?>>();
		paiementChangeListener();
		
	}
	
	private void paiementChangeListener() 
	{
		//
		paiementComponents.forEach(e->suppressElement(e));
		paiementComponents.clear();
		
		//
		StrategiePaiement st = (StrategiePaiement) boxPaiement.getValue();
		if (st==null)
		{
			return;
		}
		switch (st) 
		{
		case NON_GERE:
		case UN_PAIEMENT_PAR_LIVRAISON:
			// Nothing to do 
			break;
			
		case UN_CHEQUE_PAR_MOIS_LISSE_MODIFIABLE_1ER_MOIS:
		case UN_CHEQUE_PAR_MOIS_LISSE_NON_MODIFIABLE_1ERE_LIVRAISON:
		case UN_CHEQUE_PAR_MOIS_LISSE_MODIFIABLE_1ERE_LIVRAISON:
			paiementComponents.add(addCurrencyField("Montant minimum des chèques", "montantChequeMiniCalculProposition", false));
			break;
		
		case PLUS_DE_CHOIX:
			Field<?> c = addComboEnumField("Mode de calcul de la proposition de paiement", "saisiePaiementProposition",new NotNullValidator());
			paiementComponents.add(c);
			
			FieldLink f1 = new FieldLink(validatorManager,Arrays.asList(SaisiePaiementProposition.PAYE_AVANCE_ARRONDI,SaisiePaiementProposition.REPARTI_ARRONDI),(ComboBox) c);
			
			c = addCurrencyField("Montant minimum des chèques", "montantChequeMiniCalculProposition",false);
			paiementComponents.add(c);
			
			f1.addField((AbstractField<?>) c);
			
			c = addComboEnumField("La proposition de paiement est elle modifiable ? ", "saisiePaiementModifiable",new NotNullValidator());
			paiementComponents.add(c);
			
			c = addComboEnumField("Mode de calcul des dates de paiements ", "saisiePaiementCalculDate", new NotNullValidator());
			paiementComponents.add(c);
			break;

		default:
			throw new AmapjRuntimeException();
		}
		
	}

	protected String checkTypePaiement()
	{
		// On remplit tous les champs
		new GestionContratPaiementUtils().fillFieldPaiementFromStrategie(modeleContrat);
			
		// On calcule les dates
		new GestionContratDateUtils().computeDatePaiement(modeleContrat);
		
		//
		return null;
	}
	
	
	protected void drawDetailPaiement(boolean activeDates)
	{
		//
		setStepTitle("les informations complémentaires sur le paiement");
		
		// Liste des validators
		IValidator len_0_2048 = new StringLengthValidator(0, 2048);
		IValidator len_0_255 = new StringLengthValidator(0, 255);
		IValidator notNull = new NotNullValidator();

		
		if (modeleContrat.gestionPaiement==GestionPaiement.GESTION_STANDARD)
		{	
			// 
			modeleContrat.affichageMontant = null; 
			
			addTextField("Ordre du chèque", "libCheque",len_0_255);
			
			PopupDateField p = addDateField("Date limite de remise des chèques", "dateRemiseCheque",notNull);
			Date firstPaiement = computeDateRemiseCheque();
			p.setValue(firstPaiement);
			
			if (activeDates)
			{	
				IValidator size = new CollectionSizeValidator<DatePaiementModeleContratDTO>(1, null);
				IValidator noDuplicates = new CollectionNoDuplicates<DatePaiementModeleContratDTO>(e->e.datePaiement);
									
				//
				addCollectionEditorField("Liste des dates de paiement", "datePaiements", DatePaiementModeleContratDTO.class,size,noDuplicates);
				addColumn("datePaiement", "Date",FieldType.DATE, null,new ColumnNotNull<DatePaiementModeleContratDTO>(e->e.datePaiement));			
			}
			
		}
		else
		{
			// A la création d'un contrat de type carte prépayée, on propose le mode MONTANT_PROCHAINE_LIVRAISON
			if (modeleContrat.id==null && modeleContrat.nature==NatureContrat.CARTE_PREPAYEE)
			{
				modeleContrat.affichageMontant = AffichageMontant.MONTANT_PROCHAINE_LIVRAISON;
			}
			
			addComboEnumField("Montant affiché dans la fenêtre de paiement", "affichageMontant", notNull);
			
			TextField f = (TextField) addTextField("Texte affiché dans la fenêtre paiement", "textPaiement",len_0_2048);
			f.setMaxLength(2048);
			f.setHeight(5, Unit.CM);
		}
	}	
	

	private Date computeDateRemiseCheque() 
	{
		if (modeleContrat.datePaiements.size()==0)
		{
			return null;
		}
		return DateUtils.addDays(modeleContrat.datePaiements.get(0).datePaiement,-10);
	}
	
	// 

	protected void drawSignatureContrat() 
	{
		// Titre
		setStepTitle("la gestion des documents d'engagement et leurs signatures");
		
		ComboBox b1 = addComboEnumField("Gestion des documents d'engagement", "gestionDocEngagement",new NotNullValidator());
		
		FieldLink f1 = new FieldLink(validatorManager,Arrays.asList(GestionDocEngagement.SIGNATURE_EN_LIGNE,GestionDocEngagement.GENERATION_DOCUMENT_SEUL),b1,true);
		
		f1.addField(addSearcher("Document d'engagement (à signer) ", "idEngagement", SearcherList.ENGAGEMENT ,null,f1.getValidator()));
		
		f1.doLink();
	}
	

	@Override
	protected void performSauvegarder() throws OnSaveException
	{
		Long idNewContrat = new GestionContratService().saveNewModeleContrat(modeleContrat);
		
		if (idCreerAPartir!=null)
		{
			new ExtPModeleContratService().copyExtendedParam(idCreerAPartir,idNewContrat);
		}			
	}


	/**
	 * Validateur qui vérifie que le producteur posséde au moins un produit
	 */
	public class ProducteurAvecProduitValidator implements IValidator
	{

		@Override
		public void performValidate(Object value, ValidatorHolder a)
		{
			Long p = (Long) value;
			if (p!=null)
			{
				List<LigneContratDTO> ligs = new GestionContratService().getInfoProduitModeleContrat(p);
				if (ligs.size()==0)
				{
					a.addMessage("Ce producteur ne posséde pas de produits.");
					a.addMessage("Pour pouvoir créer un contrat pour ce producteur");
					a.addMessage("Vous devez d'abord aller dans le menu \"Gestion des produits\",");
					a.addMessage("et indiquer la liste des produits faits par ce producteur.");
				}
			}
		}

		@Override
		public boolean canCheckOnFly()
		{
			return true;
		}
		
		@Override
		public AbstractField[] revalidateOnChangeOf()
		{
			return null;
		}
	}
	
	
}
