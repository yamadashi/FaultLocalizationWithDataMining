targetPG:=SomeMutants
output:=data.csv

run:
	testdata/testdata.sh
	./compose.sh $(targetPG) $(output)
	cp $(output) ../FindRules/ExploreConcepts/data/$(output)

create/test:
	testdata/testdata.sh

create/context:
	./compose.sh $(targetPG) $(output)
	cp $(output) ../FindRules/ExploreConcepts/data/$(output)

clean:
	-rm $(output)
	-rm testdata/testdata