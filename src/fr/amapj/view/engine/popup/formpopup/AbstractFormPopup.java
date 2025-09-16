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
 package fr.amapj.view.engine.popup.formpopup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.vaadin.openesignforms.ckeditor.CKEditorConfig;
import org.vaadin.openesignforms.ckeditor.CKEditorTextField;

import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ChameleonTheme;

import fr.amapj.common.GenericUtils;
import fr.amapj.common.StackUtils;
import fr.amapj.model.engine.Identifiable;
import fr.amapj.view.engine.collectioneditor.CollectionEditor;
import fr.amapj.view.engine.collectioneditor.FieldType;
import fr.amapj.view.engine.enumselector.EnumSearcher;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.formpopup.validator.IValidator;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;
import fr.amapj.view.engine.searcher.Searcher;
import fr.amapj.view.engine.searcher.SearcherDefinition;
import fr.amapj.view.engine.tools.BaseUiTools;
import fr.amapj.view.engine.tools.table.complex.ComplexTableBuilder;
import fr.amapj.view.engine.ui.AppConfiguration;

/**
 * Fonctions communes des popups portant uniquement sur la gestion de la form et du validate  
 *  
 */
abstract public class AbstractFormPopup extends CorePopup
{
	private Item item;
	
	protected FieldGroup binder;
	protected FormLayout form;
	
	protected ValidatorManager validatorManager = new ValidatorManager();
	
	
	/**
	 * Permet d'indiquer l'objet qui sera manipulé par le formulaire 
	 */
	protected void setModel(Object dto)
	{
		item = new BeanItem(dto);
	}
	
	protected Item getModelAsItem()
	{
		return item;
	}
	
	protected boolean doCommit()
	{
		try
		{
			binder.commit();
			return true;
		}
		catch (CommitException e)
		{
			List<String> msgs = new ArrayList<>();
			msgs.add("Merci de corriger les points suivants (commit exception)");
			for (Entry<Field<?>, InvalidValueException> entry : e.getInvalidFields().entrySet())
			{
				String str = "Le champ "+getCaption(entry.getKey())+" est incorrect (Détails : "+entry.getValue().getMessage()+").";
				msgs.add(str);	
			}
			
			// Si il n'y avait pas de details
			if (msgs.size()==1)
			{
				StackUtils.popStack(msgs, e);
			}
			
			MessagePopup.open(new MessagePopup("Notification", msgs));
			return false;
		}
	}




	private String getCaption(Field<?> field) 
	{
		// Cas des dates ou des ComboBox qui sont inclus dans un HorizontalLayout
		if ( (field.getParent() instanceof FormLayout)==false)
		{
			return field.getParent().getCaption();
		}
		
		// 
		return field.getCaption();
	}




	protected <T> void  addComplexTable(ComplexTableBuilder<T> ctb)
	{
		ctb.buildComponent(form);
	}
	
	
	protected Label addText(String content)
	{
		return addHtml(content);
	}
	

	protected Label addHtml(String content)
	{
		return addLabel(content, ContentMode.HTML);
	}
	
	
	private Label addLabel(String content,ContentMode mode)
	{
		Label l = new Label(content,mode);
		l.setStyleName(ChameleonTheme.LABEL_BIG);
		l.setWidth("80%");
		form.addComponent(l);
		
		return l;
	}
	

	
	protected Label addBr()
	{
		return addHtml("<br/>"); 
	}
	
	
	protected TextField addTextField(String title, Object propertyId,IValidator... validators)
	{
		TextField f = (TextField) binder.buildAndBind(title, propertyId);
		f.setId("amapj.popup."+propertyId);
		f.setNullRepresentation("");
		f.setStyleName(ChameleonTheme.TEXTFIELD_BIG);
		f.setWidth("80%");
		form.addComponent(f);	
		
		validatorManager.add(f,title,propertyId, validators);
		
		return f;
	}
	
	
	
	protected Searcher addSearcher(String title, String propertyId,SearcherDefinition iSearcher,List<? extends Identifiable> fixedValues,IValidator... validators)
	{
		Searcher prod = new Searcher(iSearcher,title,fixedValues);
		prod.bind(binder, propertyId);
		form.addComponent(prod);
		
		validatorManager.add(prod,title,propertyId, validators);
		
		return prod;
	}
	
	

	
	
	protected TextArea addTextAeraField(String title, Object propertyId)
	{
		 TextArea f = new TextArea(title);
		 f.setId("amapj.popup."+propertyId);
		 binder.bind(f, propertyId);
		
		f.setNullRepresentation("");
		f.setStyleName(ChameleonTheme.TEXTFIELD_BIG);
		f.setWidth("80%");
		form.addComponent(f);	
		
		return f;
	}
	
	protected RichTextArea addRichTextAeraField(String title, Object propertyId)
	{
		 RichTextArea f = new RichTextArea(title);
		 binder.bind(f, propertyId);
		
		f.setNullRepresentation("");
		f.setStyleName(ChameleonTheme.TEXTFIELD_BIG);
		f.setWidth("80%");
		form.addComponent(f);	
		
		return f;
	}
	

	protected <T extends Enum<T>> ComboBox addComboEnumField(String title,String propertyId,IValidator... validators)
	{		
		return addComboEnumField(title, propertyId,null, null,validators);
	}
	
	protected <T extends Enum<T>> ComboBox addComboEnumField(String title,String propertyId,T[] enumsToExcludes,IValidator... validators)
	{
		return addComboEnumField(title, propertyId,null, enumsToExcludes,validators);
	}
	
	protected <T extends Enum<T>> ComboBox addComboEnumField(String title,String propertyId,String overrideHelpText)
	{
		return addComboEnumField(title, propertyId,overrideHelpText,null);
	}
	
	protected <T extends Enum<T>> ComboBox addComboEnumField(String title,String propertyId,String overrideHelpText,T[] enumsToExcludes,IValidator... validators)
	{
		HorizontalLayout hl = EnumSearcher.createEnumSearcher(binder, title,  propertyId,overrideHelpText,enumsToExcludes);
		
		form.addComponent(hl);
		
		ComboBox box = (ComboBox) hl.getComponent(0);
		
		validatorManager.add(box,title,propertyId, validators);
		
		return box;
	}
	
	
	
	
	protected <T> ComboBox addGeneralComboField(String title, List<T> items,String propertyId,GenericUtils.ToString<T> f,IValidator... validators)
	{
		ComboBox comboBox = new ComboBox(title);
		comboBox.setImmediate(true);
		
		for (T item : items)
		{
			String caption = f==null ? item.toString() : f.toString(item);
			
			comboBox.addItem(item);
			comboBox.setItemCaption(item, caption);
		}

		binder.bind(comboBox, propertyId);
		
		form.addComponent(comboBox);
		
		validatorManager.add(comboBox,title,propertyId, validators);
		
		return comboBox;
		
	}

	
	
	protected PopupDateField addDateField(String title, String propertyId,IValidator... validators)
	{
		return addDateField(title, propertyId,null, validators);
	}
	
	protected PopupDateField addDateField(String title, String propertyId,String helpText,IValidator... validators)
	{
		PopupDateField f = BaseUiTools.createDateField(binder, propertyId,null);
		
		HorizontalLayout hl = new HorizontalLayout();
		hl.setCaption(title);
		hl.addComponent(f);
		
		if (helpText!=null)
		{
			Button aide = new Button();
			aide.setIcon(FontAwesome.QUESTION_CIRCLE);
			aide.addStyleName("borderless-colored");
			aide.addStyleName("question-mark");
			aide.addClickListener(e->new MessagePopup("Aide", ContentMode.HTML, ColorStyle.GREEN, helpText).open());
			
			hl.addComponent(aide);
		}
		form.addComponent(hl);
		
		validatorManager.add(f,title,propertyId, validators);
		
		return f;
	}
	
	protected PasswordField addPasswordTextField(String title, Object propertyId)
	{
		PasswordField f = new PasswordField(title);
		binder.bind(f, propertyId);
		// 
		f.setNullRepresentation("");
		f.setStyleName(ChameleonTheme.TEXTFIELD_BIG);
		f.setWidth("80%");
		form.addComponent(f);	
		
		return f;
	}

	
	protected TextField addIntegerField(String title, String propertyId,IValidator... validators)
	{
		TextField f = BaseUiTools.createIntegerField(title);
		binder.bind(f, propertyId);
		form.addComponent(f);
		
		validatorManager.add(f,title,propertyId, validators);
		
		return f;
	}
	
	protected TextField addIntegerField(String title, String propertyId,String helpContent,IValidator... validators)
	{
		TextField f = BaseUiTools.createIntegerField(null);
		binder.bind(f, propertyId);
		
		validatorManager.add(f,title,propertyId, validators);
		
		Button aide = new Button();
		aide.setIcon(FontAwesome.QUESTION_CIRCLE);
		aide.addStyleName("borderless-colored");
		aide.addStyleName("question-mark");
		aide.addClickListener(e->handleAide(helpContent));
		
		HorizontalLayout hl = new HorizontalLayout();
		hl.setCaption(title);
		hl.addComponent(f);
		hl.addComponent(aide);
		
		form.addComponent(hl);
		return f;
	}
	
	
	protected TextField addDoubleField(String title, String propertyId,IValidator... validators)
	{
		TextField f = BaseUiTools.createDoubleField(title);
		binder.bind(f, propertyId);
		form.addComponent(f);
		
		validatorManager.add(f,title,propertyId, validators);
		
		return f;
	}
	
	
	
	
	protected TextField addCurrencyField(String title, String propertyId,boolean allowNegativeNumber)
	{
		TextField f = BaseUiTools.createCurrencyField(title, allowNegativeNumber);
		binder.bind(f, propertyId);
		form.addComponent(f);
		return f;
	}
	
	
	/**
	 * Permet la création d'un CKEditor très basique, pour l'édition d'un texte qui sera ensuite affiché dans un label 
	 */
	protected CKEditorTextField addCKEditorFieldForLabel(String title,String propertyId)
	{
		CKEditorConfig config = new CKEditorConfig();
        config.disableElementsPath();
        config.disableSpellChecker();
        config.setWidth("100%");
        config.setFullPage(false);
        config.setAllowedContent("true");
        config.setForcePasteAsPlainText(true);
        config.setEnterMode("BR");
        config.setResizeEnabled(false);
        
        String contextPath = AppConfiguration.getConf().getContextPath();
        config.setContentsCss(contextPath+"/VAADIN/ck_for_label.css");
        config.setStylesSet("ck_for_label:"+contextPath+"/VAADIN/ck_for_label.js");
        
        config.addCustomToolbarLine("{ name: 'basicstyles', items: [ 'Bold', 'Italic', 'Underline' ] } , { name: 'styles', items : ['FontSize','TextColor','BGColor'] } ,{ name: 'links', items: [ 'Link'] }");
        
        CKEditorTextField ckEditorTextField = new CKEditorTextField(config);
        ckEditorTextField.setWidth("100%");
        ckEditorTextField.setHeight(4,Unit.CM); 
        ckEditorTextField.setCaption(title);
		
        binder.bind(ckEditorTextField, propertyId);
        form.addComponent(ckEditorTextField);
        
        return ckEditorTextField;
	}
	
	
	/**
	 * Permet la création d'un CKEditor pour l'édition d'un document (comme les contrats d'engagements)
	 */
	protected CKEditorTextField addCKEditorFieldForDocument(String propertyId)
	{
        CKEditorConfig config = new CKEditorConfig();
        config.useCompactTags();
        config.disableElementsPath();
        config.disableSpellChecker();
        config.setWidth("100%");
        config.setFullPage(true);
        config.setAllowedContent("true");
        config.setForcePasteAsPlainText(true);
        
        String contextPath = AppConfiguration.getConf().getContextPath();
        config.setStylesSet("engagement_style:"+contextPath+"/VAADIN/engagement_style.js");
        
        
        // Boutons disponibles
        config.addCustomToolbarLine("{ name: 'clipboard', items: [ 'Cut', 'Copy', 'Paste', '-', 'Undo', 'Redo' ] } , { name: 'editing', items: [ 'Find' ] },"+
       								"{ name: 'basicstyles', items: [ 'Styles' , 'Bold', 'Italic', 'Underline' ] } , { name: 'paragraph', items: [ 'CreateDiv' , 'NumberedList', 'BulletedList', '-', 'Outdent', 'Indent', '-', 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock' ] } , { name: 'insert', items: [ 'Table', 'PageBreak' ,  'HorizontalRule'] },"+
        							"{ name: 'styles', items : ['FontSize','TextColor','BGColor','Maximize', 'ShowBlocks'] } , { name: 'document', items: [ 'Source'] } ");
        
        
        config.addExtraConfig("fontSize_sizes", "'6pt;7pt;8pt;9pt;10pt;11pt;12pt;13pt;14pt;15pt;16pt;18pt;20pt;22pt;24pt;26pt;28pt;32pt;36pt'");
        config.addExtraConfig("startupOutlineBlocks", "true");
        
        config.addToExtraPlugins("removeRedundantNBSP");
        
        final CKEditorTextField ckEditorTextField = new CKEditorTextField(config);
        ckEditorTextField.setWidth("100%");
        ckEditorTextField.setHeight("16cm"); // TODO comment faire pour le calculer juste ?
		
        binder.bind(ckEditorTextField, propertyId);
        form.addComponent(ckEditorTextField);
        
        return ckEditorTextField;
	}
	
	
	// BLOC Aide
	
	protected Button addHelpButton(String title, String helpContent)
	{
		Button aide = new Button(title);
		aide.setIcon(FontAwesome.QUESTION_CIRCLE);
		aide.addStyleName("borderless-colored");
		aide.addStyleName("question-mark");
		aide.addClickListener(e->handleAide(helpContent));
		
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth(100,Unit.PERCENTAGE);
		hl.addComponent(aide);
		hl.setComponentAlignment(aide, Alignment.MIDDLE_RIGHT);
		
		form.addComponent(hl);
		
		return aide;
	}
	
	private static void handleAide(String helpContent)
	{
		MessagePopup m = new MessagePopup("Aide", ContentMode.HTML, ColorStyle.GREEN, helpContent);
		MessagePopup.open(m);
	}
	
	
	// BLOC Gestion des collections
	
	private CollectionEditor currentCollectionEditor;
	
	/**
	 * FIXME
	 * 
	 * ATTENTION : il y a un problème global : dans certains cas (quand la forme est redessinée par un clic sur un bouton par exemple) 
	 * alors ce n'est pas correct de faire un addCollectionEditorField puis des addColumnSearcher
	 * 
	 * Dans ce cas, il faut faire comme dans la classe PopupModifRoleUtilisateur ou RegleSaisieCreateOneEditorPart
	 * 
	 * En effet, le CollectionEditorField est dessiné dés l'appel à addCollectionEditorField, du coup 
	 * les colonnes ajoutées après ne sont pas visibles 
	 * 
	 * A reprendre globalement 
	 * 
	 */
	protected <T> CollectionEditor<T> addCollectionEditorField(String title, Object propertyId,Class<T> beanType,IValidator... validators)
	{
		currentCollectionEditor = new CollectionEditor<T>(title, (BeanItem) item, propertyId, beanType);
		binder.bind(currentCollectionEditor, propertyId);
		form.addComponent(currentCollectionEditor);

		validatorManager.add(currentCollectionEditor,title,propertyId, validators);
			
		
		return currentCollectionEditor;
	}
	
	protected void addColumnSearcher(String propertyId, String title, FieldType fieldType, Object defaultValue,SearcherDefinition searcher,Searcher linkedSearcher,IValidator... validators)
	{
		
		currentCollectionEditor.addSearcherColumn(propertyId, title, fieldType, defaultValue, searcher, linkedSearcher);

		validatorManager.add(currentCollectionEditor,title,propertyId, validators);
	}
	
	protected void addColumn(String propertyId, String title,FieldType fieldType,Object defaultValue,IValidator... validators)
	{
		currentCollectionEditor.addColumn(propertyId, title, fieldType, defaultValue);
		
		validatorManager.add(currentCollectionEditor,title,propertyId, validators);
	}
	
	/**
	 * CAS SPECIFIQUE 
	 */
	protected <T> void addCollectionEditorFieldToForm(CollectionEditor<T> ce,String title, Object propertyId,IValidator... validators)
	{
		currentCollectionEditor = ce;
		binder.bind(currentCollectionEditor, propertyId);
		form.addComponent(currentCollectionEditor);

		validatorManager.add(currentCollectionEditor,title,propertyId, validators);
	}
	
	/**
	 * CAS SPECIFIQUE 
	 * Permet d'ajouter uniquement le validator d'un column searcher 
	 */
	protected void addValidatorColumnSearcher(String propertyId, String title,IValidator... validators)
	{		
		validatorManager.add(currentCollectionEditor,title,propertyId, validators);
	}
	
	// Suppression d'un element de la form
	protected void suppressElement(Field<?> c)
	{
		// Cas des composants inclus dans un HorizontalLayout 
		if (c.getParent()==form)
		{
			form.removeComponent(c);
		}
		else
		{
			form.removeComponent(c.getParent());
		}
		
		// 
		binder.unbind(c);
	}
	
	
	
	
	
}
