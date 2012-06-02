/*
 * Format a localized String that uses parameters.
 */
function msg(localizedString, parameter0, parameter1, parameter2) {
	if (parameter0 !== undefined && parameter0 !== null) {
		localizedString = localizedString.replace('{0}', parameter0.toString());
	}
	if (parameter1 !== undefined && parameter0 !== null) {
		localizedString = localizedString.replace('{1}', parameter1.toString());
	}
	if (parameter2 !== undefined && parameter0 !== null) {
		localizedString = localizedString.replace('{2}', parameter2.toString());
	}
	return localizedString;
}

function onClickRow($row, jsonRow) {
	$row.removeClass($row.attr("class"));
	var color = $colorer_selected_color_div.attr("class");
	$row.addClass(color);
	jsonRow.color = color;
	
	var $tableDiv = $row.parents("div.colorTableDiv");
	$tableDiv.addClass("modified");
}

function onClickColor($div) {
	$colorer_selected_color_div = $div;
	$("div#selected-color").attr("id", "");
	$colorer_selected_color_div.attr("id", "selected-color");
	var selectedColor = $div.attr("class");
	$('body').attr("style", "cursor:url(/assets/images/" + selectedColor + "-icon.png),auto;");
}

function onClickClear($div) {
	onClickColor($div);
}

function addTables(data) {
	var tableArray = data.tables;
	var $tablesDiv = $('<div><h1>' + msg(DbColorer.msg_attribute_tables, tableArray.length) + '</h1></div>');
	for(var i = 0; i < tableArray.length; i++) {
		$tablesDiv.append(addTable(tableArray[i]));
	}
	$("div.tableList").append($tablesDiv);
}

function addTable(table) {
	var $saveSpan = $('<span class="saveTable" title="' + DbColorer.msg_operations_save + '">*</span>');
	$saveSpan.on('click', function () {
	    saveJsonTable(/*$(this), */table);
	    $(this).parents("div.colorTableDiv").removeClass("modified");
    });
	
	var $tableDiv = $(
			'<div class="colorTableDiv"><h2>' + table.name + ' </h2>' +
			'<table class="colorTable"><thead>' + 
			'<tr><th>' + DbColorer.msg_attribute_name + '</th>' + 
			'<th>' + DbColorer.msg_attribute_type + '</th></tr></thead></table></div>');
	
	$tableDiv.find('h2').append($saveSpan);
	var $table = $tableDiv.find('table');
	
	var colArray = table.columns;
	for(var i = 0; i < colArray.length; i++) {
		var col = colArray[i];
		var $tr = $('<tr class="' + col.color + '"><td>' + col.name + '</td><td>' + col.type + '</td></tr>');
		$tr.on('click', (function(column) {
			return function() {
				onClickRow($(this), column);
			};
		})(col));
		$table.append($tr);
	}
	return $tableDiv;
}

function loadData(appName) {
	// Assign handlers immediately after making the request,
	// and remember the jqxhr object for this request
	var jqxhr = $.get("/data.json?appName=" + appName, function(data) {
	  addTables(data);
	  // Stored to a global:
	  colorer_data = data;
	  colorer_appName = appName;
	}, "json")
	.error(function(jqXHR, textStatus, errorThrown) { alert("error: " + textStatus); });
}

function onClickStore($div) {
	alert("Store!");
	
	var tableArray = colorer_data.tables;
	for(var i = 0; i < 10; i++) {
		var table = tableArray[i];
		saveJsonTable(table);
	}
}

function saveJsonTable(table) {
	var jqxhr = postJson("/data.json?appName=" + colorer_appName, table, function(data) {
	}, "json")
	.error(function(jqXHR, textStatus, errorThrown) { 
		alert("error: " + textStatus + " on table " + table.name); 
	});
}

/*
 * Does jQuery ajax post so that Play! will handle the request correctly.
 */
function postJson(url, jsonData, completeCallback, responseType) {
	var jsonString = JSON.stringify(jsonData, null, 2);
	return $.ajax(url,
		{
			complete: completeCallback,
			contentType: "text/json",
			processData: false,
			data: jsonString,
			type: "POST",
			dataType: responseType
		}
	);
}
