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
 package fr.amapj.service.services.docengagement.signonline.avenant;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;

import fr.amapj.common.DateUtils;
import fr.amapj.common.velocity.VelocityTools;
import fr.amapj.common.velocity.VelocityVar;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.model.models.contrat.reel.DocEngagementSigne;
import fr.amapj.service.engine.generator.pdf.Html2PdfConverter;
import fr.amapj.service.engine.pdftools.PdfTools;
import fr.amapj.service.services.docengagement.signonline.core.DocEngagementSignOnLineTools;
import fr.amapj.service.services.docengagement.signonline.model.DocEngagementBin;
import fr.amapj.service.services.docengagement.signonline.model.DocEngagementData;
import fr.amapj.service.services.docengagement.signonline.model.DocEngagementVar;
import fr.amapj.service.services.edgenerator.velocity.VCBuilder;
import fr.amapj.service.services.mescontrats.DocEngagementDTO;

/**
 * Gestion des avenants pour les documents d'engagements  
 */
public class AvenantManager
{
	/**
	 * Calcul des données initiales de l'avenant, à la création du document d'engagement
	 */
	public DocEngagementData computeAvenantData(DocEngagementDTO docEngagementDTO) 
	{
		DocEngagementData data = new DocEngagementData();
		data.part1NbPage = new PdfTools().getNbPages(docEngagementDTO.pdfContent);
		data.part1Vars = convert(docEngagementDTO.vars);
		data.part2Hash = null;
		
		return data;
	}
	
	
	static public enum ResultAvenant
	{
		NOTHING_TO_DO , 
		
		UPDATE_AVENANT_DATE ,
		
		UPDATE_DOCENGAGEMENTBIN
		
	}
	
	
	public ResultAvenant updateWithAvenant(RdbLink em,Contrat c,DocEngagementBin docEngagementBin)
	{
		DocEngagementSigne ds = c.docEngagementSigne;
		DocEngagementData data = docEngagementBin.docEngagementData;
		
		// Si le contrat n'a pas été modifié depuis la génération du document : rien à faire
		if (c.dateModification==null || c.dateModification.equals(ds.avenantDate) || c.dateModification.before(ds.avenantDate))
		{
			return ResultAvenant.NOTHING_TO_DO;
		}
		
		// Calcul des nouvelles variables du document 
		VelocityTools vt = new VelocityTools();
		VCBuilder.addContrat(vt, c.modeleContrat, c,null, em);
		List<VelocityVar> allVars = vt.getAllVars();

		// Si le document n'a pas d'avenant
		if (data.part2Hash==null)
		{
			List<VelocityVar> modified1Vars = getModifiedVars(data.part1Vars,allVars);
			
			// Si aucune variable n'a été modifié 
			if (modified1Vars.size()==0)
			{
				// Il n'est pas nécessaire de créer un avenant - on met juste à jour la date de verification de la validité de l'avenant
				return ResultAvenant.UPDATE_AVENANT_DATE;
			}
			// Si des variables ont été modifiées  
			else
			{ 
				// Calcul de l'avenant   
				String avenantHtml = new AvenantHtmlGenerator().createAvenantAsHtml(vt,modified1Vars,c.dateModification);
				
				// Update de l'avenant 
				return updateOrCreateAvenant(docEngagementBin,avenantHtml);
			}
		}
		// Si le document a un avenant  
		else
		{
			// On vérifie tout d'abord si l'avenant est nécessaire
			List<VelocityVar> modified1Vars = getModifiedVars(data.part1Vars,allVars);
			if (modified1Vars.size()==0)
			{
				// Suppression de l'avenant car il n'est pas nécessaire
				return suppressAvenant(docEngagementBin);
			}
			
			// Calcul de l'avenant   
			String avenantHtml = new AvenantHtmlGenerator().createAvenantAsHtml(vt,modified1Vars,c.dateModification);
			
			// Si le nouvel avenant est égal à l'avenant actuellement stocké 
			if (hash(avenantHtml).equals(data.part2Hash))
			{
				// on met juste à jour la date de verification de la validité de l'avenant
				return ResultAvenant.UPDATE_AVENANT_DATE;
			}
			else
			{
				return updateOrCreateAvenant(docEngagementBin,avenantHtml);
			}	
		}
	}

	// MISE A JOUR UNIQUEMENT DE LA DATE DE L AVENANT 

	private byte[] updateAvenantDate(byte[] content, DocEngagementSigne ds) 
	{
		ds.avenantDate = DateUtils.getDate();
		return content;
	}
	
	
	// SUPPRESSION DE L'AVENANT 
	private ResultAvenant suppressAvenant(DocEngagementBin docEngagementBin) 
	{
		// Mise à jour du pdf
		docEngagementBin.pdfContent = new DocEngagementSignOnLineTools().getFirstPages(docEngagementBin.pdfContent, docEngagementBin.docEngagementData.part1NbPage);
		
		// Mise à jour de docEngagementData
		docEngagementBin.docEngagementData.part2Hash = null;
			
		return ResultAvenant.UPDATE_DOCENGAGEMENTBIN;
	}
	
	// MISE A JOUR OU CREATION DE L'AVENANT 
	private ResultAvenant updateOrCreateAvenant(DocEngagementBin docEngagementBin,String avenantHtml) 
	{
		// Conversion en PDF de l'avenant
		byte[] avenant = new Html2PdfConverter().convertHtmlToPdf(avenantHtml);
		
		// Calcul du pdf final 
		byte[] pdfFinal = new DocEngagementSignOnLineTools().concateTwoPdf(docEngagementBin.pdfContent, docEngagementBin.docEngagementData.part1NbPage, avenant); 
		
		// Mise à jour de DocEngagementBin
		docEngagementBin.pdfContent = pdfFinal;
		docEngagementBin.docEngagementData.part2Hash = hash(avenantHtml);
		
		return ResultAvenant.UPDATE_DOCENGAGEMENTBIN;
	}

	
	
	
	
	// PARTIE TECHNIQUE 
	
	public List<DocEngagementVar> convert(List<VelocityVar> vars)
	{
		return vars.stream().map(e->convert(e)).collect(Collectors.toList());
	}
	
	
	public DocEngagementVar convert(VelocityVar var)
	{
		DocEngagementVar res = new DocEngagementVar();
		res.varName = var.varName;
		res.hash = hash(var.content);		
		return res;
	}

	private String hash(String content) 
	{
		if (content==null)
		{
			return hash("");
		}
		
		return DigestUtils.sha1Hex(content);
	}
	

	private List<VelocityVar> getModifiedVars(List<DocEngagementVar> docVars, List<VelocityVar> currentVars) 
	{
		List<VelocityVar> res = new ArrayList<>();
		for (DocEngagementVar docVar : docVars) 
		{
			VelocityVar currentVar = findCurrent(docVar,currentVars);
			if (currentVar!=null && hash(currentVar.content).equals(docVar.hash)==false)
			{
				res.add(currentVar);
			}
			
		}
		return res;
	}

	private VelocityVar findCurrent(DocEngagementVar docVar, List<VelocityVar> currentVars) 
	{
		for (VelocityVar currentVar : currentVars) 
		{
			if (currentVar.varName.equals(docVar.varName))
			{
				return currentVar;
			}
		}
		return null;
	}
	
}
