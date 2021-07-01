compile:
	@echo -en '--- Compilazione ---\n\n'
	javac ./SmartParking/Client/Client.java
	javac -cp '.:gson.jar' ./SmartParking/Server/ThreadPooledServer.java

runServer:
	@echo -en '--- Esecuzione Server --- \n\n'
	java -cp '.:gson.jar' SmartParking.Server.ThreadPooledServer

runClient:
	@echo -en '--- Esecuzione Client --- \n\n'
	java SmartParking.Client.Client

clean:
	$(RM) ./SmartParking/Server/*.class
	$(RM) ./SmartParking/Client/*.class
	$(RM) ./error.log
