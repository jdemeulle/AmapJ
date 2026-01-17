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
 package fr.amapj.service.services.docengagement.signonline.avenant;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.FormatUtils;
import fr.amapj.common.velocity.VelocityTools;
import fr.amapj.common.velocity.VelocityVar;


/**
 * Genereation des avenants pour les documents d'engagement, au format html 
 *
 */
public class AvenantHtmlGenerator 
{
	private VelocityTools vt;

	private List<VelocityVar> modified1Vars;

	private Date dateModifContrat;

	public String createAvenantAsHtml(VelocityTools vt, List<VelocityVar> modified1Vars,Date dateModifContrat) 
	{
		this.vt = vt;
		this.modified1Vars = modified1Vars;
		this.dateModifContrat = dateModifContrat;
		createProcessor();
		return process();
	}
	
	
	private String process() 
	{
		// 
		StringBuilder sb = new StringBuilder();
		for (DocPart docPart : docParts) 
		{
			sb.append(process(docPart));
		}

		// Il n'est pas possible de generer un avenant de taille 0 
		if (sb.length()==0)
		{
			throw new AmapjRuntimeException();
		}
		
		// Generation du html complet 
		String top = "<!DOCTYPE html>\r\n" + 
					"<html>\r\n" + 
					"<head><meta charset=\"utf-8\">\r\n" + 
					"<title></title>\r\n" + 
					"</head>\r\n" + 
					"<body style=\"width:538pt;margin-right:28pt;margin-left:28pt;font-family:sans-serif;font-size:10pt;\">";
		
		String intro= 	"<br/>"+
						"<p style=\"text-align:center\"><span style=\"font-size:14pt\">AVENANT<strong></strong></span></p>"+
						"<br/>"+
						"<p>Ce contrat a été modifié le "+FormatUtils.getTimeStd().format(dateModifContrat)+"."+
						"Vous trouverez ci dessous les élements modifiés.</p>"+
						"<br/>";
		
		String bottom = "<br/>"+
						"<p><strong>Nota :</strong>Un élement est affiché dans l'avenant si (1) il est présent dans le document initial "+
						"et (2) sa valeur actuelle est différente de la valeur affichée dans le document signé par l'amapien."+
						"Dans l'avenant, les valeurs affichées sont les valeurs actuelles.</p>"+
						"</body>"+"</html>";
		
		String html = top+intro+vt.evaluate(sb.toString())+bottom;

		return html;
	}





	private String process(DocPart docPart) 
	{
		StringBuilder s = new StringBuilder();
		for (LinePart linePart : docPart.lines) 
		{
			s.append(process(linePart));
		}
		
		if (s.length()==0)
		{
			return "";
		}
		
		return "<p><strong>"+docPart.title+"</strong></p><br/>"+s+"<br/><br/>";
	}

	
	private String process(LinePart linePart) 
	{
		if (isModified(linePart.varName)==false)
		{
			return "";
		}
		
		// Cas particulier si pas de prefix et suffix : on utilise une balise div 
		if (linePart.prefix==null && linePart.suffix==null)
		{
			return "<div>$"+linePart.varName+"</div>";
		}
		
		
		// Sinon : on utilise une balise p 
		StringBuilder s = new StringBuilder();
		s.append("<p>");
		if (linePart.prefix!=null)
		{
			s.append(linePart.prefix);
			s.append(" ");
		}
		s.append("$"+linePart.varName);
		if (linePart.suffix!=null)
		{
			s.append(linePart.suffix);
			s.append(" ");
		}
		s.append("</p>");
		
		return s.toString();
	}


	public void createProcessor()
	{
		// 
		addTitle("Les quantités commandées");
		
		add("montantProduit","Montant total des produits : "," €");
		add("amapienNbLivraison","Nombre de livraison pour ce contrat :");
		add("amapienNbProduit","Nombre de produits pour ce contrat :");
		
		add("tableauDateProduit");
		add("tableauDateProduitCompact");
		add("listeDateProduit");
		add("listeDateProduitCompact");
		add("tableauOrListeDateProduit");
		
		//
		addTitle("Les informations de paiement");
		
		add("libCheque","Libellé des chèques : ");
		add("nbCheque","Nombre des chèques : ");
		add("montantCheque","Montant total des chèques : ");
		add("montantAvoir","Montant de l'avoir initial : ");
		
		add("tableauDateCheque");
		add("listeDateCheque");
		add("listeDateChequeCompact");

		//
		addTitle("Les reports ou jokers");
		add("jokerNbMax","Nombre de jokers max");
		add("jokerNbMin","Nombre de jokers min");
		add("jokerTxt","Les régles de gestion des jokers : ");		
	}

	
	// PARTIE TECHNIQUE 
	
	static public class DocPart
	{
		String title;
		
		List<LinePart> lines = new ArrayList<>();
	}
	
	static public class LinePart
	{
		String varName;
		
		String prefix;
		
		String suffix;
	}
	
	// Determine si cette variable a été modifiée entre le contrat initial et les données actuelles 
	private boolean isModified(String varName) 
	{
		return modified1Vars.stream().anyMatch(e->e.varName.equals(varName));
	}
	
	
	public List<DocPart> docParts = new ArrayList<>();
	
	public DocPart current;

	private void addTitle(String title) 
	{
		current = new DocPart();
		current.title = title;
		docParts.add(current);
	}
	
	private void add(String varName, String prefix, String suffix) 
	{
		LinePart l = new LinePart();
		l.varName = "contrat."+varName;
		l.prefix = prefix;
		l.suffix = suffix;
		current.lines.add(l);
	}
	
	private void add(String varName, String prefix) 
	{
		add(varName,prefix,null);		
	}

	private void add(String varName) 
	{
		add(varName,null,null);		
	}


}


