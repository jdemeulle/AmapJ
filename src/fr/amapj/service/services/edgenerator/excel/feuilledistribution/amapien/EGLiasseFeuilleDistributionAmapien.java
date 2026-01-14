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

 package fr.amapj.service.services.edgenerator.excel.feuilledistribution.amapien;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.persistence.Query;

import fr.amapj.common.DateUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.service.engine.generator.excel.AbstractExcelGenerator;
import fr.amapj.service.engine.generator.excel.ExcelFormat;
import fr.amapj.service.engine.generator.excel.ExcelGeneratorTool;
import fr.amapj.service.services.edgenerator.excel.feuilledistribution.amapien.EGFeuilleDistributionAmapien.EGMode;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;

/**
 * Permet l'impression toutes les feuilles de distribution amapien  
 * d'un contrat
 * 
 *  
 *
 */
public class EGLiasseFeuilleDistributionAmapien  extends AbstractExcelGenerator
{
	
	Long modeleContratId;
	
	public EGLiasseFeuilleDistributionAmapien(Long modeleContratId)
	{
		this.modeleContratId = modeleContratId;
	}
	
	@Override
	public void fillExcelFile(RdbLink em,ExcelGeneratorTool et)
	{	
		ModeleContrat mc = em.find(ModeleContrat.class, modeleContratId);
		
		List<Contrat> contrats = getContrat(em, mc);
		
		for (Contrat contrat : contrats)
		{
			Utilisateur u = contrat.utilisateur;
			new EGFeuilleDistributionAmapien(EGMode.STD,modeleContratId,contrat.getId()).addOnePage(em, et, u.nom+" "+u.prenom);
		}
		
		// Si pas de contrats : on met une feuille avec cette info, sinon le fichier est illisible 
		if (contrats.size()==0)
		{
			et.addSheet("CONTRATS", 1, 20);
		    
			SimpleDateFormat df1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			
			// Ligne 1 à 5
			et.addRow("AUCUN CONTRAT SIGNÉ !!!",et.grasGaucheNonWrappe);
			et.addRow(mc.nom,et.grasGaucheNonWrappe);
			et.addRow(mc.description,et.grasGaucheNonWrappe);
			et.addRow("Extrait le "+df1.format(DateUtils.getDate()),et.grasGaucheNonWrappe);
		}
		
	}

	

	/**
	 * Retrouve la liste de tous les contrats pour ce modele, triés par nom prenom de l'utilisateur 
	 * 
	 */
	private List<Contrat> getContrat(RdbLink em, ModeleContrat mc)
	{
		// On récupère ensuite la liste de tous les contrats de ce modele de contrat, trié par nom d'utilisateur
		Query q = em.createQuery("select c from Contrat c WHERE c.modeleContrat=:mc order by c.utilisateur.nom, c.utilisateur.prenom");
		q.setParameter("mc",mc);
		List<Contrat> cs = q.getResultList();
		return cs;
	}

	@Override
	public String getFileName(RdbLink em)
	{
		ParametresDTO param = new ParametresService().getParametres();

		ModeleContrat mc = em.find(ModeleContrat.class, modeleContratId);
		return "distri-amapien-"+param.nomAmap+"-"+mc.nom;
	}


	@Override
	public String getNameToDisplay(RdbLink em)
	{
		ModeleContrat mc = em.find(ModeleContrat.class, modeleContratId);
		return "toutes les feuilles de distribution amapien";
	}
	
	@Override
	public ExcelFormat getFormat()
	{
		return ExcelFormat.XLS;
	}
	
	

	public static void main(String[] args) throws IOException
	{
		new EGLiasseFeuilleDistributionAmapien(8342L).test(); 
	}

}

