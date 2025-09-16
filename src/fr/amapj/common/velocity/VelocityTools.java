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
 package fr.amapj.common.velocity;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.service.services.edgenerator.velocity.VCAmapien;


public class VelocityTools
{
	private VelocityContext ctx;
	
	private List<VelocityVar> vars;
	
	public VelocityTools()
	{
		ctx = new VelocityContext();
		vars = new ArrayList<>();
	}
	
	public VelocityVar createVar(String varName,String content)
	{
		VelocityVar var = new VelocityVar(varName, content);
		vars.add(var);
		return var;
	}
	
	public void put(String key, Object value)
	{
		ctx.put(key, value);
	}
	
	public String evaluate(String in)
	{				
		StringReader reader = new StringReader(in);
		StringWriter writer = new StringWriter();

		boolean ret = Velocity.evaluate(ctx, writer, "velocity", reader);
		if (ret==false)
		{
			throw new AmapjRuntimeException("Impossible d'évaluer le contexte");
		}
		return writer.toString();
	}
	
	/**
	 * Retourne la liste des variables utilisées
	 */
	public List<VelocityVar> getUsedVars()
	{
		return vars.stream().filter(e->e.used).collect(Collectors.toList());
	}
	
	/**
	 * Retourne la liste de toutes les variables du contexte
	 */
	public List<VelocityVar> getAllVars() 
	{
		return vars;
	}
	

	public static void main(String[] args)
	{
		Velocity.init();

		VelocityTools vt = new VelocityTools();

		VCAmapien a = new VCAmapien();
		a.nom = "toto";
		vt.put( "a", a);

		String str = vt.evaluate("essai $a.nom");
		
		System.out.println("res="+str);
	
	}

	
	
	

}
