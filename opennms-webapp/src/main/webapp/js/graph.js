/**
 * Detects and populates graph <div>s.
 */

DynamicGraph = (function () {
  "use strict";

  var $j = jQuery.noConflict(); // Avoid conflicts with prototype.js used by graph/cropper/zoom.js

  var drawStaticGraph = function(el, def, dim) {
    var graphUrlParams = {
      'resourceId': def.resourceId,
      'report': def.graphName,
      'start': def.start,
      'end': def.end,
      'width': dim.width,
      'height': dim.height
    };
    var graphUrl = window.onmsGraphs.baseHref + "graph/graph.png?" + $j.param(graphUrlParams);

    var altSuffix;
    var imgTagAttrs = "";
    if (def.zooming) {
      altSuffix = ' (drag to zoom)';
      imgTagAttrs = 'id="zoomImage"';
    } else {
      altSuffix = ' (click to zoom)';
    }

    var graphDom = '<img ' + imgTagAttrs + ' class="graphImg" src="' + graphUrl + '" alt="Resource graph: ' + def.graphTitle + altSuffix + '" />';
    if (def.zoomable && !def.zooming) {
      var zoomUrlParams = {
        'zoom': true,
        'relativetime': 'custom',
        'resourceId': def.resourceId,
        'reports': def.graphName,
        'start': def.start,
        'end': def.end
      };

      var zoomUrl = window.onmsGraphs.baseHref + 'graph/results.htm?' + $j.param(zoomUrlParams);
      graphDom = '<a href="' + zoomUrl + '">' + graphDom + '</a>';
    }

    el.html(graphDom);

    if (def.zooming) {
      // There can only be a single image on the page
      var img = $j("#zoomImage");
      img.width(dim.width);
      img.height(dim.height);
    }
  };

  var drawInteractiveGraph = function(el, def, dim) {
    var text = def.graphTitle;
    // Use the dimensions if no title is set
    if (text === undefined || text === null) {
      text = dim.width + 'x' + dim.height;
    }

    el.html('<img class="graph-placeholder" data-src="holder.js/' + dim.width + 'x' + dim.height + '?text=' + def.graphTitle + '">');
  };

  var getDimensionsForElement = function(el, def) {
    var width = Math.round(el.width() * def.widthRatio);
    return {
      'width': width,
      'height': Math.round(width * def.heightRatio)
    };
  };

  var run = function () {
    var didDrawOneOrMoreInteractiveGraphs = false;

    $j(".dynamic-graph").each(function () {
      // Grab the element
      var el = $j(this);

      // Extract the attributes
      var def = {
        'resourceId': el.data("resource-id"),
        'graphName': el.data("graph-name"),
        'graphTitle': el.data("graph-title"),
        'start': el.data("graph-start"),
        'end': el.data("graph-end"),
        'zooming': el.data("graph-zooming"),
        'zoomable': el.data("graph-zoomable"),
        'widthRatio': el.data("width-ratio"),
        'heightRatio': el.data("height-ratio")
      };

      // Skip the entry when any of the required fields are missing
      if (def.resourceId === undefined || def.resourceId === null || def.resourceId === "") {
        return;
      }
      if (def.graphName === undefined || def.graphName === null || def.graphName === "") {
        return;
      }

      // Use sane defaults
      if (def.end === undefined || def.end === null) {
        def.end = new Date().getTime();
      }
      if (def.start === undefined || def.start === null) {
        def.start = def.end - (24 * 60 * 60 * 1000); // 24 hours ago.
      }
      if (def.widthRatio === undefined || def.widthRatio === null) {
        def.widthRatio = 0.8;
      }
      if (def.heightRatio === undefined || def.heightRatio === null) {
        def.heightRatio = 0.3;
      }

      // Determine the target dimensions
      var dim = getDimensionsForElement(el, def);

      // Render the appropriate graph
      var drawStaticGraphs = (window.onmsGraphs != undefined && window.onmsGraphs.static);
      if (drawStaticGraphs) {
        drawStaticGraph(el, def, dim);
      } else {
        drawInteractiveGraph(el, def, dim);
        didDrawOneOrMoreInteractiveGraphs = true;
      }

      // Notify other components (i.e cropper) that we have loaded a graph
      $j(document).trigger("graphLoaded", [dim.width, dim.height]);
    });

    if (didDrawOneOrMoreInteractiveGraphs) {
      Holder.run({images: ".graph-placeholder"});
    }
  };

  // Automatically trigger a run on load
  $j(function () {
    run();
  });

  return {
    run: run
  }
})();
