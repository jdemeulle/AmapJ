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
 package fr.amapj.service.services.mescontrats;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.amapj.common.CollectionUtils;
import fr.amapj.model.models.contrat.modele.GestionDocEngagement;
import fr.amapj.model.models.contrat.modele.JokerMode;
import fr.amapj.model.models.contrat.modele.NatureContrat;
import fr.amapj.model.models.contrat.modele.TypJoker;
import fr.amapj.model.models.contrat.reel.TypInscriptionContrat;
import fr.amapj.view.engine.grid.integergrid.IntegerGridCell;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.ModeSaisie;
import fr.amapj.view.views.saisiecontrat.abo.ContratAbo;

/**
 * Represente un contrat ou un modele de contrat 
 *
 */
public class ContratDTO
{
	public Long contratId;
	
	public Long modeleContratId;
	
	public String nom;
	
	public Date dateFinInscription;

	// Caractéristiques des lignes
	public List<ContratLigDTO> contratLigs = new ArrayList<ContratLigDTO>();

	// Caractéristiques des colonnes
	public List<ContratColDTO> contratColumns = new ArrayList<ContratColDTO>();
	
	// Contient les quantites cell[numero_ligne][numero_colonne]
	public ContratCellDTO[][] cell;
		
	// Caractéristiques du paiement
	public InfoPaiementDTO paiement;
	
	// Nature du contrat 
	public NatureContrat nature;
	
	// Champs concernant uniquement les contrats de type abonnement avec joker
	public TypJoker typJoker;
	public int jokerNbMin = 0;
	public int jokerNbMax = 0;
	public JokerMode jokerMode;
	public int jokerDelai;
	
	// Signature du contrat
	public GestionDocEngagement gestionDocEngagement;
	
	// Utilisé uniquement lors de la signature en ligne du contrat
	public DocEngagementDTO docEngagementDTO;
	
	// Utilisé uniquement sur les contrats ABONNEMENT
	// Outil pour manipuler les contrats de type abonnement 
	public ContratAbo contratAbo;
	
	// 
	public TypInscriptionContrat typInscriptionContrat;
	
	// Champs relatifs à l'inscription 
	public ModeSaisie modeSaisie;
	
	
	/**
	 * @return true si toutes les quantités sont à zéro
	 */
	public boolean isEmpty()
	{
		for (int i = 0; i < contratLigs.size(); i++)
		{
			if (isEmptyLine(i)==false)
			{
				return false;
			}
		}
		return true;
	}
	
	
	public boolean isEmptyLine(int i)
	{
		for (int j = 0; j < contratColumns.size(); j++)
		{
			if (cell[i][j].qte!=0)
			{
				return false;
			}
		}
		return true;
	}
	
	public boolean isEmptyCol(int j)
	{
		for (int i = 0; i < contratLigs.size(); i++)
		{
			if (cell[i][j].qte!=0)
			{
				return false;
			}
		}
		return true;
	}
	
	
	public int getQteTotal()
	{
		int qte = 0;
		
		for (int j = 0; j < contratColumns.size(); j++)
		{		
			for (int i = 0; i < contratLigs.size(); i++)
			{
				qte = qte + cell[i][j].qte;
			}
		}
		return qte;
	}
	
	/**
	 * Permet de connaitre le nombre de livraison réelle pour un contrat d'un
	 * amapien donné
	 * 
	 * Exemple : il y a 42 livraisons sur un contrats, mais l'amapien a commandé
	 * uniquement sur 10 livraisons
	 * 
	 */
	public int getNbLivraisonEffective()
	{
		int nb=0;
		for (int i = 0; i < contratLigs.size(); i++)
		{
			if (isEmptyLine(i)==false)
			{
				nb++;
			}
		}
		return nb;
	}

	

	public int getMontantTotal()
	{
		int mnt = 0;
		
		for (int j = 0; j < contratColumns.size(); j++)
		{
			int prix = contratColumns.get(j).prix;
		
			for (int i = 0; i < contratLigs.size(); i++)
			{
				mnt = mnt + cell[i][j].qte * prix;
			}
		}
		return mnt;
	}
	
	
	/**
	 * Montant total consommé strictement avant dateRef
	 * 
	 * Si dateRef est null, alors retourne le montant total consommé de tout le contrat 
	 */
	public int getMontantTotalBefore(Date dateRef)
	{
		if (dateRef==null)
		{
			return getMontantTotal();
		}
		
		int mnt = 0;
		
		for (int i = 0; i < contratLigs.size(); i++)
		{
			if (contratLigs.get(i).date.before(dateRef))
			{
				for (int j = 0; j < contratColumns.size(); j++)
				{
					int prix = contratColumns.get(j).prix;
					mnt = mnt + cell[i][j].qte * prix;
				}
			}
		}
		return mnt;
	}
	
	
	/**
	 * Retourne le montant consommé à une date précise 
	 * 
	 * Retourne 0 si il n'y a pas de livraison à cette date 
	 */
	public int getMontantOf(Date dateRef) 
	{
		if (dateRef==null)
		{
			return 0;
		}
		
		int mnt = 0;
		
		for (int i = 0; i < contratLigs.size(); i++)
		{
			if (contratLigs.get(i).date.equals(dateRef))
			{
				for (int j = 0; j < contratColumns.size(); j++)
				{
					int prix = contratColumns.get(j).prix;
					mnt = mnt + cell[i][j].qte * prix;
				}
			}
		}
		return mnt;
	}
	
	

	/**
	 * Return true si toute la ligne est exclue
	 */
	public boolean isFullExcludedLine(int lineNumber)
	{
		for (int j = 0; j <  contratColumns.size(); j++)
		{
			if (cell[lineNumber][j].excluded==false)
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Retourne la liste des dates barrées
	 */
	public List<ContratLigDTO> computeDateBarrees()
	{
		return CollectionUtils.filter(contratLigs, e->isFullExcludedLine(e.i));
	}
	

	/**
	 * Retourne true si cette cellule est exclue,false sinon 
	 * 
	 */
	public boolean isExcluded(int i, int j)
	{
		return cell[i][j].excluded;
	}


	/**
	 * Retourne true si il y a au moins une cellule exclue
	 */
	public boolean hasOneOrMoreExcluded()
	{
		for (int j = 0; j < contratColumns.size(); j++)
		{
			for (int i = 0; i < contratLigs.size(); i++)
			{
				if (cell[i][j].excluded)
				{
					return true;
				}
			}
		}	
		return false;
	}
	
	
	public boolean[][] extractExcluded()
	{
		boolean[][] res = new boolean[contratLigs.size()][contratColumns.size()];
		
		for (int j = 0; j < contratColumns.size(); j++)
		{
			for (int i = 0; i < contratLigs.size(); i++)
			{
				res[i][j] = cell[i][j].excluded;
			}
		}	
		return res;
		
	}


	public void excludeThisLine(int lineNumber)
	{
		for (int j = 0; j <  contratColumns.size(); j++)
		{
			cell[lineNumber][j].excluded = true;
		}
	}
	
	public void enableThisLine(int lineNumber)
	{
		for (int j = 0; j <  contratColumns.size(); j++)
		{
			cell[lineNumber][j].excluded = false;
		}
	}
	
	

	public void copyInExcluded(boolean[][] box)
	{
		for (int j = 0; j < contratColumns.size(); j++)
		{
			for (int i = 0; i < contratLigs.size(); i++)
			{
				cell[i][j].excluded = box[i][j];
			}
		}	
	}
	
	
	
	// =============================
	
	
	public int[][] extractQte()
	{
		int[][] res = new int[contratLigs.size()][contratColumns.size()];
		
		for (int j = 0; j < contratColumns.size(); j++)
		{
			for (int i = 0; i < contratLigs.size(); i++)
			{
				res[i][j] = cell[i][j].qte;
			}
		}	
		return res;
		
	}



	public ContratLigDTO findLig(Date date) 
	{
		for (ContratLigDTO lig : contratLigs) 
		{
			if (lig.date.equals(date))
			{
				return lig;
			}
		}
		return null;
	}


	public IntegerGridCell[][] extractCells()
	{
		IntegerGridCell[][] res = new IntegerGridCell[contratLigs.size()][contratColumns.size()];
		
		for (int j = 0; j < contratColumns.size(); j++)
		{
			int prix = contratColumns.get(j).prix;
			
			for (int i = 0; i < contratLigs.size(); i++)
			{
				IntegerGridCell c = new IntegerGridCell();
				c.qte = cell[i][j].qte;
				c.prix = prix;
				
				// Si la case est exclue, alors elle est grisée, sauf si une quantité a été saisie à un moment suite à une erreur
				// Permet au référent de corriger les contrats avec des incohérences 
				if (cell[i][j].excluded && c.qte==0)
				{
					c.isStaticText = true;
					c.staticText = "XXXXXX";
				}
				// Si il n'y a pas de stock, la cellule est grisée, sauf si une quantité a déjà été saisie
				else if (cell[i][j].available==false && c.qte==0)
				{
					c.isStaticText = true;
					c.staticText = "Non disponible";
				}
				else
				{
					c.isStaticText = false;
				}
				
				res[i][j] = c;
			}
		}	
		
		return res;
	}
	


	/**
	 * Retourne true si toutes les cases de cette ligne 
	 * sont soit exclues, soit not available (pas de stock)
	 * 
	 *  Il n'est donc pas possible de commander pour cette date 
	 */
	public boolean isFullNotAvailableLine(int ligIndex)
	{
		for (int j = 0; j < contratColumns.size(); j++)
		{
			if (cell[ligIndex][j].excluded==false && cell[ligIndex][j].available==true)
			{
				return false;
			}
		}
		return true;
	}
}
