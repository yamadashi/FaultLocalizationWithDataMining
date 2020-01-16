INPUT:=data/lift-cut.txt
MINCOOCC:=50

build:
	javac -d bin -cp src src/Main.java

exec:
	java -cp bin Main $(INPUT) $(MINCOOCC)

run:
	$(MAKE) build
	$(MAKE) exec

clean:
	rm bin/*