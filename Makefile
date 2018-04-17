all: compile
	
compile:
	javac -d compiled_source -cp src src/peer/PeerProcess.java

clean:
	find compiled_source -type f -iname \*.class -delete
	rm log_peer_100*
	rm DEBUG.100*
	
peer:
	rm -r peer_1002
	# rm -r peer_1003
	rm -r peer_1004
	rm -r peer_1005
