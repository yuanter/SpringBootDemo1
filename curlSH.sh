#!/bin/bash
curl http://ip:8080/mi?phoneNumber=手机号码\&password=密码\&steps=$[$[RANDOM%范围步数]+起始步数]