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
 package fr.amapj.service.services.edgenerator.velocity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.StringUtils;
import fr.amapj.common.velocity.VelocityTools;
import fr.amapj.common.velocity.VelocityVar;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.contrat.modele.ModeleContrat;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.service.services.docengagement.signonline.core.DocEngagementSignOnLineTools;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.gestioncontrat.ModeleContratDTO;
import fr.amapj.service.services.gestioncontrat.ModeleContratSummaryDTO;
import fr.amapj.service.services.mescontrats.ContratColDTO;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.mescontrats.ContratLigDTO;
import fr.amapj.service.services.mescontrats.DatePaiementDTO;
import fr.amapj.service.services.mescontrats.InfoPaiementDTO;
import fr.amapj.service.services.mescontrats.MesContratsService;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;


public class VCContrat
{
	public String nom;
	
	public String description;
	
	public String dateDebut;
	
	public String dateFin;
	
	public String dateFinInscription;
	
	public String nbLivraison;
	
	public String saison;
	
	public VelocityVar libCheque;
	
	public String dateRemiseCheque;
	
	public VelocityVar nbCheque;
	
	public VelocityVar tableauDateProduit;
	
	public VelocityVar tableauDateProduitCompact;
	
	public VelocityVar tableauDateCheque;
	
	public VelocityVar montantProduit;
	
	public VelocityVar montantCheque;
	
	public VelocityVar montantAvoir;

	public VelocityVar listeDateProduit;

	public VelocityVar listeDateProduitCompact;
	
	public VelocityVar tableauOrListeDateProduit;
	
	public VelocityVar listeDateCheque;
	
	public VelocityVar listeDateChequeCompact;
	
	public VelocityVar amapienNbLivraison;
	
	public VelocityVar amapienNbProduit;
	
	public VelocityVar jokerNbMax;
	
	public VelocityVar jokerNbMin;
	
	public VelocityVar jokerTxt;
	
	public String signatureAmapien;
	
	public String signatureProducteur;
	
	
	
	
	private VelocityTools ctx;
		
	private Mode mode;

	

	public enum Mode
	{
		STANDARD,
		
		VIERGE,
		
		EN_SIGNATURE
	}
	
	
	
	
	/**
	 * Trois cas sont possible 
	 * 
	 * c not null, dto null : correspond à la génération d'un contrat déjà enregistré dans la base de données (STANDARD)  
	 * c null, dto null : correspond à la génération d'un contrat vierge     (VIERGE) 
	 * c null, dto not null : correspond à la génération d'un contrat en cours de saisie et de signature , non enregistré en base de données (EN_SIGNATURE)
	 * 
	 * 
	 * @param mc : n'est jamais null
	 */
	public void load(ModeleContrat mc,Contrat c,ContratDTO contratDTO,RdbLink em,VelocityTools ctx)
	{
		this.ctx = ctx;
		this.mode = computeMode(c,contratDTO);
		
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			
		ModeleContratSummaryDTO sum = new GestionContratService().createModeleContratInfo(em, mc);
		ModeleContratDTO dto = new GestionContratService().loadModeleContrat(mc.getId());
		
		nom = s(dto.nom);
		description = s(dto.description);
		dateDebut = df.format(dto.dateDebut);
		dateFin = df.format(dto.dateFin);
		dateFinInscription = "";
		if (dto.dateFinInscription!=null)
		{
			dateFinInscription = df.format(dto.dateFinInscription);
		}
		saison = getSaison(dto.dateDebut,dto.dateFin);
		nbLivraison = ""+sum.nbLivraison;
		libCheque = v("libCheque",s(dto.libCheque));
		dateRemiseCheque = "";
		if (dto.dateRemiseCheque!=null)
		{
			dateRemiseCheque = df.format(dto.dateRemiseCheque);
		}
		
		// Information sur les jokers
		jokerNbMax = v("jokerNbMax",""+mc.jokerNbMax);
		jokerNbMin = v("jokerNbMin",""+mc.jokerNbMin);
		jokerTxt = v("jokerTxt",computeJokerTxt(mc));
		
		
		
		contratDTO = computeContratDTO(mc,c,contratDTO);
		mntTotal = contratDTO.getMontantTotal();
	
		tableauDateProduit = v("tableauDateProduit",getTableauDateProduit(em,contratDTO,false));
		tableauDateProduitCompact = v("tableauDateProduitCompact",getTableauDateProduit(em,contratDTO,true));
		tableauDateCheque = v("tableauDateCheque",getTableauCheque(em,contratDTO));
		
		listeDateProduit = v("listeDateProduit",getListeDateProduit(em,contratDTO,false));
		listeDateProduitCompact = v("listeDateProduitCompact",getListeDateProduit(em,contratDTO,true));
		
		listeDateCheque = v("listeDateCheque",getListeDateCheque(em,contratDTO,false));
		listeDateChequeCompact = v("listeDateChequeCompact",getListeDateCheque(em,contratDTO,true));
		
		tableauOrListeDateProduit = v("tableauOrListeDateProduit",getTableauOrListeDateProduit(contratDTO));
		
		
		//
		if (mode==Mode.STANDARD || mode==Mode.EN_SIGNATURE)
		{
			nbCheque = v("nbCheque",""+contratDTO.paiement.datePaiements.stream().filter(e->e.montant!=0).count());
			
			montantProduit = v("montantProduit",new CurrencyTextFieldConverter().convertToString(contratDTO.getMontantTotal()));
			
			montantCheque = v("montantCheque",new CurrencyTextFieldConverter().convertToString(contratDTO.paiement.getMontantTotalPaiement()));
		
			montantAvoir = v("montantAvoir",new CurrencyTextFieldConverter().convertToString(contratDTO.paiement.avoirInitial));
			
			amapienNbLivraison = v("amapienNbLivraison",""+contratDTO.getNbLivraisonEffective());
			amapienNbProduit = v("amapienNbProduit",""+contratDTO.getQteTotal());
		}
		else if (mode==Mode.VIERGE)
		{
			nbCheque = v("nbCheque","");
			montantProduit = v("montantProduit","");
			montantCheque = v("montantCheque","");
			montantAvoir = v("montantAvoir","");
			amapienNbLivraison = v("amapienNbLivraison","");
			amapienNbProduit = v("amapienNbProduit","");
		}
		else 
		{
			throw new AmapjRuntimeException();
		}
		
		// Signature - caractères spéciaux qui seront remplacés après la création du pdf
		signatureAmapien = "<span style=\"font-size: 2pt;\">"+DocEngagementSignOnLineTools.CHAR_SIGNATURE_AMAPIEN+"</span>";        // U+0142 
		signatureProducteur = "<span style=\"font-size: 2pt;\">"+DocEngagementSignOnLineTools.CHAR_SIGNATURE_PRODUCTEUR+"</span>";  // U+0140
		
	}


	/**
	 * De 1 à 10 colonnes : mode tableau 
	 * Au dela : mode liste 
	 */
	private String getTableauOrListeDateProduit(ContratDTO contratDTO) 
	{
		if (contratDTO.contratColumns.size()>10)
		{
			return listeDateProduit.content;
		}
		else
		{
			return tableauDateProduit.content;
		}
		
	}


	private ContratDTO computeContratDTO(ModeleContrat mc, Contrat c, ContratDTO contratDTO) 
	{
		switch (mode) 
		{
			case STANDARD: return new MesContratsService().loadContrat(mc.getId(), c.getId());
			case VIERGE : return new MesContratsService().loadContrat(mc.getId(), null);
			case EN_SIGNATURE : return contratDTO; 	
			default: throw new AmapjRuntimeException();
		}
	}

	private Mode computeMode(Contrat c, ContratDTO contratDTO) 
	{
		if (c!=null && contratDTO==null)
		{
			return Mode.STANDARD;
		}
		if (c==null && contratDTO==null)
		{
			return Mode.VIERGE;
		}
		if (c==null && contratDTO!=null)
		{
			return Mode.EN_SIGNATURE;
		}
		throw new AmapjRuntimeException("c="+c+"contratDto="+contratDTO);
	}




	private String computeJokerTxt(ModeleContrat mc) 
	{
		switch (mc.typJoker) 
		{
		case SANS_JOKER:
			return "Ce contrat n'accepte pas de jokers.";
			
		case JOKER_ABSENCE:
			return computeJokerTxt(mc,"absence (les paniers sont annulés et non payés).");
			
		case JOKER_REPORT:
			return computeJokerTxt(mc,"report (les paniers sont reportés à une autre date).");
		
		default:
			throw new AmapjRuntimeException("x="+mc.typJoker);
		}
	}




	private String computeJokerTxt(ModeleContrat mc, String lib) 
	{
		if (mc.jokerNbMax==mc.jokerNbMin)
		{
			return "Ce contrat impose "+mc.jokerNbMin+" joker(s) en mode "+lib;
		}
		else
		{
			return "Ce contrat accepte entre "+mc.jokerNbMin+" et "+mc.jokerNbMax+" joker(s) en mode "+lib;
		}
	}


	private int mntTotal;

	public String montantProduitTVA(double taux)
	{
		// Si contrat vierge, pas de montant produit 
		if (mntTotal==0)
		{
			return "";
		}
		
		double montant = mntTotal;
		double mntTVA = montant-montant/(1+taux/100.0);
		
		int mntCentimes = (int) Math.round(mntTVA);
		
		return new CurrencyTextFieldConverter().convertToString(mntCentimes);
	}
	


	/**
	 * Permet d'escaper les caracteres HTML  
	 */
	private String s(String value)
	{
		return VCBuilder.s(value);
	}
	
	
	private String getTableauDateProduit(RdbLink em, ContratDTO contratDTO,boolean compact)
	{
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
		StringBuffer buf = new StringBuffer();
		
		//		
		buf.append("<table style=\"border:1px solid black;border-collapse:collapse;width:100%;table-layout:fixed;text-align:center;\"><tbody>");
		
		// La ligne de titre
		
		buf.append("<tr>");
		
		buf.append("<td style=\"border:1px solid black;\">");
		buf.append("<p style=\"margin:0.1em\">DATE</p>");
		buf.append("</td>");
		
		
		List<ContratColDTO> contratColumns = filterCol(compact,contratDTO.contratColumns,contratDTO);
		for (ContratColDTO contratColDTO : contratColumns)
		{
			buf.append("<td style=\"border:1px solid black;\">");
			buf.append("<p style=\"margin:0.1em\">"+s(contratColDTO.nomProduit)+"</p>");
			buf.append("<p style=\"margin:0.1em\"><b>"+new CurrencyTextFieldConverter().convertToString(contratColDTO.prix)+"€ </b></p>");
			buf.append("<p style=\"margin:0.1em\">"+s(contratColDTO.condtionnementProduit)+"</b></p>");
			buf.append("</td>");
		}
		
		buf.append("</tr>");
		
		// Les lignes pour chaque date
		List<ContratLigDTO> contratLigs = contratDTO.contratLigs;
		for (ContratLigDTO contratLigDTO : contratLigs)
		{
			if (compact==false  || contratDTO.isEmptyLine(contratLigDTO.i)==false)
			{	
				buf.append("<tr>");
				
				buf.append("<td style=\"border:1px solid black;\">");
				buf.append("<p style=\"margin:0.1em\">"+df.format(contratLigDTO.date)+"</p>");
				buf.append("</td>");
				
				for (ContratColDTO contratColDTO : contratColumns)
				{
					buf.append("<td style=\"border:1px solid black;\">");
					buf.append("<p style=\"margin:0.1em\">");
					int qte = contratDTO.cell[contratLigDTO.i][contratColDTO.j].qte;
					if (qte!=0)
					{
						buf.append(""+qte);
					}
					buf.append("</p>");
					buf.append("</td>");
				}
				
				buf.append("</tr>");
			}
		}
		
		buf.append("</tbody></table>");
		return buf.toString();
	}

	private List<ContratColDTO> filterCol(boolean compact, List<ContratColDTO> contratColumns,ContratDTO contratDTO) 
	{
		if (compact==false)
		{
			return contratColumns;
		}
		return contratColumns.stream().filter(e->contratDTO.isEmptyCol(e.j)==false).collect(Collectors.toList());
	}


	private String getTableauCheque(RdbLink em, ContratDTO contratDTO)
	{
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
		StringBuffer buf = new StringBuffer();
		InfoPaiementDTO paiement = contratDTO.paiement;
		
		if (paiement.avoirInitial!=0)
		{
			buf.append("<p>Avoir initial : "+new CurrencyTextFieldConverter().convertToString(paiement.avoirInitial)+" €</p>");
		}
		
		//
		buf.append("<table style=\"border:1px solid black;border-collapse:collapse;width:100%;\"><tbody>");
		
		// La ligne de titre
		buf.append("<tr>");
		
		buf.append("<td style=\"border:1px solid black;width:50%\">");
		buf.append("<p style=\"margin:0.1em\">DATE DE DEBIT</p>");
		buf.append("</td>");
		
		buf.append("<td style=\"border:1px solid black;width:50%\">");
		buf.append("<p style=\"margin:0.1em\">MONTANT</p>");
		buf.append("</td>");
		buf.append("</tr>");
		
		
		
		for (DatePaiementDTO date : paiement.datePaiements)
		{
			// Si c'est un vierge : on met toujours la ligne
			// Si c'est un contrat classique : on supprime les lignes sans paiement
			if ( (date.montant!=0) || mode==Mode.VIERGE)
			{
				buf.append("<tr>");
				
				buf.append("<td style=\"border:1px solid black;width:50%\">");
				buf.append("<p style=\"margin:0.1em\">"+df.format(date.datePaiement)+"</p>");
				buf.append("</td>");
				
				buf.append("<td style=\"border:1px solid black;width:50%\">");
				buf.append("<p style=\"margin:0.1em\">");
				if (mode!=Mode.VIERGE)
				{
					buf.append(""+new CurrencyTextFieldConverter().convertToString(date.montant)+" €");
				}
				buf.append("</p>");
				buf.append("</td>");
				
				buf.append("</tr>");
			}
		}
		
		buf.append("</tbody></table>");
		return buf.toString();
	}
	
	
	private String getListeDateProduit(RdbLink em, ContratDTO contratDTO,boolean compact)
	{
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
		
		CompactorTools ct = new CompactorTools(compact);
		
		List<ContratColDTO> contratColumns = contratDTO.contratColumns;
		List<ContratLigDTO> contratLigs = contratDTO.contratLigs;
		
		// Calcul de la liste des couples (Date) (Liste de produits) pour les compacter
		int nbCol = contratColumns.size();
		int i=0;
		for (ContratLigDTO contratLigDTO : contratLigs)
		{
			String s1 = df.format(contratLigDTO.date);
			List<String> s2 = new ArrayList<String>(); 
			
			for (int j=0;j<nbCol;j++)
			{
				if (contratDTO.cell[i][j].qte!=0)
				{
					ContratColDTO contratColDTO = contratColumns.get(j);
					StringBuffer buf = new StringBuffer();
				
					buf.append(contratDTO.cell[i][j].qte+" ");
					buf.append(s(contratColDTO.nomProduit)+" (");
					buf.append(s(contratColDTO.condtionnementProduit));
					buf.append(" - "+new CurrencyTextFieldConverter().convertToString(contratColDTO.prix)+" €)");
					
					s2.add(buf.toString());
				}
			}
			
			ct.addLine(s1, StringUtils.asString(s2, ", "));
			i++;
		}
		
		// Formatage du HTML
		List<String> res = ct.getResult("Le ","Les ", ", ", ": ", "");
		StringBuffer buf = new StringBuffer();		
		buf.append("<ul style=\"margin:0pt;\">");
		for (String s : res)
		{
			buf.append("<li>");
			buf.append(s);
			buf.append("</li>");
		}
		buf.append("</ul>");
		
		return buf.toString();
	}
	
	
	private String getListeDateCheque(RdbLink em, ContratDTO contratDTO,boolean compact)
	{
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
		
		CompactorTools ct = new CompactorTools(compact);
		
		// Calcul de la liste des couples (Date) (Montants des cheques) pour les compacter
		for (DatePaiementDTO dp : contratDTO.paiement.datePaiements)
		{
			if (dp.montant!=0)
			{
				String s1 = df.format(dp.datePaiement);
				String s2 = new CurrencyTextFieldConverter().convertToString(dp.montant)+" €";;
				ct.addLine(s1, s2);
			}
		}
		
		// Formatage du HTML
		List<CompactorTools.Item> res = ct.getResult();
		StringBuffer buf = new StringBuffer();		
		buf.append("<ul style=\"margin:0pt;\">");
		if (contratDTO.paiement.avoirInitial!=0)
		{
			buf.append("<li>Avoir initial : "+new CurrencyTextFieldConverter().convertToString(contratDTO.paiement.avoirInitial)+" €</li>");
		}
	
		for (CompactorTools.Item item : res)
		{
			buf.append("<li>");
			
			String str = item.part1s.size()+" chèque";
			if (item.part1s.size()>1)
			{
				str = str+"s";
			}
			str = str +" de <b>"+item.part2+"</b> débité";
			if (item.part1s.size()>1)
			{
				str = str+"s";
			}
			
			str = str +" le";
			if (item.part1s.size()>1)
			{
				str = str+"s";
			}
			str = str +" ";
			
			str = str+StringUtils.asString(item.part1s, ",");
			
			buf.append(str);
			
			buf.append("</li>");
		}
		buf.append("</ul>");
		
		return buf.toString();
	}
	
	
	

	private String getSaison(Date dateDebut, Date dateFin)
	{
		SimpleDateFormat df2 = new SimpleDateFormat("yyyy");
		String s1 = df2.format(dateDebut);
		String s2 = df2.format(dateFin);
		
		if (s1.equals(s2))
		{
			return s1;
		}
		return s1+"-"+s2;
	}
	
	
	//
	private VelocityVar v(String varName,String content)
	{
		VelocityVar var = ctx.createVar("contrat."+varName, content);
		return var;
	}
	
	
	
	// Getters and setters pour Velocity 
	
	
	

	public String getNom()
	{
		return nom;
	}

	public void setNom(String nom)
	{
		this.nom = nom;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getDateDebut()
	{
		return dateDebut;
	}

	public void setDateDebut(String dateDebut)
	{
		this.dateDebut = dateDebut;
	}

	public String getDateFin()
	{
		return dateFin;
	}

	public void setDateFin(String dateFin)
	{
		this.dateFin = dateFin;
	}

	public String getNbLivraison()
	{
		return nbLivraison;
	}

	public void setNbLivraison(String nbLivraison)
	{
		this.nbLivraison = nbLivraison;
	}

	public String getSaison()
	{
		return saison;
	}

	public void setSaison(String saison)
	{
		this.saison = saison;
	}

	public VelocityVar getLibCheque()
	{
		return libCheque;
	}

	public void setLibCheque(VelocityVar libCheque)
	{
		this.libCheque = libCheque;
	}

	public VelocityVar getNbCheque()
	{
		return nbCheque;
	}

	public void setNbCheque(VelocityVar nbCheque)
	{
		this.nbCheque = nbCheque;
	}

	public VelocityVar getTableauDateProduit()
	{
		return tableauDateProduit;
	}

	public void setTableauDateProduit(VelocityVar tableauDateProduit)
	{
		this.tableauDateProduit = tableauDateProduit;
	}


	public VelocityVar getListeDateProduit()
	{
		return listeDateProduit;
	}

	public void setListeDateProduit(VelocityVar listeDateProduit)
	{
		this.listeDateProduit = listeDateProduit;
	}

	public VelocityVar getListeDateProduitCompact()
	{
		return listeDateProduitCompact;
	}

	public void setListeDateProduitCompact(VelocityVar listeDateProduitCompact)
	{
		this.listeDateProduitCompact = listeDateProduitCompact;
	}



	public VelocityVar getMontantProduit()
	{
		return montantProduit;
	}

	public void setMontantProduit(VelocityVar montantProduit)
	{
		this.montantProduit = montantProduit;
	}

	public VelocityVar getMontantCheque()
	{
		return montantCheque;
	}

	public void setMontantCheque(VelocityVar montantCheque)
	{
		this.montantCheque = montantCheque;
	}

	public VelocityVar getMontantAvoir()
	{
		return montantAvoir;
	}

	public void setMontantAvoir(VelocityVar montantAvoir)
	{
		this.montantAvoir = montantAvoir;
	}


	public VelocityVar getAmapienNbLivraison()
	{
		return amapienNbLivraison;
	}

	public void setAmapienNbLivraison(VelocityVar amapienNbLivraison)
	{
		this.amapienNbLivraison = amapienNbLivraison;
	}

	public VelocityVar getAmapienNbProduit()
	{
		return amapienNbProduit;
	}

	public void setAmapienNbProduit(VelocityVar amapienNbProduit)
	{
		this.amapienNbProduit = amapienNbProduit;
	}

	public VelocityVar getTableauDateCheque()
	{
		return tableauDateCheque;
	}

	public void setTableauDateCheque(VelocityVar tableauDateCheque)
	{
		this.tableauDateCheque = tableauDateCheque;
	}

	public VelocityVar getListeDateCheque()
	{
		return listeDateCheque;
	}

	public void setListeDateCheque(VelocityVar listeDateCheque)
	{
		this.listeDateCheque = listeDateCheque;
	}

	public VelocityVar getListeDateChequeCompact()
	{
		return listeDateChequeCompact;
	}

	public void setListeDateChequeCompact(VelocityVar listeDateChequeCompact)
	{
		this.listeDateChequeCompact = listeDateChequeCompact;
	}

	public String getDateFinInscription()
	{
		return dateFinInscription;
	}

	public void setDateFinInscription(String dateFinInscription)
	{
		this.dateFinInscription = dateFinInscription;
	}

	public String getDateRemiseCheque()
	{
		return dateRemiseCheque;
	}

	public void setDateRemiseCheque(String dateRemiseCheque)
	{
		this.dateRemiseCheque = dateRemiseCheque;
	}

	public VelocityVar getJokerNbMax() 
	{
		return jokerNbMax;
	}

	public void setJokerNbMax(VelocityVar jokerNbMax) 
	{
		this.jokerNbMax = jokerNbMax;
	}

	public VelocityVar getJokerNbMin() 
	{
		return jokerNbMin;
	}

	public void setJokerNbMin(VelocityVar jokerNbMin) 
	{
		this.jokerNbMin = jokerNbMin;
	}

	public VelocityVar getJokerTxt() 
	{
		return jokerTxt;
	}

	public void setJokerTxt(VelocityVar jokerTxt) 
	{
		this.jokerTxt = jokerTxt;
	}

	public String getSignatureAmapien() 
	{
		return signatureAmapien;
	}

	public void setSignatureAmapien(String signatureAmapien) 
	{
		this.signatureAmapien = signatureAmapien;
	}

	public String getSignatureProducteur() 
	{
		return signatureProducteur;
	}

	public void setSignatureProducteur(String signatureProducteur) 
	{
		this.signatureProducteur = signatureProducteur;
	}


	public VelocityVar getTableauOrListeDateProduit() 
	{
		return tableauOrListeDateProduit;
	}


	public void setTableauOrListeDateProduit(VelocityVar tableauOrListeDateProduit) 
	{
		this.tableauOrListeDateProduit = tableauOrListeDateProduit;
	}


	public VelocityVar getTableauDateProduitCompact() 
	{
		return tableauDateProduitCompact;
	}

	public void setTableauDateProduitCompact(VelocityVar tableauDateProduitCompact) 
	{
		this.tableauDateProduitCompact = tableauDateProduitCompact;
	}
	
	
	
}
