	// setup the websocket
	var socket = new WebSocket('ws://127.0.0.1:8080/');
	  
	// Handle any errors that occur.
	socket.onerror = function(error) {
		alert('WebSocket Error: ' + error);
	};	
	
	// Make sure we're connected to the WebSocket before trying to send anything to the server
	socket.onopen = function(event) {
	
		// parse HTML for <code> tags and send their content + post id + generated snippet id to server
		// three classes: "question", "answer", and "answer accepted-answer"
		var codeBlocks = document.getElementsByTagName("code");
		var prevParentPost = null;
		var parentPost;
		var blockIndex;
		
		var message = {
			snippets:[]
		};
		
		// iterate through the code blocks and create JSON objects out of them
		for (var i = 0; i < codeBlocks.length; i++)
		{
			// filter out code snippets from question post; we just want snippets from answers
			parentPost = getParentPost(codeBlocks[i]);
			if (parentPost != null)
			{
				if (parentPost != prevParentPost)
				{
					blockIndex = 0;
				}
				
				message.snippets.push({
					"id":getParentPost(codeBlocks[i]).getAttribute("data-answerid") + "-" + blockIndex,
					"snippet":codeBlocks[i].innerHTML
				});
				blockIndex++;
				
				prevParentPost = parentPost;
			}
		}
		
		// send the code example to the backend for parsing and analysis
		socket.send(JSON.stringify(message));
  };

	// Show a disconnected message when the WebSocket is closed.
	socket.onclose = function(event) {
		alert('Disconnected from your IDE.', false);
	};
	
	
	// this function walks through the calling element's parents until it reaches
	// the question/answer post div in which the code resides
	function getParentPost(_child) {
        var object = _child;
        while (object.className != "question" 
		&& object.className != "answer" 
		&& object.className != "answer accepted-answer") 
		{
            object = object.parentNode;
        }

		if (object.className == "question")
		{
			object = null;
		}
        return object;
    }
	
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
			document.execCommand('insertHTML', false, '<a data-toggle="popover" id="popoverLink" data-title="Missing Exception Handling" data-container="body" data-html="true">' + text + '</a>');
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


var codeStringOne = '<span style="background-color: #FFFF00">try {</span> \n\r out.write(myByteBuffer); \n\r<span style="background-color: #FFFF00">} catch (IOException e) { \n\rthrow new IllegalStateException(e); \n\r}</span>';
var codeStringTwo = '<span style="background-color: #FFFF00">try {</span> \n\r out.write(myByteBuffer); \n\r<span style="background-color: #FFFF00">} catch (ClosedChannelException e) { \n\rSystem.exit(1); \n\r}</span>';

// add content to the popover
$("[data-toggle=popover]").popover({
content: '<div class="pagination-container"><div data-page="1"><p>This code could be improved with exception handling:</p><pre><code>'+ codeStringOne +'</code></pre>'
+ '<p>This pattern is used by 3370 snippets in 578 GitHub repositories</p>'
+ '<a href="https://www.google.com/">See this in a GitHub example</a></div>'
+ '<div data-page="2" style="display:none;"><p>This code could be improved with exception handling:</p><pre><code>'+ codeStringTwo +'</code></pre>'
+ '<p>This pattern is used by 3370 snippets in 578 GitHub repositories</p>'
+ '<a href="https://www.google.com/">See this in a GitHub example</a></div>'
+ '<div data-page="3" style="display:none;"><p>This code could be improved with exception handling:</p><pre><code>'+ codeStringOne +'</code></pre>'
+ '<p>This pattern is used by 3370 snippets in 578 GitHub repositories</p>'
+ '<a href="https://www.google.com/">See this in a GitHub example</a></div>'
+ '<div class="pagination pagination-centered">'
+ '<ul class="page_control"><li class="active" data-page="1"><a href="#" >1</a></li>'
+ '<li data-page="2"><a href="#" >2</a></li>'
+ '<li data-page="3"><a href="#" >3</a></li>'
+ '</ul></div>'
+ '</div>'
});

// script for running the pagination
$(document.body).on('shown.bs.popover', function () {
  console.log("shown");
  var paginationHandler = function(){
    // store pagination container so we only select it once
    var $paginationContainer = $(".pagination-container"),
        $pagination = $paginationContainer.find('.pagination ul');

    console.log($paginationContainer);
    console.log($pagination);

    // click event
    $pagination.find("li a").on('click.pageChange',function(e){
        e.preventDefault();

        // get parent li's data-page attribute and current page
        var parentLiPage = $(this).parent('li').data("page"),
            currentPage = parseInt( $(".pagination-container div[data-page]:visible").data('page') ),
            numPages = $paginationContainer.find("div[data-page]").length;
        
        // make sure they aren't clicking the current page
        if ( parseInt(parentLiPage) !== parseInt(currentPage) ) {
            // hide the current page
            $paginationContainer.find("div[data-page]:visible").hide();

            if ( parentLiPage === '+' ) {
                // next page
                $paginationContainer.find("div[data-page="+( currentPage+1>numPages ? numPages : currentPage+1 )+"]").show();
            } else if ( parentLiPage === '-' ) {
                // previous page
                $paginationContainer.find("div[data-page="+( currentPage-1<1 ? 1 : currentPage-1 )+"]").show();
            } else {
                // specific page
                $paginationContainer.find("div[data-page="+parseInt(parentLiPage)+"]").show();
            }

        }
    });
  };
  $( document ).ready( paginationHandler );
});
