#!/bin/bash
cd /home/ec2-user
aws s3 cp s3://showtime-bucket/ShowTime-0.0.1-SNAPSHOT.jar .
java -jar ShowTime-0.0.1-SNAPSHOT.jar