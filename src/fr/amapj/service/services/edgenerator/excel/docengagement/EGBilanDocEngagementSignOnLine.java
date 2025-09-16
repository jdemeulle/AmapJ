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
 package fr.amapj.service.services.edgenerator.excel.docengagement;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.service.engine.generator.excel.AbstractExcelGenerator;
import fr.amapj.service.engine.generator.excel.ExcelFormat;
import fr.amapj.service.engine.generator.excel.ExcelGeneratorTool;
import fr.amapj.service.services.docengagement.signonline.DocEngagementSignOnLineDTO;
import fr.amapj.service.services.docengagement.signonline.DocEngagementSignOnLineService;
import fr.amapj.service.services.producteur.ProducteurService;
import fr.amapj.service.services.utilisateur.util.UtilisateurUtil;


/**
 * Permet la generation du bilan de la signature en ligne des documents d'engagements
 * 
 *  
 *
 */
public class EGBilanDocEngagementSignOnLine extends AbstractExcelGenerator
{
	
	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

	private Long idModeleContrat;
	
	public EGBilanDocEngagementSignOnLine(Long idModeleContrat)
	{
		this.idModeleContrat = idModeleContrat;
	}
	
	@Override
	public void fillExcelFile(RdbLink em,ExcelGeneratorTool et)
	{
		// Calcul du nombre de colonnes :  Nom + prénom + etat signature amapien + date signature amapien + etat signature producteur  + date signature producteur + e mail 
		et.addSheet("Bilan signature en ligne", 7, 25);
		et.setColumnWidth(6, 40);
	
		ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);
		List<DocEngagementSignOnLineDTO> dtos = new DocEngagementSignOnLineService().getBilanSignature(idModeleContrat);
		long nbContratNonSigneAmapien = getNbContratNonSigneAmapien(dtos);
				
		et.addRow("Bilan de la signature en ligne des documents d'engagement pour le contrat "+mc.nom,et.grasGaucheNonWrappe);
		et.addRow("",et.grasGaucheNonWrappe);
		et.addRow("Nom du producteur : "+mc.producteur.nom,et.grasGaucheNonWrappe);
		et.addRow("",et.grasGaucheNonWrappe);
		et.addRow("Nombre total de souscripteurs : "+dtos.size(),et.grasGaucheNonWrappe);
		et.addRow("Nombre de contrats non signés par les amapiens : "+nbContratNonSigneAmapien,et.grasGaucheNonWrappe);
		et.addRow("Nombre de contrats non signés par le producteur : "+getNbContratNonSignProducteur(dtos),et.grasGaucheNonWrappe);
		et.addRow("",et.grasGaucheNonWrappe);
		

		// Création de la ligne titre des colonnes
		et.addRow();
		et.setCell(0,"Nom",et.grasCentreBordure);
		et.setCell(1,"Prénom",et.grasCentreBordure);
		et.setCell(2,"Signature amapien",et.grasCentreBordure);
		et.setCell(3,"Date signature amapien",et.grasCentreBordure);
		et.setCell(4,"Signature producteur",et.grasCentreBordure);
		et.setCell(5,"Date signature producteur",et.grasCentreBordure);
		et.setCell(6,"E mail",et.grasCentreBordure);
		
		
		// Une ligne pour chaque contrat
		for (DocEngagementSignOnLineDTO dto : dtos)
		{
			addRow(em,dto,et);
		}	
		
		et.addRow("",et.grasGaucheNonWrappe);
		
		if (nbContratNonSigneAmapien>0)
		{
			et.addRow();
			et.setCell(0,"Mails des amapiens en retard de signature",et.nonGrasGaucheNonWrappe);
			et.setCell(2,getEmailsRetard(em,dtos),et.nonGrasGaucheNonWrappe);
		}
		
		et.addRow("",et.grasGaucheNonWrappe);
		et.addRow();
		et.setCell(0,"Mails du producteur",et.nonGrasGaucheNonWrappe);
		et.setCell(2,new ProducteurService().getEmailsProducteur(mc.producteur.id),et.nonGrasGaucheNonWrappe);
	}

	private String getEmailsRetard(RdbLink em, List<DocEngagementSignOnLineDTO> dtos) 
	{
		String res = "";
		for (DocEngagementSignOnLineDTO dto : dtos) 
		{
			Utilisateur u = em.find(Utilisateur.class, dto.idUtilisateur);
			if (dto.signedByAmapien==null && UtilisateurUtil.canSendMailTo(u))
			{
				res = res+u.email+";";
			}
		}
		return res;
	}

	private String getNbContratNonSignProducteur(List<DocEngagementSignOnLineDTO> dtos) 
	{
		return ""+dtos.stream().filter(e->e.signedByProducteur==null).count();
	}

	private long getNbContratNonSigneAmapien(List<DocEngagementSignOnLineDTO> dtos) 
	{
		return dtos.stream().filter(e->e.signedByAmapien==null).count();
	}

	private void addRow(RdbLink em, DocEngagementSignOnLineDTO dto, ExcelGeneratorTool et)
	{
		Utilisateur u = em.find(Utilisateur.class, dto.idUtilisateur);
		
		boolean applyGray = dto.signedByAmapien==null || dto.signedByProducteur==null;
		
		et.addRow();
		et.setCell(0,dto.nomUtilisateur,et.switchGray(et.grasGaucheNonWrappeBordure,applyGray));
		
		et.setCell(1,dto.prenomUtilisateur,et.switchGray(et.nonGrasGaucheBordure,applyGray));
		et.setCell(2,dto.signedByAmapien!=null ? "OUI" : "NON",et.switchGray(et.nonGrasCentreBordure,applyGray));
		et.setCell(3,f(dto.signedByAmapien),et.switchGray(et.nonGrasCentreBordure,applyGray));
		
		et.setCell(4,dto.signedByProducteur!=null ? "OUI" : "NON",et.switchGray(et.nonGrasCentreBordure,applyGray));
		et.setCell(5,f(dto.signedByProducteur),et.switchGray(et.nonGrasCentreBordure,applyGray));
		
		et.setCell(6,UtilisateurUtil.libMail(u),et.switchGray(et.nonGrasGaucheBordure,applyGray));
		
	}

	private String f(Date d) 
	{
		if (d==null)
		{
			return "";
		}
		return df.format(d);
	}

	@Override
	public String getFileName(RdbLink em)
	{
		ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);
		return "bilan-signature-"+mc.nom;
	}

	@Override
	public String getNameToDisplay(RdbLink em)
	{
		ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);
		return "le bilan signature en ligne des documents d'engagement pour "+mc.nom;
	}
	
	@Override
	public ExcelFormat getFormat()
	{
		return ExcelFormat.XLS;
	}
}
