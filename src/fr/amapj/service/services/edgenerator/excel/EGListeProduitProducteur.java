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
import java.util.ArrayList;
import java.util.List;

import fr.amapj.common.DateUtils;
import fr.amapj.common.html.HtmlToPlainText;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.fichierbase.Producteur;
import fr.amapj.service.engine.generator.excel.AbstractExcelGenerator;
import fr.amapj.service.engine.generator.excel.ExcelFormat;
import fr.amapj.service.engine.generator.excel.ExcelGeneratorTool;
import fr.amapj.service.services.importdonnees.ImportDonneesService;
import fr.amapj.service.services.importdonnees.ImportProduitProducteurDTO;
import fr.amapj.service.services.produit.ProduitService;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;

/**
 * Permet la generation du fichier d'exemple pour charger les produits et les producteurs
 * 
 */
public class EGListeProduitProducteur extends AbstractExcelGenerator
{
	
	public enum Type
	{
		STD , EXAMPLE;
	}

	private Type type;
	private Long idProducteur;



	
	public EGListeProduitProducteur(Long idProducteur,Type type)
	{
		this.idProducteur = idProducteur;
		this.type = type;
	}
	
	

	@Override
	public void fillExcelFile(RdbLink em,ExcelGeneratorTool et)
	{
		ParametresDTO param = new ParametresService().getParametres();
		et.addSheet("Liste des produits et des producteurs", 4, 60);
		
		List<ImportProduitProducteurDTO> prods;
		
		if (type==Type.EXAMPLE)
		{
			prods = new ArrayList<>();
			
			ImportProduitProducteurDTO dto = new ImportProduitProducteurDTO();
			dto.producteur = "EARL BIO LAIT";
			dto.produit = "Faisselle";
			dto.conditionnement = "le pot de 500 g";
			prods.add(dto);
			
			dto = new ImportProduitProducteurDTO();
			dto.producteur = "EARL BIO LAIT";
			dto.produit = "Yaourt";
			dto.conditionnement = "le pot de 1 kg";
			prods.add(dto);
			
			dto = new ImportProduitProducteurDTO();
			dto.producteur = "EARL PAIN";
			dto.produit = "Pain de seigle";
			dto.conditionnement = "la pièce de 900 g";
			prods.add(dto);
			
			dto = new ImportProduitProducteurDTO();
			dto.producteur = "EARL PAIN";
			dto.produit = "Pain de campagne";
			dto.conditionnement = "la pièce de 900 g";
			prods.add(dto);
			
			
		}
		else 
		{
			if (idProducteur==null)
			{
				prods = new ImportDonneesService().getAllProduits();
			}
			else
			{
				prods = new ImportDonneesService().getAllProduits(idProducteur);
			}
		}
		
		// Construction de l'entete
		contructEntete(et);
		
		// Contruction d'une ligne pour chaque produit
		for (ImportProduitProducteurDTO prod : prods) 
		{
			contructRow(et,prod);
		}	
		
	}
	
	private void contructEntete(ExcelGeneratorTool et)
	{
		ParametresDTO param = new ParametresService().getParametres();
		SimpleDateFormat df1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		// Ligne de titre
		et.addRow(param.nomAmap,et.grasGaucheNonWrappe);
		et.addRow("Extrait le "+df1.format(DateUtils.getDate()),et.grasGaucheNonWrappe);
			
		// pas de ligne vide pour avoir la liste commencant en ligne 4
//		et.addRow();
		
		// Ligne d'entete 
		et.addRow();
		et.setCell(0, "Producteur", et.grasGaucheNonWrappeBordure);
		et.setCell(1, "Nom du produit", et.grasGaucheNonWrappeBordure);
		et.setCell(2, "Conditionnement du produit", et.grasGaucheNonWrappeBordure);
		et.setCell(3, "Description du produit", et.grasGaucheNonWrappeBordure);
		
	}

	
	

	private void contructRow(ExcelGeneratorTool et, ImportProduitProducteurDTO u)
	{
		et.addRow();
		et.setCell(0, u.producteur, et.grasGaucheNonWrappeBordure);
		et.setCell(1, u.produit, et.nonGrasGaucheBordure);
		et.setCell(2, u.conditionnement, et.nonGrasGaucheBordure);
		et.setCell(3, getDescription(u.idProduit), et.nonGrasGaucheBordure);
	}
	


	private String getDescription(Long idProduit) 
	{
		// Ce cas apparait pour le fichier exemple 
		if (idProduit==null)
		{
			return "";
		}
			
		String description = new ProduitService().loadWebPage(idProduit).content;
		if (description==null)
		{
			return "";
		}
		
		return HtmlToPlainText.toPlainText(description);
	}



	@Override
	public String getFileName(RdbLink em)
	{
		ParametresDTO param = new ParametresService().getParametres();

		if (idProducteur!=null)
		{
			Producteur p = em.find(Producteur.class, idProducteur);
			return "liste-produits-"+param.nomAmap+"-"+p.nom;
		}
		else
		{
			return "liste-produits-"+param.nomAmap;
		}
	}
	

	@Override
	public String getNameToDisplay(RdbLink em)
	{
		if (type==Type.EXAMPLE)
		{
			return "un exemple de fichier pour charger les produits et les producteurs";
		}
		else if (idProducteur!=null)
		{
			Producteur p = em.find(Producteur.class, idProducteur);
			return "la liste des produits du producteur : "+p.nom;
		}
		else
		{
			return "la liste de tous les produits et tous les producteurs";
		}
	}
	
	@Override
	public ExcelFormat getFormat()
	{
		return ExcelFormat.XLS;
	}
}
