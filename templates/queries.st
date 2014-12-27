queries (path, address, message, queries) ::= <<
$header(path, address)$
<div>
&nbsp; $message$
</div>

<form class="new_query_form section" method="POST">
<p> New query: </p>
<p>
<p> Name </p>
<input type="text" name="name">
</p>
<p>
<p> Query </p>
<textarea name="query">
</textarea>
</p>
<p>
<input type="hidden" name="install" value="1">
<input type="submit" value="Install">
</p>
</form>

<div class="section">
$queries:{query | 
<div class="query">
	<form method="POST">
		<input type="hidden" name="remove" value="1">
		<input type="hidden" name="name" value="$query.name$">
		<input type="submit" value="Remove"> $query.description$ 
	</form>
</div>
}$
</div>
$footer()$
>>