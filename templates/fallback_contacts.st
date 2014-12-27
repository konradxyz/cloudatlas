fallback_contacts (path, address, contacts, message) ::= <<
$header(path, address)$
<div>
&nbsp; $message$
</div>
<div class="section">
<p> Fallback contacts: </p>
<p> Use ";" as separator. </p>
<p> Example: 192.168.1.4;192.168.1.6 </p>
<p>
<form>
<textarea name="contacts">
$contacts$
</textarea>
</p>
<input type="submit" value="Update">
<input type="hidden" name="update" value="1">
</form>
</div>
$footer()$
>>