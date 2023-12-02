#!/bin/sh

current_dir=$(pwd)

cd "$current_dir/Site_1"
javac Update.java
javac Site1.java

cd "$current_dir/Site_2"
javac Update.java
javac Site2.java

cd "$current_dir/Site_3"
javac Update.java
javac Site3.java

cd "$current_dir/AcceptTransactions"
javac Update.java
javac AcceptTransactions.java

cd "$current_dir/sessionNumberLocalStorage"

echo 0 > Site1.txt
echo 0 > Site2.txt
echo 0 > Site3.txt

cd "$current_dir/backlogNSVectors"

echo 1=1,2=1,3=1 > Site1.txt
echo 1=1,2=1,3=1 > Site2.txt
echo 1=1,2=1,3=1 > Site3.txt