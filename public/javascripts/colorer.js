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

function onClickRow($row, column) {
	$row.removeClass($row.attr("class"));
	var color = $colorer_selected_color_div.attr("class");
	$row.addClass("column");
	$row.addClass(color);
	column.color = color;
	
	markModified($row, column);
}

function onClickTag($tag, column) {
	var color = $colorer_selected_color_div.attr("class");
	if (color === "clear") {
		removeTagFromColumn($tag, column);
		markModified($tag.parent(), column);
		return true;
	}
	return false;
}

/*
 * Marks the table modified in the UI and data structure.
 */
function markModified($row, jsonRow) {
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

function findJsonTable($tableDiv) {
	var id = $tableDiv.attr("id");
	var tableArray = colorer_tables.tables;
	for(var i = 0; i < tableArray.length; i++) {
		if (id === tableArray[i].id) {
			return tableArray[i];
		}
	}
}

/*
 * Adds tags to the left side toolbar
 */
function addTags(tags) {
	var tagsArray = tags.tags;
	var $tagsHeading = $('div.tags-div h1');
	for(var i = 0; i < tagsArray.length; i++) {
		var tag = tagsArray[i];
		var $tag = createTagElement(tag);
		$tagsHeading.after($tag);
	}
}

function addTable(table) {
	var $saveSpan = $('<span class="saveTable" title="' + DbColorer.msg_operations_save + '">*</span>');
	$saveSpan.on('click', function () {
	    saveJsonColorTable(table);
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
		var $colDiv = $('<div id="' + col.id + '" class="column ' + col.color + '">' + col.name + ' <span class="columnType">' + col.type + '</span><div class="columnTags"></div></div>')
		var $colTags = $colDiv.find("div.columnTags");
		
		addTagsFromColumnToColDiv(col, $colTags);
		
		$colDiv.on('click', (function(column) {
			return function() {
				onClickRow($(this), column);
			};
		})(col));
		$colTags.droppable({
			   drop: (function(column) {
			     return function(event, ui) {
			    	addTagToColumn($(this), column, ui.draggable); // ui.draggable is the dragged tag
			    	markModified($(this), column);
			     }
			   })(col)
		});
		$tr.find('td').append($colDiv);
		$colDiv.after(createMenuIcon(col));
		$table.append($tr);
	}
	return $tableDiv;
}

/*
 * After a tag is dragged over a column this function adds the tag to the column.
 * 
 * @param $colDiv The column div that gets the tag.
 * @param column The column JSON that get the tag JSON.
 * @param $tag The tag span.
 */
function addTagToColumn($tagsDiv, column, $tag) {
	var tag = getTagForTagSpan($tag);
	column.tags.push(tag);
	unique(column.tags, tagCompareFunc);
	$tagsDiv.find("span.tag").remove();
	addTagsFromColumnToColDiv(column, $tagsDiv)
}

function getTagForTagSpan($tag) {
	// TODO: This really should get the tag from the global variable so 
	// that tags can contain more information.
	var tag = {
			id: $tag.html(),
			name: $tag.html()
		};
	return tag;
}

/*
 * Removes tag from data structure and UI.
 */
function removeTagFromColumn($tag, column) {
	var tag = getTagForTagSpan($tag);
	for(var i = 0; i < column.tags.length; i++) {
		var t = column.tags[i];
		if (t.id === tag.id) {
			column.tags.splice(i, 1);
			break;
		}
	}
	$tag.remove();
}

function addTagsFromColumnToColDiv(col, $tagsDiv) {
	var tagsArray = col.tags;
	for(var j = 0; j < tagsArray.length; j++) {
		var tag = tagsArray[j];
		var $tag = createTagElement(tag);
		$tagsDiv.append($tag);
		$tag.on('click', (function(column) {
			return function(event) {
				var eventDone = onClickTag($(this), column);
				if (eventDone) {
					event.stopPropagation();
				}
			};
		})(col));
	}
}

function createTagElement(tag) {
	var $tag = $('<span class="tag">' + tag.name + '</span>');
	$tag.draggable({
      stop: function(event, ui) {
    	// When drag ends, reset position back to original:
        $(this).attr("style", "position: relative;");
      }
	});
	return $tag;
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
	  colorer_tables = data;
	  colorer_appName = appName;
	}, "json")
	.error(function(jqXHR, textStatus, errorThrown) { alert("error: " + textStatus); });
}

function loadTags(appName) {
	// Assign handlers immediately after making the request,
	// and remember the jqxhr object for this request
	var jqxhr = $.get("/tags.json?appName=" + appName, function(tags) {
	  addTags(tags);
	  // Stored to a global:
	  colorer_tags = tags;
	}, "json")
	.error(function(jqXHR, textStatus, errorThrown) { alert("error: " + textStatus); });
}

function onClickStore($div) {
	var tableArray = colorer_tables.tables;
	for(var i = 0; i < tableArray.length; i++) {
		var table = tableArray[i];
		saveJsonColorTable(table);
	}
}

function saveJsonColorTable(table) {
	var jqxhr = postJson("/data.json?appName=" + colorer_appName, table, function(data) {
	}, "json")
	.error(function(jqXHR, textStatus, errorThrown) { 
		alert("error: " + textStatus + " on table " + table.name); 
	});
	
	$tableDiv = $("div#" + table.id);
    $tableDiv.removeClass("modified");
}

function switchToInLineView($imageElement) {
	$("div.colorTableDiv").each(function (i) {
		storeTablePositionToJsonTable($(this));
		$(this).draggable("destroy");
		$(this).attr("style", "");
	});
	
	$(".table-organization img").removeClass("not-visible");
	$imageElement.addClass("not-visible");
	
	// Remove plumbing:
	jsPlumb.removeAllEndpoints($("td img"));
	jsPlumb.removeAllEndpoints($("td div.column"));
}

function switchToOrganizableView($imageElement) {
	$("div.colorTableDiv").each(function (i) {
		$(this).draggable({ handle: "h2" });
		$(this).draggable({
			stop: function(event, ui) {
				storeTablePositionToJsonTable($(this));
			}
		});
		loadTablePositionToJsonTable($(this));
	});
	
	$(".table-organization img").removeClass("not-visible");
	$imageElement.addClass("not-visible");
	
	// Make columns plumbable:
	var source = $("td img");
	jsPlumb.makeSource(source, {container: $("div.tableList"), scope: "foo"});
	var target = $("td div.column");
	jsPlumb.makeTarget(target, {container: $("div.tableList"), scope: "foo"});
}

function storeTablePositionToJsonTable($tableDiv) {
	var table = findJsonTable($tableDiv);
	table.style = $tableDiv.attr("style");
}

function loadTablePositionToJsonTable($tableDiv) {
	var table = findJsonTable($tableDiv);
	if (table.style !== undefined && table.style !== null)
		$tableDiv.attr("style", table.style);
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

function tagCompareFunc(t1, t2) {
  return stringCompareFunc(t1.id, t2.id);
}