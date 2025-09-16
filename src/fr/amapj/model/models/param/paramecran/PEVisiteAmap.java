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

import fr.amapj.model.engine.metadata.MetaDataEnum;
import fr.amapj.model.models.acces.RoleList;
import fr.amapj.model.models.param.ChoixOuiNon;
import fr.amapj.model.models.param.paramecran.common.AbstractParamEcran;

/**
 * Parametrage de l'écran Visite Amap
 */
public class PEVisiteAmap  extends AbstractParamEcran
{
	// Mode de l'affichage  
	public GapViewer modeAffichage = GapViewer.WEEK;
	
	
	// Les information de contacts affichées
	public ChoixOuiNon producteurEmail = ChoixOuiNon.NON;
	public ChoixOuiNon producteurTel1 = ChoixOuiNon.NON;
	public ChoixOuiNon producteurTel2 = ChoixOuiNon.NON;
	
	public ChoixOuiNon referentEmail = ChoixOuiNon.NON;
	public ChoixOuiNon referentTel1 = ChoixOuiNon.NON;
	public ChoixOuiNon referentTel2 = ChoixOuiNon.NON;
	
	// Le mode d'affichage des infos producteurs
	public InfoProducteurModeAffichage infoProducteurModeAffichage = InfoProducteurModeAffichage.DESCRIPTION_PUIS_CONTACT;
	
	
	static public enum InfoProducteurModeAffichage
	{
		DESCRIPTION_PUIS_CONTACT , 
		
		CONTACT_PUIS_DESCRIPTION ; 
		
		
		static public class MetaData extends MetaDataEnum
		{
			
			public void fill()
			{		
				add("Ce champ vous permet de choisir le mode d'affichage pour le popup information producteur. La description d'un producteur est à renseigner dans TRESORIER / Gestion des producteurs / Modifier / Etape 1 / Description");

				add(DESCRIPTION_PUIS_CONTACT, "Description puis Contacts" , "Affichage de la description du producteur puis des contacts.");
				
				add(CONTACT_PUIS_DESCRIPTION, "Contacts puis description" , "Affichage des contacts puis de la description du producteur.");

			}
		}	
	}
	
	
	
	// Acces à cet écran 
	public PEVisiteAmapAccess accesEcran = PEVisiteAmapAccess.ALL;
	
	
	static public enum PEVisiteAmapAccess
	{
		ALL , 
		
		REFERENT , 
		
		NOBODY ;
		
		static public class MetaData extends MetaDataEnum
		{
			
			public void fill()
			{		
				add("Ce champ vous permet de choisir les personnes pouvant accéder à l'écran Visite AMAP");

				add(ALL, "Tout le monde" , "Dans ce mode, cet écran est accessible à tous.");
				
				add(REFERENT, "Referent,Trésorier,Admin" , "Dans ce mode, cet écran est accessible aux référents, trésoriers et admin");
				
				add(NOBODY, "Personne" , "Cet écran est caché pour tout le monde");
				

			}
		}	
	}
	
	@Override
	public boolean complyParamEcan(List<RoleList> roles)
	{
		if (accesEcran==PEVisiteAmapAccess.ALL)
		{
			return true;
		}
		if (accesEcran==PEVisiteAmapAccess.NOBODY)
		{
			return false;
		}
		
		return roles.contains(RoleList.REFERENT);
	}
	
	/*
	 * 
	 */

	public GapViewer getModeAffichage()
	{
		return modeAffichage;
	}

	public void setModeAffichage(GapViewer modeAffichage)
	{
		this.modeAffichage = modeAffichage;
	}

	public PEVisiteAmapAccess getAccesEcran() 
	{
		return accesEcran;
	}

	public void setAccesEcran(PEVisiteAmapAccess accesEcran) 
	{
		this.accesEcran = accesEcran;
	}

	public ChoixOuiNon getProducteurEmail()
	{
		return producteurEmail;
	}

	public void setProducteurEmail(ChoixOuiNon producteurEmail)
	{
		this.producteurEmail = producteurEmail;
	}

	public ChoixOuiNon getProducteurTel1()
	{
		return producteurTel1;
	}

	public void setProducteurTel1(ChoixOuiNon producteurTel1)
	{
		this.producteurTel1 = producteurTel1;
	}

	public ChoixOuiNon getProducteurTel2()
	{
		return producteurTel2;
	}

	public void setProducteurTel2(ChoixOuiNon producteurTel2)
	{
		this.producteurTel2 = producteurTel2;
	}

	public ChoixOuiNon getReferentEmail()
	{
		return referentEmail;
	}

	public void setReferentEmail(ChoixOuiNon referentEmail)
	{
		this.referentEmail = referentEmail;
	}

	public ChoixOuiNon getReferentTel1()
	{
		return referentTel1;
	}

	public void setReferentTel1(ChoixOuiNon referentTel1)
	{
		this.referentTel1 = referentTel1;
	}

	public ChoixOuiNon getReferentTel2()
	{
		return referentTel2;
	}

	public void setReferentTel2(ChoixOuiNon referentTel2)
	{
		this.referentTel2 = referentTel2;
	}

	public InfoProducteurModeAffichage getInfoProducteurModeAffichage()
	{
		return infoProducteurModeAffichage;
	}

	public void setInfoProducteurModeAffichage(InfoProducteurModeAffichage infoProducteurModeAffichage)
	{
		this.infoProducteurModeAffichage = infoProducteurModeAffichage;
	}
	
	
	
}
