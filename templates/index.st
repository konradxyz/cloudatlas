index (path, address, zones) ::= <<
$header(path, address)$
$zones:{zone | 
<div class="zone section">
	<h4>$zone.name$</h4>
	<ul>
		$zone.attrs: { attr | <li>$attr$</li>}$
	</ul> 
</div>
}$
$footer()$
>>