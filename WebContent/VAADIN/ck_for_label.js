// 
CKEDITOR.on( 'dialogDefinition', function( ev ) {
    var dialogName = ev.data.name;
    var dialogDefinition = ev.data.definition;
    
    // Pour une fenetre ajout de lien, par défaut le lien ouvre un nouvel onglet 
    if ( dialogName == 'link' ) 
    {
        var informationTab = dialogDefinition.getContents('target');
        var targetField = informationTab.get('linkTargetType');
        targetField['default'] = '_blank';
    }
});


/*
 * Remove &nbsp; entities which were inserted ie. when removing a space and
 * immediately inputting a space.
 *
 * NB: We could also set config.basicEntities to false, but this is stongly
 * adviced against since this also does not turn ie. < into &lt;.
 * @link http://stackoverflow.com/a/16468264/328272
 *
 * Based on StackOverflow answer.
 * @link http://stackoverflow.com/a/14549010/328272
 * 
 * Ceci pourra etre supprimé quand le bug 
 * http://dev.ckeditor.com/ticket/11415
 * sera corrigé 
 * 
 * A noter : la correction n'apparait que quand on quitte l'editeur ou quand 
 * on passe en source et on revient ,donc ce n'est pas completement bon 
 * 
 */
CKEDITOR.plugins.add('removeRedundantNBSP', {
  afterInit: function(editor) {
    var config = editor.config,
      dataProcessor = editor.dataProcessor,
      htmlFilter = dataProcessor && dataProcessor.htmlFilter;

    if (htmlFilter) {
      htmlFilter.addRules({
        text: function(text) {
          return text.replace(/(\w)&nbsp;/g, '$1 ');
        }
      }, {
        applyToAll: true,
        excludeNestedEditable: true
      });
    }
  }
});