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
 package fr.amapj.service.services.edgenerator.bin;


import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.service.engine.generator.bin.AbstractBinGenerator;
import fr.amapj.service.services.docengagement.signonline.core.CoreDocEngagementSignOnLineService;

public class BGDocEngagementSigne extends AbstractBinGenerator
{
	private Long idContrat;
	
	/**
	 * A utiliser uniquement dans le cas d'une signature en ligne , et si le contrat est bien signé par l'amapien au moins
	 */
	public BGDocEngagementSigne(Long idContrat)
	{
		this.idContrat = idContrat;
		
	}

	@Override
	public byte[] generateContent(RdbLink em) 
	{
		byte[] bs = new CoreDocEngagementSignOnLineService().loadDocEngagementSignePdf(idContrat);
		if (bs==null)
		{
			throw new AmapjRuntimeException("Le document d'engagement doit être signé par l'amapien au moins.");
		}
		
		return bs;		
	}

	@Override
	public String getFileName(RdbLink em) 
	{
		Contrat c = em.find(Contrat.class, idContrat);
		return "document-engagement-"+c.modeleContrat.nom+"-"+c.utilisateur.nom+" "+c.utilisateur.prenom+" "+getSuffix(c);
	}

	@Override
	public String getNameToDisplay(RdbLink em) 
	{
		Contrat c = em.find(Contrat.class, idContrat);
		return "le document d'engagement "+c.modeleContrat.nom+" pour "+c.utilisateur.nom+" "+c.utilisateur.prenom+" "+getSuffix(c);
	}

	private String getSuffix(Contrat c) 
	{
		if (c.docEngagementSigne.producteurDateSignature==null)
		{
			return "(signé par l'amapien)";
		}
		else
		{
			return "(signé par l'amapien et le producteur)";
		}
	}

	@Override
	public String getExtension() 
	{
		return "pdf";
	}
}
