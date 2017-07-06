EuriborChartServlet: InterestChart
	javac -sourcepath src/ -d bin/ -classpath "lib/jsoup-1.10.3.jar:lib/xchart-3.4.0.jar:lib/servlet-api.jar" src/interest/control/EuriborChartServlet.java
	cp -r res/EuriborChartServlet/* bin/
	jar cfm EuriborChartServlet.war res/EuriborChartServlet.MF -C bin WEB-INF/web.xml interest/*.class interest/image/*.class interest/control/*.class

InterestChart: BOFEuriborFetcher
	javac -sourcepath src/ -d bin/ -classpath "lib/jsoup-1.10.3.jar:lib/xchart-3.4.0.jar" src/interest/image/InterestChart.java
	jar cfm InterestChart.jar res/InterestChart.MF -C bin interest/*.class interest/image/*.class

BOFEuriborFetcher:
	javac -sourcepath src/ -d bin/ -classpath lib/jsoup-1.10.3.jar src/interest/BOFEuriborFetcher.java
	jar cfm BOFEuriborFetcher.jar res/BOFEuriborFetcher.MF -C bin interest/*.class

clean:
	rm -r bin/*
	mkdir bin
