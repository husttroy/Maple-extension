	//check if substring is the code we want
	// (later: get substring that violates API usage)
	// and highlight substring
function doSearch(text) {
	// if not IE, use window.find
	// else, use TextRange for IE
    if (window.find && window.getSelection) {
        document.designMode = "on";
        var sel = window.getSelection();
        sel.collapse(document.body, 0);

        while (window.find(text)) {
			//create link to dialog box
			//document.execCommand("createLink", false, "https://www.facebook.com/");
			document.execCommand('insertHTML', false, '<a href="#" runat="server" id="dialogLink">' + text + '</a>')
			
            //sel.collapseToEnd();
        }
		
		// get the selection back because inserting HTML closed it
		var sel = window.getSelection();
        sel.collapse(document.body, 0);
		
		while (window.find(text)) {
			//hightlight text
            document.execCommand("HiliteColor", false, "yellow");
		}
        document.designMode = "off";
    } else if (document.body.createTextRange) {
        var textRange = document.body.createTextRange();
        while (textRange.findText(text)) {
            textRange.execCommand("BackColor", false, "yellow");
            textRange.collapse(false);
        }
    }
}

// hard-coded for now: highlight violating code
var myText = "out.write(myByteBuffer);";
doSearch(myText);

$("#dialogLink").click( function() {
	$("#dialog").dialog("open");
	return false;
});