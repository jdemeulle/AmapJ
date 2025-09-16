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
 package fr.amapj.service.services.parametres;

/**
 * Parametres de l'archivage 
 * 
 */
public class ParametresArchivageDTO 
{
	// Contrats
	public int archivageContrat;
	
	public int suppressionContrat;

	// Utilisateur
	public int archivageUtilisateur;
	
	// Producteurs
	public int archivageProducteur;
	
	
	// Période de permanence
	public int suppressionPeriodePermanence;
	
	
	// Periode de cotisation
	public int suppressionPeriodeCotisation;
	
	
 

	
	

	public int getArchivageContrat() 
	{
		return archivageContrat;
	}

	public void setArchivageContrat(int archivageContrat) 
	{
		this.archivageContrat = archivageContrat;
	}

	public int getSuppressionContrat() 
	{
		return suppressionContrat;
	}

	public void setSuppressionContrat(int suppressionContrat) 
	{
		this.suppressionContrat = suppressionContrat;
	}

	public int getArchivageProducteur() 
	{
		return archivageProducteur;
	}

	public void setArchivageProducteur(int archivageProducteur) 
	{
		this.archivageProducteur = archivageProducteur;
	}

	public int getSuppressionPeriodePermanence() 
	{
		return suppressionPeriodePermanence;
	}

	public void setSuppressionPeriodePermanence(int suppressionPeriodePermanence) 
	{
		this.suppressionPeriodePermanence = suppressionPeriodePermanence;
	}

	public int getSuppressionPeriodeCotisation() 
	{
		return suppressionPeriodeCotisation;
	}

	public void setSuppressionPeriodeCotisation(int suppressionPeriodeCotisation) 
	{
		this.suppressionPeriodeCotisation = suppressionPeriodeCotisation;
	}

	public int getArchivageUtilisateur() 
	{
		return archivageUtilisateur;
	}

	public void setArchivageUtilisateur(int archivageUtilisateur) 
	{
		this.archivageUtilisateur = archivageUtilisateur;
	}
	
	


	
}
