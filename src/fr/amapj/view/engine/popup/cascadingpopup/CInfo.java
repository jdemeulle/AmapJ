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

import java.util.function.Supplier;

import fr.amapj.common.GenericUtils.VoidActionThrows;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;

public class CInfo
{
	// Le popup à ouvrir 
	// Peut être null, dans ce cas on passe directement à l'execution de l'action actionAfterOnSaveButton 
	public CorePopup popup;
	
	// Le libellé du bouton "Sauvegarder"
	// peut être null, dans ce cas le popup choisit le libellé à utiliser  
	public String libSaveButton;
	
	// L'action à effectuer après l'appui sur le button "Continuer / Sauvegarder" 
	// Peut être null
	// Cette action est effectuée après la fermeture du popup et avant l'appel Supplier<CInfo> onSuccess
	// En cas d'exception, l'exception est affichée dans un popup , le traitement s'arrête et le finalListener est appelé 
	public VoidActionThrows<OnSaveException> actionAfterOnSaveButton;
	
	// Le popup suivant à ouvrir si l'utilisateur a cliqué sur le bouton "Continuer / Sauvegarder"
	// Si retourne null, pas de popup suivant , le finalListener est appelé  
	public Supplier<CInfo> onSuccess;
	
	// Le popup suivant à ouvrir si l'utilisateur a cliqué sur le bouton "Quitter"
	// Si retourne null, pas de popup suivant , le finalListener est appelé 
	public Supplier<CInfo> onFail;
	
}
