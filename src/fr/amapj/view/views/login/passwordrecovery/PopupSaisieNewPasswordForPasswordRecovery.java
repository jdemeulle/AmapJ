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
 package fr.amapj.view.views.login.passwordrecovery;

import fr.amapj.service.services.authentification.PasswordManager;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;
import fr.amapj.view.engine.popup.formpopup.genericmodel.StringItem;

/**
 * Popup pour la saisie d'un nouveau password, suite à une perte 
 *  
 */
public class PopupSaisieNewPasswordForPasswordRecovery extends WizardFormPopup
{
		
	private String resetPasswordSalt;

	private Long idUtilisateur;
	
	private StringItem password;

	/**
	 * 
	 */
	public PopupSaisieNewPasswordForPasswordRecovery(String resetPasswordSalt)
	{
		popupTitle = "Changement de votre mot de passe"; 
		this.resetPasswordSalt = resetPasswordSalt;	
		
		password = new StringItem();
		setModel(password);
	}
	
	@Override
	protected String checkInitialCondition()
	{
		idUtilisateur = new PasswordManager().findUserWithResetPassword(resetPasswordSalt);
		
		if (idUtilisateur==null)
		{
			return "Demande invalide ou trop ancienne";
		}
		else
		{
			return null;
		}
	}
	
	@Override
	protected void configure()
	{
		add(()->addSaisiePassword());
		add(()->addResultat());
	}
	
	
	protected void addSaisiePassword()
	{
		addHtml("Vous avez demandé à changer de mot de passe suite à l'oubli de celui ci. Merci de saisir votre nouveau mot de passe ci dessous.");
		
		addPasswordTextField("Votre nouveau mot de passe", "value");
	}
	
	private void addResultat()
	{
		boolean res = new PasswordManager().setUserPassword(idUtilisateur,password.value);
		String msg = res==true ? "Votre mot de passe a été changé avec succés" : "Impossible de modifier votre mot de passe";
		addHtml(msg);
		
		setAllButtonsAsOK();
	}

	@Override
	protected void performSauvegarder()
	{
		// Nothing to do
	}
}
