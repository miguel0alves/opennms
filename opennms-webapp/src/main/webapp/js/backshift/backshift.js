/**
 * Created by jwhite on 6/1/14.
 */

// Addresses an issue with PhantomJS - see https://github.com/ariya/phantomjs/issues/10522
if (!Function.prototype.bind) {
    Function.prototype.bind = function (oThis) {
        if (typeof this !== "function") {
            // closest thing possible to the ECMAScript 5
            // internal IsCallable function
            throw new TypeError("Function.prototype.bind - what is trying to be bound is not callable");
        }

        var aArgs = Array.prototype.slice.call(arguments, 1),
            fToBind = this,
            fNOP = function () {},
            fBound = function () {
                return fToBind.apply(this instanceof fNOP && oThis
                        ? this
                        : oThis,
                    aArgs.concat(Array.prototype.slice.call(arguments)));
            };

        fNOP.prototype = this.prototype;
        fBound.prototype = new fNOP();

        return fBound;
    };
}

/* jshint -W079 */

var Backshift = Backshift || {};

Backshift.namespace = function (ns_string) {
    var parts = ns_string.split('.'),
        parent = Backshift,
        i;

    // split redundant leading global
    if (parts[0] === 'Backshift') {
        parts = parts.slice(1);
    }

    for (i = 0; i < parts.length; i += 1) {
        // create a property if it doesn't exist
        if (typeof parent[parts[i]] === "undefined") {
            parent[parts[i]] = {};
        }
        parent = parent[parts[i]];
    }

    return parent;
};

Backshift.keys = function(obj) {
    var keys = [];
    for (var key in obj) {
        if (obj.hasOwnProperty(key)) {
            keys.push(key);
        }
    }
    return keys;
};

Backshift.extend = function(destination, source) {
    for (var property in source) {
        if (source.hasOwnProperty(property)) {
            destination[property] = source[property];
        }
    }
    return destination;
};

Backshift.rows = function(matrix) {
    var ncols = matrix.length;
    if (ncols === 0) {
        return [];
    }
    var nrows = matrix[0].length;

    var rows = new Array(nrows);
    for (var i = 0; i < nrows; i++) {
        var row = new Array(ncols);
        for (var j = 0; j < ncols; j++) {
            row[j] = matrix[j][i];
        }
        rows[i] = row;
    }

    return rows;
};

Backshift.fail = function(msg) {
    console.log("Error: " + msg);
    throw {
        name: "Error",
        message: msg
    };
};

Backshift.clone = function(obj) {
    return JSON.parse(JSON.stringify(obj));
};

/* Adapted from https://github.com/Jakobo/PTClass */

/*
 Copyright (c) 2005-2010 Sam Stephenson

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
/* Based on Alex Arnell's inheritance implementation. */
/** section: Language
 * class Class
 *
 *  Manages Prototype's class-based OOP system.
 *
 *  Refer to Prototype's web site for a [tutorial on classes and
 *  inheritance](http://prototypejs.org/learn/class-inheritance).
 **/
(function(globalContext) {
    /* ------------------------------------ */
    /* Import from object.js                */
    /* ------------------------------------ */
    var _toString = Object.prototype.toString,
        NULL_TYPE = 'Null',
        UNDEFINED_TYPE = 'Undefined',
        BOOLEAN_TYPE = 'Boolean',
        NUMBER_TYPE = 'Number',
        STRING_TYPE = 'String',
        OBJECT_TYPE = 'Object',
        FUNCTION_CLASS = '[object Function]';
    function isFunction(object) {
        return _toString.call(object) === FUNCTION_CLASS;
    }
    function extend(destination, source) {
        for (var property in source) if (source.hasOwnProperty(property)) // modify protect primitive slaughter
            destination[property] = source[property];
        return destination;
    }
    function keys(object) {
        if (Type(object) !== OBJECT_TYPE) { throw new TypeError(); }
        var results = [];
        for (var property in object) {
            if (object.hasOwnProperty(property)) {
                results.push(property);
            }
        }
        return results;
    }
    function Type(o) {
        switch(o) {
            case null: return NULL_TYPE;
            case (void 0): return UNDEFINED_TYPE;
        }
        var type = typeof o;
        switch(type) {
            case 'boolean': return BOOLEAN_TYPE;
            case 'number':  return NUMBER_TYPE;
            case 'string':  return STRING_TYPE;
        }
        return OBJECT_TYPE;
    }
    function isUndefined(object) {
        return typeof object === "undefined";
    }
    /* ------------------------------------ */
    /* Import from Function.js              */
    /* ------------------------------------ */
    var slice = Array.prototype.slice;
    function argumentNames(fn) {
        var names = fn.toString().match(/^[\s\(]*function[^(]*\(([^)]*)\)/)[1]
            .replace(/\/\/.*?[\r\n]|\/\*(?:.|[\r\n])*?\*\//g, '')
            .replace(/\s+/g, '').split(',');
        return names.length == 1 && !names[0] ? [] : names;
    }
    function wrap(fn, wrapper) {
        var __method = fn;
        return function() {
            var a = update([bind(__method, this)], arguments);
            return wrapper.apply(this, a);
        }
    }
    function update(array, args) {
        var arrayLength = array.length, length = args.length;
        while (length--) array[arrayLength + length] = args[length];
        return array;
    }
    function merge(array, args) {
        array = slice.call(array, 0);
        return update(array, args);
    }
    function bind(fn, context) {
        if (arguments.length < 2 && isUndefined(arguments[0])) return this;
        var __method = fn, args = slice.call(arguments, 2);
        return function() {
            var a = merge(args, arguments);
            return __method.apply(context, a);
        }
    }

    /* ------------------------------------ */
    /* Import from Prototype.js             */
    /* ------------------------------------ */
    var emptyFunction = function(){};

    var Class = (function() {

        // Some versions of JScript fail to enumerate over properties, names of which
        // correspond to non-enumerable properties in the prototype chain
        var IS_DONTENUM_BUGGY = (function(){
            for (var p in { toString: 1 }) {
                // check actual property name, so that it works with augmented Object.prototype
                if (p === 'toString') return false;
            }
            return true;
        })();

        function subclass() {};
        function create() {
            var parent = null, properties = [].slice.apply(arguments);
            if (isFunction(properties[0]))
                parent = properties.shift();

            function klass() {
                this.initialize.apply(this, arguments);
            }

            extend(klass, Class.Methods);
            klass.superclass = parent;
            klass.subclasses = [];

            if (parent) {
                subclass.prototype = parent.prototype;
                klass.prototype = new subclass;
                try { parent.subclasses.push(klass) } catch(e) {}
            }

            for (var i = 0, length = properties.length; i < length; i++)
                klass.addMethods(properties[i]);

            if (!klass.prototype.initialize)
                klass.prototype.initialize = emptyFunction;

            klass.prototype.constructor = klass;
            return klass;
        }

        function addMethods(source) {
            var ancestor   = this.superclass && this.superclass.prototype,
                properties = keys(source);

            // IE6 doesn't enumerate `toString` and `valueOf` (among other built-in `Object.prototype`) properties,
            // Force copy if they're not Object.prototype ones.
            // Do not copy other Object.prototype.* for performance reasons
            if (IS_DONTENUM_BUGGY) {
                if (source.toString != Object.prototype.toString)
                    properties.push("toString");
                if (source.valueOf != Object.prototype.valueOf)
                    properties.push("valueOf");
            }

            for (var i = 0, length = properties.length; i < length; i++) {
                var property = properties[i], value = source[property];
                if (ancestor && isFunction(value) &&
                    argumentNames(value)[0] == "$super") {
                    var method = value;
                    value = wrap((function(m) {
                        return function() { return ancestor[m].apply(this, arguments); };
                    })(property), method);

                    value.valueOf = bind(method.valueOf, method);
                    value.toString = bind(method.toString, method);
                }
                this.prototype[property] = value;
            }

            return this;
        }

        return {
            create: create,
            Methods: {
                addMethods: addMethods
            }
        };
    })();

    if (globalContext.exports) {
        globalContext.exports.Class = Class;
    }
    else {
        globalContext.Class = Class;
    }
})(Backshift);
/**
 * Created by jwhite on 5/23/14.
 */

Backshift.namespace('Backshift.Class.Configurable');

Backshift.Class.Configurable = Backshift.Class.create( {
    configure: function(args) {
        args = args || {};

        Backshift.keys(this.defaults()).forEach( function(key) {

            if (!args.hasOwnProperty(key)) {
                this[key] = this[key] || this.defaults()[key];
                return;
            }

            if (typeof this.defaults()[key] == 'object') {

                Backshift.keys(this.defaults()[key]).forEach( function(k) {

                    this[key][k] =
                            args[key][k] !== undefined ? args[key][k] :
                            this[key][k] !== undefined ? this[key][k] :
                        this.defaults()[key][k];
                }, this );

            } else {
                this[key] =
                        args[key] !== undefined ? args[key] :
                        this[key] !== undefined ? this[key] :
                    this.defaults()[key];
            }

        }, this );
    }
});

/**
 * Created by jwhite on 5/25/14.
 */

Backshift.namespace('Backshift.Math');

Backshift.Math = {};

/**
 * Compute the least common multiple of an integer array.
 *
 * Shamelessly stolen from http://rosettacode.org/wiki/Least_common_multiple#JavaScript
 *
 * @param A an array of integers
 * @returns {number} the lcm
 */
Backshift.Math.lcm = function(A) {
    if (A === undefined || A.length < 1) {
        return 0;
    }
    var n = A.length, a = Math.abs(A[0]);
    for (var i = 1; i < n; i++)
    { var b = Math.abs(A[i]), c = a;
        while (a && b){ a > b ? a %= b : b %= a; }
        a = Math.abs(c*A[i])/(a+b);
    }
    return a;
};

/**
 * Created by jwhite on 6/2/14.
 */

Backshift.namespace('Backshift.Stats');

Backshift.Stats = {};

Backshift.Stats.Maximum = function (array) {
    var k, max = NaN;
    for (k = array.length; k--;) {
        if (!isNaN(array[k])) {
            if (isNaN(max)) {
                max = array[k];
            } else if (array[k] > max) {
                max = array[k];
            }
        }
    }
    return max;
};

Backshift.Stats.Minimum = function (array) {
    var k, min = NaN;
    for (k = array.length; k--;) {
        if (!isNaN(array[k])) {
            if (isNaN(min)) {
                min = array[k];
            } else if (array[k] < min) {
                min = array[k];
            }
        }
    }
    return min;
};

/**
 * @param array
 * @returns number
 */
Backshift.Stats.Average = function (array) {
    if (array.length === 0) {
        return NaN;
    }
    var k, sum = 0, n = 0;
    for (k = array.length; k--;) {
        if (!isNaN(array[k])) {
            sum += array[k];
            n++;
        }
    }
    if (n < 1) {
        return NaN;
    }
    return sum / n;
};

/**
 * @param array
 * @returns number
 */
Backshift.Stats.StdDev = function (array) {
    if (array.length < 2) {
        return NaN;
    }

    // one pass calculation
    // http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#On-line_algorithm
    var m = 0;
    var m2 = 0;
    var len = array.length;

    for (var i = 0; i < len; ++i) {
        if (isNaN(array[i])) {
            continue;
        }

        var v = array[i];
        var d = v - m;
        m += d / (i + 1);
        m2 += d * (v - m);
    }

    return Math.sqrt(m2 / (len - 1));
};

Backshift.Stats.Last = function (array) {
    var k;
    for (k = array.length; k--;) {
        if (!isNaN(array[k])) {
            return array[k];
        }
    }
    return NaN;
};

Backshift.Stats.First = function (array) {
    var k, n = array.length;
    for (k = 0; k < n; k++) {
        if (!isNaN(array[k])) {
            return array[k];
        }
    }
    return NaN;
};

/**
 * @param array
 * @returns number
 */
Backshift.Stats.Total = function (array) {
    if (array.length === 0) {
        return NaN;
    }
    var k, sum = 0;
    for (k = array.length; k--;) {
        if (!isNaN(array[k])) {
            sum += array[k];
        }
    }
    return sum;
};

Backshift.Stats.Map = {
    'max': Backshift.Stats.Maximum,
    'min': Backshift.Stats.Minimum,
    'avg': Backshift.Stats.Average,
    'stdev': Backshift.Stats.StdDev,
    'last': Backshift.Stats.Last,
    'first': Backshift.Stats.First,
    'total': Backshift.Stats.Total
};

Backshift.namespace('Backshift.Utilities.Url');

Backshift.Utilities.Url  = Backshift.Class.create( {
    initialize: function(baseUrl) {
        this.url = baseUrl;
        this.paramCount = 0;
    },
    andParam: function(kw, parameter) {
        var sep = this.paramCount > 0 ? "&" : "?";

        if (parameter !== undefined) {
            this.paramCount += 1;
            this.url = this.url + sep + kw + "=" + parameter;
        }

        return this;
    },
    toString: function() {
        return this.url;
    }
} );

Backshift.namespace('Backshift.Utilities.RpnToJexlConverter');

/**
 * References:
 *   http://oss.oetiker.ch/rrdtool/doc/rrdgraph_rpn.en.html
 *   http://commons.apache.org/proper/commons-jexl/reference/syntax.html
 *
 * @author jesse
 */
Backshift.Utilities.RpnToJexlConverter  = Backshift.Class.create( {
  initialize: function() {
    this.operators = {};
    this._buildOperators();
  },

  _buildOperators: function() {
    var simpleOp = function (op) {
      return function(stack) {
        var b = stack.pop();
        var a = stack.pop();
        return "(" + a + " " + op + " " + b + ")";
      };
    };

    var ifOp = function(stack) {
      var c = stack.pop();
      var b = stack.pop();
      var a = stack.pop();
      return "(" + a + " != 0 ? " + b + " : " + c + ")";
    };

    var unOp = function(stack) {
      var a = stack.pop();
      return "( (" + a + " == __inf) || (" + a + " == __neg_inf) ? 1 : 0)";
    };

    var booleanOp = function (op) {
      return function(stack) {
        var b = stack.pop();
        var a = stack.pop();
        return "(" + a + " " + op + " " + b + " ? 1 : 0)";
      };
    };

    this.operators['+'] = simpleOp('+');
    this.operators['-'] = simpleOp('-');
    this.operators['*'] = simpleOp('*');
    this.operators['/'] = simpleOp('/');
    this.operators['%'] = simpleOp('%');
    this.operators['IF'] = ifOp;
    this.operators['UN'] = unOp;
    this.operators['LT'] = booleanOp('<');
    this.operators['LE'] = booleanOp('<=');
    this.operators['GT'] = booleanOp('>');
    this.operators['GE'] = booleanOp('>=');
    this.operators['EQ'] = booleanOp('==');
    this.operators['NE'] = booleanOp('!=');

  },

  convert: function(rpn) {
    var token, tokens, n, i, stack = [];
    tokens = rpn.split(",");
    n = tokens.length;
    for (i = 0; i < n; i++) {
      token = tokens[i];
      if (this._isOperator(token)) {
        stack.push(this._toExpression(token, stack));
      } else {
        stack.push(token);
      }
    }

    if (stack.length === 1) {
      return stack.pop();
    } else {
      Backshift.fail('Too many input values in RPN express. RPN: ' + rpn + ' Stack: ' + JSON.stringify(stack));
    }
  },

  _isOperator: function(token) {
    return token in this.operators;
  },

  _toExpression: function(token, stack) {
    return this.operators[token](stack);
  }

} );

Backshift.namespace('Backshift.Utilities.RrdGraphConverter');

Backshift.Utilities.RrdGraphVisitor  = Backshift.Class.create( {
  initialize: function(args) {
    this.onInit(args);
  },

  onInit: function(args) {
    // Defined by subclasses
  },

  _visit: function(graphDef) {
    // Inspired from http://krasimirtsonev.com/blog/article/Simple-command-line-parser-in-JavaScript
    var CommandLineParser = (function() {
        var parse = function(str, lookForQuotes) {
          var args = [];
          var readingPart = false;
          var part = '';
          var n = str.length;
          for(var i=0; i < n; i++) {
          if(str.charAt(i) === ' ' && !readingPart) {
            args.push(part);
            part = '';
            } else {
            if(str.charAt(i) === '\"' && lookForQuotes) {
              readingPart = !readingPart;
              part += str.charAt(i);
            } else {
              part += str.charAt(i);
            }
          }
        }
        args.push(part);
        return args;
      };
      return {
        parse: parse
      }
    })();

    var i, command, name, path, dsName, consolFun, rpnExpression, subParts, width, srcName, color, legend;
    var parts = CommandLineParser.parse(graphDef.command, true);
    var n = parts.length;
    for (i = 0; i < n; i++) {
      var args = parts[i].split(":");
      command = args[0];

      if (command === "DEF") {
        subParts = args[1].split("=");
        name = subParts[0];
        path = subParts[1];
        dsName = args[2];
        consolFun = args[3];
        this._onDEF(name, path, dsName, consolFun);
      } else if (command === "CDEF") {
        subParts = args[1].split("=");
        name = subParts[0];
        rpnExpression = subParts[1];
        this._onCDEF(name, rpnExpression);
      } else if (command.match(/LINE/)) {
        width = parseInt(/LINE(\d+)/.exec(command));
        subParts = args[1].split("#");
        srcName = subParts[0];
        color = '#' + subParts[1];
        legend = this._displayString(args[2]);
        this._onLine(srcName, color, legend, width);
      } else if (command === "AREA") {
        subParts = args[1].split("#");
        srcName = subParts[0];
        color = '#' + subParts[1];
        legend = this._displayString(args[2]);
        this._onArea(srcName, color, legend);
      } else if (command === "STACK") {
        subParts = args[1].split("#");
        srcName = subParts[0];
        color = '#' + subParts[1];
        legend = this._displayString(args[2]);
        this._onStack(srcName, color, legend);
      }
    }
  },
  _onDEF: function(name, path, dsName, consolFun) {

  },
  _onCDEF: function(name, rpnExpression) {

  },
  _onLine: function(srcName, color, legend, width) {

  },
  _onArea: function(srcName, color, legend) {

  },
  _onStack: function(srcName, color, legend) {

  },
  _displayString: function(string) {
    if (string === undefined) {
      return string;
    }
    // Remove any leading/trailing quotes
    string = string.replace(/^"(.*)"$/, '$1');
    // Remove any trailing newlines
    return string.replace(/(.*)\\n$/, '$1');
  }
} );

Backshift.namespace('Backshift.Utilities.RrdGraphConverter');

/**
 * Reference: https://github.com/j-white/opennms/blob/feature-rrd-rest/opennms-webapp/src/main/java/org/opennms/web/rest/rrd/graph/NGGraphModelBuilder.java
 */
Backshift.Utilities.RrdGraphConverter  = Backshift.Class.create( Backshift.Utilities.RrdGraphVisitor, {

  onInit: function(args) {
    this.graphDef = args.graphDef;
    this.resourceId = args.resourceId;

    this.model = {
      sources: [],
      series: []
    };

    this.rpnConverter = new Backshift.Utilities.RpnToJexlConverter();

    // Replace strings.properties tokens
    var propertyValue, i, n = this.graphDef.propertiesValues.length;
    for (i = 0; i < n; i++) {
      propertyValue = this.graphDef.propertiesValues[i];
      var re = new RegExp("\\{" + propertyValue + "}", "g");
      this.graphDef.command = this.graphDef.command.replace(re, propertyValue);
    }

    this._visit(this.graphDef);

    // Determine the set of source names that are used in the series / legends
    var nonTransientSources = {};
    n = this.model.series.length;
    for (i = 0; i < n; i++) {
      nonTransientSources[this.model.series[i].source] = 1;
    }

    // Mark all other sources as transient - if we don't use their values, then don't return them
    var source;
    n = this.model.sources.length;
    for (i = 0; i < n; i++) {
      source = this.model.sources[i];
      source.transient = !(source.name in nonTransientSources);
    }
  },

  _onDEF: function(name, path, dsName, consolFun) {
    var columnIndex = parseInt(/\{rrd(\d+)}/.exec(path)[1]) - 1;
    var attribute = this.graphDef.columns[columnIndex];

    this.model.sources.push({
      name: name,
      resourceId: this.resourceId,
      attribute: attribute,
      aggregation: consolFun
    });
  },

  _onCDEF: function(name, rpnExpression) {
    this.model.sources.push({
      name: name,
      expression: this.rpnConverter.convert(rpnExpression)
    });
  },

  _onLine: function(srcName, color, legend, width) {
    this.model.series.push({
      name: legend,
      source: srcName,
      type: "line",
      color: color
    });
  },

  _onArea: function(srcName, color, legend) {
    this.model.series.push({
      name: legend,
      source: srcName,
      type: "area",
      color: color
    });
  },

  _onStack: function(srcName, color, legend) {
    this.model.series.push({
      name: legend,
      source: srcName,
      type: "stack",
      color: color
    });
  }
} );

/**
 * Created by jwhite on 5/21/14.
 */

Backshift.namespace('Backshift.Graph');

/** The core graph implementation */
Backshift.Graph = Backshift.Class.create( Backshift.Class.Configurable, {
    initialize: function(args) {
        if (args.model === undefined) {
            Backshift.fail('Graph needs a model.');
        }
        this.validateModel(args.model);
        this.model = args.model;

        if (args.element === undefined) {
            Backshift.fail('Graph needs an element.');
        }
        this.element = args.element;

        this.dp = this.createDataProcessor();

        this.configure(args);

        if (this.start === 0 && this.end === 0 && this.last === 0) {

            Backshift.fail('Graph needs start and end, or last to be non-zero.');
        }

        this.fetchInProgress = false;
        this.lastSuccessfulFetch = 0;
        this.timer = null;

        this.onInit(args);
    },

    defaults: function() {
        return {
            width: 400,
            height: 240,
            resolution: 0,
            start: 0,
            end: 0,
            last: 0,
            refreshRate: 30*1000,
            checkInterval: 1000
        };
    },

    validateModel: function(model) {
        if (model.dataProcessor === undefined) {
            Backshift.fail('Model must contain a data processor.');
        }
    },

    createDataProcessor: function() {
        var self = this;
        return Backshift.Data.Factory.create(this.model.dataProcessor, {
            sources: this.model.sources,
            beforeFetch: function(dp, args) {
                self._beforeFetch(dp, args);
            },
            onFetchSuccess: function(dp, args) {
                self._onFetchSuccess(dp, args);
            },
            onFetchFail: function(dp, args) {
                self.onFetchFail(dp, args);
            },
            afterFetch: function(dp, args) {
                self._afterFetch(dp, args);
            },
            onNewData: function(dp, args) {
                self.onNewData(dp, args);
            }
        });
    },

    render: function() {
        if (this.timer !== null) {
            clearInterval(this.timer);
            this.timer = null;
        }

        this.onRender();
        this.refresh();
        this.createTimer();
    },

    createTimer: function() {
        var self = this;
        this.timer = setInterval(function () {
            if (self.isRefreshRequired()) {
                self.refresh();
            }
        }, this.checkInterval);
    },

    setStart: function(start) {
        this.start = start;
        this.refresh();
    },

    setEnd: function(end) {
        this.end = end;
        this.refresh();
    },

    refresh: function() {
        var timeSpan = this.getTimeSpan();
        this.dp.fetch(timeSpan.start, timeSpan.end, this.getResolution());
    },

    isRefreshRequired: function() {
        // Don't refresh in another fetch is currently in progress
        if (this.fetchInProgress) {
            return false;
        }

        // Don't refresh if disabled.
        if (this.refreshRate === 0) {
            return false;
        }

        return this.lastSuccessfulFetch <= Date.now() - this.refreshRate;
    },

    getTimeSpan: function() {
        var timeSpan = {};
        if (this.last > 0) {
            timeSpan.end = Math.floor(Date.now() / 1000);
            timeSpan.start = timeSpan.end - this.last;
        } else {
            timeSpan.end = this.end;
            timeSpan.start = this.start;
        }
        return timeSpan;
    },

    getResolution: function() {
        if (this.resolution > 0) {
            return this.resolution;
        } else {
            return this.width;
        }
    },

    _beforeFetch: function(dp, args) {
        this.fetchInProgress = true;
        this.beforeFetch(dp, args);
    },

    _afterFetch: function(dp, args) {
        this.fetchInProgress = false;
        this.afterFetch(dp, args);
    },

    _onFetchSuccess: function(dp, args) {
        this.lastSuccessfulFetch = Date.now();
        this.onFetchSuccess(dp, args);
    },

    onInit: function(args) {
        // Implemented by subclasses
    },

    onRender: function() {
        // Implemented by subclasses
    },

    beforeFetch: function(dp, args) {
        // Implemented by subclasses
    },

    onFetchSuccess: function(dp, args) {
        // Implemented by subclasses
    },

    onFetchFail: function(dp, args) {
        // Implemented by subclasses
    },

    afterFetch: function(dp, args) {
        // Implemented by subclasses
    },

    onNewData: function(dp, args) {
        // Implemented by subclasses
    }
});

/**
 * Created by jwhite on 5/22/14.
 */

Backshift.namespace('Backshift.Data');

/**
 * A data provider.
 *
 * @constructor
 * @param {object} args Dictionary of arguments.
 * @param {number} [args.start] Seconds since the Unix epoch.
 * @param {number} [args.end] Seconds since the Unix epoch.
 * @param {number} [args.last] Length of sliding window. start = now - last, end = now.
 * @param {number} [args.resolution] Desired number of points. Infinity for finest.
 * @param          [args.sources] Source definitions. See @.
 */
Backshift.Data = Backshift.Class.create( Backshift.Class.Configurable, {

    initialize: function(args) {
        if (args.sources === undefined || args.sources.length === 0) {
            Backshift.fail('Data provider needs one or more sources.');
        }

        this.sources = {};
        for (var i = 0; i < args.sources.length; i++) {
            this.sources[args.sources[i].name] = {
                "def": args.sources[i],
                "values": []
            };
        }

        this.timestamps = [];

        this.configure(args);

        this.onInit(args);
    },

    defaults: function() {
        return {
            beforeFetch: function() {},
            onFetchSuccess: function() {},
            onFetchFail: function() {},
            afterFetch: function() {},
            onNewData: function() {}
        };
    },

    /**
     * @param {number} [start] Seconds since the Unix epoch.
     * @param {number} [end] Seconds since the Unix epoch.
     * @param {number} [resolution] Desired number of points.
     * @param {object} [args] Additional parameter that is passed to the callbacks.
     */
    fetch: function(start, end, resolution, args) {
        this.beforeFetch(this, args);
        this.onFetch(start, end, resolution, args);
    },

    getTimestamps: function() {
        return this.timestamps;
    },

    getValues: function(sourceName) {
        if (!(sourceName in this.sources)) {
            throw sourceName + " was not added to the data processor.";
        }

        return this.sources[sourceName].values;
    },

    getMatrix: function() {
        // Use the timestamps as the first column
        var labels = ['timestamp'];
        var mat = [this.getTimestamps()];

        Backshift.keys(this.sources).forEach( function(key) {
            var source = this.sources[key];
            labels.push(key);
            mat.push(source.values);
        }, this);

        mat.labels = labels;
        return mat;
    },

    onInit: function(args) {
        // Defined by subclasses
    },

    onFetch: function(start, end, resolution, args) {
        // Defined by subclasses
    },

    resizeTo: function(n) {
        // Create a new array iff the size has changed
        if (this.timestamps === undefined || this.timestamps.length != n) {
            this.timestamps = new Array(n);
        }

        Backshift.keys(this.sources).forEach( function(key) {
            if (this.sources[key].values === undefined || this.sources[key].values.length != n) {
                this.sources[key].values = new Array(n);
            }
        }, this);
    }
} );

/**
 * Created by jwhite on 5/21/14.
 */

Backshift.namespace('Backshift.Data.Mock');

/**
 * A data provider that allows the series values to be generated as a function of time.
 *
 * The timestamps are fitted evenly across the given range with the given resolution.
 *
 */
Backshift.Data.Mock = Backshift.Class.create( Backshift.Data, {
    defaults: function($super) {
        return Backshift.extend( $super(), {
            generator: function() { return 0; }
        } );
    },

    onFetch: function(start, end, resolution, args) {
        // Create/resize the arrays used to store the timestamps and values
        this.resizeTo(resolution);

        // Split the interval evenly into n points and use these as the timestamps
        for (var i = 0; i < resolution; i++) {
            this.timestamps[i] = start + (i / (resolution-1) ) * (end - start);
        }

        // Generate the values for every source at every timestamp using
        // the given generator function
        Backshift.keys(this.sources).forEach(function(key) {
            var source = this.sources[key];
            for (i = 0; i < resolution; i++) {
                source.values[i] = this.generator(this.timestamps[i], source.def);
            }
        }, this);

        this.onFetchSuccess(this, args);
        this.afterFetch(this, args);
    }
} );

/**
 * Created by jwhite on 5/22/14.
 */

Backshift.namespace('Backshift.Data.Mock.Trig.FnFactory');

Backshift.Data.Mock.TrigFnFactory = {};

Backshift.Data.Mock.TrigFnFactory.create = function (source) {
    var constr = source.type.toLowerCase();

    if (typeof Backshift.Data.Mock.TrigFnFactory[constr] !== "function") {
        throw {
            name: "Error",
            message: constr + " doesn't exist"
        };
    }

    return new Backshift.Data.Mock.TrigFnFactory[constr](source);
};

Backshift.Data.Mock.TrigFnFactory.sin = function(source) {
    this.amplitude = 1;
    if (source.amplitude !== undefined) {
        this.amplitude = source.amplitude;
    }

    this.hshift = 0;
    if (source.hshift !== undefined) {
        this.hshift = source.hshift;
    }

    this.vshift = 0;
    if (source.vshift !== undefined) {
        this.vshift = source.vshift;
    }

    this.period = 2 * Math.PI;
    if (source.period !== undefined) {
        this.period = source.period;
    }

    this.fn = function(x) {
        var B = (2 * Math.PI) / this.period;
        return this.amplitude * Math.sin(B * (x - this.hshift)) + this.vshift;
    };
};

/**
 * Created by jwhite on 5/22/14.
 */

Backshift.namespace('Backshift.Data.Mock.Trig');

Backshift.Data.Mock.Trig = Backshift.Class.create( Backshift.Data.Mock, {
    defaults: function($super) {
        return Backshift.extend( $super(), {
            generator: this.trigonometricGenerator
        } );
    },

    trigonometricGenerator: function(x, source) {
        var trig = Backshift.Data.Mock.TrigFnFactory.create(source);
        return trig.fn(x);
    }
} );

/**
 * Created by jwhite on 5/23/14.
 */

Backshift.namespace('Backshift.Data.Newts');

/**
 * A data provider that interacts with Newts.
 *
 */
Backshift.Data.Newts = Backshift.Class.create( Backshift.Data, {

    onInit: function() {
        // Map the sources by resource name
        this.resources = {};
        Backshift.keys(this.sources).forEach( function(sourceName) {
            var source = this.sources[sourceName].def;
            var resource = source.resource;

            if (resource in this.resources) {
                this.resources[resource].push(source);
            } else {
                this.resources[resource] = [source];
            }
        }, this );

        // Determine the unique step sizes
        this.step_sizes = {};
        Backshift.keys(this.sources).forEach( function(sourceName) {
            var source = this.sources[sourceName].def;
            // Skip sources without a step, i.e. expressions
            if (source.step === undefined) {
                return;
            }

            if (source.step in this.step_sizes) {
                this.step_sizes[source.step] += 1;
            } else {
                this.step_sizes[source.step] = 1;
            }
        }, this);

        // and calculate their LCM
        this.step_sizes.lcm = Backshift.Math.lcm(Backshift.keys(this.step_sizes));
    },

    defaults: function($super) {
        return Backshift.extend( $super(), {
            url: "http://127.0.0.1:8000/",
            username: "newts",
            password: "newts"
        } );
    },

    onFetch: function(start, end, targetResolution, args) {
        // Determine the desired resolution
        var resolution = this.getResolutionInSeconds(start, end, targetResolution);

        // Fetch each resource individually
        Backshift.keys(this.resources).forEach( function(resource) {
            // Don't process anything if there aren't any sources
            var sources = this.resources[resource];
            if (sources.length < 1) {
                return;
            }

            // Build the report
            var report = JSON.stringify(this.getReport(sources));

            // Build the URL
            var url = new Backshift.Utilities.Url(this.url + 'measurements/' + resource)
                .andParam('start', start)
                .andParam('end', end)
                .andParam('resolution', resolution + "s")
                .toString();

            // Setup the callbacks
            var self = this;

            var onSuccess = function (measurements) {
                // (Re)build the store measurements
                var n = measurements.length;
                self.resizeTo(n);
                for (var i = 0; i < n; i++) {
                    var m = measurements[i].length;
                    self.timestamps[i] = Math.floor(measurements[i][0].timestamp / 1000);
                    for (var j = 0; j < m; j++) {
                        var measurement = measurements[i][j];
                        self.sources[measurement.name].values[i] = measurement.value;
                    }
                }
                self.onFetchSuccess(self, args);
                self.afterFetch(self, args);
            };

            var onFailure = function() {
                self.onFetchFail(self, args);
                self.afterFetch(self, args);
            };

            // Make the request
            this.getMeasurements(url, report, onSuccess, onFailure);
        }, this);
    },

    getReport: function(sources) {
        if (sources.length < 1) {
            throw "Cannot build a report with no sources.";
        }

        var report = {
            interval: NaN,
            datasources: [],
            expressions: [],
            exports: []
        };

        var i, n = sources.length;
        for (i = 0; i < n; i++) {
            var source = sources[i];

            if (source.step !== undefined) {
                if (isNaN(report.interval)) {
                    report.interval = source.step;
                } else if (report.interval !== source.step) {
                    throw "All of the steps must match for a given resource: " + report.interval + " vs " + source.step;
                }
            }

            if (source.expression === undefined) {
                // Assume its a data-source
                report.datasources.push(
                    {
                        label     : source.name,
                        source    : source.dsName,
                        function  : source.csFunc,
                        heartbeat : source.heartbeat
                    }
                );
            } else {
                // It's an expression
                report.expressions.push(
                    {
                        label     : source.name,
                        expression: source.expression
                    }
                );
            }

            report.exports.push(source.name);
        }

        return report;
    },

    /** Based on the time span (t0, t1) and requested resolution (R),
     *  determine the optimal resolution (r) that will be used to
     *  retrieve the measurements.
     *
     *  The optimal resolution should be a common multiple of all of the
     *  source's steps and be as close to (t1-t0) / R as possible.
     *
     */
    getResolutionInSeconds: function(start, end, targetResolution) {
        var targetResolutionInSeconds = (end - start) / targetResolution;

        var multiple = Math.ceil( targetResolutionInSeconds / this.step_sizes.lcm );
        if (multiple < 2) {
            multiple = 2; // The Newts API requires this to be at least 2.
        }

        return multiple * this.step_sizes.lcm;
    },

    getMeasurements: function(url, report, onSuccess, onError) {
        jQuery.ajax({
            url: url,
            username: this.username,
            password: this.password,
            xhrFields: {
                withCredentials: true
            },
            type: "POST",
            data: report,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: onSuccess,
            error:  onError
        });
    }
} );

/**
 * Created by jwhite on 6/6/14.
 */

Backshift.namespace('Backshift.Data.OnmsRRD');

Backshift.Data.OnmsRRD = Backshift.Class.create( Backshift.Data, {
    defaults: function($super) {
        return Backshift.extend( $super(), {
            url: "http://127.0.0.1:8980/opennms/rest/rrd",
            username: "admin",
            password: "admin"
        } );
    },

    onFetch: function(start, end, resolution, args) {
        // Build the query request
        var queryRequest = this.getQueryRequest(start, end, resolution);

        // Setup the callbacks
        var self = this;

        var onSuccess = function (data) {
            var i, k = data.timestamps.length;
            self.timestamps = data.timestamps;
            for (i = 0; i < k; i++) {
              // Convert ms to secs
              self.timestamps[i] /= 1000;
            }

            k = data.labels.length;
            for (i = 0; i < k; i++) {
              self.sources[data.labels[i]].values = data.columns[i].values;
            }

            self.onFetchSuccess(self, args);
            self.afterFetch(self, args);
        };

        var onFailure = function() {
            self.onFetchFail(self, args);
            self.afterFetch(self, args);
        };

        // Make the call
        this.getResults(queryRequest, onSuccess, onFailure);
    },

    getQueryRequest: function(start, end, resolution) {
      var queryRequest = {
        "start": start * 1000,
        "end": end * 1000,
        "step": Math.floor((end - start) / resolution),
        "source": [],
        "expression": []
      };

      var timeDeltaInSeconds = end - start;

      var qrSource;
      Backshift.keys(this.sources).forEach( function(key) {
          var source = this.sources[key];
          if (source.def.resourceId !== undefined) {
            qrSource = {
              aggregation: source.def.aggregation,
              attribute: source.def.attribute,
              label: source.def.name,
              resourceId: source.def.resourceId,
              transient: source.def.transient
            };
            queryRequest.source.push(qrSource);
          } else {
            qrSource = {
              value: source.def.expression,
              label: source.def.name,
              transient: source.def.transient
            };
            qrSource.value = qrSource.value.replace("{diffTime}", timeDeltaInSeconds);
            queryRequest.expression.push(qrSource);
          }
      }, this );

      if (queryRequest.source.length === 0) {
        delete queryRequest.source;
      }

      if (queryRequest.expression.length === 0) {
        delete queryRequest.expression;
      }

      return queryRequest;
    },

    getResults: function(queryRequest, onSuccess, onError) {
        jQuery.ajax({
            url: this.url,
            username: this.username,
            password: this.password,
            xhrFields: {
                withCredentials: true
            },
            type: "POST",
            data: JSON.stringify(queryRequest),
            contentType: "application/json",
            dataType: "json",
            success: onSuccess,
            error:  onError
        });
    }
} );

/**
 * Created by jwhite on 5/22/14.
 */

Backshift.namespace('Backshift.Data.Factory');

Backshift.Data.Factory = {};

Backshift.Data.Factory.create = function (def, args) {
    var constr = def.type.toLowerCase();

    if (typeof Backshift.Data.Factory[constr] !== "function") {
        Backshift.fail("Data provider " + constr + " doesn't exist");
    }

    Backshift.keys(def).forEach( function(key) {
        args[key] = def[key];
    });

    return new Backshift.Data.Factory[constr](args);
};

Backshift.Data.Factory.trig = function(args) {
    return new Backshift.Data.Mock.Trig(args);
};

Backshift.Data.Factory.newts = function(args) {
    return new Backshift.Data.Newts(args);
};

Backshift.Data.Factory.onmsrrd = function(args) {
    return new Backshift.Data.OnmsRRD(args);
};

/**
 * Created by jwhite on 6/3/14.
 */
Backshift.namespace('Backshift.Legend');

Backshift.Legend  = Backshift.Class.create( Backshift.Class.Configurable, {
    initialize: function(args) {
        if (args.model === undefined) {
            Backshift.fail('Legend needs a model.');
        }
        this.model = args.model;

        if (args.element === undefined) {
            Backshift.fail('Legend needs an element.');
        }
        this.element = args.element;

        this.dp = args.dataProcessor;

        if (typeof args.model.legend === 'string') {
            // The legend is a string
            this.template = args.model.legend;
        } else if (Object.prototype.toString.call( args.model.legend ) === '[object Array]') {
            // The legend is an array of strings
            this.template = args.model.legend.join(' ');
        } else if (args.model.legend === undefined) {
            // The legend is not defined
            this.template = "";
        } else {
            Backshift.fail("Invalid legend '" + args.model.legend + "'");
        }

        this.configure(args);
    },
    defaults: function() {
        return {
            width: 400
        };
    },
    getSeriesContext: function(series) {
        var context = Backshift.clone(series);
        var values = this.dp.getValues(series.source);
        Backshift.keys(Backshift.Stats.Map).forEach( function(key) {
            context[key] = Backshift.Stats.Map[key](values);
        }, this);
        return context;
    },
    render: function() {
        var series = this.model.series,
            n = series.length, k;

        var context = {};
        for (k = 0; k < n; k++) {
            context[k] = this.getSeriesContext(series[k]);
        }

        context.series = [];
        for (k = 0; k < n; k++) {
            context.series.push(context[k]);
        }

        var result = Mark.up(this.template, context);
        if (result.length > 0) {
            this.element.innerHTML = "<pre>" + result + "</pre>";
        }
    }
} );
/**
 * Created by jwhite on 6/3/14.
 */
Backshift.namespace('Backshift.Legend.Rickshaw');

Backshift.Legend.Rickshaw  = Backshift.Class.create( Backshift.Legend, {
    initialize: function($super, args) {
        this.graph = args.graph;
        args.width = this.graph.width;

        this.graphSeries = {};
        this.graph.series.forEach( function(s) {
            this.graphSeries[s.name] = s;
        }, this );

        $super(args);
    },
    getSeriesContext: function($super, series) {
        var context = $super(series);
        context.swatch = "<div style='display: inline-block; width: 10px; height: 10px; margin: 0 8px 0 0; background-color: " +
            this.graphSeries[series.name].color + "'></div>";
        return context;
    }
});
/**
 * Created by jwhite on 5/23/14.
 */

Backshift.namespace('Backshift.Graph.Matrix');

/** Draws a table with all of the sources values. */
Backshift.Graph.Matrix  = Backshift.Class.create( Backshift.Graph, {

    onInit: function() {
        // Used to hold a reference to the div that holds the status text
        this.statusBlock = null;
    },

    beforeFetch: function() {
        this.timeBeforeFetch = Date.now();
        this.updateStatus("Fetching...");
    },

    onFetchSuccess: function(dp) {
        this.drawMatrix(dp);
        var timeAfterFetch = Date.now();
        var secondsForFetch =  Number((timeAfterFetch - this.timeBeforeFetch) / 1000).toFixed(2);
        this.updateStatus("Successfully retrieved data in " + secondsForFetch + " seconds.");
    },

    onFetchFail: function() {
        this.updateStatus("Fetch failed.");
    },

    onNewData: function() {
        this.updateStatus("Received new data!");
    },

    updateStatus: function(status) {
        if (this.statusBlock) {
            this.statusBlock.text(status);
        } else {
            this.statusBlock = d3.select(this.element).append("p").attr("align", "right").text(status);
        }
    },

    drawMatrix: function(dp) {
        var mat = dp.getMatrix(),
            n = mat.labels.length,
            rows = new Array(n),
            i = 0,
            entry,
            k;

        /* Format the rows to:
            {
              'timestamp': 100,
              'source1': 1,
              'source2:' 2
            },
         */
        Backshift.rows(mat).forEach( function(row) {
            entry = {};
            for (k = 0; k < n; k++) {
                entry[mat.labels[k]] = row[k];
            }
            rows[i++] = entry;
        }, this );

        // Retrieve the current status, if present
        var status = "";
        if (this.statusBlock) {
            status = this.statusBlock.text();
        }

        // Empty the div
        d3.select(this.element).selectAll("*").remove();

        // Re-append the status
        this.statusBlock = d3.select(this.element).append("p").attr("align", "right").text(status);

        // Draw the table
        this.tabulate(this.element, rows, mat.labels);

        // Add some meta-data to the div
        d3.select(this.element)
            .attr("data-matrix", JSON.stringify(mat));

        d3.select(this.element)
            .attr("data-rendered-at", Date.now());
    },

    /** Builds an HTML table using D3.
     *
     *  Shamelessly stolen from http://www.d3noob.org/2013/02/add-html-table-to-your-d3js-graph.html */
    tabulate: function(element, data, columns) {
        var table = d3.select(element).append("table")
                .attr("style", "font-size: 10px"),
            thead = table.append("thead"),
            tbody = table.append("tbody");

        // Append the header row
        thead.append("tr")
            .selectAll("th")
            .data(columns)
            .enter()
            .append("th")
            .text(function(column) { return column; });

        // Create a row for each object in the data
        var rows = tbody.selectAll("tr")
            .data(data)
            .enter()
            .append("tr");

        // Create a cell in each row for each column
        rows.selectAll("td")
            .data(function(row) {
                return columns.map(function(column) {
                    return {column: column, value: row[column]};
                });
            })
            .enter()
            .append("td")
            .attr("style", "font-family: Courier; padding:0 15px 0 15px;")
            .attr("align", "center")
            .html(function(d) { return Number(d.value).toFixed(4); });

        return table;
    }
});

/**
 * Created by jwhite on 31/03/15.
 */

Backshift.namespace('Backshift.Graph.Flot');

/** A graph implementation that uses Flot */
Backshift.Graph.Flot  = Backshift.Class.create( Backshift.Graph, {

    onInit: function() {
      this.plot = null;

      // Set the containers width/height
      jQuery(this.element).width(this.width).height(this.height);

      // Create empty arrays for the series data
      this.flotSeries = [];
      var n = this.model.series.length;
      for (var i = 0; i < n; i++) {
        //var series = this.model.series[i];

        this.flotSeries.push([]);
      }
    },

    onRender: function() {
      this._updatePlot();
    },

    _shouldStack: function(series, k) {
      // If there's stack following the area, set the area to stack
      if (series[k].type === "area") {
        var n = series.length;
        for (var i = k; i < n; i++) {
          if (series[i].type === "stack") {
            return 1;
          }
        }
      }
      return series[k].type === "stack";
    },

    onFetchSuccess: function(dp) {
      var timestamps = dp.getTimestamps();

      var series, values, i, j, numSeries, numValues, xy;
      numSeries = this.model.series.length;
      numValues = timestamps.length;

      for (i = 0; i < numSeries; i++) {
        series = this.model.series[i];
        values = dp.getValues(series.source);

        xy = [];
        for (j = 0; j < numValues; j++) {
          xy.push([timestamps[j] * 1000, values[j]])
        }

        this.flotSeries[i] = {
          data: xy,
          label: series.name,
          color: series.color,
          stack: this._shouldStack(this.model.series, i),
          lines: {
            show: true,
            fill: series.type === "stack" || series.type === "area",
            fillColor: series.color
          }
        };
      }

      this._updatePlot();
    },

    _updatePlot: function() {
      this.plot = jQuery.plot(this.element, this.flotSeries, {
        series: {
          shadowSize: 0	// Drawing is faster without shadows
        },
        canvas: true,
        grid: {
          labelMargin: 10,
          hoverable: true,
          borderWidth: 0
        },
        legend: {
          //noColumns: 0,
          backgroundOpacity: 0.5
        },
        xaxis: {
          mode: "time",
          timezone: "browser"
        },
        yaxis: {
          tickFormatter: function suffixFormatter(val, axis) {
            if (val > 1000000000)
              return (val / 1000000000).toFixed(axis.tickDecimals) + " GB";
            else if (val > 1000000)
              return (val / 1000000).toFixed(axis.tickDecimals) + " MB";
            else if (val > 1000)
              return (val / 1000).toFixed(axis.tickDecimals) + " kB";
            else
              return val.toFixed(axis.tickDecimals) + " B";
          }
        }
      });
    }
});

/**
 * Created by jwhite on 5/21/14.
 */

Backshift.namespace('Backshift.Graph.Rickshaw');

/** A graph implementation that uses Rickshaw */
Backshift.Graph.Rickshaw  = Backshift.Class.create( Backshift.Graph, {

    onInit: function() {
        this.graph = null;

        // Create empty arrays for the series data
        this.seriesData = {};
        var n = this.model.series.length;
        for (var i = 0; i < n; i++) {
            var series = this.model.series[i];

            // HACK: Always set a name, must be unique
            if (series.name === undefined || series.name === "") {
                series.name = "series" + i;
            }

            this.seriesData[series.name] = [];
        }

        this.previewDiv = null;
        this.legendDiv = null;
    },

    updateSeriesData: function (dp) {
        var timestamps = dp.getTimestamps();

        var series, values, i, j, n, m;
        n = this.model.series.length;

        // Collect the indices of NaNs in all of the series
        var nans = {};
        for (i = 0; i < n; i++) {
            series = this.model.series[i];
            values = dp.getValues(series.source);
            m = values.length;
            for (j = 0; j < m; j++) {
                if (isNaN(values[j])) {
                    nans[j] = 1;
                }
            }
        }

        for (i = 0; i < n; i++) {
            series = this.model.series[i];
            values = dp.getValues(series.source);
            var store = this.seriesData[series.name];

            // Clear the store
            while (store.length > 0) {
                store.pop();
            }

            // Push in the values
            m = values.length;
            for (j = 0; j < m; j++) {
                // Skip NaNs
                if (j in nans) {
                    continue;
                }
                store.push({
                    x: timestamps[j],
                    y: values[j]
                });
            }

            //console.log(series.name + " has " + store.length + " points.");
        }

        // console.log("Series data used for Rickshaw", JSON.stringify(this.seriesData));

        if (this.graph !== null) {
            this.graph.update();
        }
    },

    getSeriesColor: function(series, fallback) {
        if (series.color !== undefined) {
            return series.color;
        } else {
            return fallback;
        }
    },

    getSeriesType: function(series, k) {
        // If there's stack following the area, set the area to stack
        var n = series.length;
        for (var i = k; i < n; i++) {
            if (series[i].type === "stack") {
                return "stack";
            }
        }
        return series[k].type;
    },

    onRender: function() {
        var yAxisWidth = 30;
        var legendLeftMargin = Math.floor(yAxisWidth / 2);

        var containerDiv = d3.select(this.element);
        var containerEl = containerDiv.node();
        containerEl.style.width = (this.width + yAxisWidth) + "px";

        this.yAxisDiv = containerDiv.append("div");
        var yAxisEl = this.yAxisDiv.node();
        yAxisEl.style.width = yAxisWidth + "px";
        yAxisEl.style.height = this.height + "px";
        yAxisEl.style.float = "left";

        this.chartDiv = containerDiv.append("div");
        var chartEl = this.chartDiv.node();
        chartEl.style.width = this.width + "px";
        chartEl.style.height = this.height + "px";
        chartEl.style.float = "left";

        /*
         var xAxisHeight = 30;
        this.xAxisDiv = containerDiv.append("div");
        var xAxisEl = this.xAxisDiv.node();
        xAxisEl.style.width = this.width + "px";
        xAxisEl.style.height = xAxisHeight + "px";
        xAxisEl.style.marginLeft = yAxisWidth + "px";
        chartEl.style.float = "left";
        */

        this.previewDiv = containerDiv.append("div");
        var previewEl = this.previewDiv.node();
        previewEl.style.width = this.width + "px";
        previewEl.style.marginLeft = yAxisWidth + "px";
        previewEl.style.float = "left";

        this.legendDiv = containerDiv.append("div");
        var legendEl = this.legendDiv.node();
        legendEl.style.width = (this.width + yAxisWidth - legendLeftMargin) + "px";
        legendEl.style.paddingTop = "10px";
        legendEl.style.marginLeft = legendLeftMargin + "px";
        legendEl.style.clear = "both";

        var palette = new Rickshaw.Color.Palette( { scheme: 'classic9' } );

        var rickshawSeries = [];
        var n = this.model.series.length;
        for (var i = 0; i < n; i++) {
            var series = this.model.series[i];

            rickshawSeries.push({
                name: series.name,
                data: this.seriesData[series.name],
                color: this.getSeriesColor(series, palette.color()),
                renderer: this.getSeriesType(this.model.series, i)
            });
        }

        this.graph = new Rickshaw.Graph( {
            element: chartEl,
            renderer: 'multi',
            width: this.width,
            height: this.height,
            min: 'auto',
            preserve: this.model.preview, // Always preserve the data when using the preview pane
            // Maybe this should be done in the preview pane instead?
            series: rickshawSeries,
            interpolation: 'step-after',
            padding: {top: 0.02, left: 0.02, right: 0.02, bottom: 0.02}
        } );

        this.graph.render();

        var xAxis = new Rickshaw.Graph.Axis.Time({
            graph: this.graph,
            orientation: 'bottom',
            /* element: xAxisEl, */
            timeFixture: new Rickshaw.Fixtures.Time.Local()
        });

        xAxis.render();

        var yAxis = new Rickshaw.Graph.Axis.Y({
            graph: this.graph,
            orientation: 'left',
            element: yAxisEl,
            tickFormat: Rickshaw.Fixtures.Number.formatKMBT
        });

        yAxis.render();

        this.legend = new Backshift.Legend.Rickshaw( {
            model: this.model,
            graph: this.graph,
            element: this.legendDiv.node(),
            dataProcessor: this.dp
        } );

        this.legend.render();

        /* var hoverDetail = */ new Rickshaw.Graph.HoverDetail( {
            graph: this.graph,
            xFormatter: function(x) {
                return new Date(x * 1000).toString();
            }
        } );
    },

    onFetchSuccess: function(dp) {
        this.updateSeriesData(dp);

        // Render the preview pane once the graph has data,
        // it fails to load intermittently otherwise
        if (this.model.preview) {
            var preview = new Rickshaw.Graph.RangeSlider.Preview( {
                graph: this.graph,
                element: this.previewDiv.node()
            } );

            preview.render();
        }

        // Update the legend with the latest values
        this.legend.render(this.dp);
    }
});
