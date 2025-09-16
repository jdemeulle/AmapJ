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
 package fr.amapj.service.services.notification.daily;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.common.DateUtils;
import fr.amapj.common.StackUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.NewTransaction;
import fr.amapj.model.models.contrat.modele.EtatModeleContrat;
import fr.amapj.model.models.contrat.modele.GestionDocEngagement;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.ModeleContratDate;
import fr.amapj.model.models.fichierbase.EtatNotification;
import fr.amapj.model.models.fichierbase.Producteur;
import fr.amapj.model.models.fichierbase.ProducteurReferent;
import fr.amapj.model.models.fichierbase.ProducteurUtilisateur;
import fr.amapj.model.models.param.ChoixOuiNon;
import fr.amapj.service.engine.deamons.DeamonsContext;
import fr.amapj.service.services.edgenerator.zip.ZGAllDocEngagementModeleContrat;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.mailer.MailerAttachement;
import fr.amapj.service.services.mailer.MailerMessage;
import fr.amapj.service.services.mailer.MailerService;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.producteur.ProducteurService;
import fr.amapj.service.services.utilisateur.util.UtilisateurUtil;

/**
 * Permet l'envoi des documentsd'engagement 15 jours après la fin du contrat 
 * 
 */
public class DocEngagementNotificationService 
{
	private final static Logger logger = LogManager.getLogger();
	
	
	@DbRead
	public void  sendDocEngagementNotification(DeamonsContext deamonsContext)
	{		
		RdbLink em = RdbLink.get();
		
		// On recherche tous les contrats qui ont la signature en ligne, et pour lesquels on n'a pas envoyé la liasse de documents 
		em.createQuery( "select mc from ModeleContrat mc where " +
						"mc.etat<>:etat AND "+
						"mc.gestionDocEngagement=:g AND "+
						"mc.notificationDoneDocEngagement=:notif");

		em.setParameter("etat", EtatModeleContrat.CREATION);
		em.setParameter("g", GestionDocEngagement.SIGNATURE_EN_LIGNE);
		em.setParameter("notif", ChoixOuiNon.NON);
		
		//
		List<ModeleContrat> mcs = em.result().list(ModeleContrat.class);
		
		for (ModeleContrat mc : mcs) 
		{
			logger.info("Debut de traitement : "+mc.nom);
			try
			{
				processModeleContrat(em,mc,deamonsContext);
			}
			catch(Exception e)
			{
				// En cas d'erreur, on intercepte l'exception pour permettre la notification des autres contrats
				deamonsContext.nbError++;
				logger.info("Erreur pour le contrat "+mc.nom+"\n"+StackUtils.asString(e));
			}
			logger.info("Fin de traitement : "+mc.nom);
		}
	}


	private void processModeleContrat(RdbLink em, ModeleContrat mc, DeamonsContext deamonsContext) 
	{
		// On conserve uniquement les contrats qui sont terminées depuis plus de 15 jours
		List<ModeleContratDate> dates = new GestionContratService().getAllDates(em, mc);
		ModeleContratDate fin = dates.get(dates.size()-1);
		Date ref = DateUtils.addDays(DateUtils.getDate(), -15);
		if (fin.dateLiv.after(ref))
		{
			logger.info("Le contrat n'est pas encore terminé");
			return;
		}
		
		// Calcul de la liste des emails
		String emails = getEmails(em,mc.producteur);
		
		// Construction du message
		MailerMessage message  = new MailerMessage();
		ParametresDTO param = new ParametresService().getParametres();
	
		message.setTitle(param.nomAmap+" - Document d'engagement du contrat "+mc.nom);
		message.setContent(getMessageContent(mc,param));
		message.addAttachement(new MailerAttachement(new ZGAllDocEngagementModeleContrat(mc.id)));
		message.setEmail(emails);
		
		// Envoi du message dans une autre transaction 
		NewTransaction.write(em2->sendMessageAndMemorize(em2,message,mc.id));
	}


	private String getEmails(RdbLink em, Producteur producteur) 
	{
		StringBuilder sb = new StringBuilder();
		
		List<ProducteurUtilisateur> us = new ProducteurService().getProducteurUtilisateur(em, producteur);
		for (ProducteurUtilisateur u : us) 
		{
			if (u.notification==EtatNotification.AVEC_NOTIFICATION_MAIL && UtilisateurUtil.canSendMailTo(u.utilisateur.email))
			{
				sb.append(u.utilisateur.email+";");
			}
		}
		
		List<ProducteurReferent> rs = new ProducteurService().getProducteurReferent(em, producteur);
		for (ProducteurReferent r : rs) 
		{
			if (r.notification==EtatNotification.AVEC_NOTIFICATION_MAIL && UtilisateurUtil.canSendMailTo(r.referent.email))
			{
				sb.append(r.referent.email+";");
			}
		}
		return sb.toString();
	}


	private void sendMessageAndMemorize(RdbLink em, MailerMessage message, Long modeleContratId)
	{
		// On mémorise dans la base de données que l'on va envoyer le message
		ModeleContrat mc = em.find(ModeleContrat.class, modeleContratId);
		mc.notificationDoneDocEngagement = ChoixOuiNon.OUI;		
		// On envoie le message
		new MailerService().sendHtmlMail(message);
	}

	private String getMessageContent(ModeleContrat mc, ParametresDTO param)
	{
		String link = param.getUrl();
		
		StringBuffer buf = new StringBuffer();
		buf.append("<h3>"+param.nomAmap+"-"+param.villeAmap+"</h3><br/>");
		buf.append("Bonjour ,");
		buf.append("<br/>");
		buf.append("<br/>");
		buf.append("Vous trouverez ci joint les documents d'engagement pour le contrat qui vient de se terminer.");
		buf.append("<br/>");
		buf.append("<br/>");
		buf.append("Nom du contrat : "+mc.nom);
		buf.append("<br/>");
		buf.append("Nom du producteur : "+mc.nom);
		buf.append("<br/>");
		buf.append("Ces documents sont à conserver.");
		buf.append("<br/>");		
		buf.append("Si vous souhaitez accéder à l'application : <a href=\""+link+"\">Cliquez ici </a>");
		buf.append("<br/>");
		buf.append("<br/>");
		buf.append("Bonne journée !");
		buf.append("<br/>");
		buf.append("<br/>");
		
		return buf.toString();
	}

}
