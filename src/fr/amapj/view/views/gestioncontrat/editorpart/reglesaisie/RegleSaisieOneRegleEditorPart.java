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
 package fr.amapj.view.views.gestioncontrat.editorpart.reglesaisie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.rits.cloning.Cloner;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.IdContainer;
import fr.amapj.model.models.contrat.modele.extendparam.reglesaisie.RSChampApplication;
import fr.amapj.model.models.contrat.modele.extendparam.reglesaisie.RSContrainteDate;
import fr.amapj.model.models.contrat.modele.extendparam.reglesaisie.RSContrainteProduit;
import fr.amapj.model.models.param.ChoixOuiNon;
import fr.amapj.service.services.gestioncontrat.reglesaisie.RegleSaisieDTO;
import fr.amapj.service.services.gestioncontrat.reglesaisie.RegleSaisieModeleContratDTO;
import fr.amapj.service.services.gestioncontrat.reglesaisie.RegleSaisieModeleContratService;
import fr.amapj.view.engine.collectioneditor.CollectionEditor;
import fr.amapj.view.engine.collectioneditor.FieldType;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.popup.formpopup.fieldlink.FieldLink;
import fr.amapj.view.engine.popup.formpopup.validator.CollectionNoDuplicates;
import fr.amapj.view.engine.popup.formpopup.validator.CollectionSizeValidator;
import fr.amapj.view.engine.popup.formpopup.validator.ColumnNotNull;
import fr.amapj.view.engine.popup.formpopup.validator.IValidator;
import fr.amapj.view.engine.popup.formpopup.validator.NotNullValidator;
import fr.amapj.view.views.searcher.SDModeleContratDate;
import fr.amapj.view.views.searcher.SDProduitOneProducteur;

/**
 * Permet de modifier une regle de saisie
 * 
 *
 */
public class RegleSaisieOneRegleEditorPart extends WizardFormPopup
{
	private RegleSaisieDTO regleSaisieDTO;
	
	private RegleSaisieDTO regleSaisieInitial;

	private RegleSaisieModeleContratDTO dto;
	
	/**
	 * 
	 */
	public RegleSaisieOneRegleEditorPart( RegleSaisieModeleContratDTO dto,RegleSaisieDTO regleSaisieInitial)
	{
		this.dto = dto;
		this.regleSaisieInitial = regleSaisieInitial;
		
		if (regleSaisieInitial==null)
		{
			this.regleSaisieDTO = new RegleSaisieDTO();
			this.regleSaisieDTO.activateLibPersonnalise = ChoixOuiNon.NON;
			this.regleSaisieDTO.champApplication = RSChampApplication.AMAPIEN;
			popupTitle = "Ajout d'une règle de saisie";
		}
		else
		{
			this.regleSaisieDTO = new Cloner().deepClone(regleSaisieInitial);
			popupTitle = "Modification d'une règle de saisie";
		}
		
		setWidth(50);
		setModel(regleSaisieDTO);
	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldDate());
		add(()->addFieldProd());
		add(()->addFieldOperateur());
		add(()->addFieldLibelle(),()->checkLibelle());
		add(()->addFieldApplication());
		
	}
	
	// LES DATES 


	private List<Field<?>> dateComponents = new ArrayList<>();
	private ComboBox dateBox;
	
	
	private void addFieldDate()
	{	
		setStepTitle("Les dates");
		
		dateBox = addComboEnumField("Filtre date", "contrainteDate", new NotNullValidator());
		dateBox.addValueChangeListener(e->refreshFieldDate());
		
		dateComponents.clear();
		refreshFieldDate();
		
	}
	
	private void refreshFieldDate() 
	{
		//
		dateComponents.forEach(e->suppressElement(e));
		dateComponents.clear();
		
		//
		RSContrainteDate st = (RSContrainteDate) dateBox.getValue();
		if (st==null)
		{
			return;
		}
		
		
		switch (st) 
		{
		case POUR_CHAQUE_DATE:
		case POUR_TOUT_CONTRAT:
			// Nothing to do 
			break;
			
		case POUR_UNE_DATE:
			dateComponents.add(addSearcher("Date", "modeleContratDateId", new SDModeleContratDate(dto.idModeleContrat), null, new NotNullValidator())); 
			break;
		
		case POUR_PLUSIEURS_DATES:
			IValidator size = new CollectionSizeValidator<IdContainer>(1, null);
			IValidator noDuplicates = new CollectionNoDuplicates<IdContainer>(e->e.id);
			
			CollectionEditor<IdContainer> f1 = new CollectionEditor<IdContainer>("Dates", (BeanItem) getModelAsItem() , "modeleContratDateIds", IdContainer.class);
			f1.addSearcherColumn("id", "Date",FieldType.SEARCHER, null,new SDModeleContratDate(dto.idModeleContrat),null);

			addCollectionEditorFieldToForm(f1, "Dates", "modeleContratDateIds", size,noDuplicates);
			addValidatorColumnSearcher("id", "Date", new ColumnNotNull<IdContainer>(e->e.id));
			
			dateComponents.add(f1);
			break;
			
		default:
			throw new AmapjRuntimeException();
		}
		
	}


	// LES PRODUITS 
	

	private List<Field<?>> prodComponents = new ArrayList<>();
	private ComboBox prodBox;
	
	
	private void addFieldProd()
	{	
		setStepTitle("Les produits");
		
		prodBox = addComboEnumField("Filtre produit", "contrainteProduit", new NotNullValidator());
		prodBox.addValueChangeListener(e->refreshFieldProd());
		
		prodComponents.clear();
		refreshFieldProd();
		
	}
	
	private void refreshFieldProd() 
	{
		//
		prodComponents.forEach(e->suppressElement(e));
		prodComponents.clear();
		
		//
		RSContrainteProduit st = (RSContrainteProduit) prodBox.getValue();
		if (st==null)
		{
			return;
		}
		
		
		switch (st) 
		{
		case POUR_TOUS_PRODUITS:
			// Nothing to do 
			break;
			
		case POUR_UN_PRODUIT:
			prodComponents.add(addSearcher("Produit", "produitId", new SDProduitOneProducteur(dto.idProducteur), null, new NotNullValidator())); 
			break;
		
		case POUR_PLUSIEURS_PRODUITS:
			IValidator size = new CollectionSizeValidator<IdContainer>(1, null);
			IValidator noDuplicates = new CollectionNoDuplicates<IdContainer>(e->e.id);
			
			CollectionEditor<IdContainer> f1 = new CollectionEditor<IdContainer>("Produits", (BeanItem) getModelAsItem() , "produitIds", IdContainer.class);
			f1.addSearcherColumn("id", "Produit",FieldType.SEARCHER, null,new SDProduitOneProducteur(dto.idProducteur),null);

			addCollectionEditorFieldToForm(f1, "Produits", "produitIds", size,noDuplicates);
			addValidatorColumnSearcher("id", "Produit", new ColumnNotNull<IdContainer>(e->e.id));
			
			prodComponents.add(f1);
			break;
			
		default:
			throw new AmapjRuntimeException();
		}
		
	}

	

	
	
	
	private void addFieldOperateur()
	{	
		setStepTitle("Operateur");
	
		addComboEnumField("Operateur", "contrainteOperateur", new NotNullValidator());
		
		addIntegerField("Valeur", "val");
		
	}
	
	
	private void addFieldLibelle()
	{	
		setStepTitle("Libellé personnalisé");
		
		// Mise à jour du libellé de la règle 
		regleSaisieDTO.libelle = new RegleSaisieModeleContratService().getLib(regleSaisieDTO);
		
		addHtml("<b>Libellé de la règle</b>");
		addHtml(regleSaisieDTO.libelle);
	
		ComboBox b1 = addComboEnumField("Saisir un libellé personnalisé", "activateLibPersonnalise", new NotNullValidator());
		FieldLink f1 = new FieldLink(validatorManager,Arrays.asList(ChoixOuiNon.OUI),b1,true);
		f1.addField(addCKEditorFieldForLabel("", "libPersonnalise"));
		f1.doLink();
		
	}
	
	
	private String checkLibelle() 
	{
		if (regleSaisieDTO.activateLibPersonnalise==ChoixOuiNon.NON)
		{
			return null;
		}
		
		String s = regleSaisieDTO.libPersonnalise;
		if (s==null || s.length()==0 || isEmptyHtml(s))
		{
			return "Vous devez saisir un libellé personnalisé ou choisir Saisir un libellé personnalisé=NON";
		}
		return null;
	}

	
	private boolean isEmptyHtml(String s) 
	{
		s = s.replaceAll("<br />","");
		s = s.replaceAll("\r","");
		s = s.replaceAll("\n","");
		s = s.replaceAll("&nbsp;","");
		
		return s.length()==0;
	}

	private void addFieldApplication()
	{	
		setStepTitle("Champ d'application");
		
		addComboEnumField("Champ d'application", "champApplication", new NotNullValidator());
				
	}
	
	
	

	@Override
	protected void performSauvegarder()
	{
		if (regleSaisieInitial==null)
		{
			dto.regleSaisies.add(regleSaisieDTO);	
		}
		else
		{
			Cloner cloner=new Cloner();		
			cloner.copyPropertiesOfInheritedClass(regleSaisieDTO,regleSaisieInitial);
		}
	}
}
