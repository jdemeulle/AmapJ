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

 package fr.amapj.view.views.utilisateur;

import java.text.SimpleDateFormat;
import java.util.List;

import com.vaadin.data.util.BeanItem;

import fr.amapj.common.FormatUtils;
import fr.amapj.service.services.access.AccessManagementService;
import fr.amapj.service.services.edgenerator.excel.EGListeAdherent;
import fr.amapj.service.services.edgenerator.excel.EGListeAdherent.Type;
import fr.amapj.service.services.edgenerator.excel.amapien.EGBilanCompletAmapien;
import fr.amapj.service.services.producteur.ProducteurDTO;
import fr.amapj.service.services.utilisateur.UtilisateurDTO;
import fr.amapj.service.services.utilisateur.UtilisateurService;
import fr.amapj.view.engine.excelgenerator.LinkCreator;
import fr.amapj.view.engine.popup.formpopup.FormPopup;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;

/**
 * Permet de viusaliser un utilisateur
 */
public class PopupUtilisateurVoirPart extends WizardFormPopup
{
	

	private UtilisateurDTO utilisateurDTO;

	/**
	 * 
	 */
	public PopupUtilisateurVoirPart(Long idUtilisateur)
	{
		setWidth(80);
		popupTitle = "Visualisation d'un utilisateur";
		saveButtonTitle = "OK";

		this.utilisateurDTO = new UtilisateurService().loadUtilisateurDto(idUtilisateur);
		setModel(utilisateurDTO);

	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldBasic());
		add(()->addFieldAutres());
	}
	
	protected void addFieldBasic()
	{
		setStepTitle("Informations générales");
		
		// Champ 1
		addTextField("Nom", "nom").setReadOnly(true);

		// Champ 2
		addTextField("Prénom", "prenom").setReadOnly(true);

		// Champ 3
		addTextField("E mail", "email").setReadOnly(true);
		
		// Champ 4
		addTextField("Téléphone 1", "numTel1").setReadOnly(true);
		
		// Champ 5
		addTextField("Téléphone 2", "numTel2").setReadOnly(true);

		// Champ 6
		addTextField("Adresse", "libAdr1").setReadOnly(true);
		
		// Champ 7
		addTextField("Code postal", "codePostal").setReadOnly(true);
		
		// Champ 8
		addTextField("Ville", "ville").setReadOnly(true);
		
	}
	
	protected void addFieldAutres()
	{
		
		setStepTitle("Informations sur les contrats et les rôles");
		
		String str = getInfoUtilisateur(utilisateurDTO);
		addHtml(str);
		
		form.addComponent(LinkCreator.createLink(new EGBilanCompletAmapien(utilisateurDTO.id)));

	}

	static public String getInfoUtilisateur(UtilisateurDTO utilisateurDTO)
	{
		
		String str = "Nombre de contrats de cet utilisateur : "+new UtilisateurService().countContrat(utilisateurDTO.id)+"</br></br>";
		
		List<String> roles = new AccessManagementService().detailDesRoles(utilisateurDTO.id); 
		for (String role : roles) 
		{
			str = str+role+"<br/>";
		}
		str = str+"<br/>";
		
		SimpleDateFormat df = FormatUtils.getTimeStd();
		
		str = str +"Date de création de cet utilisateur: "+df.format(utilisateurDTO.dateCreation)+"<br/>"+
					 "Date de dernière modification : ";
				
		if (utilisateurDTO.dateModification!=null)
		{
			str = str + df.format(utilisateurDTO.dateModification);
		}
		str = str+"<br/>";
		
		return str;
	}
	

	@Override
	protected void performSauvegarder() 
	{	
	}
}
