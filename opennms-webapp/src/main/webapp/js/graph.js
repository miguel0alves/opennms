/**
 * Detects and populates graph <div>s.
 */
(function () {

  var getDimensionsForElement = function(el) {
    var width = Math.round(el.width() * 0.8);
    return {
      'width': width,
      'height': Math.round(width * 0.3)
    };
  };

  var $j = jQuery.noConflict(); // Avoid conflicts with prototype.js used by graph/cropper/zoom.js
  $j(function ($) {
    // Keep track of the last width and height, since we'll send these in our graphsLoaded event
    // The values are used by the image cropper i.e. zoom functionality
    var dimensions = {'width': 0, 'height': 0};

    if (window.onmsGraphs.static) {
      $(".dynamic-graph").each(function (index) {
        var resourceId = $(this).data("resource-id");
        var graphName = $(this).data("graph-name");
        var graphTitle = $(this).data("graph-title");
        var start = $(this).data("graph-start");
        var end = $(this).data("graph-end");
        var zooming = $(this).data("graph-zooming");
        var zoomable = $(this).data("graph-zoomable");

        dimensions = getDimensionsForElement($(this));

        var graphUrlParams = {
          'resourceId': resourceId,
          'report': graphName,
          'start': start,
          'end': end,
          'width': dimensions.width,
          'height': dimensions.height
        };
        var graphUrl = "graph/graph.png?" + $.param(graphUrlParams);

        var altSuffix;
        var imgTagAttrs = "";
        if (zooming) {
          altSuffix = ' (drag to zoom)';
          imgTagAttrs = 'id="zoomImage"';
        } else {
          altSuffix = ' (click to zoom)';
        }

        var graphDom = '<img ' + imgTagAttrs + ' class="graphImg" src="' + graphUrl + '" alt="Resource graph: ' + graphTitle + altSuffix + '" />';
        if (zoomable && !zooming) {
          var zoomUrlParams = {
            'zoom': true,
            'relativetime': 'custom',
            'resourceId': resourceId,
            'reports': graphName,
            'start': start,
            'end': end
          };

          var zoomUrl = window.onmsGraphs.baseHref + 'graph/results.htm?' + $.param(zoomUrlParams);
          graphDom = '<a href="' + zoomUrl + '">' + graphDom + '</a>';
        }

        $(this).append(graphDom);

        if (zooming) {
          // There can only be a single image on the page
          var img = $("#zoomImage");
          img.width(dimensions.width);
          img.height(dimensions.height);
        }
      });
    } else {
      $(".dynamic-graph").each(function (index) {
        var resourceId = $(this).data("resource-id");
        var graphName = $(this).data("graph-name");
        var graphTitle = $(this).data("graph-title");
        var start = $(this).data("graph-start");
        var end = $(this).data("graph-end");

        dimensions = getDimensionsForElement($(this));

        $(this).append('<img class="graph-placeholder" data-src="holder.js/' + dimensions.width + 'x' + dimensions.height + '?text=' + graphTitle + '">');
      });
      Holder.run({images: ".graph-placeholder"});
    }

    $(document).trigger("graphsLoaded", [dimensions.width, dimensions.height]);
  });
})();
