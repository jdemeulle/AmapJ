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

public class VCTest
{
	public String nom;
	
	public VelocityVar prenom;
	
	public String email;
	
	
	

	public String getNom()
	{
		return nom;
	}

	public void setNom(String nom)
	{
		this.nom = nom;
	}

	public VelocityVar getPrenom()
	{
		return prenom;
	}

	public void setPrenom(VelocityVar prenom)
	{
		this.prenom = prenom;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}
	
	
	public static void main(String[] args) 
	{
		VCTest u = new VCTest();
		u.nom = "brun";
		u.prenom = new VelocityVar("prenom","manu");
		
		VelocityTools ctx = new VelocityTools();
		ctx.put("u", u);
		
		String in = "ceci est $u.nom $u.prenom ici";
		String out = ctx.evaluate(in);
		
		System.out.println("out="+out);
		
	}
}
