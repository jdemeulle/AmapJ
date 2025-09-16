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
 package fr.amapj.service.services.gestioncontratsigne.suivimodification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.common.DateUtils;
import fr.amapj.common.FormatUtils;
import fr.amapj.common.GenericUtils.GetFieldTyped;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.session.SessionManager;

/**
 * Permet le suivi des modifications faites sur un contrat par un référent 
 *
 */
public class SuiviModificationContrat
{
	private final static Logger logger = LogManager.getLogger();
	
	static public void logModification(String libOperation,ModeleContrat mc,String details)
	{
		String fullMessage = 	"Suivi des modifications sur un contrat \n"+
								"Nature de la modification : "+libOperation+"\n"+
								"Nom de l'utilisateur qui a fait la modification :"+SessionManager.getSessionParameters().userNom+" "+SessionManager.getSessionParameters().userPrenom+"\n"+
								"Date et heure de la modification "+FormatUtils.getTimeStd().format(DateUtils.getDate())+"\n"+
								"Nom du contrat : "+mc.nom+" id="+mc.id+"\n"+
								"Producteur : "+mc.producteur.nom+" id="+mc.producteur.id+"\n"+
								"Détails :"+details;
								
		
		logger.info(fullMessage);
	}
	
	static public void logModification(String libOperation,ContratDTO c,String details)
	{
		String fullMessage = 	"Suivi des modifications sur un contrat \n"+
								"Nature de la modification : "+libOperation+"\n"+
								"Nom de l'utilisateur qui a fait la modification :"+SessionManager.getSessionParameters().userNom+" "+SessionManager.getSessionParameters().userPrenom+"\n"+
								"Date et heure de la modification "+FormatUtils.getTimeStd().format(DateUtils.getDate())+"\n"+
								"Nom du contrat : "+c.nom+" id="+c.modeleContratId+"\n"+
								"Détails :"+details;
										
		logger.info(fullMessage);
	}
	
	
	/**
	 * Permet le formatage d'une liste de date (sans heure ni minutes) 
	 */
	static public <T> String listeDate(List<T> dates,Function<T,Date> f1)
	{
		SimpleDateFormat df = FormatUtils.getStdDate();
		return dates.stream().map(e->df.format(f1.apply(e))).collect(Collectors.joining(","));
	}

}
