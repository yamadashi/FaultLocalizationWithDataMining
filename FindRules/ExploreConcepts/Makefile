INPUT:=data/data.csv
#OUTPUT:=
MINSUP:=30
MINCONF:=0.5

build:
	javac -d ExploreConcepts/bin -cp ExploreConcepts/src ExploreConcepts/src/Main.java

exec:
	java -cp ExploreConcepts/bin Main $(INPUT) $(MINSUP) $(MINCONF)

run:
	javac -d ExploreConcepts/bin -cp ExploreConcepts/src ExploreConcepts/src/Main.java
	java -cp ExploreConcepts/bin Main $(INPUT) $(MINSUP) $(MINCONF)

run/test:
	$(MAKE) run INPUT=data/testdata MINSUP=2

clean:
	- rm ExploreConcepts/bin/*.class
	#- rm $(OUTPUT)