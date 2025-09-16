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
import java.util.stream.Collectors;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.FormatUtils;
import fr.amapj.common.IdContainer;
import fr.amapj.common.StringUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.ModeleContratDate;
import fr.amapj.model.models.contrat.modele.extendparam.reglesaisie.RegleSaisie;
import fr.amapj.model.models.contrat.modele.extendparam.reglesaisie.RegleSaisieModeleContrat;
import fr.amapj.model.models.fichierbase.Produit;
import fr.amapj.model.models.param.ChoixOuiNon;
import fr.amapj.service.engine.tools.DbToDto;
import fr.amapj.service.services.gestioncontrat.ExtPModeleContratService;
import fr.amapj.service.services.mescontrats.MonContratDTO;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.ModeSaisie;

public class RegleSaisieModeleContratService 
{

	/**
	 * Chargement des régles
	 */
	@DbRead
	public RegleSaisieModeleContratDTO getRegleSaisieModeleContratDTO(Long idModeleContrat) 
	{
		RdbLink em = RdbLink.get();
		
		ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);
		
		return getRegleSaisieModeleContratDTO(em,mc);
		
	}
	


	private RegleSaisieModeleContratDTO getRegleSaisieModeleContratDTO(RdbLink em, ModeleContrat mc) 
	{
		RegleSaisieModeleContratDTO dto = new RegleSaisieModeleContratDTO();
		dto.idModeleContrat = mc.id;
		dto.idProducteur = mc.producteur.id;
		RegleSaisieModeleContrat regleSaisieModeleContrat = new ExtPModeleContratService().loadRegleSaisieModeleContrat(mc.id);
		
		dto.regleSaisies = DbToDto.convert(regleSaisieModeleContrat.regleSaisies, e->createDto(em,e));
		
		return dto;
	}





	private RegleSaisieDTO createDto(RdbLink em, RegleSaisie r) 
	{
		RegleSaisieDTO dto = new RegleSaisieDTO();
		
		dto.produitId = r.produitIds.stream().findFirst().orElse(null);
		dto.produitIds = r.produitIds.stream().map(e->new IdContainer(e)).collect(Collectors.toList());
		dto.modeleContratDateId = r.modeleContratDateIds.stream().findFirst().orElse(null);
		dto.modeleContratDateIds = r.modeleContratDateIds.stream().map(e->new IdContainer(e)).collect(Collectors.toList());
		
		dto.contrainteDate = r.contrainteDate;
		dto.contrainteOperateur = r.contrainteOperateur;
		dto.contrainteProduit = r.contrainteProduit;
		dto.val = r.val;
		
		dto.libPersonnalise = r.libPersonnalise;
		dto.activateLibPersonnalise = r.libPersonnalise==null ? ChoixOuiNon.NON : ChoixOuiNon.OUI;
		dto.champApplication = r.champApplication;
		
		dto.libelle = getLib(r,em);
		
		return dto;
	}
	
	
	/**
	 *  Calcul du libellé de la règle 
	 */
	public String getLib(RegleSaisie r,RdbLink em) 
	{
	
		StringBuilder sb = new StringBuilder();
		
		sb.append(addDate(r,em));
		
		sb.append(addProduit(r,em));
		
		sb.append(addOperateurValeur(r));
		
		return sb.toString();
	}


	


	private String addDate(RegleSaisie r, RdbLink em) 
	{
		switch (r.contrainteDate) 
		{
		case POUR_CHAQUE_DATE: return "Pour chaque date de livraison, ";
		case POUR_TOUT_CONTRAT: return "Sur la totalité du contrat, ";
		case POUR_UNE_DATE: return "Pour la date du "+computeDate(r,em);
		case POUR_PLUSIEURS_DATES: return "Pour les dates du "+computeDate(r,em);
		
		default: throw new AmapjRuntimeException();

		}

		
	}


	private String computeDate(RegleSaisie r, RdbLink em) 
	{
		SimpleDateFormat df = FormatUtils.getStdDate();
		StringBuilder sb = new StringBuilder();
		for (Long idDate : r.modeleContratDateIds) 
		{
			ModeleContratDate mcd = em.find(ModeleContratDate.class, idDate);
			sb.append(df.format(mcd.dateLiv));
			sb.append(", "); // TODO 
		}
		return sb.toString();
	}
	
	
	private String addProduit(RegleSaisie r, RdbLink em) 
	{
		switch (r.contrainteProduit) 
		{
		case POUR_UN_PRODUIT: return "la quantité commandée du produit :"+computeProduit(r, em);
		case POUR_PLUSIEURS_PRODUITS: return "la somme des quantités commandées des produits :"+computeProduit(r, em);
		case POUR_TOUS_PRODUITS : return "la quantité commandée globale (tous produits confondus) ";
		
		
		default: throw new AmapjRuntimeException();

		}
	}


	private String computeProduit(RegleSaisie r, RdbLink em) 
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<ul>");
		
		
		for (Long idProduit : r.produitIds) 
		{
			Produit p = em.find(Produit.class, idProduit);
			sb.append("<li>"+s(p.nom)+", "+s(p.conditionnement)+"</li>");
		}
		
		sb.append("</ul>");
		
		return sb.toString();
	}

	
	public String s(String value)
	{
		return StringUtils.s(value);
	}
	
	private String addOperateurValeur(RegleSaisie r) 
	{
		switch (r.contrainteOperateur) 
		{
		case EGAL: return "doit être égale à "+r.val;
		case INFERIEUR_OU_EGAL: return "doit être inférieure ou égale à "+r.val;
		case SUPERIEUR_OU_EGAL: return "doit être supérieure ou égale à "+r.val;
		case MULTIPLE_DE: return "doit être un multiple de "+r.val;
		
		default: throw new AmapjRuntimeException();

		}
	}
	
	// Calcul du libellé d'une régle en cours de saisie
	@DbRead
	public String getLib(RegleSaisieDTO dto)
	{
		RdbLink em = RdbLink.get();
		
		RegleSaisie r = convertToRegleSaisie(dto);
		return getLib(r, em);
	}


	
	// Sauvegarde des regles

	public void saveRegleSaisieModeleContrat(RegleSaisieModeleContratDTO dto) 
	{
		RegleSaisieModeleContrat rsmc = new RegleSaisieModeleContrat();
		
		for (RegleSaisieDTO rs : dto.regleSaisies) 
		{
			rsmc.regleSaisies.add(convertToRegleSaisie(rs));
		}
		
		new ExtPModeleContratService().saveRegleSaisieModeleContrat(dto.idModeleContrat, rsmc);
	}
	


	
	private RegleSaisie convertToRegleSaisie(RegleSaisieDTO src) 
	{
		RegleSaisie dest = new RegleSaisie(); 
	
		switch (src.contrainteProduit) 
		{
		case POUR_TOUS_PRODUITS:
			// Nothing to do 
			break;
			
		case POUR_UN_PRODUIT: 
			dest.produitIds.add(src.produitId); 
			break;
			
		case POUR_PLUSIEURS_PRODUITS: 
			dest.produitIds.addAll(src.produitIds.stream().map(e->e.id).collect(Collectors.toList())); 
			break;

		default:
			throw new AmapjRuntimeException();

		}
		
		
		switch (src.contrainteDate) 
		{
		case POUR_TOUT_CONTRAT:
		case POUR_CHAQUE_DATE:
			// Nothing to do 
			break;
			
		case POUR_UNE_DATE: 
			dest.modeleContratDateIds.add(src.modeleContratDateId); 
			break;
			
		case POUR_PLUSIEURS_DATES: 
			dest.modeleContratDateIds.addAll(src.modeleContratDateIds.stream().map(e->e.id).collect(Collectors.toList())); 
			break;

		default:
			throw new AmapjRuntimeException();

		}
		
		
		dest.contrainteDate = src.contrainteDate;
		dest.contrainteOperateur = src.contrainteOperateur;
		dest.contrainteProduit = src.contrainteProduit;
		dest.val = src.val;	
		
		dest.champApplication = src.champApplication;
		dest.libPersonnalise = src.activateLibPersonnalise==ChoixOuiNon.NON ? null : src.libPersonnalise;
		
		return dest;
	}







	/**
	 * 
	 * 
	 */
	public void insertInfoVerifSaisie(RdbLink em, ModeleContrat mc, MonContratDTO m, ModeSaisie modeSaisie) 
	{
		if (mc.regleSaisieModeleContrat==null)
		{
			return;
		}
		
		m.verifRegleSaisieDTO = new VerifRegleSaisieModeleContratDTO();
		m.verifRegleSaisieDTO.contratDTO = m.contratDTO;
		m.verifRegleSaisieDTO.modeSaisie = modeSaisie;
		m.verifRegleSaisieDTO.regleSaisies = getRegleSaisieModeleContratDTO(em, mc).regleSaisies;
		
	}

	
}
