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
 package fr.amapj.view.views.common.amapientelecharger;

import java.util.Date;

import fr.amapj.common.DateUtils;
import fr.amapj.common.periode.TypPeriode;
import fr.amapj.service.services.edgenerator.excel.amapien.EGBilanCompletAmapien;
import fr.amapj.service.services.edgenerator.excel.cheque.EGSyntheseCheque;
import fr.amapj.service.services.edgenerator.excel.cheque.EGSyntheseCheque.Mode;
import fr.amapj.service.services.edgenerator.excel.livraison.EGLivraisonAmapien;
import fr.amapj.view.engine.excelgenerator.TelechargerPopup;
import fr.amapj.view.engine.popup.corepopup.CorePopup;

public class TelechargerAmapien 
{
	static public CorePopup handleTelecharger(Long idUtilisateur)
	{
		TelechargerPopup popup = new TelechargerPopup("Informations d'un amapien",80);
	
		Date startDate = DateUtils.getDateWithNoTime();
		popup.addGenerator(new EGLivraisonAmapien(TypPeriode.A_PARTIR_DE, startDate, null, idUtilisateur));
		popup.addSeparator();
		
		popup.addGenerator(new EGBilanCompletAmapien(idUtilisateur));
		popup.addSeparator();
		
		popup.addGenerator(new EGSyntheseCheque(Mode.CHEQUE_A_REMETTRE,idUtilisateur));
		popup.addGenerator(new EGSyntheseCheque(Mode.CHEQUE_AMAP,idUtilisateur));
		popup.addGenerator(new EGSyntheseCheque(Mode.CHEQUE_REMIS_PRODUCTEUR,idUtilisateur));
		popup.addGenerator(new EGSyntheseCheque(Mode.TOUS,idUtilisateur));
		
		return popup;
			
	}
}
