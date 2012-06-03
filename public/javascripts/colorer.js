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
	$row.addClass("column");
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
	    saveJsonTable(table);
	    $(this).parents("div.colorTableDiv").removeClass("modified");
    });
	
	var $tableDiv = $(
			'<div id="' + table.id + '" class="colorTableDiv"><A name="' + table.id + '"><h2>' + table.name + ' </h2></a>' +
			'<table class="colorTable"></table></div>');
	
	$tableDiv.find('h2').append($saveSpan);
	var $table = $tableDiv.find('table');
	
	var colArray = table.columns;
	for(var i = 0; i < colArray.length; i++) {
		var col = colArray[i];
		var $tr = $('<tr><td></td></tr>');
		var $colDiv = $('<div id="' + col.id + '" class="column ' + col.color + '">' + col.name + ' <span class="columnType">' + col.type + '</span></div>')
		$colDiv.append(createMenuIcon(col));
		
		addTagsFromColumnToColDiv(col, $colDiv);
		
		$colDiv.on('click', (function(column) {
			return function() {
				onClickRow($(this), column);
			};
		})(col));
		$colDiv.droppable({
			   drop: (function(column) {
			     return function(event, ui) {
			    	addTagToColumn($(this), column, ui.draggable); // ui.draggable is the dragged tag
			    	// Reset the draggable back to it's place:
			    	ui.draggable.attr("style", "position: relative;");
			     }
			   })(col)
		});
		$tr.find('td').append($colDiv);
		$table.append($tr);
	}
	return $tableDiv;
}

function addTagToColumn($colDiv, column, $tag) {
	column.tags.push($tag.html());
	unique(column.tags, stringCompareFunc);
	$colDiv.find("span.columnTag").remove();
	addTagsFromColumnToColDiv(column, $colDiv)
}

function addTagsFromColumnToColDiv(col, $colDiv) {
	var tagsArray = col.tags;
	for(var j = 0; j < tagsArray.length; j++) {
		var tag = tagsArray[j];
		addTagSpanToColumnDiv(tag/*.name()*/, $colDiv);
	}
}

function addTagSpanToColumnDiv(tagName, $colDiv) {
	 console.log("4");
	var $tag = $('<span class="columnTag">' + tagName + '</span>');
	$tag.draggable();
	$colDiv.append($tag);
}

/*
 * Creates an icon that opens the column menu.
 * 
 * @param col The column JSON object.
 * @returns A JQuery img tag.
 */
function createMenuIcon(col) {
	var $icon = $('<img class="icon" src="/assets/images/ratas.png">');
	$icon.on('click', (function(column) {
		return function() {
			showMenu($(this), column);
		};
	})(col));
	return $icon;
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

/* 
 * Array unique function from StackOverflow.
 * Provide your own comparison
 */
function unique(a, compareFunc){
    a.sort( compareFunc );
    for(var i = 1; i < a.length; ){
        if( compareFunc(a[i-1], a[i]) === 0){
            a.splice(i, 1);
        } else {
            i++;
        }
    }
    return a;
}

function stringCompareFunc(s1, s2) {
  if (s1 === s2) return 0;
  return (s1 < s2) ? 1 : -1;
}