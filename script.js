// setup the websocket
var socket = new WebSocket('ws://127.0.0.1:8080/');
	  
// Handle any errors that occur.
socket.onerror = function(error) {
	//alert('WebSocket Error: ' + error);
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
				"snippet":codeBlocks[i].innerText //note: textContent strips out newlines
			});
			blockIndex++;
			
			prevParentPost = parentPost;
		}
	}
	
	// send the code example to the backend for parsing and analysis
	socket.send(JSON.stringify(message.snippets));
};

// Show a disconnected message when the WebSocket is closed.
socket.onclose = function(event) {
	//alert('Disconnected from your IDE.', false);
};
	
// Handle messages sent by the backend.
socket.onmessage = function(event) {
	"use strict";
    var message = event.data;
	var jsonData = JSON.parse(message);
	
	var tempAltSuppressor = [];
	
	for (var data in jsonData) {
		// TODO: I'll need the alternative guys as well, populate popover with them
		// before calling doSearch()? Or create another method that would make this
		// popover thing more dynamic
		var toContinue = true;
		if (tempAltSuppressor.length > 0) {
			for (var i = 0; i < tempAltSuppressor.length; i++) {
				if (tempAltSuppressor[i] == jsonData[data].apiCall) {
					toContinue = false;
				}	
			}	
		}
		if (toContinue) {
			tempAltSuppressor.push(jsonData[data].apiCall);
			console.log(jsonData[data].apiCall);
			var tempCodeString = '<span style="background-color: #FFFF00">try {</span> \n\r out.write(myByteBuffer); \n\r<span style="background-color: #FFFF00">} catch (IOException e) { \n\rthrow new IllegalStateException(e); \n\r}</span>';
			var tempVioMessage = "";
		
			var content = '<div class="pagination-container"><div data-page="1"><p>' + jsonData[data].vioMessage + '</p>'
			+ '<table><tbody><tr><td class="voteCell_1"><div class="upvote" id="upvote1"></div><div class="voteSpacer"></div><div class="downvote" id="downvote1"></div></td>'
			+ '<td class="codeCell_1"><pre><code>'+ tempCodeString +'</code></pre></td></tr></tbody></table>'
			+ '<a href="https://www.google.com/">See this in a GitHub example</a></div>'
			+ '<div data-page="2" style="display:none;"><p>' + tempVioMessage + '</p>'
			+ '<table><tbody><tr><td class="voteCell_2"><div class="upvote" id="upvote2"></div><div class="voteSpacer"></div><div class="downvote" id="downvote2"></div></td>'
			+ '<td class="codeCell_2"><pre><code>'+ tempCodeString +'</code></pre></td></tr></tbody></table>'
			+ '<a href="https://www.google.com/">See this in a GitHub example</a></div>'
			+ '<div data-page="3" style="display:none;"><p>' + tempVioMessage + '</p>'
			+ '<table><tbody><tr><td class="voteCell_3"><div class="upvote" id="upvote3"></div><div class="voteSpacer"></div><div class="downvote" id="downvote3"></div></td>'
			+ '<td class="codeCell_3"><pre><code>'+ tempCodeString +'</code></pre></td></tr></tbody></table>'
			+ '<a href="https://www.google.com/">See this in a GitHub example</a></div>'
			+ '<div class="pagination-container">'
			+ '<ul class="pagination pagination-centered"><li class="active" data-page="1"><a href="#" >1</a></li>'
			+ '<li data-page="2"><a href="#" >2</a></li>'
			+ '<li data-page="3"><a href="#" >3</a></li>'
			+ '</ul></div>'
			+ '</div>';
			
			doSearch(jsonData[data].apiCall, jsonData[data].csID, content);
		}
	}
	
	// TEST
	//doSearch("close", "10506546-0", content);
	//doSearch("write", "10506546-0", content);
	//doSearch("read", "10506546-1", content);
	
	// initialize all popovers
	$('[data-toggle="popover"]').popover({
		container: 'body'
	});
};

// find the location of the API call, highlight it, and generate a popover on it
function doSearch(_apiCall, _csID, _content) {
	"use strict";
	// if not IE, use window.find
	// else, use TextRange for IE
    if (window.find && window.getSelection) {
        document.designMode = "on";
        var sel = window.getSelection();
        sel.collapse(document.body, 0);
		
		if (_csID != null) {
			var parentPostID = _csID.substr(0, _csID.indexOf('-')); 
			// TODO: use csIndex to find the code snippet the text is in
			var csIndex = _csID.substr(_csID.indexOf('-') + 1);
			
			// surround the text with a popover and highlight
			var replaced = $('#answer-' + parentPostID).find(".post-text").html().replace(_apiCall, '<a data-toggle="popover" id="popoverLink' + _csID + '" data-title="Potential API Misuse" data-container="body" data-html="true"><span style="background-color: #FFFF00">' + _apiCall + '</span></a>');
			$('#answer-' + parentPostID).find(".post-text").html(replaced);
		
			if (document.getElementById('popoverLink' + _csID) != null) {
				document.getElementById('popoverLink' + _csID).setAttribute('data-content', _content);
				console.log(document.getElementById('popoverLink' + _csID));
			}
		}
		
		// get the selection back because inserting HTML closed it
		var sel = window.getSelection();
        sel.collapse(document.body, 0);
        document.designMode = "off";
		
	// TODO: change IE code to work too
    } else if (document.body.createTextRange) {
        var textRange = document.body.createTextRange();
        while (textRange.findText(text)) {
            textRange.execCommand("BackColor", false, "yellow");
            textRange.collapse(false);
        }
    }
}

// script for running the pagination
$(document.body).on('shown.bs.popover', function () {
  //console.log("shown");
  var paginationHandler = function(){
    // store pagination container so we only select it once
    var $paginationContainer = $(".pagination-container"),
        $pagination = $paginationContainer.find('.pagination');

    //console.log($paginationContainer);
    //console.log($pagination);

    // click event
    $pagination.find("li a").on('click.pageChange',function(e){
        e.preventDefault();

        // get parent li's data-page attribute and current page
        var parentLiPage = $(this).parent('li').data("page"),
            currentPage = parseInt( $(".pagination-container div[data-page]:visible").data('page') ),
            numPages = $paginationContainer.find("div[data-page]").length;
        
		$('.active').not($($(this).parent('li'))).removeClass('active');
		$(this).parent('li').toggleClass('active');
		
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


$(document).on( "click", ".upvote", function () {
	// make a JSON message
	// should send server whether it's an up or downvote,
	// plus its id to identify which pattern it belongs to
	socket.send('{"vote":1, "id":"'+this.id+'"}');
	
	// change the color to green
	$(this).css("border-bottom", "12px solid green");
	
	// disable the button so the user can't send more than one upvote
	 this.disabled = true;
});

$(document).on( "click", ".downvote", function () {
	socket.send('{"vote":-1, "id":"'+this.id+'"}');
	$(this).css("border-top", "12px solid red");
	this.disabled = true;
});

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