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
 package fr.amapj.view.engine.popup.formpopup.validator;

import com.vaadin.ui.AbstractField;


/**
 * Permet de valider que cet entier appartient bien à un intervalle donné
 * 
 * Les bornes sont considérées comme des valeurs correctes
 * 
 * La valeur null est considérée comme incorrecte
 *
 */
public class IntegerRangeValidator implements IValidator
{
	
	private Integer valMin = null;

    private Integer valMax = null;

    
    /**
     * 
     */
	public IntegerRangeValidator(Integer valMin,Integer valMax)
	{
		super();
		this.valMin = valMin;
		this.valMax = valMax;
	}




	@Override
	public void performValidate(Object value,ValidatorHolder a)
	{
		if (value==null)
		{
			a.addMessage("La valeur doit être renseignée");
			return;
		}

		int val;
		try
		{
			val = new Integer((String) value);	
		}
		catch (NumberFormatException e) 
		{
			 a.addMessage("Pour le champ \""+a.title+"\" , impossible de décoder "+value);
			 return;
		}
		
	    if (valMin!=null && val<valMin.intValue())
	    { 
	    	a.addMessage("La valeur \""+a.title+"\" est trop petite. Elle doit être supérieur ou égale à "+valMin);
	    }
	    		 
	    if (valMax!=null && val>valMax.intValue())
	    {
	    	a.addMessage("La valeur \""+a.title+"\" est trop grande. Elle doit être inférieur ou égale à "+valMax);
	    }
	}




	@Override
	public boolean canCheckOnFly()
	{
		return true;
	}
	
	@Override
	public AbstractField[] revalidateOnChangeOf()
	{
		return null;
	}

}
