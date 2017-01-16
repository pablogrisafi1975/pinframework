//based on https://gist.github.com/topliceanu/1537847#file-micro-mustache-js-L28
//based on John Resig - http://ejohn.org/ - MIT Licensed
tmpl = function tmpl(str, data){
	var fn = new Function("obj",
		"var p=[],print=function(){p.push.apply(p,arguments);};" +
		
		// Introduce the data as local variables using with(){}
	        "with(JSON.parse(obj)){p.push('" +
			
		// Convert the template into pure JavaScript
			str
			  .replace(/[\r\t\n]/g, " ")
			  .split("{{").join("\t")
			  .replace(/((^|\}\})[^\t]*)'/g, "$1\r")
			  .replace(/\t=(.*?)\}\}/g, "',$1,'")
			  .split("\t").join("');")
			  .split("}}").join("p.push('")
			  .split("\r").join("\\'")
			+ "');}return p.join('');");
		
	return fn( data );
  }
print(tmpl.toString());