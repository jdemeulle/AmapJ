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
 package fr.amapj.model.models.param.paramecran;

import java.util.List;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.engine.metadata.MetaDataEnum;
import fr.amapj.model.models.acces.RoleList;
import fr.amapj.model.models.param.paramecran.common.AbstractParamEcran;



/**
 * Parametrage de l'écran liste des adhérents
 */
public class PEListeAdherent  extends AbstractParamEcran
{
	// Acces à cet écran niveau global 
	public PEListeAdherentAccess accesEcran = PEListeAdherentAccess.ALL;
	
	// Indique qui peut accéder aux e mails 
	public RoleList canAccessEmail = RoleList.ADHERENT;
	
	// Indique qui peut accéder aux numéros de telephone 1 
	public RoleList canAccessTel1 = RoleList.ADHERENT;

	// Indique qui peut accéder aux numéros de telephone 2 
	public RoleList canAccessTel2 = RoleList.ADHERENT;
	
	// Indique qui peut accéder aux 3 champs adresses
	public RoleList canAccessAdress = RoleList.ADHERENT;
	
	
	static public enum PEListeAdherentAccess
	{
		ALL , 
		
		PRODUCTEUR ,
		
		REFERENT ,
		
		TRESORIER;
		
		static public class MetaData extends MetaDataEnum
		{
			
			public void fill()
			{		
				add("Ce champ vous permet de choisir les personnes pouvant accéder à l'écran Visite AMAP");

				add(ALL, "Tout le monde" , "Dans ce mode, cet écran est accessible à tous.");
				
				add(PRODUCTEUR, "Producteur, Referent,Trésorier,Admin" , "Dans ce mode, cet écran est accessible aux producteurs , référents, trésoriers et admin");
				
				add(REFERENT, "Referent,Trésorier,Admin" , "Dans ce mode, cet écran est accessible aux référents, trésoriers et admin");
				
				add(TRESORIER, "Trésorier,Admin" , "Dans ce mode, cet écran est accessible aux trésoriers et admin");

			}
		}	
	}
	
	@Override
	public boolean complyParamEcan(List<RoleList> roles)
	{
		if (accesEcran==PEListeAdherentAccess.ALL)
		{
			return true;
		}
		if (accesEcran==PEListeAdherentAccess.PRODUCTEUR)
		{
			return roles.contains(RoleList.PRODUCTEUR);	
		}
		if (accesEcran==PEListeAdherentAccess.REFERENT)
		{
			return roles.contains(RoleList.REFERENT);	
		}
		if (accesEcran==PEListeAdherentAccess.TRESORIER)
		{
			return roles.contains(RoleList.TRESORIER);	
		}
		throw new AmapjRuntimeException();
	}
	
	
	


	public RoleList getCanAccessEmail()
	{
		return canAccessEmail;
	}

	public void setCanAccessEmail(RoleList canAccessEmail)
	{
		this.canAccessEmail = canAccessEmail;
	}

	public RoleList getCanAccessTel1()
	{
		return canAccessTel1;
	}

	public void setCanAccessTel1(RoleList canAccessTel1)
	{
		this.canAccessTel1 = canAccessTel1;
	}

	public RoleList getCanAccessTel2()
	{
		return canAccessTel2;
	}

	public void setCanAccessTel2(RoleList canAccessTel2)
	{
		this.canAccessTel2 = canAccessTel2;
	}

	public RoleList getCanAccessAdress()
	{
		return canAccessAdress;
	}

	public void setCanAccessAdress(RoleList canAccessAdress)
	{
		this.canAccessAdress = canAccessAdress;
	}

	public PEListeAdherentAccess getAccesEcran() 
	{
		return accesEcran;
	}

	public void setAccesEcran(PEListeAdherentAccess accesEcran) 
	{
		this.accesEcran = accesEcran;
	}
	
	

	
}
