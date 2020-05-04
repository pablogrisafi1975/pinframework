# Design

* Java 11 and only server supported is com.sun.httpserver in jdk.httpserver module
* No reflection used
* Functionality
  * get/post/put/patch/delete verbs
  * path params
  * read and write json
  * read upload form
  * upload and download files
  * generate txt
  * download static content
  
* All class names start with Pin
* All exceptions are runtime and extend from PinRuntimeException
* Any initialization problem and you get a PinInitializationException
* No serialVersionUID in any place
* By default, json output is assumed with status = 200, if object is null or optional.empty status = 404
* las slash is ignored: /users/ or /users route to the same handler
* Use accept header to determine render!
* register mime type for files
* On name collision in static files: external files > resource files > webjar files, soy you can fix stuff easily

