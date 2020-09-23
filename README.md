# pinframework
A(nother) minimal Java web framework, based on [com.sun.net.httpserver](http://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/package-summary.html)



## FAQ


### Why a new micro framework?
Everybody else was creating his/her own, so why cant I?

### I heard you are not supposed to use anything in com.sun.*, so why are you?
Package com.sun.net.httpserver has `@Exported annotation, and according to [Oracle](https://docs.oracle.com/javase/8/docs/jdk/api/javac/tree/jdk/Exported.html)


>If in one release a type or package is @Exported(true), in a subsequent major release such a type or package can transition to @Exported(false).
>
>If a type or package is @Exported(false) in a release, it may be removed in a subsequent major release. 


So it will take 2 major releases to be deleted, that is at least 4 years in normal java time

### Is this thing used in production environment anywhere?
Of course not! I tried at work once, and my boss almost fired me.
I'm going to try again soon.

### Why you write like a ten years old kid?
English is not my language.


---
Written with the help of [(GitHub-Flavored) Markdown Editor](https://jbt.github.io/markdown-editor)