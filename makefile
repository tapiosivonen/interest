EuriborChartServlet: InterestChart
	javac -sourcepath src/ -d bin/ -classpath "lib/jsoup-1.10.3.jar:lib/xchart-3.4.0.jar:lib/servlet-api.jar" src/interest/control/EuriborChartServlet.java
	cp -r res/EuriborChartServlet/* bin/
	cd bin ; jar cfm ../jar/EuriborChartServlet.war ../res/EuriborChartServlet.MF WEB-INF/web.xml interest/*.class interest/image/*.class interest/control/*.class ; cd .. ;

InterestChart: BOFEuriborFetcher
	javac -sourcepath src/ -d bin/ -classpath "lib/jsoup-1.10.3.jar:lib/xchart-3.4.0.jar" src/interest/image/InterestChart.java
	cd bin ; jar cfm ../jar/InterestChart.jar ../res/InterestChart.MF interest/*.class interest/image/*.class ; cd ..

BOFEuriborFetcher:
	javac -sourcepath src/ -d bin/ -classpath lib/jsoup-1.10.3.jar src/interest/BOFEuriborFetcher.java
	cd bin ; jar cfm ../jar/BOFEuriborFetcher.jar ../res/BOFEuriborFetcher.MF interest/*.class ; cd .. ;

clean:
	if [ -d "bin/" ] ; then if [ -f "bin/*" ] ; then rm -r bin/* ; fi ; else mkdir bin ; fi ; 
	if [ -d "jar/" ] ; then if [ -f "jar/*" ] ; then rm -r jar/* ; fi ; else mkdir jar ; fi
