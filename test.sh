#/bin/sh
curl -X POST -d @input.txt "http://localhost:8080/compute" --header "Content-Type: application/json" --header "Accept: application/json"

curl -X POST -d @input_error.txt "http://localhost:8080/compute" --header "Content-Type: application/json" --header "Accept: application/json"
