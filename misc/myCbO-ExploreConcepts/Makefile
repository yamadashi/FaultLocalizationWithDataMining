INPUT:=data/testdata
#OUTPUT:=
MINSUP:=2
MINCONF:=0.5

run:
	javac -d bin -cp src src/Main.java
	java -cp bin Main $(INPUT) $(MINSUP) $(MINCONF)

clean:
	- rm bin/*.class
	#- rm $(OUTPUT)