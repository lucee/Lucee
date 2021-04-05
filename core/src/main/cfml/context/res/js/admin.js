/* init functions */
$(function(){
	$('#resizewin').click(resizelayout);

	initTooltips();

	$('table.checkboxtbl').each(function(){
		var btns = $('tfoot input.submit,tfoot input.reset', this);
		/* when admin sync is active, an extra table is added*/
		if (btns.length==0)
		{
			btns = $(this).next('h4.rsync').next('table.rsync').find('tfoot input.submit,tfoot input.reset');

		}
		var cbs = $('tbody input.checkbox', this);
		if (btns.length && cbs.length)
		{
			enableBtnsWhenChecked(btns, cbs);
		}
	});
	
	$('#selectAll').on('click',function(){
		$(this).parents('table:first').find('tbody tr td:first-child input:checkbox').prop('checked',$(this).prop('checked'))
		.filter(':first').triggerHandler('change');
		if($(this).prop('checked')){
			$('.enablebutton').removeAttr('disabled').css('opacity',1)
		}else{
			$('.enablebutton').attr('disabled',true).css('opacity',0.5)
		}
	})

	scrollToEl('div.error,div.warning:not(.nofocus)');
});

function scrollToEl(selector)
{
	var intoview = $(selector).eq(0).css('opacity',.1);
	if (intoview.length)
	{
		$('html,body').animate({scrollTop: intoview.offset().top-50}, 500, function(){ intoview.animate({opacity:1}, 1000) });
	}
}


/* older functions */
function initMenu() {
	// $('#menu ul').show();
	$('#menu > li > a').click(
		function(e) {
			var $this = $(this);
			var ul = $this.next();
			if (ul.length)
			{
				ul.slideToggle('normal');
				var li = $this.parent();
				li.toggleClass('collapsed');
				var id = li.prop('id');
				var collapsed = li.hasClass('collapsed');
				$.get('?action=internal.savedata&action2=setdata&key=collapsed_'+id+'&data='+collapsed);
				e=e||event;
				e.preventDefault();
			}
		}
	);
}

$("#clickCheckbox").change(function() {
	set = $("#clickCheckbox")[0].checked;
	if(set == true) {
		$(".enablebutton").attr({disabled:false,style:"opacity:1"});
	}
	else if(set == false) {
		$(".enablebutton").attr({disabled:true,style:"opacity:0.5"});
	}
});
$("#clickCancel").click(function() {
	$(".enablebutton").attr({disabled:true,style:"opacity:0.5"});
});

function initMenu2() {
	$('#menu ul').hide();
	$('#menu ul:first').show();
	$('#menu li a').click(
	function() {
		var checkElement = $(this).next();
		if((checkElement.is('ul')) && (checkElement.is(':visible'))) {
		return false;
		}
		if((checkElement.is('ul')) && (!checkElement.is(':visible'))) {
		$('#menu ul:visible').slideUp('normal');
		checkElement.slideDown('normal');
		return false;
		}
		}
	);
}

var disableBlockUI=false;
// {form:_form,name:_input.name,value:v,error:err.error};
function customError(errors){
	if(!errors || errors.length==0) return;
	var err;
	var form=errors[0].form;
	var el;
	var clazz;
	var input;

	// remove error from last round
	try{
		for(var i=0;i<form.elements.length;i++){
			input=form.elements[i];
			el=$(input);
			clazz=el.attr("class");
			if(clazz && clazz=="InputError") {
				el.removeClass();
				el=$("#msg_"+input.name);
				el.remove();
			}
		}
	}
	catch(err){
		alert(err)
	}

	// create new error
	document.getElementById("ioMapOut").innerHTML ='';
	for(var i=0;i<errors.length;i++){
		err=errors[i];
		var input=form[err.name];
		var _input=$(input);
		if(i==0) _input.focus();
		_input.addClass("InputError");
		_input.after('<span id="msg_'+err.name+'" class="commentError"><br/>'+err.error+'</span>');
	}
	disableBlockUI=true;
}

function inputMapping() {
	var input = document.getElementById("ioMapping").value;
	if(input.length == 1 || input=='/') {
		document.getElementById("ioMapping").focus();
		document.getElementById("ioMapping").style.backgroundColor = "#ffeeee";
		document.getElementById("ioMapOut").innerHTML =' <br>'+"Virtual value not valid";
		disableBlockUI=true;
		return false;
	}

}

function createWaitBlockUI(msg)
{
	var _blockUI=function() {
		if(!disableBlockUI)
		{
			$.blockUI({
				message:msg,
				css: {
					border: 'none',
					padding: '15px',
					backgroundColor: '#000',
					'-webkit-border-radius': '10px',
					'-moz-border-radius': '10px',
					opacity: .5,
					color: '#fff' ,
					fontSize : "18pt"
				},
				fadeIn: 0,
				fadeOut: 0
			});
		}
	}
	return _blockUI;
}

/* form helpers */
function selectAll(field)
{
	$(field).parents('table:first').find('tbody tr td:first-child input:checkbox').prop('checked', field.checked)
		.filter(':first').triggerHandler('change');
}

function checkTheBox(field) {
	var apendix=field.name.split('_')[1];
	var box = $(field.form['row_'+apendix]);
	if (box.filter(':checked').length==0)
	{
		// calls the click handlers as well
		$(box).click();
	}
}

function enableBtnsWhenChecked(btns, checkboxes)
{
	checkboxes.change(function(){
		var chkd = checkboxes.filter(':checked').length > 0;
		btns.prop('disabled', chkd ? '':'disabled').css('opacity', (chkd ? 1:.5));
	})
		.filter(':first').triggerHandler('change');
}


/* tooltips */
function createTooltip(element, text, x, y, mouseAction )
{
	element.bind(mouseAction, function (event) {
		// remove title from element, so we won't see the default tooltip as well
		element.data('title', element.prop('title')).prop('title', '');
		// detect max x position
		containerRight = $('#layout').offset().left + $('#layout').width() - 20;
		// if you remove() an element it is deleted from the DOM, but the element.tooltip var stays where it is.
		// When an tooltip has been shown before, just re-add the tooltip DOM element.
		// If the tooltip is never created before, create it and add it to the DOM
		if (typeof element.tooltip.data == 'undefined') {
			element.tooltip = $('<div class="tooltip tooltip_'+mouseAction+'">'+ text +'<div class="sprite arrow"></div></div>').data('parent', element);
			$('body').append( element.tooltip );
		} else if (typeof element.tooltip == 'object') {
			$('body').append( element.tooltip );
			element.tooltip.removeClass('stayput');
		}
		// reference to the parent
		element.tooltip.data('parent', element);

		// Recalculate the position every time the tooltip is added to a page.
		// This is needed due to the clicked/hovered elements keep changing position when rows ar folded and unfolded
		if (x == 0) {
			var elWidth = element.width();
			if (elWidth > 40) {
				var xPos = element.offset().left;
			} else {
				var xPos = element.offset().left - 20 + (elWidth / 2);
			}
		} else {
			var xPos = x;
		}
		if (y == 0) {
			var yPos = element.offset().top - element.tooltip.outerHeight() - 4;
		} else {
			var yPos = y - 4;
		}
		// if rightside is out of the sitecontainer, shift it left
		var outerRight = xPos + element.tooltip.width();

		if (outerRight > containerRight)
		{
			oldXPos = xPos;
			xPos = 	containerRight - $(element.tooltip).width();
			offset = oldXPos - xPos + 20;
			$(element.tooltip).find('.arrow').css({
				left: offset
			});
		}
		// Set the tooltip position
		$(element.tooltip).css({
			left : xPos,
			top: yPos
		});
		if (mouseAction == 'mouseover') {
			$(this)
				.mouseout(function(){
					var tt = element.tooltip;
					if (!tt.hasClass('stayput'))
					{
						tt.remove();
						// re-add title to element
						element.prop('title', element.data('title'));
					}
				})
				.click(function(e){ $(element.tooltip).toggleClass('stayput'); e.stopPropagation(); });
		} else if (mouseAction == 'click') {
			var overlay = $('<div class="removeClickOverlay"></div>');
			$('body').prepend(overlay);
			$(overlay).click(function(){ $(element.tooltip).remove(); $(overlay).remove(); });
		}
		return false;
	})
}

disableBlockUI=false;
function validatePass() {
	var passDefault = document.getElementById("passEmpty").value;
	if(passDefault.length == 0) {
		$("#passEmpty").focus();
		$("#passEmpty").style.backgroundColor = "#ffeeee";
		$("#messagePass").innerHTML = "This field not to be empty";
		$("#messagePass").style.color = "#bf4f36";
		disableBlockUI=true;
		return false;
	}
}

function initTooltips()
{
	// lookup all elements with a class "tooltipMe" and add a tooltip to them.
	// Use the title attribute when available over the alt atribute
	// images most likely will only have alt
	$('.tooltipMe,abbr').each(function(){
		var $this = $(this);
		var tooltipText = '';
		var title = $this.prop('title');
		var alt = $this.prop('alt');
		if (typeof title !== 'undefined' && title !== false && title !== '') {
			tooltipText = title;
		} else if (typeof alt !== 'undefined' && alt !== false && alt !== '') {
			tooltipText = alt;
		}
		if (tooltipText !== '')
		{
			createTooltip( $this, tooltipText, 0, 0, 'mouseover');
		}
	});
	$('a.btn-mini').each(function(){
		var $this = $(this);
		var tooltipText = $this.find('span').html();
		if (tooltipText !== '')
		{
			createTooltip($this, tooltipText, 0, 0, 'mouseover');
		}
	});

	/*$('table.maintbl div.comment:not(.inline)').each(function(){
		var $this = $(this).addClass('helptextimage').removeClass('comment');
		var parent = $this.parent('td');
		if (parent.length && parent.prev('th').length)
		{
			parent.prev().append($this);
		}
		var html = $this.html();
		$this.html('<div class="inner">' + html + "</div>");
		createTooltip($this, html, 0, 0, 'mouseover');
	});*/
	$('body').on('click', function(){
		$('div.tooltip.stayput').removeClass('stayput').each(function(){ $(this).data('parent').triggerHandler('mouseout') });
	});
}

function resizelayout(e)
{
	var isfull = $('body').hasClass('full') == 0;
	$('body').toggleClass('full');
	e.preventDefault();
	var contentwidth = parseInt($('#innercontent').width(), 10);
	$.get('?action=internal.savedata&action2=setdata&key=fullscreen&data='+isfull);
	$.get('?action=internal.savedata&action2=setdata&key=contentwidth&data='+contentwidth);
	return false;
};
