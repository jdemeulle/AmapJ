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
 package fr.amapj.service.services.gestioncontrat.reglesaisie;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.FormatUtils;
import fr.amapj.common.IdContainer;
import fr.amapj.model.models.contrat.modele.extendparam.reglesaisie.RSChampApplication;
import fr.amapj.service.services.gestioncontrat.reglesaisie.VerifRegleSaisieResultDTO.ResultDTO;
import fr.amapj.service.services.mescontrats.ContratColDTO;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.mescontrats.ContratLigDTO;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.ModeSaisie;

public class VerifRegleSaisieModeleContratDTO 
{
	public List<RegleSaisieDTO> regleSaisies = new ArrayList<>();
	
	public ContratDTO contratDTO;
	
	public ModeSaisie modeSaisie;
	
	public VerifRegleSaisieResultDTO performCheck()
	{
		VerifRegleSaisieResultDTO res = new VerifRegleSaisieResultDTO();
		for (RegleSaisieDTO regleSaisieDTO : regleSaisies) 
		{
			isValid(regleSaisieDTO,res);
		}
		return res;
	}
	
	private void isValid(RegleSaisieDTO r, VerifRegleSaisieResultDTO res) 
	{
		if (isApplicable(r)==false)
		{
			return;
		}		
		
		// On calcule les produits concernés
		List<ContratColDTO> produits = computeProduits(r);
		
		switch (r.contrainteDate) 
		{
			case POUR_CHAQUE_DATE: 
				isValidPourChaqueDate(produits,r,res); 
				break;
			case POUR_PLUSIEURS_DATES : 
			case POUR_UNE_DATE:
			case POUR_TOUT_CONTRAT:
				isValidPourUneOuPlusieursDates(produits,r,res); 
				break;
				
			default: 
				throw new AmapjRuntimeException();
		}
	}

	
	/**
	 * Est ce que cette regle est applicable ? 
	 */
	private boolean isApplicable(RegleSaisieDTO r) 
	{
		// 
		switch (modeSaisie) 
		{
		case CHEQUE_SEUL:
		case READ_ONLY:
			return false;
		
		case QTE_CHEQUE_REFERENT:
		case QTE_SEUL:
			return r.champApplication==RSChampApplication.TOUS;
			
		case FOR_TEST:
		case STANDARD:
		case JOKER:
			return true;

		default: throw new AmapjRuntimeException();
		}

	}

	private void isValidPourChaqueDate(List<ContratColDTO> produits, RegleSaisieDTO r, VerifRegleSaisieResultDTO res) 
	{
		SimpleDateFormat df = FormatUtils.getStdDate();
		List<String> ls = new ArrayList<>();
		for (ContratLigDTO contratLigDTO : contratDTO.contratLigs) 
		{
			checkOneRegle(produits,Arrays.asList(contratLigDTO),r,ls,"Date : "+df.format(contratLigDTO.date));
		}		
		if (ls.size()!=0)
		{
			ResultDTO x = new ResultDTO();
			x.regleSaisieDTO = r;
			x.msgs = ls;
			res.errors.add(x);
		}
	}

	private void isValidPourUneOuPlusieursDates(List<ContratColDTO> produits, RegleSaisieDTO r, VerifRegleSaisieResultDTO res) 
	{
		List<String> ls = new ArrayList<>();
		
		List<ContratLigDTO> dates = computeDates(r);
		checkOneRegle(produits,dates,r,ls,"");
		
		if (ls.size()!=0)
		{
			ResultDTO x = new ResultDTO();
			x.regleSaisieDTO = r;
			x.msgs = ls;
			res.errors.add(x);
		}
	}
	

	// Calcul des dates concernées
	

	private List<ContratLigDTO> computeDates(RegleSaisieDTO r) 
	{
		switch (r.contrainteDate) 
		{
			case POUR_UNE_DATE: 
			case POUR_PLUSIEURS_DATES : return findContratLigDTOs(r.modeleContratDateIds);
			case POUR_TOUT_CONTRAT : return contratDTO.contratLigs;
			case POUR_CHAQUE_DATE : // Ne doit pas arriver
			default: throw new AmapjRuntimeException();
		}
	}

	private List<ContratLigDTO> findContratLigDTOs(List<IdContainer> modeleContratDateIds) 
	{
		return contratDTO.contratLigs.stream().filter(e->isInList(modeleContratDateIds,e.modeleContratDateId)).collect(Collectors.toList());
	}

	// Calcul des produits concernés
	private List<ContratColDTO> computeProduits(RegleSaisieDTO r) 
	{
		switch (r.contrainteProduit) 
		{
			case POUR_UN_PRODUIT: 
			case POUR_PLUSIEURS_PRODUITS : return findContratColDTOs(r.produitIds);
			case POUR_TOUS_PRODUITS : return contratDTO.contratColumns;
			default: throw new AmapjRuntimeException();
		}
	}


	private List<ContratColDTO> findContratColDTOs(List<IdContainer> produitIds) 
	{
		return contratDTO.contratColumns.stream().filter(e->isInList(produitIds,e.produitId)).collect(Collectors.toList());
	}


	private boolean isInList(List<IdContainer> ids, Long id) 
	{
		return ids.stream().anyMatch(e->e.id.equals(id));
	}
	
	
	// Verification unitaire d'une régle, avec la liste des produits et la liste des dates connues
	
	private void checkOneRegle(List<ContratColDTO> produits, List<ContratLigDTO> dates, RegleSaisieDTO r,List<String> res,String context) 
	{
		int qte = getQteTotal(produits, dates);
		
		boolean ret = isValidOperateurVal(r,qte);
		if (ret==false)
		{
			res.add(context+" Quantité réelle : "+qte);
		}
		
	}
	
	
	private boolean isValidOperateurVal(RegleSaisieDTO r, int qte) 
	{
		switch (r.contrainteOperateur) 
		{
			case EGAL: return qte==r.val;
			case INFERIEUR_OU_EGAL : return qte<=r.val;
			case SUPERIEUR_OU_EGAL : return qte>=r.val;
			case MULTIPLE_DE : return (qte % r.val) == 0;
			default: throw new AmapjRuntimeException();
		}

	}

	private int getQteTotal(List<ContratColDTO> colProduits, List<ContratLigDTO> ligDates)
	{
		int qteTotale = 0;
		
		for (int j = 0; j < colProduits.size(); j++)
		{
			int cellJindex = colProduits.get(j).j;
			for (int i = 0; i < ligDates.size(); i++)
			{
				int cellIindex = ligDates.get(i).i;
				qteTotale = qteTotale + contratDTO.cell[cellIindex][cellJindex].qte;
			}
		}
		return qteTotale;
	}
	
	
	private int getMntTotal(List<ContratColDTO> colProduits, List<ContratLigDTO> ligDates)
	{
		int mnt = 0;
		
		for (int j = 0; j < colProduits.size(); j++)
		{
			int cellJindex = colProduits.get(j).j;
			int prix = colProduits.get(cellJindex).prix;
		
			for (int i = 0; i < ligDates.size(); i++)
			{
				int cellIindex = ligDates.get(i).i;
				mnt = mnt + contratDTO.cell[cellIindex][cellJindex].qte*prix;
			}
		}
		return mnt;
	}

	
}
