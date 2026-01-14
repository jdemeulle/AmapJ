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

 package fr.amapj.service.services.edgenerator.excel.permanence;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import fr.amapj.common.CollectionUtils;
import fr.amapj.common.DateUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.permanence.periode.PeriodePermanence;
import fr.amapj.service.engine.generator.excel.AbstractExcelGenerator;
import fr.amapj.service.engine.generator.excel.ExcelFormat;
import fr.amapj.service.engine.generator.excel.ExcelGeneratorTool;
import fr.amapj.service.services.permanence.periode.PeriodePermanenceDTO;
import fr.amapj.service.services.permanence.periode.PeriodePermanenceDateDTO;
import fr.amapj.service.services.permanence.periode.PeriodePermanenceService;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;

/**
 * Permet la generation du planning de dsitribution au format Excel 
 * 
 *  
 *
 */
public class EGPlanningPermanence extends AbstractExcelGenerator
{
	private Long idPeriodePermanence;
	
	// Date de départ - toutes les dates avant cette date là ne seront pas dans le fichier généré  
	// Cette date peut être nulle, dans ce cas pas de filtre 
	private Date startingDate;
	
	public EGPlanningPermanence(Long idPeriodePermanence,Date startingDate)
	{
		this.idPeriodePermanence = idPeriodePermanence;
		this.startingDate = startingDate;
	}
	
	@Override
	public void fillExcelFile(RdbLink em,ExcelGeneratorTool et)
	{
		addSheet(et,false);
		addSheet(et,true);
	}
	

	private void addSheet(ExcelGeneratorTool et, boolean withRole) 
	{
		PeriodePermanenceDTO dto = new PeriodePermanenceService().loadPeriodePermanenceDTO(idPeriodePermanence);
		SimpleDateFormat df = new SimpleDateFormat("EEEEE dd MMMMM yyyy");

		
		if (withRole)
		{
			// Il y a systèmatiquement 4 colonnes
			et.addSheet("Planning avec rôles", 4, 56);
			et.setColumnWidth(0, 2);
			et.setColumnWidth(2, 2);
		}
		else
		{
			// Il y a systèmatiquement 6 colonnes
			et.addSheet("Planning sans rôles", 6, 28);
			et.setColumnWidth(0, 2);
			et.setColumnWidth(2, 2);
			et.setColumnWidth(4, 2);			
		}
	
				
		et.addRow("Planning des permanences",et.grasGaucheNonWrappe);
		et.addRow(dto.nom,et.grasGaucheNonWrappe);
		et.addRow("",et.grasGaucheNonWrappe);
		
		
		List<PeriodePermanenceDateDTO> datesToProcess = dto.datePerms;
		if (startingDate!=null)
		{
			startingDate = DateUtils.suppressTime(startingDate);
			datesToProcess = CollectionUtils.filter(datesToProcess,e->( e.datePerm.after(startingDate) || e.datePerm.equals(startingDate) ));
		}
		
		// 3 colonnes si les roles ne sont pas affichées, 2 sinon 
		int nbCol = withRole ? 2 : 3;
		List<List<PeriodePermanenceDateDTO>> lines = CollectionUtils.cutInSubList(datesToProcess, nbCol);
		
		
		for (List<PeriodePermanenceDateDTO> line : lines)
		{
			processOneLine(line,et,df,withRole);
		}	
		
	}

	private void processOneLine(List<PeriodePermanenceDateDTO> line, ExcelGeneratorTool et,SimpleDateFormat df, boolean withRole)
	{
	
		// Ligne de titre
		et.addRow();
		int index =1;
		for (PeriodePermanenceDateDTO distributionDTO : line)
		{
			et.setCell(index, df.format(distributionDTO.datePerm), et.grasCentreBordure);
			index = index +2;
		}
		
		// Ligne avec les noms
		et.addRow();
		index =1;
		int maxLine = 1;
		for (PeriodePermanenceDateDTO distributionDTO : line)
		{
			String str = withRole ? distributionDTO.getNomInscritWithRoles("\n") : distributionDTO.getNomInscrit("\n");
			et.setCell(index, str, et.grasCentreBordure);
			
			maxLine = Math.max(maxLine, distributionDTO.getNbInscrit());
			
			index = index +2;
		}
		et.setRowHeigth(maxLine+1);
		
		// Une ligne vide
		et.addRow();
	}


	@Override
	public String getFileName(RdbLink em)
	{
		ParametresDTO param = new ParametresService().getParametres();
		
		PeriodePermanence pp = em.find(PeriodePermanence.class, idPeriodePermanence);
		return "planning-permanence-"+param.nomAmap+"-"+pp.nom; 
	}

	@Override
	public String getNameToDisplay(RdbLink em)
	{
		PeriodePermanence pp = em.find(PeriodePermanence.class, idPeriodePermanence);
		return "le planning des permanences "+pp.nom;
	}
	
	@Override
	public ExcelFormat getFormat()
	{
		return ExcelFormat.XLS;
	}

	public static void main(String[] args) throws IOException
	{
		new EGPlanningPermanence(1L,null).test();
	}

}
