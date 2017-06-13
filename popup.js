//the code example to include
var codeStringOne = '<span style="background-color: #FFFF00">try {</span> \n\r out.write(myByteBuffer); \n\r<span style="background-color: #FFFF00">} catch (IOException e) { \n\rthrow new IllegalStateException(e); \n\r}</span>';
var codeStringTwo = '<span style="background-color: #FFFF00">try {</span> \n\r out.write(myByteBuffer); \n\r<span style="background-color: #FFFF00">} catch (ClosedChannelException e) { \n\rSystem.exit(1); \n\r}</span>';

$(function() {
//the body of the dialog
var newDialog = $('<div id="dialog">'
+ '<div id="panel-one"><p>This code could be improved with exception handling:</p><pre><code>'+ codeStringOne +'</code></pre>'
+ '<p>This pattern is used by 3370 snippets in 578 GitHub repositories</p>'
+ '<a href="https://www.google.com/">See this in a GitHub example</a></div>'
+ '<div id="panel-two"><p>This code could be improved with exception handling:</p><pre><code>'+ codeStringTwo +'</code></pre>'
+ '<p>This pattern is used by 3370 snippets in 578 GitHub repositories</p>'
+ '<a href="https://www.google.com/">See this in a GitHub example</a></div>'
+ '<div id="paginationButtons"><p><ul class="pagination"><li><a class="toggle" href="#dialog" id="toggle-panel-one">1</a></li><li><a class="toggle" href="#dialog" id="toggle-panel-two">2</a></li>'
+ '<li><a class="toggle" href="#dialog" id="toggle-panel-three">3</a></li></ul></p></div>'
+ '</div>');


$(".toggle").click(function() {
    var $toggled = $(this).attr('id');
	
	$($toggled).siblings().css("display", none);
	$($toggled).css("display", "block");
	
    //$($toggled).siblings(':visible').hide("slide", {direction: 'up'}, 750);
    //$($toggled).toggle("slide", {direction: 'up'}, 750);

    return false;
});

// initializing the dialog
newDialog.dialog({
autoOpen: false,
title: "Missing Exception Handling",
modal: false
// hide X-close button
//open: function(event, ui) {
     //$(".ui-dialog-titlebar-close", ui.dialog | ui).hide();
//},
//buttons: [
 //{text: "Close", click: function() {$(this).dialog("close")}}

//]
});
});