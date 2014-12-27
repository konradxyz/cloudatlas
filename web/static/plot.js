
function plot(placeholderId, path, attribute, interval, howLong) {
	var points = []
	var selector = "#" + placeholderId
	var lastTimestamp = -1
	var options = {
		xaxis: { mode: "time" }
	}
	$(document).ready(function() {
		$(selector).width(800);
		$(selector).height(300);
		$.plot(selector, [points], options);
		setInterval(function() {
			$.ajax("../getattr", {
				data: {
					path: path,
					attribute: attribute
				},
				success: function(object) {
					if ( object.timestamp > lastTimestamp ){
						lastTimestamp = object.timestamp
						points.push([object.timestamp, object.value])
						points = points.filter(function(point) {
							return point[0] > lastTimestamp - howLong
						})
						$.plot(selector, [points], options);
					}
				}	
			})
		}, interval)
	});
}