/**
 * Entry Point and zoom level of the initial map
 * Coordinates of Brandenburger Tor
 */
//var initMap = {
//  lon : 13.41,
//  lat : 52.49,
//  zoom : 15
//};

//  Bremen:
var initMap = {
 lon : 8.82046,
 lat : 53.06866,
 zoom : 15
};

// These are the URLs of the servlets which provide routing and geocoding
var routingServiceURL  = "/HHRoutingWebservice/";

/**
 * Initialization of the OpenStreetMap map, layers and controls
 */
window.onload = function(){
  // set map div size once
  resizeMapWindow();

	map = new OpenLayers.Map("map", {
		controls : [ new OpenLayers.Control.Navigation(),
				new OpenLayers.Control.PanZoomBar(),
				new OpenLayers.Control.MousePosition(),
				new OpenLayers.Control.LayerSwitcher(),
				new OpenLayers.Control.Attribution()],
		projection : projSpheMe,
		displayProjection : projWSG84
	});

  // disable regular right click menu, call our own context menu function instead
	map.div.oncontextmenu = function cm(e) {
		contextmenu(e);
		return false;
	};
  // This simply removes the routing popup when the left mouse is clicked.
  var click = new OpenLayers.Control.Click();
  map.addControl(click);
  click.activate();

  // Here two Layers are added, OSM is the base Layer
	layerMapnik = new OpenLayers.Layer.OSM();
	map.addLayer(layerMapnik);

	// This is the style definition of how vectors are displayed
	var styleMap = new OpenLayers.StyleMap(OpenLayers.Util.applyDefaults({
      fillColor: "#FFBBBB",
      fillOpacity: 0.5,
      strokeColor: "#FF0000",
      strokeOpacity: 0.7,
      strokeWidth: 3
    },OpenLayers.Feature.Vector.style["default"]));

	layerVectors = new OpenLayers.Layer.Vector("Vectors", {styleMap:styleMap});
	map.addLayer(layerVectors);

	// Super awesome drag controler allows dragging of start and end points
  dragcontrol = new OpenLayers.Control.DragFeature(layerVectors, {
      geometryTypes: ["OpenLayers.Geometry.Point"],
      onDrag: dragpoint,
      onStart: dragstart,
      onComplete: dragcomplete
    });
	map.addControl(dragcontrol);
	dragcontrol.activate();

  // TODO: OpenLayers.Control.GetFeature

  // Add a little "Permalink" button to the map
  permalink = new OpenLayers.Control.Permalink();
  map.addControl(permalink);
  permalink.activate();
  // whenever the map is moved, the permalink is also updated
  map.events.register("moveend", map, updatePermaLink);

  // set the center of the map, if it has not been set by the permalink control
  var centerLonLat = new OpenLayers.LonLat(initMap.lon, initMap.lat).tf();
  if (!map.getCenter()) {
    map.setCenter(centerLonLat, initMap.zoom);
  }

  // read the routing coordinates from the url's parameters
  var args = OpenLayers.Util.getParameters();
  route.start.lat = args["route.start.lat"];
  route.start.lon = args["route.start.lon"];
  route.via.lat = args["route.via.lat"];
  route.via.lon = args["route.via.lon"];
  route.end.lat = args["route.end.lat"];
  route.end.lon = args["route.end.lon"];
  hhRoute();

  // Setup event handlers:
  document.getElementById("routeButton").onclick = hhRoute;
  document.getElementById("newRequestButton").onclick = newRequest;
  document.getElementById("sizeSwitch").onclick = sizeSwitch;

  document.search.start.onblur = geoCode;
  document.search.via.onblur = geoCode;
  document.search.end.onblur = geoCode;
};

var map; // complex object of type OpenLayers.Map
var dragcontrol; // the drag control object
var permalink; // the permalink contrik object

// These are the only 2 projections that are ever used in this project
var projWSG84 = new OpenLayers.Projection("EPSG:4326");
var projSpheMe = new OpenLayers.Projection("EPSG:900913");

// The route object contains all the info relevant to the routing
var route = {
  start: {},
  via: {},
  end: {}
};

/************************************************************************************
 * Right click menu is handled in this section
 ************************************************************************************/
var menupopup;
// This function draws the context menu
function contextmenu(e) {
  e = e || window.event; // For my beloved Internet Explorer
  clickX = e.clientX - leftDivWidth + 10; // 10 because of padding
  clickY = e.clientY - 5; // 5 also because of padding
  clickLatLon = map.getLonLatFromPixel(new OpenLayers.Pixel(clickX, clickY)).rtf();
  newValues = clickLatLon.lon
              + ',' + clickLatLon.lat
              + ',\'' + clickLatLon.lat
              + ', ' + clickLatLon.lon+ '\',';
  menuhtml = '<div id="popupmenu"><a onclick="setValues(' + newValues + '\'start\')">Set Start</a><br>'
           + '<a onclick="setValues(' + newValues + '\'via\')">Set Via</a><br>'
           + '<a onclick="setValues(' + newValues + '\'end\')">Set End</a></div>';

  menupopup = new OpenLayers.Popup("menu",
                               clickLatLon.tf(),
                               new OpenLayers.Size(80,60),
                               menuhtml,
                               false);
  map.addPopup(menupopup, true);
};

 // This control simply removes the context menu when the left mouse button is clicked.
OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {
    defaultHandlerOptions: {
        'single': true,
        'double': false,
        'pixelTolerance': 0,
        'stopSingle': false,
        'stopDouble': false
    },

    initialize: function(options) {
        this.handlerOptions = OpenLayers.Util.extend(
            {}, this.defaultHandlerOptions
        );
        OpenLayers.Control.prototype.initialize.apply(
            this, arguments
        );
        this.handler = new OpenLayers.Handler.Click(
            this, {
                'click': this.trigger
            }, this.handlerOptions
        );
    },

    trigger: function(e) {
      for ( var i = 0, len = map.popups.length; i < len; i++) {
    		map.removePopup(map.popups[i]);
    	}
    }
});


/**
 * Set the "permalink" to include the values of the route if applicable
 */

function updatePermaLink() {
  if (permalink.element != undefined) {
    if (route.start.lat != null && route.start.lon != null && route.end.lat != null && route.end.lon != null) {
      params = permalink.createParams(); //OpenLayers.Util.getParameters();
      params["route.start.lat"] = route.start.lat;
      params["route.start.lon"] = route.start.lon;
      params["route.end.lat"] = route.end.lat;
      params["route.end.lon"] = route.end.lon;
      if (route.via.lat != null && route.via.lon != null) {
        params["route.via.lat"] = route.via.lat;
        params["route.via.lon"] = route.via.lon;
      }
      permalink.element.href = '?' + OpenLayers.Util.getParameterString(params);
    }
  }
}

/**
 * Sets the route parameters and requests a new route
 *
 * @param lon
 *            longitude of the given coordinate
 * @param lat
 *            latitude of the given coordinate
 * @param desc
 *            textual description shown in the text field
 * @param station
 *            declares, which parameters will be set, this can be "start", "via" or "end"
 */
function setValues(lon, lat, desc, station) {
  route[station].lon = lon;
  route[station].lat = lat;
  route[station].text = desc;
	//document.search[station].value = desc;
	for ( var i = 0, len = map.popups.length; i < len; i++) {
		map.removePopup(map.popups[i]);
	}
	hhRoute();
	updatePermaLink();
	if (!route.drag.isdragging) {
	 reverseGeoCode(lat, lon, station);
	}
}

/**
 * Clean all input fields and OpenLayers layers.
 */
function newRequest() {
  //remove popups
  for ( var i = 0, len = map.popups.length; i < len; i++) {
		map.removePopup(map.popups[i]);
	}
	//remove vectors
  if(route.line) layerVectors.removeFeatures(route.line);
  if(startCircle) layerVectors.removeFeatures(startCircle);
  if(endCircle) layerVectors.removeFeatures(endCircle);
  f = ["start", "via", "end"];
  for (x in f) {
    x = f[x];
    route[x].lon = null;
    route[x].lat = null;
    document.search[x].value = "";
  }
}


/************************************************************************************
 * Hangle dragging and handle it well
 ************************************************************************************/
route.drag = {}; // encapsulate the dragging stuff
route.drag.isdragging = false;
function dragpoint(feature, pixel) {
  route.drag.date = new Date;
  // Enforce a minimum delay of 300 milliseconds between routing requests
  if ( (route.drag.lastDrag == undefined ||
          (route.drag.date.getTime() - route.drag.lastDrag > 300))
          && !(route.waitingForResponse) ) {
    route.drag.lastDrag = route.drag.date.getTime();
  	route.drag.LonLat = map.getLonLatFromPixel(new OpenLayers.Pixel(pixel.x, pixel.y));
    setValues(
      route.drag.LonLat.rtf().lon, // the transformed longitude
      route.drag.LonLat.rtf().lat, // the transformed latitude
      route.drag.LonLat.rtf().lon +", "+ route.drag.LonLat.rtf().lat, // The text that will be displayed
      feature.attributes.mf // This can be "start", "via" or "end"
    );
  }
}

function dragstart(feature, pixel) {
  route.drag.isdragging = true;
  route.drag.which = feature.attributes.mf;
}

function dragcomplete(feature, pixel) {
  route.drag.isdragging = false;
  if (!route.waitingForResponse) {
    redrawEndPoints();
  }
  reverseGeoCode(route.drag.LonLat.rtf().lat, route.drag.LonLat.rtf().lon, route.drag.which);
  route.drag.which = null;
  // it seems like sometimes the dragevent doesn't really end by itself,
  // so I force it here which seems to help
  dragcontrol.cancel();

}

var startCircle;
var endCircle;
function redrawEndPoints() {
    layerVectors.removeFeatures(startCircle);
    layerVectors.removeFeatures(endCircle);
    startCircle = new OpenLayers.Feature.Vector(route.start.point);
    startCircle.attributes.mf = "start";
    layerVectors.addFeatures(startCircle);
    endCircle = new OpenLayers.Feature.Vector(route.end.point);
    endCircle.attributes.mf = "end";
    layerVectors.addFeatures(endCircle);
}

/************************************************************************************
 *  Here the routing request is handled
 ************************************************************************************/

// send it
function hhRoute() {
  if (route.start.lon === undefined || route.start.lon === "" || route.end.lon === undefined || route.end.lon === "" ) return ;
	route.url = routingServiceURL + "?format=json&points=" + route.start.lon + "," + route.start.lat + ";" + route.end.lon + "," + route.end.lat;
  route.waitingForResponse = true;
	OpenLayers.Request.GET( {
		url : route.url,
		callback : hhRouteResponseHandler,
		scope : this
	});
}

// deal with routing request response
function hhRouteResponseHandler(response) {
  if (response.status == 200) {
    if(route.line) layerVectors.removeFeatures(route.line);
    geoJSONParser = new OpenLayers.Format.GeoJSON();
    route.line = geoJSONParser.read(response.responseText);
    // We need to manualy convert all points to sperical mercator
    // While iterating over the whole thing, I'll collect the streetnames too
    var directions = "";
    for (i in route.line) {
      for (j in route.line[i].geometry.components) {
        route.line[i].geometry.components[j].transform(projWSG84, projSpheMe); // I actually do want to transform the object itself
      }
      if (route.line[i].attributes.Motorway_Link == false) {
        streetName = route.line[i].attributes.Name;
        if (streetName == "") {
          streetName = route.line[i].attributes.Ref;
        } else if (route.line[i].attributes.Ref != "" && route.line[i].attributes.Ref != streetName) {
          streetName += " (" + route.line[i].attributes.Ref + ")";
        }
        curlength = Math.round(route.line[i].attributes["Length"]/10)*10;
        unit = "m";
        if (curlength > 1000) {
          curlength = Math.round(curlength/100)/10;
          unit = "km";
        }
        angle = route.line[i].attributes.Angle;
      	if (angle == -360 || isNaN(angle) || angle == undefined) {
      		arrow = "";
      	} else if (angle > 337 || angle < 22) {
      		arrow = '<div class="Straight Arrow"></div>';
      	} else if (angle > 22 && angle < 67) {
      		arrow = '<div class="R45 Arrow"></div>';
      	} else if (angle > 67 && angle < 112) {
      		arrow = '<div class="R90 Arrow"></div>';
      	} else if (angle > 112 && angle < 157) {
      		arrow = '<div class="R135 Arrow"></div>';
      	} else if (angle > 157 && angle < 202) {
      		arrow = '<div class="UTurn Arrow"></div>';
      	} else if (angle > 202 && angle < 247 ) {
      		arrow = '<div class="L135 Arrow"></div>';
      	} else if (angle > 247 && angle < 292 ) {
      		arrow = '<div class="L90 Arrow"></div>';
      	} else if (angle > 292 && angle < 337 ) {
      		arrow = '<div class="L45 Arrow"></div>';
      	}
        angle = Math.round(angle);
      	directions += '<tr><td class="direction">' + arrow + '</td><td class="street">' + streetName + '</td><td class="length">' +  curlength + '&nbsp;' + unit + '</td></tr>';
      } else {
        if (route.line[i].attributes.Name != "") {
          directions += '<tr><td class="direction"></td><td class="street">Take exit ' + route.line[i].attributes.Ref + ' (' + route.line[i].attributes.Name + ')</td><td class="length"></td></tr>';
        } else {
          directions += '<tr><td class="direction"></td><td class="street">Take the motorway link</td><td class="length"></td></tr>';
        }
      }
    }
    document.getElementById("turnByTurn").innerHTML = '<table id="turnTable" />' + directions + '</table>';
    layerVectors.addFeatures(route.line);
    route.start.point = route.line[0].geometry.components[0];
    route.end.point = route.line[route.line.length-1].geometry.components[route.line[route.line.length-1].geometry.components.length-1];
    if (!route.drag.isdragging) redrawEndPoints();
  }
  route.waitingForResponse = false;
}

/************************************************************************************
 *  Here the GeoCoding happens
 ************************************************************************************/
// send it
var geoCodeStation;
var jsonReader = new OpenLayers.Format.JSON();
geoCode = function(e) {
  // Cross browser implementation of event target detection
  if (e.target) targ = e.target;
  else if (e.srcElement) targ = e.srcElement;
	if (targ.nodeType == 3) // defeat Safari bug
		targ = targ.parentNode;

  locationName = document.search[targ.name].value;
  geoCodeStation = targ.name;
	OpenLayers.Request.GET( {
		url : "http://nominatim.openstreetmap.org/search?format=json&q=" + locationName,
		success : geoCodeResponseHandler,
		scope : this
	});
};

geoCodeResponseHandler = function(response){
  data = jsonReader.read(response.responseText);
  if (data instanceof Array && data.length > 0 && data[0].hasOwnProperty("lat") && data[0].hasOwnProperty("lon")) {
    route[geoCodeStation].lat = data[0].lat;
    route[geoCodeStation].lon = data[0].lon;
    hhRoute();
  }
};

//reverse it
reverseGeoCode = function(lat, lon, station) {
  geoCodeStation = station;
	OpenLayers.Request.GET( {
		url : "http://nominatim.openstreetmap.org/reverse?format=json&addressdetails=1&lat=" + lat + "&lon=" + lon,
		success : reverseGeoCodeResponseHandler,
		scope : this
	});
};

reverseGeoCodeResponseHandler = function(response){
  data = jsonReader.read(response.responseText);
  var result = [];
  if (data.hasOwnProperty("address")) {
    if (data.address.hasOwnProperty("road")) {
      if (data.address.hasOwnProperty("house_number"))
        data.address.road += " " + data.address.house_number;
      result.push(data.address.road);
    }
    if (data.address.hasOwnProperty("city")) {
      if (data.address.hasOwnProperty("postcode"))
        data.address.city = data.address.postcode + " " + data.address.city;
      result.push(data.address.city);
    }
    if (data.address.hasOwnProperty("country"))
      result.push(data.address.country);
    document.search[geoCodeStation].value = result.join(", ");
  }
};

/************************************************************************************
 *  This part takes care of the resizing of the map div,
 *  it's not really related to routing or openlayers
 ************************************************************************************/

var leftDivWidth;
var resizeMapWindow = function resizeMap() {
	window.windowWidth = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
	window.windowHeight = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
	document.getElementById("map").style.height = window.windowHeight - 10; // 10 because body padding
	document.getElementById("leftDiv").style.height = window.windowHeight - 10; // 10 because body padding
	document.getElementById("turnByTurn").style.height = window.windowHeight - 20 - document.getElementById('searchForm').offsetHeight - document.getElementById('sizeSwitch').offsetHeight;
	leftDivWidth = parseInt(document.getElementById("leftDiv").offsetWidth) + 10;
	document.getElementById("leftDiv").style.width = leftDivWidth - 10;
	document.getElementById("map").style.width = window.windowWidth - leftDivWidth - 20;
	document.getElementById("map").style.left = leftDivWidth + 10;
};

window.onresize = resizeMapWindow;
// This function is for showing / hiding the left DIV
var sizeSwitchFunction;
function sizeSwitch() {
  document.getElementById("sizeSwitch").onclick = null;
  if (leftDivWidth > 100) {
    document.getElementById('sizeSwitch').disabled="disabled";
    sizeSwitchFunction = window.setInterval("changeLeftDivSize(20)", 10);
  } else {
    document.getElementById('sizeSwitch').disabled="disabled";
    sizeSwitchFunction = window.setInterval("changeLeftDivSize(200)", 10);
  }
}
function changeLeftDivSize(targetSize) {
  var lw = parseInt(document.getElementById("leftDiv").style.width);
  if (lw < 200) {
    document.getElementById("turnByTurn").style.display = "none";
    document.getElementById("searchForm").style.display = "none";
  }
  if (targetSize < lw) {
    document.getElementById("leftDiv").style.width = lw - 10;
  } else {
    document.getElementById("leftDiv").style.width = lw + 10;
  }
	document.getElementById("map").style.width = window.windowWidth - lw - 10;
	document.getElementById("map").style.left = lw;
	if (lw >= 200) {
    document.getElementById("turnByTurn").style.display = "block";
    document.getElementById("searchForm").style.display = "block";
  }
  if (lw == targetSize) {
    window.clearInterval(sizeSwitchFunction);
    resizeMapWindow();
    map.updateSize();
    document.getElementById("sizeSwitch").onclick = sizeSwitch;
  }
}


/************************************************************************************
 *  Helper functions
 ************************************************************************************/

/**
 * Finally a helper method for detecting empty objects
 *
 * @return true if the object is empty, false otherwise
 */
function isEmpty(obj) {
  for(var prop in obj) {
    if(obj.hasOwnProperty(prop))
      return false;
  }
  return true;
}

/**
 * Helper function for not writing the same transformation over and over again
 *
 * @return this LonLat object in the projection of the OpenStreetMap layer
 */
OpenLayers.LonLat.prototype.tf = function() {
  if (this.tf_result == undefined) {
    this.tf_result = this.clone().transform(projWSG84, projSpheMe);
    this.tf_result.rtf_result = this;
  }
  return this.tf_result;
};

/**
 * Helper function for not writing the same transformation over and over again
 *
 * @return this LonLat object in the WSG 1984 projection
 */
OpenLayers.LonLat.prototype.rtf = function() {
  if (this.rtf_result == undefined) {
    this.rtf_result = this.clone().transform(projSpheMe, projWSG84);
    this.rtf_result.tf_result = this;
  }
  return this.rtf_result;
};