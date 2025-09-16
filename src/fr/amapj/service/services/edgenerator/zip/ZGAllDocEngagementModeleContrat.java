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
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.service.engine.generator.zip.AbstractZipGenerator;
import fr.amapj.service.engine.generator.zip.ZipGeneratorTool;
import fr.amapj.service.services.edgenerator.bin.BGDocEngagementSigne;
import fr.amapj.service.services.edgenerator.excel.docengagement.EGBilanDocEngagementSignOnLine;
import fr.amapj.service.services.edgenerator.pdf.PGEngagement;
import fr.amapj.service.services.edgenerator.pdf.PGEngagement.PGEngagementMode;

/**
 * Retourne un zip avec tous les documents d'engagement pour un modele de contrat
 * 
 * Fonctionne uniquement dans le cas de la signature en ligne 
 * 
 * Certains documents peuvent être des documents à signer, certains documents
 * peuvent être des documents signés 
 * 
 * Le zip contient également un fichier de synthèse de l'état d'avancement des signatures
 *
 */
public class ZGAllDocEngagementModeleContrat extends AbstractZipGenerator
{
	private Long idModeleContrat;

	/**
	 */
	public ZGAllDocEngagementModeleContrat(Long idModeleContrat)
	{
		this.idModeleContrat = idModeleContrat;		
	}

	@Override
	public void fillZipFile(RdbLink em, ZipGeneratorTool et) 
	{
		// Ajout du bilan en premier
		et.addFile(em,new EGBilanDocEngagementSignOnLine(idModeleContrat));
				
		// Ajout des documents d'engagement signés
		em.createQuery("select c from Contrat c where c.modeleContrat.id=:id order by c.utilisateur.nom,c.utilisateur.prenom");
		em.setParameter("id", idModeleContrat);
		List<Contrat> cs = em.result().list(Contrat.class);
		
		for (Contrat c : cs) 
		{
			if (c.docEngagementSigne!=null)
			{
				et.addFile(em,new BGDocEngagementSigne(c.id));
			}
			else
			{
				et.addFile(em,new PGEngagement(PGEngagementMode.UN_CONTRAT, c.modeleContrat.id, c.id,null)); 
			}
		}
	}

	@Override
	public String getFileName(RdbLink em) 
	{
		return "contrats";
	}

	@Override
	public String getNameToDisplay(RdbLink em) 
	{
		ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);
		return "tous les documents d'engagement "+mc.nom;
	}	
}
