# Design

* Java 11 and only server supported is com.sun.httpserver in jdk.httpserver module
* No reflection used
* Functionality
  * get/post/put/patch/delete verbs
  * read and write json
  * upload and download files
  * generate txt
  * download static content
  * path params
  
* All class names start with Pin
* All exceptions are runtime and extend from PinRuntimeException
* Any initialization problem and you get a PinInitializationException
* No serialVersionUID in any place


