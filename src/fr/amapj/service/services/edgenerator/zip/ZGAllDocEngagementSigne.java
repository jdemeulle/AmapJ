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
 package fr.amapj.service.services.edgenerator.zip;

import java.util.List;

import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.service.engine.generator.zip.AbstractZipGenerator;
import fr.amapj.service.engine.generator.zip.ZipGeneratorTool;
import fr.amapj.service.services.docengagement.signonline.DocEngagementSignOnLineDTO;
import fr.amapj.service.services.docengagement.signonline.core.CoreDocEngagementSignOnLineService;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;


/**
 * Retourne un zip avec une liste de documents d'engagement signés par les amapiens
 * 
 *  Ce zip sera lu par le producteur, puis le producteur signera alors les documents
 *
 */
public class ZGAllDocEngagementSigne extends AbstractZipGenerator
{
	
	
	private List<DocEngagementSignOnLineDTO> dtos;

	/**
	 * 
	 */
	public ZGAllDocEngagementSigne(List<DocEngagementSignOnLineDTO> dtos)
	{
		this.dtos = dtos;
				
	}

	@Override
	public void fillZipFile(RdbLink em, ZipGeneratorTool et) 
	{
		for (DocEngagementSignOnLineDTO dto : dtos) 
		{
			Contrat c = em.find(Contrat.class, dto.idContrat);
			String fileName = c.modeleContrat.nom+" - "+c.utilisateur.nom+" "+c.utilisateur.prenom;
			byte[] fileContent = new CoreDocEngagementSignOnLineService().loadDocEngagementSignePdf(c.id);
			et.addFile(fileName, "pdf" ,fileContent);
		}
	}

	@Override
	public String getFileName(RdbLink em) 
	{
		ParametresDTO param = new ParametresService().getParametres();
		return "contrats-"+param.nomAmap;
	}

	@Override
	public String getNameToDisplay(RdbLink em) 
	{
		return "tous les documents d'engagement";
	}	
}
