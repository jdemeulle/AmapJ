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
 package fr.amapj.view.views.saisiecontrat.abo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.FormatUtils;
import fr.amapj.model.models.contrat.modele.TypJoker;
import fr.amapj.service.services.mescontrats.ContratColDTO;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.mescontrats.ContratLigDTO;
import fr.amapj.service.services.mescontrats.ContratLigDTO.AboLigStatus;
import fr.amapj.view.engine.popup.formpopup.OnSaveException;
import fr.amapj.view.views.saisiecontrat.abo.model.AboData;
import fr.amapj.view.views.saisiecontrat.abo.model.AboDataLig;
import fr.amapj.view.views.saisiecontrat.abo.model.AboDataManager;
import fr.amapj.view.views.saisiecontrat.abo.model.AboDataProd;

public class ContratAbo 
{
	//
	public ContratDTO contratDTO;
	
	// Tableau de taille "nombre de produits" : pour chaque produit, donne la quantité unitaire
	private int qte[];
	
	public ContratAbo(ContratDTO contratDTO, int[] qte) 
	{
		this.contratDTO = contratDTO;
		this.qte = qte;
	}

	
	public int getQte(int indexProduit)
	{
		return qte[indexProduit];
	}
	
	/**
	 * Retourne le nombre de livraison prévues pour ce produit, en prenant en compte les dates exclues 
	 * et les dates jokers 
	 */
	public int getNbLivraison(int indexProduit)
	{
		int nbLivraison = 0;
		for (int i = 0; i < contratDTO.contratLigs.size(); i++)
		{
			ContratLigDTO lig = contratDTO.contratLigs.get(i);
			if (contratDTO.isExcluded(i,indexProduit)==false && lig.status!=AboLigStatus.FORCED_TO_0 && lig.status!=AboLigStatus.JOKER) 
			{
				nbLivraison++;
			}
		}
		return nbLivraison;
	}
	
	/**
	 * Retourne true si le produit est disponible à toutes les dates ou je devrais m'inscrire  
	 */
	public boolean isDisponible(int indexProduit)
	{
		for (int i = 0; i < contratDTO.contratLigs.size(); i++)
		{
			ContratLigDTO lig = contratDTO.contratLigs.get(i);
			if (contratDTO.isExcluded(i,indexProduit)==false && lig.status!=AboLigStatus.FORCED_TO_0 && lig.status!=AboLigStatus.JOKER) 
			{
				if (contratDTO.cell[i][indexProduit].available==false)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	
	
	

	/**
	 * On met à jour à la fois dans les quantitées condensées et dans le contratDTO
	 */
	public void updateQte(int indexProduit,int qteSaisie)
	{
		qte[indexProduit] = qteSaisie;
		int[] res = computeCol(indexProduit);
		for (int i = 0; i < contratDTO.contratLigs.size(); i++)
		{
			contratDTO.cell[i][indexProduit].qte = res[i];
		}
	}
	
	
	
	
	public int getNbJokersUsed()
	{
		return (int) contratDTO.contratLigs.stream().filter(e->e.status==AboLigStatus.JOKER || e.status==AboLigStatus.REPORT).count();
	}
	
	/**
	 * Retourne la liste de toutes les dates du contrat, dans une nouvelle liste non liée au ContratDTO
	 * Cette liste sera utilisée pour la manipulation des jokers
	 */
	public List<DateJokerInfo> getAllDates()
	{
		List<DateJokerInfo> res = new ArrayList<>();
		for (ContratLigDTO lig : contratDTO.contratLigs) 
		{
			DateJokerInfo di = new DateJokerInfo();
			
			di.date = lig.date;
			di.status = lig.status;
			di.i = lig.i;
			di.isModifiable= lig.isModifiable;
			di.reportDateDestination = lig.reportDateDestination;
			
			res.add(di);
		}
		return res;
	}

	static public class DateJokerInfo
	{
		public Date date;
		
		public AboLigStatus status;
		
		public boolean isModifiable;
		
		public int i;
		
		// Si status == REPORT : cette date est reportée vers celle ci 
		public Date reportDateDestination;
	}
	
	
	/**
	 * Méthode utilisée par le popup de mise à jour des jokers par l'amapien 
	 * 
	 * Cette méthode vérifie si la saisie est correcte
	 * Si la saisie est incorrecte, une exception OnSaveException est jetée 
	 */
	public void updateJokers(List<DateJokerInfo> dateInfos) throws OnSaveException
	{
		// On verifie d'abord la coherence avant de copier dans le modele
		for (DateJokerInfo dateJokerInfo : dateInfos) 
		{
			String msg = checkDateJokerInfo(dateJokerInfo,dateInfos);
			if (msg!=null)
			{
				throw new OnSaveException(msg);
			}
		}
		
		// On copie dans le modele
		for (DateJokerInfo dateJokerInfo : dateInfos) 
		{
			ContratLigDTO lig = contratDTO.contratLigs.get(dateJokerInfo.i);
			lig.status = dateJokerInfo.status;
			lig.reportDateDestination = dateJokerInfo.reportDateDestination;
		}
		
		// On verifie que tout est valide
		String str = checkRegleJokerValide();
		if (str!=null)
		{
			throw new AmapjRuntimeException(str);
		}
		
		// On recalcule toutes les quantités
		recomputeAllQte();
				
	}



	/**
	 * Permet le recalcul de toutes les quantités en se basant 
	 * sur les quantités unitaires et les dates JOKER, FORCED_0, ...
	 */
	private void recomputeAllQte() 
	{
		// On met ensuite à jour le tableau des quantités 
		for (int j = 0; j < contratDTO.contratColumns.size(); j++)
		{
			// On lit la quantité unitaire
			int qteSaisie = qte[j];
			
			updateQte(j, qteSaisie);
		}		
	}

	/**
	 * Permet la serialisation en base de données 
	 */
	public String getAboInfo()
	{
		AboData aboData = new AboData();
		
		for (ContratLigDTO ligDTO : contratDTO.contratLigs)
		{
			if (ligDTO.status!=AboLigStatus.NORMAL) 
			{
				AboDataLig lig = new AboDataLig();
				lig.date = ligDTO.date;
				lig.status = ligDTO.status;
				lig.reportDateDestination = ligDTO.reportDateDestination;
				aboData.ligs.add(lig);
			}
		}
		
		for (ContratColDTO contratColDTO : contratDTO.contratColumns) 
		{
			int qte = getQte(contratColDTO.j);
			if (qte!=0)
			{
				AboDataProd prod = new AboDataProd();
				prod.idProduit = contratColDTO.modeleContratProduitId;
				prod.qte = qte;
				aboData.prods.add(prod);
			}
		}
		
		return AboDataManager.toString(aboData);
	}
	
	
	/**
	 * Retourne null si les regles jokes sont valides, sinon retourne un message d'erreur
	 */
	public String checkRegleJokerValide()
	{
		SimpleDateFormat df = FormatUtils.getStdDate();
		
		// ON verifie l'absence de melange
		if (contratDTO.typJoker==TypJoker.SANS_JOKER)
		{
			long nb = contratDTO.contratLigs.stream().filter(e->e.status==AboLigStatus.JOKER || e.status==AboLigStatus.REPORT).count();
			if (nb!=0)
			{
				return "Le contrat est de type SANS JOKER et il y a "+nb+" jokers";
			}
		}
		if (contratDTO.typJoker==TypJoker.JOKER_ABSENCE)
		{
			long nb = contratDTO.contratLigs.stream().filter(e->e.status==AboLigStatus.REPORT).count();
			if (nb!=0)
			{
				return "Le contrat est de type JOKER_ABSENCE et il y a "+nb+" JOKER_REPORT";
			}
		}
		if (contratDTO.typJoker==TypJoker.JOKER_REPORT)
		{
			long nb = contratDTO.contratLigs.stream().filter(e->e.status==AboLigStatus.JOKER).count();
			if (nb!=0)
			{
				return "Le contrat est de type JOKER_REPORT et il y a "+nb+" JOKER_ABSENCE";
			}
		}
		
		// On verifie la coherence des lignes
		for (int i = 0; i < contratDTO.contratLigs.size(); i++)
		{
			ContratLigDTO lig = contratDTO.contratLigs.get(i);
			if (lig.status==AboLigStatus.REPORT) 
			{
				if (lig.reportDateDestination==null)
				{
					return "Pour la date "+df.format(lig.date)+" un REPORT est renseigné  mais la date de destination n'est pas renseignée";
				}
				ContratLigDTO ligDest = contratDTO.findLig(lig.reportDateDestination);
				if (ligDest==null)
				{
					return "Pour la date "+df.format(lig.date)+" un REPORT est renseigné  mais la date de destination n'est pas trouvable.";
				}
				if (ligDest.status!=AboLigStatus.NORMAL)
				{
					return "Pour la date "+df.format(lig.date)+" un REPORT est renseigné  vers la date "+df.format(lig.reportDateDestination)+". Ceci est impossible, car la date destination est déjà une date reportée";
				}
				
				// Cas des produits barres : on ne peut pas faire un report vers une date ou le produit est barré
				for (ContratColDTO col : contratDTO.contratColumns) 
				{
					if (qte[col.j]!=0 && contratDTO.isExcluded(lig.i,col.j)==false && contratDTO.isExcluded(ligDest.i, col.j)==true)
					{
						return "Pour la date "+df.format(lig.date)+" un REPORT est renseigné  vers la date "+df.format(lig.reportDateDestination)+". Ceci est impossible pour le produit "+col.nomProduit+", car celui ci n'est pas disponible le "+df.format(lig.reportDateDestination);
					}
				}
			}
		}
		
		
		// On verifie si l'utilisateur a saisi suffisamment ou pas trop de date jokers
		if(getNbJokersUsed()<contratDTO.jokerNbMin)
		{
			return "Vous n'avez pas saisi suffisamment de dates jokers. Il faut au minimum "+contratDTO.jokerNbMin+" jokers.";
		}
		
		if(getNbJokersUsed()>contratDTO.jokerNbMax)
		{
			return "Vous avez saisi trop de dates jokers. Il faut au maximum "+contratDTO.jokerNbMax+" jokers.";
		}
		
		return null;
	}
	
	

	private String checkDateJokerInfo(DateJokerInfo dateJokerInfo, List<DateJokerInfo> dateInfos) 
	{
		SimpleDateFormat df = FormatUtils.getStdDate();
		if (dateJokerInfo.status==AboLigStatus.REPORT) 
		{
			if (dateJokerInfo.reportDateDestination==null)
			{
				return "Pour la date "+df.format(dateJokerInfo.date)+" un REPORT est renseigné  mais la date de destination n'est pas renseignée";
			}
			if (dateJokerInfo.date.equals(dateJokerInfo.reportDateDestination))
			{
				return "Pour la date "+df.format(dateJokerInfo.date)+" un REPORT est renseigné  mais vers la même date.";
			}
			DateJokerInfo ligDest = findLig(dateInfos,dateJokerInfo.reportDateDestination);
			if (ligDest==null)
			{
				return "Pour la date "+df.format(dateJokerInfo.date)+" un REPORT est renseigné  mais la date de destination n'est pas trouvable.";
			}
			if (ligDest.status!=AboLigStatus.NORMAL)
			{
				return "Pour la date "+df.format(dateJokerInfo.date)+" un REPORT est renseigné  vers la date "+df.format(dateJokerInfo.reportDateDestination)+". Ceci est impossible, car la date destination est déjà une date reportée";
			}
			
			// Cas des produits barres : on ne peut pas faire un report vers une date ou le produit est barré
			for (ContratColDTO col : contratDTO.contratColumns) 
			{
				if (qte[col.j]!=0 && contratDTO.isExcluded(dateJokerInfo.i,col.j)==false && contratDTO.isExcluded(ligDest.i, col.j)==true)
				{
					return "Pour la date "+df.format(dateJokerInfo.date)+" un REPORT est renseigné  vers la date "+df.format(dateJokerInfo.reportDateDestination)+". Ceci est impossible pour le produit "+col.nomProduit+", car celui ci n'est pas disponible le "+df.format(dateJokerInfo.reportDateDestination);
				}
			}
		}
		return null;
	}



	private DateJokerInfo findLig(List<DateJokerInfo> dateInfos, Date date) 
	{
		for (DateJokerInfo lig : dateInfos) 
		{
			if (lig.date.equals(date))
			{
				return lig;
			}
		}
		return null;
	}


	/**
	 * Vérifie la coherence des données :
	 * 
	 * => les regles Jokers sont bien formées
	 * => le recalul indique une concordance entre les quantités de la grille et les données AboData
	 * 
	 */
	public String checkValid() 
	{
		// 
		String str = checkRegleJokerValide();
		if (str!=null)
		{
			return str;
		}
		
		
		// Verification de la concordance entre les quantités de la grille et les données AboData	
		for (int j = 0; j < contratDTO.contratColumns.size(); j++)
		{
			int[] res = computeCol(j);
			
			for (int i = 0; i < contratDTO.contratLigs.size(); i++)
			{
				if (res[i]!=contratDTO.cell[i][j].qte)
				{
					return "Il y a une erreur sur le recalcul de la quantité à la ligne "+(i+1)+" colonne "+(j+1);
				}
			}
		}
		return null;
	}
	
	// CALCUL DES QUANTITES D'UNE COLONNE A PARTIR DES DONNEES ABODATA
	private int[] computeCol(int indexProduit) 
	{		
		int[] res = new int[contratDTO.contratLigs.size()];
		
		int qteSaisie = qte[indexProduit];
		
		// On l'applique à toutes les dates (sauf si exclusion ou si date joker ou si contrat commencé en retardataire) 
		for (int i = 0; i < contratDTO.contratLigs.size(); i++)
		{
			ContratLigDTO lig = contratDTO.contratLigs.get(i);
			if (contratDTO.isExcluded(i,indexProduit) || lig.status==AboLigStatus.FORCED_TO_0 || lig.status==AboLigStatus.JOKER || lig.status==AboLigStatus.REPORT) 
			{
				res[i] = 0;
			}
			else
			{
				res[i] = qteSaisie;
			}			
		}
		
		// On fait ensuite le calcul des reports
		for (int i = 0; i < contratDTO.contratLigs.size(); i++)
		{
			ContratLigDTO lig = contratDTO.contratLigs.get(i);
			if (contratDTO.isExcluded(i,indexProduit)==false && lig.status==AboLigStatus.REPORT)
			{
				ContratLigDTO ligDest = contratDTO.findLig(lig.reportDateDestination);
				if (ligDest==null || ligDest.status!=AboLigStatus.NORMAL)
				{
					throw new AmapjRuntimeException("Erreur à la ligne "+i+" REPORT incorrect");
				}
				res[ligDest.i] += qteSaisie;
			}
		}
		
		return res;
	}

	/**
	 * Réalise un dump des regles et des quantites
	 */
	public String dump() 
	{
		StringBuffer buf = new StringBuffer();
		
		for (int j = 0; j <contratDTO.contratColumns.size(); j++) 
		{
			buf.append("Prod  "+j+" Qte = "+qte[j]+"\n");
		}
		
		//
		SimpleDateFormat df = FormatUtils.getStdDate();
		for (int i = 0; i < contratDTO.contratLigs.size(); i++)
		{
			ContratLigDTO lig = contratDTO.contratLigs.get(i);
			String dest = lig.reportDateDestination==null ? "" : df.format(lig.reportDateDestination);
			buf.append("Lig "+i+" Date = "+df.format(lig.date) +"  status = "+lig.status+ " Destination Date = "+dest+"\n");
		}
		return buf.toString();
	}
}
