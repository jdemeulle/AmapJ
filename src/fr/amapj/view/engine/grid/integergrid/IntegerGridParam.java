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
 package fr.amapj.view.engine.grid.integergrid;

import java.util.ArrayList;
import java.util.List;

import fr.amapj.view.engine.grid.GridHeaderLine;
import fr.amapj.view.engine.grid.integergrid.lignecumul.LigneCumulParam;


/**
 * Liste des parametres pour un PopupIntegerGrid
 *
 */
public class IntegerGridParam
{
	//
	public int nbLig;
	
	//
	public int nbCol;
	
	// Contient les caracteristiques  de chaque cellule prix[numero_ligne][numero_colonne]
	// Attention : ceci doit être un nouveau tableau, car il est modifié
	public IntegerGridCell[][] cell;
		
	// Largeur des colonnes de saisie  en pixel, exemple 110
	public int largeurCol;
	
	// Grille en lecture seule
	public boolean readOnly;
	
	// Message specifique a afficher en haut de popup
	public String messageSpecifique;

	// Paramètres de la ligne de cumul
	public LigneCumulParam ligneCumulParam = new LigneCumulParam();

	// Message specifique a afficher en bas de popup
	public String messageSpecifiqueBottom;
	
	public List<GridHeaderLine> headerLines = new ArrayList<>();
	
	// Largeur de la colonne de gauche en pixel
	public int leftPartLineLargeur;
	
	// Style de la colonne de gauche 
	public String leftPartLineStyle;
	
	// Description des lignes
	public List<IntegerGridLine> lines = new ArrayList<>();
	
	// si true, il est autorisé de saisir une grille avec uniquement des 0
	// si false, il est impossible de saisir une grille avec uniquement des 0
	public boolean allowedEmpty = false;
	
	// Si true,présence d'un boutton copier la première ligne  
	public boolean buttonCopyFirstLine = true;
	
	// Libellé pour le bouton Sauvegarder / Valider
	public String libButtonSave;
	
	// 
	public Runnable switchButtonAction;
	
	
	/**
	 * Cette méthode met à jour les quantités
	 * 
	 */
	public void updateQte(int lig,int col,int newQte)
	{	
		cell[lig][col].qte = newQte;
	}
	
	/**
	 * Cette fonction doit être utilisée uniquement par PopupIntegerGrid
	 * 
	 * On retourne le montant total des lignes <b>visibles</b> uniquement  
	 */
	public int getMontantTotal()
	{
		int montantTotal = 0;
		for (int i = 0; i < nbLig; i++)
		{
			if (lines.get(i).isVisible)
			{
				for (int j = 0; j < nbCol; j++)
				{
					IntegerGridCell c = cell[i][j];
					montantTotal = montantTotal +c.qte*c.prix;
				}
			}
		}
		return montantTotal;
	}
	
	
	/**
	 * Cette fonction doit être utilisée uniquement par PopupIntegerGrid
	 */
	public int getMontantOfLine(int i)
	{
		int montantTotal = 0;
		for (int j = 0; j < nbCol; j++)
		{
			IntegerGridCell c = cell[i][j];
			montantTotal = montantTotal +c.qte*c.prix;
		}	
		return montantTotal;
	}
	
	
	
	/**
	 * Retourne true si la grille est vide (uniquement des 0)
	 */
	public boolean isEmpty()
	{
		for (int i = 0; i < nbLig; i++)
		{
			for (int j = 0; j < nbCol; j++)
			{
				if (cell[i][j].qte!=0)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	public int getNbLineVisible()
	{
		int nb=0;
		for (int i = 0; i < nbLig; i++)
		{
			if (lines.get(i).isVisible)
			{
				nb++;
			}
		}
		return nb;
		
	}
}
