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

import java.util.List;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;

import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.model.models.param.EtatModule;
import fr.amapj.service.services.mesadhesions.AdhesionDTO;
import fr.amapj.service.services.mesadhesions.MesAdhesionDTO;
import fr.amapj.service.services.mesadhesions.MesAdhesionsService;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.utilisateur.UtilisateurDTO;
import fr.amapj.service.services.utilisateur.UtilisateurService;
import fr.amapj.service.services.utilisateur.UtilisateurService.UtilisateurInfo;
import fr.amapj.service.services.utilisateur.util.UtilisateurUtil;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.popup.formpopup.validator.EmailValidator;
import fr.amapj.view.engine.popup.formpopup.validator.IValidator;
import fr.amapj.view.engine.popup.formpopup.validator.NotNullValidator;
import fr.amapj.view.engine.popup.formpopup.validator.UniqueInDatabaseValidator;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;
import fr.amapj.view.views.importdonnees.UtilisateurImporter;

/**
 * Permet de creer les utilisateurs
 * 
 *
 */
public class CreationUtilisateurEditorPart extends WizardFormPopup
{

	private UtilisateurDTO utilisateurDTO;

	private boolean sendMail;

	private ParametresDTO parametresGen;

	private UtilisateurInfo info;

	/**
	 * 
	 */
	public CreationUtilisateurEditorPart()
	{
		setWidth(80);
		popupTitle = "Création d'un utilisateur";

		utilisateurDTO = new UtilisateurDTO();
		setModel(utilisateurDTO);
		
		saveButtonTitle = "OK";
		parametresGen = new ParametresService().getParametres();

	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldSaisie(),()->checkSaisie());
		add(()->addFieldEnvoi(),()->doEnvoi());
		add(()->addFieldComplements());
		
	}

	private void addFieldSaisie()
	{
		IValidator notNull = new NotNullValidator();
		IValidator email = new EmailValidator();
		
		
		// Titre
		setStepTitle("les informations du nouvel utilisateur");
		
		// Champ 1
		addTextField("Nom", "nom",notNull);

		// Champ 2
		addTextField("Prénom", "prenom",notNull);

		// Champ 3
		addTextField("E mail", "email",email);
		
		// Champ 4
		addTextField("Téléphone 1", "numTel1");
		
		// Champ 5
		addTextField("Téléphone 2", "numTel2");

		// Champ 6
		addTextField("Adresse", "libAdr1");
		
		// Champ 7
		addTextField("Code postal", "codePostal");
		
		// Champ 8
		addTextField("Ville", "ville");

	}
	
	private String checkSaisie()
	{
		UtilisateurImporter importer = new UtilisateurImporter();
		List<String> check = importer.checkThisElement(utilisateurDTO);
		
		if (check==null)
		{
			return null;
		}
		
		String err = "Impossible de créer cet utilisateur<br/>"+
					  "Il n'est pas possible de créer cet utilisateur.Raison : <br/>";
		
		for (String str : check)
		{
			err = err + str+"<br/>";
		}
		return err;
	}

	private void addFieldEnvoi()
	{	
		// Titre
		setStepTitle("confirmation avant envoi");
		
		String str = 	"Vous avez demandé à créer un nouvel utilisateur.<br/><br/>"+
						"Nom = <b>"+utilisateurDTO.nom+"</b><br/>"+
						"Prenom= <b>"+utilisateurDTO.prenom+"</b><br/>"+
						"E mail = <b>"+utilisateurDTO.email+"</b><br/><br/>";
		
		
		
		if (UtilisateurUtil.canSendMailTo(utilisateurDTO.email)==false)
		{
			sendMail = false;
			str = str+"Cet utilisateur n'a pas d'email , le mot de passe va être affiché et vous devrez le transmettre à l'utilisateur";
		}
		else
		{
			if (parametresGen.serviceMailActif==true && UtilisateurUtil.canSendMailTo(utilisateurDTO.email))
			{
				sendMail = true;
				str = str+"Un mot de passe va être automatiquement généré et un e mail sera envoyé à l'utilisateur";
			}
			else
			{
				sendMail = false;
				str = str+"Votre service de mail n'est pas actif, le mot de passe va être affiché et vous devrez le transmettre à l'utilisateur";
			}
		}
		str = str+"<br/><br/>Cliquez sur Etape Suivante pour réaliser cette opération, ou Annuler pour ne rien faire";
		
		addHtml(str);
	}
	

	private String doEnvoi()
	{
		info = new UtilisateurService().createNewUser(utilisateurDTO,true,sendMail);
		return null;
	}
	
	
	private void addFieldComplements()
	{	
		// On interdit tout retour en arrière
		form.removeAllComponents();
		previousButton.setEnabled(false);
		cancelButton.setVisible(false);
		
		// Titre
		setStepTitle("compléments sur cet utilisateur");
		
		addHtml("L'utilisateur "+utilisateurDTO.nom+" "+utilisateurDTO.prenom+"a été créé avec succés.</br>");		
		
		if (sendMail)
		{
			addHtml("Le mot de passe a été envoyé par mail directement à l'utilisateur</br>");
		}
		else
		{
			addHtml("Voici le mot de passe , à transmettre à l'utilisateur : "+info.password+"</br>");
		}
		
		addHtml("</br><br/>");
		
		// 
		if (parametresGen.etatGestionCotisation==EtatModule.ACTIF)
		{
			String str = findAdhesion();
			if (str!=null)
			{
				addHtml("<b>Vous avez créé une adhésion pour cet utilisateur : "+str+"</b>");
			}
			else
			{
				addHtml("Vous pouvez maintenant compléter en enregistrant tout de suite l'adhésion de cet utilisateur.</br>");
			
				Button b = new Button("Enregistrer l'adhésion", e->handleAdhesion());
				form.addComponent(b);
			}
		}
		
	}

	private void handleAdhesion() 
	{
		new CreationUtilisateurAjoutCotisation(info.id).open(()->addFieldComplements());
	}
	
	private String findAdhesion() 
	{
		MesAdhesionDTO adhs = new MesAdhesionsService().computeAdhesionInfo(info.id);
		if (adhs.enCours.size()!=0)
		{
			AdhesionDTO adh = adhs.enCours.get(0);
			return "Période :"+adh.nomPeriode+" Montant : "+new CurrencyTextFieldConverter().convertToString(adh.montantAdhesion)+" €";
		}
		
		return null;
	}

	@Override
	protected void performSauvegarder()
	{
		// Nothing to do
	}
}
