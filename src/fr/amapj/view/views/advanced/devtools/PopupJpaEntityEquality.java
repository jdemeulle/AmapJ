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
 package fr.amapj.view.views.advanced.devtools;

import java.util.List;

import fr.amapj.model.models.fichierbase.EtatUtilisateur;
import fr.amapj.service.services.advanced.devtools.DevToolsService;
import fr.amapj.service.services.advanced.supervision.SupervisionService;
import fr.amapj.service.services.utilisateur.UtilisateurDTO;
import fr.amapj.service.services.utilisateur.UtilisateurService;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;

/**
 * Permet de modifier les paramètres mineurs des modeles de contrat
 * 
 *
 */
public class PopupJpaEntityEquality extends WizardFormPopup
{
	
	private Long id;
	

	/**
	 * 
	 */
	public PopupJpaEntityEquality()
	{		
		setWidth(80);
		popupTitle = "Test de l'égalité des entités";
				
	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldInfos());
		add(()->addFieldAffichage1());
		add(()->addFieldReset());
		add(()->addFieldAffichage2());
		add(()->addFieldRecherche());
		add(()->addFieldRecherche2());
		add(()->addFieldChargement());
	}

	

	private void addFieldInfos()
	{	
		setStepTitle("Infos");
				
		String str = "Cet outil permet de tester le cache JPA , son reset et l'impact sur l'opération == entre deux Long<br/>"+
					 "Les deux Long ayant la même valeur mais provenant avant et aprés le reset du cache JPA";
		
		addHtml(str);
	}
	
	private void addFieldAffichage1()
	{	
		setStepTitle("Affichage 1");

		String str = "Voici la liste de tous les utilisateurs de la base<br/>";
		
		List<UtilisateurDTO> users = new UtilisateurService().getAllUtilisateurs(EtatUtilisateur.ACTIF);
		for (UtilisateurDTO utilisateurDTO : users)
		{
			str = str+utilisateurDTO.nom+" "+utilisateurDTO.prenom+" Id = "+utilisateurDTO.id+" @Long id="+ System.identityHashCode(utilisateurDTO.id);
			id = utilisateurDTO.id;
		}
		
		
		
		
		addHtml(str);
		
	
		
	}
	
	
	private void addFieldReset()
	{	
		setStepTitle("Reset");

		String str = "Un appel au reset du cache JPA puis un garbage collector ont été faits";
		
		new SupervisionService().resetAllDataBaseCache();
		System.gc();
		
		addHtml(str);
	
	}
	
	
	private void addFieldAffichage2()
	{	
		setStepTitle("Affichage 2");

		String str = "Voici la liste de tous les utilisateurs de la base<br/>";
		
		List<UtilisateurDTO> users = new UtilisateurService().getAllUtilisateurs(EtatUtilisateur.ACTIF);
		for (UtilisateurDTO utilisateurDTO : users)
		{
			str = str+utilisateurDTO.nom+" "+utilisateurDTO.prenom+" Id = "+utilisateurDTO.id+" @Long id="+ System.identityHashCode(utilisateurDTO.id);
		}
		
		addHtml(str);
		
	
		
	}
	
	private void addFieldRecherche()
	{	
		setStepTitle("Recherche");

		String str = "On essaye maintenant de retrouver le dernier utilisateur en faisant un == entre deux Longs <br/>";
		
		boolean found = false;
		
		List<UtilisateurDTO> users = new UtilisateurService().getAllUtilisateurs(EtatUtilisateur.ACTIF);
		for (UtilisateurDTO utilisateurDTO : users)
		{
			if (utilisateurDTO.id==id)
			{
				found = true;
			}
		}
		
		
		if (found)
		{
			str = str+" Trouvé.";
		}
		else
		{
			str = str+" Impossible de retrouver l'élément !!!! ";  // On se retrouve dans ce cas là 
		}
		
		
		addHtml(str);
		
	
		
	}
	
	
	private void addFieldRecherche2()
	{	
		setStepTitle("Recherche");

		String str = "On fait la même chose avec un equals entre deux Longs <br/>";
		
		boolean found = false;
		
		List<UtilisateurDTO> users = new UtilisateurService().getAllUtilisateurs(EtatUtilisateur.ACTIF);
		for (UtilisateurDTO utilisateurDTO : users)
		{
			if (utilisateurDTO.id.equals(id))
			{
				found = true;
			}
		}
		
		
		if (found)
		{
			str = str+" Trouvé."; // On se retrouve dans ce cas là 
		}
		else
		{
			str = str+" Impossible de retrouver l'élément !!!! ";  
		}
		
		
		addHtml(str);
		
	
		
	}
	
	private void addFieldChargement()
	{	
		setStepTitle("Chargement");

		String str = "On essaye maintenant de faire un em.find() <br/>";
		
		str = str + "On obtient " +new DevToolsService().loadOneUtilisateur(id);   // Ca fonctionne normalement 
		
		addHtml(str);

	}
	
	


	@Override
	protected void performSauvegarder()
	{
	}

}
