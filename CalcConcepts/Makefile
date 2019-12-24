INPUT:=data/simple.txt
FILTER:=none # support, lift, all
MINSUP:=0

build:
	javac -d bin -cp src src/Main.java

exec:
	java -cp bin Main $(INPUT) $(FILTER) $(MINSUP)

run:
	$(MAKE) build
	$(MAKE) exec

clean:
	rm bin/*