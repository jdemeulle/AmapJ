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
 package fr.amapj.service.services.utilisateur;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import fr.amapj.common.CollectionUtils;
import fr.amapj.common.DateUtils;
import fr.amapj.common.FormatUtils;
import fr.amapj.common.LongUtils;
import fr.amapj.common.RandomUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.acces.RoleList;
import fr.amapj.model.models.fichierbase.EtatUtilisateur;
import fr.amapj.model.models.fichierbase.Produit;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.model.models.permanence.periode.PeriodePermanenceUtilisateur;
import fr.amapj.model.models.permanence.reel.PermanenceCell;
import fr.amapj.service.engine.tools.DbToDto;
import fr.amapj.service.services.access.AccessManagementService;
import fr.amapj.service.services.archivage.tools.SuppressionState;
import fr.amapj.service.services.archivage.tools.SuppressionState.SStatus;
import fr.amapj.service.services.authentification.PasswordManager;
import fr.amapj.service.services.gestioncotisation.GestionCotisationService;
import fr.amapj.service.services.gestioncotisation.PeriodeCotisationUtilisateurDTO;
import fr.amapj.service.services.mailer.MailerMessage;
import fr.amapj.service.services.mailer.MailerService;
import fr.amapj.service.services.notification.DeleteNotificationService;
import fr.amapj.service.services.notification.PermanenceNotificationService;
import fr.amapj.service.services.parametres.ParametresArchivageDTO;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.permanence.periode.PeriodePermanenceService;
import fr.amapj.service.services.permanence.periode.SmallPeriodePermanenceDTO;
import fr.amapj.service.services.utilisateur.envoimail.EnvoiMailDTO;
import fr.amapj.service.services.utilisateur.envoimail.EnvoiMailUtilisateurDTO;
import fr.amapj.service.services.utilisateur.envoimail.StatusEnvoiMailDTO;
import fr.amapj.service.services.utilisateur.util.UtilisateurUtil;
import fr.amapj.view.engine.popup.suppressionpopup.UnableToSuppressException;

/**
 * Permet la gestion des utilisateurs en masse
 * ou du changement de son état
 * 
 */
public class UtilisateurService
{
	
	
	// PARTIE REQUETAGE POUR AVOIR LA LISTE DES UTILISATEURS
	
	/**
	 * Permet de charger la liste de tous les utilisateurs
	 * dans une transaction en lecture
	 * 
	 * si etat = null, alors on charge tous les utilisateurs, quelque soit leur etat
	 */
	@DbRead
	public List<UtilisateurDTO> getAllUtilisateurs(EtatUtilisateur etat)
	{
		RdbLink em = RdbLink.get();
		List<Utilisateur> us = getUtilisateurs(etat);
		return DbToDto.transform(us, (Utilisateur u) ->createUtilisateurDto(em, u));
	}

	
	public UtilisateurDTO createUtilisateurDto(RdbLink em, Utilisateur u)
	{
		UtilisateurDTO dto = new UtilisateurDTO();
		
		dto.id = u.getId();
		dto.nom = u.nom;
		dto.prenom = u.prenom;
		dto.roles = new AccessManagementService().getRoleAsString(em,u);
		dto.email = u.email;
		dto.etatUtilisateur = u.etatUtilisateur;
		dto.dateCreation = u.dateCreation;
		dto.dateModification = u.dateModification;
		
		dto.numTel1 = u.numTel1;
		dto.numTel2 = u.numTel2;
		dto.libAdr1 = u.libAdr1;
		dto.codePostal = u.codePostal;
		dto.ville = u.ville;
		
		return dto;
	}
	
	@DbRead
	public UtilisateurDTO loadUtilisateurDto(Long idUtilisateur)
	{
		RdbLink em = RdbLink.get();
		Utilisateur u = em.find(Utilisateur.class, idUtilisateur);
		return createUtilisateurDto(em, u);
	}


	// PARTIE MISE A JOUR DES UTILISATEURS

	
	@DbWrite
	public void updateUtilisateur(UtilisateurDTO dto)
	{
		RdbLink em = RdbLink.get();
		
		Utilisateur u = em.find(Utilisateur.class, dto.id);
		u.nom = dto.nom;
		u.prenom = dto.prenom;
		u.email = dto.email;
		u.dateModification = DateUtils.getDate();
		
		u.numTel1 = dto.numTel1;
		u.numTel2 = dto.numTel2;
		u.libAdr1 = dto.libAdr1;
		u.codePostal = dto.codePostal;
		u.ville = dto.ville;
		
	}

	
	// PARTIE MISE A JOUR DE L ETAT D'UN UTILISATEUR

	
	@DbWrite
	public void updateEtat(EtatUtilisateur newValue, Long id)
	{
		RdbLink em = RdbLink.get();
		
		Utilisateur u = em.find(Utilisateur.class, id);
		u.etatUtilisateur = newValue;
		u.dateModification = DateUtils.getDate();
	}
	
	
	
	
	// PARTIE CREATION D UN UTILISATEUR

	/**
	 * Permet la création d'un utilisateur dans une transaction en ecriture
	 * 
	 * Retourne le mot de passe si sendMail == false
	 * 
	 */
	@DbWrite
	public UtilisateurInfo createNewUser(UtilisateurDTO utilisateurDTO,boolean generatePassword,boolean sendMail)
	{
		RdbLink em = RdbLink.get();
		
		// TODO faire les vérifications sur nom, prenom, email 
		
		String nom = utilisateurDTO.nom.trim();
		String prenom = utilisateurDTO.prenom.trim();
		String email = utilisateurDTO.email.trim().toLowerCase();
		
		Utilisateur u = new Utilisateur();
		u.nom = nom;
		u.prenom = prenom;
		u.email = email;
		u.dateCreation = DateUtils.getDate();
		u.numTel1 = utilisateurDTO.numTel1;
		u.numTel2 = utilisateurDTO.numTel2;
		u.libAdr1 = utilisateurDTO.libAdr1;
		u.codePostal = utilisateurDTO.codePostal;
		u.ville = utilisateurDTO.ville;

		em.persist(u);
		
		UtilisateurInfo res = new UtilisateurInfo();
		res.id = u.getId();
		
		
		if (generatePassword==false)
		{
			return res;
		}

		// Génère un mot de passe de 8 caractères majuscules
		String clearPassword = RandomUtils.generatePasswordMaj(8);

		new PasswordManager().setUserPassword(u.getId(), clearPassword);
		
		
		
		if (sendMail==true)
		{
			ParametresDTO param = new ParametresService().getParametres();
			String link = param.getUrl()+"?username="+u.email;
		
			StringBuffer buf = new StringBuffer();
			buf.append("<h2>"+param.nomAmap+"</h2>");
			buf.append("<br/>");
			buf.append("Bonjour , voici vos identifiants pour vous connecter à l'application WEB de :"+param.nomAmap);
			buf.append("<br/>");
			buf.append("<br/>");
			buf.append("Adresse e mail : "+email);
			buf.append("<br/>");
			buf.append("<br/>");
			buf.append("Mot de passe : "+clearPassword);
			buf.append("<br/>");
			buf.append("<br/>");
			buf.append("<a href=\""+link+"\">Cliquez ici pour accéder à l'application</a>");
			buf.append("<br/>");
			buf.append("<br/>");
			buf.append("Merci de conserver ce lien pour pouvoir vous reconnecter plus tard.");
			buf.append("<br/>");
			buf.append("Si vous souhaitez changer votre mot de passe, vous pourrez le faire en vous " +
					"connectant dans l'application, puis en allant dans le menu \"Mon Compte\"");
			buf.append("<br/>");
			buf.append("<br/>");
			
			new MailerService().sendHtmlMail( new MailerMessage(email, "Bienvenue à "+param.nomAmap, buf.toString()));
			
			return res;
		}
		else
		{
			res.password = clearPassword;
			return res;
		}
	}

	static public class UtilisateurInfo
	{
		public String password;
		public Long id;
	}
	
	
	// PARTIE SUPPRESSION

	/**
	 * Permet de supprimer un utilisateur 
	 * Ceci est fait dans une transaction en ecriture
	 */
	@DbWrite
	public void deleteUtilisateur(Long id)
	{
		RdbLink em = RdbLink.get();
		
		Utilisateur u = em.find(Utilisateur.class, id);

		int r = countContrat(u,em);
		if (r>0)
		{
			throw new UnableToSuppressException("Cet utilisateur posséde "+r+" contrats.");
		}
		
		List<PeriodeCotisationUtilisateurDTO> ps = new GestionCotisationService().getPeriodeCotisation(id);
		if (ps.size()>0)
		{
			throw new UnableToSuppressException("Cet utilisateur est indiqué comme cotisant sur les périodes de cotisation suivantes :"+CollectionUtils.asStdString(ps, e->e.periodeNom));
		}
		
		List<RoleList> rs = new AccessManagementService().getUserRole(u, em);
		checkRole(rs,RoleList.ADMIN);
		checkRole(rs,RoleList.TRESORIER);
		checkRole(rs,RoleList.PRODUCTEUR);
		checkRole(rs,RoleList.REFERENT);
		
		new DeleteNotificationService().deleteAllNotificationDoneUtilisateur(em, u);
		
		new PeriodePermanenceService().deleteUtilisateur(em,u);
		
		em.remove(u);
	}


	private void checkRole(List<RoleList> rls, RoleList rl)
	{
		if (rls.contains(rl))
		{
			throw new UnableToSuppressException("Cet utilisateur est "+rl+". Enlevez lui ce rôle avant de le supprimer.");
		}
		
	}


	private int countContrat(Utilisateur u, RdbLink em)
	{
		Query q = em.createQuery("select count(c) from Contrat c WHERE c.utilisateur=:u");
		q.setParameter("u", u);
			
		return LongUtils.toInt(q.getSingleResult());
	}
	
	// 
	@DbRead
	public int countContrat(Long idUtilisateur)
	{
		RdbLink em = RdbLink.get();
		Utilisateur u = em.find(Utilisateur.class, idUtilisateur);
		return countContrat(u,em);
	}
	
	// PARTIE REQUETAGE POUR AVOIR LA LISTE DES UTILISATEURS POUR LE SEARCHER
	
	/**
	 * Permet de charger la liste de tous les utilisateurs
	 * dans une transaction en lecture
	 * 
	 * si etat = null, alors on charge tous les utilisateurs, quelque soit leur etat
	 */
	@DbRead
	public List<Utilisateur> getUtilisateurs(EtatUtilisateur etat)
	{
		RdbLink em = RdbLink.get();
		
		Query q;
		if (etat==null)
		{
			q = em.createQuery("select u from Utilisateur u " +
					"order by u.nom,u.prenom");
		}
		else
		{
			q = em.createQuery("select u from Utilisateur u " +
					"where u.etatUtilisateur=:etat " +
					"order by u.nom,u.prenom");
				q.setParameter("etat", etat);
		}
		List<Utilisateur> us = q.getResultList();
		return us;
	}

	
	// INSERTION EN MASSE DES UTILISATEURS

	@DbWrite
	public void insertAllUtilisateurs(List<UtilisateurDTO> utilisateurs)
	{
		for (UtilisateurDTO utilisateurDTO : utilisateurs)
		{
			createNewUser(utilisateurDTO, false,false);
		}
	}
	
	// ENVOI DES E MAILS DE BIENVENUE

	/**
	 * 
	 * @return
	 */
	@DbRead
	public EnvoiMailDTO getEnvoiMailDTO()
	{
		RdbLink em = RdbLink.get();
		
		EnvoiMailDTO dto = new EnvoiMailDTO();
		
		dto.utilisateurs = new ArrayList<>();
		
		Query q = em.createQuery("select u from Utilisateur u where u.etatUtilisateur=:etat and u.password is null order by u.nom,u.prenom");
		q.setParameter("etat", EtatUtilisateur.ACTIF);
		List<Utilisateur> us = q.getResultList();
		
		for (Utilisateur u : us)
		{
			if (UtilisateurUtil.canSendMailTo(u))
			{
				EnvoiMailUtilisateurDTO emu = new EnvoiMailUtilisateurDTO();
				emu.idUtilisateur = u.getId();
				emu.sendMail = true;
				dto.utilisateurs.add(emu);
			}
		}
		
		return dto;
	}

	/**
	 * Envoi un e mail a chauqe utilisateur, dans une transaction separée 
	 * pour chaque utilisateur 
	 * 
	 * ATTENTION : NE PAS AJOUTER ICI un tag DbWrite ou DbRead !!
	 *  
	 */
	public StatusEnvoiMailDTO envoiEmailBienvenue(EnvoiMailDTO envoiMail)
	{
		StatusEnvoiMailDTO ret = new StatusEnvoiMailDTO();
		
		for (EnvoiMailUtilisateurDTO dto : envoiMail.utilisateurs)
		{
			if (dto.sendMail)
			{
				
				// Récupération de l'email seul dans une transaction en lecture 
				String email = getEmail(dto.idUtilisateur);
				
				// Génère un mot de passe de 8 caractères majuscules 
				String clearPassword = RandomUtils.generatePasswordMaj(8);
					
				try
				{
					// Envoi du mail , sans transaction 
					sendEmail(email,  envoiMail.texteMail,clearPassword);
				
					// positionne le mot de passe pour cet utilisateur dans une transaction en ecriture independante 
					new PasswordManager().setUserPassword(dto.idUtilisateur, clearPassword);
					
					ret.nbMailOK++;

				}
				catch(Exception e)
				{
					String msg = "Impossible d'envoyer un e mail à "+email+" car :"+e.getMessage();
					ret.erreurs.add(msg);
				}
			}
		}
		
		return ret;
		
	}
	
	@DbRead
	private String getEmail(Long idUtilisateur)
	{
		RdbLink em = RdbLink.get();
		Utilisateur utilisateur = em.find(Utilisateur.class, idUtilisateur);
		String email=utilisateur.email;
		return email;
	}


	
	private void sendEmail(String email, String texte, String clearPassword)
	{
		ParametresDTO param = new ParametresService().getParametres();
		
		//
		
		String subject = param.nomAmap+" - Bienvenue";
		String htmlContent = texte;
		
		// Mise en place des <br/>
		htmlContent = htmlContent.replaceAll("\r\n", "<br/>");
		htmlContent = htmlContent.replaceAll("\n\r", "<br/>");
		htmlContent = htmlContent.replaceAll("\n", "<br/>");
		htmlContent = htmlContent.replaceAll("\r", "<br/>");
		
		
		// Remplacement des zones de textes
		String link = param.getUrl()+"?username="+email;
		htmlContent = htmlContent.replaceAll("#LINK#", link);
		
		htmlContent = htmlContent.replaceAll("#PASSWORD#", clearPassword);
		htmlContent = htmlContent.replaceAll("#EMAIL#", email);
		
		// Construction du message
		MailerMessage message  = new MailerMessage();
		message.setEmail(email);
		message.setTitle(subject);
		message.setContent(htmlContent);
		
		// Envoi du message
		new MailerService().sendHtmlMail(message);
		//System.out.println("titre="+message.getTitle());
		//System.out.println("email="+message.getEmail());
		//System.out.println("content="+message.getContent());
		
	}
	
	/**
	 *
	 */
	@DbRead
	public String prettyString(Long idUtilisateur)
	{
		RdbLink em = RdbLink.get();
		
		if (idUtilisateur==null)
		{
			return "";
		}
		
		Utilisateur p = em.find(Utilisateur.class, idUtilisateur);
		return p.nom+" "+p.prenom;
	}
	
	
	


}
