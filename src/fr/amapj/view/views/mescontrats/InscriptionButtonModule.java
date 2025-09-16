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
 package fr.amapj.view.views.mescontrats;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.service.services.mescontrats.MesContratsService;
import fr.amapj.service.services.mescontrats.small.SmallContratDTO;
import fr.amapj.service.services.mescontrats.small.inscription.InscriptionButton;
import fr.amapj.service.services.session.SessionManager;
import fr.amapj.view.engine.popup.PopupListener;
import fr.amapj.view.engine.popup.suppressionpopup.SuppressionPopup;
import fr.amapj.view.views.saisiecontrat.SaisieContrat;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.ModeSaisie;


/**
 * Permet l'affichage du bloc de boutons "S'inscrire" , "Modifier", "Supprimer", ...
 * Ce bloc est commun entre l'écran MesContratsView et VisiteAmapView
 * 
 */
public class InscriptionButtonModule 
{
	private PopupListener view;
	
	public InscriptionButtonModule(PopupListener view)
	{
		this.view = view;
		
	}
	
	public void buildBloc(HorizontalLayout hl,SmallContratDTO c)
	{			
		VerticalLayout vl2 = new VerticalLayout();
		vl2.setWidth("115px");
		vl2.setSpacing(true);	
		
		for (InscriptionButton button : c.inscriptionDTO.buttons)
		{
			switch (button)
			{
			case SINCRIRE: addButtonSinscrire(vl2,c); break;
			case MODIFIER: addButtonModifier(vl2,c); break;
			case SUPPRIMER: addButtonSupprimer(vl2,c); break;
			case JOKER : addButtonJoker(vl2,c); break;
			case VOIR : addButtonVoir(vl2,c); break;

			default: throw new AmapjRuntimeException();
			}
		}

		hl.addComponent(vl2);
		hl.setComponentAlignment(vl2, Alignment.MIDDLE_CENTER);		
	}
	
	
	/**
	 * Ce bouton peut être VERT ou JAUNE
	 */
	private void addButtonSinscrire(VerticalLayout vl2, SmallContratDTO c)
	{
		Button b = addButtonInscription("S'inscrire",c);
		if (c.inscriptionDTO.isRetardataire)
		{
			b.addStyleName("retardataire");
		}
		else
		{
			b.addStyleName("principal");
		}
		vl2.addComponent(b);
	}
	
	/**
	 * Ce bouton est toujours BLANC
	 */
	private void addButtonModifier(VerticalLayout vl2, SmallContratDTO c)
	{
		Button b = addButtonInscription("Modifier",c);
		vl2.addComponent(b);
	}
	
	/**
	 * Ce bouton est toujours BLANC
	 */
	private void addButtonSupprimer(VerticalLayout vl2, SmallContratDTO c)
	{
		Button b = addButtonSupprimer("Supprimer",c);
		vl2.addComponent(b);
	}
	
	/**
	 * Ce bouton est toujours BLANC
	 */
	private void addButtonJoker(VerticalLayout vl2, SmallContratDTO c)
	{
		Button b = addButtonJoker("Gérer jokers",c);
		vl2.addComponent(b);	
	}
	
	/**
	 * Ce bouton peut être VERT ou JAUNE
	 */
	private void addButtonVoir(VerticalLayout vl2, SmallContratDTO c)
	{
		Button b = addButtonVoir("Voir",c); 
		if (c.inscriptionDTO.isRetardataire)
		{
			b.addStyleName("retardataire");
		}
		else
		{
			b.addStyleName("principal");
		}
		vl2.addComponent(b);
	}
	

	/*
	
	
	private void buildCartePrepayee(VerticalLayout vl2,SmallContratDTO m)
	{
		if (m.contratId==null)
		{
			Button b = addButtonInscription("S'inscrire",m);
			b.addStyleName("principal");
			vl2.addComponent(b);
		}
		else
		{
			if (m.inscriptionDTO.icCartePrepayee.isModifiable)
			{
				Button b = addButtonInscription("Modifier",m);
				vl2.addComponent(b);
			}
			
			if (m.inscriptionDTO.icCartePrepayee.isSupprimable)
			{
				Button b = addButtonSupprimer("Supprimer",m);
				vl2.addComponent(b);
			}
			
			Button v = addButtonVoir("Voir",m);
			v.addStyleName("principal");
		
			vl2.addComponent(v);
		}
	}
	
	
	// ==================  OUTILS GENERIQUES

	private void buildStandard(VerticalLayout vl2,SmallContratDTO c)
	{
		if (c.contratId==null)
		{
			Button b = addButtonInscription("S'inscrire",c);
			b.addStyleName("principal");
			vl2.addComponent(b);
		}
		else
		{
			Button b = addButtonInscription("Modifier",c);
			vl2.addComponent(b);
			
			b = addButtonSupprimer("Supprimer",c);
			vl2.addComponent(b);
			
			Button v = addButtonVoir("Voir",c);
			v.addStyleName("principal");
		
			vl2.addComponent(v);
		}
	}
	
	

	
	
	private void buildRetardataire(VerticalLayout vl2,SmallContratDTO c)
	{
		if (c.contratId==null)
		{
			Button b = addButtonInscription("S'inscrire",c);
			b.addStyleName("retardataire");
			vl2.addComponent(b);
		}
		else
		{
			Button b = addButtonInscription("Modifier",c);
			vl2.addComponent(b);
			
			b = addButtonSupprimer("Supprimer",c);
			vl2.addComponent(b);
			
			Button v = addButtonVoir("Voir",c);
			v.addStyleName("retardataire");
		
			vl2.addComponent(v);
		}
	}
	
	
	private void buildJoker(VerticalLayout vl2,SmallContratDTO c)
	{
		Button b = addButtonJoker("Gérer jokers",c);
		vl2.addComponent(b);	
		
		b= addButtonVoir("Voir",c);
		b.addStyleName("principal");
	
		vl2.addComponent(b);
	}
	
	
	private void buildNone(VerticalLayout vl2,SmallContratDTO c)
	{
		if (c.contratId!=null)
		{
			Button v = addButtonVoir("Voir",c);
			v.addStyleName("principal");
		
			vl2.addComponent(v);
		}
	}
	
	*/
	
	// PARTIE TECHNIQUE 
	

	private Button addButtonInscription(String str,final SmallContratDTO c)
	{
		Button b = new Button(str);
		b.setWidth("100%");
		b.addClickListener(e ->	handleInscription(c,ModeSaisie.STANDARD));
		return b;
	}
	
	private Button addButtonVoir(String str,final SmallContratDTO c)
	{
		Button b = new Button(str);
		b.setWidth("100%");
		b.addClickListener(e -> handleInscription(c,ModeSaisie.READ_ONLY));
		return b;
	}
	
	private Button addButtonJoker(String str,final SmallContratDTO c)
	{
		Button b = new Button(str);
		b.setWidth("100%");
		b.addClickListener(e -> handleJoker(c.modeleContratId,c.contratId));
		return b;
	}
	
	
	private void handleJoker(Long modeleContratId, Long contratId)
	{
		SaisieContrat.saisieContrat(modeleContratId,contratId,SessionManager.getUserId(),null,ModeSaisie.JOKER,view);		
	}

	private Button addButtonSupprimer(String str,SmallContratDTO c)
	{
		Button b = new Button(str);
		b.setWidth("100%");
		b.addClickListener(e -> handleSupprimer(c));
		return b;
	}
	

	private void handleSupprimer(SmallContratDTO c)
	{
		String text = "Etes vous sûr de vouloir supprimer le contrat de "+c.nom+" ?";
		SuppressionPopup confirmPopup = new SuppressionPopup(text,c.contratId,e->new MesContratsService().deleteContrat(e));
		confirmPopup.open(view);		
	}
	
	

	private void handleInscription(SmallContratDTO c,ModeSaisie modeSaisie)
	{
		// Dans le cas de l'inscription à un nouveau contrat
		SaisieContrat.saisieContrat(c.modeleContratId,c.contratId,SessionManager.getUserId(),null,modeSaisie,view);
	}

}
