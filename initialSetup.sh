#!/bin/sh

cd /Users/akshay/IdeaProjects/SiteRecoveryROWAA/Site_1
javac Update.java
javac Site1.java

cd /Users/akshay/IdeaProjects/SiteRecoveryROWAA/Site_2
javac Update.java
javac Site2.java

cd /Users/akshay/IdeaProjects/SiteRecoveryROWAA/Site_3
javac Update.java
javac Site3.java

cd /Users/akshay/IdeaProjects/SiteRecoveryROWAA/AcceptTransactions
javac Update.java
javac AcceptTransactions.java

cd /Users/akshay/IdeaProjects/SiteRecoveryROWAA/sessionNumberLocalStorage

echo 0 > Site1.txt
echo 0 > Site2.txt
echo 0 > Site3.txt