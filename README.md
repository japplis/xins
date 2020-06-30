# xins
### XML Interface for Network Services

A Java Framework to create XML-RPC APIs

#### Description:
* XINS is a web application framework.
* XINS is designed to be able to create web applications based on defined specifications.
* XINS is based on a simple way to send requests (using URLs) and handle result (Simple XML format).
* XINS generates a set of HTML pages from the specification and some forms to test the application.
* XINS generates Java code to invoke the web application and to develop it.
* XINS generates WSDL, OpenDocument format, unit tests and stubs.
* XINS also detects if the parameters are conform to the specification.
* XINS includes concepts like load balancing, fail over, logging, security, properties and statistics.
* XINS accepts a lot of protocols: REST, SOAP, XML-RPC, JSON, JSON-RPC, Front-end Framework, ....

#### Documentation:
* XINS User Guide in [HTML](https://www.xins.org/docs/index.html) and [PDF](docs/XINSGuide.pdf).
* XINS protocol in [HTML](https://www.xins.org/docs/protocol/index.html) and [PDF](docs/protocol/XINSProtocol.pdf).
* [Javadoc](https://www.xins.org/docs/javadoc/index.html).
* Logdoc for the [client](https://www.xins.org/docs/logdoc/client/index.html),
		[common](https://www.xins.org/docs/logdoc/common/index.html) and
		[server](https://www.xins.org/docs/logdoc/server/index.html).
* [Last changes](CHANGES), [COPYRIGHT](Copyright).

#### Other projects:
* [XINS documentation](../xins-docs) Source code for the documentation
* [XINS examples](../xins-examples) Examples of API's client and server side.
* [XINS GUI](../xins-gui) User interface for creating and viewing API's
* [XINS Website](../xins-website) Website https://www.xins.org

#### Quick start:
If you have chosen "Compile and run demo" with the Windows installer, you can directly go to the specdocs (https://www.xins.org/demo/xins-project/build/specdocs/myproject/index.html)
* Check that the Java Development Kit and [Apache Ant](https://ant.apache.org/) are properly installed.
* Set the _XINS_HOME_ environment variable to the installed xins directory.
* Add %XINS_HOME%\bin to your PATH environment variable.
* In the _demo\xins-projects_ directory execute _xins specdocs-myproject_ to generated the HTML pages containing the specification and the test forms.
* Compile and run the API with
		_xins -Dorg.xins.server.config=..\xins.properties run-myproject_
* Go to the web page demo\xins-project\build\specdocs\myproject\index.html
More detailed information can be found in the [documentation](docs/index.html).

#### Links:
* [Web site](https://www.xins.org).
* [Online demo](http://xins.sourceforge.net/demo.html).
* [Download page](http://sourceforge.net/projects/xins/files/).
