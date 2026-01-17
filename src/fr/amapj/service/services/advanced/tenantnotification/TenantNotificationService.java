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
 package fr.amapj.service.services.advanced.tenantnotification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import fr.amapj.messagingcenter.miniproxy.TNProxyClient;
import fr.amapj.messagingcenter.miniproxy.core.ServiceNotAvailableException;
import fr.amapj.messagingcenter.miniproxy.model.notification.DbHasNotification;
import fr.amapj.messagingcenter.miniproxy.model.notification.TenantNotificationDTO;
import fr.amapj.messagingcenter.miniproxy.model.notification.request.GetAllNotificationRequest;
import fr.amapj.messagingcenter.miniproxy.model.notification.request.HasNewNotificationRequest;
import fr.amapj.messagingcenter.miniproxy.model.notification.request.LoadNotificationRequest;
import fr.amapj.messagingcenter.miniproxy.model.notification.response.GetAllNotificationResponse;
import fr.amapj.messagingcenter.miniproxy.model.notification.response.HasNewNotificationResponse;
import fr.amapj.messagingcenter.miniproxy.model.notification.response.LoadNotificationResponse;
import fr.amapj.model.engine.db.DbManager;
import fr.amapj.model.models.acces.RoleList;
import fr.amapj.service.engine.deamons.DeamonsUtils;
import fr.amapj.service.services.session.SessionManager;
import fr.amapj.view.engine.ui.AppConfiguration;

public class TenantNotificationService implements Job
{
	private final static Logger logger = LogManager.getLogger();
	
	// Clé : nom de l'instance 
	static private Map<String,DbHasNotification> notificationInfos = new HashMap<>();
	
	/**
	 * Mise à jour toutes les heures de la map des notifications
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException
	{
		DeamonsUtils.executeAsDeamonInMaster(getClass(),e->deamonUpdate());
	}
	
	private void deamonUpdate()
	{
		logger.info("Démarrage du service TenantNotificationService");
		HasNewNotificationRequest r = new HasNewNotificationRequest();
		r.dbNames = DbManager.get().getAllDbs().stream().map(e->e.getDbName()).collect(Collectors.toList());
		
		try
		{
			HasNewNotificationResponse res = getProxy().hasNewNotification(r);
			synchronized (notificationInfos) 
			{
				notificationInfos.clear();
				for (DbHasNotification dbn : res.dbHasNotifications)
				{
					notificationInfos.put(dbn.dbName, dbn);
				}
			}
		} 
		catch (ServiceNotAvailableException e)
		{
			throw new RuntimeException(e);
		}
		
		logger.info("Fin du service TenantNotificationService");
	}
	
	
	
	public boolean isActif() 
	{
		return AppConfiguration.getConf().getMessagingCenterUrl()!=null;
	}
	
	/**
	 * Valable uniquement pour les tresoriers et les admins 
	 */
	public boolean hasNewNotification()
	{
		if (SessionManager.getUserRoles().contains(RoleList.TRESORIER)==false)
		{
			return false;
		}
		
		String dbName = SessionManager.getDb().getDbName();
		Long idUser = SessionManager.getUserId();
		
		synchronized (notificationInfos) 
		{
			DbHasNotification dbn = notificationInfos.get(dbName);
			
			if (dbn==null)
			{
				return false;
			}
			return !dbn.noNewMessageUsers.contains(idUser);		
		}
	}
	
	
	
	
	
	public List<SmallTenantNotificationDTO> getAllNotifications() throws ServiceNotAvailableException
	{
		String dbName = SessionManager.getDb().getDbName();
		Long idUser = SessionManager.getUserId();
		
		saveMessageReadLocally(dbName, idUser);
		
		GetAllNotificationRequest request = new GetAllNotificationRequest();
		request.dbName = dbName;
		request.idUser = idUser;
		
		GetAllNotificationResponse response = getProxy().getAllNotifications(request);
		
		List<TenantNotificationDTO> notifications = response.notifications;
		List<SmallTenantNotificationDTO> res = new ArrayList<SmallTenantNotificationDTO>();
		for (TenantNotificationDTO t : notifications) 
		{
			SmallTenantNotificationDTO small = new SmallTenantNotificationDTO();
			small.id = t.id;
			small.refDate = t.refDate;
			small.title = t.title;
			
			res.add(small);
		}
		return res;
	}
	
	private void saveMessageReadLocally(String dbName,Long idUser)
	{
		// Mise à jour localement
		synchronized (notificationInfos) 
		{
			DbHasNotification dbn = notificationInfos.get(dbName);
			
			if (dbn!=null)
			{
				if (!dbn.noNewMessageUsers.contains(idUser))
				{
					dbn.noNewMessageUsers.add(idUser);
				}
			}
		}
	}
	
	
	public TenantNotificationDTO loadNotification(Long idNotification) throws ServiceNotAvailableException
	{
		LoadNotificationRequest request = new LoadNotificationRequest();
		request.idNotification = idNotification;
		LoadNotificationResponse response = getProxy().loadNotification(request); 
		
		return response.notification;
	}
	
	
	private TNProxyClient getProxy()
	{
		AppConfiguration conf = AppConfiguration.getConf();
		return new TNProxyClient(conf.getMessagingCenterKey(),conf.getMessagingCenterUrl()); 
	}
}
