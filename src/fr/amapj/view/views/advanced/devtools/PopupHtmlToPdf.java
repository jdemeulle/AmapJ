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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Link;
import com.vaadin.ui.TextArea;

import fr.amapj.service.engine.generator.pdf.Html2PdfConverter;
import fr.amapj.view.engine.popup.formpopup.WizardFormPopup;

/**
 * Permet de faire une conversion HTML To Pdf avec wkhtmltopdf
 * 
 *
 */
public class PopupHtmlToPdf extends WizardFormPopup
{
	
	private TextArea tf;


	/**
	 * 
	 */
	public PopupHtmlToPdf()
	{		
		setWidth(80);
		popupTitle = "Transformation HTML vers PDF";
				
	}
	
	@Override
	protected void configure()
	{
		add(()->addFieldSaisie());

	}

	

	private void addFieldSaisie()
	{	
		setStepTitle("Conversion");
				
		tf = new TextArea("Contenu html");
		tf.setWidth("90%");
		tf.setHeight(10, Unit.CM);
		tf.setImmediate(true);
		form.addComponent(tf);
		
		
		StreamResource streamResource = new StreamResource(new PdfResource(tf), "test.pdf");
		streamResource.setCacheTime(1000);
			
		Link extractFile = new Link("Télécharger le pdf",streamResource);
		extractFile.setIcon(FontAwesome.DOWNLOAD);
		extractFile.setTargetName("_blank");
		
		form.addComponent(extractFile);
	}


	@Override
	protected void performSauvegarder()
	{
	}

	
	
	
	static public class PdfResource implements StreamResource.StreamSource
	{

		private TextArea t;

		public PdfResource(TextArea t)
		{
			this.t = t;
		}

		@Override
		public InputStream getStream()
		{
			String content = t.getValue();
			
			byte[] b = new Html2PdfConverter().convertHtmlToPdf(content);
			//
			return new ByteArrayInputStream(b);
		}

	}
	
	
	
}
