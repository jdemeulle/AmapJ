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
 package fr.amapj.view.engine.popup.cascadingpopup;

import java.util.Arrays;
import java.util.List;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.view.engine.popup.PopupListener;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;


/**
 * Utilitaire permettant de faire du cascading de popup 
 * 
 *
 */
public class CascadingPopup
{
	private PopupListener finalListener;
	
	private CascadingData data;	
	
	private boolean startDone = false;

	
	public CascadingPopup(PopupListener finalListener,CascadingData data)
	{
		this.finalListener = finalListener;
		this.data = data;
	}
	
	/**
	 * Démarrage du cascading popup 
	 * 
	 * Doit être appelé une seule fois 
	 */
	public void start(CInfo info)
	{
		if (startDone)
		{
			throw new AmapjRuntimeException("Il est interdit d'appeler deux fois la fonction start");
		}
		startDone = true;
		
		openNextPopup(info);
	}
		
	/**
	 * Ouvre le popup indiqué si besoin, puis enchaine avec les actions suivantes
	 */
	private void openNextPopup(CInfo info)
	{
		// Si rien a faire, on ne fait rien et on revient au final listener 
		if (info==null)
		{
			callFinalListener();
			return;
		}
		
		// Si pas de popup : on passe à la suite comme si l'utilisateur avait cliqué sur le bouton OK
		// En general cela va mener à l'execution de actionAfterOnSaveButton
		if (info.popup==null)
		{
			data.validate();
			endOfPopup(info);
			return;
		}

		// On memorise les informations nécessaires dans data 
		data.shouldContinue = false;
		data.libSaveButton = info.libSaveButton;
		data.currentPopup = info.popup;
		
		// On ouvre la  fenetre 
		CorePopup.open(info.popup, ()->endOfPopup(info));
		
	}


	

	/**
	 * Fonction appelée à la fin du popup 
	 */
	private void endOfPopup(CInfo info)
	{
		// Si une popup a été insérée dans le flux 
		CorePopup insertedPopup = data.insertedPopup;
		if (insertedPopup!=null)
		{
			// Mise à jour de l'objet data 
			data.insertedPopup = null;
			data.currentPopup = insertedPopup;
			
			// Mise à jour de l'objet info
			info.popup = insertedPopup;
			
			// On ouvre la  fenetre insérée, en gardant les mêmes actions à la fermeture de la popup 
			CorePopup.open(info.popup, ()->endOfPopup(info));
			return;
		}
		
		// L'opérateur a fait OK
		if (data.shouldContinue())
		{
			// On execute l'action à effectuer après l'appui sur le button "Continuer / Sauvegarder" si il y en a une 
			if (info.actionAfterOnSaveButton!=null)
			{
				try 
				{
					info.actionAfterOnSaveButton.action();
				} 
				catch (OnSaveException e) 
				{
					// Si il y a une erreur , on l'affiche, an appelle le finalListener et on stoppe le traitement 
					displayError(e.getAllMessages());
					return;
				}
			}
			
			// Si pas de onSuccess : c'est fini 
			if (info.onSuccess==null)
			{
				callFinalListener();
				return;
			}
		
			// Demande de calcul du popup suivant 
			CInfo nextInfo = info.onSuccess.get();
			openNextPopup(nextInfo);
		}
		// L'opérateur n'a pas fait de OK
		else
		{
			// Si pas de onFail : c'est fini 
			if (info.onFail==null)
			{
				callFinalListener();
				return;
			}
		
			// Demande de calcul du popup suivant 
			CInfo nextInfo = info.onFail.get();
			openNextPopup(nextInfo);
		}
		
		
	}
	

	private void callFinalListener()
	{
		if (finalListener!=null)
		{
			finalListener.onPopupClose();
		}
	}

	
	/**
	 * Permet l'affichage d'une erreur
	 * Quand l'utilisateur acquitte l'erreur (appui sur OK), il y a l'appel du finalListener 
	 * puis la fin du traitement 
	 */
	public void displayError(List<String> msgs)
	{
		MessagePopup p = new MessagePopup("Impossible de continuer",msgs);
		p.open(()->callFinalListener());
	}
	
	/**
	 * Permet l'affichage d'une erreur
	 * Quand l'utilisateur acquitte l'erreur (appui sur OK), il y a l'appel du finalListener 
	 * puis la fin du traitement 
	 */
	public void displayError(String msg)
	{
		displayError(Arrays.asList(msg));
	}
	
}
