plot (path, address, zone_path, attribute, refresh_period_ms, length_ms) ::= <<
$header(path, address)$
<h2 class="plot_header"> Plot for an attribute <b> "$attribute$" </b> in zone <b> "$zone_path$" </b>: </h2>
<div id="plot_placeholder"></div>
<script type="text/javascript">
	plot("plot_placeholder", "$zone_path$", "$attribute$", $refresh_period_ms$, $length_ms$);
</script>
$footer()$
>>