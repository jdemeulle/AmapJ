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
 package fr.amapj.view.views.saisiecontrat.abo.joker;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.SaisieContratData;

public class SaisieJoker 
{
	public static CorePopup computePopupSaisieJoker(SaisieContratData data, boolean readOnly) 
	{
		switch (data.contratDTO.typJoker) 
		{
		case JOKER_ABSENCE:
			return new PopupSaisieJokerAbsence(data, readOnly);
			
			
		case JOKER_REPORT:
			return new PopupSaisieJokerReport(data, readOnly);

		default:
			throw new AmapjRuntimeException();
		}
	}
		

	public static void doSaisieJoker(SaisieContratData data, boolean readOnly, Runnable actionAfterClose) 
	{
		CorePopup popup = computePopupSaisieJoker(data, readOnly);
		popup.open(()->actionAfterClose.run());
	}
}
