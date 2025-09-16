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
 package fr.amapj.view.views.historiquepaiements;

import java.util.List;

import fr.amapj.service.services.mespaiements.MesPaiementsService;
import fr.amapj.service.services.mespaiements.PaiementHistoriqueDTO;
import fr.amapj.service.services.session.SessionManager;
import fr.amapj.view.engine.listpart.StandardListPart;


/**
 * Page permettant de presenter la liste des paiements passés
 * 
 *  
 *
 */
public class HistoriquePaiementsView extends StandardListPart<PaiementHistoriqueDTO>
{
	public HistoriquePaiementsView()
	{
		super(PaiementHistoriqueDTO.class,false);
	}
	
	
	@Override
	protected String getTitle() 
	{
		return "Liste des paiements passés";
	}


	@Override
	protected void drawButton() 
	{
		addSearchField("Rechercher par le nom du contrat ou du producteur");
	}


	@Override
	protected void drawTable() 
	{
		addColumn("nomProducteur","Producteur");
		addColumn("nomContrat","Contrat");
		addColumnDate("datePrevu","Mois de paiement prévu");
		addColumnDate("dateReelle","Date réelle de remise");
		addColumnCurrency("montant","Montant (en €)").right();
	}



	@Override
	protected List<PaiementHistoriqueDTO> getLines() 
	{
		return new MesPaiementsService().getMesPaiements(SessionManager.getUserId()).paiementHistorique;
	}


	@Override
	protected String[] getSortInfos() 
	{
		return new String[] { "datePrevu" , "nomContrat" };
	}
	
	@Override
	protected boolean[] getSortAsc()
	{
		return new boolean[] { false, true };
	}
	
	protected String[] getSearchInfos()
	{
		return new String[] { "nomProducteur" , "nomContrat" };
	}
}
