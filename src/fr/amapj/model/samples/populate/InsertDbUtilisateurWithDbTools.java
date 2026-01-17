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
 package fr.amapj.model.samples.populate;

import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.tools.TestTools;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.models.fichierbase.Utilisateur;

/**
 * Cette classe permet de créer des utilisateur dans la base de données
 *
 */
public class InsertDbUtilisateurWithDbTools
{
	
	@DbWrite
	public void createData()
	{
		RdbLink em = RdbLink.get();
		

		Utilisateur u = new Utilisateur();
		u.nom = "nom_c";
		u.prenom = "prenom_c";
		u.email = "c";
		
		
		em.persist(u);
		
		u = new Utilisateur();
		u.nom = "nom_d";
		u.prenom = "prenom_d";
		u.email = "d";
		
		em.persist(u);
		
		
	}

	public static void main(String[] args)
	{
		TestTools.init();
		
		InsertDbUtilisateurWithDbTools insertDbRole = new InsertDbUtilisateurWithDbTools();
		System.out.println("Debut de l'insertion des données");
		insertDbRole.createData();
		System.out.println("Fin de l'insertion des données");

	}

}
