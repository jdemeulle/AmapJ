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

 package fr.amapj.service.services.edgenerator.excel.mntlivre;

import java.util.List;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.collections.G2D;
import fr.amapj.common.periode.PeriodeManager.Periode;
import fr.amapj.common.periode.TypPeriode;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.contrat.reel.ContratCell;
import fr.amapj.model.models.fichierbase.Producteur;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.service.engine.generator.excel.AbstractExcelGenerator;
import fr.amapj.service.engine.generator.excel.ExcelFormat;
import fr.amapj.service.engine.generator.excel.ExcelGeneratorTool;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;

/**
 * Permet la generation d'un bilan des montants livrés avec
 *  
 * * un onglet par periode 
 * * pour chaque periode  un tableau avec en colonne les producteurs et en ligne les adhérents  
 */
public class EGProducteurMntLivre extends AbstractExcelGenerator
{
	private TypPeriode typPeriode;
	private MntLivreTools tools;
	

	public EGProducteurMntLivre(TypPeriode typPeriode,int nbJourAvant,int nbJourApres)
	{
		this.tools = new MntLivreTools(typPeriode, nbJourAvant, nbJourApres);
		this.typPeriode = typPeriode;
	}
	
	@Override
	public void fillExcelFile(RdbLink em,ExcelGeneratorTool et)
	{
		tools.fillExcelFile(em, et, periode->fillTab(em, et, periode));
	}
	
	
	

	private void fillTab(RdbLink em, ExcelGeneratorTool et, Periode periode)
	{
		String nomPeriode = periode.getLib();
		
		// On recherche touutes les livraisons concernées
		List<ContratCell> cs = tools.getContratCell(em, periode);
		
		// On réalise une projection 2D de ces livraisons
		// En ligne les adherents   et en colonne les producteurs  
		G2D<Utilisateur,Producteur,ContratCell> c1 = new G2D<>();
		
		// 
		c1.fill(cs);
		c1.groupByLig(e->e.contrat.utilisateur);
		c1.groupByCol(e->e.contrat.modeleContrat.producteur);
		
		// Tri par nom prenom des lignes
		c1.sortLig(e->e.nom,true);
		c1.sortLig(e->e.prenom,true);
		
		// Tri des colonnes par producteur 
		c1.sortCol(e->e.nom,true);
		
		// Pas de tri sur les cellules
		c1.compute();
		
		
		// On en deduit la liste des titre de lignes et de colonnes
		List<Utilisateur> ligs = c1.getLigs();
		List<Producteur> cols = c1.getCols();
		
		
		// Les colonnes en + sont le nom de l'amapien et le total
		int nbCol =  cols.size()+2;		
		et.addSheet(nomPeriode, nbCol, 25);
		et.setColumnWidth(0, 50);
		et.setColumnWidth(1, 15);

		
		// Ecriture de la ligne de titre avec le nom des producteurs 
		et.addRow();
		et.setRowHeigth(2);
		et.setCell(0, "", et.grasCentreBordureGray);
		et.setCell(1, "", et.grasCentreBordureGray);
		int index = 2;
		for (Producteur u : cols)
		{
			et.setCell(index, u.nom, et.grasCentreBordureGray);
			index++;
		}

		et.createFreezePane(1, 1);
		
		tools.addAllLines(ligs, nbCol, em, et, c1);
		
	}


	
	@Override
	public String getFileName(RdbLink em)
	{
		ParametresDTO param = new ParametresService().getParametres();

		switch (typPeriode)
		{
		case MOIS: return "mnt-livre-producteur-mois-"+param.nomAmap;
		case JOUR: return "mnt-livre-producteur-jour-"+param.nomAmap;
		default: throw new AmapjRuntimeException();
		}
		
	}
	

	@Override
	public String getNameToDisplay(RdbLink em)
	{
		switch (typPeriode)
		{
		case MOIS: return "Montant livré pour chaque producteur et pour chaque amapien, avec une feuille par mois";
		case JOUR: return "Montant livré pour chaque producteur et pour chaque amapien, avec une feuille par jour";
		default: throw new AmapjRuntimeException();
		}
	}
	
	
	
	
	@Override
	public ExcelFormat getFormat()
	{
		return ExcelFormat.XLS;
	}
	
}
