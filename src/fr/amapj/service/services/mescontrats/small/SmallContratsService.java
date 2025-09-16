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
 package fr.amapj.service.services.mescontrats.small;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.DateUtils;
import fr.amapj.common.SQLUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.models.contrat.modele.EtatModeleContrat;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.modele.ModeleContratDate;
import fr.amapj.model.models.contrat.modele.NatureContrat;
import fr.amapj.model.models.contrat.modele.TypJoker;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.mescontrats.ContratStatusService;
import fr.amapj.service.services.mescontrats.small.inscription.InscriptionButton;
import fr.amapj.service.services.mescontrats.small.inscription.SmallInscriptionDTO;

/**
 * Permet l'affichage des contrats dans MesContratsView et VisiteAmapView
 */
public class SmallContratsService
{
	
	/**
	 * Retourne la liste contrats pour l'utilisateur courant, avec les régles pour s'inscrire 
	 */
	@DbRead
	public List<SmallContratDTO> getMesContrats(Long userId)
	{
		RdbLink em = RdbLink.get();
	
		List<SmallContratDTO> res = new ArrayList<SmallContratDTO>();

		Utilisateur user = em.find(Utilisateur.class, userId);
		Date now =DateUtils.getDate();
		Date d = DateUtils.suppressTime(now);
		d = DateUtils.addDays(d, -5);

		// On récupère d'abord la liste de tous les modeles de contrats qui ont au moins une date de livraison dans le futur (-5 jours) et qui sont ACTIF 
		TypedQuery<ModeleContrat> q = em.createQuery("select distinct(mcd.modeleContrat) from ModeleContratDate mcd WHERE " +
				" mcd.modeleContrat.etat=:etat AND "+
				" mcd.dateLiv>=:d " , ModeleContrat.class);
		q.setParameter("etat",EtatModeleContrat.ACTIF);
		q.setParameter("d",d);	
		List<ModeleContrat> mcs = q.getResultList();
		
		// Pour chaque modele de contrat, on crée le SmallContratDTO correspondant 
		for (ModeleContrat modeleContrat : mcs)
		{
			// On recherche le contrat de cet utilisateur si il existe 
			TypedQuery<Contrat> q2 = em.createQuery("select c from Contrat c where c.modeleContrat=:mc AND c.utilisateur=:u",Contrat.class);
			q2.setParameter("mc",modeleContrat);
			q2.setParameter("u",user);
			Contrat c = SQLUtils.oneOrZero(q2);
			
			// Avec une sous requete, on obtient la liste de toutes les dates de livraison
			List<ModeleContratDate> dates = new GestionContratService().getAllDates(em, modeleContrat);
				
			//
			SmallContratDTO contratDTO = buildBasicContratDTO(em, modeleContrat, c,dates);
			SmallInscriptionDTO inscriptionDTO = buildInscriptionDTO(em,modeleContrat,c,now,dates,contratDTO);
			
			// InscriptionDTO est null pour les contrats sur lequels l'utilisateur ne peut pas s'inscrire et pour lequels l'utilisateur n'a pas de contrat
			// ces contrats sont donc exclus
			if (inscriptionDTO!=null)
			{
				contratDTO.inscriptionDTO = inscriptionDTO;
				res.add(contratDTO);
			} 
		}
		return res;
	}


	
	private SmallContratDTO buildBasicContratDTO(RdbLink em, ModeleContrat mc,Contrat c, List<ModeleContratDate> dates)
	{
		int nbLivraison = dates.size()-new GestionContratService().getNbDateAnnulees(em,mc);
		Date dateDebut = dates.get(0).dateLiv;
		Date dateFin = dates.get(dates.size() - 1).dateLiv;
		
		SmallContratDTO dto = new SmallContratDTO();
		dto.contratId = c==null ? null : c.id;
		dto.modeleContratId = mc.getId();
		dto.nom = mc.nom;
		dto.description = mc.description;
		dto.nomProducteur = mc.producteur.nom;
		dto.dateFinInscription = mc.dateFinInscription;
		dto.nbLivraison = nbLivraison;
		dto.dateDebut = dateDebut;
		dto.dateFin = dateFin;
		dto.nature = mc.nature;
	
		return dto;
	}
	
	
	
	private SmallInscriptionDTO buildInscriptionDTO(RdbLink em, ModeleContrat modeleContrat, Contrat c,  Date now,List<ModeleContratDate> dates, SmallContratDTO contratDTO)
	{
		SimpleDateFormat df = new SimpleDateFormat("EEEEE dd MMMMM yyyy");
		
		switch (modeleContrat.nature) 
		{
		case ABONNEMENT: 	
		case LIBRE: 		
			return computeAboEtLibre(df,modeleContrat, now,c,dates,contratDTO);
			
		case CARTE_PREPAYEE:
			return computeCartePrepayee(df,modeleContrat, em, now,c,dates,contratDTO);
		
		default:			
			throw new AmapjRuntimeException();
		}
	}
	
	
	private SmallInscriptionDTO computeAboEtLibre(SimpleDateFormat df, ModeleContrat modeleContrat, Date now, Contrat c,List<ModeleContratDate> dates, SmallContratDTO contratDTO)
	{
		SmallInscriptionDTO dto = new SmallInscriptionDTO();
		if (c==null)
		{
			// Si on est avant la date de fin des inscriptions 
			if (new ContratStatusService().isInscriptionNonTerminee(modeleContrat, now))
			{
				dto.buttons = new InscriptionButton[] { InscriptionButton.SINCRIRE };
				dto.isRetardataire = false;
				dto.libContrat = formatLibelleStandard(df,contratDTO, true);
				return dto;
			}
			
			// Si on peut s'inscrire en tant que retardataire 
			if (new ContratStatusService().isInscriptionRetardatairePossible(modeleContrat,now,dates))
			{
				dto.buttons = new InscriptionButton[] { InscriptionButton.SINCRIRE };
				dto.isRetardataire = true;
				dto.libContrat = formatLibelleRetardataire(df,contratDTO, true);
				return dto;
			}
			
			// On ne peut rien faire 
			return null;
			
		}
		else
		{
			// Si on est avant la date de fin des inscriptions 
			if (new ContratStatusService().isInscriptionNonTerminee(modeleContrat, now))
			{
				dto.buttons = new InscriptionButton[] { InscriptionButton.MODIFIER , InscriptionButton.SUPPRIMER , InscriptionButton.VOIR };
				dto.isRetardataire = false;
				dto.libContrat = formatLibelleStandard(df,contratDTO, false);
				return dto;
			}
			
			// le jour même, on peut modifier son inscription retardataire
			if (new ContratStatusService().isContratRetardataireModifiable(c,now))
			{
				dto.buttons = new InscriptionButton[] { InscriptionButton.MODIFIER , InscriptionButton.SUPPRIMER , InscriptionButton.VOIR };
				dto.isRetardataire = true;
				dto.libContrat = formatLibelleRetardataire(df,contratDTO, false);
				return dto;
			}
			
			// Si il est peut etre possible de modifier les jokers 
			if (modeleContrat.nature==NatureContrat.ABONNEMENT && modeleContrat.typJoker!=TypJoker.SANS_JOKER && modeleContrat.jokerNbMax>0)
			{
				dto.buttons = new InscriptionButton[] { InscriptionButton.JOKER , InscriptionButton.VOIR };
				dto.isRetardataire = false;
				dto.libContrat = formatLibelleJoker(df, contratDTO);
				return dto;
			}
			
			
			// On peut juste voir son contrat
			dto.buttons = new InscriptionButton[] { InscriptionButton.VOIR };
			dto.isRetardataire = false;
			dto.libContrat = formatLibelleNone(df,contratDTO);
			return dto;
			
		}
	}

	

	private SmallInscriptionDTO computeCartePrepayee(SimpleDateFormat df,ModeleContrat modeleContrat, RdbLink em, Date now, Contrat c,List<ModeleContratDate> dates,SmallContratDTO contratDTO)
	{
		Date nextDateLiv = getNextDateLivraison(dates,now,modeleContrat.cartePrepayeeDelai);
		Date nextDateLivModifiable = getNextDateLivModifiable(dates,now);
		
		SmallInscriptionDTO dto = new SmallInscriptionDTO();
		
		if (c==null)
		{
			// Si il y a encore une date modifiable 
			if (nextDateLivModifiable!=null)
			{
				dto.buttons = new InscriptionButton[] { InscriptionButton.SINCRIRE };
				dto.isRetardataire = false;
				dto.libContrat = formatLibelleContratCartePrepayee(df, contratDTO, nextDateLiv, nextDateLivModifiable, modeleContrat);
				return dto;
			}
			
			// On ne peut rien faire 
			return null;
			
		}
		else
		{
			// Si il y a encore une date modifiable 
			if (nextDateLivModifiable!=null)
			{
				if (computeIsContratPrepayeeSupprimable(now, c, em))
				{
					dto.buttons = new InscriptionButton[] { InscriptionButton.MODIFIER , InscriptionButton.SUPPRIMER , InscriptionButton.VOIR  };
				}
				else
				{
					dto.buttons = new InscriptionButton[] { InscriptionButton.MODIFIER , InscriptionButton.VOIR  };
				}
				dto.isRetardataire = false;
				dto.libContrat = formatLibelleContratCartePrepayee(df, contratDTO, nextDateLiv, nextDateLivModifiable, modeleContrat);
				return dto;
			}
			
			
			// On peut juste voir son contrat
			dto.buttons = new InscriptionButton[] { InscriptionButton.VOIR };
			dto.isRetardataire = false;
			dto.libContrat = formatLibelleContratCartePrepayee(df, contratDTO, nextDateLiv, nextDateLivModifiable, modeleContrat);
			return dto;
			
		}
	}

	
	
	private boolean computeIsContratPrepayeeSupprimable(Date now,Contrat contrat, RdbLink em)
	{
		// On verifie si il y a des lignes non modifiables avec des quantités non nulles
		Date d = DateUtils.suppressTime(now);
		d = DateUtils.addDays(d, contrat.modeleContrat.cartePrepayeeDelai);
		
		Query q = em.createQuery("select count(cc) from ContratCell cc  WHERE cc.contrat=:c and cc.modeleContratDate.dateLiv<=:d");
		q.setParameter("c",contrat);
		q.setParameter("d",d);
		
		int count = SQLUtils.toInt(q.getSingleResult());
		return (count==0);
		
	}
	
	/** 
	 * Retourne la date de la prochaine livraison (aujourd'hui est une valeur possible)  
	 */
	private Date getNextDateLivraison(List<ModeleContratDate> datLivs, Date now, int cartePrepayeeDelai)
	{
		Date ref = DateUtils.addDays(now, -1);
		for (ModeleContratDate lig : datLivs)
		{
			if (ref.before(lig.dateLiv))
			{
				return lig.dateLiv;
			}
		}
		return null;
	}
	
	
	private Date getNextDateLivModifiable(List<ModeleContratDate> mcds, Date now)
	{
		for (ModeleContratDate mcd : mcds)
		{
			if (new ContratStatusService().isDateModifiableCartePrepayee(mcd, now))
			{
				return mcd.dateLiv;
			}
		}
		return null;
	}
	
	

	
	
	private String formatLibelleContratCartePrepayee(SimpleDateFormat df,SmallContratDTO c,Date nextDateLiv,Date nextDateLivModifiable,ModeleContrat modeleContrat)
	{	
		// Ligne 1
		String str = c.description;
		str=str+"<br/>";
		
		// Ligne 2 - Les dates de livraisons
		if (nextDateLiv==null)
		{
			str = str+"<b>Toutes les livraisons sont faites.</b>";
		}
		else
		{
			str = str+"<b>Prochaine livraison le "+df.format(nextDateLiv)+"</b>";
		}
		str=str+"<br/>";
		
		// Ligne : modifiable ou non 
		if (nextDateLivModifiable==null)
		{
			str = str+"Ce contrat n'est plus modifiable.";
		}
		else
		{
			Date datLimit = DateUtils.addDays(nextDateLivModifiable, -(modeleContrat.cartePrepayeeDelai+1));
			str = str+"La livraison du "+df.format(nextDateLivModifiable)+" est modifiable ( modification possible jusqu'au "+df.format(datLimit)+ " minuit).";
		}
		
		str=str+"<br/>";
		
		return str;
	}

	
	// ABO et LIBRE
	
	private String formatLibelleStandard(SimpleDateFormat df, SmallContratDTO c, boolean isInscription)
	{
		String str = enteteContratAboEtLibre(df,c);	
		if (isInscription)
		{
			str = str+"Vous pouvez vous inscrire et modifier ce contrat jusqu'au "+df.format(c.dateFinInscription)+ " minuit.";
		}
		else
		{
			str = str+"Ce contrat est modifiable jusqu'au "+df.format(c.dateFinInscription)+ " minuit.";
		}
		str=str+"<br/>";
		
		return str;
	}
	
	
	
	
	private String formatLibelleJoker(SimpleDateFormat df,SmallContratDTO c)
	{
		String str = enteteContratAboEtLibre(df,c);	
		str = str+"Ce contrat n'est plus modifiable, mais vous pouvez éventuellement ajuster vos jokers.";
		str=str+"<br/>";
		
		return str;
	}
	
	

	private String formatLibelleRetardataire(SimpleDateFormat df,SmallContratDTO c, boolean isInscription)
	{
		String str = enteteContratAboEtLibre(df,c);	
		if (isInscription)
		{
			str = str+"Vous pouvez vous inscrire en tant que nouvel arrivant / retardataire pour ce contrat (vous pourrez modifier vos choix jusqu'à ce soir minuit).";
		}
		else
		{
			str = str+"Vous vous êtes inscrit en tant que nouvel arrivant / retardataire pour ce contrat, vous pouvez modifier ce contrat jusqu'à ce soir minuit.";
		}
		str=str+"<br/>";
		return str;
	}

	private String formatLibelleNone(SimpleDateFormat df,SmallContratDTO c)
	{
		String str = enteteContratAboEtLibre(df,c);	
		str = str+"Ce contrat n'est plus modifiable.";
		str=str+"<br/>";
		return str;
	}
	
	private String enteteContratAboEtLibre(SimpleDateFormat df,SmallContratDTO c)
	{	
		
		// Ligne 1
		String str = c.description;
		str=str+"<br/>";
		
		// Ligne 2 - Les dates de livraisons
		if (c.nbLivraison==1)
		{
			str = str+"<b>Une seule livraison le "+df.format(c.dateDebut)+"</b>";
		}
		else
		{
			str = str+"<b>"+c.nbLivraison+" livraisons à partir du "+df.format(c.dateDebut)+" jusqu'au "+df.format(c.dateFin)+"</b>";
		}
		str=str+"<br/>";
		
		return str;
	}

	
	
	
	

}
