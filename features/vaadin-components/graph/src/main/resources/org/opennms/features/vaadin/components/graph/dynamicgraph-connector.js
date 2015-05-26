org_opennms_features_vaadin_components_graph_DynamicGraph = function() {
  var e = this.getElement();
  console.log("dynamicGraph - onInit()");

  this.onStateChange = function() {
    console.log("dynamicGraph - onStateChange()");

    window.onmsGraphs = {
        'baseHref': this.getState().baseHref,
        'static': this.getState().useStaticGraphs
    };

    var div = document.createElement('div');
    div.setAttribute('class', 'dynamic-graph');
    div.setAttribute('data-resource-id', this.getState().resourceId);
    div.setAttribute('data-graph-name', this.getState().graphName);
    if (this.getState().start != undefined && this.getState().start != null) {
    	div.setAttribute('data-graph-start', this.getState().start);
    }
    if (this.getState().end != undefined && this.getState().end != null) {
    	div.setAttribute('data-graph-end', this.getState().end);
    }
    if (this.getState().widthRatio != undefined && this.getState().widthRatio != null) {
    	div.setAttribute('data-width-ratio', this.getState().widthRatio);
    }
    if (this.getState().heightRatio != undefined && this.getState().heightRatio != null) {
    	div.setAttribute('data-height-ratio', this.getState().heightRatio);
    }
    if (this.getState().title != undefined && this.getState().title != null) {
    	div.setAttribute('data-graph-title', this.getState().title);
    }

    e.appendChild(div);

    DynamicGraph.run();
  }
}
