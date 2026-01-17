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

import java.io.IOException;

import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.param.ChoixOuiNon;
import fr.amapj.model.models.param.paramecran.PEReceptionCheque;
import fr.amapj.model.models.remise.RemiseProducteur;
import fr.amapj.service.engine.generator.excel.AbstractExcelGenerator;
import fr.amapj.service.engine.generator.excel.ExcelFormat;
import fr.amapj.service.engine.generator.excel.ExcelGeneratorTool;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.remiseproducteur.PaiementRemiseDTO;
import fr.amapj.service.services.remiseproducteur.RemiseDTO;
import fr.amapj.service.services.remiseproducteur.RemiseProducteurService;
import fr.amapj.view.engine.menu.MenuList;


/**
 * Permet la generation de la feuille d'une remise
 * 
 *  
 *
 */
public class EGRemise extends AbstractExcelGenerator
{
	
	Long remiseId;
	
	public EGRemise(Long remiseId)
	{
		this.remiseId = remiseId;
	}
	
	@Override
	public void fillExcelFile(RdbLink em,ExcelGeneratorTool et)
	{
		RemiseProducteur remise = em.find(RemiseProducteur.class, remiseId);
		ModeleContrat mc = remise.datePaiement.modeleContrat;
		RemiseDTO dto = new RemiseProducteurService().loadRemise(remiseId);
		
		PEReceptionCheque peConf = (PEReceptionCheque) new ParametresService().loadParamEcran(MenuList.RECEPTION_CHEQUES);
		
		// Calcul du nombre de colonnes :  Nom + prénom + 1 montant du chéque + commentaire 1 + commentaire 2 +commentaire 3 + commentaire 4
		et.addSheet(dto.moisRemise, 7, 20);
				
		et.addRow("Remise de chèques du "+dto.moisRemise,et.grasGaucheNonWrappe);
		et.addRow("",et.grasGaucheNonWrappe);
		
		et.addRow("Nom du contrat : "+mc.nom,et.grasGaucheNonWrappe);
		et.addRow("Nom du producteur : "+mc.producteur.nom,et.grasGaucheNonWrappe);
		et.addRow("Ordre des chèques : "+mc.libCheque,et.grasGaucheNonWrappe);
	
		
		et.addRow(dto.paiements.size()+" chèques dans cette remise",et.grasGaucheNonWrappe);
		et.addRow("",et.grasGaucheNonWrappe);
		

		// Création de la ligne titre des colonnes
		et.addRow();
		et.setCell(0,"Nom",et.grasCentreBordure);
		et.setCell(1,"Prénom",et.grasCentreBordure);
		et.setCell(2,"Montant chèques",et.grasCentreBordure);
		addTitreCommentaire(et,3,peConf.saisieCommentaire1,peConf.libSaisieCommentaire1);
		addTitreCommentaire(et,4,peConf.saisieCommentaire2,peConf.libSaisieCommentaire2);
		addTitreCommentaire(et,5,peConf.saisieCommentaire3,peConf.libSaisieCommentaire3);
		addTitreCommentaire(et,6,peConf.saisieCommentaire4,peConf.libSaisieCommentaire4);
				
		
		// Une ligne pour chaque chèque 
		for (PaiementRemiseDTO paiementRemiseDTO : dto.paiements)
		{
			addRow(paiementRemiseDTO,et,peConf);
		}
		
		// Une ligne vide
		et.addRow("",et.grasGaucheNonWrappe);
		
		addRowCumul(et, dto.paiements.size());
		

	}

	private void addTitreCommentaire(ExcelGeneratorTool et, int col, ChoixOuiNon saisieCommentaire, String libSaisieCommentaire) 
	{
		et.setCell(col,"",et.grasCentreBordure);
		if (saisieCommentaire==ChoixOuiNon.OUI)
		{
			et.setCell(col,libSaisieCommentaire,et.grasCentreBordure);
		}
		else
		{
			et.setColumnWidth(col, 0);
		}
	}

	private void addRow(PaiementRemiseDTO paiementRemiseDTO, ExcelGeneratorTool et, PEReceptionCheque peConf)
	{
		et.addRow();
		et.setCell(0,paiementRemiseDTO.nomUtilisateur,et.grasGaucheNonWrappeBordure);
		et.setCell(1,paiementRemiseDTO.prenomUtilisateur,et.nonGrasGaucheBordure);
		et.setCellPrix(2,paiementRemiseDTO.montant,et.prixCentreBordure);
		
		addCellCommentaire(et,3,peConf.saisieCommentaire1,paiementRemiseDTO.commentaire1);
		addCellCommentaire(et,4,peConf.saisieCommentaire2,paiementRemiseDTO.commentaire2);
		addCellCommentaire(et,5,peConf.saisieCommentaire3,paiementRemiseDTO.commentaire3);
		addCellCommentaire(et,6,peConf.saisieCommentaire4,paiementRemiseDTO.commentaire4);
				
	}


	
	
	private void addCellCommentaire(ExcelGeneratorTool et, int col, ChoixOuiNon saisieCommentaire, String commentaire) 
	{
		et.setCell(col,"",et.grasCentreBordure);
		if (saisieCommentaire==ChoixOuiNon.OUI)
		{
			et.setCell(col,commentaire,et.nonGrasGaucheBordure);
		}
		
	}

	private void addRowCumul(ExcelGeneratorTool et, int nbPaiements)
	{
		et.addRow();
		
		et.setCell(0,"Total",et.grasGaucheNonWrappeBordure);
		et.setCell(1,"",et.nonGrasGaucheBordure);
		et.setCellSumInColUp(2, 2, nbPaiements, et.prixCentreBordure);
	}

	

	@Override
	public String getFileName(RdbLink em)
	{
		RemiseProducteur remise = em.find(RemiseProducteur.class, remiseId);
		ModeleContrat mc = remise.datePaiement.modeleContrat;
		return "remise-"+mc.nom;
	}

	@Override
	public String getNameToDisplay(RdbLink em)
	{
		return "la feuille de remise des chèques au producteur";
	}
	
	@Override
	public ExcelFormat getFormat()
	{
		return ExcelFormat.XLS;
	}

	public static void main(String[] args) throws IOException
	{
		new EGRemise(12652L).test();
	}

}
