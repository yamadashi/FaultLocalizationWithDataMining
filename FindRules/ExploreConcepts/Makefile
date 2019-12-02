INPUT:=data/data.csv
#OUTPUT:=
MINSUP:=30
MINCONF:=0.5
INTERVAL:=5

build:
	javac -d bin/ -cp src src/main/Main.java

exec:
	java -cp bin main.Main $(INPUT) $(MINSUP) $(MINCONF)

run:
	javac -d bin/main -cp src src/main/Main.java
	java -cp bin main.Main $(INPUT) $(MINSUP) $(MINCONF)

run/test:
	$(MAKE) run INPUT=data/testdata MINSUP=2

clean:
	- rm bin/main/*.class
	#- rm $(OUTPUT)

build/mem:
	javac -d bin/ -cp src src/memmonitor/Main.java

exec/mem:
	java -cp bin memmonitor.Main $(INPUT) $(MINSUP) $(MINCONF) $(INTERVAL)
