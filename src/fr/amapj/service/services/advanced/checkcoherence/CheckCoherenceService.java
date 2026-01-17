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
 package fr.amapj.service.services.advanced.checkcoherence;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.model.engine.db.DbManager;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.tools.SpecificDbUtils;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.fichierbase.RoleAdmin;
import fr.amapj.model.models.fichierbase.RoleTresorier;
import fr.amapj.service.services.appinstance.AppInstanceDTO;
import fr.amapj.service.services.gestioncontrat.datebarree.DateBarreCheckService;

/**
 * Permet la gestion des pacths pour les migrations
 */
public class CheckCoherenceService 
{

	private final static Logger logger = LogManager.getLogger();

	/**
	
	 *
	 */
	public String performCheck(List<AppInstanceDTO> apps) 
	{
		StringBuffer str = new StringBuffer();

		for (AppInstanceDTO app : apps) 
		{
			SpecificDbUtils.executeInSpecificDb(app.nomInstance, () -> patch(str));
		}

		return str.toString();
	}

	private Void patch(StringBuffer buf) 
	{
		String dbName = DbManager.get().getCurrentDb().getDbName();

		msg(buf, "Traitement de la base " + dbName);

		doCheckQteSurDateBarrees(buf);

		return null;
	}

	@DbRead
	public void doCheckQteSurDateBarrees(StringBuffer buf) 
	{
		List<Long> idModeleContrats = getAllModeleContrats(buf);
		for (Long idModeleContrat : idModeleContrats) 
		{
			checkQteSurDateBarrees(buf, idModeleContrat);
		}
	}

	private List<Long> getAllModeleContrats(StringBuffer buf) 
	{
		RdbLink em = RdbLink.get();

		// Récuperation de tous les modeles de contrats
		TypedQuery<Long> q = em.createQuery("select c.id from ModeleContrat c", Long.class);
		List<Long> ls = q.getResultList();

		return ls;
	}

	private void checkQteSurDateBarrees(StringBuffer buf, Long idModeleContrat) 
	{
		RdbLink em = RdbLink.get();

		ModeleContrat mc = em.find(ModeleContrat.class, idModeleContrat);

		String msg = new DateBarreCheckService().checkCoherenceDateBarreesModeleContrat(idModeleContrat);
		if (msg != null) 
		{
				msg = "===================================================================\n" +
					"Mails=" + getMailsAdmin(em)+ "," + getMailsTresorier(em) + "\n\n" + 
					"Il y a une incohérence pour le modèle de contrat :"
					+ mc.nom + "\n" + "Producteur=" + mc.producteur.nom + "\n" + "État=" + mc.etat + "\n"
					+ "Détails : \n" + msg;

			msg(buf, msg);

		}

	}

	private String getMailsAdmin(RdbLink em) 
	{
		TypedQuery<RoleAdmin> q = em.createQuery("select r from RoleAdmin r", RoleAdmin.class);
		return q.getResultList().stream().map(e -> e.utilisateur.email).distinct().collect(Collectors.joining(","));
	}

	private String getMailsTresorier(RdbLink em) 
	{
		TypedQuery<RoleTresorier> q = em.createQuery("select r from RoleTresorier r", RoleTresorier.class);
		return q.getResultList().stream().map(e -> e.utilisateur.email).distinct().collect(Collectors.joining(","));
	}

	// PARTIE TECHNIQUE

	/**
	 * Ajout d'un message
	 */
	private void msg(StringBuffer str, String msg) 
	{
		str.append(msg.replaceAll("\n", "<br/>") + "<br/>");
		logger.info(msg);
	}

}
