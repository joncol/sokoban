#!/usr/bin/env bash

app_name="sokoban"

lein do clean, uberjar
scp target/$app_name.jar jcodev.eu:/var/www/$app_name/app
ssh -t jcodev.eu "sudo supervisorctl restart $app_name"
