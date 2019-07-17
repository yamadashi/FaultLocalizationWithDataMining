INPUT:=data/testdata
#OUTPUT:=
MINSUP:=2
MINCONF:=0.5

run:
	javac -d ExploreConcepts/bin -cp ExploreConcepts/src ExploreConcepts/src/Main.java
	java -cp ExploreConcepts/bin Main $(INPUT) $(MINSUP) $(MINCONF)

clean:
	- rm ExploreConcepts/bin/*.class
	#- rm $(OUTPUT)