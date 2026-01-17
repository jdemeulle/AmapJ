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
 package fr.amapj.service.services.edgenerator.excel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;

import fr.amapj.common.FormatUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.model.models.contrat.reel.EtatPaiement;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.service.engine.generator.excel.AbstractExcelGenerator;
import fr.amapj.service.engine.generator.excel.ExcelFormat;
import fr.amapj.service.engine.generator.excel.ExcelGeneratorTool;
import fr.amapj.service.services.gestioncontratsigne.GestionContratSigneService;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.mescontrats.DatePaiementDTO;
import fr.amapj.service.services.mescontrats.MesContratsService;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;

/**
 * Permet la generation d'un bilan complet sur les chèques 
 */
public class EGBilanCompletCheque extends AbstractExcelGenerator
{
	Long modeleContratId;
	
	public EGBilanCompletCheque(Long modeleContratId)
	{
		this.modeleContratId = modeleContratId;
	}
	
	
	@Override
	public void fillExcelFile(RdbLink em,ExcelGeneratorTool et)
	{
		ParametresDTO param = new ParametresService().getParametres();

		ModeleContrat mc = em.find(ModeleContrat.class, modeleContratId);
		SimpleDateFormat df = FormatUtils.getLiteralMonthDate();

		// Charge des informations sur le modele
		ContratDTO contratDTO = new MesContratsService().loadContrat(mc.getId(),null);
		
		// Calcul du nombre de colonnes :  Nom + prénom + 1 montant commandé + 1 espace + promis + recu + remis + 1 espace + 1 solde+ 1 ecart chèque + +1 vide + 1 avoir 
		// + 3 colonnes par mois 
		int numColFirstMonth= 12;
		int nbMois = contratDTO.paiement.datePaiements.size();
		int nbCol = numColFirstMonth + 3*nbMois;
		
		et.addSheet("Amap", nbCol, 11);
		et.setColumnWidth(0, 14);
		et.setColumnWidth(1, 12);
		et.setColumnWidth(2, 12);
				
		
		et.addRow(param.nomAmap+" - Bilan des chèques",et.grasGaucheNonWrappe);
		et.addRow("",et.grasGaucheNonWrappe);
		
		et.addRow("Nom du contrat : "+mc.nom,et.grasGaucheNonWrappe);
		et.addRow("Nom du producteur : "+mc.producteur.nom,et.grasGaucheNonWrappe);
		et.addRow("Ordre des chèques : "+mc.libCheque,et.grasGaucheNonWrappe);
		

		
		
		// Avec une sous requete, on obtient la liste de tous les utilisateur ayant commandé au moins un produit
		List<Utilisateur> utilisateurs = new MesContratsService().getUtilisateur(em, mc);
		et.addRow(utilisateurs.size()+" adhérents pour ce contrat",et.grasGaucheNonWrappe);
		et.addRow("",et.grasGaucheNonWrappe);
		

		// Création de la ligne titre des colonnes
		et.addRow();
		et.setCell(0,"Nom",et.grasCentreBordure);
		et.setCell(1,"Prénom",et.grasCentreBordure);
		et.setCell(2,"Montant commandé",et.grasCentreBordure);
		et.addComment(2, "Somme du montant de tous les produits commandés par l'amapien", 2);
				
		et.setCell(4,"Chèques",et.grasCentreBordure);
		et.mergeCellsRight(4, 3);
		
		et.setCell(8,"Solde final",et.grasCentreBordure);
		et.addComment(8, "Est égal à montant commandé - montant chez le producteur - montant de l'avoir", 2);
		et.setCell(9,"Ecart saisie chèques",et.grasCentreBordure);
		et.addComment(9,"différence entre le montant commandé et les chèques prévus (quelque soit leur état). Un écart apparaît quand le contrat a été modifié par le référent, par exemple quand il modifie les quantités sans modifier les chèques. L'écart doit être corrigé manuellement par le référent en corrigeant les chèques.",6);
		
		et.setCell(11,"Avoir initial",et.grasCentreBordure);
		
		for (int i = 0; i < nbMois; i++)
		{
			Date d = contratDTO.paiement.datePaiements.get(i).datePaiement;
			String libMois = df.format(d);
			int numCol = numColFirstMonth+i*3; 
			et.setCell(numCol, libMois,et.switchColor(et.grasCentreBordure, i));
			et.mergeCellsRight(numCol, 3);
		}
		
		// Sous ligne de titre
		et.addRow();
		et.setRowHeigth(2);
		
		et.setCell(4,"A fournir à l'AMAP",et.grasCentreBordure);
		et.addComment(4, "Chèques que les amapiens doivent donner à l'AMAP", 2);
		et.setCell(5,"A l'AMAP",et.grasCentreBordure);
		et.addComment(5, "Chèques qui ont été donnés à l'AMAP, et le référent a marqué ces chèques comme réceptionnés", 2);
		et.setCell(6,"Chez le producteur",et.grasCentreBordure);
		et.addComment(6,"Chèques qui ont été remis au producteur",2);
		
		
		for (int i = 0; i < nbMois; i++)
		{
			// 
			CellStyle st = et.switchColor(et.grasCentreBordure,i);
		
			et.setCell(numColFirstMonth+i*3, "A fournir à l'AMAP",st);
			et.setCell(numColFirstMonth+i*3+1, "A l'AMAP",st);
			et.setCell(numColFirstMonth+i*3+2, "Chez le producteur",st);
				
		}
		
		// Merge des cellules du titre
		et.mergeCellsUp(0,2);
		et.mergeCellsUp(1,2);
		et.mergeCellsUp(2,2);
		et.mergeCellsUp(8,2);
		et.mergeCellsUp(9,2);
		et.mergeCellsUp(11,2);
				
		// Une ligne vide
		et.addRow("",et.grasGaucheNonWrappe);
		
		// Une ligne pour le cumul
		addRowCumul(et,nbMois,utilisateurs,numColFirstMonth);
		
		// Une ligne vide
		et.addRow("",et.grasGaucheNonWrappe);
		
		// Une ligne pour chaque utilisateur 
		for (Utilisateur utilisateur : utilisateurs)
		{
			addRow(utilisateur,et,mc,em,nbMois,numColFirstMonth);
		}
	}

	
	
	
	private void addRowCumul(ExcelGeneratorTool et, int nbMois, List<Utilisateur> utilisateurs, int numColFirstMonth)
	{
		et.addRow();
		
		int nbUser = utilisateurs.size();
		
		et.setCell(0,"Cumul",et.grasGaucheNonWrappeBordure);
		et.setCell(1,"",et.nonGrasGaucheBordure);
		
		
		et.setCellSumInColDown(2, 2, nbUser, et.prixCentreBordure);
		et.setCellSumInColDown(4, 2, nbUser, et.prixCentreBordure);
		et.setCellSumInColDown(5, 2, nbUser, et.prixCentreBordure);
		et.setCellSumInColDown(6, 2, nbUser, et.prixCentreBordure);
		et.setCellSumInColDown(8, 2, nbUser, et.prixCentreBordure);
		et.setCellSumInColDown(9, 2, nbUser, et.prixCentreBordure);
		et.setCellSumInColDown(11, 2, nbUser, et.prixCentreBordure);
		
		
		for (int i = 0; i < nbMois; i++)
		{
			CellStyle st = et.switchColor(et.prixCentreBordure,i);	
			
			et.setCellSumInColDown(numColFirstMonth+i*3, 2, nbUser, st);
			et.setCellSumInColDown(numColFirstMonth+i*3+1, 2, nbUser, st);
			et.setCellSumInColDown(numColFirstMonth+i*3+2, 2, nbUser, st);
		}
		
	}




	private void addRow(Utilisateur utilisateur, ExcelGeneratorTool et, ModeleContrat mc, RdbLink em, int nbMois, int numColFirstMonth)
	{
		Contrat c = new MesContratsService().getContrat(mc.getId(),em,utilisateur);
		ContratDTO contratDTO = new MesContratsService().loadContrat(mc.getId(), c.getId());
		int montantDu = new GestionContratSigneService().getMontant(em, c);
		
		
		et.addRow();
		et.setCell(0,utilisateur.nom,et.grasGaucheNonWrappeBordure);
		et.setCell(1,utilisateur.prenom,et.nonGrasGaucheBordure);
		
		
		et.setCellPrix(2,montantDu,et.prixCentreBordure);
		
		et.setCellSumInRow(4, numColFirstMonth, 3, nbMois, null , et.prixCentreBordureNoZero);
		et.setCellSumInRow(5, numColFirstMonth+1, 3, nbMois, null , et.prixCentreBordureNoZero);
		et.setCellSumInRow(6, numColFirstMonth+2, 3, nbMois, null , et.prixCentreBordureNoZero);
		
		et.setCellBasicFormulaInRow(8,new int[] { 6 , 11 }, new int[] { 2 } , et.prixCentreBordure);
		et.setCellBasicFormulaInRow(9,new int[] { 4 , 5 , 6 , 11 }, new int[] { 2 } , et.prixCentreBordureNoZero);
		
		et.setCellPrix(11, contratDTO.paiement.avoirInitial,et.prixCentreBordure);
		
		for (int i = 0; i < nbMois; i++)
		{
			DatePaiementDTO dateDto=contratDTO.paiement.datePaiements.get(i);
			
			CellStyle st = et.switchColor(et.prixCentreBordureNoZero,i);	
			
			et.setCellPrix(numColFirstMonth+i*3, getPromis(dateDto),st);
			et.setCellPrix(numColFirstMonth+i*3+1, getRecu(dateDto),st);
			et.setCellPrix(numColFirstMonth+i*3+2, getRemis(dateDto),st);
		}
	}



	private int getPromis(DatePaiementDTO dateDto)
	{
		return dateDto.etatPaiement==EtatPaiement.A_FOURNIR ? dateDto.montant : 0;
	}
	
	private int getRecu(DatePaiementDTO dateDto)
	{
		return dateDto.etatPaiement==EtatPaiement.AMAP ? dateDto.montant : 0;
	}

	private int getRemis(DatePaiementDTO dateDto)
	{
		return dateDto.etatPaiement==EtatPaiement.PRODUCTEUR ? dateDto.montant : 0;
	}


	

	@Override
	public String getFileName(RdbLink em)
	{
		ParametresDTO param = new ParametresService().getParametres();
		ModeleContrat mc = em.find(ModeleContrat.class, modeleContratId);
		return "bilan-cheque-"+param.nomAmap+"-"+mc.nom;
	}


	@Override
	public String getNameToDisplay(RdbLink em)
	{
		return "le bilan complet chèques";
	}


	@Override
	public ExcelFormat getFormat()
	{
		return ExcelFormat.XLS;
	}



}
