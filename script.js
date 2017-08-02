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
	
	// hard-coded URL for testing
	var tempURL = "https://github.com/coderplay/h2-bitmap/blob/833860f31d50cc060434340fb6226f913da5e7f5/h2/src/main/org/h2/store/fs/FilePathNio.java";
	
	// each API call has one or more required patterns + violations mapped to it
	// this generates a single popover for the API call whose pages correspond to the different violations
	for (var apiCall in jsonData) {
		var content = "";
		
		for (var i=0; i < jsonData[apiCall].length; i++) {
			// save an instance of this API call's csID
			// this is kind of awkward but it would be more so to change the JSON message's structure
			var csID = jsonData[apiCall][i].csID;
			// the actual index of the alt pattern is off by one (e.g. jsonData[apicCall][0] should be in data-page 1, etc.)
			var iOffset = i + 1;

			// TODO: this will be pExample when that part is implemented
			var tempCodeString = '<span style="background-color: #FFFF00">try {</span> \n\r FileChannel.write(); \n\r<span style="background-color: #FFFF00">} catch (IOException e) { \n\rthrow new IllegalStateException(e); \n\r}</span>';
			//var tempCodeString = 'FileChannel.write();                       \n<span style="background-color: #FFFF00">FileChannel.close();</span>';
			
			// if this is the first page, add outer div and make it visible
			if (i == 0) {
				content += '<div class="pagination-container"><div data-page="1">';
			}
			else {
				content += '<div data-page="'+ iOffset +'" style="display:none;">';
			}
			
			// add the rest of the data-page
			// the pID is the id of the pattern in this page; we'll use it to keep track of voting
			content += '<p>' + jsonData[apiCall][i].vioMessage + '</p>'
			+ '<table><tbody><tr><td class="voteCell_'+ iOffset +'"><div class="upvote" id="' + jsonData[apiCall][i].pID +'"></div><div class="voteSpacer"></div>'
			+ '<div class="downvote" id="' + jsonData[apiCall][i].pID +'"></div></td>'
			+ '<td class="codeCell_'+ iOffset +'"><pre><code>'+ tempCodeString +'</code></pre></td></tr></tbody></table>';
			
			if (jsonData[apiCall][i].hasOwnProperty('ex1')) {
				content+= 'See this in a GitHub example: </br>'
				+ '<a class="ghLink" id="'+jsonData[apiCall][i].ex1Method+'" target="_blank" href="'+jsonData[apiCall][i].ex1+'")">Example 1</a></br>'
				+ '<a class="ghLink" id="'+jsonData[apiCall][i].ex2Method+'" target="_blank" href="'+jsonData[apiCall][i].ex2+'")">Example 2</a></br>'
				+ '<a class="ghLink" id="'+jsonData[apiCall][i].ex3Method+'" target="_blank" href="'+jsonData[apiCall][i].ex3+'")">Example 3</a>';
			}
			
			content += '</div>';
		}
		
		content += '<div class="pagination-container"><ul class="pagination pagination-centered"><li class="active" data-page="1"><a href="#" >1</a></li>';
				
		// add any subsequent pagination buttons
		if (jsonData[apiCall].length > 1) {
			// the page offset is off by two since we've already added the first page
			// add one to the length of the array for +2 offset, -1 page
			for (var j=2; j < jsonData[apiCall].length + 1; j++) {
				content += '<li data-page="'+ j +'"><a href="#" >'+ j +'</a></li>';
			}
		}
		
		content += '</ul></div></div>';
				
		doSearch(apiCall, csID, content);
	}
	
	// initialize all popovers
	$('[data-toggle="popover"]').popover({
		container: 'body'
	});
}

// find the location of the API call, highlight it, and generate a popover on it
function doSearch(_apiCall, _csID, _content) {
	"use strict";
	if (_csID != null) {
		var parentPostID = _csID.substr(0, _csID.indexOf('-')); 
		// TODO: use csIndex to find the code snippet the text is in
		var csIndex = _csID.substr(_csID.indexOf('-') + 1);
			
		// If the text is in a span of class "pln" or "typ", surround the text with a popover and highlight
		// else, the text we found is not in the SO code snippet proper or is not an exact match
		if (($('#answer-' + parentPostID).find($(".pln:contains(" + _apiCall + ")")).html() === _apiCall)
			|| ($('#answer-' + parentPostID).find($(".typ:contains(" + _apiCall + ")")).html() === _apiCall)) {
					
				var replaced = $('#answer-' + parentPostID).find($(".pln:contains(" + _apiCall + ")")).first().html().replace(_apiCall, '<a data-toggle="popover" id="popoverLink' + _csID + _apiCall + '" data-title="Potential API Misuse" data-container="body" data-html="true"><span style="background-color: #FFFF00">' + _apiCall + '</span></a>');
				$('#answer-' + parentPostID).find($(".pln:contains(" + _apiCall + ")")).first().html(replaced);
					
				if (document.getElementById('popoverLink' + _csID + _apiCall) != null) {
					document.getElementById('popoverLink' + _csID + _apiCall).setAttribute('data-content', _content);
				}		
		}
			
	}
}

// script for running the pagination
$(document.body).on('shown.bs.popover', function () {
  var paginationHandler = function(){
    // store pagination container so we only select it once
    var $paginationContainer = $(".pagination-container"),
        $pagination = $paginationContainer.find('.pagination');

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

// Close the popover on any outside click
$('html').on('click', function(e) {
    $('[data-toggle="popover"]').each(function() {
		//Check if the popover is active / contain an aria-describedby with a value
        if ($(this).attr('aria-describedby') != null ) { 
            var id = $(this).attr('aria-describedby');
			//Look if the click is a child of the popover box or if it's the button itself or a child of the button
            if (!$(e.target).closest(".popover-content").length && $(e.target).attr("aria-describedby") != id && !$(e.target).closest('[aria-describedby="'+id+'"').length) { 
                //trigger a click as if you clicked the button
				$('[aria-describedby="'+id+'"]').trigger( "click" );
            }
        }
    });
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

$(document).on( "click", ".ghLink", function () {
	chrome.storage.local.set({"methodName": this.id}, function() {});
});