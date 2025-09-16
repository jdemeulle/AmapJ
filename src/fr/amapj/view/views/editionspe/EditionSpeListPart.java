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
 package fr.amapj.view.views.editionspe;

import java.util.List;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.service.services.editionspe.EditionSpeDTO;
import fr.amapj.service.services.editionspe.EditionSpeService;
import fr.amapj.view.engine.listpart.ButtonType;
import fr.amapj.view.engine.listpart.StandardListPart;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.suppressionpopup.SuppressionPopup;
import fr.amapj.view.engine.popup.swicthpopup.SwitchPopup;
import fr.amapj.view.views.editionspe.bilanlivraison.BilanLivraisonEditorPart;
import fr.amapj.view.views.editionspe.bulletinadhesion.BulletinAdhesionEditorPart;
import fr.amapj.view.views.editionspe.engagement.EngagementEditorPart;


/**
 * Gestion des étiquettes
 *
 */
public class EditionSpeListPart extends StandardListPart<EditionSpeDTO>
{

	public EditionSpeListPart()
	{
		super(EditionSpeDTO.class,false);
	}
	
	
	@Override
	protected String getTitle() 
	{
		return "Liste des éditions spécifiques";
	}


	@Override
	protected void drawButton() 
	{
		addButton("Créer une nouvelle édition spécifique",ButtonType.ALWAYS,e->handleAjouter());
		addButton("Modifier",ButtonType.EDIT_MODE,e->handleEditer());
		addButton("Dupliquer",ButtonType.EDIT_MODE,e->new DupliquerEditionSpeEditorPart(e));
		addButton("Supprimer",ButtonType.EDIT_MODE,e->handleSupprimer());

		addSearchField("Rechercher par nom ");
	}

	

	@Override
	protected void drawTable() 
	{
		addColumn("nom","Nom");
		addColumn("typEditionSpecifique","Type de l'édition");
	}



	@Override
	protected List<EditionSpeDTO> getLines() 
	{
		return new EditionSpeService().getAllEtiquettes();
	}


	@Override
	protected String[] getSortInfos() 
	{
		return new String[] { "nom"  };
	}
	
	protected String[] getSearchInfos()
	{
		return new String[] { "nom" };
	}
	
	

	private SwitchPopup handleAjouter()
	{
		SwitchPopup popup = new SwitchPopup("Choix de l'édition spécifique à créer",50);
		
		popup.addLine("Un nouveau document d'engagement", ()->new EngagementEditorPart(true, null));
		popup.addLine("Un nouveau bulletin d'adhésion", ()->new BulletinAdhesionEditorPart(true, null));
		popup.addLine("Une nouvelle étiquette", ()->new EtiquetteProducteurEditorPart(true, null));
		popup.addLine("Une nouvelle feuille d'émargement (mensuelle ou hebdomadaire)", ()->new FeuilleEmargementEditorPart(true, null));
		popup.addLine("Un nouveau bilan de livraison", ()->new BilanLivraisonEditorPart(true, null));
		
		return popup;
	}

	

	protected CorePopup handleEditer()
	{
		EditionSpeDTO dto = getSelectedLine();
		
		switch (dto.typEditionSpecifique)
		{
			case ETIQUETTE_PRODUCTEUR: return new EtiquetteProducteurEditorPart(false, dto);
			case FEUILLE_EMARGEMENT: return new FeuilleEmargementEditorPart(false, dto);
			case CONTRAT_ENGAGEMENT: return new EngagementEditorPart(false, dto);
			case BULLETIN_ADHESION: return new BulletinAdhesionEditorPart(false, dto);
			case BILAN_LIVRAISON: return new BilanLivraisonEditorPart(false, dto);
			default: throw new AmapjRuntimeException("Erreur");
		}
	}

	protected SuppressionPopup handleSupprimer()
	{
		EditionSpeDTO dto = getSelectedLine();
		String text = "Etes vous sûr de vouloir supprimer l'édition spécifique "+dto.nom+" ?";
		return new SuppressionPopup(text,dto.id,e->new EditionSpeService().delete(e));	
	}
}
