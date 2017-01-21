Instructions to run the test cases

i.  make is used to compile. 
ii. Following commands are used to execute your program with 4 input files.

timeout 10 java hashtagcounter input_1000.txt 
timeout 10 java hashtagcounter input_10000.txt 
timeout 15 java hashtagcounter input_100000.txt 
timeout 25 java hashtagcounter input_1000000.txt

Difference is checked with the each produced output file and our answer file using the diff command.

diff -w -B -s output_1000.txt output_file.txt | wc -l 
diff -w -B -s output_10000.txt output_file.txt | wc -l 
diff -w -B -s output_100000.txt output_file.txt | wc -l 
diff -w -B -s output_1000000.txt output_file.txt | wc -l

For more information please refer to the project report: ADSProjectReport_DeshpandeVinayak.pdf
