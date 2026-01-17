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
 package fr.amapj.view.views.producteur.basicform;

import java.text.SimpleDateFormat;

import com.vaadin.data.util.BeanItem;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.TextArea;

import fr.amapj.common.FormatUtils;
import fr.amapj.model.models.param.EtatModule;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.producteur.ProdUtilisateurDTO;
import fr.amapj.service.services.producteur.ProducteurDTO;
import fr.amapj.service.services.producteur.ProducteurService;
import fr.amapj.view.engine.collectioneditor.CollectionEditor;
import fr.amapj.view.engine.collectioneditor.FieldType;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.popup.formpopup.validator.NotNullValidator;
import fr.amapj.view.views.searcher.SearcherList;

/**
 * La fiche producteur 
 * 
 *
 */
public class ProducteurVoirPart extends WizardFormPopup
{

	private ProducteurDTO producteurDTO;


	/**
	 * 
	 */
	public ProducteurVoirPart(ProducteurDTO p)
	{	
		setWidth(80);
		setHeight("90%");
		
		saveButtonTitle = "OK";
		popupTitle = "Visualisation d'un producteur";
		this.producteurDTO = p;
		
		setModel(this.producteurDTO);

	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldGeneral());
		add(()->addFieldDocuments());
		add(()->addFieldUtilisateur());
		add(()->addFieldReferents());
	}

	private void addFieldGeneral()
	{
		// Titre
		setStepTitle("les informations générales du producteur");
		
		// Champ 1
		addTextField("Nom", "nom").setReadOnly(true);
		
		TextArea f =  addTextAeraField("Description", "description");
		f.setMaxLength(20480);
		f.setHeight(5, Unit.CM);
		f.setReadOnly(true);
		
		if (new ParametresService().getParametres().etatGestionStock==EtatModule.ACTIF)
		{
			addComboEnumField("Activer la gestion des limites en quantité pour ce producteur", "gestionStock").setReadOnly(true);
		}
		
		addHtml(getInfoProducteur(producteurDTO));

	}
	
	
	static public String getInfoProducteur(ProducteurDTO producteurDTO)
	{
		SimpleDateFormat df = FormatUtils.getTimeStd();
		SimpleDateFormat df2 = FormatUtils.getStdDate();
		
		String str = "Date de création : "+df.format(producteurDTO.dateCreation)+"<br/>"+
					 "Date de dernière modification : ";
				
		if (producteurDTO.dateModification!=null)
		{
			str = str + df.format(producteurDTO.dateModification);
		}
		str = str+"<br/>";
		
		str = str +"Nombre de modèles de contrats à l'état CREATION ou ACTIF de ce producteur : "+producteurDTO.nbModeleContratActif+"</br>";
		if (producteurDTO.dateDerniereLivraison==null)
		{
			str = str +"Aucune date de livraison connue pour ce producteur</br>";
		}
		else
		{
			str = str +"Date de la dernière livraison : "+df2.format(producteurDTO.dateDerniereLivraison)+"</br>";	
		}
		return str;
	}
	
	
	
	private void addFieldDocuments()
	{
		// Titre
		setStepTitle("les documents de ce producteur");
	
		
		addHtml("<b>La feuille de distribution producteur</b>");
		
		addComboEnumField("La feuille de distribution contient un onglet avec les produits à livrer en tableau", "feuilleDistributionGrille").setReadOnly(true);
		
		addComboEnumField("La feuille de distribution contient un onglet avec les produits à livrer en liste", "feuilleDistributionListe").setReadOnly(true);
		
		
		addComboEnumField("La feuille de distribution contient un onglet avec les étiquettes des produits ", "feuilleDistributionEtiquette").setReadOnly(true);
		
		addSearcher("Type des étiquettes", "idEtiquette", SearcherList.ETIQUETTE,null).setReadOnly(true);
		
		
		
		
		
		addHtml("<b>Le contrat d'engagement</b>");
		
		addTextField("Identification du producteur sur le contrat d'engagement", "libContrat").setReadOnly(true);
	
		
		
		addHtml("<b>L'envoi automatique des feuilles de distribution au producteur</b>");
		
		addIntegerField("Délai en jours entre l'envoi de la feuille de distribution par mail et la livraison", "delaiModifContrat").setReadOnly(true);
		
		String str = 	"Exemple :<br>" +
						"Si les livraisons ont lieu le jeudi et si vous mettez 3 dans le champ précédent<br>"+
						"alors le producteur recevra le mail avec la feuille de distribution le lundi à 2h00 du matin<br>";
		
		addHtml(str);
	
	}

	private void addFieldUtilisateur()
	{
		// Titre
		setStepTitle("les noms des producteurs");
		
	
		CollectionEditor<ProdUtilisateurDTO> f1 = addCollectionEditorField("Liste des producteurs", "utilisateurs", ProdUtilisateurDTO.class);	
		f1.addSearcherColumn("idUtilisateur", "Nom du producteur",FieldType.SEARCHER,false, null,SearcherList.UTILISATEUR_ACTIF,null);
		f1.addColumn("etatNotification","Notification par mail",FieldType.CHECK_BOX,false,true);	
		f1.disableAllButtons();
	}
	
	
	
	private void addFieldReferents()
	{	
		// Titre
		setStepTitle("les noms des référents");
		
		CollectionEditor<ProdUtilisateurDTO> f1 = addCollectionEditorField("Liste des référents", "referents", ProdUtilisateurDTO.class);	
		f1.addSearcherColumn("idUtilisateur", "Nom des référents",FieldType.SEARCHER, false,null,SearcherList.UTILISATEUR_ACTIF,null);
		f1.addColumn("etatNotification","Notification par mail",FieldType.CHECK_BOX,false,false);
		f1.disableAllButtons();
	
	}


	@Override
	protected void performSauvegarder() throws OnSaveException
	{
	}
}
