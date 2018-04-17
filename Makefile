all: compile
	
compile:
	javac -d compiled_source -cp src src/peer/PeerProcess.java

clean:
	find compiled_source -type f -iname \*.class -delete
	rm log_peer_100*
	rm DEBUG.100*
