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

 package fr.amapj.service.services.edgenerator.excel.stats;

import java.util.List;

import javax.persistence.TypedQuery;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.CollectionUtils;
import fr.amapj.common.collections.G2D;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.service.engine.generator.excel.AbstractExcelGenerator;
import fr.amapj.service.engine.generator.excel.ExcelFormat;
import fr.amapj.service.engine.generator.excel.ExcelGeneratorTool;
import fr.amapj.service.services.gestioncontratsigne.ContratSigneDTO;
import fr.amapj.service.services.gestioncontratsigne.GestionContratSigneService;
import fr.amapj.service.services.gestioncotisation.BilanAdhesionDTO;
import fr.amapj.service.services.gestioncotisation.GestionCotisationService;
import fr.amapj.service.services.gestioncotisation.PeriodeCotisationDTO;
import fr.amapj.service.services.gestioncotisation.PeriodeCotisationUtilisateurDTO;


/**
 * Permet la generation d'un bilan avec 
 * * un onglet par periode de cotisation 
 * * pour chaque periode de cotisation, un tableau avec en colonne les adhérents et en ligne les contrats 
 */
public class EGStatPeriodeContratAdherent extends AbstractExcelGenerator
{
	
	
	public EGStatPeriodeContratAdherent()
	{
	}
	
	@Override
	public void fillExcelFile(RdbLink em,ExcelGeneratorTool et)
	{
		// Récuperation de la liste des periodes , triés par date de debut décroissante
		List<PeriodeCotisationDTO> periodes = new GestionCotisationService().getAll();
		CollectionUtils.sort(periodes, e->e.dateDebut,false);
		
		// Traitement de chaque periode
		for (PeriodeCotisationDTO periode : periodes)
		{
			fillTab(em,et,periode);
		}
		
		// On ajoute un onglet pour les contrats non rattachés à une période de cotisation
		fillTab(em,et,null);
		
	}
	
	
	private void fillTab(RdbLink em, ExcelGeneratorTool et, PeriodeCotisationDTO periode)
	{
		String nomPeriode = periode==null ? "CONTRATS SANS PÉRIODE DE COTISATION" : periode.nom;
		Long idPeriode = periode==null ? null : periode.id;
		
		// On recherche tous contrats signés relatifs à cette periode de cotisation 
		List<Contrat> cs = getContrat(em, idPeriode);
		
		// On réalise une projection 2D de ces contrats
		// En ligne les modeles de contrats , en colonne, les adherents  
		G2D<ModeleContrat,Utilisateur,Contrat> c1 = new G2D<>();
		
		// 
		c1.fill(cs);
		c1.groupByLig(e->e.modeleContrat);
		c1.groupByCol(e->e.utilisateur);
		
		// Tri par nom prenom des colonnes
		c1.sortCol(e->e.nom,true);
		c1.sortCol(e->e.prenom,true);
		
		// Tri des lignes par nom
		c1.sortLig(e->e.nom,true);
		
		// Pas de tri sur les cellules
		c1.compute();
		
		
		// On en deduit la liste des titre de lignes et de colonnes
		List<ModeleContrat> ligs = c1.getLigs();
		List<Utilisateur> cols = c1.getCols();
		
		
		// Les colonnes en + sont le nom du contrat et le total
		int nbCol =  cols.size()+2;		
		et.addSheet(nomPeriode, nbCol, 25);
		et.setColumnWidth(0, 50);
		et.setColumnWidth(1, 15);

		
		// Ecriture de la ligne de titre
		et.addRow();
		et.setRowHeigth(2);
		et.createFreezePane(1, 1);
		
		et.setCell(0, "Nom du contrat", et.grasCentreBordureGray);
		et.setCell(1, "Total", et.grasCentreBordureGray);
		
		int index = 2;
		for (Utilisateur u : cols)
		{
			et.setCell(index, u.nom+" "+u.prenom, et.grasCentreBordureGray);
			index++;
		}
		
		// Ecriture des lignes de contrats
		for (int i = 0; i < ligs.size(); i++)
		{
			ModeleContrat mc = ligs.get(i);
			List<List<Contrat>> contrats = c1.getLine(i);
			addRow(mc,contrats,et,em);
		}
		
		// Une ligne vide
		et.addRow();
		
		// Une ligne total
		if (ligs.size()>0)
		{
			et.addRow();
			et.setCell(0, "Total des contrats", et.grasCentreBordureGray);
			for (int i = 1; i < nbCol; i++) 
			{
				et.setCellSumInColUp(i, 2, ligs.size(), et.prixCentreBordureGray);
			}
		}
	
		// Une ligne vide
		et.addRow();
		
		// Deux lignes avec les adhésions
		if (idPeriode!=null)
		{
			BilanAdhesionDTO bilan = new GestionCotisationService().loadBilanAdhesion(idPeriode);
			et.addRow();
			et.setCell(0, "Montant adhésion", et.grasCentreBordure);
			if (cols.size()>0)
			{
				et.setCellSumInRow(1, 2, cols.size(), et.prixCentreBordure);
			}
			index = 2;
			
			for (Utilisateur u : cols)
			{
				PeriodeCotisationUtilisateurDTO pcu = findPeriodeCotisationUtilisateurDTO(bilan,u.id);
				if (pcu==null)
				{
					et.setCell(index, "NA", et.grasCentreBordure);
				}
				else
				{
					et.setCellPrix(index, pcu.montantAdhesion, et.prixCentreBordure);
				}
				index++;
			}
			et.addRow();
			et.setCell(0, "État adhésion", et.grasCentreBordure);
			et.setCell(1, "", et.nonGrasCentreBordure);
			
			index = 2;
			
			for (Utilisateur u : cols)
			{
				PeriodeCotisationUtilisateurDTO pcu = findPeriodeCotisationUtilisateurDTO(bilan,u.id);
				if (pcu==null)
				{
					et.setCell(index, "NA", et.grasCentreBordure);
				}
				else
				{
					et.setCell(index, ""+pcu.etatPaiementAdhesion, et.nonGrasCentreBordure);
				}
				index++;
			}
		}
	}


	private PeriodeCotisationUtilisateurDTO findPeriodeCotisationUtilisateurDTO(BilanAdhesionDTO bilan, Long idUtilisateur) 
	{
		return bilan.utilisateurDTOs.stream().filter(e->e.idUtilisateur.equals(idUtilisateur)).findAny().orElse(null);
	}

	private List<Contrat> getContrat(RdbLink em,Long idPeriodeCotisation)
	{
		if (idPeriodeCotisation!=null)
		{
			TypedQuery<Contrat> q = em.createQuery("select c from Contrat c  where c.modeleContrat.periodeCotisation.id = :id ",Contrat.class);
			q.setParameter("id", idPeriodeCotisation);
			return q.getResultList();
		}
		else
		{
			TypedQuery<Contrat> q = em.createQuery("select c from Contrat c  where c.modeleContrat.periodeCotisation is NULL ",Contrat.class);
			return q.getResultList();
		}
	}

	private void addRow(ModeleContrat mc, List<List<Contrat>> contrats, ExcelGeneratorTool et, RdbLink em)
	{
		et.addRow();
		et.setCell(0, mc.nom, et.grasCentreBordure);
		
		if (contrats.size()>0)
		{
			et.setCellSumInRow(1, 2, contrats.size(), et.prixCentreBordure);
		}
		
		int index = 2;
		for (List<Contrat> cs : contrats)
		{
			Contrat c = getContrat(cs);
			if (c==null)
			{
				et.setCellPrix(index, 0, et.prixCentreBordure);
			}
			else
			{
				ContratSigneDTO dto = new GestionContratSigneService().createContratSigneInfo(em, c);
				int montant = dto.mntCommande;
				et.setCellPrix(index, montant, et.prixCentreBordure);
			}
			index++;
		}
		
	}

	private Contrat getContrat(List<Contrat> cs)
	{
		if (cs.size()==0)
		{
			return null;
		}
		else if (cs.size()==1)
		{
			return cs.get(0);
		}
		else
		{
			throw new AmapjRuntimeException("cs size = "+cs.size());
		}
	}


	
	@Override
	public String getFileName(RdbLink em)
	{
		return "cotisation-contrat-adherent";
	}
	

	@Override
	public String getNameToDisplay(RdbLink em)
	{
		return "Montant des contrats et de l'adhésion pour chaque amapien, avec une feuille par période de cotisation";
	}
	
	
	
	
	@Override
	public ExcelFormat getFormat()
	{
		return ExcelFormat.XLSX;
	}
	
}
