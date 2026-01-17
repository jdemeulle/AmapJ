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
 package fr.amapj.view.views.producteur;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

import fr.amapj.model.models.fichierbase.EtatProducteur;
import fr.amapj.model.models.fichierbase.Producteur;
import fr.amapj.service.services.access.AccessManagementService;
import fr.amapj.service.services.producteur.ProducteurService;
import fr.amapj.service.services.session.SessionManager;
import fr.amapj.view.engine.popup.PopupListener;
import fr.amapj.view.engine.searcher.Searcher;
import fr.amapj.view.engine.tools.BaseUiTools;
import fr.amapj.view.views.searcher.SearcherList;


/**
 * Outil permettant le choix du producteur 
 * sous la forme d'un bandeau en haut de l'écran
 */
public class ProducteurSelectorPart
{	
	private Searcher producteurBox;
	
	private Long idProducteur;
	
	private Button reinitButton;
	
	private List<Producteur> allowedProducteurs;	
	
	private PopupListener listener;
	
	private boolean isCompactMode;

	/**
	 * si actifOnly = true : il y a uniquement les producteurs actifs
	 * si actifOnly = false : il y a tous les producteurs (actifs et inactifs)
	 * 
	 * si filterByUserRole = true : il y a uniquement les producteurs autorisés pour cet utilisateur
	 * si filterByUserRole = false : il y a tous les producteurs actifs 
	 */
	public ProducteurSelectorPart(PopupListener listener,boolean actifOnly,boolean filterByUserRole)
	{
		this.listener = listener;
		if (filterByUserRole)
		{
			allowedProducteurs = new AccessManagementService().getAccessLivraisonProducteur(SessionManager.getUserRoles(),SessionManager.getUserId(),actifOnly);
		}
		else
		{
			allowedProducteurs = new ProducteurService().getAll(EtatProducteur.ACTIF);
			if (actifOnly==false)
			{
				allowedProducteurs.addAll(new ProducteurService().getAll(EtatProducteur.ARCHIVE));
			}
			allowedProducteurs.sort((p1,p2)->Collator.getInstance().compare(p1.nom, p2.nom));
		}
		isCompactMode = BaseUiTools.isCompactMode();
	}


	public HorizontalLayout getChoixProducteurComponent()
	{
		// Partie choix du producteur
		HorizontalLayout toolbar1 = new HorizontalLayout();	
		toolbar1.addStyleName("producteur-selectorpart");
	
		if (allowedProducteurs.size()>1)
		{
			constructMultipleProducteur(toolbar1);
		}
		else
		{
			constructOneProducteur(toolbar1);
		}
		
		toolbar1.setSpacing(true);
		toolbar1.setWidth("100%");
	
		return toolbar1;
	}
	
	
	
	
	private void constructOneProducteur(HorizontalLayout toolbar1)
	{
		Producteur p = allowedProducteurs.get(0);
		idProducteur = p.getId();
		
		String content = isCompactMode ? p.nom : "Producteur : "+p.nom;
		Label pLabel = BaseUiTools.addStdLabel(toolbar1, content, "unproducteur");
		pLabel.setSizeUndefined(); // Obligatoire pour que le centrage fonctionne 
		toolbar1.setComponentAlignment(pLabel, Alignment.MIDDLE_CENTER);
	}


	private void constructMultipleProducteur(HorizontalLayout toolbar1)
	{
		if (isCompactMode==false)
		{
			Label pLabel = new Label("Producteur");
			pLabel.addStyleName("xproducteurs");
			pLabel.setSizeUndefined();
			toolbar1.addComponent(pLabel);
		}
		
		
		producteurBox = new Searcher(SearcherList.PRODUCTEUR,null,allowedProducteurs);
		producteurBox.setImmediate(true);
		producteurBox.addValueChangeListener(e->handleProducteurChange());
			
		reinitButton = new Button("Changer de producteur");
		reinitButton.addClickListener(e->handleReinit());
					
		toolbar1.addComponent(producteurBox);
		toolbar1.addComponent(reinitButton);
		toolbar1.setExpandRatio(reinitButton, 1);
		toolbar1.setComponentAlignment(reinitButton, Alignment.TOP_RIGHT);
		
	}


	/**
	 * 
	 */
	private void handleProducteurChange()
	{
		idProducteur = (Long) producteurBox.getConvertedValue();
		if (idProducteur!=null)
		{
			producteurBox.setEnabled(false);
		}
		listener.onPopupClose();
	}
	
	
	protected void handleReinit()
	{
		producteurBox.setValue(null);
		producteurBox.setEnabled(true);
		idProducteur = null;
		listener.onPopupClose();
	}
	
	
	public Long getProducteurId()
	{
		return idProducteur;
	}
	

}
