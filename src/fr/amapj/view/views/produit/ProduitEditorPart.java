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
 package fr.amapj.view.views.produit;

import java.util.Arrays;

import com.vaadin.ui.ComboBox;

import fr.amapj.model.models.produitextended.reglesconversion.ProduitLimiteQuantite;
import fr.amapj.service.services.produit.ProduitDTO;
import fr.amapj.service.services.produit.ProduitService;
import fr.amapj.service.services.stockservice.StockUtilService;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.popup.formpopup.fieldlink.FieldLink;
import fr.amapj.view.engine.popup.formpopup.validator.IValidator;
import fr.amapj.view.engine.popup.formpopup.validator.NotNullValidator;
import fr.amapj.view.engine.popup.formpopup.validator.StringLengthValidator;
import fr.amapj.view.views.searcher.SDProduitOneProducteur;

/**
 * Permet uniquement de creer des contrats
 * 
 *
 */
public class ProduitEditorPart extends WizardFormPopup
{

	private ProduitDTO dto;

	private boolean create;
	
	private boolean gestionStock;
	

	/**
	 * 
	 */
	public ProduitEditorPart(boolean create,Long idProduit,Long idProducteur)
	{
		this.create = create;
		
		setWidth(80);
		
		if (create)
		{
			popupTitle = "Création d'un produit";
			this.dto = new ProduitDTO();
			this.dto.producteurId = idProducteur;
			this.dto.limiteQuantite= ProduitLimiteQuantite.SUIVI_STANDARD;
		}
		else
		{
			popupTitle = "Modification d'un produit";
			this.dto = new ProduitService().loadProduit(idProduit);
		}	
		
		gestionStock = new StockUtilService().hasProducteurGestionStock(dto.producteurId);
		
		setModel(this.dto);

	}
	
	@Override
	protected void configure() 
	{
		add(()->addInfoGenerales());
		if (gestionStock)
		{
			add(()->addInfoStock(),()->checkStock());
		}
	}

	protected void addInfoGenerales()
	{
		IValidator len_1_255 = new StringLengthValidator(1, 255);
		IValidator len_1_500 = new StringLengthValidator(1, 500);
		
	
		addTextField("Nom", "nom",len_1_255);
		
		addTextField("Conditionnement", "conditionnement",len_1_500);	
		
	}
	
	private void addInfoStock() 
	{
		ComboBox b1 = addComboEnumField("La gestion des limites en quantité pour ce produit", "limiteQuantite", new NotNullValidator());
		
		FieldLink f1 = new FieldLink(validatorManager,Arrays.asList(ProduitLimiteQuantite.SUIVI_AVEC_REGLE_CALCUL),b1,true);
		
		f1.addField(addSearcher("Produit équivalent", "idProduitEnStock", new SDProduitOneProducteur(dto.producteurId), null, f1.getValidator()));
		
		f1.addField(addDoubleField("Coefficient", "coefficient"));
				
		f1.doLink();
	}
		
	
	
	
	private String checkStock() 
	{
		if (dto.limiteQuantite==ProduitLimiteQuantite.NON_SUIVI || dto.limiteQuantite==ProduitLimiteQuantite.SUIVI_STANDARD)
		{
			dto.coefficient = 0;
			dto.idProduitEnStock = null;
			return null;
		}
		
		
		if (dto.coefficient<=0)
		{
			return "Le coefficient doit être un nombre strictement positif. Exemple : 0.5 ou 1 ou 6 ou ..."; 
		}
		
		if (dto.idProduitEnStock==null)
		{
			return "Si vous utilisez une regele de conversion, vous devez indiquer un produit equivalent";
		}
		
		ProduitDTO prodEquivalent = new ProduitService().loadProduit(dto.idProduitEnStock);
		if (prodEquivalent.limiteQuantite==ProduitLimiteQuantite.NON_SUIVI)
		{
			return "Le produit equivalent n'est pas correct : il doit être lui même suivi en stock";
		}
		
		if (prodEquivalent.limiteQuantite==ProduitLimiteQuantite.SUIVI_AVEC_REGLE_CALCUL)
		{
			return "Le produit equivalent n'est pas correct : il ne peut pas être lui même basé sur une régle de calcul.";
		}
		
		return null;
	}



	@Override
	protected void performSauvegarder() throws OnSaveException
	{
		new ProduitService().update(dto, create);
	}

	
}
