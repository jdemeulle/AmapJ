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

import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import fr.amapj.common.DateUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.contrat.modele.EtatModeleContrat;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.service.engine.generator.excel.AbstractExcelGenerator;
import fr.amapj.service.engine.generator.excel.ExcelFormat;
import fr.amapj.service.engine.generator.excel.ExcelGeneratorTool;
import fr.amapj.service.services.edgenerator.excel.feuilledistribution.amapien.EGFeuilleDistributionAmapien;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.gestioncontrat.ModeleContratSummaryDTO;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;


/**
 * Permet de generer un fichier avec l'ensemble des contrats vierges
 */
public class EGAllContratsVierges extends AbstractExcelGenerator
{
	
	private EGMode egMode;

	public enum EGMode
	{
		// Tous les contrats actifs  
		ACTIFS ,
		
		// Tous les contrats avec une date de livraison dans le futur et actifs 
		FUTUR
	}
	
	public EGAllContratsVierges(EGMode egMode)
	{
		this.egMode = egMode;
	}

	@Override
	public void fillExcelFile(RdbLink em,ExcelGeneratorTool et)
	{
		TypedQuery<ModeleContrat> q;
		if (egMode==EGMode.ACTIFS)
		{
			q = em.createQuery("select mc from ModeleContrat mc where mc.etat =:etat order by mc.producteur.nom,mc.nom",ModeleContrat.class);
			q.setParameter("etat", EtatModeleContrat.ACTIF);
		}
		else
		{
			Date ref = DateUtils.getDateWithNoTime();
			q = em.createQuery("select distinct(mcd.modeleContrat) from ModeleContratDate mcd where mcd.dateLiv>=:ref AND mcd.modeleContrat.etat =:etat order by mcd.modeleContrat.producteur.nom,mcd.modeleContrat.nom",ModeleContrat.class);
			q.setParameter("etat", EtatModeleContrat.ACTIF);
			q.setParameter("ref", ref);
		}
		
		
		List<ModeleContrat> mcs = q.getResultList();
		
		addListeModeleContrats(em, et, mcs);
		
		for (int i = 0; i < mcs.size(); i++) 
		{
			ModeleContrat mc = mcs.get(i);
			new EGFeuilleDistributionAmapien(EGFeuilleDistributionAmapien.EGMode.UN_VIERGE, mc.id, null).addOnePage(em, et, "MC"+(i+1));
		}
		
	}

	private void addListeModeleContrats(RdbLink em, ExcelGeneratorTool et, List<ModeleContrat> mcs) 
	{
		ParametresDTO param = new ParametresService().getParametres();

		et.addSheet("Liste", 5, 50);
		et.setColumnWidth(0, 15);
		et.setColumnWidth(1, 60);
		et.setColumnWidth(3, 15);
		et.setColumnWidth(4, 15);
		
		String str;
		if (egMode==EGMode.ACTIFS)
		{
			str = "Liste des modèles de contrats actifs";
		}
		else
		{
			str = "Liste des modèles de contrats actifs ayant au moins une livraison dans le futur";
		}
		
		
		et.addRow(param.nomAmap,et.titre);
		et.addRow(str,et.grasGaucheNonWrappe);
		
		et.addRow();
		
		et.addRow();
		
		
		et.setCell(0, "Numéro", et.grasCentreBordure);

		et.setCell(1, "Nom du modèle de contrat", et.grasGaucheNonWrappeBordure);
		
		et.setCell(2, "Nom du producteur", et.grasGaucheNonWrappeBordure);
				
		et.setCell(3, "Date de début", et.grasCentreBordure);

		et.setCell(4, "Date de fin", et.grasCentreBordure);
		
		
		for (int i = 0; i < mcs.size(); i++) 
		{
			ModeleContrat mc = mcs.get(i);
			ModeleContratSummaryDTO mcDto = new GestionContratService().createModeleContratInfo(em, mc);
			
			et.addRow();
			et.setCell(0, "MC"+(i+1), et.nonGrasCentreBordure);
			et.setCell(1, mc.nom, et.nonGrasGaucheBordure);
			et.setCell(2, mc.producteur.nom, et.nonGrasGaucheBordure);
			et.setCellDate(3, mcDto.dateDebut, et.nonGrasCentreBordure);
			et.setCellDate(4, mcDto.dateFin, et.nonGrasCentreBordure);
		}	
	}



	@Override
	public String getFileName(RdbLink em)
	{
		ParametresDTO param = new ParametresService().getParametres();
		
		if (egMode==EGMode.ACTIFS)
		{
			return "liasse-contrats-vierges-actifs-"+param.nomAmap;
		}
		else
		{
			return "liasse-contrats-vierges-futurs-"+param.nomAmap;
		}
	}
	

	@Override
	public String getNameToDisplay(RdbLink em)
	{
		if (egMode==EGMode.ACTIFS)
		{
			return "La liasse des feuilles de distribution vierges pour tous les modeles de contrats actifs";
		}
		else
		{
			return "La liasse des feuilles de distribution vierges pour tous les modeles de contrats actifs ayant au moins une livraison dans le futur";
		}
	}
	
	@Override
	public ExcelFormat getFormat()
	{
		return ExcelFormat.XLS;
	}

}
