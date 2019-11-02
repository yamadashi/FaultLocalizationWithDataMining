targetPG:=SomeMutants
output:=data.csv

run:
	testdata/testdata.sh
	./compose.sh $(targetPG) $(output)

create/test:
	testdata/testdata.sh

create/context:
	./compose.sh $(targetPG) $(output)

clean:
	-rm $(output)
	-rm testdata/testdata