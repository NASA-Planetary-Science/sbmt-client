/*
 * Image preview script 
 * powered by jQuery (http://www.jquery.com)
 * 
 * written by Alen Grakalic (http://cssglobe.com)
 * modified by Philip Twu to scale image and dynamically change preview location
 * 
 * for more info visit http://cssglobe.com/post/1695/easiest-tooltip-and-image-preview-using-jquery
 *
 */
 
this.imagePreview = function(){	
	/* CONFIG */
		
		verticalOffset = 10;
		horizontalOffset = 10; // used to be 30 in original implementation
		
		// these 2 variable determine popup's distance from the cursor
		// you might want to adjust to get the right result
		
	/* END CONFIG */
	$("a.preview").hover(function(e){
	
		// Determine max dimensions of preview
		var maxHeight = window.innerHeight/2 - 2*verticalOffset;
		var maxWidth = window.innerWidth/2 - 2*horizontalOffset;

		this.t = this.title;
		this.title = "";	
		var c = (this.t != "") ? "<br/>" + this.t : "";	
		var imgHref = this.href;
		
		// Determine preview image dimensions
		var img = new Image();
		img.onload = function()
		{
			var scaleFactor = 1;
			if(this.height > maxHeight)
			{
				scaleFactor = maxHeight/this.height;
			}
			if(this.width > maxWidth)
			{
				scaleFactor = Math.min(scaleFactor, maxWidth/this.width);
			}
	
			// Append preview element to body
			$("#preview").remove();
			$("body").append("<p id='preview'>" + 
				"<img src='"+ imgHref + "' " +
				"height='" + scaleFactor*this.height + "px' " +
				"alt='Image preview' " + 
				"/>"+ c + "</p>");

			// Determine where to show preview based on where mouse is in window
			if(e.clientX < window.innerWidth/2)
			{
				if(e.clientY < window.innerHeight/2)
				{
					// Mouse on top left
					$("#preview")
						.css("top",(e.pageY + verticalOffset) + "px")
						.css("bottom","auto")
						.css("left",(e.pageX + horizontalOffset) + "px")
						.css("right","auto")
						.fadeIn("fast");						
				}
				else
				{
					// Mouse on bottom left
					$("#preview")
						.css("top","auto")
						.css("bottom",(window.innerHeight - e.pageY + verticalOffset) + "px")
						.css("left",(e.pageX + horizontalOffset) + "px")
						.css("right","auto")
						.fadeIn("fast");						
				}
			}
			else
			{
				if(e.clientY < window.innerHeight/2)
				{
					// Mouse on top right
					$("#preview")
						.css("top",(e.pageY + verticalOffset) + "px")
						.css("bottom","auto")
						.css("left","auto")
						.css("right",(window.innerWidth - e.pageX - horizontalOffset) + "px")
						.fadeIn("fast");						
				}
				else
				{
					// Mouse on bottom right
					$("#preview")
						.css("top","auto")
						.css("bottom",(window.innerHeight - e.pageY + verticalOffset) + "px")
						.css("left","auto")
						.css("right",(window.innerWidth - e.pageX - horizontalOffset) + "px")
						.fadeIn("fast");						
				}
			}				
		}
		img.src = this.href;
    },
	function(){
		this.title = this.t;	
		$("#preview").remove();
    });	
	$("a.preview").mousemove(function(e){
		// Determine where to show preview based on where mouse is in window
		if(e.clientX < window.innerWidth/2)
		{
			if(e.clientY < window.innerHeight/2)
			{
				// Mouse on top left
				$("#preview")
					.css("top",(e.pageY + verticalOffset) + "px")
					.css("bottom","auto")
					.css("left",(e.pageX + horizontalOffset) + "px")
					.css("right","auto")
					.fadeIn("fast");						
			}
			else
			{
				// Mouse on bottom left
				$("#preview")
					.css("top","auto")
					.css("bottom",(window.innerHeight - e.pageY + verticalOffset) + "px")
					.css("left",(e.pageX + horizontalOffset) + "px")
					.css("right","auto")
					.fadeIn("fast");						
			}
		}
		else
		{
			if(e.clientY < window.innerHeight/2)
			{
				// Mouse on top right
				$("#preview")
					.css("top",(e.pageY + verticalOffset) + "px")
					.css("bottom","auto")
					.css("left","auto")
					.css("right",(window.innerWidth - e.pageX - horizontalOffset) + "px")
					.fadeIn("fast");						
			}
			else
			{
				// Mouse on bottom right
				$("#preview")
					.css("top","auto")
					.css("bottom",(window.innerHeight - e.pageY + verticalOffset) + "px")
					.css("left","auto")
					.css("right",(window.innerWidth - e.pageX - horizontalOffset) + "px")
					.fadeIn("fast");						
			}
		}
	});
};


// starting the script on page load
$(document).ready(function(){
	imagePreview();
});