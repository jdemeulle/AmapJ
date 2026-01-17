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
 package fr.amapj.service.services.mailer;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.messagingcenter.miniproxy.TNProxyClient;
import fr.amapj.messagingcenter.miniproxy.core.ServiceNotAvailableException;
import fr.amapj.messagingcenter.miniproxy.model.mailer.RemoteMailerAttachement;
import fr.amapj.messagingcenter.miniproxy.model.mailer.RemoteMailerMessage;
import fr.amapj.messagingcenter.miniproxy.model.mailer.request.SendMailRequest;
import fr.amapj.messagingcenter.miniproxy.model.mailer.response.SendMailResponse;
import fr.amapj.model.engine.db.DbManager;
import fr.amapj.model.engine.tools.TestTools;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.view.engine.ui.AppConfiguration;
import fr.amapj.view.engine.ui.AppConfiguration.MailConfig;

/**
 * Permet d'envoyer des mails
 * 
 *
 */
public class MailerService
{
	private final static Logger logger = LogManager.getLogger();
	
	public static final String HTML_MAIL_HEADER = "<html><head></head><body>";
	
	public static final String HTML_MAIL_FOOTER = "</body></html>";


	public MailerService()
	{

	}
	
	
	/**
	 *  Envoi d'un mail 
	 */
	public void sendHtmlMail(MailerMessage mailerMessage)
	{
		
		// PARTIE 0 : VERIFICATION 
		
		ParametresDTO param = new ParametresService().getParametres();
		
		// 
		if (MailerCounter.isAllowed(param)==false)	
		{
			throw new AmapjRuntimeException("Impossible d'envoyer un mail car le quota par jour est dépassé (quota = "+param.sendingMailNbMax+" )");
		}
		
		// 
		String username = param.getSendingMailUsername();
		if ( (username==null) || (username.length()==0))
		{
			throw new AmapjRuntimeException("Le service mail n'est pas paramétré : absence de l'adresse d'envoi");
		}
		
		
		// PARTIE 1 : CONSTRUCTION DU MAIL 
		
		RemoteMailerMessage rmm = new RemoteMailerMessage();
		rmm.fromAddress = param.sendingMailUsername;
		rmm.fromPersonalName = param.nomAmap;
		
		rmm.recipentsTo = mailerMessage.getEmail();
		rmm.recipentsCc = null;
		rmm.recipentsBcc=param.mailCopyTo;
		
		rmm.title = mailerMessage.getTitle();
		
		rmm.htmlContent = buildHtlmContent(mailerMessage, param);
		
		for (MailerAttachement ma : mailerMessage.getAttachements()) 
		{
			RemoteMailerAttachement rma = new RemoteMailerAttachement();
			rma.contentType = ma.getMimeType();
			rma.data = ma.getData();
			rma.name = ma.getName();
			
			rmm.addAttachement(rma);
		}
		
		
		// PARTIE 2 : ENVOI DU MAIL
		
		MailConfig mailTarget = AppConfiguration.getConf().getMailConfig();
		
		logger.info("Début envoi d'un message dest= : "+mailerMessage.getEmail()+" en utilisant le mecanisme "+mailTarget);
		
		
		try 
		{
			switch (mailTarget) 
			{
				case GMAIL : sendGmail(param,rmm); 	break;
				case POSTFIX_LOCAL : sendPostFix(param,rmm); break;
				case MESSAGING_CENTER : sendMessagingCenter(param,rmm); break;
				case STORAGE : sendStorage(param,rmm); break;
				case NO_MAIL: sendNoMail(); break;  

				default: throw new AmapjRuntimeException();
			}
		} 
		catch (ServiceNotAvailableException e) 
		{
			throw new RuntimeException("Le service n'est pas disponible. Merci de réessayer plus tard.");
		}
		catch (MessagingException | UnsupportedEncodingException e) 
		{
			throw new RuntimeException(e.getMessage(),e);
		}
		
		logger.info("Message envoyé avec succés");
		
	}
	


	private void sendGmail(ParametresDTO param,RemoteMailerMessage rmm) throws MessagingException, UnsupportedEncodingException 
	{
		Message message = new MimeMessage(getGmailSession(param));
		rmm.fillMessage(message);
		Transport.send(message);
	}
	
	
	private Session getGmailSession(ParametresDTO param)
	{
		String username = param.getSendingMailUsername();
		String password = param.getSendingMailPassword();
		
		if ( (password==null) || (password.length()==0))
		{
			throw new AmapjRuntimeException("Le service mail n'est pas paramétré correctement: absence du mot de passe");
		}
		
		Properties props = new Properties();
		
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
			
		Session session = Session.getInstance(props, new javax.mail.Authenticator()
			{
				protected PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(username, password);
				}
			});
		
		return session;
	}


	private void sendPostFix(ParametresDTO param,RemoteMailerMessage rmm) throws MessagingException, UnsupportedEncodingException 
	{
		Properties props = new Properties();
		props.put("mail.smtp.host", "127.0.0.1");
		Session	session = Session.getInstance(props);
		
		Message message = new MimeMessage(session);
		rmm.fillMessage(message);
		Transport.send(message);
	}
	
	



	private void sendMessagingCenter(ParametresDTO param,RemoteMailerMessage rmm) throws ServiceNotAvailableException 
	{
		SendMailRequest request = new SendMailRequest();
		request.dbName= DbManager.get().getCurrentDb().getDbName();
		request.message = rmm;
		SendMailResponse response = getProxy().sendMail(request);
		if (response.error!=null)
		{
			throw new AmapjRuntimeException(response.error);
		}
		
	}
	
	private TNProxyClient getProxy()
	{
		AppConfiguration conf = AppConfiguration.getConf();
		return new TNProxyClient(conf.getMessagingCenterKey(),conf.getMessagingCenterUrl()); 
	}


	private void sendNoMail() 
	{
		// Nothing to do 
		
	}
	
	
	private void sendStorage(ParametresDTO param,RemoteMailerMessage rmm) 
	{
		MailerStorage.store(rmm);
	}

	
	//
	
	private String buildHtlmContent(MailerMessage mailerMessage, ParametresDTO param)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append(HTML_MAIL_HEADER);
		
		sb.append(mailerMessage.getContent());
		if (param.sendingMailFooter!=null && param.sendingMailFooter.length()>0)
		{
			sb.append("<br/><br/>");
			sb.append(param.sendingMailFooter);
		}
		
		sb.append(HTML_MAIL_FOOTER);
		
		return sb.toString();
	}

	public static void main(String[] args)
	{
		TestTools.init();
		
		MailerMessage message = new MailerMessage("essai@gmail.com", "essai", "<h1>This is actual message</h1>");
		
		new MailerService().sendHtmlMail(message);
	}
}
