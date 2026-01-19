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
 package fr.amapj.service.services.edgenerator.excel.nouvelarrivant;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import fr.amapj.common.DateUtils;
import fr.amapj.common.FormatUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.contrat.modele.EtatModeleContrat;
import fr.amapj.service.engine.generator.excel.AbstractExcelGenerator;
import fr.amapj.service.engine.generator.excel.ExcelFormat;
import fr.amapj.service.engine.generator.excel.ExcelGeneratorTool;
import fr.amapj.service.services.visiteamap.VisiteAmapDTO;
import fr.amapj.service.services.visiteamap.VisiteAmapDTO.Contrat;
import fr.amapj.service.services.visiteamap.VisiteAmapDTO.Jour;
import fr.amapj.service.services.visiteamap.VisiteAmapDTO.LigneContrat;
import fr.amapj.service.services.visiteamap.VisiteAmapDTO.Producteur;
import fr.amapj.service.services.visiteamap.VisiteAmapService;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;


/**
 * Permet de generer un fichier avec une vision style VisiteAmap
 */
public class EGVisiteAmap extends AbstractExcelGenerator
{
	
	private EGMode egMode;
	
	private SimpleDateFormat df1 = FormatUtils.getFullDate();
	
	private SimpleDateFormat df = FormatUtils.getStdDate();
	
	private static final char BULLET_CHARACTER = '\u2022';

	public enum EGMode
	{
		// Tous les contrats actifs  
		ACTIFS ,
		
		// Tous les contrats actifs  et sur les dates futures 
		FUTUR
	}
	
	public EGVisiteAmap(EGMode egMode)
	{
		this.egMode = egMode;
	}

	@Override
	public void fillExcelFile(RdbLink em,ExcelGeneratorTool et)
	{
		ParametresDTO param = new ParametresService().getParametres();

		// Récupération de toutes les dates de livraison 
		TypedQuery<Date> q = em.createQuery("select distinct(mcd.dateLiv) from ModeleContratDate mcd where mcd.modeleContrat.etat =:etat order by mcd.dateLiv",Date.class);
		q.setParameter("etat", EtatModeleContrat.ACTIF);
		
		List<Date> dateLivs = q.getResultList();
		if (dateLivs.size()==0)
		{
			et.addSheet("Découverte / Visite", 1, 50);
			et.addRow(param.nomAmap,et.titre);
			et.addRow("Aucune livraison pour aucune date",et.grasGaucheNonWrappe);
			return;
		}
		
		Date dateDebut = dateLivs.get(0);
		Date dateFin = dateLivs.get(dateLivs.size()-1);
		
		
		if (egMode==EGMode.FUTUR)
		{
			dateDebut = DateUtils.getDateWithNoTime();
		}
		
		VisiteAmapDTO dto = new VisiteAmapService().getAll(dateDebut, dateFin, null);
		
		if (dto.jours.size()==0)
		{				
			et.addSheet("Découverte / Visite", 1, 50);
			et.addRow(param.nomAmap,et.titre);
			et.addRow("Aucune livraison pour aucune date",et.grasGaucheNonWrappe);
			return;
		}
		
		for (Jour jour : dto.jours) 
		{
			addOnePage(jour,et);
		}
	}

	


	private void addOnePage(Jour jour, ExcelGeneratorTool et) 
	{
		ParametresDTO param = new ParametresService().getParametres();

		et.addSheet(df.format(jour.dateLiv), 1, 200);
		
		// La date 
		et.addRow(param.nomAmap,et.titre);
		et.addRow(df1.format(jour.dateLiv),et.grasGaucheNonWrappe);
		et.addRow();
		
		// On affiche chaque bloc PRODUCTEUR
		for (Producteur producteur : jour.producteurs) 
		{
			addBlocProducteur(producteur,et);
		}	
		
	}

	private void addBlocProducteur(Producteur producteur, ExcelGeneratorTool et) 
	{
		// On affiche chaque bloc CONTRAT
		for (Contrat contrat : producteur.contrats) 
		{
			addBlocContrat(contrat,et,producteur.producteurNom);
		}	
	}
	
	private void addBlocContrat(Contrat contrat, ExcelGeneratorTool et,String producteurNom) 
	{
		
		et.addRow("Producteur : "+producteurNom+" Contrat : "+contrat.contratNom,et.grasGaucheNonWrappe);
		et.addRow();
		for (LigneContrat lig : contrat.ligneContrats) 
		{
			et.addRow(" "+BULLET_CHARACTER+" "+lig.produitNom+", "+lig.produitConditionnement+" - Prix : "+FormatUtils.prix(lig.produitPrix),et.nonGrasGaucheNonWrappe);
		}
		et.addRow();
	}

	@Override
	public String getFileName(RdbLink em)
	{
		ParametresDTO param = new ParametresService().getParametres();
		
		if (egMode==EGMode.ACTIFS)
		{
			return "découverte-"+param.nomAmap;
		}
		else
		{
			return "découverte-futur-"+param.nomAmap;
		}
	}
	

	@Override
	public String getNameToDisplay(RdbLink em)
	{
		if (egMode==EGMode.ACTIFS)
		{
			return "La description des produits proposés pour toutes les dates de livraisons (passées ou futures)";
		}
		else
		{
			return "La description des produits proposés pour toutes les dates de livraisons futures";
		}
	}
	
	@Override
	public ExcelFormat getFormat()
	{
		return ExcelFormat.XLS;
	}

}
