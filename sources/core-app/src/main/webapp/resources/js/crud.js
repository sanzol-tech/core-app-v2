/**
 * 
 */

$(document).ready(function () {
	if ($(window).width() < 800) {
		hideFilters();
	}
});

function hideFilters() {
	$('#btnFilterHide').hide();
	$('#btnFilterShow').show();
	$('#pnlFilterTop').hide(); 
	$('#pnlFilterLeft').hide(); 
}

function showFilters() {
	$('#btnFilterHide').show();
	$('#btnFilterShow').hide();
	$('#pnlFilterTop').show(); 
	$('#pnlFilterLeft').show(); 
}
