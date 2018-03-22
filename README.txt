compile -
javac -d /path/to/destination_dir -cp src src/peer/PeerProcess.java

(Note - current working dir assumed to be inside BitTorrent2)
(destination_dir will have to exist)

Run -
From inside /path/to/destination_dir run
java -cp . peer/PeerProcess 1001

example -
Shobhits-MacBook-Pro:BitTorrent2 shobhit$ pwd
/Users/shobhit/Documents/workspace/BitTorrent2

Shobhits-MacBook-Pro:BitTorrent2 shobhit$ javac -d ~/Desktop/bin/ -cp src src/peer/PeerProcess.java

Shobhits-MacBook-Pro:bin shobhit$ cd /Users/shobhit/Desktop/bin

Shobhits-MacBook-Pro:bin shobhit$ java -cp . peer/PeerProcess