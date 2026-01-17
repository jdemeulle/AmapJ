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

import java.time.LocalDateTime;
import java.util.Date;

import fr.amapj.common.DateUtils;
import fr.amapj.view.engine.tools.TableItem;

public class SmallTenantNotificationDTO implements TableItem 
{
	public Long id;
	
	public LocalDateTime refDate;
	
	public String title;

	
	// Getters and Setters
	public Long getId() 
	{
		return id;
	}

	public Date getDate() 
	{
		return DateUtils.asDate(refDate);
	}

	public void setDate(Date refDate) 
	{
		// 
	}

	public String getTitle() 
	{
		return title;
	}

	public void setTitle(String title) 
	{
		this.title = title;
	}

	
	
}
