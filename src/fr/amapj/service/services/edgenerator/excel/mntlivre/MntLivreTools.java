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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import fr.amapj.common.DateUtils;
import fr.amapj.common.collections.G2D;
import fr.amapj.common.periode.PeriodeManager;
import fr.amapj.common.periode.PeriodeManager.Periode;
import fr.amapj.common.periode.TypPeriode;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.contrat.reel.ContratCell;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.service.engine.generator.excel.ExcelGeneratorTool;


/**
 * Partie commune sur les montants livrés   
 */
public class MntLivreTools 
{
	
	
	private int nbJourAvant;
	private int nbJourApres;
	private TypPeriode typPeriode;
	

	public MntLivreTools(TypPeriode typPeriode,int nbJourAvant,int nbJourApres)
	{
		this.typPeriode = typPeriode;
		this.nbJourAvant = nbJourAvant;
		this.nbJourApres = nbJourApres;
	}
	
	public void fillExcelFile(RdbLink em,ExcelGeneratorTool et,Consumer<Periode> fillTab)
	{
		// Récuperation de la liste des periodes 
		LocalDateTime now = DateUtils.getLocalDateTime();
		PeriodeManager pm = new PeriodeManager(now, typPeriode, nbJourAvant, nbJourApres,(a,b)->getAllowedDate(em,a,b));
		List<Periode> periodes = pm.getAllPeriodes();
		
		// Traitement de chaque periode
		for (Periode periode : periodes)
		{
			fillTab.accept(periode);
		}	
	}
	
	
	

	public void addAllLines(List<Utilisateur> ligs,int nbCol,RdbLink em, ExcelGeneratorTool et,G2D<Utilisateur,?,ContratCell> c1)
	{
		// Une ligne total
		if (ligs.size()>0)
		{
			et.addRow();
			et.setCell(0, "Total des contrats", et.grasCentreBordureGray);
			for (int i = 1; i < nbCol; i++) 
			{
				et.setCellSumInColDown(i, 1, ligs.size(), et.prixCentreBordureGray);
			}
		}
		
		
		// Ecriture des lignes pour chaque utilisateur
		for (int i = 0; i < ligs.size(); i++)
		{
			Utilisateur u = ligs.get(i);
			List<List<ContratCell>> contrats = c1.getLine(i);
			addRow(u,contrats,et,em);
		}
	
	}
	
	
	/**
	 * Retourne la liste de toutes les livraisons concernées
	 */
	public List<ContratCell> getContratCell(RdbLink em, Periode periode)
	{
		
		String query = "select c from ContratCell c WHERE c.modeleContratDate.dateLiv >= :d1 AND c.modeleContratDate.dateLiv<=:d2 ";
		TypedQuery<ContratCell> q = em.createQuery(query,ContratCell.class);
				
		q.setParameter("d1",DateUtils.asDate(periode.startDate));
		q.setParameter("d2",DateUtils.asDate(periode.endDate));
		
		return q.getResultList();
	}

	private List<LocalDate> getAllowedDate(RdbLink em, LocalDate startDate, LocalDate endDate)
	{
		String query = "select distinct(mcd.dateLiv) from ModeleContratDate mcd WHERE mcd.dateLiv >= :d1 AND mcd.dateLiv<=:d2 ";
		TypedQuery<Date> q = em.createQuery(query,Date.class);
				
		q.setParameter("d1",DateUtils.asDate(startDate));
		q.setParameter("d2",DateUtils.asDate(endDate));
		
		return q.getResultList().stream().map(e->DateUtils.asLocalDate(e)).collect(Collectors.toList());
	}

	

	private void addRow(Utilisateur u, List<List<ContratCell>> cells, ExcelGeneratorTool et, RdbLink em)
	{
		et.addRow();
		et.setCell(0, u.nom+" "+u.prenom, et.nonGrasCentreBordure);
	
		if (cells.size()>0)
		{
			et.setCellSumInRow(1, 2, cells.size(), et.prixCentreBordure);
		}
		
		int index = 2;
		for (List<ContratCell> cs : cells)
		{
			int montant = cs.stream().mapToInt(e->e.qte*e.modeleContratProduit.prix).sum();
			et.setCellPrix(index, montant, et.prixCentreBordure);
			index++;
		}
	}
}
