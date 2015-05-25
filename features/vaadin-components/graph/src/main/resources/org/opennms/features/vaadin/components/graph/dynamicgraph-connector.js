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
    div.setAttribute('data-graph-name', this.getState().name);
    if (this.getState().start != undefined && this.getState().start != null) {
    	div.setAttribute('data-graph-start', this.getState().start);
    }
    if (this.getState().end != undefined && this.getState().end != null) {
    	div.setAttribute('data-graph-end', this.getState().end);
    }
    e.appendChild(div);

    DynamicGraph.run();
  }
}
